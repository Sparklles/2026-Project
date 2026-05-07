package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.BookTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BookTagMapper extends BaseMapper<BookTag> {
    //精准找出某本书的全部标签名
    @Select("SELECT t.name FROM book_tag t JOIN book_tag_relation r ON t.id = r.tag_id WHERE r.book_id = #{bookId}")
    List<String> getTagsByBookId(@Param("bookId") Long bookId);
}
