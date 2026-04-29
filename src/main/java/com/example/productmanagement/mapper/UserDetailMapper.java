package com.example.productmanagement.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.UserDetail;
import org.apache.ibatis.annotations.Mapper;

/**
* @author freedom
* @description 针对表【user_detail(用户详细信息表)】的数据库操作Mapper
* @createDate 2026-04-21 15:14:41
* @Entity com.dujiang.sailmart.entity.UserDetail
*/
@Mapper
public interface UserDetailMapper extends BaseMapper<UserDetail> {

}




