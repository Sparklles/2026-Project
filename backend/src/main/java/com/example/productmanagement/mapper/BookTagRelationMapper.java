package com.example.productmanagement.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.productmanagement.entity.BookTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 书籍与标签关联表 Mapper 接口
 */
@Mapper
public interface BookTagRelationMapper extends BaseMapper<BookTagRelation> {

    /**
     * 批量插入书籍与标签的关联关系（提升性能，避免 for 循环单条插入）
     * * @param relationList 关联关系列表
     * @return 影响的行数
     */
    int insertBatch(@Param("list") List<BookTagRelation> relationList);

}
