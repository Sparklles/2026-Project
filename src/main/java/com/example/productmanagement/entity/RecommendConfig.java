package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("recommend_config")
public class RecommendConfig {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String configKey;
    private String configName;
    private String sceneCode;
    private String ruleJson;
    private Integer priority;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}