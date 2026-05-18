<template>
  <div class="cart-page-wrapper">
    <top-nav></top-nav>
    <div class="cart-page">

    <div class="cart-container">

      <div class="cart-left">
        <div class="cart-tabs">
          <div class="tab-item active">全部商品 <span class="count">{{ totalItemsCount }}</span></div>
          <div class="tab-item"><i class="el-icon-s-ticket" style="color: #ff5000;"></i> 消费券</div>
          <div class="tab-item"><i class="el-icon-bottom" style="color: #ff5000;"></i> 降价商品</div>
        </div>

        <div class="action-bar">
          <div class="action-left">
            <el-checkbox v-model="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
            <el-button type="text" size="small" class="text-btn" @click="handleBatchMoveFav">移入收藏</el-button>
            <el-button type="text" size="small" class="text-btn" @click="handleBatchDelete" :loading="deleteLoading">删除</el-button>
          </div>
          <div class="action-right">
            <el-select v-model="filterStatus" placeholder="状态" size="small" style="width: 100px; margin-right: 10px;">
              <el-option label="全部状态" value=""></el-option>
              <el-option label="正常售卖" value="normal"></el-option>
              <el-option label="已失效" value="invalid"></el-option>
            </el-select>
            <el-input
                v-model="searchKeyword"
                placeholder="搜索购物车内商品"
                size="small"
                style="width: 180px;"
                suffix-icon="el-icon-search">
            </el-input>
          </div>
        </div>

        <div class="promo-banner">
          <div class="promo-left">
            <i class="el-icon-s-opportunity"></i> 您有 <b>2张共计21元</b> 航海津贴，可尽快使用
          </div>
          <div class="promo-right">
            距结束 <span class="time-box">08</span> : <span class="time-box">27</span> : <span class="time-box">09</span>
          </div>
        </div>

        <div class="shop-group" v-for="(shop, shopIndex) in filteredShopList" :key="shop.shopId">
          <div class="shop-header">
            <el-checkbox v-model="shop.checked" @change="handleShopSelect(shop)"></el-checkbox>
            <span class="shop-icon"><i class="el-icon-s-shop"></i></span>
            <span class="shop-name">{{ shop.shopName }}</span>
            <i class="el-icon-chat-dot-round" style="color: #1890ff; margin-left: 5px; cursor: pointer;"></i>
          </div>

          <div
              class="cart-item"
              v-for="(item, itemIndex) in shop.items"
              :key="item.cartId"
              :class="{ 'is-invalid': item.status === 'invalid' }">

            <div class="item-checkbox">
              <el-checkbox v-model="item.checked" :disabled="item.status === 'invalid'" @change="handleItemSelect"></el-checkbox>
            </div>

            <div class="item-img">
              <el-image :src="item.image" fit="cover"></el-image>
              <div class="invalid-mask" v-if="item.status === 'invalid'">商品下架</div>
            </div>

            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-tags" v-if="item.tags">
                <span class="p-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>

            <div class="item-sku">
              <p>{{ item.skuName }}</p>
              <el-button v-if="item.status === 'invalid'" size="mini" plain style="margin-top: 5px;">找同款</el-button>
            </div>

            <div class="item-price">
              <span class="price-symbol">¥</span>{{ item.price }}
            </div>

            <div class="item-quantity">
              <el-input-number
                  v-if="item.status === 'normal'"
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stock"
                  size="mini"
                  @change="(val) => handleQuantityChange(val, item)">
              </el-input-number>
              <span v-else class="invalid-text">1</span>
            </div>

            <div class="item-action">
              <a href="javascript:;" @click="handleMoveFav(shopIndex, itemIndex)">移入收藏</a>
              <a href="javascript:;" @click="handleDelete(shopIndex, itemIndex)">删除</a>
            </div>
          </div>
        </div>

        <el-empty v-if="loading" description="正在装载您的购物车..."></el-empty>
        <el-empty v-else-if="filteredShopList.length === 0" description="购物车竟然是空的，去挑选一些航海装备吧！"></el-empty>

      </div>

      <div class="cart-right">
        <div class="checkout-panel">
          <h3 class="panel-title">结算明细</h3>

          <div class="checkout-empty" v-if="selectedCount === 0">
            <i class="el-icon-shopping-cart-2 empty-icon"></i>
            <p>选择商品查看实际支付价格</p>
          </div>

          <div class="checkout-details" v-else>
            <div class="summary-row">
              <span>已选商品</span>
              <span>{{ selectedCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品总价</span>
              <span>¥ {{ totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-row discount">
              <span>航海津贴立减</span>
              <span>- ¥ 0.00</span>
            </div>
          </div>

          <div class="checkout-footer">
            <div class="total-box">
              <span>合计:</span>
              <span class="total-price"><small>¥</small>{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button
                class="checkout-btn"
                :disabled="selectedCount === 0"
                @click="handleCheckout">
              结 算<span v-if="selectedCount > 0"> ({{ selectedCount }})</span>
            </el-button>
          </div>
        </div>
      </div>

    </div>
    </div>
  </div>
</template>

<script>
import TopNav from "@/components/TopNav.vue";
import request from '@/utils/request'

export default {
  name: 'UserCart',
  components: {
    TopNav
  },
  data() {
    return {
      isAllSelected: false,
      searchKeyword: '',
      filterStatus: '',
      loading: false,
      deleteLoading: false,

      userId: 1,
      shopList: []
    }
  },
  computed: {
    // 过滤后的店铺列表 (用于支持搜索和下拉筛选)
    filteredShopList() {
      return this.shopList.map(shop => {
        return {
          ...shop,
          items: shop.items.filter(item => {
            const matchKey = item.title.includes(this.searchKeyword)
            const matchStatus = this.filterStatus === '' || item.status === this.filterStatus
            return matchKey && matchStatus
          })
        }
      }).filter(shop => {
        return shop.items.length > 0
      })
    },
    // 所有正常商品总数
    totalItemsCount() {
      let count = 0
      this.shopList.forEach(shop => { count += shop.items.length })
      return count
    },
    // 已勾选的商品总数
    selectedCount() {
      let count = 0
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.checked && item.status === 'normal') count += item.quantity
        })
      })
      return count
    },
    // 已勾选的商品总价
    totalPrice() {
      let total = 0
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.checked && item.status === 'normal') {
            total += parseFloat(item.price) * item.quantity
          }
        })
      })
      return total
    }
  },
  created() {
    this.fetchCartList()
  },
  methods: {
    async fetchCartList() {
      this.loading = true
      try {
        const res = await request.get('/api/cart/list', { params: { userId: this.userId } })
        let cartData = []

        if (Array.isArray(res)) {
          cartData = res
        } else if (res && Array.isArray(res.records)) {
          cartData = res.records
        } else if (res && res.data && Array.isArray(res.data.records)) {
          cartData = res.data.records
        } else if (res && Array.isArray(res.data)) {
          cartData = res.data
        }

        const mappedItems = cartData.map(item => {
          return {
            cartId: item.cartId,
            bookId: item.bookId,
            title: item.bookName,
            image: item.coverUrl,
            skuName: item.author ? `作者：${item.author}` : '标准版',
            price: item.price,
            quantity: item.quantity,
            stock: item.stock,
            status: item.status === 1 ? 'normal' : 'invalid',
            tags: ['官方正品', '极速发货'],
            checked: false
          }
        })

        if (mappedItems.length > 0) {
          this.shopList = [{
            shopId: 1,
            shopName: '航海时代官方直营店',
            checked: false,
            items: mappedItems
          }]
        } else {
          this.shopList = []
        }

        this.checkAllStatus()
      } catch (error) {
        console.error('加载购物车报错: ', error)
        this.$message.error('获取购物车数据失败，请检查网络')
      } finally {
        this.loading = false
      }
    },
    async handleQuantityChange(val, item) {
      try {
        await request.put('/api/cart/update', {
          userId: this.userId,
          cartId: item.cartId,
          quantity: val
        })
      } catch (error) {
        this.$message.error(error.message || '更新数量失败，可能是库存不足')
        this.fetchCartList()
      }
    },
    // 勾选全选
    handleSelectAll(val) {
      this.shopList.forEach(shop => {
        shop.checked = val
        shop.items.forEach(item => {
          if (item.status === 'normal') item.checked = val
        })
      })
    },
    // 勾选店铺
    handleShopSelect(shop) {
      shop.items.forEach(item => {
        if (item.status === 'normal') item.checked = shop.checked
      })
      this.checkAllStatus()
    },
    // 勾选单品
    handleItemSelect() {
      this.shopList.forEach(shop => {
        // 判断该店铺下所有正常商品是否都选中了
        const allNormalItems = shop.items.filter(i => i.status === 'normal')
        if (allNormalItems.length > 0) {
          shop.checked = allNormalItems.every(i => i.checked)
        }
      })
      this.checkAllStatus()
    },
    // 检查并更新底部的全选框状态
    checkAllStatus() {
      let allChecked = true
      let hasNormalItem = false
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.status === 'normal') {
            hasNormalItem = true
            if (!item.checked) allChecked = false
          }
        })
      })
      this.isAllSelected = hasNormalItem && allChecked
    },
    // 单个移入收藏
    async handleMoveFav(shopIndex, itemIndex) {
      const item = this.shopList[shopIndex].items[itemIndex]
      try {
        await request.post(`/api/front/favorites/add/${item.bookId}`)
        this.$message.success('成功移入收藏夹！')
        this.handleDelete(shopIndex, itemIndex, true)
      } catch (error) {
        this.$message.error('移入收藏失败，请稍后重试')
      }
    },
    // 单个删除
    async handleDelete(shopIndex, itemIndex, skipConfirm = false) {
      const item = this.shopList[shopIndex].items[itemIndex]
      const performDelete = async () => {
        try {
          await request.delete('/api/cart/delete', {
            data: {
              userId: this.userId,
              cartIds: [item.cartId]
            }
          })
          if (!skipConfirm) this.$message.success('删除成功')
          this.fetchCartList()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }

      if (skipConfirm) {
        performDelete()
      } else {
        this.$confirm('确认将该商品从购物车中删除吗？', '提示', { type: 'warning' }).then(performDelete).catch(() => {})
      }
    },
    // 批量移入收藏
    async handleBatchMoveFav() {
      if (this.selectedCount === 0) return this.$message.warning('请先选择商品')

      const selectedBookIds = []
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.checked) selectedBookIds.push(item.bookId)
        })
      })

      this.deleteLoading = true
      try {
        const promises = selectedBookIds.map(bookId => request.post(`/api/front/favorites/add/${bookId}`))
        await Promise.all(promises)
        this.$message.success('已将选中商品移入收藏夹')
        await this.removeCheckedItemsFromBackend()
      } catch (error) {
        this.$message.error('部分或全部商品移入收藏失败')
      } finally {
        this.deleteLoading = false
      }
    },
    // 批量删除
    handleBatchDelete() {
      if (this.selectedCount === 0) return this.$message.warning('请先选择商品')
      this.$confirm('确认删除选中的商品吗？', '提示', { type: 'warning' }).then(() => {
        this.removeCheckedItemsFromBackend()
      }).catch(() => {})
    },
    async removeCheckedItemsFromBackend() {
      const cartIdsToDelete = []
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.checked) cartIdsToDelete.push(item.cartId)
        })
      })

      this.deleteLoading = true
      try {
        await request.delete('/api/cart/delete', {
          data: {
            userId: this.userId,
            cartIds: cartIdsToDelete
          }
        })
        this.$message.success('操作成功')
        this.isAllSelected = false
        this.fetchCartList()
      } catch (error) {
        this.$message.error('操作失败')
      } finally {
        this.deleteLoading = false
      }
    },
    // 结算
    handleCheckout() {
      const selectedItems = []
      this.shopList.forEach(shop => {
        shop.items.forEach(item => {
          if (item.checked && item.status === 'normal') {
            selectedItems.push(item)
          }
        })
      })

      sessionStorage.setItem('checkoutItems', JSON.stringify(selectedItems))
      this.$message.success(`准备结算！需支付 ¥${this.totalPrice.toFixed(2)}`)
      this.$router.push('/checkout')
    }
  }
}
</script>

