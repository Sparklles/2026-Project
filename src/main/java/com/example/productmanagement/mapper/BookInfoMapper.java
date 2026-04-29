package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.entity.BookInfo;
import com.example.productmanagement.vo.BookDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    List<BookDetailVO> searchBooksDynamic(IPage<BookDetailVO> page,
                                          @Param("categoryId") Long categoryId,
                                          @Param("keyword") String keyword);
}
