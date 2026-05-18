import axios from 'axios'
import { Message, MessageBox } from 'element-ui'

// 1. 创建 axios 实例
const service = axios.create({
    // 配合 vue.config.js 中的 proxy 代理，这里设为空即可触发转发
    baseURL: process.env.VUE_APP_BASE_API || '',
    timeout: 60000 // 请求超时时间
})

// 2. request 请求拦截器
service.interceptors.request.use(
    config => {
        // 根据请求路径识别是管理端接口还是用户端接口；必要时允许 API 显式指定 tokenKey
        const requestUrl = config.url || ''
        const isAdminApi = requestUrl.startsWith('/api/admin') || requestUrl.startsWith('/api/statistics') || requestUrl.includes('/admin') || requestUrl.startsWith('/statistics')
        const tokenKey = config.tokenKey || (isAdminApi ? 'admin-token' : 'user-token')

        const token = localStorage.getItem(tokenKey)
        if (token) {
            config.headers['access-token'] = token
        }

        return config
    },
    error => {
        console.log(error)
        return Promise.reject(error)
    }
)

// 3. response 响应拦截器
service.interceptors.response.use(
    response => {
        const userToken = response.headers['user-token']
        const adminToken = response.headers['admin-token']

        if (userToken) {
            localStorage.setItem('user-token', userToken)
        }
        if (adminToken) {
            localStorage.setItem('admin-token', adminToken)
        }

        if (response.config.responseType === 'blob') {
            return response
        }

        // res 就是我们在后端定义的 Result<T> 对象
        const res = response.data
        const silentError = response.config.silentError === true

        // 🌟 核心排错法宝：在控制台打印后端真实返回的完整结构
        console.log('【Axios响应拦截】后端返回的真实数据:', res)

        // 🌟 高容错数组：允许 200数字, "200"字符串, 20000, 0 等所有常见的成功码
        const successCodes = [200, '200', 20000, '20000', 0, '0']

        // 如果后端返回的 code 不在这个“成功白名单”里，说明业务逻辑出错了
        if (!successCodes.includes(res.code)) {

            // 兼容有些后端使用 msg 而不是 message 的情况
            const errorMsg = res.message || res.msg || ('请求失败，状态码：' + res.code)

            if (!silentError) {
                Message({
                    message: errorMsg,
                    type: 'error',
                    duration: 5 * 1000
                })
            }

            // 401: Token 过期或未登录
            if ((res.code === 401 || res.code === '401') && !silentError) {
                MessageBox.confirm('您已登出，或者登录已失效，请重新登录', '确认注销', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    localStorage.removeItem('userId')
                    localStorage.removeItem('user-token')
                    localStorage.removeItem('admin-token')
                    localStorage.removeItem('token')
                    location.reload()
                })
            }
            return Promise.reject(new Error(errorMsg))
        } else {
            // 一切正常，直接剥离最外层的 Result，把核心的 data 数据返回给页面组件
            return res.data
        }
    },
    error => {
        console.log('【Axios底层网络报错】' + error)
        const silentError = error.config && error.config.silentError === true
        if (!silentError) {
            Message({
                message: '网络异常或服务器宕机，请稍后再试',
                type: 'error',
                duration: 5 * 1000
            })
        }
        return Promise.reject(error)
    }
)

export default service

