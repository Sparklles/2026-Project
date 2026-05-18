package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.RecommendationItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RecommendationItemMapper extends BaseMapper<RecommendationItem> {

    @Select("SELECT ri.target_book_id FROM recommendation_item ri " +
            "WHERE ri.scene_code = #{sceneCode} " +
            "AND ri.source_book_id = #{sourceBookId} " +
            "AND (ri.expire_time IS NULL OR ri.expire_time > NOW()) " +
            "ORDER BY ri.score DESC, ri.sort_order ASC " +
            "LIMIT #{limit}")
    List<Long> selectTargetBookIdsBySource(@Param("sceneCode") String sceneCode,
                                           @Param("sourceBookId") Long sourceBookId,
                                           @Param("limit") int limit);

    @Select("SELECT ri.target_book_id FROM recommendation_item ri " +
            "WHERE ri.scene_code = #{sceneCode} " +
            "AND ri.user_id = #{userId} " +
            "AND (ri.expire_time IS NULL OR ri.expire_time > NOW()) " +
            "ORDER BY ri.score DESC, ri.sort_order ASC " +
            "LIMIT #{limit}")
    List<Long> selectTargetBookIdsByUser(@Param("sceneCode") String sceneCode,
                                         @Param("userId") Long userId,
                                         @Param("limit") int limit);

    @Select("SELECT ri.target_book_id FROM recommendation_item ri " +
            "WHERE ri.scene_code = #{sceneCode} " +
            "AND ri.user_id IS NULL AND ri.source_book_id IS NULL " +
            "AND (ri.expire_time IS NULL OR ri.expire_time > NOW()) " +
            "ORDER BY ri.score DESC, ri.sort_order ASC " +
            "LIMIT #{limit}")
    List<Long> selectTargetBookIdsByScene(@Param("sceneCode") String sceneCode,
                                          @Param("limit") int limit);

    @Select("SELECT ri.target_book_id FROM recommendation_item ri " +
            "WHERE ri.scene_code = #{sceneCode} " +
            "AND (ri.expire_time IS NULL OR ri.expire_time > NOW()) " +
            "ORDER BY ri.score DESC, ri.sort_order ASC " +
            "LIMIT #{limit}")
    List<Long> selectTargetBookIdsBySceneAll(@Param("sceneCode") String sceneCode,
                                             @Param("limit") int limit);
}