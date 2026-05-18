<template>
  <div class="favorites-page">
    <div class="card-panel">

          <div class="fav-header">
            <el-tabs v-model="activeTab" class="fav-tabs" @tab-click="handleTabChange">
              <el-tab-pane label="全部装备" name="all"></el-tab-pane>
              <el-tab-pane label="航海图书" name="books"></el-tab-pane>
              <el-tab-pane label="航海仪器" name="devices"></el-tab-pane>
              <el-tab-pane label="失效宝贝" name="invalid"></el-tab-pane>
            </el-tabs>

            <div class="fav-toolbar">
              <div class="toolbar-left">
                <el-dropdown trigger="click" @command="handleSort">
                  <span class="el-dropdown-link">
                    {{ sortLabel }} <i class="el-icon-arrow-down el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="time">默认排序(收藏时间)</el-dropdown-item>
                    <el-dropdown-item command="priceDesc">价格从高到低</el-dropdown-item>
                    <el-dropdown-item command="priceAsc">价格从低到高</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
                <el-checkbox v-model="onlyShowDiscount" style="margin-left: 20px;">有降价</el-checkbox>
              </div>
              <div class="toolbar-right">
                <el-button size="small" plain v-if="!isBatchMode" @click="isBatchMode = true">批量管理</el-button>
                <div v-else class="batch-actions">
                  <el-checkbox v-model="isAllSelected" @change="handleSelectAll">全选</el-checkbox>
                  <el-button size="small" type="danger" plain @click="handleBatchDelete">取消收藏</el-button>
                  <el-button size="small" @click="isBatchMode = false">完成</el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="fav-grid" v-loading="loading">
            <el-empty v-if="filteredList.length === 0" description="收藏夹空空如也，快去发现深蓝宝藏吧！"></el-empty>

            <div
                class="fav-card"
                v-for="item in filteredList"
                :key="item.id"
                :class="{ 'is-invalid': item.status === 0 }"
                @click="goToDetail(item.id, item.status)">

              <div class="batch-checkbox" v-if="isBatchMode" @click.stop>
                <el-checkbox v-model="item.selected"></el-checkbox>
              </div>

              <div class="img-wrap">
                <el-image :src="item.image" fit="cover" lazy>
                  <div slot="placeholder" class="image-slot"><i class="el-icon-loading"></i></div>
                </el-image>

                <div class="discount-tag drop" v-if="item.priceStatus === 1">比收藏时降 ¥{{ item.priceDiff }}</div>
                <div class="discount-tag rise" v-if="item.priceStatus === -1">比收藏时涨 ¥{{ item.priceDiff }}</div>

                <div class="hover-mask" v-if="!isBatchMode && item.status !== 0">
                  <div class="mask-btn" @click.stop="findSimilar(item)">找相似</div>
                  <div class="mask-btn cancel" @click.stop="removeFav(item.id)">取消收藏</div>
                </div>

                <div class="invalid-mask" v-if="item.status === 0">
                  <div class="invalid-text">宝贝已失效</div>
                  <div class="mask-btn" @click.stop="findSimilar(item)" style="margin-top: 10px; border-color: #fff;">找相似</div>
                </div>
              </div>

              <div class="info-wrap">
                <h3 class="title" :title="item.title">
                  <el-tag size="mini" type="danger" effect="dark" v-if="item.isSelfOperated" class="shop-tag">自营</el-tag>
                  {{ item.title }}
                </h3>

                <div class="meta-info">
                  <span class="fav-count">{{ item.favCount || '0' }}人收藏</span>
                  <span class="status-text" v-if="item.status === 0">宝贝失效了</span>
                </div>

                <div class="price-wrap">
                  <span class="price-symbol">¥</span>
                  <span class="price-val">{{ item.price }}</span>
                  <el-button
                      type="text"
                      class="cart-btn"
                      icon="el-icon-shopping-cart-2"
                      v-if="item.status !== 0"
                      @click.stop="addToCart(item)"></el-button>
                </div>
              </div>

            </div>
          </div>

    </div>
  </div>
