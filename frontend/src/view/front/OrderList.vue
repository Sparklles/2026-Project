<template>
  <div class="order-list-page">
    <div class="order-tabs">
          <el-tabs v-model="activeStatus" @tab-click="handleTabClick">
            <el-tab-pane label="所有订单" name="all"></el-tab-pane>
            <el-tab-pane label="待付款" name="unpaid"></el-tab-pane>
            <el-tab-pane label="待发货" name="unshipped"></el-tab-pane>
            <el-tab-pane label="待收货" name="shipped"></el-tab-pane>
            <el-tab-pane label="交易完成" name="success"></el-tab-pane>
          </el-tabs>
          <div class="recycle-bin">
            <i class="el-icon-delete"></i> 订单回收站
          </div>
        </div>

        <div class="order-search-bar">
          <el-input
              v-model="searchKeyword"
              placeholder="输入商品标题或订单号进行搜索"
              class="search-input"
              size="small"
              @keyup.enter.native="handleSearch">
            <el-button slot="append" @click="handleSearch">订单搜索</el-button>
          </el-input>
          <div class="more-filter" @click="showFilterPanel = !showFilterPanel">
            <span class="filter-link">
              更多筛选条件 <i :class="showFilterPanel ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
            </span>
          </div>
        </div>

        <!-- 筛选面板 -->
        <div v-if="showFilterPanel" class="filter-panel">
          <!-- 下单时间 -->
          <div class="filter-row">
            <div class="filter-label">下单时间</div>
            <div class="filter-content">
              <el-radio-group v-model="filterForm.timeRange" size="small">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="1month">近1个月</el-radio-button>
                <el-radio-button label="3months">近3个月</el-radio-button>
                <el-radio-button label="6months">近6个月</el-radio-button>
              </el-radio-group>
              <el-date-picker
                  v-model="filterForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  size="small"
                  style="margin-left: 15px;"
                  value-format="yyyy-MM-dd">
              </el-date-picker>
            </div>
          </div>

          <!-- 交易状态 -->
          <div class="filter-row">
            <div class="filter-label">交易状态</div>
            <div class="filter-content">
              <el-radio-group v-model="filterForm.orderStatus" size="small">
                <el-radio-button :label="null">全部</el-radio-button>
                <el-radio-button :label="1">等待买家付款</el-radio-button>
                <el-radio-button :label="2">买家已付款</el-radio-button>
                <el-radio-button :label="3">卖家已发货</el-radio-button>
                <el-radio-button :label="4">交易成功</el-radio-button>
                <el-radio-button :label="5">交易关闭</el-radio-button>
                <el-radio-button :label="6">退款中的订单</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="filter-actions">
            <el-button type="primary" size="small" @click="handleFilterConfirm">确定</el-button>
            <el-button size="small" @click="handleFilterReset">重置</el-button>
          </div>
        </div>

        <div class="order-table-header">
          <div class="th-item">宝贝</div>
          <div class="th-price">单价</div>
          <div class="th-qty">数量</div>
          <div class="th-action">商品操作</div>
          <div class="th-amount">实付款</div>
          <div class="th-status">交易状态</div>
          <div class="th-operate">交易操作</div>
        </div>

        <div class="order-list-content" v-loading="loading">
          <el-empty v-if="displayOrders.length === 0" description="没有找到符合条件的订单哦~"></el-empty>

          <div class="order-card" v-for="order in displayOrders" :key="order.orderNo">

            <div class="order-header">
              <div class="header-left">
                <span class="order-date"><strong>{{ order.createTime }}</strong></span>
                <span class="order-no">订单号: {{ order.orderNo }}</span>
                <span class="shop-name"><i class="el-icon-s-shop"></i> {{ order.shopName }}</span>
                <i class="el-icon-chat-dot-round contact-icon" title="联系客服"></i>
              </div>
              <div class="header-right">
                <i v-if="['success', 'canceled', 'refunded'].includes(order.status)" class="el-icon-delete delete-icon" title="删除订单" @click="deleteOrder(order)"></i>
              </div>
            </div>

            <div class="order-body">
              <div class="item-list-col">
                <div class="item-row" v-for="(item, index) in order.items" :key="index">
                  <div class="td-item">
                    <el-image :src="item.image" class="item-img" @click="goToDetail(item.id)"></el-image>
                    <div class="item-info">
                      <p class="item-title" @click="goToDetail(item.id)">{{ item.title }}</p>
                      <p class="item-specs">{{ item.specs }}</p>
                      <div class="item-tags" v-if="item.tags">
                        <img v-if="item.tags.includes('正品')" src="https://img.alicdn.com/tfs/TB1L1XNXZrI8KJjSzaVXXbPbpXa-48-48.png" class="tiny-tag">
                        <img v-if="item.tags.includes('七天退换')" src="https://img.alicdn.com/tfs/TB1u_4fXCf2gK0jSZFPXXX1cUQa-48-48.png" class="tiny-tag">
                      </div>
                    </div>
                  </div>
                  <div class="td-price">
                    <p class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</p>
                    <p class="current-price">¥{{ item.price }}</p>
                  </div>
                  <div class="td-qty">{{ item.quantity }}</div>
                  <div class="td-action">
                  <a v-if="['shipped', 'success'].includes(order.status) && order.status !== 'refunding'" href="javascript:;" class="link-text" @click="applyAfterSales(order, item)">申请售后</a>
                  <span v-if="order.status === 'refunding'" class="refunding-badge">售后处理中</span>
                </div>
                </div>
              </div>

              <div class="summary-col">
                <div class="td-amount">
                  <p class="total-price"><strong>¥{{ order.actualAmount }}</strong></p>
                  <p class="shipping-fee">(含运费: ¥{{ order.shippingFee }})</p>
                  <span class="pay-method"><i class="el-icon-mobile-phone"></i> 手机订单</span>
                </div>
                <div class="td-status">
                  <p class="status-text success" v-if="order.status === 'success'">交易成功</p>
                  <p class="status-text warning" v-if="order.status === 'unpaid'">等待买家付款</p>
                  <p class="status-text primary" v-if="order.status === 'shipped'">卖家已发货</p>
                  <p class="status-text" v-if="order.status === 'unshipped'">买家已付款(待发货)</p>
                  <p class="status-text" v-if="order.status === 'canceled'" style="color:#999;">订单已取消</p>
                  <p class="status-text refunding" v-if="order.status === 'refunding'">
                    <i class="el-icon-warning-outline"></i> 正在申请售后
                  </p>
                  <p class="status-text refunded" v-if="order.status === 'refunded'">
                    <i class="el-icon-circle-check"></i> 退款成功
                  </p>

                  <a href="javascript:;" class="link-text" @click="viewDetails(order)">订单详情</a>
                  <a v-if="['shipped', 'success'].includes(order.status)" href="javascript:;" class="link-text" @click="viewLogistics(order)">查看物流</a>
                </div>
                <div class="td-operate">
                  <el-button v-if="order.status === 'unpaid'" type="primary" size="small" class="btn-main pay" @click="payOrder(order)">立即付款</el-button>
                  <el-button v-if="order.status === 'shipped'" type="primary" size="small" class="btn-main confirm" @click="confirmReceipt(order)">确认收货</el-button>
                  <el-button v-if="order.status === 'success' || order.status === 'unshipped'" size="small" class="btn-main normal" @click="buyAgain(order)">再次购买</el-button>
                  <el-button v-if="order.status === 'unpaid'" size="small" class="btn-main normal" style="margin-top: 5px;" @click="cancelOrder(order)">取消订单</el-button>

                  <a v-if="order.status === 'success'" href="javascript:;" class="link-text action-link" @click="addReview(order)">评价商品</a>
                </div>
              </div>
            </div>

          </div>
        </div>

        <div class="pagination-wrap">
          <el-pagination
              background
              layout="prev, pager, next"
              :total="totalOrders"
              :page-size="pageSize"
              :current-page.sync="currentPage">
          </el-pagination>
        </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { addToCart } from '@/api/front/cart';
