<template>
  <div class="order-detail-page">
    <header class="uc-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代 · 订单详情</span>
        </div>
      </div>
    </header>

    <div v-loading="loading" class="detail-container">
      <template v-if="!loading || orderInfo.orderNo">
        <div v-if="!orderInfo.orderNo" style="text-align:center;padding:100px 0;width:100%;">
          <el-empty description="未找到订单信息"></el-empty>
        </div>
        <template v-else>
          <div class="left-panel">

        <!-- 订单已取消时显示简化视图 -->
        <div v-if="orderInfo.status === 5" class="closed-order-section">
          <div class="closed-status">
            <h2 class="closed-title">交易关闭</h2>
            <p class="closed-reason">订单已取消</p>
          </div>
          <div class="action-buttons">
            <el-button type="primary" class="btn-main" @click="buyAgain">再买一单</el-button>
            <el-button size="small" @click="addToCart">加入购物车</el-button>
            <el-button size="small" @click="deleteOrder">删除订单</el-button>
            <el-button size="small" @click="printOrder">打印</el-button>
          </div>
        </div>

        <!-- 正常订单显示进度条 -->
        <div v-else class="progress-section">
          <el-steps :active="stepIndex" align-center finish-status="success">
            <el-step title="拍下宝贝" :description="orderInfo.createTime"></el-step>
            <el-step title="买家付款" :description="orderInfo.payTime"></el-step>
            <el-step title="卖家发货" :description="orderInfo.shipTime"></el-step>
            <el-step title="确认收货" :description="orderInfo.finishTime"></el-step>
          </el-steps>
        </div>

        <div class="status-section" v-if="orderInfo.status !== 5">
          <h2 class="status-title">{{ orderInfo.statusText }}</h2>

          <div class="info-row logistics-row">
            <i :class="statusNotice.icon" class="info-icon" style="color: #ff5000;"></i>
            <div class="info-content">
              <span class="highlight-text">{{ statusNotice.label }}</span>
              {{ statusNotice.text }}
              <a v-if="canViewLogistics" href="javascript:;" class="text-link" @click="viewLogistics">查看物流详情 <i class="el-icon-arrow-right"></i></a>
            </div>
          </div>

          <div class="info-row address-row">
            <i class="el-icon-location-outline info-icon"></i>
            <div class="info-content">
              {{ orderInfo.address.detail }}<br>
              {{ orderInfo.address.receiver }} {{ orderInfo.address.phone }}
            </div>
          </div>

          <div class="action-buttons">
            <!-- 待付款订单显示付款按钮 -->
            <el-button v-if="orderInfo.status === 1" type="primary" class="btn-main" @click="payOrder">付款</el-button>
            <el-button v-else type="primary" class="btn-main" @click="buyAgain">再买一单</el-button>
            
            <el-button size="small" @click="addToCart">加入购物车</el-button>
            <el-button v-if="canViewLogistics" size="small" @click="viewLogistics">查看物流</el-button>
            <el-button v-if="canConfirmReceipt" size="small" @click="confirmReceipt">确认收货</el-button>
            <el-button v-if="canReview" size="small" @click="addReview">追加评价</el-button>
            <el-button size="small" @click="deleteOrder">删除订单</el-button>
            <el-button size="small" @click="printOrder">打印</el-button>
          </div>
        </div>

        <div class="package-section" v-for="(pkg, pIndex) in orderInfo.packages" :key="pIndex">

          <div class="package-header">
            <span class="pkg-title">包裹{{ pIndex + 1 }} (共{{ pkg.items.length }}件)</span>
          </div>

          <div class="express-info">
            <img src="https://img.alicdn.com/tfs/TB1w5Z5d7P2gK0jSZPxXXacQpXa-130-42.png" alt="快递" class="express-logo" v-if="pkg.expressName.includes('圆通')">
            <i class="el-icon-truck" v-else style="font-size: 20px; color: #1890ff; margin-right: 10px;"></i>
            <span class="express-name">{{ pkg.expressName }}</span>
            <span class="express-no">{{ pkg.expressNo }}</span>
            <a href="javascript:;" class="text-link expand-link">展开全部商品 <i class="el-icon-arrow-down"></i></a>
          </div>

          <div class="shop-info">
            <i class="el-icon-s-shop" style="color: #1890ff;"></i> {{ pkg.shopName }}
            <i class="el-icon-chat-dot-round contact-icon"></i>
            <a href="javascript:;" class="text-link float-right">查看交易快照 <i class="el-icon-arrow-right"></i></a>
          </div>

          <div class="product-list">
            <div class="product-item" v-for="item in pkg.items" :key="item.id">
              <div class="p-img">
                <el-image :src="item.image" fit="cover"></el-image>
              </div>
              <div class="p-info">
                <div class="p-title">{{ item.title }}</div>
                <div class="p-spec">{{ item.spec }}</div>
                <div class="p-tags">
                  <span class="service-tag">退货宝</span>
                  <span class="service-tag">7天价保</span>
                  <span class="service-tag">极速退款</span>
                  <span class="service-tag">7天无理由退货</span>
                </div>
                <div class="p-actions">
                  <el-button size="mini" plain @click="addToCart">加入购物车</el-button>
                  <el-button size="mini" plain @click="applyAfterSales(item)">申请售后</el-button>
                </div>
              </div>
              <div class="p-price-wrap">
                <div class="current-price">¥{{ item.price.toFixed(2) }}</div>
                <div class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice.toFixed(2) }}</div>
                <div class="p-qty">x{{ item.qty }}</div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <div class="right-panel">

        <div class="meta-card">
          <h3 class="card-title">付款详情</h3>
          <div class="meta-row">
            <span class="label">商品总价</span>
            <span class="value">¥{{ orderInfo.pricing.total.toFixed(2) }}</span>
          </div>
          <div class="meta-row">
            <span class="label">运费</span>
            <span class="value">¥{{ orderInfo.pricing.shipping.toFixed(2) }}</span>
          </div>
          <div class="meta-row">
            <span class="label">店铺优惠</span>
            <span class="value highlight-red">- ¥{{ orderInfo.pricing.discount.toFixed(2) }}</span>
          </div>
          <el-divider class="custom-divider"></el-divider>
          <div class="meta-row actual-pay">
            <span class="label">实付款</span>
            <span class="value highlight-main">¥{{ orderInfo.pricing.actual.toFixed(2) }}</span>
          </div>
        </div>

        <div class="meta-card">
          <h3 class="card-title">订单信息</h3>
          <div class="meta-row">
            <span class="label">订单编号</span>
            <span class="value">
              {{ orderInfo.orderNo }}
              <a href="javascript:;" class="copy-btn" @click="copyText(orderInfo.orderNo)">复制</a>
            </span>
          </div>
          <div class="meta-row">
            <span class="label">支付方式</span>
            <span class="value">{{ orderInfo.payMethod }}</span>
          </div>
          <div class="meta-row multi-line">
            <span class="label">收货信息</span>
            <span class="value masked-address">
              {{ orderInfo.address.receiver }}, {{ maskPhone(orderInfo.address.phone) }}, {{ orderInfo.address.detail }}
            </span>
          </div>
          <div class="meta-row">
            <span class="label">交易快照</span>
            <span class="value"><a href="javascript:;" class="text-link">发生交易争议时，可作为判断依据 <i class="el-icon-arrow-right"></i></a></span>
          </div>
          <div class="meta-row">
            <span class="label">微信交易号</span>
            <span class="value">{{ orderInfo.transactionId }}</span>
          </div>
          <div class="meta-row">
            <span class="label">创建时间</span>
            <span class="value">{{ orderInfo.createTime }}</span>
          </div>
          <div class="meta-row">
            <span class="label">付款时间</span>
            <span class="value">{{ orderInfo.payTime }}</span>
          </div>
          <div class="meta-row">
            <span class="label">发货时间</span>
            <span class="value">{{ orderInfo.shipTime }}</span>
          </div>
          <div class="meta-row">
            <span class="label">成交时间</span>
            <span class="value">{{ orderInfo.finishTime }}</span>
          </div>

          <div class="more-info-toggle">
            <a href="javascript:;" class="text-link">收起更多订单信息 <i class="el-icon-arrow-up"></i></a>
          </div>
        </div>

        <div class="meta-card">
          <h3 class="card-title">订单服务</h3>
          <div class="meta-row">
            <span class="label">包含服务</span>
            <span class="value">退货宝等服务 <i class="el-icon-arrow-right"></i></span>
          </div>
        </div>

      </div>
        </template>
      </template>

    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import { addToCart } from '@/api/front/cart';
