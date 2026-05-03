package com.example.productmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.CartAddRequest;
import com.example.productmanagement.dto.CartDeleteRequest;
import com.example.productmanagement.dto.CartUpdateRequest;
import com.example.productmanagement.vo.CartVO;


import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务接口
 *
 * @author vibe coding
 * @since 2026-04-20
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * 如果商品已存在，则更新数量；如果不存在，则新增
     *
     * @param request 添加请求参数
     */
    void addCart(CartAddRequest request);

    /**
     * 更新购物车商品数量
     *
     * @param request 更新请求参数
     */
    void updateCartQuantity(CartUpdateRequest request);

    /**
     * 删除购物车商品
     *
     * @param request 删除请求参数
     */
    void deleteCart(CartDeleteRequest request);

    /**
     * 根据购物车记录ID删除单个商品
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     */
    void deleteCartById(Long userId, Long cartId);

    /**
     * 分页查询用户购物车列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 分页购物车视图对象
     */
    IPage<CartVO> getCartList(Long userId, Integer page, Integer pageSize);

    /**
     * 查询用户购物车列表（不分页）
     *
     * @param userId 用户ID
     * @return 购物车视图对象列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 计算购物车商品总金额
     *
     * @param userId 用户ID
     * @return 总金额
     */
    BigDecimal calculateTotalAmount(Long userId);

    /**
     * 查询购物车商品总数
     *
     * @param userId 用户ID
     * @return 商品总数
     */
    Integer getCartTotalQuantity(Long userId);

    /**
     * 根据购物车ID列表查询选中的购物车商品
     * 用于生成订单时获取选中的商品
     *
     * @param userId 用户ID
     * @param cartIds 购物车ID列表
     * @return 购物车视图对象列表
     */
    List<CartVO> getCartListByIds(Long userId, List<Long> cartIds);

    /**
     * 批量删除购物车商品
     * 用于生成订单后移除已购买的商品
     *
     * @param userId 用户ID
     * @param cartIds 购物车ID列表
     */
    void deleteBatchCart(Long userId, List<Long> cartIds);

    /**
     * 检查购物车商品是否属于当前用户
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     * @return true-属于当前用户，false-不属于
     */
    boolean checkCartOwnership(Long userId, Long cartId);

    /**
     * 更新购物车中的商品种类
     * 将购物车中的商品更换为另一本书
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     * @param newBookId 新书籍ID
     * @param quantity 数量
     */
    void updateCartBook(Long userId, Long cartId, Long newBookId, Integer quantity);

    /**
     * 根据书籍类别分页查询购物车列表
     *
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @param page       页码
     * @param pageSize   每页大小
     * @return 分页购物车列表
     */
    IPage<CartVO> getCartListByCategory(Long userId, Long categoryId, Integer page, Integer pageSize);

    /**
     * 根据书籍类别查询购物车列表（不分页）
     *
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @return 购物车列表
     */
    List<CartVO> getCartListByCategory(Long userId, Long categoryId);
}
