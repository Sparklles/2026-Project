<template>
  <div class="product-detail-wrapper">
    <top-nav></top-nav>
    <div class="product-detail-page" v-if="product">
      <div class="breadcrumb-wrap">
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>航海图书中心</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-container">
      <div class="left-panel">
        <div class="gallery-section">
          <div class="main-image">
            <el-image :src="currentImage || (product.images && product.images[0])" fit="contain" :preview-src-list="product.images"></el-image>
          </div>
          <div class="thumbnail-list">
            <div
                v-for="(img, index) in product.images"
                :key="index"
                class="thumb-item"
                :class="{ 'active': currentImage === img }"
                @click="currentImage = img"
                @mouseenter="currentImage = img"> <el-image :src="img" fit="cover"></el-image>
            </div>
          </div>
        </div>

        <div class="content-tabs-section">
          <el-tabs v-model="activeTab" class="custom-tabs">
            <el-tab-pane label="图书简介" name="details">
              <div class="rich-content">
                <div class="desc-text">
                  <h3>书籍概览</h3>
                  <p style="white-space: pre-wrap; line-height: 1.8; color: #666;">{{ product.subtitle }}</p>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="出版信息" name="params">
              <div class="params-grid">
                <div class="param-item"><span>书籍ID:</span> {{ product.id }}</div>
                <div class="param-item"><span>分类标签:</span> {{ product.tags ? product.tags.join('、') : '暂无' }}</div>
                <div class="param-item"><span>库存状态:</span> {{ product.stock > 0 ? '现货' : '缺货' }}</div>
              </div>
            </el-tab-pane>

            <el-tab-pane :label="`用户评价 (${reviewList.length})`" name="reviews">

              <div class="review-header-action">
                <el-radio-group v-model="filterRating" size="small" @change="fetchReviews">
                  <el-radio-button :label="0">全部</el-radio-button>
                  <el-radio-button :label="5">5星 (极好)</el-radio-button>
                  <el-radio-button :label="4">4星 (满意)</el-radio-button>
                  <el-radio-button :label="3">3星 (一般)</el-radio-button>
                  <el-radio-button :label="2">2星以下 (差评)</el-radio-button>
                </el-radio-group>
                <el-button type="primary" size="small" icon="el-icon-edit" @click="showReviewDialog = true">我要评价</el-button>
              </div>

              <div class="review-list" v-loading="reviewLoading">
                <div class="review-item" v-for="review in reviewList" :key="review.id">

                  <div class="reviewer-info">
                    <el-avatar :size="32" :src="review.avatar"></el-avatar>
                    <span class="username">{{ review.username }}</span>
                    <el-rate v-model="review.rating" disabled show-score text-color="#ff9900"></el-rate>
                  </div>

                  <p class="review-content">{{ review.content }}</p>

                  <div class="official-reply" v-if="review.adminReply">
                    <div class="reply-arrow"></div> <span class="reply-label">商家官方回复：</span>
                    <span class="reply-text">{{ review.adminReply }}</span>
                  </div>

                  <div class="review-meta">{{ review.date }} | 官方认证购买</div>
                </div>
                <el-empty v-if="reviewList.length === 0" description="该星级下暂无评价哦"></el-empty>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <div class="right-panel">
        <div class="sticky-buy-box">
          <h1 class="product-title">{{ product.title }}</h1>
          <p class="product-subtitle">{{ product.subtitle ? product.subtitle.substring(0, 50) + '...' : '' }}</p>

          <div class="price-banner">
            <div class="price-left">
              <span class="price-label">官方售价</span>
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.minPrice }}</span>
            </div>
            <div class="price-right">
              <div class="tag-box">正品保障</div>
              <div>航海直邮</div>
            </div>
          </div>

          <div class="service-meta">
            <div class="meta-row">
              <span class="label">配送:</span>
              <span class="value">官方仓配 | 预计 48小时 内发货</span>
            </div>
            <div class="meta-row guarantee">
              <span class="label">保障:</span>
              <span class="value">
                <i class="el-icon-circle-check"></i> 7天退换
                <i class="el-icon-circle-check"></i> 隐私保护
              </span>
            </div>
          </div>

          <div class="sku-selection">
            <div class="sku-label">版本</div>
            <div class="sku-list">
              <div class="sku-item active">标准全彩版</div>
            </div>
          </div>

          <div class="quantity-selection">
            <div class="sku-label">数量</div>
            <el-input-number
                v-model="buyCount"
                :min="1"
                :max="product.stock"
                size="medium">
            </el-input-number>
            <span class="stock-info">库存 {{ product.stock }} 件</span>
          </div>

          <div class="action-buttons">
            <el-button class="btn-cart" @click="handleAddToCart" :disabled="product.stock <= 0" :loading="addCartLoading">加入购物车</el-button>
            <el-button class="btn-buy" @click="handleBuyNow" :disabled="product.stock <= 0">立即购买</el-button>
            <div class="collect-btn" @click="toggleCollect">
              <i :class="isCollected ? 'el-icon-star-on collected' : 'el-icon-star-off'"></i>
              <span>{{ isCollected ? '已收藏' : '收藏' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="编写商品评价" :visible.sync="showReviewDialog" width="500px" :close-on-click-modal="false">
      <el-form :model="reviewForm" ref="reviewForm" label-width="80px">
        <el-form-item label="综合评分">
          <el-rate v-model="reviewForm.rating" show-text style="margin-top: 10px;"></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
              type="textarea"
              v-model="reviewForm.content"
              :rows="4"
              placeholder="这本书对您的航海考证或工作有帮助吗？分享您的感受吧！"
              maxlength="200"
              show-word-limit>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showReviewDialog = false">取 消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitLoading">发 布</el-button>
      </div>
    </el-dialog>

    <div class="also-bought-section" v-if="alsoBoughtList.length > 0">
      <div class="also-bought-header">
        <h3><i class="el-icon-goods" style="color: #ff5000;"></i> 看了又看 · 关联推荐</h3>
      </div>
      <el-row :gutter="16" class="also-bought-grid">
        <el-col :span="4" v-for="book in alsoBoughtList" :key="book.id">
          <el-card class="also-card" shadow="hover" :body-style="{ padding: '0px' }" @click.native="goToAlsoBook(book.id)">
            <div class="also-img-wrap">
              <el-image :src="book.coverImageUrl" fit="cover" class="also-img">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
            </div>
            <div class="also-info">
              <div class="also-title">{{ book.title }}</div>
              <div class="also-author">{{ book.author }}</div>
              <div class="also-tags">
                <span class="difficulty-tag" v-if="book.difficultyTag != null" :class="'level-' + book.difficultyTag">{{ getDifficultyText(book.difficultyTag) }}</span>
                <el-tag v-for="tag in book.tags" :key="tag" size="mini" effect="plain" class="also-mini-tag">{{ tag }}</el-tag>
              </div>
              <div class="also-bottom">
                <span class="also-price"><i>¥</i>{{ book.price }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</div>
</template>

<script>
// 整合了第一份代码的 API 封装引入 和 第二份代码需要的 request 引入
import request from "@/utils/request";
import { getBookDetail, getAlsoBought } from '@/api/front/book'
import { getBookReviews} from '@/api/front/review'
import TopNav from "@/components/TopNav.vue";

export default {
  name: 'ProductDetail',
  components: {
    TopNav
  },
  data() {
    return {
      userId: 1,
      productId: null,
      activeTab: 'details',
      currentImage: '',
      isCollected: false,
      buyCount: 1,
      selectedSku: null,
      product: null,
      addCartLoading: false,
      alsoBoughtList: [],

      // 评价相关数据
      reviewList: [],
      filterRating: 0,
      reviewLoading: false,
      showReviewDialog: false,
      submitLoading: false,
      reviewForm: {
        rating: 5,
        content: ''
      }
    }
  },
  created() {
    this.productId = this.$route.params.id;
    this.fetchProductData(this.productId);
    this.fetchReviews();
    this.fetchAlsoBought();

    // 整合新增：页面加载时，查询该商品是否已被收藏
    this.checkFavoriteStatus();

    // 整合新增：检查路由地址中是否带有自动呼出评价窗口的指令
    if (this.$route.query.action === 'review') {
      this.activeTab = 'reviews';
      this.showReviewDialog = true;
    }
  },
  methods: {
    // 整合新增：调用后端接口，检查收藏状态
    async checkFavoriteStatus() {
      try {
        const res = await request.get(`/api/front/favorites/check/${this.productId}`);
        this.isCollected = res.data === true || res === true;
      } catch (error) {
        console.warn('获取收藏状态失败', error);
      }
    },
    // 保持使用封装好的接口调用
    async fetchProductData(id) {
      try {
        const res = await getBookDetail(id);
        this.product = res;
        if (this.product.images && this.product.images.length > 0) {
          this.currentImage = this.product.images[0];
        }
      } catch (error) {
        if (error.responseCode === 401) {
          this.$router.replace('/login')
          return
        }
        this.$message.error('获取商品详情失败');
      }
    },
    // 保持使用封装好的接口调用
    async fetchReviews() {
      this.reviewLoading = true;
      try {
        const res = await getBookReviews(this.productId, {
          rating: this.filterRating
        });
        this.reviewList = Array.isArray(res) ? res : (res.records || []);
      } catch (error) {
        console.error('评价请求失败:', error);
        this.$message.error('获取评价失败');
      } finally {
        this.reviewLoading = false;
      }
    },
    async fetchAlsoBought() {
      try {
        const res = await getAlsoBought(this.productId);
        this.alsoBoughtList = Array.isArray(res) ? res : [];
      } catch (error) {
        if (error.responseCode === 401) {
          return
        }
        console.error('获取关联推荐失败', error);
      }
    },
    goToAlsoBook(bookId) {
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
    },    // 保持使用封装好的接口调用
    submitReview() {
      if (this.reviewForm.rating < 1) {
        return this.$message.warning('请至少给商品打1颗星哦');
      }
      if (!this.reviewForm.content.trim()) {
        return this.$message.warning('评价内容不能为空');
      }

      this.submitLoading = true;
      const payload = {
        bookId: this.productId,
        rating: this.reviewForm.rating,
        content: this.reviewForm.content
      };

      // 🌟 核心修复点：获取当前用户 ID（使用字符串防止 JS 精度丢失）
      // 如果你的登录系统完善了，这里应该是从 localStorage 取真实 ID
      const currentUserId = this.userId || '2049811061216985090';

      // 🌟 核心修复点：绕过封装的 api，直接使用 request 发送，并在 Headers 中强行塞入 userId
      request.post('/api/front/reviews', payload, {
        headers: {
          'userId': currentUserId  // 👈 满足后端 @RequestHeader("userId") 的强制要求！
        }
      }).then(() => {
        this.$message.success('评价发布成功！');
        this.showReviewDialog = false;

        // 清空表单
        this.reviewForm.content = '';
        this.reviewForm.rating = 5;
        this.filterRating = 0;

        // 重新拉取最新的评价列表
        this.fetchReviews();
    this.fetchAlsoBought();
      }).catch(err => {
        this.$message.error('评价发布失败: ' + (err.message || '未知错误'));
      }).finally(() => {
        this.submitLoading = false;
      });
    },

    async handleAddToCart() {
      this.addCartLoading = true;
      try {
        await request.post('/api/cart/add', {
          userId: this.userId,
          bookId: this.productId,
          quantity: this.buyCount
        });

        this.$confirm(`成功将 ${this.buyCount} 件【${this.product.title}】加入购物车！是否立即去购物车查看？`, '添加成功', {
          confirmButtonText: '去购物车结算',
          cancelButtonText: '继续逛逛',
          type: 'success'
        }).then(() => {
          this.$router.push('/cart');
        }).catch(() => {});
      } catch (error) {
        this.$message.error(error.message || '加入购物车失败，请稍后重试');
      } finally {
        this.addCartLoading = false;
      }
    },
    handleBuyNow() {
      this.$message.success('正在跳转订单结算页...')
      this.$router.push({
        path: '/checkout',
        query: {
          productId: this.productId,
          count: this.buyCount
        }
      });
    },
    // 整合新增：使用真实后端接口处理收藏/取消收藏
    async toggleCollect() {
      try {
        if (!this.isCollected) {
          await request.post(`/api/front/favorites/add/${this.productId}`);
          this.isCollected = true;
          this.$message.success('收藏成功，可在【我的收藏】中查看');
        } else {
          await request.delete('/api/front/favorites/remove', {
            data: [this.productId]
          });
          this.isCollected = false;
          this.$message.info('已取消收藏');
        }
      } catch (error) {
        this.$message.error('操作失败，请稍后重试');
        console.error("收藏接口请求异常:", error);
      }
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.product-detail-page { background-color: #f4f4f4; min-height: 100vh; padding: 20px 0 60px 0; }
.breadcrumb-wrap { width: 1200px; margin: 0 auto 15px auto; font-size: 14px; }
.detail-container { width: 1200px; margin: 0 auto; display: flex; align-items: flex-start; gap: 20px; }

/* 左侧面板 */
.left-panel { width: 750px; background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.gallery-section { display: flex; gap: 20px; margin-bottom: 40px; }
.main-image { width: 400px; height: 400px; border-radius: 8px; overflow: hidden; border: 1px solid #eee; }
.main-image .el-image { width: 100%; height: 100%; }
.thumbnail-list { display: flex; flex-direction: column; gap: 10px; }
.thumb-item { width: 75px; height: 75px; border-radius: 6px; border: 2px solid transparent; cursor: pointer; overflow: hidden; }
.thumb-item.active { border-color: #ff5000; }
.thumb-item .el-image { width: 100%; height: 100%; }

.content-tabs-section { margin-top: 20px; }
.custom-tabs ::v-deep .el-tabs__item { font-size: 16px; padding: 0 30px; }
.custom-tabs ::v-deep .el-tabs__item.is-active { color: #ff5000; font-weight: bold; }
.custom-tabs ::v-deep .el-tabs__active-bar { background-color: #ff5000; }

.params-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; padding: 20px 10px; background: #fafafa; border-radius: 8px; font-size: 13px; color: #666; }
.param-item span { color: #999; margin-right: 5px; }

/* 🌟 评价区样式 */
.review-header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}
.review-list { min-height: 150px; }
.review-item { border-bottom: 1px dashed #f0f0f0; padding: 20px 0; }
.review-item:last-child { border-bottom: none; }
.reviewer-info { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.username { font-size: 14px; color: #333; font-weight: bold; }
.review-content { font-size: 14px; line-height: 1.6; color: #333; margin: 0 0 10px 0; white-space: pre-wrap; }
.review-meta { font-size: 12px; color: #999; margin-top: 15px;}

/* 🌟 新增：官方回复专属样式 (仿淘宝气泡样式) */
.official-reply {
  position: relative;
  background-color: #f6f6f6;
  padding: 12px 15px;
  border-radius: 6px;
  margin: 15px 0 15px 0;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
.reply-arrow {
  position: absolute;
  top: -8px;
  left: 20px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid #f6f6f6; /* 必须和背景色一致 */
}
.reply-label {
  color: #ff5000;
  font-weight: bold;
  margin-right: 5px;
}
.reply-text {
  word-wrap: break-word;
}

/* 右侧交易区 */
.right-panel { width: 430px; }
.sticky-buy-box { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); position: sticky; top: 20px; }
.product-title { font-size: 20px; font-weight: bold; color: #333; margin: 0 0 10px 0; line-height: 1.4; }
.product-subtitle { font-size: 14px; color: #ff5000; margin: 0 0 20px 0; line-height: 1.5; }

.price-banner { background: linear-gradient(90deg, #ff5000 0%, #ff8c00 100%); border-radius: 8px; padding: 15px 20px; color: #fff; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.price-left { display: flex; align-items: baseline; }
.price-label { font-size: 13px; margin-right: 8px; opacity: 0.9; }
.price-symbol { font-size: 16px; font-weight: bold; }
.price-value { font-size: 32px; font-weight: bold; margin-right: 10px; }
.price-right { text-align: right; font-size: 12px; }
.tag-box { background: rgba(255,255,255,0.2); padding: 2px 6px; border-radius: 4px; margin-bottom: 4px; display: inline-block; }

.service-meta { font-size: 13px; color: #666; margin-bottom: 25px; background: #fafafa; padding: 15px; border-radius: 8px; }
.meta-row { margin-bottom: 10px; }
.meta-row:last-child { margin-bottom: 0; }
.meta-row .label { color: #999; margin-right: 15px; display: inline-block; width: 40px; }
.guarantee i { color: #13ce66; margin-left: 10px; }
.guarantee i:first-child { margin-left: 0; }

.sku-label { font-size: 13px; color: #999; margin-bottom: 10px; }
.sku-selection { margin-bottom: 25px; }
.sku-list { display: flex; flex-wrap: wrap; gap: 10px; }
.sku-item { padding: 8px 16px; border: 1px solid #ff5000; border-radius: 4px; font-size: 13px; color: #ff5000; background: #fff5f0; font-weight: bold; }

.quantity-selection { display: flex; align-items: center; margin-bottom: 30px; }
.quantity-selection .sku-label { margin-bottom: 0; margin-right: 15px; }
.stock-info { margin-left: 15px; font-size: 12px; color: #999; }

.action-buttons { display: flex; align-items: center; gap: 15px; }
.btn-cart { flex: 1; background-color: #ff9000; color: #fff; border: none; height: 48px; font-size: 16px; font-weight: bold; border-radius: 24px; }
.btn-cart:hover { background-color: #ff7b00; color: #fff; }
.btn-cart:disabled { background-color: #ccc; cursor: not-allowed; }
.btn-buy { flex: 1; background-color: #ff5000; color: #fff; border: none; height: 48px; font-size: 16px; font-weight: bold; border-radius: 24px; }
.btn-buy:hover { background-color: #e64800; color: #fff; }
.btn-buy:disabled { background-color: #ccc; cursor: not-allowed; }
.collect-btn { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 50px; cursor: pointer; color: #666; }
.collect-btn i { font-size: 20px; margin-bottom: 4px; }
.collect-btn i.collected { color: #ff5000; }
.collect-btn span { font-size: 12px; }

.also-bought-section {
  width: 1200px;
  margin: 40px auto 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.also-bought-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.also-bought-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}
.also-card {
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.also-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1) !important;
}
.also-img-wrap {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background-color: #f9f9f9;
}
.also-img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}
.also-card:hover .also-img {
  transform: scale(1.05);
}
.also-info {
  padding: 10px;
}
.also-title {
  font-size: 13px;
  color: #333;
  line-height: 18px;
  height: 36px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 4px;
}
.also-author {
  font-size: 11px;
  color: #888;
  margin-bottom: 4px;
}
.also-tags {
  height: 22px;
  overflow: hidden;
  margin-bottom: 6px;
}
.also-mini-tag {
  color: #ff5000;
  border-color: #ffcccc;
  background-color: #fff5f0;
  margin-right: 3px;
  border-radius: 4px;
}
.difficulty-tag {
  display: inline-block;
  font-size: 11px;
  padding: 0 4px;
  border-radius: 2px;
  color: #fff;
  line-height: 16px;
  margin-right: 3px;
}
.level-0 { background-color: #909399; }
.level-1 { background-color: #67c23a; }
.level-2 { background-color: #e6a23c; }
.level-3 { background-color: #f56c6c; }
.also-bottom {
  display: flex;
  justify-content: space-between;
  align-items: bottom;
}
.also-price {
  color: #ff5000;
  font-size: 16px;
  font-weight: bold;
}
.also-price i {
  font-size: 11px;
  font-style: normal;
  margin-right: 1px;
}
</style>