</template>

<script>
// 🌟 引入 axios request 工具
import request from '@/utils/request';

export default {
  name: 'UserFavorites',
  data() {
    return {
      userId: 1,
      searchKeyword: '',
      activeTab: 'all',
      sortType: 'time',
      sortLabel: '默认排序(收藏时间)',
      onlyShowDiscount: false,
      isBatchMode: false,
      isAllSelected: false,
      loading: false,

      // 🌟 清空假数据，初始化为空数组
      favList: []
    }
  },
  computed: {
    filteredList() {
      let result = this.favList;

      if (this.activeTab !== 'all') {
        if (this.activeTab === 'invalid') {
          result = result.filter(item => item.status === 0);
        } else {
          result = result.filter(item => item.category === this.activeTab && item.status === 1);
        }
      }

      if (this.onlyShowDiscount) {
        result = result.filter(item => item.priceStatus === 1);
      }

      if (this.searchKeyword) {
        result = result.filter(item => item.title.includes(this.searchKeyword));
      }

      if (this.sortType === 'priceDesc') {
        result.sort((a, b) => parseFloat(b.price) - parseFloat(a.price));
      } else if (this.sortType === 'priceAsc') {
        result.sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
      }

      return result;
    }
  },
  // 🌟 页面创建时调用后端接口
  created() {
    this.fetchList();
  },
  methods: {
    // 🌟 核心：真实调用后端拉取收藏列表
    async fetchList() {
      this.loading = true;
      try {
        const res = await request.get('/api/front/favorites/list');
        // 根据你的 request.js 封装情况取值
        this.favList = res.data || res || [];
      } catch (error) {
        this.$message.error('获取收藏列表失败');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },

    goToDetail(id, status) {
      if (this.isBatchMode) {
        const item = this.favList.find(i => i.id === id);
        if (item) item.selected = !item.selected;
        return;
      }
      if (status === 0) return this.$message.warning('该宝贝已下架或失效，无法查看详情');
      this.$router.push(`/product/${id}`);
    },

    // 🌟 核心：取消单条收藏对接真实后端
    removeFav(id) {
      this.$confirm('确定要取消收藏该宝贝吗？', '提示', { type: 'warning' }).then(async () => {
        try {
          await request.delete('/api/front/favorites/remove', { data: [id] });
          this.favList = this.favList.filter(item => item.id !== id);
          this.$message.success('已取消收藏');
        } catch (e) {
          this.$message.error('取消失败');
        }
      }).catch(() => {});
    },

    // 🌟 核心：批量取消收藏对接真实后端
    handleBatchDelete() {
      const selectedItems = this.favList.filter(item => item.selected);
      if (selectedItems.length === 0) {
        return this.$message.warning('请先选择要取消收藏的宝贝');
      }

      const idsToDelete = selectedItems.map(item => item.id);

      this.$confirm(`确认取消收藏选中的 ${idsToDelete.length} 个宝贝吗？`, '提示', { type: 'warning' }).then(async () => {
        try {
          await request.delete('/api/front/favorites/remove', { data: idsToDelete });
          this.favList = this.favList.filter(item => !item.selected);
          this.isAllSelected = false;
          this.isBatchMode = false;
          this.$message.success('批量取消收藏成功');
        } catch (e) {
          this.$message.error('批量取消失败');
        }
      }).catch(() => {});
    },

    handleSearch() {
      // 本地前端过滤，无需请求后端
    },
    handleTabChange() {
      // 本地前端过滤
    },
    handleSort(command) {
      this.sortType = command;
      const map = {
        'time': '默认排序(收藏时间)',
        'priceDesc': '价格从高到低',
        'priceAsc': '价格从低到高'
      };
      this.sortLabel = map[command];
    },
    findSimilar(item) {
      this.$message.info(`正在为您寻找相似的：${item.title.substring(0, 10)}...`);
    },
    async addToCart(item) {
      try {
        await request.post('/api/cart/add', {
          userId: this.userId,
          bookId: item.id,
          quantity: 1
        });
        this.$message.success(`成功将《${item.title.substring(0, 10)}...》加入购物车`);
      } catch (error) {
        this.$message.error(error.message || '加入购物车失败，请稍后重试');
      }
    },
    handleSelectAll(val) {
      this.filteredList.forEach(item => {
        item.selected = val;
      });
    }
  }
}
</script>

<style scoped>
.favorites-page { padding: 0; }
.card-panel { background: #fff; border-radius: 12px; padding: 20px 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); min-height: 600px; }
.fav-header { margin-bottom: 20px; }
.fav-tabs ::v-deep .el-tabs__item { font-size: 16px; font-weight: bold; }
.fav-tabs ::v-deep .el-tabs__item.is-active { color: #ff5000; }
.fav-tabs ::v-deep .el-tabs__active-bar { background-color: #ff5000; }
.fav-toolbar { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; background-color: #f9f9f9; padding: 10px 15px; border-radius: 6px; }
.el-dropdown-link { cursor: pointer; color: #666; font-size: 13px; }
.el-dropdown-link:hover { color: #ff5000; }
.batch-actions { display: flex; align-items: center; gap: 15px; }
.fav-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 20px; padding-top: 10px; }
.fav-card { position: relative; border: 1px solid transparent; border-radius: 8px; overflow: hidden; transition: border-color 0.3s, box-shadow 0.3s; cursor: pointer; }
.fav-card:hover { border-color: #ff5000; box-shadow: 0 4px 12px rgba(255, 80, 0, 0.15); }
.fav-card.is-invalid { opacity: 0.7; filter: grayscale(100%); }
.batch-checkbox { position: absolute; top: 8px; left: 8px; z-index: 10; background: rgba(255,255,255,0.8); border-radius: 2px; }
.img-wrap { position: relative; width: 100%; aspect-ratio: 1 / 1; overflow: hidden; background-color: #f5f5f5; }
.img-wrap .el-image { width: 100%; height: 100%; transition: transform 0.3s; }
.fav-card:hover .el-image { transform: scale(1.05); }

.discount-tag { position: absolute; bottom: 0; left: 0; width: 100%; font-size: 12px; text-align: center; padding: 4px 0; color: #fff; }
.discount-tag.drop { background: rgba(255, 80, 0, 0.85); }
.discount-tag.rise { background: rgba(0, 0, 0, 0.55); }

.hover-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; flex-direction: column; justify-content: center; align-items: center; opacity: 0; transition: opacity 0.3s; gap: 15px; }
.fav-card:hover .hover-mask { opacity: 1; }
.mask-btn { background: #ff5000; color: #fff; padding: 6px 20px; border-radius: 20px; font-size: 13px; font-weight: bold; }
.mask-btn.cancel { background: rgba(255,255,255,0.2); border: 1px solid #fff; }
.mask-btn:hover { transform: scale(1.05); }
.invalid-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255, 255, 255, 0.7); display: flex; flex-direction: column; justify-content: center; align-items: center; }
.invalid-text { background: #666; color: #fff; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
.info-wrap { padding: 10px; }
.title { font-size: 13px; color: #333; line-height: 1.4; height: 36px; margin: 0 0 8px 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.shop-tag { margin-right: 4px; transform: scale(0.9); transform-origin: left; }
.meta-info { display: flex; justify-content: space-between; font-size: 12px; color: #999; margin-bottom: 8px; }
.status-text { color: #f56c6c; }
.price-wrap { display: flex; align-items: baseline; position: relative; }
.price-symbol { font-size: 12px; color: #ff5000; }
.price-val { font-size: 18px; font-weight: bold; color: #ff5000; }
.cart-btn { position: absolute; right: 0; bottom: -5px; color: #ff5000; font-size: 18px; }
.cart-btn:hover { transform: scale(1.2); }
</style>
