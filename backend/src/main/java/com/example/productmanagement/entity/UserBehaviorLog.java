package com.example.productmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户行为日志实体。
 *
 * <p>对应 user_behavior_log 表，是推荐模块的行为数据源。
 * 该表记录用户在前台产生的搜索、浏览、加购、收藏、购买等行为，
 * 定时推荐任务会基于这些数据生成个性化推荐缓存。</p>
 */
@Data
@TableName("user_behavior_log")
public class UserBehaviorLog {

    /** 主键，使用雪花算法生成。 */
    @TableId(type = IdType.INPUT)
    private Long id;

    /** 行为所属用户。当前实现只记录登录用户行为，未登录行为不写入。 */
    @TableField("user_id")
    private Long userId;

    /** 行为类型：1-浏览 2-搜索 3-点击 4-加购 5-收藏 6-购买。 */
    @TableField("behavior_type")
    private Integer behaviorType;

    /** 行为关联书籍ID。搜索行为可为空。 */
    @TableField("book_id")
    private Long bookId;

    /** 搜索关键词，仅搜索行为使用。 */
    @TableField("search_keyword")
    private String searchKeyword;

    /** 浏览时长，当前暂未接入前端停留时长埋点。 */
    @TableField("duration")
    private Integer duration;

    /** 扩展数据，预留给来源页面、设备信息等附加属性。 */
    @TableField("extra_data")
    private String extraData;

    /** 日志过期时间，过期数据由推荐定时任务清理。 */
    @TableField("expire_time")
    private Date expireTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
