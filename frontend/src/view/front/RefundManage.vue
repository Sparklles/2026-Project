<template>
  <div class="refund-manage-page">
    <div class="card-panel">

      <div class="page-header">
        <div class="page-title">退款管理</div>
      </div>

      <div class="filter-box">
        <el-form :inline="true" :model="filterForm" size="small" class="refund-form">
          <div class="form-row">
            <el-form-item label="退款编号：">
              <el-input
                v-model.trim="filterForm.refundNo"
                placeholder="请输入退款编号"
                clearable
                style="width: 200px;"
                @keyup.enter.native="handleSearch">
              </el-input>
            </el-form-item>

            <el-form-item label="退款类型：">
              <el-select v-model="filterForm.refundType" clearable placeholder="全部" style="width: 140px;">
                <el-option label="仅退款" :value="0"></el-option>
                <el-option label="退货退款" :value="1"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="申请时间：">
              <el-select v-model="timeRangeType" style="width: 140px;" @change="handleTimeRangeChange">
                <el-option label="最近申请" value="recent"></el-option>
                <el-option label="最近三个月" value="three_months"></el-option>
                <el-option label="最近半年" value="half_year"></el-option>
                <el-option label="自定义" value="custom"></el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="从：" v-if="timeRangeType === 'custom'">
              <el-date-picker
                v-model="filterForm.beginTime"
                type="date"
                placeholder="开始日期"
                value-format="yyyy-MM-dd"
                style="width: 160px;">
              </el-date-picker>
            </el-form-item>

            <el-form-item label="到：" v-if="timeRangeType === 'custom'">
              <el-date-picker
                v-model="filterForm.endTime"
                type="date"
                placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 160px;">
              </el-date-picker>
            </el-form-item>

            <el-form-item v-else>
              <span class="time-display">{{ filterForm.beginTime }} 到 {{ filterForm.endTime }}</span>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="退款状态：">
              <el-select v-model="filterForm.refundStatus" clearable placeholder="全部" style="width: 150px;">
                <el-option label="待审核" :value="0"></el-option>
                <el-option label="审核通过" :value="1"></el-option>
                <el-option label="用户已寄回" :value="2"></el-option>
                <el-option label="商家已收货" :value="3"></el-option>
                <el-option label="退款成功" :value="4"></el-option>
                <el-option label="审核拒绝" :value="5"></el-option>
                <el-option label="用户关闭" :value="6"></el-option>
              </el-select>
            </el-form-item>
          </div>

          <div class="submit-action">
            <el-button type="primary" class="tb-submit-btn" @click="handleSearch">搜索</el-button>
          </div>
        </el-form>
      </div>

      <div class="list-container" v-loading="loading">
        <div class="list-header">
          <div class="col-product">宝贝</div>
          <div class="col-amount">退款金额</div>
          <div class="col-time">申请时间</div>
          <div class="col-type">服务类型</div>
          <div class="col-status">退款状态</div>
          <div class="col-action">交易操作</div>
        </div>

        <el-empty v-if="refundList.length === 0" description="没有找到符合条件的退款记录"></el-empty>

        <div class="refund-item" v-for="item in refundList" :key="item.id">
          <div class="item-head">
            <span class="order-no">订单编号：{{ item.orderNo }}</span>
            <span class="refund-no">退款编号：{{ item.refundNo }}</span>
            <span class="shop-name"><i class="el-icon-s-shop"></i> {{ item.shopName }}</span>
            <span class="contact-seller"><i class="el-icon-service"></i> 和我联系</span>
          </div>

          <div class="item-body">
            <div class="col-product">
              <img :src="item.productImage" alt="" class="p-img">
              <div class="p-info">
                <a href="javascript:;" class="p-title" @click="$router.push(`/product/${item.productId}`)">{{ item.productName }}</a>
                <p class="p-sku" v-if="item.sku">{{ item.sku }}</p>
              </div>
            </div>

            <div class="col-amount">
              <span class="price-val">¥{{ item.refundAmount }}</span>
            </div>

            <div class="col-time">
              <span class="time-text">{{ item.applyTime }}</span>
            </div>

            <div class="col-type">
              <span class="type-text" :class="item.serviceType">{{ item.serviceTypeText }}</span>
            </div>

            <div class="col-status">
              <span class="status-badge" :class="item.status">{{ item.statusText }}</span>
            </div>

            <div class="col-action">
              <el-button type="text" size="small" class="action-link" @click="viewDetail(item)">服务详情</el-button>
              <el-button type="text" size="small" class="danger-text" v-if="item.canCancel" @click="cancelRefund(item)">取消退款</el-button>
            </div>
          </div>
        </div>

        <div class="pagination-wrap" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange">
          </el-pagination>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { getRefundList, cancelRefund } from '@/api/front/refund';