<style scoped>
.cart-page {
  background-color: #f4f4f4;
  min-height: 100vh;
  padding: 30px 0 60px 0;
}
.cart-container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

/* ================== 左侧列表区 ================== */
.cart-left {
  width: 860px;
}

/* 顶部 Tabs */
.cart-tabs {
  display: flex;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
}
.tab-item {
  margin-right: 30px;
  cursor: pointer;
  color: #333;
}
.tab-item.active {
  color: #ff5000;
  border-bottom: 2px solid #ff5000;
  padding-bottom: 5px;
}
.tab-item .count {
  font-size: 14px;
  font-weight: normal;
  color: #ff5000;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.action-left .el-checkbox {
  margin-right: 20px;
}
.text-btn {
  color: #333;
  margin-right: 10px;
}
.text-btn:hover {
  color: #ff5000;
}
.action-right {
  display: flex;
}

/* 促销横幅 */
.promo-banner {
  background-color: #fff0eb;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 13px;
  color: #ff5000;
}
.time-box {
  background-color: #ff5000;
  color: #fff;
  padding: 2px 4px;
  border-radius: 3px;
  margin: 0 2px;
}

/* 店铺卡片 */
.shop-group {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 20px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}
.shop-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 14px;
}
.shop-icon {
  margin: 0 10px;
  color: #ff5000;
}

