import request from '@/utils/request'

/**
 * 普通用户登录
 * @param {Object} data { account, password, type }
 * type: 1 为手机号密码，2 为用户名密码
 */
export function userLogin(data) {
  return request({
    url: '/api/login/user',
    method: 'post',
    data
  })
}

/**
 * 用户退出登录
 */
export function userLogout() {
  return request({
    url: '/api/logout/user',
    method: 'post'
  })
}

/**
 * 用户注册
 * @param {Object} data { account, password, type }
 */
export function userRegister(data) {
  return request({
    url: '/api/register/user',
    method: 'post',
    data
  })
}

/**
 * 获取登录公钥
 */
export function getPublicKey() {
  return request({
    url: '/api/auth/public-key',
    method: 'get'
  })
}

/**
 * 查询当前登录用户的个人信息
 */
export function getUserProfile(options = {}) {
  return request({
    url: '/api/profile/me',
    method: 'get',
    ...options
  })
}

/**
 * 上传用户头像，仅返回头像 URL，不直接写入用户资料
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/profile/avatar/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
/**
 * 修改个人信息
 */
export function updateUserProfile(data) {
  return request({
    url: '/api/profile/me',
    method: 'put',
    data
  })
}

/**
 * 修改电子邮箱
 */
export function updateEmail(email) {
  return request({
    url: '/api/profile/email',
    method: 'put',
    data: { email } // 修改为放在请求体 (Body) 中
  })
}

/**
 * 修改密码
 */
export function updatePassword(data) {
  return request({
    url: '/api/profile/password',
    method: 'put',
    data
  })
}





