package com.example.productmanagement.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.productmanagement.common.Result;
import com.example.productmanagement.dto.AdminAuditRefundDto;
import com.example.productmanagement.dto.AdminProcessRefundDto;
import com.example.productmanagement.dto.ApplyRefundDto;
import com.example.productmanagement.service.OrderRefundService;
import com.example.productmanagement.utils.UserHolder;
import com.example.productmanagement.vo.AdminRefundListVo;
import com.example.productmanagement.vo.RefundDetailVo;
import com.example.productmanagement.vo.RefundListVo;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/refund")
public class RefundController {

    private final OrderRefundService orderRefundService;

    public RefundController(OrderRefundService orderRefundService) {
        this.orderRefundService = orderRefundService;
    }

    /**
     * 用户发起退款申请
     * @param dto
     * @return
     */
    @PostMapping("user/apply-refund")
    public Result<?> userApplyRefund(@RequestBody ApplyRefundDto dto) {
        Long userId = UserHolder.getUserId();
        dto.setUserId(userId);
        orderRefundService.applyRefund(dto);
        return Result.success("申请成功");
    }

    /**
     * 卖家审核退款请求
     * @param dto
     * @return
     */
    @PostMapping("admin/audit-refund")
    public Result<?> adminAuditRefund(@RequestBody AdminAuditRefundDto dto) {
        dto.setAdminId(UserHolder.getUserId());
        orderRefundService.auditRefund(dto);
        return Result.success("处理成功");
    }

    /**
     * 卖家进行退款
     * @param dto
     * @return
     */
    @PostMapping("admin/process-refund")
    public Result<?> adminProcessRefund(@RequestBody AdminProcessRefundDto dto) {
        dto.setAdminId(UserHolder.getUserId());
        orderRefundService.processRefund(dto);
        return Result.success("退款成功");
    }


    /**
     * 当前登录用户查询自己的退款/售后记录（分页）
     * @param refundNo 退款编号（可选）
     * @param refundType 退款类型（可选，0-仅退款，1-退货退款）
     * @param refundStatus 退款状态（可选）
     * @param beginTime 申请开始日期（可选，yyyy-MM-dd）
     * @param endTime 申请结束日期（可选，yyyy-MM-dd）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/list")
    public Result<IPage<RefundListVo>> listRefunds(
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserHolder.getUserId();
        LocalDateTime beginDateTime = beginTime == null ? null : beginTime.atStartOfDay();
        LocalDateTime endDateTime = endTime == null ? null : endTime.atTime(23, 59, 59);
        IPage<RefundListVo> refundPage = orderRefundService.listRefunds(
                userId, refundNo, refundType, refundStatus, beginDateTime, endDateTime, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 查询用户的所有退款订单（不分页）
     * @param userId 用户ID
     * @return 退款列表
     */
    @GetMapping("/list/all")
    public Result<List<RefundListVo>> listAllRefunds(
            @RequestParam(required = false) Long userId) {
        userId = UserHolder.getUserId();
        List<RefundListVo> refundList = orderRefundService.listAllRefunds(userId);
        return Result.success(refundList);
    }

    /**
     * 根据退款状态查询退款订单（分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态（0-待审核，1-审核通过，2-用户已寄回，3-商家已收货，4-已退款，5-审核拒绝，6-用户已取消）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/list/status")
    public Result<IPage<RefundListVo>> listRefundsByStatus(
            @RequestParam(required = false) Long userId,
            @NotNull @RequestParam Integer refundStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        userId = UserHolder.getUserId();
        IPage<RefundListVo> refundPage = orderRefundService.listRefundsByStatus(userId, refundStatus, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 根据退款状态查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态
     * @return 退款列表
     */
    @GetMapping("/list/status/all")
    public Result<List<RefundListVo>> listAllRefundsByStatus(
            @RequestParam(required = false) Long userId,
            @NotNull @RequestParam Integer refundStatus) {
        userId = UserHolder.getUserId();
        List<RefundListVo> refundList = orderRefundService.listAllRefundsByStatus(userId, refundStatus);
        return Result.success(refundList);
    }