export default {
  name: 'RefundManage',
  data() {
    return {
      loading: false,
      timeRangeType: 'recent',

      // 筛选表单
      filterForm: {
        refundNo: '',
        refundType: null,
        beginTime: '',
        endTime: '',
        refundStatus: null
      },

      // 退款列表数据
      refundList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10
    }
  },
  async created() {
    this.initTimeRange();
    this.fetchRefundList();
  },
  methods: {
    // 初始化时间范围（最近3个月）
    initTimeRange() {
      const end = new Date();
      const start = new Date();
      start.setMonth(start.getMonth() - 3);
      this.filterForm.endTime = this.formatDate(end);
      this.filterForm.beginTime = this.formatDate(start);
    },

    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },

    // 处理时间范围变化
    handleTimeRangeChange(val) {
      const end = new Date();
      let start = new Date();

      switch (val) {
        case 'recent':
          start.setDate(start.getDate() - 7);
          break;
        case 'three_months':
          start.setMonth(start.getMonth() - 3);
          break;
        case 'half_year':
          start.setMonth(start.getMonth() - 6);
          break;
        case 'custom':
          this.filterForm.beginTime = '';
          this.filterForm.endTime = '';
          return;
      }

      this.filterForm.endTime = this.formatDate(end);
      this.filterForm.beginTime = this.formatDate(start);
    },

    async fetchRefundList() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize
        };

        if (this.filterForm.refundNo) {
          params.refundNo = this.filterForm.refundNo;
        }
        if (this.filterForm.refundType !== null && this.filterForm.refundType !== '') {
          params.refundType = this.filterForm.refundType;
        }
        if (this.filterForm.refundStatus !== null && this.filterForm.refundStatus !== '') {
          params.refundStatus = this.filterForm.refundStatus;
        }
        if (this.filterForm.beginTime) {
          params.beginTime = this.filterForm.beginTime;
        }
        if (this.filterForm.endTime) {
          params.endTime = this.filterForm.endTime;
        }

        const res = await getRefundList(params);
        const data = res.data || res;
        const records = data.records || data.list || data || [];

        this.refundList = records.map(item => this.mapRefundItem(item));
        this.total = data.total || records.length;
      } catch (error) {
        console.error('获取退款列表失败', error);
        this.$message.error('加载退款列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 映射退款列表项
    mapRefundItem(item) {
      const statusMap = {
        0: { text: '待审核', class: 'pending', canCancel: true },
        1: { text: '审核通过', class: 'approved', canCancel: false },
        2: { text: '用户已寄回', class: 'returned', canCancel: false },
        3: { text: '商家已收货', class: 'received', canCancel: false },
        4: { text: '退款成功', class: 'refunded', canCancel: false },
        5: { text: '审核拒绝', class: 'rejected', canCancel: false },
        6: { text: '用户关闭', class: 'cancelled', canCancel: false }
      };

      const statusInfo = statusMap[item.refundStatus] || { text: '未知', class: 'unknown', canCancel: false };
      const serviceType = item.refundType === 0 ? 'refund_only' : 'return_refund';

      return {
        id: item.id,
        refundNo: item.refundNo,
        orderNo: item.orderNo,
        shopName: '航海时代官方直营店',
        productId: item.bookId,
        productName: item.bookName,
        productImage: item.coverUrl || '/images/default-book.png',
        sku: '标准版',
        refundAmount: item.refundAmount || '0.00',
        applyTime: (item.applyTime || item.createTime || '').replace('T', ' '),
        serviceType: serviceType,
        serviceTypeText: item.refundType === 0 ? '仅退款' : '退货退款',
        status: statusInfo.class,
        statusText: statusInfo.text,
        canCancel: statusInfo.canCancel
      };
    },

    handleSearch() {
      this.currentPage = 1;
      this.fetchRefundList();
    },

    handlePageChange(page) {
      this.currentPage = page;
      this.fetchRefundList();
    },

    viewDetail(item) {
      this.$router.push({
        path: '/user/refund-detail',
        query: { refundNo: item.refundNo }
      });
    },

    async cancelRefund(item) {
      this.$confirm('确定要取消退款申请吗？取消后该订单将恢复正常状态。', '撤销提示', {
        confirmButtonText: '确定撤销',
        cancelButtonText: '暂不撤销',
        type: 'warning'
      }).then(async () => {
        try {
          await cancelRefund(item.id);
          this.$message.success('退款已取消');
          this.fetchRefundList();
        } catch (error) {
          this.$message.error(error.message || '取消失败');
        }
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.refund-manage-page { padding: 0; }

/* ================== 页面头部 ================== */
.page-header {
  border-bottom: 2px solid #ff5000;
  margin-bottom: 20px;
  padding-bottom: 10px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #ff5000;
}

/* ================== 卡片面板 ================== */
.card-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  min-height: 600px;
}

/* ================== 表单筛选区 ================== */
.filter-box {
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
  margin-bottom: 20px;
}
.refund-form .form-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}
.refund-form ::v-deep .el-form-item {
  margin-bottom: 0;
}
.refund-form ::v-deep .el-form-item__label {
  font-size: 13px;
  color: #333;
}
.time-display {
  font-size: 13px;
  color: #666;
  padding: 0 10px;
}

/* 橙色提交按钮 */
.submit-action {
  margin-top: 15px;
}
.tb-submit-btn {
  background-color: #ff4400;
  border-color: #ff4400;
  color: #fff;
  border-radius: 4px;
  padding: 10px 40px;
  font-weight: bold;
  font-size: 14px;
}
.tb-submit-btn:hover {
  background-color: #f03e00;
  border-color: #f03e00;
}

/* ================== 数据列表区域 ================== */
.list-header {
  display: flex;
  background-color: #f5f5f5;
  padding: 12px 0;
  font-size: 13px;
  color: #333;
  text-align: center;
  border: 1px solid #e8e8e8;
  font-weight: bold;
}
.col-product { width: 35%; text-align: left; padding-left: 20px; }
.col-amount { width: 12%; }
.col-time { width: 18%; }
.col-type { width: 10%; }
.col-status { width: 12%; }
.col-action { width: 13%; }

/* 订单卡片 */
.refund-item {
  border: 1px solid #e8e8e8;
  margin-top: 15px;
  border-radius: 4px;
  overflow: hidden;
}
.refund-item:hover {
  border-color: #ff5000;
}

/* 卡片头部 */
.item-head {
  background-color: #e6f7ff;
  padding: 10px 20px;
  font-size: 12px;
  color: #666;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 20px;
}
.order-no {
  font-weight: bold;
  color: #333;
}
.refund-no {
  color: #666;
}
.shop-name i {
  color: #ff5000;
  margin-right: 4px;
}
.contact-seller {
  color: #1890ff;
  cursor: pointer;
  margin-left: auto;
}
.contact-seller i {
  margin-right: 4px;
}

/* 卡片主体 */
.item-body {
  display: flex;
  align-items: center;
  text-align: center;
  padding: 20px;
  background: #fff;
}

/* 宝贝信息列 */
.col-product {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.p-img {
  width: 80px;
  height: 80px;
  border: 1px solid #eee;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}
.p-info {
  text-align: left;
  flex: 1;
  padding-right: 15px;
}
.p-title {
  font-size: 13px;
  color: #333;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  margin-bottom: 8px;
  cursor: pointer;
}
.p-title:hover {
  color: #ff5000;
}
.p-sku {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 金额时间状态等 */
.price-val {
  font-weight: bold;
  color: #ff5000;
  font-size: 14px;
}
.time-text {
  font-size: 12px;
  color: #666;
}
.type-text {
  font-size: 13px;
  color: #ff5000;
}
.type-text.return_refund {
  color: #1890ff;
}

/* 状态徽章 */
.status-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  display: inline-block;
}
.status-badge.pending {
  color: #ff5000;
  background-color: #fff0eb;
}
.status-badge.approved {
  color: #1890ff;
  background-color: #e6f7ff;
}
.status-badge.returned,
.status-badge.received {
  color: #faad14;
  background-color: #fffbe6;
}
.status-badge.refunded {
  color: #52c41a;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}
.status-badge.rejected {
  color: #ff4d4f;
  background-color: #fff1f0;
}
.status-badge.cancelled {
  color: #999;
  background-color: #f5f5f5;
}

/* 操作列 */
.col-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}
.col-action .el-button {
  margin-left: 0;
  padding: 4px 0;
}
.action-link {
  color: #ff5000 !important;
  font-size: 13px;
}
.danger-text {
  color: #999 !important;
  font-size: 12px;
}
.danger-text:hover {
  color: #ff4d4f !important;
}

/* 分页 */
.pagination-wrap {
  margin-top: 30px;
  text-align: right;
}
.pagination-wrap ::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: #ff5000;
}
.pagination-wrap ::v-deep .el-pagination.is-background .el-pager li:not(.disabled):hover {
  color: #ff5000;
}
</style>
