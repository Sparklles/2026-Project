<template>
  <div class="refund-page">
    <div class="refund-container">
      <!-- 页面标题 -->
      <div class="page-title">
        <i class="el-icon-warning" style="color: #ff5000;"></i>
        退款/售后
      </div>

      <!-- 商品卡片 -->
      <div class="product-card">
        <div class="product-header">
          <span class="product-title">{{ productTitle }}</span>
        </div>
        <div class="product-body">
          <el-image :src="productImage" fit="cover" class="product-img"></el-image>
          <div class="product-detail">
            <div class="product-name">{{ productTitle }}</div>
            <div class="product-spec">{{ productSpec }}</div>
            <div class="product-price-row">
              <span class="product-price">
                <small>¥</small>{{ productPrice }}
              </span>
              <span class="product-quantity">x{{ productQty }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 退款类型选择 -->
      <div class="form-section">
        <div class="section-title">选择退款类型</div>
        <el-radio-group v-model="refundType" class="refund-type-group">
          <el-radio :label="0" style="margin-bottom: 0;">
            <div class="type-card">
              <span class="type-name">我要退款（无需退货）</span>
              <span class="type-desc">没收到货，或与卖家协商同意不用退货只退款</span>
            </div>
          </el-radio>
          <el-radio :label="1" style="margin-bottom: 0;">
            <div class="type-card">
              <span class="type-name">我要退货退款</span>
              <span class="type-desc">已收到货，需要退还收到的货物</span>
            </div>
          </el-radio>
          <el-radio :label="2" style="margin-bottom: 0;" disabled>
            <div class="type-card type-disabled">
              <span class="type-name">我要换货</span>
              <span class="type-desc">已收到货，需要退还更换（功能开发中）</span>
            </div>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 货物状态（仅退款时显示） -->
      <div class="form-section" v-if="refundType === 0">
        <div class="section-title">选择货物状态</div>
        <div class="reason-list">
          <el-radio-group v-model="goodsStatus">
            <el-radio label="not_received" style="margin-bottom: 0;">未收到货</el-radio>
            <el-radio label="received" style="margin-bottom: 0;">已收到货</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 退款原因 -->
      <div class="form-section" v-if="refundType !== null && (refundType === 1 || goodsStatus)">
        <div class="section-title">选择退款原因</div>
        <div class="reason-select">
          <el-select v-model="refundReason" placeholder="请选择退款原因" style="width: 100%;">
            <el-option
              v-for="reason in currentReasons"
              :key="reason"
              :label="reason"
              :value="reason">
            </el-option>
          </el-select>
        </div>
      </div>

      <!-- 退款说明 -->
      <div class="form-section" v-if="refundType !== null">
        <div class="section-title">退款说明</div>
        <el-input
          v-model="description"
          type="textarea"
          :rows="4"
          placeholder="请输入退款详细说明（选填）"
          maxlength="500"
          show-word-limit>
        </el-input>
      </div>

      <!-- 物流单号（退货退款时显示） -->
      <div class="form-section" v-if="showLogisticsInput">
        <div class="section-title">物流单号 <span style="color: #999;">(选填)</span></div>
        <el-input v-model="expressNo" placeholder="请输入退回商品的物流单号" maxlength="50"></el-input>
      </div>

      <!-- 提示 -->
      <div class="tips-section">
        <i class="el-icon-info"></i>
        <span>退款将在审核通过后，在1-7个工作日内退回至原支付账户，请耐心等待。</span>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <el-button type="primary" class="submit-btn" :disabled="!canSubmit" @click="handleSubmit">提交申请</el-button>
        <el-button class="cancel-btn" @click="handleCancel">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { applyRefund } from '@/api/front/refund';
import { getUserProfile } from '@/api/front/user';

export default {
  name: 'RefundApply',
  data() {
    return {
      userId: null,
      orderItemId: null,
      productTitle: '',
      productImage: '',
      productSpec: '',
      productPrice: '',
      productQty: '',
      refundType: null,
      goodsStatus: '',
      refundReason: '',
      description: '',
      expressNo: '',
      // 退货退款原因列表
      returnRefundReasons: [
        '不想要了',
        '协商一致退款',
        '退运费',
        '成分与描述不符',
        '生产日期/保质期与描述不符',
        '颜色/款式/大小/尺寸与描述不符',
        '包装/商品破损/污渍/裂痕/变形',
        '做工粗糙有瑕疵',
        '质量问题',
        '商家发错货'
      ],
      // 仅退款 - 已收到货原因列表
      refundOnlyReceivedReasons: [
        '不想要了',
        '协商一致退款',
        '退运费',
        '成分与描述不符',
        '生产日期/保质期与描述不符',
        '颜色/款式/大小/尺寸与描述不符',
        '包装/商品破损/污渍/裂痕/变形',
        '做工粗糙有瑕疵',
        '质量问题',
        '商家发错货'
      ],
      // 仅退款 - 未收到货原因列表
      refundOnlyNotReceivedReasons: [
        '不想要了',
        '协商一致退款',
        '空包裹',
        '未按约定时间发货',
        '快递/物流一直未送到',
        '商品破损已拒签'
      ]
    };
  },
  computed: {
    showLogisticsInput() {
      return this.refundType === 1;
    },
    canSubmit() {
      // 仅退款(0)：需要选择货物状态和原因
      // 退货退款(1)：需要选择原因
      if (this.refundType === 0) {
        return this.userId != null && this.orderItemId != null && this.goodsStatus && this.refundReason;
      }
      return this.userId != null && this.orderItemId != null && this.refundType !== null && this.refundReason;
    },
    // 根据退款类型和货物状态返回对应的原因列表
    currentReasons() {
      if (this.refundType === 1) {
        // 退货退款
        return this.returnRefundReasons;
      } else if (this.refundType === 0) {
        // 仅退款
        if (this.goodsStatus === 'received') {
          return this.refundOnlyReceivedReasons;
        } else if (this.goodsStatus === 'not_received') {
          return this.refundOnlyNotReceivedReasons;
        }
      }
      return [];
    }
  },
  async created() {
    // 从 URL 参数获取信息
    const params = this.getQueryParams();
    this.orderItemId = params.orderItemId ? parseInt(params.orderItemId) : null;
    this.productTitle = decodeURIComponent(params.productTitle || '');
    this.productImage = decodeURIComponent(params.productImage || '');
    this.productSpec = decodeURIComponent(params.productSpec || '标准版');
    this.productPrice = params.productPrice || '';
    this.productQty = params.productQty || '1';
    await this.fetchUserInfo();

    if (!this.userId || !this.orderItemId) {
      this.$message.error('缺少必要参数');
      setTimeout(() => window.close(), 2000);
    }
  },
  watch: {
    refundType(newVal) {
      // 重置相关字段
      this.refundReason = '';
      this.goodsStatus = '';
      if (newVal !== 2) {
        this.expressNo = '';
      }
    },
    goodsStatus() {
      // 切换货物状态时重置退款原因
      this.refundReason = '';
    }
  },
  methods: {
    getQueryParams() {
      const search = window.location.search;
      if (!search) return {};
      const params = {};
      search.substring(1).split('&').forEach(pair => {
        const [key, value] = pair.split('=');
        params[key] = value;
      });
      return params;
    },
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
    async handleSubmit() {
      this.$confirm('确认提交退款申请？提交后将等待管理员审核。', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await applyRefund({
            userId: this.userId,
            orderItemId: this.orderItemId,
            refundType: this.refundType,
            refundReason: this.refundReason,
            description: this.description || undefined
          });
          this.$message.success('退款申请已提交，等待管理员审核');
          setTimeout(() => window.close(), 1500);
        } catch (error) {
          this.$message.error(error.message || '提交失败，请重试');
        }
      }).catch(() => {});
    },
    handleCancel() {
      window.close();
    }
  }
};
</script>

