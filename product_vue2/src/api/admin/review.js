import request from '../../utils/request'

/**
 * 1. 后台多维度筛选评价列表
 * @param {Object} query 包含分页参数(current, size)和检索条件(rating, status)
 * @returns Promise
 */
export function getAdminReviewList(query) {
    return request({
        url: '/api/admin/reviews',
        method: 'get',
        params: query // GET 请求的参数放在 URL 问号后面，Axios 用 params 接收
    })
}

/**
 * 2. 隐藏违规评论
 * @param {Number|String} id 评价记录的 ID
 * @returns Promise
 */
export function hideReview(id) {
    return request({
        // 使用 ES6 模板字符串拼接 URL 中的 PathVariable
        url: `/api/admin/reviews/${id}/hide`,
        method: 'put'
    })
}

/**
 * 3. 官方身份回复评论
 * @param {Number|String} id 评价记录的 ID
 * @param {String} replyContent 回复的文字内容
 * @returns Promise
 */
export function replyReview(id, replyContent) {
    return request({
        url: `/api/admin/reviews/${id}/reply`,
        method: 'post',
        // 💡 重点注意：我们在后端使用的是 @RequestParam("replyContent")
        // 所以即使是 POST 请求，前端也需要把参数作为 query 传过去（用 params，而不是 data）
        params: {
            replyContent: replyContent
        }
    })
}