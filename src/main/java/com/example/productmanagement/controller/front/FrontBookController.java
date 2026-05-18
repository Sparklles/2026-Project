package com.example.productmanagement.controller.front;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.BookQueryDTO;
import com.example.productmanagement.entity.BookCategory;
import com.example.productmanagement.service.FrontBookService;
import com.example.productmanagement.vo.ProductDetailVO;
import com.example.productmanagement.vo.SearchBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/front/book")
public class FrontBookController {

    @Autowired
    private FrontBookService frontBookService;

    @GetMapping("/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable("id") Long id) {
        try {
            ProductDetailVO detail = frontBookService.getProductDetail(id);
            return Result.success(detail);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }
    /**
     * 大搜索框模糊查询接口
     */
    @GetMapping("/search")
    public Result<IPage<SearchBookVO>> searchBooks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        Page<SearchBookVO> page = new Page<>(current, size);
        IPage<SearchBookVO> result = frontBookService.searchBooks(page, keyword, sortField, sortOrder);
        return Result.success(result);
    }

    /**
     * 前台书籍高级筛选接口
     */
    @GetMapping("/list")
    public Result<IPage<SearchBookVO>> listBooks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            BookQueryDTO queryDto) {

        Page<SearchBookVO> page = new Page<>(current, size);
        IPage<SearchBookVO> result = frontBookService.queryBooks(page, queryDto);
        return Result.success(result);
    }

    /**
     * 获取所有书籍分类
     */
    @GetMapping("/categories")
    public Result<List<BookCategory>> getCategories() {
        List<BookCategory> categories = frontBookService.getAllCategories();
        return Result.success(categories);
    }
}
