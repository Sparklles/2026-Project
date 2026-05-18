package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 搜索日志实体类
 */
@Data
@TableName("search_log")
public class SearchLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("search_keyword")
    private String searchKeyword;

    @TableField("search_type")
    private Integer searchType;

    @TableField("result_count")
    private Integer resultCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
