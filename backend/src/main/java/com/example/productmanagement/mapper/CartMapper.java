package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.productmanagement.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;

/**
 * 购物车 Mapper 接口
 * 继承 BaseMapper 获得基础 CRUD 能力
 * 自定义购物车相关查询方法
 *
 * @author vibe coding
 * @since 2026-04-20
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID分页查询购物车列表
     *
     * @param userId 用户ID
     * @return 分页购物车列表
     */
    IPage<Cart> selectCartListByUserId(@Param("page") Page<Cart> page, @Param("userId") Long userId);

    /**
     * 根据用户id查询购物车列表
     *
     * @param userId 用户id
     * @return 购物车列表
     */
    List<Cart> selectCartListByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和书籍ID分页查询购物车记录
     * 用于判断商品是否已在购物车中
     *
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 购物车记录
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND book_id = #{bookId}")
    Cart selectByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /**
     * 根据用户ID查询购物车商品总数
     *
     * @param userId 用户ID
     * @return 商品总数
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = #{userId}")
    Integer selectTotalQuantityByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID清空购物车
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量删除购物车商品
     *
     * @param userId 用户ID
     * @param cartIds 购物车ID列表
     * @return 影响行数
     */
    int deleteBatchByIds(@Param("userId") Long userId, @Param("cartIds") List<Long> cartIds);

    /**
     * 根据用户ID和购物车ID查询
     * 用于校验权限
     *
     * @param userId 用户ID
     * @param cartId 购物车ID
     * @return 购物车记录
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND cart_id = #{cartId}")
    Cart selectByUserIdAndCartId(@Param("userId") Long userId, @Param("cartId") Long cartId);

    /**
     * 根据购物车ID列表分页查询购物车商品
     * 用于生成订单时获取选中的购物车商品
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param cartIds 购物车ID列表
     * @return 分页购物车列表
     */
    IPage<Cart> selectCartListByIds(@Param("page") Page<Cart> page, @Param("userId") Long userId, @Param("cartIds") List<Long> cartIds);

    /**
     * 根据用户ID和书籍类别分页查询购物车列表
     *
     * @param page       分页参数
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @return 分页购物车列表
     */
    IPage<Cart> selectCartListByUserIdAndCategoryId(@Param("page") Page<Cart> page, @Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * 根据用户ID和书籍类别查询购物车列表（不分页）
     *
     * @param userId     用户ID
     * @param categoryId 书籍类别ID
     * @return 购物车列表
     */
    List<Cart> selectCartListByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}

