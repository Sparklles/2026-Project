import request from '@/utils/request'

/**
 * 管理员查询退款列表（分页）
 * @param {Object} params { adminId, page, pageSize }
 */
export function getAdminRefundList(params) {
  return request.get('/api/refund/admin/list', { params })
}

/**
 * 管理员查询所有退款单（不分页）
 * @param {Object} params { adminId }
 */
export function getAdminRefundListAll(params) {
  return request.get('/api/refund/admin/list/all', { params })
}

/**
 * 管理员根据退款单号查询退款单
 * @param {Object} params { adminId, refundNo }
 */
export function getAdminRefundByNo(params) {
  return request.get('/api/refund/admin/getByNo', { params })
}

/**
 * 管理员按状态查询退款列表（分页）
 * @param {Object} params { adminId, refundStatus, page, pageSize }
 */
export function getAdminRefundListByStatus(params) {
  return request.get('/api/refund/admin/list/status', { params })
}

/**
 * 管理员根据退款状态查询退款单（不分页）
 * @param {Object} params { adminId, refundStatus }
 */
export function getAdminRefundListByStatusAll(params) {
  return request.get('/api/refund/admin/list/status/all', { params })
}

/**
 * 管理员按类型查询退款列表（分页）
 * @param {Object} params { adminId, refundType, page, pageSize }
 */
export function getAdminRefundListByType(params) {
  return request.get('/api/refund/admin/list/type', { params })
}

/**
 * 管理员根据退款类型查询退款单（不分页）
 * @param {Object} params { adminId, refundType }
 */
export function getAdminRefundListByTypeAll(params) {
  return request.get('/api/refund/admin/list/type/all', { params })
}

/**
 * 管理员按时间范围查询退款列表（分页）
 * @param {Object} params { adminId, beginTime, endTime, page, pageSize }
 */
export function getAdminRefundListByTime(params) {
  return request.get('/api/refund/admin/list/time', { params })
}

/**
 * 管理员根据申请时间范围查询退款单（不分页）
 * @param {Object} params { adminId, beginTime, endTime }
 */
export function getAdminRefundListByTimeAll(params) {
  return request.get('/api/refund/admin/list/time/all', { params })
}

/**
 * 管理员条件组合查询退款单（分页）
 * @param {Object} params { adminId, refundNo, refundStatus, refundType, beginTime, endTime, page, pageSize }
 */
export function queryAdminRefundList(params) {
  return request.get('/api/refund/admin/query', { params })
}

/**
 * 管理员条件组合查询退款单（不分页）
 * @param {Object} params { adminId, refundNo, refundStatus, refundType, beginTime, endTime }
 */
export function queryAdminRefundListAll(params) {
  return request.get('/api/refund/admin/query/all', { params })
}

/**
 * 管理员查询退款详情
 * @param {Object} params { adminId, refundNo }
 */
export function getAdminRefundDetail(params) {
  return request.get('/api/refund/admin/detail', { params })
}

/**
 * 管理员审核退款申请
 * @param {Object} data { adminId, refundId, approved, rejectReason }
 */
export function auditRefund(data) {
  return request.post('/api/refund/admin/audit-refund', data, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 管理员执行退款
 * @param {Object} data { adminId, refundId }
 */
export function processRefund(data) {
  return request.post('/api/refund/admin/process-refund', data, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
