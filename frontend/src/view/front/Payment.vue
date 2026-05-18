<template>
  <div class="payment-page">
    <!-- 顶部极简导航 -->
    <header class="payment-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代支付中心</span>
        </div>
        <div class="header-right">
          <span class="service-phone">客服热线：400-888-9999</span>
        </div>
      </div>
    </header>

    <div class="payment-container">
      <el-card class="payment-card" shadow="hover">
        <!-- 订单概览栏 -->
        <div class="order-overview">
          <div class="info-left">
            <h2 class="order-status-text">订单提交成功，请尽快付款！</h2>
            <p class="order-id">订单号：{{ orderNo }}</p>
            <p class="order-details-toggle" @click="showDetails = !showDetails">
              订单详情 <i :class="showDetails ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
            </p>
          </div>
          <div class="info-right">
            <span class="amount-label">应付金额：</span>
            <span class="amount-symbol">¥</span>
            <span class="amount-value">{{ totalAmount }}</span>
          </div>
        </div>

        <!-- 展开的商品详情 -->
        <el-collapse-transition>
          <div v-show="showDetails" class="details-content">
            <div class="item-row" v-for="item in productList" :key="item.id">
              <span class="item-name">{{ item.title }}</span>
              <span class="item-spec">{{ item.spec }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
              <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </el-collapse-transition>

        <!-- 支付核心区 -->
        <div class="payment-core">
          <div class="payment-methods-tab">
            <div class="method-item active">
              <i class="el-icon-full-screen"></i> 扫码支付
            </div>
            <div class="method-item disabled">网银支付</div>
            <div class="method-item disabled">余额支付</div>
          </div>

          <div class="qr-section">
            <div class="qr-wrapper">
              <!-- 这里模拟二维码图片 -->
              <div class="qr-code-placeholder">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MaritimePaymentMock" alt="支付二维码">
                <div class="qr-expired-mask" v-if="isExpired">
                  <p>二维码已失效</p>
                  <el-button type="text" @click="refreshQR">点击刷新</el-button>
                </div>
              </div>
              <div class="qr-instruction">
                <!-- 换成 Element UI 自带的扫码图标 -->
                <i class="el-icon-full-screen scan-icon-font"></i>
                <p>请使用 <span>手机支付软件</span><br>扫描二维码完成支付</p>
              </div>
            </div>

            <div class="payment-tips">
              <p><i class="el-icon-time"></i> 请在 <strong>{{ countdown }}</strong> 内完成支付，超时订单将自动取消</p>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-area">
            <el-button size="large" @click="handleCancel">取消支付</el-button>
            <el-button type="primary" size="large" class="btn-success-mock" @click="mockSuccess">模拟支付成功</el-button>
            <el-button type="danger" size="large" plain @click="mockFail">模拟支付失败</el-button>
          </div>
        </div>
      </el-card>

      <!-- 底部安全保障 -->
      <div class="security-footer">
        <div class="security-item">
          <i class="el-icon-circle-check"></i>
          <span>银联特约商户</span>
        </div>
        <div class="security-item">
          <i class="el-icon-lock"></i>
          <span>SSL加密传输</span>
        </div>
        <div class="security-item">
          <i class="el-icon-shield-check"></i>
          <span>支付安全保障</span>
        </div>
      </div>
    </div>

    <!-- 支付成功弹窗 -->
    <el-dialog
        title="支付结果"
        :visible.sync="successVisible"
        width="400px"
        center
        :close-on-click-modal="false"
        :show-close="false">
      <div class="result-dialog">
        <i class="el-icon-success result-icon success"></i>
        <h3 class="result-title">支付成功！</h3>
        <p class="result-msg">您的订单已进入待发货状态，我们将尽快起航。</p>
        <div class="result-actions">
          <el-button type="primary" @click="goToOrders">查看订单</el-button>
          <el-button @click="goToHome">回到首页</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 支付失败弹窗 -->
    <el-dialog
        title="支付结果"
        :visible.sync="failVisible"
        width="400px"
        center>
      <div class="result-dialog">
        <i class="el-icon-error result-icon error"></i>
        <h3 class="result-title">支付遇到问题</h3>
        <p class="result-msg">可能是余额不足或网络异常，请尝试重新扫码。</p>
        <div class="result-actions">
          <el-button type="primary" @click="failVisible = false">重新支付</el-button>
          <el-button @click="handleCancel">取消订单</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request';
export default {
  name: 'UserPayment',
  data() {
    return {
      userId: null,      // 🌟 动态获取
      orderNo: '',       // 🌟 动态获取
      totalAmount: '1387.00',
      showDetails: false,
      isExpired: false,
      countdown: '14:59',
      timer: null,
      successVisible: false,
      failVisible: false,
      productList: []
    }
  },
  async created() {
    // 1. 从路由中获取传递过来的订单号和金额
    this.orderNo = this.$route.query.orderNo || '';
    this.totalAmount = this.$route.query.amount || '0.00';

    // 2. 解析本地 Token 获取真实 userId
    await this.fetchUserInfo();

    // 3. 如果有订单号，拉取真实的订单明细用于展示
    if (this.orderNo) {
      this.fetchOrderDetails();
    }
  },
  mounted() {
    this.startCountdown();
    // 从路由中获取传递过来的订单号和金额
    if (this.$route.query.orderNo) {
      this.orderNo = this.$route.query.orderNo;
    }
    if (this.$route.query.amount) {
      this.totalAmount = this.$route.query.amount;
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  },
  methods: {

    async fetchUserInfo() {
      try {
        const token = localStorage.getItem('user-token');
        if (token) {
          const payloadBase64 = token.split('.')[1];
          const decodedPayload = decodeURIComponent(atob(payloadBase64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));

          const jwtData = JSON.parse(decodedPayload);
          this.userId = jwtData.userId;
        } else {
          this.$message.warning('请先登录');
          this.$router.push('/login');
        }
      } catch (error) {
        console.error('解析用户信息失败', error);
        this.$router.push('/login');
      }
    },

    // 🌟 真实调用后端获取订单明细接口
    async fetchOrderDetails() {
      try {
        const res = await request.get('/api/order/detail/full', {
          params: { orderNo: this.orderNo }
        });
        const orderData = res.data || res;

        // 将后端返回的 items 映射到前端列表
        if (orderData && orderData.items) {
          this.productList = orderData.items.map(item => ({
            id: item.bookId,
            title: item.bookTitle,
            spec: '标准版',
            price: item.price,
            quantity: item.quantity
          }));
        }
      } catch (error) {
        console.error('获取订单明细失败', error);
      }
    },
    startCountdown() {
      let minutes = 14;
      let seconds = 59;
      this.timer = setInterval(() => {
        if (seconds === 0) {
          if (minutes === 0) {
            clearInterval(this.timer);
            this.isExpired = true;
            return;
          }
          minutes--;
          seconds = 59;
        } else {
          seconds--;
        }
        this.countdown = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
      }, 1000);
    },


    refreshQR() {
      this.isExpired = false;
      this.startCountdown();
      this.$message.success('二维码已刷新');
    },
    handleCancel() {
      this.$confirm('确认要离开支付页面吗？您的订单会被保留在"待付款"状态。', '提示', {
        confirmButtonText: '确定离开',
        cancelButtonText: '继续支付',
        type: 'warning'
      }).then(() => {
        this.$router.push('/user/orders');
      }).catch(() => {});
    },
    // 🌟 核心：真实调用后端支付接口
    async mockSuccess() {
      try {
        const payload = {
          userId: this.userId,
          orderNo: this.orderNo,
          payType: 1 // 假设 1 代表支付宝/扫码支付
        };

        // 发送支付请求，后端会校验库存、扣减库存、更新订单为已支付(待发货)
        await request.post('/api/order/pay', payload);

        // 支付成功后停止倒计时，并弹出成功框
        if (this.timer) clearInterval(this.timer);
        this.successVisible = true;
      } catch (error) {
        // 请求拦截器会自动弹出 error message，如“库存不足”等
        console.error('支付失败', error);
      }
    },

    mockFail() {
      this.failVisible = true;
    },
    goToOrders() {
      this.$router.push('/user/orders');
    },
    goToHome() {
      this.$router.push('/');
    }
  }
}
</script>

<style scoped>
.payment-page {
  background-color: #f4f7f9;
  min-height: 100vh;
  padding-bottom: 50px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}

/* 顶部 Header */
.payment-header {
  background-color: #fff;
  border-bottom: 2px solid #1890ff;
  padding: 15px 0;
}
.header-content {
  width: 1000px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo-area {
  display: flex;
  align-items: center;
  color: #1890ff;
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
.service-phone {
  font-size: 14px;
  color: #999;
}

/* 容器布局 */
.payment-container {
  width: 1000px;
  margin: 30px auto;
}

.payment-card {
  border-radius: 8px;
  border: none;
}

/* 订单概览 */
.order-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 10px;
  border-bottom: 1px dashed #e8e8e8;
}
.order-status-text {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}
.order-id {
  font-size: 14px;
  color: #666;
  margin: 0 0 5px 0;
}
.order-details-toggle {
  font-size: 13px;
  color: #1890ff;
  cursor: pointer;
  display: inline-block;
}

.info-right {
  text-align: right;
}
.amount-label {
  font-size: 14px;
  color: #666;
}
.amount-symbol {
  font-size: 16px;
  color: #ff5000;
  font-weight: bold;
}
.amount-value {
  font-size: 32px;
  color: #ff5000;
  font-weight: bold;
}

/* 商品详情列表 */
.details-content {
  background-color: #fafafa;
  padding: 15px 20px;
  border-radius: 4px;
  margin: 10px 0;
}
.item-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}
.item-name { flex: 2; }
.item-spec { flex: 1; color: #999; }
.item-qty { flex: 0.5; text-align: center; }
.item-subtotal { flex: 0.5; text-align: right; font-weight: bold; }

/* 支付核心区 */
.payment-core {
  padding: 40px 0;
}
.payment-methods-tab {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 30px;
}
.method-item {
  padding: 10px 30px;
  font-size: 16px;
  cursor: pointer;
  position: relative;
  color: #666;
}
.method-item.active {
  color: #1890ff;
  font-weight: bold;
}
.method-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #1890ff;
}
.method-item.disabled {
  color: #ccc;
  cursor: not-allowed;
}

/* 扫码区 */
.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.qr-wrapper {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.qr-code-placeholder {
  width: 200px;
  height: 200px;
  position: relative;
  border: 1px solid #eee;
}
.qr-code-placeholder img {
  width: 100%;
  height: 100%;
}
.qr-expired-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #333;
}
.qr-instruction {
  width: 180px;
}
/* 修改为字体图标的样式 */
.scan-icon-font {
  font-size: 38px;
  color: #1890ff;
  margin-bottom: 10px;
  display: inline-block;
}
.qr-instruction p {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
}
.qr-instruction p span {
  color: #ff5000;
  font-weight: bold;
}

.payment-tips {
  margin-top: 20px;
  font-size: 13px;
  color: #999;
}
.payment-tips strong {
  color: #ff5000;
}

/* 操作区域 */
.action-area {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
.btn-success-mock {
  background-color: #13ce66;
  border-color: #13ce66;
  color: #fff;
}
.btn-success-mock:hover {
  background-color: #42d885;
  border-color: #42d885;
}

/* 底部安全图标 */
.security-footer {
  display: flex;
  justify-content: center;
  gap: 50px;
  margin-top: 40px;
  color: #b0b0b0;
  font-size: 14px;
}
.security-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.security-item i {
  font-size: 20px;
}

/* 弹窗样式 */
.result-dialog {
  text-align: center;
  padding: 20px 0;
}
.result-icon {
  font-size: 60px;
  margin-bottom: 20px;
}
.result-icon.success { color: #67C23A; }
.result-icon.error { color: #F56C6C; }
.result-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 10px;
}
.result-msg {
  font-size: 14px;
  color: #999;
  margin-bottom: 30px;
}
.result-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>