import request from '../../utils/request'

// 销售汇总
export function getSalesSummary() {
    return request({
        url: '/api/statistics/sales/summary',
        method: 'get'
    })
}

// 按日期销售额统计（支持日期范围）
export function getSalesByDate(startDate, endDate) {
    return request({
        url: '/api/statistics/sales/by-date',
        method: 'get',
        params: { startDate, endDate }
    })
}

// 按日期范围获取销售汇总
export function getSalesSummaryByDateRange(startDate, endDate) {
    return request({
        url: '/api/statistics/sales/summary-by-date',
        method: 'get',
        params: { startDate, endDate }
    })
}

// 书籍销量排行榜
export function getTopBooks(limit = 10) {
    return request({
        url: '/api/statistics/sales/top-books',
        method: 'get',
        params: { limit }
    })
}

// 分类销量排行榜
export function getTopCategories() {
    return request({
        url: '/api/statistics/sales/top-categories',
        method: 'get'
    })
}

// 分类销量趋势
export function getCategorySalesTrend() {
    return request({
        url: '/api/statistics/sales/category-trend',
        method: 'get'
    })
}

// 订单状态分布
export function getOrderStatusDistribution() {
    return request({
        url: '/api/statistics/order/status-distribution',
        method: 'get'
    })
}

// 支付方式分布
export function getPayTypeDistribution() {
    return request({
        url: '/api/statistics/order/pay-type-distribution',
        method: 'get'
    })
}

// 每日新增用户报表
export function getDailyUserReport() {
    return request({
        url: '/api/statistics/user/daily-report',
        method: 'get'
    })
}

export function exportTopBooks(limit = 10) {
    return request({
        url: '/api/statistics/export/top-books',
        method: 'get',
        params: { limit },
        responseType: 'blob'
    })
}

export function exportTopCategories() {
    return request({
        url: '/api/statistics/export/top-categories',
        method: 'get',
        responseType: 'blob'
    })
}

export function exportCategoryTrend() {
    return request({
        url: '/api/statistics/export/category-trend',
        method: 'get',
        responseType: 'blob'
    })
}
