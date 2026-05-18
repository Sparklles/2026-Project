<template>
  <div class="refund-manage-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">售后申请处理</span>
      </div>

      <!-- 搜索筛选区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form" size="small" @submit.native.prevent>
        <el-row :gutter="10">
          <el-col :span="6">
            <el-form-item label="退款单号" style="width: 100%;">
              <el-input v-model="searchForm.refundNo" placeholder="请输入退款单号" clearable @keyup.enter.native="handleSearch"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单编号" style="width: 100%;">
              <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable @keyup.enter.native="handleSearch"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请人" style="width: 100%;">
              <el-input v-model="searchForm.username" placeholder="请输入用户名/手机号" clearable @keyup.enter.native="handleSearch"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="退款状态" style="width: 100%;">
              <el-select v-model="searchForm.refundStatus" placeholder="全部状态" clearable style="width: 100%;">
                <el-option label="待审核" :value="0"></el-option>
                <el-option label="审核通过" :value="1"></el-option>
                <el-option label="用户已寄回" :value="2"></el-option>
                <el-option label="商家已收货" :value="3"></el-option>
                <el-option label="已退款" :value="4"></el-option>
                <el-option label="审核拒绝" :value="5"></el-option>
                <el-option label="已取消" :value="6"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="6">
            <el-form-item label="退款类型" style="width: 100%;">
              <el-select v-model="searchForm.refundType" placeholder="全部类型" clearable style="width: 100%;">
                <el-option label="仅退款" :value="1"></el-option>
                <el-option label="退货退款" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请时间" style="width: 100%;">
              <el-date-picker
                v-model="searchForm.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="6" style="text-align: right;">
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
              <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 数据统计卡片 -->
      <el-row :gutter="20" class="stat-row" style="margin-bottom: 20px;">
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-value">{{ statData.total || 0 }}</div>
            <div class="stat-label">全部申请</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card pending">
            <div class="stat-value">{{ statData.pending || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card approved">
            <div class="stat-value">{{ statData.approved || 0 }}</div>
            <div class="stat-label">审核通过</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card returned">
            <div class="stat-value">{{ statData.returned || 0 }}</div>
            <div class="stat-label">用户已寄回</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card refunded">
            <div class="stat-value">{{ statData.refunded || 0 }}</div>
            <div class="stat-label">已退款</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card rejected">
            <div class="stat-value">{{ statData.rejected || 0 }}</div>
            <div class="stat-label">审核拒绝</div>
          </div>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%;"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
      >
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="refundNo" label="退款单号" min-width="170" show-overflow-tooltip></el-table-column>
        <el-table-column prop="orderNo" label="订单编号" min-width="170" show-overflow-tooltip></el-table-column>
        <el-table-column label="申请人信息" min-width="140">
          <template slot-scope="scope">
            <div class="user-info">
              <div class="username">{{ scope.row.username || '-' }}</div>
              <div class="phone">{{ scope.row.userPhone || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="200">
          <template slot-scope="scope">
            <div class="product-info">
              <img :src="scope.row.coverUrl || '/images/default-book.png'" class="product-img" @error="handleImageError">
              <div class="product-detail">
                <div class="product-name" :title="scope.row.bookName">{{ scope.row.bookName || '未知商品' }}</div>
                <div class="product-price">¥{{ scope.row.refundAmount }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="退款类型" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.refundType === 1 ? 'warning' : 'info'">
              {{ scope.row.refundType === 1 ? '仅退款' : '退货退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退款状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.refundStatus)">
              {{ getStatusText(scope.row.refundStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="100" align="center">
          <template slot-scope="scope">
            {{ scope.row.processAdminName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-view"
              @click="viewDetails(scope.row)"
            >查看</el-button>
            <el-button
              v-if="scope.row.refundStatus === 0"
              type="text"
              size="small"
              icon="el-icon-check"
              style="color: #67c23a;"
              @click="handleAudit(scope.row, true)"
            >通过</el-button>
            <el-button
              v-if="scope.row.refundStatus === 0"
              type="text"
              size="small"
              icon="el-icon-close"
              style="color: #f56c6c;"
              @click="handleAudit(scope.row, false)"
            >拒绝</el-button>
            <el-button
              v-if="scope.row.refundStatus === 1"
              type="text"
              size="small"
              icon="el-icon-money"
              style="color: #409eff;"
              @click="handleRefund(scope.row)"
            >退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="searchForm.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="searchForm.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <!-- 退款详情弹窗 -->
    <el-dialog
      title="退款详情"
      :visible.sync="detailDialogVisible"
      width="850px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" v-if="refundDetail">
        <!-- 退款基本信息 -->
        <div class="detail-section">
          <div class="section-title">退款信息</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">退款单号：</span>
                <span class="value">{{ refundDetail.refundNo }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">订单编号：</span>
                <span class="value">{{ refundDetail.orderNo }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">退款状态：</span>
                <el-tag size="small" :type="getStatusType(refundDetail.refundStatus)">
                  {{ getStatusText(refundDetail.refundStatus) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">退款类型：</span>
                <el-tag size="small" :type="refundDetail.refundType === 1 ? 'warning' : 'info'">
                  {{ refundDetail.refundType === 1 ? '仅退款' : '退货退款' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">申请时间：</span>
                <span class="value">{{ formatDate(refundDetail.applyTime) }}</span>
              </div>
            </el-col>
            <el-col :span="8" v-if="refundDetail.auditTime">
              <div class="detail-item">
                <span class="label">审核时间：</span>
                <span class="value">{{ formatDate(refundDetail.auditTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 申请人信息 -->
        <div class="detail-section">
          <div class="section-title">申请人信息</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">用户ID：</span>
                <span class="value">{{ refundDetail.userId }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">用户名：</span>
                <span class="value">{{ refundDetail.username }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">手机号：</span>
                <span class="value">{{ refundDetail.userPhone || '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <div class="section-title">商品信息</div>
          <div class="product-detail-card">
            <img :src="refundDetail.coverUrl || '/images/default-book.png'" class="product-img-large" @error="handleImageError">
            <div class="product-detail-info">
              <div class="detail-row">
                <span class="label">商品名称：</span>
                <span class="value">{{ refundDetail.bookName || '未知商品' }}</span>
              </div>
              <div class="detail-row">
                <span class="label">商品单价：</span>
                <span class="value">¥{{ refundDetail.price }}</span>
              </div>
              <div class="detail-row">
                <span class="label">购买数量：</span>
                <span class="value">{{ refundDetail.quantity }}</span>
              </div>
              <div class="detail-row">
                <span class="label">商品总价：</span>
                <span class="value">¥{{ refundDetail.itemTotalPrice }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 退款金额信息 -->
        <div class="detail-section">
          <div class="section-title">退款金额</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">退款金额：</span>
                <span class="value highlight">¥{{ refundDetail.refundAmount }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">订单实付金额：</span>
                <span class="value">¥{{ refundDetail.orderPayAmount }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">订单运费：</span>
                <span class="value">¥{{ refundDetail.freightAmount || 0 }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 退款原因 -->
        <div class="detail-section">
          <div class="section-title">退款原因</div>
          <el-row :gutter="20">
            <el-col :span="24">
              <div class="detail-item">
                <span class="label">退款原因：</span>
                <span class="value">{{ refundDetail.refundReason }}</span>
              </div>
            </el-col>
            <el-col :span="24" v-if="refundDetail.refundDesc">
              <div class="detail-item">
                <span class="label">详细说明：</span>
                <span class="value desc">{{ refundDetail.refundDesc }}</span>
              </div>
            </el-col>
            <el-col :span="24" v-if="refundDetail.rejectReason">
              <div class="detail-item">
                <span class="label">拒绝原因：</span>
                <span class="value reject">{{ refundDetail.rejectReason }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 处理信息 -->
        <div class="detail-section" v-if="refundDetail.processAdminName">
          <div class="section-title">处理信息</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">处理人：</span>
                <span class="value">{{ refundDetail.processAdminName }}</span>
              </div>
            </el-col>
            <el-col :span="8" v-if="refundDetail.returnTime">
              <div class="detail-item">
                <span class="label">用户寄回时间：</span>
                <span class="value">{{ formatDate(refundDetail.returnTime) }}</span>
              </div>
            </el-col>
            <el-col :span="8" v-if="refundDetail.receiveTime">
              <div class="detail-item">
                <span class="label">商家收货时间：</span>
                <span class="value">{{ formatDate(refundDetail.receiveTime) }}</span>
              </div>
            </el-col>
            <el-col :span="8" v-if="refundDetail.refundFinishTime">
              <div class="detail-item">
                <span class="label">退款完成时间：</span>
                <span class="value">{{ formatDate(refundDetail.refundFinishTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="refundDetail && refundDetail.refundStatus === 0"
          type="success"
          icon="el-icon-check"
          @click="handleAuditFromDialog(true)"
        >审核通过</el-button>
        <el-button
          v-if="refundDetail && refundDetail.refundStatus === 0"
          type="danger"
          icon="el-icon-close"
          @click="handleAuditFromDialog(false)"
        >审核拒绝</el-button>
        <el-button
          v-if="refundDetail && refundDetail.refundStatus === 1"
          type="primary"
          icon="el-icon-money"
          @click="handleRefundFromDialog"
        >执行退款</el-button>
      </div>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog
      :title="auditForm.approved ? '审核通过' : '审核拒绝'"
      :visible.sync="auditDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="auditForm" :rules="auditRules" ref="auditForm" label-width="100px" size="small">
        <el-form-item label="退款单号">
          <span>{{ currentRefund.refundNo }}</span>
        </el-form-item>
        <el-form-item label="订单编号">
          <span>{{ currentRefund.orderNo }}</span>
        </el-form-item>
        <el-form-item label="申请人">
          <span>{{ currentRefund.username }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ currentRefund.refundAmount }}</span>
        </el-form-item>
        <el-form-item label="退款类型">
          <el-tag :type="currentRefund.refundType === 1 ? 'warning' : 'info'" size="small">
            {{ currentRefund.refundType === 1 ? '仅退款' : '退货退款' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-tag :type="auditForm.approved ? 'success' : 'danger'">
            {{ auditForm.approved ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="拒绝原因" prop="rejectReason" v-if="!auditForm.approved">
          <el-input
            type="textarea"
            v-model="auditForm.rejectReason"
            placeholder="请输入拒绝原因"
            :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditLoading">确认</el-button>
      </div>
    </el-dialog>

    <!-- 执行退款弹窗 -->
    <el-dialog
      title="执行退款"
      :visible.sync="refundDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px" size="small">
        <el-form-item label="退款单号">
          <span>{{ currentRefund.refundNo }}</span>
        </el-form-item>
        <el-form-item label="订单编号">
          <span>{{ currentRefund.orderNo }}</span>
        </el-form-item>
        <el-form-item label="申请人">
          <span>{{ currentRefund.username }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 18px;">¥{{ currentRefund.refundAmount }}</span>
        </el-form-item>
        <el-form-item label="退款类型">
          <el-tag :type="currentRefund.refundType === 1 ? 'warning' : 'info'">
            {{ currentRefund.refundType === 1 ? '仅退款' : '退货退款' }}
          </el-tag>
        </el-form-item>
      </el-form>
      <div style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
        <p style="margin: 0; color: #606266; font-size: 13px;">
          <i class="el-icon-warning" style="color: #e6a23c; margin-right: 5px;"></i>
          确认执行退款后，退款金额将原路返回给用户，请谨慎操作！
        </p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund" :loading="refundLoading">确认退款</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminRefundList,
  getAdminRefundListByStatus,
  getAdminRefundListByType,
  queryAdminRefundList,
  getAdminRefundDetail,
  auditRefund,
  processRefund
} from '@/api/admin/refund'
import { getAdminProfile } from '@/api/admin/user'

export default {
  name: 'AdminRefundManage',
  data() {
    return {
      loading: false,
      detailLoading: false,
      auditLoading: false,
      refundLoading: false,
      adminId: null,
      tableData: [],
      total: 0,
      searchForm: {
        page: 1,
        pageSize: 10,
        refundNo: '',
        orderNo: '',
        username: '',
        refundStatus: null,
        refundType: null,
        timeRange: null
      },
      statData: {
        total: 0,
        pending: 0,
        approved: 0,
        returned: 0,
        refunded: 0,
        rejected: 0
      },
      detailDialogVisible: false,
      refundDetail: null,
      auditDialogVisible: false,
      refundDialogVisible: false,
      currentRefund: {},
      auditForm: {
        approved: true,
        rejectReason: ''
      },
      auditRules: {
        rejectReason: [
          { required: true, message: '请输入拒绝原因', trigger: 'blur', validator: (rule, value, callback) => {
            if (!this.auditForm.approved && (!value || value.trim() === '')) {
              callback(new Error('拒绝原因不能为空'))
            } else {
              callback()
            }
          }}
        ]
      }
    }
  },
  async created() {
    await this.fetchAdminInfo()
    this.fetchRefundList()
    this.fetchStatData()
  },
  methods: {
    // 获取管理员信息
    async fetchAdminInfo() {
      try {
        const profile = await getAdminProfile()
        this.adminId = profile && (profile.adminId || profile.userId || profile.id)
      } catch (error) {
        console.error('解析管理员信息失败', error)
      }
    },

    // 获取统计数据
    async fetchStatData() {
      if (!this.adminId) return

      try {
        // 这里可以通过调用不分页接口获取所有数据来统计
        // 暂时使用模拟数据，实际项目中可以调用 getAdminRefundListAll
        const res = await getAdminRefundList({
          adminId: this.adminId,
          page: 1,
          pageSize: 9999
        })
        const data = res.data || res
        const records = data.records || data.list || data || []

        this.statData = {
          total: records.length,
          pending: records.filter(r => r.refundStatus === 0).length,
          approved: records.filter(r => r.refundStatus === 1).length,
          returned: records.filter(r => r.refundStatus === 2).length,
          refunded: records.filter(r => r.refundStatus === 4).length,
          rejected: records.filter(r => r.refundStatus === 5).length
        }
      } catch (error) {
        console.error('获取统计数据失败', error)
      }
    },

    // 获取退款列表
    async fetchRefundList() {
      if (!this.adminId) {
        this.$message.warning('请先登录')
        return
      }

      this.loading = true
      try {
        let res

        // 判断是否需要使用组合查询
        const hasTimeRange = this.searchForm.timeRange && this.searchForm.timeRange.length === 2
        const hasRefundNo = this.searchForm.refundNo && this.searchForm.refundNo.trim() !== ''

        if (hasTimeRange || hasRefundNo || this.searchForm.orderNo) {
          // 使用组合查询接口
          const params = {
            adminId: this.adminId,
            page: this.searchForm.page,
            pageSize: this.searchForm.pageSize
          }

          if (hasRefundNo) {
            params.refundNo = this.searchForm.refundNo.trim()
          }
          if (this.searchForm.orderNo) {
            params.orderNo = this.searchForm.orderNo.trim()
          }
          if (this.searchForm.refundStatus !== null && this.searchForm.refundStatus !== '') {
            params.refundStatus = this.searchForm.refundStatus
          }
          if (this.searchForm.refundType !== null && this.searchForm.refundType !== '') {
            params.refundType = this.searchForm.refundType
          }
          if (hasTimeRange) {
            params.beginTime = this.searchForm.timeRange[0]
            params.endTime = this.searchForm.timeRange[1]
          }

          res = await queryAdminRefundList(params)
        } else if (this.searchForm.refundStatus !== null && this.searchForm.refundStatus !== '') {
          // 按状态查询
          res = await getAdminRefundListByStatus({
            adminId: this.adminId,
            refundStatus: this.searchForm.refundStatus,
            page: this.searchForm.page,
            pageSize: this.searchForm.pageSize
          })
        } else if (this.searchForm.refundType !== null && this.searchForm.refundType !== '') {
          // 按类型查询
          res = await getAdminRefundListByType({
            adminId: this.adminId,
            refundType: this.searchForm.refundType,
            page: this.searchForm.page,
            pageSize: this.searchForm.pageSize
          })
        } else {
          // 查询全部
          res = await getAdminRefundList({
            adminId: this.adminId,
            page: this.searchForm.page,
            pageSize: this.searchForm.pageSize
          })
        }

        const data = res.data || res
        let records = data.records || data.list || data || []

        // 前端过滤用户名（如果输入了用户名筛选条件）
        if (this.searchForm.username && this.searchForm.username.trim() !== '') {
          const keyword = this.searchForm.username.trim().toLowerCase()
          records = records.filter(r => {
            const username = (r.username || '').toLowerCase()
            const phone = (r.userPhone || '').toLowerCase()
            return username.includes(keyword) || phone.includes(keyword)
          })
        }

        this.tableData = records
        this.total = data.total || records.length
      } catch (error) {
        console.error('获取退款列表失败', error)
        this.$message.error('获取退款列表失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.searchForm.page = 1
      this.fetchRefundList()
    },

    // 重置搜索
    resetSearch() {
      this.searchForm = {
        page: 1,
        pageSize: 10,
        refundNo: '',
        orderNo: '',
        username: '',
        refundStatus: null,
        refundType: null,
        timeRange: null
      }
      this.fetchRefundList()
    },

    // 分页大小变化
    handleSizeChange(val) {
      this.searchForm.pageSize = val
      this.fetchRefundList()
    },

    // 页码变化
    handleCurrentChange(val) {
      this.searchForm.page = val
      this.fetchRefundList()
    },

    // 查看退款详情
    async viewDetails(row) {
      this.detailDialogVisible = true
      this.detailLoading = true
      this.currentRefund = row

      try {
        const res = await getAdminRefundDetail({
          adminId: this.adminId,
          refundNo: row.refundNo
        })
        this.refundDetail = res.data || res
      } catch (error) {
        console.error('获取退款详情失败', error)
        this.$message.error('获取退款详情失败')
      } finally {
        this.detailLoading = false
      }
    },

    // 处理审核
    handleAudit(row, approved) {
      this.currentRefund = row
      this.auditForm = {
        approved: approved,
        rejectReason: ''
      }
      this.auditDialogVisible = true
    },

    // 从详情弹窗处理审核
    handleAuditFromDialog(approved) {
      this.auditForm = {
        approved: approved,
        rejectReason: ''
      }
      this.auditDialogVisible = true
    },

    // 提交审核
    submitAudit() {
      this.$refs.auditForm.validate(async (valid) => {
        if (valid) {
          this.auditLoading = true
          try {
            await auditRefund({
              adminId: this.adminId,
              refundId: this.currentRefund.id,
              approved: this.auditForm.approved,
              rejectReason: this.auditForm.rejectReason
            })
            this.$message.success(this.auditForm.approved ? '审核通过' : '已拒绝退款申请')
            this.auditDialogVisible = false
            this.detailDialogVisible = false
            this.fetchRefundList()
            this.fetchStatData()
          } catch (error) {
            console.error('审核失败', error)
            this.$message.error(error.message || '审核失败')
          } finally {
            this.auditLoading = false
          }
        }
      })
    },

    // 处理退款
    handleRefund(row) {
      this.currentRefund = row
      this.refundDialogVisible = true
    },

    // 从详情弹窗处理退款
    handleRefundFromDialog() {
      this.refundDialogVisible = true
    },

    // 提交退款
    async submitRefund() {
      this.refundLoading = true
      try {
        await processRefund({
          adminId: this.adminId,
          refundId: this.currentRefund.id
        })
        this.$message.success('退款成功')
        this.refundDialogVisible = false
        this.detailDialogVisible = false
        this.fetchRefundList()
        this.fetchStatData()
      } catch (error) {
        console.error('退款失败', error)
        this.$message.error(error.message || '退款失败')
      } finally {
        this.refundLoading = false
      }
    },

    // 获取退款状态文本
    getStatusText(status) {
      const statusMap = {
        0: '待审核',
        1: '审核通过',
        2: '用户已寄回',
        3: '商家已收货',
        4: '已退款',
        5: '审核拒绝',
        6: '已取消'
      }
      return statusMap[status] || '未知'
    },

    // 获取退款状态标签类型
    getStatusType(status) {
      const typeMap = {
        0: 'warning',   // 待审核 - 橙色
        1: 'success',   // 审核通过 - 绿色
        2: 'info',      // 用户已寄回 - 灰色
        3: 'info',      // 商家已收货 - 灰色
        4: 'success',   // 已退款 - 绿色
        5: 'danger',    // 审核拒绝 - 红色
        6: 'info'       // 已取消 - 灰色
      }
      return typeMap[status] || 'info'
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },

    // 处理图片加载失败
    handleImageError(e) {
      e.target.src = '/images/default-book.png'
    }
  }
}
</script>

<style scoped>
.refund-manage-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 统计卡片 */
.stat-row {
  margin-top: 10px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  border-top: 3px solid #909399;
}

.stat-card.pending {
  border-top-color: #e6a23c;
}

.stat-card.approved {
  border-top-color: #67c23a;
}

.stat-card.returned {
  border-top-color: #409eff;
}

.stat-card.refunded {
  border-top-color: #67c23a;
}

.stat-card.rejected {
  border-top-color: #f56c6c;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

/* 用户信息 */
.user-info {
  font-size: 13px;
}

.user-info .username {
  color: #303133;
  margin-bottom: 3px;
}

.user-info .phone {
  color: #909399;
  font-size: 12px;
}

/* 商品信息展示 */
.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 13px;
  color: #f56c6c;
  font-weight: bold;
}

/* 详情弹窗样式 */
.detail-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.detail-item {
  margin-bottom: 10px;
  font-size: 13px;
}

.detail-item .label {
  color: #606266;
  margin-right: 5px;
}

.detail-item .value {
  color: #303133;
}

.detail-item .value.highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.detail-item .value.reject {
  color: #f56c6c;
}

.detail-item .value.desc {
  color: #606266;
  line-height: 1.6;
}

/* 商品详情卡片 */
.product-detail-card {
  display: flex;
  gap: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.product-img-large {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.product-detail-info {
  flex: 1;
}

.detail-row {
  margin-bottom: 10px;
  font-size: 13px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  text-align: right;
}
</style>
