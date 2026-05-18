import request from '@/utils/request'

/**
 * 申请退款
 * @param {Object} data - 退款申请数据
 */
export function applyRefund(data) {
  return request.post('/api/refund/user/apply-refund', data)
}

/**
 * 获取退款列表
 * @param {Object} params - 查询参数
 */
export function getRefundList(params) {
  return request.get('/api/refund/list', { params })
}

/**
 * 获取退款详情
 * @param {Object} params - 查询参数 { userId, refundNo }
 */
export function getRefundDetail(params) {
  return request.get('/api/refund/detail', { params })
}

/**
 * 取消退款申请
 * @param {Number} id - 退款 ID
 */
export function cancelRefund(id) {
  return request.put(`/api/refund/user/cancel/${id}`)
}