import { getUserProfile } from '@/api/front/user';

export default {
  name: 'OrderDetail',
  data() {
    return {
      loading: false,
      currentUserId: null,
      orderInfo: {
        status: 'success',
        statusText: '交易成功',
        logisticsText: '您的快件已投递，收件人：门卫。如遇找不到包裹等问题无需找商家/平台，请联系快递员: 18371234881，或致电专属客服。感谢使用圆通速递，期待再次为您服务！',
        address: {
          receiver: '',
          phone: '',
          province: '',
          city: '',
          district: '',
          detail: ''
        },
        orderNo: '',
        payMethod: '微信支付',
        transactionId: '',
        createTime: '',
        payTime: '',
        shipTime: '',
        finishTime: '',
        pricing: {
          total: 0,
          shipping: 0,
          discount: 0,
          actual: 0
        },
        packages: []
      }
    }
  },
  computed: {
    statusNumber() {
      return Number(this.orderInfo.status);
    },
    stepIndex() {
      // Element UI Steps 的 active 表示当前步骤位置；要让最后一步也变为完成态，
      // 4 个步骤全部完成时需要传 4，而不是最后一个下标 3。
      const map = {
        1: 1, // 已拍下，等待付款
        2: 2, // 已付款，等待发货
        3: 3, // 已发货，等待确认收货
        4: 4, // 交易成功，四步全部完成
        5: 0, // 已取消
        6: 4, // 售后处理中，订单主流程已完成
        7: 4, // 已签收
        8: 0  // 已退款
      };
      return map[this.statusNumber] ?? 0;
    },
    statusNotice() {
      const notices = {
        1: {
          icon: 'el-icon-wallet',
          label: '待付款',
          text: '订单已提交，请尽快完成支付，支付成功后商家会为您安排发货。'
        },
        2: {
          icon: 'el-icon-box',
          label: '待发货',
          text: '您已完成付款，商家正在备货，发货后将同步物流信息。'
        },
        3: {
          icon: 'el-icon-truck',
          label: '运输中',
          text: this.orderInfo.logisticsText || '商家已发货，包裹正在运输途中，请留意物流更新。'
        },
        4: {
          icon: 'el-icon-box',
          label: '已签收',
          text: this.orderInfo.logisticsText || '订单已完成，感谢您的购买。'
        },
        6: {
          icon: 'el-icon-service',
          label: '售后处理中',
          text: '该订单正在售后处理中，您可以在退款/售后页面查看处理进度。'
        },
        7: {
          icon: 'el-icon-box',
          label: '已签收',
          text: this.orderInfo.logisticsText || '包裹已签收，订单主流程已完成。'
        },
        8: {
          icon: 'el-icon-refresh-left',
          label: '已退款',
          text: '该订单已完成退款处理，如有疑问请联系客服。'
        }
      };
      return notices[this.statusNumber] || {
        icon: 'el-icon-info',
        label: '订单状态',
        text: '订单状态已更新，请以订单详情信息为准。'
      };
    },
    canViewLogistics() {
      return [3, 4, 7].includes(this.statusNumber);
    },
    canConfirmReceipt() {
      return this.statusNumber === 3;
    },
    canReview() {
      return [4, 7].includes(this.statusNumber);
    }
  },
  async created() {
    await this.fetchUserInfo();
    if (!this.currentUserId) return;
    const orderNo = this.$route.query.orderNo;
    if (orderNo) {
      this.fetchOrderDetail(orderNo);
    } else {
      this.$message.error('缺少订单号');
      this.$router.push('/user/orders');
    }
  },
  methods: {
    async fetchUserInfo() {
      try {
        const profile = await getUserProfile();
        this.currentUserId = profile && (profile.userId || profile.id);
        if (!this.currentUserId) throw new Error('未获取到用户信息');
      } catch (error) {
        console.error('获取用户信息失败', error);
        this.$message.error('登录状态异常，请重新登录');
        this.$router.push('/login');
      }
    },
    async fetchOrderDetail(orderNo) {
      this.loading = true;
      try {
        const res = await request.get('/api/order/detail/full', {
          params: { orderNo }
        });
        const data = res.data || res;
        this.mapOrderData(data);
      } catch (error) {
        console.error('获取订单详情失败', error);
        this.$message.error('加载订单详情失败');
      } finally {
        this.loading = false;
      }
    },
    mapOrderData(data) {
      this.orderInfo.orderNo = data.orderNo || '';
      this.orderInfo.createTime = data.createTime ? data.createTime.replace('T', ' ') : '';
      this.orderInfo.payTime = data.payTime ? data.payTime.replace('T', ' ') : '';
      this.orderInfo.shipTime = data.shipTime ? data.shipTime.replace('T', ' ') : '';
      this.orderInfo.finishTime = data.closeTime ? data.closeTime.replace('T', ' ') : '';
      this.orderInfo.transactionId = data.transactionId || '';
      this.orderInfo.status = data.orderStatus;
      this.orderInfo.statusText = this.mapStatusText(data.orderStatus);
      this.orderInfo.userId = data.userId || this.currentUserId;

      // OrderDetailVo: consignee, phone, address 是平铺字段
      this.orderInfo.address = {
        receiver: data.consignee || '',
        phone: data.phone || '',
        province: '',
        city: '',
        district: '',
        detail: data.address || ''
      };

      // OrderDetailVo: totalPrice, freightPrice, discountPrice, payPrice
      this.orderInfo.pricing = {
        total: data.totalPrice ?? 0,
        shipping: data.freightPrice ?? 0,
        discount: data.discountPrice ?? 0,
        actual: data.payPrice ?? 0
      };

      this.orderInfo.payMethod = this.mapPayType(data.payType);

      if (data.items && data.items.length > 0) {
        this.orderInfo.packages = [{
          expressName: data.expressName || '圆通速递',
          expressNo: data.expressNo || '',
          shopName: data.shopName || '航海时代官方直营店',
          items: data.items.map(item => ({
            id: item.id || item.bookId,
            title: item.bookTitle,
            spec: item.bookAuthor ? `作者：${item.bookAuthor}` : '标准版',
            image: item.coverUrl,
            price: item.price,
            originalPrice: item.originalPrice,
            qty: item.quantity,
            orderItemId: item.orderItemId || item.id
          }))
        }];
      }
    },
    mapStatusText(orderStatus) {
      // 注意：后端接口返回的状态码定义
      // 1-待支付, 2-待发货, 3/7-已发货/已签收, 4-交易成功, 5-已取消/交易关闭, 6-售后处理中, 8-已退款
      const map = { 
        1: '等待买家付款', 
        2: '买家已付款', 
        3: '卖家已发货', 
        4: '交易成功', 
        5: '交易关闭', 
        6: '售后处理中', 
        7: '已签收', 
        8: '已退款' 
      };
      return map[orderStatus] || '未知';
    },
    mapPayType(payType) {
      const map = { 1: '微信支付', 2: '支付宝', 3: '银行卡' };
      return map[payType] || '其他';
    },
    maskPhone(phone) {
      if (!phone) return '';
      return phone.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2');
    },
    copyText(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('订单编号已复制');
      }).catch(() => {
        this.$message.info('复制失败，请手动复制');
      });
    },
    viewLogistics() {
      if (!this.canViewLogistics) {
        this.$message.warning('当前订单暂无物流信息');
        return;
      }
      if (!this.orderInfo.packages || this.orderInfo.packages.length === 0) {
        this.$message.warning('暂无物流信息');
        return;
      }
      const pkg = this.orderInfo.packages[0];
      this.$message.info(`正在查询 ${pkg.expressName} ${pkg.expressNo} 的物流轨迹...`);
    },
    async buyAgain() {
      const items = this.orderInfo.packages?.[0]?.items || [];
      if (items.length === 0) {
        this.$message.warning('订单中没有商品');
        return;
      }
      try {
        // 遍历订单中的所有商品，逐个添加到购物车
        for (const item of items) {
          await addToCart({
            userId: this.orderInfo.userId,
            bookId: item.id,
            quantity: item.qty
          });
        }
        this.$message.success('已将订单商品加入购物车');
        this.$router.push('/cart');
      } catch (error) {
        console.error('再次购买失败', error);
        this.$message.error(error.message || '添加购物车失败');
      }
    },
    async addToCart() {
      const items = this.orderInfo.packages?.[0]?.items || [];
      if (items.length === 0) {
        this.$message.warning('订单中没有商品');
        return;
      }
      try {
        // 遍历订单中的所有商品，逐个添加到购物车
        for (const item of items) {
          await addToCart({
            userId: this.orderInfo.userId,
            bookId: item.id,
            quantity: item.qty
          });
        }
        this.$message.success('已成功加入购物车');
      } catch (error) {
        console.error('加入购物车失败', error);
        this.$message.error(error.message || '添加购物车失败');
      }
    },
    addReview() {
      if (!this.canReview) {
        this.$message.warning('订单完成后才能评价商品');
        return;
      }
      const item = this.orderInfo.packages?.[0]?.items?.[0];
      if (!item) {
        this.$message.warning('没有可评价的商品');
        return;
      }
      this.$router.push({
        path: '/user/reviews',
        query: {
          orderNo: this.orderInfo.orderNo,
          itemId: item.id,
          bookId: item.id,
          bookTitle: item.title
        }
      });
    },
    confirmReceipt() {
      if (!this.canConfirmReceipt) {
        this.$message.warning('当前订单不能确认收货');
        return;
      }
      this.$confirm('请确认您已收到货物，确认后订单将完成。', '确认收货', { type: 'warning' })
        .then(async () => {
          try {
            await request.post('/api/order/confirm-receive', {
              userId: this.orderInfo.userId,
              orderNo: this.orderInfo.orderNo
            });
            this.$message.success('确认收货成功');
            this.fetchOrderDetail(this.orderInfo.orderNo);
          } catch (error) {
            console.error('确认收货失败', error);
            this.$message.error(error.message || '确认收货失败');
          }
        }).catch(() => {});
    },
    async deleteOrder() {
      this.$confirm('删除后订单将放入回收站，是否继续？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await request.delete(`/api/order/delete/${this.orderInfo.orderNo}`, {
              params: { userId: this.orderInfo.userId }
            });
            this.$message.success('订单已删除');
            this.$router.push('/user/orders');
          } catch (error) {
            this.$message.error('删除失败');
          }
        }).catch(() => {});
    },
    applyAfterSales(item) {
      if (!item || !item.orderItemId) {
        this.$message.warning('无法获取商品信息');
        return;
      }
      const url = `/user/refund-apply?orderItemId=${item.orderItemId}&productTitle=${encodeURIComponent(item.title)}&productImage=${encodeURIComponent(item.image)}&productSpec=${encodeURIComponent(item.spec)}&productPrice=${item.price}&productQty=${item.qty}`;
      const routeUrl = this.$router.resolve(url);
      window.open(routeUrl.href, '_blank');
    },
    printOrder() {
      window.print();
    },
    payOrder() {
      if (!this.orderInfo || !this.orderInfo.orderNo) {
        this.$message.warning('订单信息异常');
        return;
      }
      this.$router.push({
        path: '/pay',
        query: {
          orderNo: this.orderInfo.orderNo,
          amount: this.orderInfo.pricing.actual.toFixed(2)
        }
      });
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  background-color: #f4f7f9;
  min-height: 100vh;
  padding-bottom: 50px;
}