    /**
     * 根据退款类型查询退款订单（分页）
     * @param userId 用户ID
     * @param refundType 退款类型（0-退货退款，1-仅退款）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/list/type")
    public Result<IPage<RefundListVo>> listRefundsByType(
            @RequestParam(required = false) Long userId,
            @NotNull @RequestParam Integer refundType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        userId = UserHolder.getUserId();
        IPage<RefundListVo> refundPage = orderRefundService.listRefundsByType(userId, refundType, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 根据退款类型查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundType 退款类型
     * @return 退款列表
     */
    @GetMapping("/list/type/all")
    public Result<List<RefundListVo>> listAllRefundsByType(
            @RequestParam(required = false) Long userId,
            @NotNull @RequestParam Integer refundType) {
        userId = UserHolder.getUserId();
        List<RefundListVo> refundList = orderRefundService.listAllRefundsByType(userId, refundType);
        return Result.success(refundList);
    }

    /**
     * 根据申请时间范围查询退款订单（分页）
     * @param userId 用户ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/list/time")
    public Result<IPage<RefundListVo>> listRefundsByTimeRange(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        userId = UserHolder.getUserId();
        IPage<RefundListVo> refundPage = orderRefundService.listRefundsByTimeRange(userId, beginTime, endTime, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 根据申请时间范围查询退款订单（不分页）
     * @param userId 用户ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 退款列表
     */
    @GetMapping("/list/time/all")
    public Result<List<RefundListVo>> listAllRefundsByTimeRange(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        userId = UserHolder.getUserId();
        List<RefundListVo> refundList = orderRefundService.listAllRefundsByTimeRange(userId, beginTime, endTime);
        return Result.success(refundList);
    }

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
    @GetMapping("/query")
    public Result<IPage<RefundListVo>> queryRefunds(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        userId = UserHolder.getUserId();
        IPage<RefundListVo> refundPage = orderRefundService.queryRefunds(userId, refundStatus, refundType, beginTime, endTime, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 条件组合查询退款订单（不分页）
     * @param userId 用户ID
     * @param refundStatus 退款状态（可选）
     * @param refundType 退款类型（可选）
     * @param beginTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 退款列表
     */
    @GetMapping("/query/all")
    public Result<List<RefundListVo>> queryAllRefunds(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        userId = UserHolder.getUserId();
        List<RefundListVo> refundList = orderRefundService.queryAllRefunds(userId, refundStatus, refundType, beginTime, endTime);
        return Result.success(refundList);
    }

    /**
     * 用户查询退款单详情
     * @param userId 用户ID
     * @param refundNo 退款单号
     * @return 退款单详情
     */
    @GetMapping("/detail")
    public Result<RefundDetailVo> getRefundDetail(
            @RequestParam(required = false) Long userId,
            @NotNull @RequestParam String refundNo) {
        userId = UserHolder.getUserId();
        RefundDetailVo detail = orderRefundService.getRefundDetail(userId, refundNo);
        return Result.success(detail);
    }

    /**
     * 管理员查询退款单详情
     * @param adminId 管理员ID
     * @param refundNo 退款单号
     * @return 退款单详情
     */
    @GetMapping("/admin/detail")
    public Result<RefundDetailVo> adminGetRefundDetail(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam String refundNo) {
        adminId = UserHolder.getUserId();
        RefundDetailVo detail = orderRefundService.adminGetRefundDetail(adminId, refundNo);
        return Result.success(detail);
    }

    // ====================== 管理员退款单列表查询接口 ======================

    /**
     * 管理员查询所有退款单（分页）
     * @param adminId 管理员ID
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/admin/list")
    public Result<IPage<AdminRefundListVo>> adminListRefunds(
            @RequestParam(required = false) Long adminId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        adminId = UserHolder.getUserId();
        IPage<AdminRefundListVo> refundPage = orderRefundService.adminListRefunds(adminId, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 管理员查询所有退款单（不分页）
     * @param adminId 管理员ID
     * @return 退款列表
     */
    @GetMapping("/admin/list/all")
    public Result<List<AdminRefundListVo>> adminListAllRefunds(
            @RequestParam(required = false) Long adminId) {
        adminId = UserHolder.getUserId();
        List<AdminRefundListVo> refundList = orderRefundService.adminListAllRefunds(adminId);
        return Result.success(refundList);
    }

    /**
     * 管理员根据退款单号查询退款单
     * @param adminId 管理员ID
     * @param refundNo 退款单号
     * @return 退款单信息
     */
    @GetMapping("/admin/getByNo")
    public Result<AdminRefundListVo> adminGetRefundByNo(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam String refundNo) {
        adminId = UserHolder.getUserId();
        AdminRefundListVo refund = orderRefundService.adminGetRefundByNo(adminId, refundNo);
        return Result.success(refund);
    }

    /**
     * 管理员根据退款类型查询退款单（分页）
     * @param adminId 管理员ID
     * @param refundType 退款类型（0-仅退款，1-退货退款）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/admin/list/type")
    public Result<IPage<AdminRefundListVo>> adminListRefundsByType(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer refundType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        adminId = UserHolder.getUserId();
        IPage<AdminRefundListVo> refundPage = orderRefundService.adminListRefundsByType(adminId, refundType, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 管理员根据退款类型查询退款单（不分页）
     * @param adminId 管理员ID
     * @param refundType 退款类型
     * @return 退款列表
     */
    @GetMapping("/admin/list/type/all")
    public Result<List<AdminRefundListVo>> adminListAllRefundsByType(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer refundType) {
        adminId = UserHolder.getUserId();
        List<AdminRefundListVo> refundList = orderRefundService.adminListAllRefundsByType(adminId, refundType);
        return Result.success(refundList);
    }

    /**
     * 管理员根据退款状态查询退款单（分页）
     * @param adminId 管理员ID
     * @param refundStatus 退款状态（0-待审核，1-审核通过，2-用户已寄回，3-商家已收货，4-已退款，5-审核拒绝，6-用户已取消）
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/admin/list/status")
    public Result<IPage<AdminRefundListVo>> adminListRefundsByStatus(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer refundStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        adminId = UserHolder.getUserId();
        IPage<AdminRefundListVo> refundPage = orderRefundService.adminListRefundsByStatus(adminId, refundStatus, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 管理员根据退款状态查询退款单（不分页）
     * @param adminId 管理员ID
     * @param refundStatus 退款状态
     * @return 退款列表
     */
    @GetMapping("/admin/list/status/all")
    public Result<List<AdminRefundListVo>> adminListAllRefundsByStatus(
            @RequestParam(required = false) Long adminId,
            @NotNull @RequestParam Integer refundStatus) {
        adminId = UserHolder.getUserId();
        List<AdminRefundListVo> refundList = orderRefundService.adminListAllRefundsByStatus(adminId, refundStatus);
        return Result.success(refundList);
    }

    /**
     * 管理员根据申请时间范围查询退款单（分页）
     * @param adminId 管理员ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页退款列表
     */
    @GetMapping("/admin/list/time")
    public Result<IPage<AdminRefundListVo>> adminListRefundsByTimeRange(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        adminId = UserHolder.getUserId();
        IPage<AdminRefundListVo> refundPage = orderRefundService.adminListRefundsByTimeRange(adminId, beginTime, endTime, page, pageSize);
        return Result.success(refundPage);
    }

    /**
     * 管理员根据申请时间范围查询退款单（不分页）
     * @param adminId 管理员ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 退款列表
     */
    @GetMapping("/admin/list/time/all")
    public Result<List<AdminRefundListVo>> adminListAllRefundsByTimeRange(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        adminId = UserHolder.getUserId();
        List<AdminRefundListVo> refundList = orderRefundService.adminListAllRefundsByTimeRange(adminId, beginTime, endTime);
        return Result.success(refundList);
    }

    /**
     * 管理员条件组合查询退款单（分页）
     * 支持通过退款单号（模糊查询）、退款类型、退款状态、申请时间范围查询
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
    @GetMapping("/admin/query")
    public Result<IPage<AdminRefundListVo>> adminQueryRefunds(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        adminId = UserHolder.getUserId();
        IPage<AdminRefundListVo> refundPage = orderRefundService.adminQueryRefunds(adminId, refundNo, refundStatus, refundType, beginTime, endTime, page, pageSize);
        return Result.success(refundPage);
    }

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
    @GetMapping("/admin/query/all")
    public Result<List<AdminRefundListVo>> adminQueryAllRefunds(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String refundNo,
            @RequestParam(required = false) Integer refundStatus,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        adminId = UserHolder.getUserId();
        List<AdminRefundListVo> refundList = orderRefundService.adminQueryAllRefunds(adminId, refundNo, refundStatus, refundType, beginTime, endTime);
        return Result.success(refundList);
    }
}

