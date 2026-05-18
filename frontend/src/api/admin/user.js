import request from '../../utils/request'

/**
 * 管理员登录
 * @param {Object} data { account, password, type, expectedRole }
 */
export function login(data) {
    return request({
        url: '/api/login/admin',
        method: 'post',
        data
    })
}

/**
 * 获取当前登录管理员信息
 */
export function getAdminProfile() {
    return request({
        url: '/api/admin/profile/me',
        method: 'get'
    })
}

/**
 * 分页搜索用户
 * @param {Object} params { loginAccount, email, phone, pageNum, pageSize }
 */
export function pageUsers(params) {
    return request({
        url: '/api/admin/search/users/page',
        method: 'get',
        params
    })
}

/**
 * 查询任意用户的完整信息
 * @param {Number|String} userId 目标用户 ID
 */
export function getUserProfile(userId) {
    return request({
        url: `/api/admin/profile/users/${userId}`,
        method: 'get'
    })
}

/**
 * 修改用户状态（冻结/解冻）
 * @param {Number|String} userId 目标用户 ID
 * @param {Object} data { status, reasonType, reasonDetail }
 */
export function updateUserStatus(userId, data) {
    return request({
        url: `/api/admin/profile/users/${userId}/status`,
        method: 'put',
        data
    })
}

/**
 * 分页查询账号状态变更审计日志
 * @param {Object} params 查询参数
 */
export function pageUserStatusAudit(params) {
    return request({
        url: '/api/admin/user-status-audit/page',
        method: 'get',
        params
    })
}
