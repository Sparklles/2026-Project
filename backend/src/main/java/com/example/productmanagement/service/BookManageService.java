package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.dto.BookDTO;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.entity.BookCategory;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.entity.BookTag;
import com.example.productmanagement.vo.BookDetailVO;

import java.util.List;

public interface BookManageService extends IService<BookInfo> {

    /**
     * (2) 商品信息维护: 录入新书并绑定标签
     */
    void addBookWithTags(BookDTO dto);

    /**
     * (2) 商品信息维护: 修改书籍信息并更新标签 (包含乐观锁校验)
     */
    void updateBookWithTags(BookDTO dto);

    /**
     * (1) 商品生命周期管理: 上架/下架书籍
     */
    boolean changeBookStatus(Long bookId, Integer status);

    /**
     * (1) 商品生命周期管理: 安全删除书籍 (有历史订单则拒绝)
     */
    void deleteBookSafe(Long bookId);

    /**
     * 查询所有分类 (用于前端下拉框)
     */
    List<BookCategory> listAllCategories();

    /**
     * 查询所有标签 (用于前端下拉框)
     */
    List<BookTag> listAllTags();

    /**
     * 分页多条件查询书籍列表
     */
    IPage<BookDetailVO> getAdminBookPage(PageQueryDTO queryDTO, Long categoryId);

    boolean batchChangeStatus(List<Long> ids, Integer status);

    /**
     * 新增分类
     */
    void addCategory(BookCategory category);

    /**
     * 修改分类
     */
    void updateCategory(BookCategory category);

    /**
     * 删除分类 (带防误删校验)
     */
    void deleteCategory(Long id);

    // ================== 标签字典维护 ==================
    /**
     * 新增标签
     */
    void addTag(BookTag tag);

    /**
     * 修改标签
     */
    void updateTag(BookTag tag);

    /**
     * 删除标签 (带防误删校验)
     */
    void deleteTag(Long id);

}