/* ================== 1. 顶部导航 ================== */
.uc-header {
  background-color: #fff;
  border-bottom: 2px solid #ff5000;
  padding: 15px 0;
  margin-bottom: 20px;
}
.header-content {
  width: 1200px;
  margin: 0 auto;
}
.logo-area {
  display: inline-flex;
  align-items: center;
  color: #ff5000;
  cursor: pointer;
}
.logo-icon { font-size: 32px; margin-right: 10px; }
.logo-text { font-size: 22px; font-weight: bold; }

/* ================== 核心布局 ================== */
.detail-container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

/* ================== 左侧主链路区 ================== */
.left-panel {
  width: 850px;
}

/* 进度条 */
.progress-section {
  background: #fff;
  padding: 30px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.progress-section ::v-deep .el-step__head.is-success {
  color: #ff5000;
  border-color: #ff5000;
}
.progress-section ::v-deep .el-step__title.is-success,
.progress-section ::v-deep .el-step__description.is-success {
  color: #ff5000;
}

/* 状态与地址 */
.status-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.status-title {
  font-size: 24px;
  color: #333;
  margin: 0 0 25px 0;
}
.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}
.info-icon {
  font-size: 20px;
  margin-right: 15px;
  color: #666;
  margin-top: 2px;
}
.info-content {
  flex: 1;
  font-size: 13px;
  color: #333;
  line-height: 1.6;
}
.highlight-text {
  color: #ff5000;
  font-weight: bold;
  margin-right: 5px;
}
.text-link {
  color: #1890ff;
  text-decoration: none;
  margin-left: 5px;
}
.text-link:hover {
  text-decoration: underline;
}