import { getUserProfile } from '@/api/front/user';

export default {
  name: 'OrderList',
  data() {
    return {
      userId: null,
      activeStatus: 'all',
      searchKeyword: '',
      loading: false,
      currentPage: 1,
      pageSize: 10,
      allOrders: [],
      showFilterPanel: false,  // 是否显示筛选面板
      filterForm: {
        timeRange: '',         // 时间范围：'', '1month', '3months', '6months'
        dateRange: null,       // 自定义日期范围 [startDate, endDate]
        orderStatus: null      // 订单状态：null 或 1-6
      }
    }
  },
  computed: {
    filteredOrders() {
      let result = this.allOrders

      if (this.activeStatus !== 'all') {
        result = result.filter(order => order.status === this.activeStatus)
      }

      if (this.searchKeyword) {
        result = result.filter(order => {
          const matchNo = order.orderNo.includes(this.searchKeyword)
          const matchTitle = order.items.some(item => item.title.includes(this.searchKeyword))
          return matchNo || matchTitle
        })
      }
      return result
    },
    totalOrders() {
      return this.filteredOrders.length
    },
    displayOrders() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredOrders.slice(start, end)
    }
  },
  async created() {
    // 🌟 先同步等待解析 Token 获取 userId
    await this.fetchUserInfo();

    // 如果没有获取到 userId，直接停止执行后续请求
    if (!this.userId) return;

    // 拿到真实 userId 后，再去请求订单列表
    this.fetchOrderList()
  },
  methods: {
    async fetchUserInfo() {
      try {
        const profile = await getUserProfile();
        this.userId = profile && (profile.userId || profile.id);
        if (!this.userId) throw new Error('未获取到用户信息');
      } catch (error) {
        console.error('获取用户信息失败', error);
        this.$message.error('登录状态异常，请重新登录');
        this.$router.push('/login');
      }
    },
    async fetchOrderList() {
      this.loading = true
      try {
        // 这时 this.userId 已经有真实的值了！
        const res = await request.get('/api/order/list/all', {
          params: { userId: this.userId }
        })
        const records = res.data || res || []

        this.allOrders = records.map(order => {
          return {
            orderNo: order.orderNo,
            createTime: order.createTime ? order.createTime.replace('T', ' ') : '',
            shopName: '航海时代官方直营店',
            status: this.mapOrderStatus(order.orderStatus),
            actualAmount: order.payPrice ? order.payPrice.toFixed(2) : '0.00',
            shippingFee: '0.00',
            items: (order.items || []).map(item => ({
              id: item.bookId,
              orderItemId: item.orderItemId || item.id,
              title: item.bookTitle,
              specs: '标准版',
              image: item.coverUrl,
              price: item.price ? item.price.toFixed(2) : '0.00',
              quantity: item.quantity,
              tags: ['官方正品', '七天退换']
            }))
          }
        })
      } catch (error) {
        // request.js 拦截器会帮你打印错误，这里无需重复弹窗
        console.error('获取订单列表失败', error);
      } finally {
        this.loading = false
      }
    },

    // 带筛选条件的订单查询
    async fetchOrderListWithFilter() {
      this.loading = true
      try {
        const { beginTime, endTime } = this.calculateTimeRange()

        // 构建请求参数
        const params = {
          userId: this.userId
        }

        // 添加时间范围参数
        if (beginTime) params.beginTime = beginTime
        if (endTime) params.endTime = endTime

        // 添加订单状态参数
        if (this.filterForm.orderStatus !== null) {
          params.orderStatus = this.filterForm.orderStatus
        }

        // 调用条件查询接口（不分页）
        const res = await request.post('/api/order/query/all', params)
        const records = res.data || res || []

        this.allOrders = records.map(order => {
          return {
            orderNo: order.orderNo,
            createTime: order.createTime ? order.createTime.replace('T', ' ') : '',
            shopName: '航海时代官方直营店',
            status: this.mapOrderStatus(order.orderStatus),
            actualAmount: order.payPrice ? order.payPrice.toFixed(2) : '0.00',
            shippingFee: '0.00',
            items: (order.items || []).map(item => ({
              id: item.bookId,
              orderItemId: item.orderItemId || item.id,
              title: item.bookTitle,
              specs: '标准版',
              image: item.coverUrl,
              price: item.price ? item.price.toFixed(2) : '0.00',
              quantity: item.quantity,
              tags: ['官方正品', '七天退换']
            }))
          }
        })

        this.$message.success(`筛选完成，共找到 ${this.allOrders.length} 个订单`)
      } catch (error) {
        console.error('筛选订单失败', error);
        this.$message.error('筛选订单失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    mapOrderStatus(backendStatus) {
      switch (backendStatus) {
        case 1: return 'unpaid'
        case 2: return 'unshipped'
        case 3:
        case 7: return 'shipped'
        case 4: return 'success'
        case 5: return 'canceled'
        case 6: return 'refunding'  // 退款中
        case 8: return 'refunded'   // 已退款
        default: return 'all'
      }
    },
    handleTabClick() {
      this.currentPage = 1
    },
    handleSearch() {
      this.currentPage = 1
    },

    // 计算时间范围
    calculateTimeRange() {
      const now = new Date()
      let beginTime = null
      let endTime = null

      // 如果有自定义日期范围，优先使用
      if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
        beginTime = this.filterForm.dateRange[0] + 'T00:00:00'
        endTime = this.filterForm.dateRange[1] + 'T23:59:59'
      } else {
        // 使用预设时间范围
        switch (this.filterForm.timeRange) {
          case '1month':
            beginTime = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] + 'T00:00:00'
            endTime = now.toISOString().split('T')[0] + 'T23:59:59'
            break
          case '3months':
            beginTime = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] + 'T00:00:00'
            endTime = now.toISOString().split('T')[0] + 'T23:59:59'
            break
          case '6months':
            beginTime = new Date(now.getTime() - 180 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] + 'T00:00:00'
            endTime = now.toISOString().split('T')[0] + 'T23:59:59'
            break
          default:
            // 全部时间，不设置时间范围
            beginTime = null
            endTime = null
        }
      }

      return { beginTime, endTime }
    },

    // 筛选确认
    async handleFilterConfirm() {
      this.currentPage = 1
      await this.fetchOrderListWithFilter()
    },

    // 筛选重置
    handleFilterReset() {
      this.filterForm = {
        timeRange: '',
        dateRange: null,
        orderStatus: null
      }
      this.currentPage = 1
      this.fetchOrderList()
    },
    goToDetail(itemId) {
      this.$router.push({
        name: 'ProductDetail',
        params: { id: itemId }
      })
    },
    viewDetails(order) {
      this.$router.push({
        path: '/user/order-detail',
        query: { orderNo: order.orderNo }
      })
    },
    viewLogistics(order) {
      if (!order || !order.orderNo) {
        this.$message.warning('订单信息异常');
        return;
      }
      this.$message.info(`正在为您查询订单 ${order.orderNo} 的物流信息...`);
    },
    deleteOrder(order) {
      this.$confirm('确定要删除该订单吗？删除后将放入回收站。', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await request.delete(`/api/order/delete/${order.orderNo}`, { params: { userId: this.userId } });
            this.$message.success('删除成功');
            this.fetchOrderList();
          } catch (error) {
            console.error('删除订单失败', error);
            this.$message.error(error.message || '删除失败');
          }
        }).catch(() => {});
    },
    payOrder(order) {
      if (!order || !order.orderNo) {
        this.$message.warning('订单信息异常');
        return;
      }
      this.$router.push({
        path: '/pay',
        query: {
          orderNo: order.orderNo,
          amount: order.actualAmount
        }
      });
    },
    confirmReceipt(order) {
      this.$confirm('请确认您已收到货物，确认后钱款将打给卖家。', '确认收货', { type: 'warning' })
        .then(async () => {
          try {
            await request.post('/api/order/confirm-receive', {
              userId: this.userId,
              orderNo: order.orderNo
            });
            this.$message.success('确认收货成功！');
            this.fetchOrderList();
          } catch (error) {
            console.error('确认收货失败', error);
            this.$message.error(error.message || '确认收货失败');
          }
        }).catch(() => {});
    },
    cancelOrder(order) {
      this.$confirm('确定要取消这笔订单吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await request.post('/api/order/cancel', {
              userId: this.userId,
              orderNo: order.orderNo
            });
            this.$message.success('订单已取消');
            this.fetchOrderList();
          } catch (error) {
            console.error('取消订单失败', error);
            this.$message.error(error.message || '取消订单失败');
          }
        }).catch(() => {});
    },
    async buyAgain(order) {
      if (!order || !order.items || order.items.length === 0) {
        this.$message.warning('订单中没有商品');
        return;
      }

      try {
        // 遍历订单中的所有商品，逐个添加到购物车
        for (const item of order.items) {
          await addToCart({
            userId: this.userId,
            bookId: item.id,
            quantity: item.quantity
          });
        }
        this.$message.success('已将该订单商品加入购物车');
        this.$router.push('/cart');
      } catch (error) {
        console.error('再次购买失败', error);
        this.$message.error(error.message || '添加购物车失败');
      }
    },
    addReview(order) {
      const item = order.items[0]
      if (!item || !item.id) {
        this.$message.warning('未找到可评价的商品')
        return
      }
      this.$router.push({
        name: 'ProductDetail',
        params: { id: item.id },
        query: {
          action: 'review',
          orderNo: order.orderNo,
          orderItemId: item.orderItemId || item.id,
          bookId: item.id,
          bookTitle: item.title
        }
      })
    },
    applyAfterSales(order, item) {
      const url = `/user/refund-apply?orderItemId=${item.orderItemId || item.id}&productTitle=${encodeURIComponent(item.title)}&productImage=${encodeURIComponent(item.image)}&productSpec=${encodeURIComponent(item.specs)}&productPrice=${item.price}&productQty=${item.quantity}`
      const routeUrl = this.$router.resolve(url)
      window.open(routeUrl.href, '_blank')
    }
  }
}
</script>

