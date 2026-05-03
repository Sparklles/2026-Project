package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.BookQueryDTO;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.vo.BookDetailVO;
import com.example.productmanagement.vo.SearchBookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 根据书籍ID查询书籍价格
     *
     * @param bookId 书籍ID
     * @return 书籍价格
     */
    @Select("SELECT price FROM book_info WHERE id = #{bookId} AND is_deleted = 0 AND status = 1")
    BigDecimal selectPriceById(@Param("bookId") Long bookId);

    /**
     * 根据用户ID查询用户购物车内的所有书籍信息
     * 关联购物车表查询
     *
     * @param userId 用户ID
     * @return 书籍信息列表
     */
    List<BookInfo> selectBooksInCartByUserId(@Param("userId") Long userId);

    /**
     * 根据书籍ID列表查询书籍信息
     * 用于批量查询购物车中的书籍
     *
     * @param bookIds 书籍ID列表
     * @return 书籍信息列表
     */
    List<BookInfo> selectBatchByIds(@Param("bookIds") List<Long> bookIds);

    /**
     * 检查书籍是否上架且未删除
     *
     * @param bookId 书籍ID
     * @return 符合条件的书籍数量
     */
    @Select("SELECT COUNT(*) FROM book_info WHERE id = #{bookId} AND is_deleted = 0 AND status = 1")
    Integer checkBookAvailable(@Param("bookId") Long bookId);

    /**
     * 根据书籍ID查询库存
     *
     * @param bookId 书籍ID
     * @return 库存数量
     */
    @Select("SELECT stock FROM book_info WHERE id = #{bookId} AND is_deleted = 0")
    Integer selectStockById(@Param("bookId") Long bookId);

    /**
     * 扣减商品库存
     * 🌟 核心修复：补上真实的 SQL 语句！
     * 🌟 安全机制：加上 stock >= #{quantity} 条件，利用数据库行级锁，完美防止高并发下的“超卖”现象！
     *
     * @param bookId   商品ID
     * @param quantity 购买数量
     * @return 影响的行数（返回 1 说明扣减成功，返回 0 说明库存不足）
     */
    @Update("UPDATE book_info SET stock = stock - #{quantity} WHERE id = #{bookId} AND stock >= #{quantity} AND is_deleted = 0")
    int decreaseStock(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    /**
     * 增加销量
     *
     * @param bookId 书籍ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    int increaseSales(@Param("bookId") Long bookId, @Param("quantity") Integer quantity);

    /**
     * 根据ISBN查询书籍信息
     *
     * @param isbn ISBN
     * @return 书籍信息
     */
    @Select("SELECT * FROM book_info WHERE isbn = #{isbn} AND is_deleted = 0")
    BookInfo selectByIsbn(@Param("isbn") String isbn);

    /**
     * 根据分类ID查询书籍列表
     *
     * @param categoryId 分类ID
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE category_id = #{categoryId} AND is_deleted = 0 AND status = 1 ORDER BY sales DESC")
    List<BookInfo> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据难度标签查询书籍列表
     *
     * @param difficultyTag 难度标签
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE difficulty_tag = #{difficultyTag} AND is_deleted = 0 AND status = 1 ORDER BY sales DESC")
    List<BookInfo> selectByDifficultyTag(@Param("difficultyTag") String difficultyTag);

    /**
     * 根据航行地区查询书籍列表
     *
     * @param region 航行地区
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE region = #{region} AND is_deleted = 0 AND status = 1 ORDER BY sales DESC")
    List<BookInfo> selectByRegion(@Param("region") String region);

    /**
     * 根据年龄分段查询书籍列表
     *
     * @param ageLevel 年龄分段
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE age_level = #{ageLevel} AND is_deleted = 0 AND status = 1 ORDER BY sales DESC")
    List<BookInfo> selectByAgeLevel(@Param("ageLevel") Integer ageLevel);

    /**
     * 查询热销书籍列表
     *
     * @param limit 查询数量限制
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE is_deleted = 0 AND status = 1 ORDER BY sales DESC LIMIT #{limit}")
    List<BookInfo> selectHotBooks(@Param("limit") Integer limit);

    /**
     * 查询高分书籍列表
     *
     * @param limit 查询数量限制
     * @return 书籍信息列表
     */
    @Select("SELECT * FROM book_info WHERE is_deleted = 0 AND status = 1 AND avg_rating >= 4.0 ORDER BY avg_rating DESC LIMIT #{limit}")
    List<BookInfo> selectHighRatingBooks(@Param("limit") Integer limit);

    /**
     * 更新书籍平均评分和评价数量
     *
     * @param bookId 书籍ID
     * @param avgRating 平均评分
     * @param reviewCount 评价数量
     * @return 影响行数
     */
    int updateRatingAndReviewCount(@Param("bookId") Long bookId, @Param("avgRating") BigDecimal avgRating, @Param("reviewCount") Integer reviewCount);

    /**
     * 增加收藏次数
     *
     * @param bookId 书籍ID
     * @return 影响行数
     */
    @Select("UPDATE book_info SET favorite_count = favorite_count + 1 WHERE id = #{bookId}")
    int increaseFavoriteCount(@Param("bookId") Long bookId);

    /**
     * 减少收藏次数
     *
     * @param bookId 书籍ID
     * @return 影响行数
     */
    @Select("UPDATE book_info SET favorite_count = favorite_count - 1 WHERE id = #{bookId} AND favorite_count > 0")
    int decreaseFavoriteCount(@Param("bookId") Long bookId);
}
