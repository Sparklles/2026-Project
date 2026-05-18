import request from '../../utils/request'

// 提交评价 (防刷单逻辑已在后端)
export function submitReview(data) {
    return request({
        url: '/api/reviews/submit',
        method: 'post',
        data: data // { bookId, rating, content }
    })
}

// 分页获取某本书的评价列表
export function getBookReviews(bookId, query) {
    return request({
        url: `/api/reviews/book/${bookId}`,
        method: 'get',
        params: query // { current: 1, size: 10 }
    })
}