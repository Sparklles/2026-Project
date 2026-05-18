<template>
  <div class="review-manage-page">
    <div class="card-panel">

          <div class="review-header">
            <el-tabs v-model="activeTab" class="custom-tabs">
              <el-tab-pane label="来自卖家的评价" name="received"></el-tab-pane>
              <el-tab-pane label="给他人的评价" name="given"></el-tab-pane>
            </el-tabs>
          </div>

          <div class="filter-toolbar">
            <el-select v-model="filterType" size="small" style="width: 120px; margin-right: 15px;">
              <el-option label="评价 (全部)" value="all"></el-option>
              <el-option label="好评" value="good"></el-option>
              <el-option label="中评" value="neutral"></el-option>
              <el-option label="差评" value="bad"></el-option>
            </el-select>

            <el-select v-model="filterContent" size="small" style="width: 120px;">
              <el-option label="评论 (全部)" value="all"></el-option>
              <el-option label="有评论内容" value="hasContent"></el-option>
              <el-option label="无评论内容" value="noContent"></el-option>
            </el-select>
          </div>

          <div class="list-header">
            <div class="col-eval">评价内容</div>
            <div class="col-user">{{ activeTab === 'received' ? '评价人' : '被评价人' }}</div>
            <div class="col-product">宝贝信息</div>
            <div class="col-action">操作 <el-tooltip content="点击可进行回复或修改" placement="top"><i class="el-icon-question"></i></el-tooltip></div>
          </div>

          <div class="review-list" v-loading="loading">
            <el-empty v-if="pagedReviews.length === 0" description="暂无符合条件的评价记录"></el-empty>

            <div class="review-row" v-for="item in pagedReviews" :key="item.id">

              <div class="col-eval">
                <div class="eval-icon-wrap">
                  <i class="el-icon-medal eval-icon good" v-if="item.ratingType === 'good'" title="好评"></i>
                  <i class="el-icon-remove eval-icon neutral" v-if="item.ratingType === 'neutral'" title="中评"></i>
                  <i class="el-icon-warning eval-icon bad" v-if="item.ratingType === 'bad'" title="差评"></i>
                </div>
                <div class="eval-text-wrap">
                  <p class="eval-content" :class="{ 'system-text': item.isSystem }">
                    <span v-if="item.isSystem">[系统默认] </span>
                    {{ item.content || '此用户没有填写评价。' }}
                  </p>
                  <p class="eval-date">[{{ item.date }}]</p>

                  <div class="admin-reply" v-if="item.adminReply">
                    <span class="reply-label">[解释]</span> {{ item.adminReply }}
                  </div>
                </div>
              </div>

              <div class="col-user">
                <p class="user-role">{{ activeTab === 'received' ? '卖家 :' : '商家 :' }}</p>
                <a href="javascript:;" class="shop-link">
                  {{ item.targetName }}
                  <i class="el-icon-s-custom shop-icon" v-if="activeTab === 'received'"></i>
                  <el-tag size="mini" type="danger" effect="dark" v-else style="margin-left: 5px; transform: scale(0.8);">商城</el-tag>
                </a>
              </div>

              <div class="col-product">
                <a href="javascript:;" class="product-link" v-if="item.product" @click="goToProduct(item.product.id)">{{ item.product.title }}</a>
                <p class="product-price" v-if="item.product"><span class="symbol">¥</span>{{ item.product.price }}</p>
              </div>

              <div class="col-action">
                <el-button type="text" size="small" v-if="activeTab === 'received'" @click="handleReply(item)">回复</el-button>
                <el-button type="text" size="small" v-if="activeTab === 'given'" @click="handleEdit(item)">修改/删除</el-button>
                <el-button type="text" size="small" v-if="activeTab === 'given'" @click="handleAppend(item)">追加评论</el-button>
              </div>

            </div>
          </div>

          <div class="pagination-wrap" v-if="reviewTotal > 0">
            <el-pagination
                background
                layout="prev, pager, next"
                :current-page="currentPage"
                :page-size="pageSize"
                :total="reviewTotal"
                @current-change="handlePageChange">
            </el-pagination>
          </div>

        </div>

      <el-dialog title="管理我的评价" :visible.sync="editDialogVisible" width="500px" :close-on-click-modal="false">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="综合评分">
            <el-rate v-model="editForm.rating" show-text style="margin-top: 10px;"></el-rate>
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
                type="textarea"
                v-model="editForm.content"
                :rows="4"
                placeholder="修改您的评价内容..."
                maxlength="200"
                show-word-limit>
            </el-input>
          </el-form-item>
        </el-form>

        <div slot="footer" class="dialog-footer" style="display: flex; justify-content: space-between; align-items: center;">
          <el-button type="danger" plain @click="deleteReview" icon="el-icon-delete">删除评价</el-button>
          <div>
            <el-button @click="editDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitEdit" :loading="editLoading">保存修改</el-button>
          </div>
        </div>
      </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'ReviewManage',
  data() {
    return {
      searchKeyword: '',
      activeTab: 'given', // B2C商城通常打开页面直接看自己给出的评价
      filterType: 'all',
      filterContent: 'all',
      loading: false,

      // 直接接收后端返回的真实数据
      filteredReviews: [],
      currentPage: 1,
      pageSize: 10,
      reviewTotal: 0,
      // 新增：编辑弹窗相关状态
      editDialogVisible: false,
      editLoading: false,
      editForm: {
        id: null,
        rating: 5,
        content: ''
      }
    }
  },
  computed: {
    pagedReviews() {
      if (this.reviewTotal !== this.filteredReviews.length) {
        return this.filteredReviews
      }
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredReviews.slice(start, start + this.pageSize)
    }
  },
  created() {
    // 页面加载即去后端拉取数据
    this.fetchData();
  },
  watch: {
    // 🌟 监听所有过滤条件，一旦改变立刻让后端重新发数据
    filterType() {
      this.currentPage = 1;
      this.fetchData();
    },
    filterContent() {
      this.currentPage = 1;
      this.fetchData();
    },
    activeTab() {
      // 切换 Tab 时重置过滤条件，并拉取新数据
      this.filterType = 'all';
      this.filterContent = 'all';
      this.searchKeyword = '';
      this.currentPage = 1;
      this.fetchData();
    }
  },
  methods: {
    // 🌟 核心：全权委托给后端接口处理查询
    async fetchData() {
      this.loading = true;
      try {
        const payload = {
          tabType: this.activeTab,
          filterType: this.filterType,
          filterContent: this.filterContent,
          keyword: this.searchKeyword,
          current: this.currentPage,
          size: this.pageSize,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        };
        // 调用我们刚刚写的 POST /api/front/user-reviews/list 接口
        const res = await request.post('/api/front/user-reviews/list', payload);
        this.applyReviewPageData(res);
      } catch (error) {
        this.$message.error('获取评价列表失败，请检查网络');
        console.error(error);
      } finally {
        this.loading = false;
      }
    },

    applyReviewPageData(data) {
      const pageData = data && (data.data || data)
      const list = Array.isArray(pageData)
          ? pageData
          : (pageData && (pageData.records || pageData.list || pageData.rows || pageData.items))
      this.filteredReviews = Array.isArray(list) ? list : [];
      this.reviewTotal = Number(pageData && (pageData.total || pageData.totalCount || pageData.count)) || this.filteredReviews.length;
      this.currentPage = Number(pageData && (pageData.current || pageData.pageNum || pageData.page)) || this.currentPage;
      this.pageSize = Number(pageData && (pageData.size || pageData.pageSize)) || this.pageSize;
    },

    handleSearch() {
      this.currentPage = 1;
      this.fetchData(); // 触发搜索接口
    },

    handlePageChange(page) {
      this.currentPage = page;
      this.fetchData();
    },

    goToProduct(id) {
      if (!id) return;
      this.$router.push(`/product/${id}`);
    },
    handleReply(item) {
      if (!item.product || !item.product.id) {
        return this.$message.error('未找到关联的商品信息');
      }

      this.$message.success('正在为您跳转至商品详情页...');

      // 使用 Vue Router 跳转，并携带 action 参数
      this.$router.push({
        path: `/product/${item.product.id}`,
        query: { action: 'review' }
      });
    },
    // 🌟 1. 点击列表中的修改按钮，回显数据并打开弹窗
    handleEdit(item) {
      this.editForm.id = item.id;
      // 由于之前后端返回的是 good/neutral/bad，我们做一个简单的反向兼容。如果后端加入了具体的 rating 字段，可以直接用 item.rating
      this.editForm.rating = item.rating || (item.ratingType === 'good' ? 5 : item.ratingType === 'neutral' ? 3 : 1);

      // 处理系统默认评价
      this.editForm.content = item.isSystem ? '' : item.content;
      this.editDialogVisible = true;
    },

    // 🌟 2. 提交修改后的内容到后端
    async submitEdit() {
      if (this.editForm.rating < 1) {
        return this.$message.warning('请至少给出1颗星的评价哦');
      }
      if (!this.editForm.content.trim()) {
        return this.$message.warning('评价内容不能为空');
      }

      this.editLoading = true;
      try {
        await request.put('/api/front/user-reviews/update', this.editForm);
        this.$message.success('评价修改成功！');
        this.editDialogVisible = false;

        // 重新拉取列表数据，刷新页面状态
        this.fetchData();
      } catch (error) {
        this.$message.error('修改失败：' + (error.message || '系统异常'));
      } finally {
        this.editLoading = false;
      }
    },
    // 🌟 3. 删除评价逻辑
    deleteReview() {
      this.$confirm('确定要彻底删除这条评价吗？删除后将无法恢复。', '删除警告', {
        type: 'warning',
        confirmButtonText: '坚决删除',
        cancelButtonText: '再想想',
        confirmButtonClass: 'el-button--danger' // 按钮标红
      }).then(async () => {
        try {
          await request.delete(`/api/front/user-reviews/remove/${this.editForm.id}`);
          this.$message.success('评价已彻底删除');
          this.editDialogVisible = false;

          // 重新拉取列表数据
          this.fetchData();
        } catch (error) {
          this.$message.error('删除失败');
        }
      }).catch(() => {
        this.$message.info('已取消删除操作');
      });
    },

    handleAppend(item) {
      if (!item.product || !item.product.id) {
        return this.$message.error('未找到关联的商品信息');
      }

      this.$message.success('正在为您跳转至商品详情页...');

      // 使用 Vue Router 跳转，并携带 action 参数
      this.$router.push({
        path: `/product/${item.product.id}`,
        query: { action: 'review' }
      });
    }
  }
}
</script>

