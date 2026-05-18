package com.example.productmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.productmanagement.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT COUNT(*) FROM notification WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND status = 0")
    Integer countUnread(@Param("receiverId") Long receiverId, @Param("receiverType") Integer receiverType);

    @Update("UPDATE notification SET status = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE notification SET status = 1 WHERE receiver_id = #{receiverId} AND receiver_type = #{receiverType} AND status = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId, @Param("receiverType") Integer receiverType);
}
