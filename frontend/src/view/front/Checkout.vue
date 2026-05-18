<template>
  <div class="checkout-page">
    <header class="checkout-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代结算台</span>
        </div>
        <div class="step-indicator">
          <el-steps :active="2" align-center finish-status="success">
            <el-step title="购物车"></el-step>
            <el-step title="确认订单"></el-step>
            <el-step title="付款"></el-step>
            <el-step title="交易成功"></el-step>
          </el-steps>
        </div>
      </div>
    </header>

    <div class="checkout-container">
      <div class="left-panel">

        <div class="section-card address-section">
          <div class="section-header">
            <h3>确认收货地址</h3>
            <div class="header-actions">
              <el-button type="text" size="small">管理地址</el-button>
              <el-button type="text" size="small" @click="handleAddAddress">使用新地址</el-button>
            </div>
          </div>

          <div class="address-list">
            <div
                class="address-card"
                v-for="addr in addressList"
                :key="addr.id"
                :class="{ 'is-active': selectedAddressId === addr.id }"
                @click="selectedAddressId = addr.id">
              <div class="addr-tag" v-if="addr.isDefault === 1">默认</div>
              <div class="addr-name">
                <i class="el-icon-location-outline"></i> {{ addr.province }} {{ addr.city }} ({{ addr.consigneeName }} 收)
              </div>
              <div class="addr-detail">{{ addr.district }} {{ addr.detailAddress }}</div>
              <div class="addr-phone">{{ addr.phone }}</div>
              <div class="check-mark" v-if="selectedAddressId === addr.id">
                <i class="el-icon-check"></i>
              </div>
            </div>
          </div>
          <div class="show-more-addr" v-if="addressList.length > 3">
            <el-button type="text" size="mini">显示全部地址 <i class="el-icon-arrow-down"></i></el-button>
          </div>
        </div>

        <div class="section-card order-section">
          <div class="section-header">
            <h3>确认订单信息</h3>
          </div>

          <div class="shop-group" v-for="(shop) in orderData" :key="shop.shopId">
            <div class="shop-title">
              <i class="el-icon-s-shop shop-icon"></i> {{ shop.shopName }}
            </div>

            <div class="order-table-header">
              <span class="th-item">店铺宝贝</span>
              <span class="th-sku">商品属性</span>
              <span class="th-price">单价</span>
              <span class="th-qty">数量</span>
              <span class="th-sub">小计</span>
            </div>

            <div class="order-item" v-for="(item) in shop.items" :key="item.bookId">

              <div class="td-item">
                <el-image :src="item.image" class="item-img"></el-image>
                <div class="item-title">{{ item.title }}</div>
              </div>
              <div class="td-sku">
                <p v-for="(val, key) in item.specs" :key="key">{{ key }}: {{ val }}</p>
              </div>
              <div class="td-price">
                <span class="real-price">¥{{ item.price }}</span>
              </div>
              <div class="td-qty">
                <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="item.stock"
                    size="mini"
                    @change="calculateTotals">
                </el-input-number>
              </div>
              <div class="td-sub">
                <span class="sub-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
            </div>

            <div class="shop-footer">
              <div class="footer-row">
                <div class="row-label">订单备注</div>
                <div class="row-content">
                  <el-input
                      v-model="shop.remark"
                      placeholder="选填，请先和商家协商一致"
                      maxlength="200"
                      show-word-limit
                      size="small"
                      style="width: 400px;">
                  </el-input>
                </div>
              </div>
              <div class="footer-row">
                <div class="row-label">配送服务</div>
                <div class="row-content delivery-info">
                  快递免邮 <span class="delivery-time">预计付款后48小时内发货</span>
                </div>
                <div class="row-price">¥ 0.00</div>
              </div>
              <div class="shop-subtotal">
                店铺合计 (含运费): <span class="highlight-price">¥{{ getShopSubtotal(shop).toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="sticky-summary-box">
          <h3 class="summary-title">付款详情</h3>
          <p class="summary-subtitle">共 {{ totalQuantity }} 件商品</p>

          <div class="summary-list">
            <div class="sum-row">
              <span class="label">商品总价</span>
              <span class="val">¥ {{ totalProductPrice.toFixed(2) }}</span>
            </div>
            <div class="sum-row">
              <span class="label">运费</span>
              <span class="val">¥ {{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="sum-row discount-row" v-if="discountAmount > 0">
              <span class="label">店铺优惠券</span>
              <span class="val">- ¥ {{ discountAmount.toFixed(2) }}</span>
            </div>
          </div>

          <div class="extra-options">
            <el-checkbox v-model="usePrivacyNum" class="privacy-check">
              号码隐私保护 <span class="tip">商家将隐藏真实手机号</span>
            </el-checkbox>
          </div>

          <div class="final-pay-box">
            <div class="pay-text">实付款</div>
            <div class="pay-amount"><small>¥</small>{{ finalPayAmount.toFixed(2) }}</div>
          </div>

          <div class="action-box">
            <el-button class="btn-back" @click="$router.go(-1)">返回</el-button>
            <el-button class="btn-submit" :loading="isSubmitting" @click="handleSubmitOrder">提交订单</el-button>
          </div>

          <div class="selected-addr-preview" v-if="selectedAddress">
            <p><strong>寄送至:</strong> {{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detailAddress }}</p>
            <p><strong>收货人:</strong> {{ selectedAddress.consigneeName }} {{ selectedAddress.phone }}</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'UserCheckout',
  data() {
    return {
      userId:null,
      isSubmitting: false,
      usePrivacyNum: true,

      shippingFee: 0.00,
      discountAmount: 0.00,
      totalProductPrice: 0,
      totalQuantity: 0,
      finalPayAmount: 0,

      selectedAddressId: null,
      addressList: [],
      orderData: [],
      originalCartItems: []
    }
  },
  computed: {
    selectedAddress() {
      return this.addressList.find(addr => addr.id === this.selectedAddressId)
    }
  },
  async created() {
    // 🌟 1. 核心流程控制：必须先同步等待获取到当前登录用户的 ID
    await this.fetchUserInfo();

    // 如果没有获取到 userId（没登录），则直接 return，阻止后续无用的请求
    if (!this.userId) return;

    // 🌟 2. 拿到真实的 userId 后，再去加载商品和地址
    this.loadItemsFromStorage();
    this.fetchAddressList();
  },
  methods: {
    // 🌟 获取当前登录用户信息的逻辑
    async fetchUserInfo() {
      try {
        // 【方案 A：直接解析本地的 JWT Token】(推荐！对应你后端的 JwtUtil.java)
        const token = localStorage.getItem('user-token');
        if (token) {
          // JWT 格式为 Header.Payload.Signature，中间段包含自定义数据
          const payloadBase64 = token.split('.')[1];
          // 解决 base64 汉字解析及规范化
          const decodedPayload = decodeURIComponent(atob(payloadBase64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          }).join(''));

          const jwtData = JSON.parse(decodedPayload);
          // 对应你后端 JwtUtil 里的 .claim("userId", userId)
          this.userId = jwtData.userId;
        } else {
          // 【方案 B：通过你定义的后端接口获取】
          // 如果你更倾向于请求后端，可以解开下面的注释并使用：
          // const res = await request.get('/api/profile/me');
          // this.userId = res.data.id;

          if(!this.userId) {
            this.$message.warning('您还未登录，请先登录后再进行结算');
            this.$router.push('/login');
          }
        }
      } catch (error) {
        console.error('解析用户信息失败', error);
        this.$message.error('登录状态已失效，请重新登录');
        this.$router.push('/login');
      }
    },
    async fetchAddressList() {
      try {
        // 使用动态获取的 userId 去查地址
        const res = await request.get('/api/address/list', { params: { userId: this.userId } });
        this.addressList = res.data || res || [];

        if (this.addressList.length > 0) {
          const defaultAddr = this.addressList.find(addr => addr.isDefault === 1);
          this.selectedAddressId = defaultAddr ? defaultAddr.id : this.addressList[0].id;
        }
      } catch (error) {
        this.$message.error('获取收货地址失败');
      }
    },

    async loadItemsFromStorage() {
      const cartItemsStr = sessionStorage.getItem('checkoutItems');

      if (cartItemsStr) {
        const cartItems = JSON.parse(cartItemsStr);
        this.originalCartItems = cartItems;
        this.buildOrderData(cartItems);
      } else if (this.$route.query.productId) {
        const { productId, count } = this.$route.query;
        try {
          const res = await request.get(`/api/front/book/${productId}`);
          const book = res.data || res;
          this.buildOrderData([{
            bookId: book.id,
            title: book.title,
            image: book.images ? book.images[0] : book.coverImageUrl,
            skuName: '标准版',
            price: book.minPrice || book.price,
            quantity: parseInt(count) || 1,
            stock: book.stock
          }]);
        } catch (e) {
          this.$message.error('获取商品信息失败');
        }
      } else {
        this.$message.warning('没有结算商品，请返回购物车重新选择');
        this.$router.push('/cart');
      }
    },

    buildOrderData(items) {
      this.orderData = [{
        shopId: 101,
        shopName: '航海时代官方直营店',
        remark: '',
        items: items.map(i => ({
          bookId: i.bookId || i.id,
          title: i.title,
          image: i.image || i.coverUrl,
          specs: { '版本': i.skuName || '标准版' },
          price: parseFloat(i.price),
          quantity: i.quantity,
          stock: i.stock || 99
        }))
      }];
      this.calculateTotals();
    },

    // 动态计算总价与件数
    calculateTotals() {
      let sumPrice = 0
      let sumQty = 0
      this.orderData.forEach(shop => {
        shop.items.forEach(item => {
          sumPrice += item.price * item.quantity
          sumQty += item.quantity
        })
      })
      this.totalProductPrice = sumPrice
      this.totalQuantity = sumQty
      this.finalPayAmount = sumPrice + this.shippingFee - this.discountAmount
    },

    getShopSubtotal(shop) {
      let sub = 0
      shop.items.forEach(item => {
        sub += item.price * item.quantity
      })
      return sub
    },

    handleAddAddress() {
      this.$message.info('呼出新增地址弹窗...')
    },

    async handleSubmitOrder() {
      if (!this.selectedAddressId) {
        return this.$message.warning('请选择收货地址！')
      }
      this.isSubmitting = true

      try {
        const orderItems = [];
        this.orderData.forEach(shop => {
          shop.items.forEach(item => {
            orderItems.push({
              bookId: item.bookId,
              quantity: item.quantity
            });
          });
        });

        const payload = {
          userId: this.userId,
          addressId: this.selectedAddressId,
          orderItems: orderItems,
          remark: this.orderData[0].remark || ''
        };

        const res = await request.post('/api/order/create', payload);
        this.$message.success('订单提交成功，正在跳转支付...');
        await this.deleteCartItemsAfterOrder();
        sessionStorage.removeItem('checkoutItems');

        // 🌟 核心修复：安全提取订单号，不再写 res.data
        // 现在 res 就是后端 Map 里的那个对象：{ orderNo: "20505..." }
        const orderNo = (res && res.orderNo) ? res.orderNo : res;

        this.$router.push({
          path: '/pay',
          query: {
            orderNo: orderNo,
            amount: this.finalPayAmount.toFixed(2)
          }
        });
      } catch (error) {
        this.$message.error(error.message || '提交订单失败');
      } finally {
        this.isSubmitting = false;
      }
    },

    async deleteCartItemsAfterOrder() {
      if (!this.originalCartItems || this.originalCartItems.length === 0) return;

      try {
        const deleteTasks = this.originalCartItems.map(item => {
          if (item.cartId) {
            return request.delete(`/api/cart/delete/${item.cartId}`, {
              params: { userId: this.userId }
            });
          }
          return Promise.resolve();
        });
        await Promise.all(deleteTasks);
      } catch (error) {
        console.error('删除已结算购物车商品失败', error);
      }
    }
  }
}
</script>

<style scoped>
.checkout-page {
  background-color: #f4f4f4;
  min-height: 100vh;
  padding-bottom: 60px;
}

/* ================== 1. 顶部 Header ================== */
.checkout-header {
  background-color: #fff;
  padding: 15px 0;
  box-shadow: 0 1px 5px rgba(0,0,0,0.05);
  margin-bottom: 20px;
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
.step-indicator {
  width: 500px;
}

/* ================== 布局容器 ================== */
.checkout-container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}
.left-panel {
  width: 850px;
}
.right-panel {
  width: 330px;
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

/* ================== 2. 收货地址模块 ================== */
.address-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}
.address-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  background-color: #fff;
  overflow: hidden;
}
.address-card:hover {
  border-color: #ff5000;
}
.address-card.is-active {
  border-color: #ff5000;
  background-color: #fffaf7;
}
.addr-tag {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #f2f2f2;
  color: #999;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 0 8px 0 8px;
}
.address-card.is-active .addr-tag {
  background-color: #ff5000;
  color: #fff;
}
.addr-name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}
.addr-detail {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  height: 36px; /* 锁死两行高度 */
  overflow: hidden;
}
.addr-phone {
  margin-top: 8px;
  font-size: 13px;
  color: #333;
}
.check-mark {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background-color: #ff5000;
  color: #fff;
  border-radius: 12px 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
.show-more-addr {
  margin-top: 15px;
}

/* ================== 3. 订单详情模块 ================== */
.shop-group {
  margin-bottom: 30px;
}
.shop-group:last-child {
  margin-bottom: 0;
}
.shop-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}
.shop-icon {
  color: #1890ff;
  margin-right: 5px;
}
.order-table-header {
  display: flex;
  font-size: 12px;
  color: #999;
  border-bottom: 2px solid #f4f4f4;
  padding-bottom: 10px;
  margin-bottom: 15px;
}
.th-item { width: 300px; }
.th-sku { width: 180px; }
.th-price { width: 100px; text-align: center; }
.th-qty { width: 120px; text-align: center; }
.th-sub { width: 100px; text-align: right; }

