package com.example.productmanagement.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.productmanagement.dto.BookDTO;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.entity.*;
import com.example.productmanagement.mapper.*;
import com.example.productmanagement.service.BookManageService;
import com.example.productmanagement.vo.BookDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookManageServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookManageService {

    @Autowired
    private BookTagRelationMapper bookTagRelationMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private BookCategoryMapper categoryMapper;

    @Autowired
    private BookTagMapper tagMapper;

    @Autowired
    private BookImageMapper bookImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBookWithTags(BookDTO dto) {
        // 1. 拷贝基础属性并保存书籍
        BookInfo book = new BookInfo();
        BeanUtils.copyProperties(dto, book);

        // 🌟 提取封面图：默认取传过来的第一张图片
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            book.setCoverImageUrl(dto.getImageUrls().get(0));
        }

        book.setStatus(0); // 默认下架状态
        book.setSales(0);
        this.save(book); // MyBatis-Plus 会自动将生成的 ID 回填到 book 对象中

        // 2. 处理多级分类与标签体系 (Middle) - 绑定标签
        saveTagRelations(book.getId(), dto.getTagIds());
        // 🌟 处理多图画廊
        saveImages(book.getId(), dto.getImageUrls());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBookWithTags(BookDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("修改书籍时ID不能为空");
        }

        // 1. 拷贝属性执行更新 (MyBatis-Plus 的 updateById 会自动携带 @Version 乐观锁字段进行校验)
        BookInfo book = new BookInfo();
        BeanUtils.copyProperties(dto, book);

        // 🌟 提取封面图：默认取传过来的第一张图片
        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            book.setCoverImageUrl(dto.getImageUrls().get(0));
        } else {
            book.setCoverImageUrl(""); // 清空封面
        }

        boolean updateSuccess = this.updateById(book);
        if (!updateSuccess) {
            // 乐观锁拦截：如果影响行数为0，说明数据已被其他管理员修改
            throw new RuntimeException("并发冲突：该书籍信息已被他人修改，请刷新页面后重试！");
        }

        // 2. 全量更新标签 (先删后增模式)
        LambdaQueryWrapper<BookTagRelation> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(BookTagRelation::getBookId, book.getId());
        bookTagRelationMapper.delete(deleteWrapper);

        saveTagRelations(book.getId(), dto.getTagIds());

        // 🌟 处理图片 (先删后增)
        LambdaQueryWrapper<BookImage> deleteImgWrapper = new LambdaQueryWrapper<>();
        deleteImgWrapper.eq(BookImage::getBookId, book.getId());
        bookImageMapper.delete(deleteImgWrapper);
        saveImages(book.getId(), dto.getImageUrls());
    }

    @Override
    public boolean changeBookStatus(Long bookId, Integer status) {
        BookInfo book = new BookInfo();
        book.setId(bookId);
        book.setStatus(status);
        return this.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBookSafe(Long bookId) {
        // 1. 校验历史订单关联 (防误删机制)
        LambdaQueryWrapper<OrderItem> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(OrderItem::getBookId, bookId);
        Long orderCount = orderItemMapper.selectCount(orderQuery);

        if (orderCount != null && orderCount > 0) {
            throw new RuntimeException("操作拒绝：该书籍存在 [" + orderCount + "] 笔历史订单记录，为保证数据完整性，仅支持下架操作！");
        }

        // 2. 执行逻辑删除 (由实体类的 @TableLogic 注解保障底层执行的是 UPDATE is_deleted=1)
        this.removeById(bookId);
    }

    /**
     * 辅助方法：批量插入标签关联关系 (已优化为 Mapper 层的一条 SQL 批量插入)
     */
    private void saveTagRelations(Long bookId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            // 将前端传来的 ID 列表转换为 实体对象 列表
            List<BookTagRelation> relations = tagIds.stream().map(tagId -> {
                BookTagRelation relation = new BookTagRelation();
                relation.setBookId(bookId);
                relation.setTagId(tagId);
                return relation;
            }).collect(Collectors.toList());

            // 调用我们刚才手写的批量插入方法
            bookTagRelationMapper.insertBatch(relations);
        }
    }


    @Override
    public List<BookCategory> listAllCategories() {
        // 🌟 按照 sortOrder 降序排列（数字越大越靠前），如果相同则按创建时间降序
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BookCategory::getSortOrder)
                .orderByDesc(BookCategory::getCreateTime);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<BookTag> listAllTags() {
        // 🌟 同样按照 sortOrder 降序排列
        LambdaQueryWrapper<BookTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BookTag::getSortOrder)
                .orderByDesc(BookTag::getCreateTime);
        return tagMapper.selectList(wrapper);
    }

    @Override
    public IPage<BookDetailVO> getAdminBookPage(PageQueryDTO queryDTO, Long categoryId) {
        try {
            long current = (queryDTO.getCurrent() != null) ? queryDTO.getCurrent() : 1L;
            long size = (queryDTO.getSize() != null) ? queryDTO.getSize() : 10L;
            Page<BookDetailVO> page = new Page<>(current, size);

            // 🌟 1. 用 List 接收返回值，完美避开 ClassCastException 强转报错
            List<BookDetailVO> records = this.baseMapper.searchBooksDynamic(page, categoryId, queryDTO.getKeyword());

            // 🌟 2. 循环挂载标签
            // 在 getAdminBookPage 的循环挂载中补充：
            if (records != null && !records.isEmpty() && tagMapper != null) {
                for (BookDetailVO vo : records) {
                    if (vo != null && vo.getId() != null) {
                        vo.setTags(tagMapper.getTagsByBookId(vo.getId()));
                        // 查出该书的所有图片，组装进 VO，供前端回显
                        List<String> images = bookImageMapper.selectObjs(new LambdaQueryWrapper<BookImage>()
                                        .select(BookImage::getImageUrl)
                                        .eq(BookImage::getBookId, vo.getId())
                                        .orderByAsc(BookImage::getSortOrder))
                                .stream().map(Object::toString).collect(Collectors.toList());
                        vo.setImages(images);
                    }
                }
            }

            // 🌟 3. 手动把 List 塞进 Page 对象中并返回（此时 page 对象里已经有 total 总数了）
            page.setRecords(records);
            return page;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("分页组装数据时发生异常，请查看后端控制台");
        }
    }

    @Override
    public boolean batchChangeStatus(List<Long> ids, Integer status) {
        // 构造要更新的实体（只更新 status 字段）
        BookInfo book = new BookInfo();
        book.setStatus(status);

        // 构造 WHERE id IN (1, 2, 3...) 的条件
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BookInfo::getId, ids);

        // 执行批量更新
        return this.update(book, wrapper);
    }

    // ================== 分类字典维护 ==================
    @Override
    public void addCategory(BookCategory category) {
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(BookCategory category) {
        if (category.getId() == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 防误删校验：检查是否有书籍正在使用该分类
        LambdaQueryWrapper<BookInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookInfo::getCategoryId, id)
                .eq(BookInfo::getIsDeleted, 0); // 只查未删除的书籍

        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("操作拒绝：该分类下还有 [" + count + "] 本书籍，请先解除绑定后再删除！");
        }

        categoryMapper.deleteById(id);
    }

    // ================== 标签字典维护 ==================
    @Override
    public void addTag(BookTag tag) {
        tagMapper.insert(tag);
    }

    @Override
    public void updateTag(BookTag tag) {
        if (tag.getId() == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        tagMapper.updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
        // 防误删校验：检查该标签是否被书籍关联
        LambdaQueryWrapper<BookTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookTagRelation::getTagId, id);

        Long count = bookTagRelationMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("操作拒绝：该标签已被 [" + count + "] 本书籍使用，无法直接删除！");
        }

        tagMapper.deleteById(id);
    }
    //**********

    /**
     * 🌟 新增的辅助方法：保存图片列表
     */
    private void saveImages(Long bookId, List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);

                // 🌟 核心防空处理：如果前端传了 null 或空字符串，直接跳过，不存数据库
                if (url == null || url.trim().isEmpty()) {
                    continue;
                }

                BookImage img = new BookImage();
                img.setBookId(bookId);
                img.setImageUrl(url);
                img.setSortOrder(i);
                bookImageMapper.insert(img);
            }
        }
    }

}
