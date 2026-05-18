<template>
  <div class="refund-detail-page">
    <div class="detail-container">
      <!-- 进度条 -->
      <div class="progress-section" v-if="refundData.currentStep">
        <div class="progress-bar">
          <div class="progress-step" :class="{ active: refundData.currentStep >= 1, current: refundData.currentStep === 1 }">
            <div class="step-num">1</div>
            <div class="step-text">买家申请退款</div>
          </div>
          <div class="progress-line" :class="{ active: refundData.currentStep >= 2 }"></div>
          <div class="progress-step" :class="{ active: refundData.currentStep >= 2, current: refundData.currentStep === 2 }">
            <div class="step-num">2</div>
            <div class="step-text">卖家处理退款申请</div>
          </div>
          <div class="progress-line" :class="{ active: refundData.currentStep >= 3 }"></div>
          <div class="progress-step" :class="{ active: refundData.currentStep >= 3, current: refundData.currentStep === 3 }">
            <div class="step-num">3</div>
            <div class="step-text">退款完毕</div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <!-- 左侧主要内容 -->
        <div class="left-panel">
          <!-- 退款状态 -->
          <div class="status-section">
            <h2 class="status-title">{{ refundData.refundStatusDesc }}</h2>
            <p class="status-time" v-if="refundData.refundFinishTime">退款成功时间：{{ refundData.refundFinishTime }}</p>
            <p class="status-time" v-else-if="refundData.auditTime">审核时间：{{ refundData.auditTime }}</p>
            <p class="status-time" v-else>申请时间：{{ refundData.applyTime }}</p>
          </div>

          <!-- 退款金额 -->
          <div class="amount-section">
            <div class="amount-row">
              <span class="amount-label">退款总金额：</span>
              <span class="amount-value">¥{{ refundData.refundAmount }}</span>
            </div>
          </div>

          <!-- 退款信息 -->
          <div class="info-section">
            <div class="info-title">退款信息</div>
            <div class="info-content">
              <div class="info-row">
                <span class="info-label">退款编号：</span>
                <span class="info-value">{{ refundData.refundNo }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">退款类型：</span>
                <span class="info-value">{{ refundData.refundTypeDesc }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">退款原因：</span>
                <span class="info-value">{{ refundData.refundReason }}</span>
              </div>
              <div class="info-row" v-if="refundData.refundDesc">
                <span class="info-label">退款说明：</span>
                <span class="info-value">{{ refundData.refundDesc }}</span>
              </div>
              <div class="info-row" v-if="refundData.rejectReason">
                <span class="info-label">拒绝原因：</span>
                <span class="info-value reject">{{ refundData.rejectReason }}</span>
              </div>
            </div>
          </div>

          <!-- 协商历史 -->
          <div class="history-section" v-if="refundData.historyList && refundData.historyList.length > 0">
            <div class="history-title">协商历史</div>
            <div class="history-list">
              <div class="history-item" v-for="(item, index) in refundData.historyList" :key="index">
                <div class="history-header">
                  <span class="history-user">{{ item.userName }}</span>
                  <span class="history-time">{{ item.createTime }}</span>
                </div>
                <div class="history-content">{{ item.content }}</div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section" v-if="canCancel">
            <el-button type="primary" class="btn-primary" @click="handleCancel">撤销退款申请</el-button>
          </div>
        </div>

        <!-- 右侧退款详情 -->
        <div class="right-panel">
          <div class="detail-card">
            <div class="card-title">退款详情</div>

            <!-- 商品信息 -->
            <div class="product-section">
              <img :src="refundData.coverUrl" alt="" class="product-img">
              <div class="product-info">
                <div class="product-name">{{ refundData.bookName }}</div>
                <div class="product-price">¥{{ refundData.price }} x {{ refundData.quantity }}</div>
              </div>
            </div>

            <el-divider></el-divider>

            <!-- 订单信息 -->
            <div class="order-info">
              <div class="info-row">
                <span class="label">卖家：</span>
                <span class="value">航海时代官方直营店</span>
              </div>
              <div class="info-row">
                <span class="label">订单编号：</span>
                <span class="value link" @click="viewOrder">{{ refundData.orderNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">成交时间：</span>
                <span class="value">{{ refundData.orderPayTime }}</span>
              </div>
              <div class="info-row">
                <span class="label">单价：</span>
                <span class="value">¥{{ refundData.price }}</span>
              </div>
              <div class="info-row">
                <span class="label">数量：</span>
                <span class="value">{{ refundData.quantity }}</span>
              </div>
              <div class="info-row">
                <span class="label">运费：</span>
                <span class="value">¥{{ refundData.freightAmount || 0 }}</span>
              </div>
              <div class="info-row">
                <span class="label">商品总价：</span>
                <span class="value">¥{{ refundData.itemTotalPrice }}</span>
              </div>
            </div>

            <el-divider></el-divider>

            <!-- 退款信息 -->
            <div class="refund-info">
              <div class="info-row">
                <span class="label">退款编号：</span>
                <span class="value">{{ refundData.refundNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">退款金额：</span>
                <span class="value highlight">¥{{ refundData.refundAmount }}</span>
              </div>
              <div class="info-row">
                <span class="label">退款原因：</span>
                <span class="value">{{ refundData.refundReason }}</span>
              </div>
              <div class="info-row">
                <span class="label">要求：</span>
                <span class="value">{{ refundData.refundTypeDesc }}</span>
              </div>
              <div class="info-row">
                <span class="label">货物状态：</span>
                <span class="value">{{ goodsStatusText }}</span>
              </div>
              <div class="info-row" v-if="refundData.refundDesc">
                <span class="label">说明：</span>
                <span class="value">{{ refundData.refundDesc }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getRefundDetail, cancelRefund } from '@/api/front/refund';
import { getUserProfile } from '@/api/front/user';

export default {
  name: 'RefundDetail',
  data() {
    return {
      loading: false,
      userId: null,
      refundNo: '',
      refundData: {
        id: null,
        refundNo: '',
        refundType: null,
        refundTypeDesc: '',
        refundStatus: null,
        refundStatusDesc: '',
        refundAmount: '0.00',
        itemTotalPrice: '0.00',
        refundReason: '',
        refundDesc: '',
        rejectReason: '',
        orderItemId: null,
        bookId: null,
        bookName: '',
        coverUrl: '',
        price: '0.00',
        quantity: 1,
        orderId: null,
        orderNo: '',
        orderPayTime: '',
        orderPayAmount: '0.00',
        freightAmount: '0.00',
        applyTime: '',
        auditTime: '',
        returnTime: '',
        receiveTime: '',
        refundFinishTime: '',
        historyList: [],
        currentStep: 1
      }
    }
  },
  computed: {
    canCancel() {
      // 只有待审核状态可以取消
      return this.refundData.refundStatus === 0;
    },
    goodsStatusText() {
      // 根据退款类型和描述推断货物状态
      if (this.refundData.refundType === 1) {
        return '未收到货';
      }
      return '已收到货';
    }
  },
  async created() {
    await this.fetchUserInfo();
    this.refundNo = this.$route.query.refundNo || this.$route.query.id;
    if (this.refundNo) {
      this.fetchRefundDetail();
    } else {
      this.$message.error('缺少退款单号');
      this.$router.push('/user/refund');
    }
  },
  methods: {
    async fetchUserInfo() {
      try {
        const profile = await getUserProfile();
        this.userId = profile && (profile.userId || profile.id);
        if (!this.userId) throw new Error('未获取到用户信息');
      } catch (error) {
        console.error('获取用户信息失败', error);
        this.$message.error('登录状态异常');
        this.$router.push('/login');
      }
    },

    async fetchRefundDetail() {
      if (!this.userId || !this.refundNo) return;

      this.loading = true;
      try {
        const params = {
          userId: this.userId,
          refundNo: this.refundNo
        };
        const res = await getRefundDetail(params);
        const data = res.data || res;

        if (data) {
          this.refundData = { ...this.refundData, ...data };
          // 格式化时间
          if (this.refundData.applyTime) {
            this.refundData.applyTime = this.refundData.applyTime.replace('T', ' ');
          }
          if (this.refundData.auditTime) {
            this.refundData.auditTime = this.refundData.auditTime.replace('T', ' ');
          }
          if (this.refundData.refundFinishTime) {
            this.refundData.refundFinishTime = this.refundData.refundFinishTime.replace('T', ' ');
          }
          if (this.refundData.orderPayTime) {
            this.refundData.orderPayTime = this.refundData.orderPayTime.replace('T', ' ');
          }
        }
      } catch (error) {
        console.error('获取退款详情失败', error);
        this.$message.error('加载退款详情失败');
      } finally {
        this.loading = false;
      }
    },

    viewOrder() {
      if (this.refundData.orderNo) {
        this.$router.push({
          path: '/user/order-detail',
          query: { orderNo: this.refundData.orderNo }
        });
      }
    },

    async handleCancel() {
      this.$confirm('确定要取消退款申请吗？取消后该订单将恢复正常状态。', '撤销提示', {
        confirmButtonText: '确定撤销',
        cancelButtonText: '暂不撤销',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelRefund(this.refundData.id);
          this.$message.success('退款已取消');
          this.fetchRefundDetail();
        } catch (error) {
          this.$message.error(error.message || '取消失败');
        }
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.refund-detail-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 20px 0;
}

.detail-container {
  width: 1200px;
  margin: 0 auto;
}

/* ================== 进度条 ================== */
.progress-section {
  background: #fff;
  border-radius: 8px;
  padding: 30px 50px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.progress-bar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.step-text {
  font-size: 14px;
  color: #999;
  white-space: nowrap;
}

.progress-step.active .step-num {
  background: #ff5000;
  color: #fff;
}

.progress-step.active .step-text {
  color: #ff5000;
  font-weight: bold;
}

.progress-step.current .step-num {
  background: #ff5000;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(255, 80, 0, 0.2);
}

.progress-line {
  width: 150px;
  height: 2px;
  background: #e0e0e0;
  margin: 0 20px;
  margin-bottom: 30px;
  transition: all 0.3s;
}

.progress-line.active {
  background: #ff5000;
}

/* ================== 主内容区 ================== */
.main-content {
  display: flex;
  gap: 20px;
}

.left-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.right-panel {
  width: 350px;
}

/* ================== 状态区 ================== */
.status-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.status-title {
  font-size: 20px;
  font-weight: bold;
  color: #52c41a;
  margin: 0 0 10px 0;
}

.status-time {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* ================== 金额区 ================== */
.amount-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.amount-row {
  display: flex;
  align-items: baseline;
}

.amount-label {
  font-size: 14px;
  color: #333;
  margin-right: 10px;
}

.amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #ff5000;
}

/* ================== 信息区 ================== */
.info-section {
  margin-bottom: 25px;
}

.info-title,
.history-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #ff5000;
}

.info-content {
  background: #fafafa;
  border-radius: 6px;
  padding: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 100px;
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: #333;
}

.info-value.reject {
  color: #ff4d4f;
}

/* ================== 协商历史 ================== */
.history-section {
  margin-bottom: 25px;
}

.history-list {
  background: #fafafa;
  border-radius: 6px;
  padding: 20px;
}

.history-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #e0e0e0;
}

.history-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.history-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.history-user {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.history-time {
  font-size: 12px;
  color: #999;
}

.history-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

/* ================== 操作按钮 ================== */
.action-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-primary {
  background: #ff5000;
  border-color: #ff5000;
  padding: 12px 40px;
  font-size: 14px;
}

.btn-primary:hover {
  background: #e64800;
  border-color: #e64800;
}

/* ================== 右侧详情卡片 ================== */
.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

/* 商品信息 */
.product-section {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #eee;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 13px;
  color: #999;
}

/* 订单和退款信息 */
.order-info,
.refund-info {
  margin-top: 15px;
}

.order-info .info-row,
.refund-info .info-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 13px;
}

.order-info .label,
.refund-info .label {
  width: 80px;
  color: #999;
  flex-shrink: 0;
}

.order-info .value,
.refund-info .value {
  flex: 1;
  color: #333;
}

.order-info .value.link {
  color: #1890ff;
  cursor: pointer;
}

.order-info .value.link:hover {
  text-decoration: underline;
}

.order-info .value.highlight,
.refund-info .value.highlight {
  color: #ff5000;
  font-weight: bold;
  font-size: 16px;
}

/* Element UI 分割线 */
::v-deep .el-divider {
  margin: 15px 0;
}
</style>