.order-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}
.td-item {
  width: 300px;
  display: flex;
  gap: 12px;
}
.item-img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  border: 1px solid #eee;
}
.item-title {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
}
.td-sku {
  width: 180px;
  font-size: 12px;
  color: #999;
}
.td-sku p {
  margin: 0 0 4px 0;
}
.td-price {
  width: 100px;
  text-align: center;
  display: flex;
  flex-direction: column;
}
.del-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}
.real-price {
  font-size: 14px;
  color: #333;
}
.td-qty {
  width: 120px;
  text-align: center;
}
.td-sub {
  width: 100px;
  text-align: right;
  font-weight: bold;
  color: #ff5000;
}

/* 底部服务与备注 */
.shop-footer {
  background-color: #fdfdfd;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
.footer-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 13px;
}
.row-label {
  width: 80px;
  color: #666;
}
.row-content {
  flex: 1;
}
.delivery-info {
  color: #333;
}
.delivery-time {
  color: #ff5000;
  margin-left: 10px;
}
.row-price {
  font-weight: bold;
  color: #ff5000;
}
.shop-subtotal {
  text-align: right;
  font-size: 14px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #e8e8e8;
}
.highlight-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
  margin-left: 10px;
}

/* ================== 4. 右侧结算面板 (吸顶) ================== */
.sticky-summary-box {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  position: sticky;
  top: 20px;
}
.summary-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}
.summary-subtitle {
  margin: 0 0 20px 0;
  font-size: 12px;
  color: #999;
}

.summary-list {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}
.sum-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
.discount-row {
  color: #ff5000;
}

.extra-options {
  margin-bottom: 20px;
}
.privacy-check .tip {
  font-size: 12px;
  color: #999;
  margin-left: 5px;
}

.final-pay-box {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 20px;
}
.pay-text {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
.pay-amount {
  font-size: 28px;
  font-weight: bold;
  color: #ff5000;
}

.action-box {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}
.btn-back {
  flex: 1;
  border-radius: 20px;
}
.btn-submit {
  flex: 2;
  background: linear-gradient(90deg, #ff9000, #ff5000);
  border: none;
  color: #fff;
  font-weight: bold;
  border-radius: 20px;
}

/* 收货信息回显 (防止用户填错地址) */
.selected-addr-preview {
  background-color: #fafafa;
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
  border: 1px dashed #e8e8e8;
}
.selected-addr-preview p {
  margin: 0;
}
</style>
