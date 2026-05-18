import request from '../../utils/request'

export function listRecommendConfigs() {
  return request({
    url: '/api/admin/recommend-configs',
    method: 'get'
  })
}

export function getRecommendConfig(id) {
  return request({
    url: `/api/admin/recommend-configs/${id}`,
    method: 'get'
  })
}

export function createRecommendConfig(data) {
  return request({
    url: '/api/admin/recommend-configs',
    method: 'post',
    data
  })
}

export function updateRecommendConfig(data) {
  return request({
    url: '/api/admin/recommend-configs',
    method: 'put',
    data
  })
}

export function deleteRecommendConfig(id) {
  return request({
    url: `/api/admin/recommend-configs/${id}`,
    method: 'delete'
  })
}

export function toggleRecommendConfigStatus(id, status) {
  return request({
    url: `/api/admin/recommend-configs/${id}/status`,
    method: 'put',
    params: { status }
  })
}
export function refreshRecommendData() {
  return request({
    url: '/api/admin/recommend-configs/refresh',
    method: 'post'
  })
}
