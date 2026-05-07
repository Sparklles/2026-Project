package com.example.productmanagement.dto;

import lombok.Data;

/**
 * 后台用户列表搜索参数
 * <p>三个维度均为模糊匹配，互不排斥（同时传则取交集）。</p>
 */
@Data
public class UserSearchDto {

    /** 按登录账号模糊搜索（可为空） */
    private String loginAccount;

    /** 按邮箱模糊搜索（可为空） */
    private String email;

    /** 按手机号模糊搜索（可为空） */
    private String phone;

    /** 当前页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;
}