<style scoped>
.review-manage-page { padding: 0; }

/* ================== 2. 右侧主内容区 ================== */
.card-panel { background: #fff; border-radius: 12px; padding: 20px 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); min-height: 600px; }

.review-header { margin-bottom: 15px; border-bottom: 2px solid #ff5000; padding-bottom: 0;}
.custom-tabs ::v-deep .el-tabs__item { font-size: 16px; font-weight: bold; height: 45px; line-height: 45px; }
.custom-tabs ::v-deep .el-tabs__item.is-active { color: #ff5000; }
.custom-tabs ::v-deep .el-tabs__active-bar { background-color: #ff5000; height: 3px; }
.custom-tabs ::v-deep .el-tabs__nav-wrap::after { display: none; }

.filter-toolbar { margin-bottom: 20px; display: flex; align-items: center; }

/* 仿表格列表头部 */
.list-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 10px 0;
  font-size: 13px;
  font-weight: bold;
  color: #333;
  text-align: center;
  border: 1px solid #e8e8e8;
  border-bottom: none;
}
.col-eval { width: 45%; text-align: left; padding-left: 60px; }
.col-user { width: 20%; }
.col-product { width: 25%; }
.col-action { width: 10%; }
.list-header .el-icon-question { color: #999; margin-left: 2px; cursor: help; }

/* 评价列表行 */
.review-list { border-top: 1px solid #e8e8e8; }
.review-row {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px dashed #e8e8e8;
  transition: background-color 0.2s;
}
.review-row:hover { background-color: #fafafa; }

/* 第一列：评价内容 */
.col-eval { width: 45%; display: flex; padding-left: 20px; }
.eval-icon-wrap { width: 40px; }
.eval-icon { font-size: 20px; margin-top: 2px; }
.eval-icon.good { color: #ff5000; }
.eval-icon.neutral { color: #fadb14; }
.eval-icon.bad { color: #000000; }

.eval-text-wrap { flex: 1; padding-right: 20px; }
.eval-content { font-size: 13px; color: #333; line-height: 1.6; margin: 0 0 8px 0; word-break: break-all; }
.eval-content.system-text { color: #666; }
.eval-date { font-size: 12px; color: #999; margin: 0; }

.admin-reply { margin-top: 10px; font-size: 12px; color: #cc4a00; background-color: #fff0eb; padding: 6px 10px; border-radius: 4px; }
.reply-label { font-weight: bold; }

/* 第二列：用户/商家信息 */
.col-user { width: 20%; font-size: 12px; color: #333; }
.user-role { margin: 0 0 5px 0; color: #666; }
.shop-link { color: #1890ff; text-decoration: none; display: inline-flex; align-items: center;}
.shop-link:hover { text-decoration: underline; }
.shop-icon { color: #1890ff; margin-left: 5px; }

/* 第三列：商品信息 */
.col-product { width: 25%; font-size: 12px; padding-right: 20px; }
.product-link { color: #1890ff; text-decoration: none; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4; margin-bottom: 5px;}
.product-link:hover { text-decoration: underline; }
.product-price { color: #ff5000; margin: 0; font-weight: bold;}
.symbol { font-weight: normal; margin-right: 2px;}

/* 第四列：操作 */
.col-action { width: 10%; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: flex-start;}
.col-action .el-button { margin-left: 0; padding: 4px 0; }

.pagination-wrap { margin-top: 30px; text-align: right; }
</style>
