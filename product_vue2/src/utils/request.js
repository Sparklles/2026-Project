import axios from 'axios'
import { Message, MessageBox } from 'element-ui'

// 1. 创建 axios 实例
const service = axios.create({
    // 根据环境变量决定基础路径 (开发环境指向后端 8080 端口，生产环境指向域名)
    // 可以在 vue.config.js 中配置 proxy 代理解决跨域，或者直接依赖后端的 CorsConfig
    baseURL: process.env.VUE_APP_BASE_API || 'http://localhost:8083',
    timeout: 10000 // 请求超时时间
})

// 2. request 请求拦截器
service.interceptors.request.use(
    config => {
        // 假设将 userId 或 Token 存在了 localStorage 中
        const userId = localStorage.getItem('userId')
        if (userId) {
            // 对应后端 ReviewFrontController 中 @RequestHeader("userId") 的要求
            config.headers['userId'] = userId
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
        // res 就是我们在后端定义的 Result<T> 对象: { code, message, data }
        const res = response.data

        // 如果后端的 code 不等于 200，说明业务逻辑出错了（比如防刷单被拦截）
        if (res.code !== 200) {
            Message({
                message: res.message || 'Error',
                type: 'error',
                duration: 5 * 1000
            })

            // 401: Token 过期或未登录
            if (res.code === 401) {
                MessageBox.confirm('您已登出，或者登录已失效，请重新登录', '确认注销', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    // 清除本地缓存并跳转到登录页
                    localStorage.removeItem('userId')
                    location.reload()
                })
            }
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            // 一切正常，直接剥离最外层的 Result，把核心的 data 数据返回给页面组件
            return res.data
        }
    },
    error => {
        console.log('err' + error)
        Message({
            message: '网络异常或服务器宕机，请稍后再试',
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
