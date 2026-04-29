package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("book_review")
public class BookReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private Long userId;
    private Integer rating;
    private String content;
    /** 状态: 1-正常显示, 0-管理员屏蔽隐藏 */
    private Integer status;
    private String adminReply;
    private Date createTime;
}