/* 已取消订单样式 */
.closed-order-section {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.closed-status {
  margin-bottom: 25px;
}
.closed-title {
  font-size: 24px;
  color: #999;
  margin: 0 0 10px 0;
}
.closed-reason {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.action-buttons {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #e8e8e8;
  display: flex;
  gap: 15px;
}
.btn-main {
  background-color: #ff5000;
  border-color: #ff5000;
  color: #fff;
}
.btn-main:hover {
  background-color: #e64800;
}

/* 包裹与商品 */
.package-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  padding-bottom: 20px;
  margin-bottom: 20px;
}
.package-header {
  padding: 15px 20px;
  border-bottom: 2px solid #ff5000;
}
.pkg-title {
  font-size: 14px;
  color: #ff5000;
  font-weight: bold;
}
.express-info {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  font-size: 13px;
  color: #333;
}
.express-logo {
  height: 20px;
  margin-right: 10px;
}
.express-name {
  font-weight: bold;
  margin-right: 10px;
}
.express-no {
  color: #666;
}
.expand-link {
  margin-left: auto;
  color: #ff5000;
}

.shop-info {
  padding: 15px 20px;
  font-size: 14px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}
.contact-icon {
  color: #1890ff;
  margin-left: 5px;
  cursor: pointer;
}
.float-right {
  float: right;
  font-weight: normal;
  font-size: 12px;
  color: #999;
}

/* 商品卡片 */
.product-list {
  padding: 0 20px;
}
.product-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;
}
.product-item:last-child {
  border-bottom: none;
}
.p-img {
  width: 80px;
  height: 80px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-right: 15px;
}
.p-img .el-image { width: 100%; height: 100%; }

.p-info {
  flex: 1;
}
.p-title {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 5px;
}
.p-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}
.p-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}
.service-tag {
  font-size: 11px;
  color: #666;
}
.p-actions .el-button {
  margin-right: 10px;
}

.p-price-wrap {
  width: 100px;
  text-align: right;
}
.current-price {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}
.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-bottom: 5px;
}
.p-qty {
  font-size: 12px;
  color: #999;
}


/* ================== 右侧元数据区 ================== */
.right-panel {
  width: 330px;
}
.meta-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.card-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
}
.meta-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}
.meta-row.multi-line {
  flex-direction: column;
}
.meta-row.multi-line .value {
  margin-top: 5px;
  text-align: left;
}
.label {
  color: #999;
  width: 70px;
  flex-shrink: 0;
}
.value {
  text-align: right;
  word-break: break-all;
}
.highlight-red { color: #ff5000; }
.highlight-main { font-size: 18px; font-weight: bold; color: #333; }
.custom-divider { margin: 15px 0; }

.copy-btn {
  color: #1890ff;
  text-decoration: none;
  margin-left: 5px;
}
.masked-address {
  color: #999;
}
.more-info-toggle {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}
</style>