<style scoped>
.order-list-page { padding: 0; }

/* ================== 1. 状态 Tabs ================== */
.order-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.order-tabs ::v-deep .el-tabs__nav-wrap::after { display: none; }
.order-tabs ::v-deep .el-tabs__item {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}
.order-tabs ::v-deep .el-tabs__item.is-active { color: #ff5000; }
.order-tabs ::v-deep .el-tabs__active-bar { background-color: #ff5000; }
.recycle-bin {
  font-size: 13px;
  color: #666;
  cursor: pointer;
}
.recycle-bin:hover { color: #ff5000; }

/* ================== 2. 搜索过滤栏 ================== */
.order-search-bar {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.search-input { width: 350px; margin-right: 20px; }
.more-filter { font-size: 12px; color: #666; cursor: pointer; }
.more-filter:hover { color: #ff5000; }
.filter-link { display: flex; align-items: center; gap: 5px; }

/* 筛选面板样式 */
.filter-panel {
  background: #f8f8f8;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 15px;
}
.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
}
.filter-row:last-of-type {
  margin-bottom: 0;
}
.filter-label {
  width: 80px;
  font-size: 13px;
  color: #666;
  font-weight: bold;
  padding-top: 8px;
}
.filter-content {
  flex: 1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
}

/* ================== 3. 表头结构 ================== */
.order-table-header {
  display: flex;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  padding: 10px 0;
  font-size: 12px;
  color: #666;
  text-align: center;
  margin-bottom: 15px;
}
.th-item { width: 380px; }
.th-price { width: 80px; }
.th-qty { width: 60px; }
.th-action { width: 100px; }
.th-amount { width: 130px; border-left: 1px solid #e8e8e8;}
.th-status { width: 130px; border-left: 1px solid #e8e8e8;}
.th-operate { width: 150px; border-left: 1px solid #e8e8e8;}

/* ================== 4. 订单卡片 ================== */
.order-card {
  border: 1px solid #ececec;
  background: #fff;
  margin-bottom: 15px;
  transition: border-color 0.2s;
}
.order-card:hover {
  border-color: #d1d1d1;
}

/* 订单卡片 Header */
.order-header {
  background: #f1f1f1;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #333;
}
.order-date { margin-right: 15px; }
.order-no { margin-right: 30px; color: #666; }
.shop-name { font-weight: bold; cursor: pointer; }
.shop-name:hover { color: #ff5000; }
.contact-icon { color: #1890ff; font-size: 16px; margin-left: 10px; cursor: pointer; vertical-align: middle;}
.delete-icon { color: #999; font-size: 16px; cursor: pointer; }
.delete-icon:hover { color: #ff5000; }

/* 订单卡片 Body (表格布局) */
.order-body {
  display: flex;
  width: 100%;
  overflow: hidden;
}

/* 左侧商品列 */
.item-list-col {
  flex: 1 1 auto;
  min-width: 0;
}
.item-row {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #ececec;
}
.item-row:last-child { border-bottom: none; }

.td-item { flex: 1 1 auto; min-width: 0; display: flex; padding-left: 15px; }
.item-img { width: 80px; height: 80px; border: 1px solid #eee; cursor: pointer; margin-right: 10px; }
.item-info { flex: 1; min-width: 0; padding-right: 15px; }
.item-title { margin: 0 0 5px 0; font-size: 12px; color: #333; cursor: pointer; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;}
.item-title:hover { color: #ff5000; text-decoration: underline; }
.item-specs { margin: 0 0 5px 0; font-size: 12px; color: #999; }
.tiny-tag { height: 14px; margin-right: 4px; }

.td-price { flex: 0 0 80px; text-align: center; }
.original-price { margin: 0 0 2px 0; font-size: 12px; color: #999; text-decoration: line-through; }
.current-price { margin: 0; font-size: 12px; color: #333; }

.td-qty { flex: 0 0 60px; text-align: center; font-size: 12px; color: #333; }
.td-action { flex: 0 0 90px; text-align: center; }
.refunding-badge {
  font-size: 12px;
  color: #ff5000;
  background: #fff0eb;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: bold;
}

/* 右侧汇总列 */
.summary-col {
  flex: 0 0 390px;
  display: flex;
  border-left: 1px solid #ececec;
  min-width: 0;
}
.td-amount, .td-status, .td-operate {
  padding: 15px 0;
  text-align: center;
  border-left: 1px solid #ececec;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}
.td-amount { flex: 0 0 130px; border-left: none; }
.total-price { margin: 0 0 4px 0; font-size: 14px; color: #333; }
.shipping-fee { margin: 0 0 4px 0; font-size: 12px; color: #999; }
.pay-method { font-size: 12px; color: #1890ff; background: #e6f7ff; padding: 2px 6px; border-radius: 2px;}

.td-status { flex: 0 0 130px; }
.status-text { margin: 0 0 5px 0; font-size: 12px; color: #333; }
.status-text.success { color: #666; }
.status-text.warning { color: #ff5000; font-weight: bold; }
.status-text.primary { color: #1890ff; }
.status-text.refunding {
  color: #ff5000;
  font-weight: bold;
  font-size: 13px;
  background: #fff0eb;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.status-text.refunded {
  color: #52c41a;
  font-weight: bold;
  font-size: 13px;
  background: #f6ffed;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.td-operate {
  flex: 0 0 130px;
  overflow: hidden;
}
.td-operate ::v-deep .el-button {
  margin-left: 0;
}
.btn-main {
  width: 90px;
  max-width: calc(100% - 20px);
  border-radius: 4px;
  margin-bottom: 5px;
  box-sizing: border-box;
}
.btn-main.pay { background-color: #ff5000; border-color: #ff5000; color: #fff; font-weight: bold;}
.btn-main.pay:hover { background-color: #e64800; }
.btn-main.confirm { background-color: #1890ff; border-color: #1890ff; color: #fff;}
.btn-main.normal { border-color: #dcdfe6; color: #333; }
.btn-main.normal:hover { border-color: #ff5000; color: #ff5000; }

/* 通用链接文字 */
.link-text { font-size: 12px; color: #333; text-decoration: none; margin-bottom: 5px; }
.link-text:hover { color: #ff5000; }
.action-link { margin-top: 5px; }

/* 分页 */
.pagination-wrap {
  text-align: right;
  margin-top: 20px;
}
</style>
