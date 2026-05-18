package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    /** 角色: 1-初级海员, 2-经验丰富海员, 3-非海员买家, 10-管理员, 11-报表查看者 */
    private Integer role;
    /** 账号状态: 1-正常, 0-冻结/禁用 */
    private Integer status;
    private Date createTime;
    private Date updateTime;
}