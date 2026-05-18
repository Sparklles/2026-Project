import request from '@/utils/request'

/**
 * 管理员分页查询所有订单
 * @param {Object} params { adminId, page, pageSize }
 */
export function getAdminOrderList(params) {
  return request.get('/api/order/admin/list', { params })
}

/**
 * 管理员查询所有订单（不分页）
 * @param {Object} params { adminId }
 */
export function getAdminOrderListAll(params) {
  return request.get('/api/order/admin/list/all', { params })
}

/**
 * 管理员分页查询已支付订单
 * @param {Object} params { adminId, page, pageSize }
 */
export function getAdminPaidOrderList(params) {
  return request.get('/api/order/admin/paid/list', { params })
}

/**
 * 管理员查询所有已支付订单（不分页）
 * @param {Object} params { adminId }
 */
export function getAdminPaidOrderListAll(params) {
  return request.get('/api/order/admin/paid/list/all', { params })
}

/**
 * 管理员按状态分页查询订单
 * @param {Object} params { adminId, orderStatus, page, pageSize }
 */
export function getAdminOrderListByStatus(params) {
  return request.get('/api/order/admin/status/list', { params })
}

/**
 * 管理员按状态查询所有订单（不分页）
 * @param {Object} params { adminId, orderStatus }
 */
export function getAdminOrderListByStatusAll(params) {
  return request.get('/api/order/admin/status/list/all', { params })
}

/**
 * 管理员查询订单详情
 * @param {Object} params { adminId, orderNo }
 */
export function getAdminOrderDetail(params) {
  return request.get('/api/order/admin/detail', { params })
}

/**
 * 管理员发货
 * @param {Object} data { userId: adminId, orderNo }
 */
export function shipOrder(data) {
  return request.put('/api/order/admin/ship', data)
}
