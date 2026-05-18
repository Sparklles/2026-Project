import request from '@/utils/request'

/**
 * 添加商品到购物车
 * @param {Object} data - 添加请求数据
 * @param {Long} data.userId - 用户 ID
 * @param {Long} data.bookId - 书籍 ID
 * @param {Integer} data.quantity - 商品数量
 */
export function addToCart(data) {
  return request.post('/api/cart/add', data)
}

/**
 * 获取购物车列表
 * @param {Long} userId - 用户 ID
 */
export function getCartList(userId) {
  return request.get('/api/cart/list', { params: { userId } })
}

/**
 * 更新购物车商品数量
 * @param {Object} data - 更新数据
 * @param {Long} data.userId - 用户 ID
 * @param {Long} data.bookId - 书籍 ID
 * @param {Integer} data.quantity - 新的数量
 */
export function updateCartQuantity(data) {
  return request.put('/api/cart/update/quantity', data)
}

/**
 * 删除购物车商品
 * @param {Long} userId - 用户 ID
 * @param {Long} bookId - 书籍 ID
 */
export function deleteCartItem(userId, bookId) {
  return request.delete('/api/cart/delete', { params: { userId, bookId } })
}

/**
 * 批量删除购物车商品
 * @param {Object} data - 删除数据
 * @param {Long} data.userId - 用户 ID
 * @param {Array<Long>} data.bookIds - 书籍 ID 列表
 */
export function batchDeleteCartItem(data) {
  return request.post('/api/cart/batch-delete', data)
}

/**
 * 清空购物车
 * @param {Long} userId - 用户 ID
 */
export function clearCart(userId) {
  return request.delete('/api/cart/clear', { params: { userId } })
}
