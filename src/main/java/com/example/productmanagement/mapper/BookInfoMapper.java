package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.BookQueryDTO;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.vo.BookDetailVO;
import com.example.productmanagement.vo.SearchBookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 根据ID查询书籍详情（包含所属分类名称和绑定的所有标签）
     */
    BookDetailVO getBookDetailWithTags(@Param("bookId") Long bookId);

    /**
     * 前台多维动态查询书籍列表 (仅查询已上架的书籍)
     */
    IPage<BookDetailVO> searchBooksDynamic(IPage<BookDetailVO> page,
                                           @Param("categoryId") Long categoryId,
                                           @Param("keyword") String keyword);

    /**
     * 模糊搜索（支持分类+关键词）
     */
    IPage<SearchBookVO> searchBooks(IPage<SearchBookVO> page,
                                    @Param("categoryId") Long categoryId,
                                    @Param("keyword") String keyword);

    /**
     * 前台高级筛选分页查询（仅上架且未删除的书籍）
     * @param page 分页对象
     * @param query 查询条件 DTO
     * @return 分页结果（含标签名称字符串）
     */
    IPage<SearchBookVO> selectBookPageWithFilters(IPage<?> page, @Param("query") BookQueryDTO query);
}
