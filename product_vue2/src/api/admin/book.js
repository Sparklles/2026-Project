import request from '../../utils/request'

// 录入新书
export function addBook(data) {
    return request({
        url: '/api/admin/books',
        method: 'post',
        data: data // 包含 title, price, tagIds 等
    })
}

// 修改书籍
export function updateBook(data) {
    return request({
        url: '/api/admin/books',
        method: 'put',
        data: data
    })
}

// 更改上下架状态
export function changeBookStatus(id, status) {
    return request({
        url: `/api/admin/books/${id}/status`,
        method: 'put',
        params: { status }
    })
}

// 删除书籍
export function deleteBook(id) {
    return request({
        url: `/api/admin/books/${id}`,
        method: 'delete'
    })
}
