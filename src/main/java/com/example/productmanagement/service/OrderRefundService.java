package com.example.productmanagement.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.dto.AdminAuditRefundDto;
import com.example.productmanagement.dto.AdminProcessRefundDto;
import com.example.productmanagement.dto.ApplyRefundDto;
import com.example.productmanagement.vo.AdminRefundListVo;
import com.example.productmanagement.vo.RefundDetailVo;
import com.example.productmanagement.vo.RefundListVo;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRefundService {

    /**
     * 用户提交退款申请
     * @param applyRefundDto 退款申请DTO
     */
    void applyRefund(ApplyRefundDto applyRefundDto);

    /**
     * 卖家审核退款申请
     * @param auditRefundDto 审核DTO
     */
    void auditRefund(AdminAuditRefundDto auditRefundDto);

    /**
     * 卖家处理退款（执行退款操作）
     * @param processRefundDto 退款处理DTO
     */
    void processRefund(AdminProcessRefundDto processRefundDto);

    /**
     * 查询用户的退款订单（分页）
     * @param userId 用户ID
     * @param refundNo 退款单号（可选，模糊查询）
     * @param refundType 退款类型（可选）
     * @param refundStatus 退款状态（可选）
     * @param beginTime 申请开始时间（可选）
     * @param endTime 申请结束时间（可选）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<RefundListVo> listRefunds(Long userId, String refundNo, Integer refundType, Integer refundStatus,
                                     LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize);

    /**
     * 查询用户的所有退款订单（不分页）
     * @param userId 用户ID
     * @return 退款列表
     */
    List<RefundListVo> listAllRefunds(Long userId);

    /**
     * 根据退款状态查询退款订单（分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<RefundListVo> listRefundsByStatus(Long userId, Integer refundStatus, Integer page, Integer pageSize);

    /**
     * 根据退款状态查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态
     * @return 退款列表
     */
    List<RefundListVo> listAllRefundsByStatus(Long userId, Integer refundStatus);

    /**
     * 根据退款类型查询退款订单（分页）
     * @param userId 用户ID
     * @param refundType 退款类型
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<RefundListVo> listRefundsByType(Long userId, Integer refundType, Integer page, Integer pageSize);

    /**
     * 根据退款类型查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundType 退款类型
     * @return 退款列表
     */
    List<RefundListVo> listAllRefundsByType(Long userId, Integer refundType);

    /**
     * 根据申请时间范围查询退款订单（分页）
     * @param userId 用户ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<RefundListVo> listRefundsByTimeRange(Long userId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize);

    /**
     * 根据申请时间范围查询退款订单（不分页）
     * @param userId 用户ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 退款列表
     */
    List<RefundListVo> listAllRefundsByTimeRange(Long userId, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 条件组合查询退款订单（分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param beginTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<RefundListVo> queryRefunds(Long userId, Integer refundStatus, Integer refundType, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize);

    /**
     * 条件组合查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param beginTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 退款列表
     */
    List<RefundListVo> queryAllRefunds(Long userId, Integer refundStatus, Integer refundType, LocalDateTime beginTime, LocalDateTime endTime);

    RefundDetailVo getRefundDetail(Long userId, String refundNo);

    RefundDetailVo adminGetRefundDetail(Long adminId, String refundNo);

    /**
     * 管理员查询所有退款单（分页）
     * @param adminId 管理员ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<AdminRefundListVo> adminListRefunds(Long adminId, Integer page, Integer pageSize);

    /**
     * 管理员查询所有退款单（不分页）
     * @param adminId 管理员ID
     * @return 退款列表
     */
    List<AdminRefundListVo> adminListAllRefunds(Long adminId);

    /**
     * 管理员根据退款单号查询退款单
     * @param adminId 管理员ID
     * @param refundNo 退款单号
     * @return 退款单信息
     */
    AdminRefundListVo adminGetRefundByNo(Long adminId, String refundNo);

    /**
     * 管理员根据退款类型查询退款单（分页）
     * @param adminId 管理员ID
     * @param refundType 退款类型（0-仅退款，1-退货退款）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<AdminRefundListVo> adminListRefundsByType(Long adminId, Integer refundType, Integer page, Integer pageSize);

    /**
     * 管理员根据退款类型查询退款单（不分页）
     * @param adminId 管理员ID
     * @param refundType 退款类型
     * @return 退款列表
     */
    List<AdminRefundListVo> adminListAllRefundsByType(Long adminId, Integer refundType);

    /**
     * 管理员根据退款状态查询退款单（分页）
     * @param adminId 管理员ID
     * @param refundStatus 退款状态
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<AdminRefundListVo> adminListRefundsByStatus(Long adminId, Integer refundStatus, Integer page, Integer pageSize);

    /**
     * 管理员根据退款状态查询退款单（不分页）
     * @param adminId 管理员ID
     * @param refundStatus 退款状态
     * @return 退款列表
     */
    List<AdminRefundListVo> adminListAllRefundsByStatus(Long adminId, Integer refundStatus);

    /**
     * 管理员根据申请时间范围查询退款单（分页）
     * @param adminId 管理员ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<AdminRefundListVo> adminListRefundsByTimeRange(Long adminId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize);

    /**
     * 管理员根据申请时间范围查询退款单（不分页）
     * @param adminId 管理员ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 退款列表
     */
    List<AdminRefundListVo> adminListAllRefundsByTimeRange(Long adminId, LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 管理员条件组合查询退款单（分页）
     * @param adminId 管理员ID
     * @param refundNo 退款单号（可选，模糊查询）
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param beginTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    IPage<AdminRefundListVo> adminQueryRefunds(Long adminId, String refundNo, Integer refundStatus, Integer refundType,
                                               LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize);

    /**
     * 管理员条件组合查询退款单（不分页）
     * @param adminId 管理员ID
     * @param refundNo 退款单号（可选，模糊查询）
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param beginTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 退款列表
     */
    List<AdminRefundListVo> adminQueryAllRefunds(Long adminId, String refundNo, Integer refundStatus, Integer refundType,
                                                 LocalDateTime beginTime, LocalDateTime endTime);
}
