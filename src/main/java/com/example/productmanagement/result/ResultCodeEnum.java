package com.example.productmanagement.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    PARAM_ERROR(202, "参数不正确"),
    SERVICE_ERROR(203, "服务异常"),
    DATA_ERROR(204, "数据异常"),
    ILLEGAL_REQUEST(205, "非法请求"),
    REPEAT_SUBMIT(206, "重复提交"),
    DELETE_ERROR(207, "请先删除子集"),

    ACCOUNT_EXIST_ERROR(301, "账号已存在"),
    ACCOUNT_PASSWORD_ERROR(302, "密码错误"),
    LOGIN_AUTH(305, "未登陆"),
    ACCOUNT_NOT_EXIST_ERROR(306, "账号不存在"),
    ACCOUNT_ERROR(307, "用户名或密码错误"),
    ACCOUNT_DISABLED_ERROR(308, "该用户已被冻结"),
    ACCESS_FORBIDDEN(309, "无访问权限"),
    //邮箱格式错误
    EMAIL_ERROR(310, "邮箱格式错误"),
    //邮箱长度不能大于100
    EMAIL_LENGTH_ERROR(311, "邮箱长度不能大于100"),
    //邮箱已被使用
    EMAIL_EXIST_ERROR(312, "邮箱已被使用"),
    EMAIL_NOT_BIND_ERROR(313, "该账号未绑定邮箱"),
    VERIFY_CODE_ERROR(314, "验证码错误"),
    VERIFY_CODE_EXPIRED(315, "验证码已过期"),
    VERIFY_CODE_SEND_TOO_FREQUENT(316, "验证码发送过于频繁，请稍后再试"),


    LOGIN_ACCOUNT_EMPTY(501, "手机号或账号为空"),
    LOGIN_PASSWORD_EMPTY(502, "密码为空"),
    //登录方式错误
    LOGIN_TYPE_ERROR(503, "非法的登录方式"),
    //非法的注册方式
    REGISTER_TYPE_ERROR(504, "非法的注册方式"),
    //手机号格式错误
    PHONE_ERROR(505, "手机号格式错误"),




    TOKEN_EXPIRED(601, "token过期"),
    TOKEN_INVALID(602, "token非法");


    private final Integer code;

    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
