<template>
  <div class="user-center-page">
    <header class="uc-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代商城</span>
        </div>
        <div class="search-area">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索商品/订单..."
              size="small"
              class="header-search">
            <el-button slot="append" icon="el-icon-search"></el-button>
          </el-input>
        </div>
      </div>
    </header>

    <div class="uc-container">

      <aside class="uc-sidebar">
        <el-menu :default-active="$route.path" class="sidebar-menu" :collapse="false" @select="handleSelect">
          <el-menu-item-group title="我的交易">
            <el-menu-item index="/user"><i class="el-icon-user"></i> 个人中心</el-menu-item>
            <el-menu-item index="/user/orders"><i class="el-icon-s-order"></i> 已买到的宝贝</el-menu-item>
            <el-menu-item index="cart-item" @click.native.capture.stop="goToCart"><i class="el-icon-shopping-cart-2"></i> 我的购物车</el-menu-item>
            <el-menu-item index="/user/favorites"><i class="el-icon-star-on"></i> 我的收藏</el-menu-item>
          </el-menu-item-group>

          <el-menu-item-group title="我的服务">
            <el-menu-item index="/user/address"><i class="el-icon-location"></i> 收货地址</el-menu-item>
            <el-menu-item index="/user/reviews"><i class="el-icon-chat-dot-square"></i> 评价管理</el-menu-item>
            <el-menu-item index="/user/refund"><i class="el-icon-service"></i> 退款/售后</el-menu-item>
          </el-menu-item-group>

          <el-menu-item-group title="账户设置">
            <el-menu-item index="/user/profile"><i class="el-icon-edit-outline"></i> 个人信息</el-menu-item>
            <el-menu-item index="/user/password"><i class="el-icon-lock"></i> 修改密码</el-menu-item>
          </el-menu-item-group>
        </el-menu>
      </aside>

      <main class="uc-main">
        
        <template v-if="$route.name === 'UserCenter'">
          <div class="profile-card">
            <div class="user-info">
            <el-avatar :size="70" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="avatar"></el-avatar>
            <div class="user-text">
              <h2 class="username">{{ userInfo.nickname || userInfo.account || 'tb_航海家Alex' }}</h2>
              <div class="user-tags">
                <span class="level-tag"><i class="el-icon-medal-1"></i> 高级船长 V4</span>
                <span class="address-link" @click="goToAddress">收货地址管理 <i class="el-icon-arrow-right"></i></span>
              </div>
            </div>
          </div>
          <div class="user-assets">
            <div class="asset-item">
              <span class="val">24<small>元</small></span>
              <span class="label">航海津贴</span>
            </div>
            <div class="asset-item">
              <span class="val">2<small>张</small></span>
              <span class="label">优惠券</span>
            </div>
            <div class="asset-item">
              <span class="val">6.74<small>元</small></span>
              <span class="label">淘金币抵扣</span>
            </div>
          </div>
        </div>

        <div class="dashboard-row">

            <div class="order-overview card-panel">
              <div class="panel-header">
                <h3>我的订单</h3>
                <a href="javascript:;" class="view-all" @click="goToOrders">查看全部订单 <i class="el-icon-arrow-right"></i></a>
              </div>
              <div class="order-status-list">
              <div class="status-item" @click="goToOrders">
                <el-badge :value="orderStats.unpaid" :hidden="orderStats.unpaid === 0" class="status-badge">
                  <i class="el-icon-wallet"></i>
                </el-badge>
                <span>待付款</span>
              </div>
              <div class="status-item" @click="goToOrders">
                <el-badge :value="orderStats.unshipped" :hidden="orderStats.unshipped === 0" class="status-badge">
                  <i class="el-icon-box"></i>
                </el-badge>
                <span>待发货</span>
              </div>
              <div class="status-item" @click="goToOrders">
                <el-badge :value="orderStats.shipped" :hidden="orderStats.shipped === 0" class="status-badge">
                  <i class="el-icon-truck"></i>
                </el-badge>
                <span>待收货</span>
              </div>
              <div class="status-item" @click="goToOrders">
                <el-badge :value="orderStats.toReview" :hidden="orderStats.toReview === 0" class="status-badge">
                  <i class="el-icon-chat-dot-round"></i>
                </el-badge>
                <span>待评价</span>
              </div>
              <div class="status-item" @click="$router.push('/user/refund')">
                <el-badge :value="orderStats.refund" :hidden="orderStats.refund === 0" class="status-badge">
                  <i class="el-icon-service"></i>
                </el-badge>
                <span>退款/售后</span>
              </div>
            </div>
            <div class="logistics-marquee" v-if="latestOrderTip">
              <i class="el-icon-truck"></i>
              <span class="logistics-text">{{ latestOrderTip }}</span>
            </div>
          </div>

          <div class="quick-links card-panel">
            <div class="quick-col" @click="$router.push('/user/favorites')">
              <div class="col-header">
                <h3>我的收藏</h3>
                <i class="el-icon-arrow-right"></i>
              </div>
              <div class="col-preview">
                <img src="https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=150&q=80" alt="收藏商品">
                <div class="p-info">
                  <p class="p-title">高级海图作业...</p>
                  <p class="p-price">¥32.5</p>
                </div>
              </div>
            </div>
            <div class="vertical-divider"></div>

            <div class="quick-col" @click="goToCart">
              <div class="col-header">
                <h3>购物车</h3>
                <i class="el-icon-arrow-right"></i>
              </div>
              <div class="col-preview">
                <img src="https://images.unsplash.com/photo-1534067783941-51c9c23ecefd?w=150&q=80" alt="购物车商品">
                <div class="p-info">
                  <p class="p-title">纯黄铜六分仪...</p>
                  <p class="p-price">¥1299</p>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div class="recommend-section">
          <div class="section-title">
            <i class="el-icon-magic-stick" style="color: #ff5000;"></i> 猜你喜欢 · 为您推荐
          </div>
          <div class="recommend-grid">
            <div class="rec-card" v-for="item in recommendList" :key="getBookId(item)" @click="goToProduct(getBookId(item))">
              <div class="img-wrap">
                <img :src="getBookImage(item)" alt="推荐商品">
              </div>
              <div class="rec-info">
                <p class="rec-title">{{ item.title }}</p>
                <p class="rec-author">{{ item.author }}</p>
                <div class="rec-tags">
                  <span class="difficulty-tag" v-if="item.difficultyTag != null" :class="'level-' + item.difficultyTag">{{ getDifficultyText(item.difficultyTag) }}</span>
                  <span class="rec-tag" v-for="tag in (item.tags || [])" :key="tag">{{ tag }}</span>
                </div>
                <div class="rec-bottom">
                  <span class="rec-price"><small>¥</small>{{ item.price }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        </template>
        
        <!-- 子路由组件展示区 -->
        <router-view v-else></router-view>

      </main>
    </div>
  </div>
</template>

<script>
import { getUserProfile } from '@/api/front/user';
import { getPersonalized } from '@/api/front/book';
import { getRefundList } from '@/api/front/refund';
import request from '@/utils/request';

export default {
  name: 'UserCenter',
  data() {
    return {
      searchKeyword: '',
      userInfo: {},
      userId: null,
      orderStats: {
        unpaid: 0,
        unshipped: 0,
        shipped: 0,
        toReview: 0,
        refund: 0
      },
      latestOrderTip: '',
      recommendList: []
    }
  },
  async created() {
    await this.fetchUserInfo();
    this.fetchOrderOverviewIfNeeded();
    this.fetchPersonalizedIfNeeded();
  },
  watch: {
    '$route.name'() {
      this.fetchOrderOverviewIfNeeded()
      this.fetchPersonalizedIfNeeded()
    }
  },
  methods: {
    async fetchUserInfo() {
      try {
        const res = await getUserProfile();
        this.userInfo = res || {};
        this.userId = res && (res.userId || res.id);
      } catch (error) {
        console.error('获取用户信息失败', error);
      }
    },
    getBookId(book) {
      return book.id || book.bookId || book.targetId
    },

    getBookImage(book) {
      return book.coverImageUrl || book.image || book.imageUrl || 'https://images.unsplash.com/photo-1582921017967-79d1cb6702e0?w=300&q=80'
    },

    getFallbackRecommendList() {
      return [
        {
          id: 101,
          title: '航海学基础（第4版）',
          image: 'https://images.unsplash.com/photo-1582921017967-79d1cb6702e0?w=300&q=80',
          price: '68.50',
          tags: ['基础理论']
        },
        {
          id: 102,
          title: '专业远洋防风望远镜',
          image: 'https://images.unsplash.com/photo-1473186578172-c141e6798cf4?w=300&q=80',
          price: '399.00',
          tags: ['航海装备']
        },
        {
          id: 103,
          title: '国际海事信号旗套装',
          image: 'https://images.unsplash.com/photo-1505228395891-9a51e7e86bf6?w=300&q=80',
          price: '128.00',
          tags: ['海事用品']
        },
        {
          id: 104,
          title: '船用高精度气压计',
          image: 'https://images.unsplash.com/photo-1605281317010-e2583ffc6178?w=300&q=80',
          price: '245.00',
          tags: ['航海仪器']
        },
        {
          id: 105,
          title: '航海防晕船贴/药箱',
          image: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=300&q=80',
          price: '35.90',
          tags: ['出航必备']
        }
      ]
    },

    normalizeRecommendList(data) {
      const list = Array.isArray(data)
        ? data
        : (data && (data.records || data.list || data.rows || data.data))
      return Array.isArray(list) ? list.filter(item => this.getBookId(item)) : []
    },

    applyPersonalizedData(data) {
      const list = this.normalizeRecommendList(data)
      this.recommendList = list.length > 0 ? list : this.getFallbackRecommendList()
    },

    fetchOrderOverviewIfNeeded() {
      if (this.$route.name !== 'UserCenter' || !this.userId) {
        return
      }
      this.fetchOrderOverview()
      this.fetchRefundOverview()
    },

    async fetchOrderOverview() {
      try {
        const res = await request.get('/api/order/list/all', {
          params: { userId: this.userId }
        })
        const orders = Array.isArray(res) ? res : (res && (res.records || res.list || res.data)) || []
        this.orderStats.unpaid = orders.filter(order => Number(order.orderStatus) === 1).length
        this.orderStats.unshipped = orders.filter(order => Number(order.orderStatus) === 2).length
        this.orderStats.shipped = orders.filter(order => Number(order.orderStatus) === 3).length
        this.orderStats.toReview = orders.filter(order => Number(order.orderStatus) === 4).length

        const latestShipped = orders.find(order => Number(order.orderStatus) === 3)
        this.latestOrderTip = latestShipped
          ? `您的订单 [${this.getFirstOrderTitle(latestShipped)}] 已发货，请留意物流更新`
          : ''
      } catch (error) {
        console.error('获取订单概览失败', error)
      }
    },

    async fetchRefundOverview() {
      try {
        const res = await getRefundList({ page: 1, pageSize: 1 })
        const data = res && (res.data || res)
        const list = Array.isArray(data) ? data : (data && (data.records || data.list || data.rows)) || []
        this.orderStats.refund = Number(data && (data.total || data.totalCount || data.count)) || list.length
      } catch (error) {
        console.error('获取退款售后概览失败', error)
      }
    },

    getFirstOrderTitle(order) {
      const item = order && order.items && order.items[0]
      return (item && (item.bookTitle || item.title)) || '订单商品'
    },

    fetchPersonalizedIfNeeded() {
      if (this.$route.name !== 'UserCenter' || this.recommendList.length > 0) {
        return
      }
      this.fetchPersonalized()
    },

    async fetchPersonalized() {
      try {
        const res = await getPersonalized();
        this.applyPersonalizedData(res)
      } catch (error) {
        if (error.responseCode === 401) {
          this.$router.replace('/login')
          return
        }
        console.error('获取个人推荐失败，已使用兜底推荐', error);
        this.applyPersonalizedData([])
      }
    },
    goToAddress() {
      this.$router.push('/user/address');
    },
    goToOrders() {
      this.$router.push('/user/orders');
    },
    goToCart() {
      // 获取完整的跳转路径
      const routeData = this.$router.resolve({ path: '/cart' });
      window.open(routeData.href, '_blank');
    },
    handleSelect(index) {
      this.$router.push(index).catch(() => {});
    },
    goToProduct(bookId) {
      const routeUrl = this.$router.resolve({
        name: 'ProductDetail',
        params: { id: bookId }
      });
      window.open(routeUrl.href, '_blank');
    },
    getDifficultyText(tag) {
      if (tag == null) return ''
      const map = { 0: '全水平', 1: '入门', 2: '中级', 3: '高级' }
      return map[tag] || ''
    }
  }
}
</script>

<style scoped>
.user-center-page {
  background-color: #f4f7f9;
  min-height: 100vh;
  padding-bottom: 40px;
}

/* ================== 1. 顶部导航 ================== */
.uc-header {
  background-color: #ff5000; /* 淘宝标志性的顶部色，或改用航海蓝 #1890ff */
  background: linear-gradient(90deg, #ff9000, #ff5000);
  padding: 15px 0;
}
.header-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo-area {
  display: flex;
  align-items: center;
  color: #fff;
  cursor: pointer;
}
.logo-icon {
  font-size: 32px;
  margin-right: 10px;
}
.logo-text {
  font-size: 22px;
  font-weight: bold;
}
.header-search {
  width: 400px;
}
.header-search ::v-deep .el-input-group__append {
  background-color: #fff;
  color: #ff5000;
  border: none;
}
.header-search ::v-deep .el-input__inner {
  border: none;
}

/* ================== 2. 核心布局 ================== */
.uc-container {
  width: 1200px;
  margin: 20px auto;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

/* 左侧边栏 */
.uc-sidebar {
  width: 180px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.sidebar-menu {
  border-right: none;
}
.sidebar-menu ::v-deep .el-menu-item-group__title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  padding-top: 15px;
}
.sidebar-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  font-size: 13px;
  color: #666;
}
.sidebar-menu .el-menu-item.is-active {
  color: #ff5000;
  background-color: #fffaf7;
  font-weight: bold;
}
.sidebar-menu .el-menu-item:hover {
  color: #ff5000;
}

/* 右侧主内容区 */
.uc-main {
  width: 1000px;
}
.card-panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

/* ================== A. 用户信息卡片 ================== */
.profile-card {
  background: #fff url('https://img.alicdn.com/tfs/TB1L5vQoaL0gK0jSZFtXXXQCXXa-1000-150.png') no-repeat right top;
  background-size: cover;
  border-radius: 12px;
  padding: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.username {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}
.user-tags {
  display: flex;
  align-items: center;
  gap: 15px;
}
.level-tag {
  background: linear-gradient(90deg, #ffd000, #ff9900);
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: bold;
}
.address-link {
  font-size: 12px;
  color: #666;
  cursor: pointer;
}
.address-link:hover {
  color: #ff5000;
}
.user-assets {
  display: flex;
  gap: 40px;
  background: rgba(255,255,255,0.8);
  padding: 15px 30px;
  border-radius: 8px;
}
.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.asset-item .val {
  font-size: 20px;
  font-weight: bold;
  color: #ff5000;
  margin-bottom: 4px;
}
.asset-item .val small {
  font-size: 12px;
  font-weight: normal;
}
.asset-item .label {
  font-size: 12px;
  color: #666;
}

/* ================== B. 订单概览 & 快捷入口 ================== */
.dashboard-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

/* B1. 订单区 */
.order-overview {
  flex: 6;
  padding: 20px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}
.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}
.view-all {
  font-size: 12px;
  color: #999;
  text-decoration: none;
}
.view-all:hover {
  color: #ff5000;
}
.order-status-list {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}
.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #333;
  transition: color 0.2s;
}
.status-item:hover {
  color: #ff5000;
}
.status-badge i {
  font-size: 32px;
  color: #1890ff; /* 航海蓝 */
  margin-bottom: 10px;
}
.status-item span {
  font-size: 13px;
}

/* 底部最新物流滚动区 */
.logistics-marquee {
  background: #f8f8f8;
  border-radius: 4px;
  padding: 10px 15px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logistics-marquee i {
  color: #1890ff;
}

/* B2. 快捷模块 (收藏与购物车) */
.quick-links {
  flex: 4;
  padding: 20px;
  display: flex;
  align-items: center;
}
.quick-col {
  flex: 1;
  cursor: pointer;
  padding: 0 10px;
  transition: transform 0.2s;
}
.quick-col:hover {
  transform: translateY(-2px);
}
.col-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.col-header h3 {
  margin: 0;
  font-size: 15px;
  color: #333;
}
.col-header i {
  color: #999;
}
.col-preview {
  display: flex;
  align-items: center;
  gap: 10px;
}
.col-preview img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.p-info {
  flex: 1;
  overflow: hidden;
}
.p-title {
  margin: 0 0 5px 0;
  font-size: 12px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.p-price {
  margin: 0;
  font-size: 14px;
  color: #ff5000;
  font-weight: bold;
}
.vertical-divider {
  width: 1px;
  height: 80%;
  background-color: #eee;
  margin: 0 10px;
}

/* ================== C. 专属推荐 ================== */
.recommend-section {
  margin-top: 30px;
}
.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
}
.rec-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}
.rec-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}
.img-wrap {
  width: 100%;
  aspect-ratio: 1/1;
  overflow: hidden;
}
.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.rec-info {
  padding: 10px;
}
.rec-title {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  height: 38px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.rec-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rec-price {
  color: #ff5000;
  font-size: 16px;
  font-weight: bold;
}
.rec-author {
  margin: 0 0 4px 0;
  font-size: 11px;
  color: #999;
}
.rec-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
  height: 20px;
  overflow: hidden;
}
.rec-tag {
  font-size: 10px;
  color: #ff5000;
  background-color: #fff5f0;
  border: 1px solid #ffcccc;
  padding: 0 4px;
  border-radius: 3px;
  line-height: 18px;
}
.difficulty-tag {
  display: inline-block;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 2px;
  color: #fff;
  line-height: 18px;
}
.level-0 { background-color: #909399; }
.level-1 { background-color: #67c23a; }
.level-2 { background-color: #e6a23c; }
.level-3 { background-color: #f56c6c; }
</style>

