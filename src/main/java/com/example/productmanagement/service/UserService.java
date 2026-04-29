package com.example.productmanagement.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.productmanagement.dto.LoginDto;
import com.example.productmanagement.entity.User;

/**
* @author freedom
* @description 针对表【user(系统用户核心表(鉴权与风控))】的数据库操作Service
* @createDate 2026-04-21 15:14:41
*/
public interface UserService extends IService<User> {

    String login(LoginDto loginDto);
}
