import request from '@/utils/request'

/**
 * 获取未读消息数量
 * @param {Object} params { receiverType }
 */
export function getUnreadCount(params) {
  return request.get('/api/notification/unread-count', { params, tokenKey: 'admin-token' })
}

/**
 * 分页获取消息列表
 * @param {Object} params { receiverType, page, size }
 */
export function getNotificationList(params) {
  return request.get('/api/notification/vo/list', { params, tokenKey: 'admin-token' })
}

/**
 * 获取全部消息列表
 * @param {Object} params { receiverType }
 */
export function getAllNotifications(params) {
  return request.get('/api/notification/vo/list/all', { params, tokenKey: 'admin-token' })
}

/**
 * 标记单条消息为已读
 * @param {Number} id 消息ID
 */
export function markAsRead(id) {
  return request.put(`/api/notification/read/${id}`, null, { tokenKey: 'admin-token' })
}

/**
 * 标记全部消息为已读
 * @param {Object} params { receiverType }
 */
export function markAllAsRead(params) {
  return request.put('/api/notification/read-all', null, { params, tokenKey: 'admin-token' })
}
