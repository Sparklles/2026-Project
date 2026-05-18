import request from '@/utils/request'

/**
 * 前台商品多维度搜索
 * @param {Object} params - 包含 keyword, current, size 等分页和搜索参数
 */
export function searchBooks(params) {
    return request({
        url: '/api/front/book/search',
        method: 'get',
        params: {
            keyword: params.keyword,
            current: params.current || 1,
            size: params.size || 20,
            sortField: params.sortField,
            sortOrder: params.sortOrder
        }
    })
}

// 获取所有书籍分类（对应首页左侧菜单）
export function getCategories() {
    return request({
        url: '/api/front/book/categories',
        method: 'get',
        silentError: true
    })
}

// 前台书籍高级筛选（备用，可按标签、价格、排序等筛选）
export function listBooks(params) {
    return request({
        url: '/api/front/book/list',
        method: 'get',
        params
    })
}

/**
 * 获取商品详情（书籍详情）
 * @param {number|string} id - 商品ID
 */
export function getBookDetail(id) {
    return request({
        url: `/api/front/book/${id}`,
        method: 'get'
    })
}
// 首页推荐数据：主题轮播、畅销榜、新书上架
export function getHomeRecommend() {
    return request({
        url: '/api/front/recommend/home',
        method: 'get',
        silentError: true
    })
}

// 商品详情页关联推荐
export function getAlsoBought(bookId) {
    return request({
        url: '/api/front/recommend/also-bought',
        method: 'get',
        params: { bookId }
    })
}

// 个人中心个性化推荐
export function getPersonalized() {
    return request({
        url: '/api/front/recommend/personalized',
        method: 'get',
        silentError: true
    })
}


