package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.common.Result;
import com.example.productmanagement.dto.CartAddRequest;
import com.example.productmanagement.dto.CartDeleteRequest;
import com.example.productmanagement.dto.CartUpdateRequest;
import com.example.productmanagement.service.CartService;
import com.example.productmanagement.vo.CartVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 分页查询用户购物车列表
     *
     * @param userId   用户ID
     * @param page     当前页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 分页购物车列表
     */
    @GetMapping("/list")
    public Result<IPage<CartVO>> getCartList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CartVO> cartPage = cartService.getCartList(userId, page, pageSize);
        return Result.success(cartPage);
    }

    /**
     * 查询用户购物车列表（不分页）
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @GetMapping("/list/all")
    public Result<List<CartVO>> getCartListAll(
            @NotNull(message = "用户id不能为空")
            @Positive(message = "用户id必需大于0")
            @RequestParam
            Long userId) {
        List<CartVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    /**
     * 根据购物车ID列表查询选中的购物车商品
     *
     * @param userId  用户ID
     * @param cartIds 购物车ID列表
     * @return 选中的购物车商品列表
     */
    @GetMapping("/list/selected")
    public Result<List<CartVO>> getCartListByIds(
            @RequestParam Long userId,
            @RequestParam List<Long> cartIds) {
        List<CartVO> cartList = cartService.getCartListByIds(userId, cartIds);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     *
     * @param request 添加请求参数
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addCart(@Valid @RequestBody CartAddRequest request) {
        cartService.addCart(request);
        return Result.success("添加成功");
    }

    /**
     * 更新购物车商品数量
     *
     * @param request 更新请求参数
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Void> updateCartQuantity(@Valid @RequestBody CartUpdateRequest request) {
        try {
            cartService.updateCartQuantity(request);
            return Result.success("更新数量成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 批量/单个删除购物车商品
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteCart(@RequestBody @Valid CartDeleteRequest request) {
        try {
            // 调用 Service 层的删除逻辑
            cartService.deleteCart(request);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 删除单个购物车商品
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{cartId}")
    public Result<Void> deleteCartById(
            @RequestParam Long userId,
            @PathVariable Long cartId) {
        cartService.deleteCartById(userId, cartId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除购物车商品
     *
     * @param request 删除请求参数
     * @return 操作结果
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteCartBatch(@Valid @RequestBody CartDeleteRequest request) {
        cartService.deleteCart(request);
        return Result.success("批量删除成功");
    }

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }

    /**
     * 根据书籍类别分页查询购物车列表
     *
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @param page       当前页码（默认1）
     * @param pageSize   每页大小（默认10）
     * @return 分页购物车列表
     */
    @GetMapping("/list/category")
    public Result<IPage<CartVO>> getCartListByCategory(
            @RequestParam Long userId,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CartVO> cartPage = cartService.getCartListByCategory(userId, categoryId, page, pageSize);
        return Result.success(cartPage);
    }

    /**
     * 根据书籍类别查询购物车列表（不分页）
     *
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @return 购物车列表
     */
    @GetMapping("/list/category/all")
    public Result<List<CartVO>> getCartListByCategoryAll(
            @RequestParam Long userId,
            @RequestParam Long categoryId) {
        List<CartVO> cartList = cartService.getCartListByCategory(userId, categoryId);
        return Result.success(cartList);
    }

    /**
     * 计算购物车商品总金额
     *
     * @param userId 用户ID
     * @return 总金额
     */
    @GetMapping("/total-amount")
    public Result<BigDecimal> calculateTotalAmount(@RequestParam Long userId) {
        BigDecimal totalAmount = cartService.calculateTotalAmount(userId);
        return Result.success(totalAmount);
    }

    /**
     * 查询购物车商品总数
     *
     * @param userId 用户ID
     * @return 商品总数
     */
    @GetMapping("/total-quantity")
    public Result<Integer> getCartTotalQuantity(@RequestParam Long userId) {
        Integer totalQuantity = cartService.getCartTotalQuantity(userId);
        return Result.success(totalQuantity);
    }
}
