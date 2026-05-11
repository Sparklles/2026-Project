package com.example.productmanagement.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.controller.Result;
import com.example.productmanagement.dto.BookDTO;
import com.example.productmanagement.dto.PageQueryDTO;
import com.example.productmanagement.entity.BookCategory;
import com.example.productmanagement.entity.BookTag;
import com.example.productmanagement.service.BookManageService;
import com.example.productmanagement.service.impl.ProductVectorSyncService;
import com.example.productmanagement.vo.BookDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台-商品管理控制器
 */
@RestController
@RequestMapping("/api/admin/books")
public class BookAdminController {

    @Autowired
    private BookManageService bookManageService;
    @Autowired
    private ProductVectorSyncService productVectorSyncService;

    /**
     * (2) 商品信息维护: 录入新书并绑定标签
     */
    @PostMapping
    public Result<?> addBook(@RequestBody BookDTO bookDTO) {
        bookManageService.addBookWithTags(bookDTO);
        return Result.success("书籍录入成功，已保存为草稿(下架)状态");
    }

    /**
     * (2) 商品信息维护: 修改书籍信息并更新标签
     */
    @PutMapping
    public Result<?> updateBook(@RequestBody BookDTO bookDTO) {
        if (bookDTO.getId() == null) {
            return Result.error(400, "书籍ID不能为空");
        }
        bookManageService.updateBookWithTags(bookDTO);
        return Result.success("书籍信息更新成功");
    }

    /**
     * (1) 商品生命周期管理: 上架/下架书籍
     * 使用 PUT /api/admin/books/{id}/status?status=1 格式
     */
    @PutMapping("/{id}/status")
    public Result<?> changeBookStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        boolean success = bookManageService.changeBookStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error(500, "状态更新失败");
    }

    /**
     * (1) 商品生命周期管理: 安全删除书籍 (有历史订单会触发 Service 层抛出异常并拦截)
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteBook(@PathVariable("id") Long id) {
        bookManageService.deleteBookSafe(id);
        return Result.success("书籍逻辑删除成功");
    }

    /**
     * 获取分类字典 (给前端下拉框用)
     */
    @GetMapping("/categories/listAll")
    public Result<List<BookCategory>> listAllCategories() {
        return Result.success(bookManageService.listAllCategories());
    }

    /**
     * 获取标签字典 (给前端下拉框用)
     */
    @GetMapping("/tags/listAll")
    public Result<List<BookTag>> listAllTags() {
        return Result.success(bookManageService.listAllTags());
    }

    /**
     * 前台分页获取商品列表
     */
    /**
     * 前台分页获取商品列表 (修复空指针问题)
     */
    @GetMapping("/page")
    public Result<IPage<BookDetailVO>> getBookPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        // 1. 手动将接收到的参数封装到 DTO 中，避免 null
        PageQueryDTO queryDTO = new PageQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        queryDTO.setKeyword(keyword);

        // 2. 调用 Service
        IPage<BookDetailVO> pageResult = bookManageService.getAdminBookPage(queryDTO, categoryId);
        return Result.success(pageResult);
    }

    /**
     * (1) 商品生命周期管理: 批量上架/下架书籍
     * 接收一个 ID 数组，将它们的状态统一更新
     */
    @PutMapping("/batch-status")
    public Result<?> batchChangeStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请至少选择一条记录");
        }
        boolean success = bookManageService.batchChangeStatus(ids, status);
        return success ? Result.success("批量状态更新成功") : Result.error(500, "批量状态更新失败");
    }

    // ================== 分类字典维护接口 ==================

    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody BookCategory category) {
        bookManageService.addCategory(category);
        return Result.success("新增分类成功");
    }

    @PutMapping("/categories")
    public Result<?> updateCategory(@RequestBody BookCategory category) {
        bookManageService.updateCategory(category);
        return Result.success("修改分类成功");
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable("id") Long id) {
        bookManageService.deleteCategory(id);
        return Result.success("删除分类成功");
    }

    // ================== 标签字典维护接口 ==================

    @PostMapping("/tags")
    public Result<?> addTag(@RequestBody BookTag tag) {
        bookManageService.addTag(tag);
        return Result.success("新增标签成功");
    }

    @PutMapping("/tags")
    public Result<?> updateTag(@RequestBody BookTag tag) {
        bookManageService.updateTag(tag);
        return Result.success("修改标签成功");
    }

    @DeleteMapping("/tags/{id}")
    public Result<?> deleteTag(@PathVariable("id") Long id) {
        bookManageService.deleteTag(id);
        return Result.success("删除标签成功");
    }

    /**
     * 🌟 新增：手动强制重构 AI 向量知识库
     * 给前端管理员面板的按钮使用
     */
    @GetMapping("/sync-ai")
    public Result<?> forceSyncAiKnowledgeBase() {
        // 因为大模型调用耗时较长，放入新线程异步执行，立刻给前端返回成功响应
        new Thread(() -> {
            try {
                productVectorSyncService.syncAllToVectorStore();
            } catch (Exception e) {
                // 此处可接邮件告警等机制
                e.printStackTrace();
            }
        }).start();

        return Result.success("AI 知识库重构指令已下发，大模型正在后台默默学习中，请稍后测试。");
    }


}