<style scoped>
.refund-page {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 30px 0;
}

.refund-container {
  width: 750px;
  margin: 0 auto;
}

/* 页面标题 */
.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

/* 商品卡片 */
.product-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.product-header {
  padding: 15px 20px 0;
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
.product-body {
  display: flex;
  padding: 15px 20px;
  gap: 15px;
}
.product-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
}
.product-detail {
  flex: 1;
}
.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.product-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}
.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 20px;
}
.product-price {
  font-size: 16px;
  color: #ff5000;
  font-weight: bold;
}
.product-price small {
  font-size: 12px;
  font-weight: normal;
}
.product-quantity {
  font-size: 13px;
  color: #999;
}

/* 表单区块 */
.form-section {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

/* 退款类型 */
.refund-type-group ::v-deep .el-radio {
  margin-right: 0;
  margin-bottom: 15px;
  width: 100%;
}
.refund-type-group ::v-deep .el-radio__input {
  display: none;
}
.refund-type-group ::v-deep .el-radio__label {
  padding-left: 0;
  width: 100%;
}
.type-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.type-card:hover {
  border-color: #ff5000;
}
.type-card .type-name {
  display: block;
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}
.type-card .type-desc {
  display: block;
  font-size: 12px;
  color: #999;
}
.type-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.type-disabled:hover {
  border-color: #e8e8e8;
}

/* 选中态 */
.refund-type-group ::v-deep .el-radio.is-checked .type-card {
  border-color: #ff5000;
  background: #fff7f3;
}
.refund-type-group ::v-deep .el-radio.is-checked .type-card .type-name {
  color: #ff5000;
}

/* 退款原因 */
.reason-list ::v-deep .el-radio {
  display: block;
  margin-bottom: 10px;
  line-height: 1.6;
  font-size: 13px;
  color: #333;
}
.reason-list ::v-deep .el-radio:last-child {
  margin-bottom: 0;
}
.reason-list ::v-deep .el-radio__input.is-checked + .el-radio__label {
  color: #ff5000;
  font-weight: bold;
}

/* 提示 */
.tips-section {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 20px;
  font-size: 12px;
  color: #d48806;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.5;
}
.tips-section i {
  margin-top: 2px;
}

/* 提交按钮 */
.submit-section {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-bottom: 20px;
}
.submit-btn {
  width: 140px;
  height: 40px;
  border-radius: 20px;
  background: #ff5000;
  border-color: #ff5000;
  font-size: 14px;
}
.submit-btn:hover {
  background: #e64800;
  border-color: #e64800;
}
.submit-btn:disabled {
  background: #f5f5f5;
  border-color: #dcdfe6;
  color: #c0c4cc;
}
.cancel-btn {
  width: 90px;
  height: 40px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  font-size: 14px;
}
</style>