/* 商品项 */
.cart-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}
.cart-item.is-invalid {
  opacity: 0.6;
  background-color: #fdfdfd;
}
.item-checkbox {
  width: 40px;
  padding-top: 10px;
}
.item-img {
  width: 90px;
  height: 90px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-right: 15px;
  position: relative;
  overflow: hidden;
}
.item-img .el-image {
  width: 100%;
  height: 100%;
}
.invalid-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(0,0,0,0.6);
  color: #fff;
  text-align: center;
  font-size: 12px;
  padding: 2px 0;
}

.item-info {
  width: 220px;
  margin-right: 20px;
}
.item-title {
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-tags .p-tag {
  font-size: 12px;
  color: #ff5000;
  margin-right: 10px;
}

.item-sku {
  width: 150px;
  font-size: 12px;
  color: #999;
  margin-right: 20px;
}

.item-price {
  width: 80px;
  font-weight: bold;
  font-size: 14px;
  color: #333;
}
.price-symbol {
  font-size: 12px;
  font-weight: normal;
}

.item-quantity {
  width: 120px;
}
.invalid-text {
  font-size: 14px;
  color: #999;
  padding-left: 15px;
}

.item-action {
  width: 80px;
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.item-action a {
  font-size: 13px;
  color: #666;
  text-decoration: none;
}
.item-action a:hover {
  color: #ff5000;
}

/* ================== 右侧吸顶结算区 ================== */
.cart-right {
  width: 320px;
}
.checkout-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  position: sticky;
  top: 20px; /* 🌟 核心：吸顶特效 */
}
.panel-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  color: #333;
}

/* 空状态 */
.checkout-empty {
  text-align: center;
  padding: 30px 0;
  color: #ff5000;
  font-weight: bold;
  font-size: 14px;
}
.empty-icon {
  font-size: 50px;
  color: #ffd8cc;
  margin-bottom: 10px;
}

/* 详情结算态 */
.checkout-details {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
}
.summary-row.discount {
  color: #ff5000;
}

/* 底部计价 */
.checkout-footer {
  text-align: right;
}
.total-box {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 20px;
}
.total-box span:first-child {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}
.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff5000;
}
.checkout-btn {
  width: 100%;
  background: linear-gradient(90deg, #ff9000, #ff5000);
  border: none;
  color: #fff;
  height: 48px;
  font-size: 18px;
  border-radius: 24px;
  font-weight: bold;
}
.checkout-btn.is-disabled {
  background: #f5f5f5;
  color: #c0c4cc;
}
</style>
