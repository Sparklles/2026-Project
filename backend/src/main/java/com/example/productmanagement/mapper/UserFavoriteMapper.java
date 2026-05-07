package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
