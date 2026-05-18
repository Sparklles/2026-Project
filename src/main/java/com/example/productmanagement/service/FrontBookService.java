package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.dto.BookQueryDTO;
import com.example.productmanagement.entity.BookCategory;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.vo.ProductDetailVO;

import com.example.productmanagement.vo.ProductDetailVO;
import com.example.productmanagement.vo.SearchBookVO;

import java.util.List;

public interface FrontBookService extends IService<BookInfo> {

    /**
     * 前台高级筛选分页查询
     * @param page 分页对象
     * @param queryDto 查询条件
     * @return 分页结果（VO 含标签列表）
     */
    IPage<SearchBookVO> queryBooks(IPage<SearchBookVO> page, BookQueryDTO queryDto);

    /**
     * 获取所有书籍分类（按排序字段升序）
     */
    List<BookCategory> getAllCategories();

    /**
     * 首页大搜索框关键词搜索（分页）
     * @param page    分页对象
     * @param keyword 搜索关键词
     * @param sortField 排序字段
     * @param sortOrder 排序方向
     * @return 分页结果
     */
    IPage<SearchBookVO> searchBooks(IPage<SearchBookVO> page, String keyword, String sortField, String sortOrder);

    /**
     * 获取前台商品详情（包含基础信息、标签、评价聚合）
     * @param productId 商品主键ID
     * @return 完整商品详情视图对象
     */
    ProductDetailVO getProductDetail(Long productId);
}
