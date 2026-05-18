package com.example.productmanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.productmanagement.entity.UserDetail;
import com.example.productmanagement.service.UserDetailService;
import com.example.productmanagement.mapper.UserDetailMapper;
import org.springframework.stereotype.Service;

/**
* @author freedom
* @description 针对表【user_detail(用户详细信息表)】的数据库操作Service实现
* @createDate 2026-04-21 15:14:41
*/
@Service
public class UserDetailServiceImpl extends ServiceImpl<UserDetailMapper, UserDetail>
    implements UserDetailService{

}




