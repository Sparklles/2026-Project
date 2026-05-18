import request from '@/utils/request'

/**
 * 获取当前用户的所有收货地址
 */
export function getAddressList() {
  return request({
    url: '/api/address/list',
    method: 'get'
  })
}

/**
 * 新增收货地址
 * @param {Object} data 
 */
export function addAddress(data) {
  return request({
    url: '/api/address',
    method: 'post',
    data
  })
}

/**
 * 修改已有收货地址
 * @param {Number|String} id
 * @param {Object} data 
 */
export function updateAddress(id, data) {
  return request({
    url: `/api/address/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除收货地址
 * @param {Number|String} id 
 */
export function deleteAddress(id) {
  return request({
    url: `/api/address/${id}`,
    method: 'delete'
  })
}

/**
 * 设为默认地址
 * @param {Number|String} id 
 */
export function setDefaultAddress(id) {
  return request({
    url: `/api/address/default/${id}`,
    method: 'put'
  })
}
