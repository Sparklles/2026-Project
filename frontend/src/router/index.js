import Vue from 'vue'
import VueRouter from 'vue-router'
import ProductDetail from "@/view/front/ProductDetail.vue";
import AdminLayout from "@/view/admin/AdminLayout.vue";

// 告诉 Vue 使用路由插件
Vue.use(VueRouter)

// 定义路由规则
const routes = [
    // ================= 前台用户页面 =================
    {
        path: '/',
        name: 'Home',
        component: () => import('@/view/front/Home.vue') // 首页
    },
    {
        path: '/login',
        name: 'UserLogin',
        component: () => import('@/view/front/Login.vue') // 用户登录
    },
    {
        path: '/register',
        name: 'UserRegister',
        component: () => import('@/view/front/Register.vue') // 用户注册
    },
    {
        path: '/search',
        name: 'Search',
        component: () => import('@/view/front/Search.vue') // 搜索页
    },
    {
        path: '/cart',
        name: 'UserCart',
        component: () => import('@/view/front/Cart.vue') // 购物车
    },
    {
        path: '/checkout',
        name: 'UserCheckout',
        component: () => import('@/view/front/Checkout.vue') // 确认订单页
    },
    {
        path: '/pay',
        name: 'UserPayment',
        component: () => import('@/view/front/Payment.vue') // 支付页面
    },
    {
        path: '/user',
        name: 'UserCenter',
        component: () => import('@/view/front/UserCenter.vue'), // 个人中心
        children: [
            {
                path: 'profile',
                name: 'UserProfile',
                component: () => import('@/view/front/UserProfile.vue') // 个人信息修改
            },
            {
                path: 'password',
                name: 'UserPassword',
                component: () => import('@/view/front/UserPassword.vue') // 修改密码
            },
            {
                path: 'address',
                name: 'UserAddress',
                component: () => import('@/view/front/UserAddress.vue') // 收货地址管理
            },
            {
                path: 'orders',
                name: 'OrderList',
                component: () => import('@/view/front/OrderList.vue') // 订单列表
            },
            {
                path: 'order-detail',
                name: 'UserOrderDetail',
                component: () => import('@/view/front/OrderDetail.vue') // 订单详情
            },
            {
                path: 'favorites',
                name: 'UserFavorites',
                component: () => import('@/view/front/Favorites.vue') // 我的收藏
            },
            {
                path: 'reviews',
                name: 'ReviewManage',
                component: () => import('@/view/front/ReviewManage.vue') // 评价管理
            },
            {
                path: 'refund',
                name: 'RefundManage',
                component: () => import('@/view/front/RefundManage.vue') // 退款/售后
            },
            {
                path: 'refund-apply',
                name: 'RefundApply',
                component: () => import('@/view/front/RefundApply.vue') // 申请售后
            },
            {
                path: 'refund-detail',
                name: 'RefundDetail',
                component: () => import('@/view/front/RefundDetail.vue') // 售后详情
            }
        ]
    },
    // 添加详情页路由，使用 :id 作为占位符
    {
        path: '/product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
        props: true // 允许将 id 作为 props 传给组件
    },
    {
        path: '/admin',
        component: AdminLayout,
        redirect: '/admin/book-list',
        children: [
            {
                path: 'book-list',
                name: 'AdminBookList',
                component: () => import('@/view/admin/BookList.vue')
            },
            {
                path: 'review-audit',
                name: 'AdminReviewAudit',
                component: () => import('@/view/admin/ReviewAudit.vue')
            },
            {
                path: 'dict-manage',
                name: 'AdminDictManage',
                component: () => import('@/view/admin/DictManage.vue')
            },
            {
                path: 'user-search',
                name: 'AdminUserSearch',
                component: () => import('@/view/admin/UserSearch.vue')
            },
            {
                path: 'user-detail/:id',
                name: 'AdminUserDetail',
                component: () => import('@/view/admin/UserDetail.vue')
            },
            {
                path: 'user-status-audit',
                name: 'AdminUserStatusAudit',
                component: () => import('@/view/admin/UserStatusAudit.vue')
            },
            {
                path: 'statistics-dashboard',
                name: 'AdminStatisticsDashboard',
                component: () => import('@/view/admin/StatisticsDashboard.vue')
            },
            {
                path: 'daily-user-report',
                name: 'AdminDailyUserReport',
                component: () => import('@/view/admin/DailyUserReport.vue')
            },
            {
                path: 'recommend-config',
                name: 'AdminRecommendConfig',
                component: () => import('@/view/admin/RecommendConfig.vue')
            },
            {
                path: 'order-manage',
                name: 'AdminOrderManage',
                component: () => import('@/view/admin/OrderManage.vue')
            },
            {
                path: 'refund-manage',
                name: 'AdminRefundManage',
                component: () => import('@/view/admin/RefundManage.vue')
            },
            {
                path: 'notification-center',
                name: 'AdminNotificationCenter',
                component: () => import('@/view/admin/NotificationCenter.vue')
            }
        ]
    },
    {
        path: '/admin/login',
        name: 'AdminLogin',
        component: () => import('@/view/admin/AdminLogin.vue')
    }
]

// 创建路由实例
const router = new VueRouter({
    mode: 'history', // 去掉 URL 中的 # 号
    base: process.env.BASE_URL,
    routes
})

/**
 * 校验 token 是否有效且未过期
 * @param {String} tokenKey localStorage 中的键名 ('user-token' 或 'admin-token')
 * @returns {Boolean}
 */
function isTokenValid(tokenKey) {
    const token = localStorage.getItem(tokenKey);
    if (!token) {
        return false;
    }

    try {
        // JWT 的结构为 header.payload.signature，取第二部分
        const payloadBase64Url = token.split('.')[1];
        if (!payloadBase64Url) return false;

        // 将 Base64Url 转换为标准 Base64
        const base64 = payloadBase64Url.replace(/-/g, '+').replace(/_/g, '/');
        // 解码并解析 JSON (兼容含有中文字符的 payload)
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        const payload = JSON.parse(jsonPayload);

        // 获取当前时间的秒级时间戳
        const currentTime = Math.floor(Date.now() / 1000);

        // 判断是否过期
        if (payload.exp && payload.exp < currentTime) {
            localStorage.removeItem(tokenKey); // 已过期，立即删除
            return false;
        }

        return true;
    } catch (e) {
        // 解析失败，说明 token 格式错误或损坏
        localStorage.removeItem(tokenKey);
        return false;
    }
}

// 导航守卫，控制路由权限
router.beforeEach((to, from, next) => {
    // 1. 普通用户逻辑：已登录则禁止访问登录、注册、找回密码等页面
    const guestOnlyRoutes = ['/login', '/register', '/forgot-password'];
    if (isTokenValid('user-token') && guestOnlyRoutes.includes(to.path)) {
        return next('/');
    }

    // 2. 管理员逻辑：已登录则禁止访问管理员登录页
    if (isTokenValid('admin-token') && to.path === '/admin/login') {
        return next('/admin'); // 跳转到后台主页（/admin 会自动重定向到默认子路由）
    }

    next();
});



// 解决 Vue Router 3.x 版本中重复点击同一个路由报错的问题
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err)
}

export default router

