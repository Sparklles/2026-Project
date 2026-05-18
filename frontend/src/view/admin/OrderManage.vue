<template>
  <div class="order-manage-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">订单管理</span>
      </div>

      <!-- 搜索筛选区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form" size="small" @submit.native.prevent>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="收货人">
          <el-input v-model="searchForm.consignee" placeholder="请输入收货人" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.orderStatus" placeholder="全部状态" clearable style="width: 140px;">
            <el-option label="待付款" :value="1"></el-option>
            <el-option label="待发货" :value="2"></el-option>
            <el-option label="待收货" :value="3"></el-option>
            <el-option label="已完成" :value="4"></el-option>
            <el-option label="已取消" :value="5"></el-option>
            <el-option label="售后中" :value="6"></el-option>
            <el-option label="待签收" :value="7"></el-option>
            <el-option label="已退款" :value="8"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%; margin-top: 10px;"
        :header-cell-style="{background:'#f5f7fa', color:'#606266'}"
      >
        <el-table-column prop="orderNo" label="订单编号" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="下单用户" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="consignee" label="收货人" min-width="100"></el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="130" align="center"></el-table-column>
        <el-table-column label="订单金额" min-width="120" align="center">
          <template slot-scope="scope">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.payPrice || scope.row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.orderStatus)">
              {{ getStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="getPayStatusType(scope.row.payStatus)">
              {{ getPayStatusText(scope.row.payStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              icon="el-icon-view"
              @click="viewDetails(scope.row)"
            >查看详情</el-button>
            <el-button
              v-if="scope.row.orderStatus === 2"
              type="text"
              size="small"
              icon="el-icon-truck"
              style="color: #67c23a;"
              @click="handleShip(scope.row)"
            >发货</el-button>
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

    <!-- 订单详情弹窗 -->
    <el-dialog
      title="订单详情"
      :visible.sync="detailDialogVisible"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" v-if="orderDetail">
        <!-- 订单基本信息 -->
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">订单编号：</span>
                <span class="value">{{ orderDetail.orderNo }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">下单用户：</span>
                <span class="value">{{ orderDetail.username }} (ID: {{ orderDetail.userId }})</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">订单状态：</span>
                <el-tag size="small" :type="getStatusType(orderDetail.orderStatus)">
                  {{ getStatusText(orderDetail.orderStatus) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">支付状态：</span>
                <el-tag size="small" :type="getPayStatusType(orderDetail.payStatus)">
                  {{ getPayStatusText(orderDetail.payStatus) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 订单时间轴 -->
        <div class="detail-section">
          <div class="section-title">订单时间</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">创建时间：</span>
                <span class="value">{{ formatDate(orderDetail.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12" v-if="orderDetail.payTime">
              <div class="detail-item">
                <span class="label">支付时间：</span>
                <span class="value">{{ formatDate(orderDetail.payTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12" v-if="orderDetail.shipTime">
              <div class="detail-item">
                <span class="label">发货时间：</span>
                <span class="value">{{ formatDate(orderDetail.shipTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12" v-if="orderDetail.closeTime">
              <div class="detail-item">
                <span class="label">完成/关闭时间：</span>
                <span class="value">{{ formatDate(orderDetail.closeTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 收货信息 -->
        <div class="detail-section">
          <div class="section-title">收货信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">收货人：</span>
                <span class="value">{{ orderDetail.consignee }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">联系电话：</span>
                <span class="value">{{ orderDetail.phone }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="label">收货地址：</span>
                <span class="value">{{ orderDetail.address }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <div class="section-title">商品信息</div>
          <el-table :data="orderDetail.items" border size="small">
            <el-table-column label="商品图片" width="80" align="center">
              <template slot-scope="scope">
                <img :src="scope.row.coverUrl || scope.row.bookCover || '/images/default-book.png'" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
              </template>
            </el-table-column>
            <el-table-column label="商品ID" width="100" align="center">
              <template slot-scope="scope">
                {{ scope.row.bookId || scope.row.productId || scope.row.id || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="商品名称" min-width="150" show-overflow-tooltip>
              <template slot-scope="scope">
                {{ scope.row.bookTitle || scope.row.bookName || scope.row.productName || scope.row.name || '未知商品' }}
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100" align="center">
              <template slot-scope="scope">
                ¥{{ scope.row.price || scope.row.unitPrice || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center">
              <template slot-scope="scope">
                {{ scope.row.quantity || scope.row.num || 1 }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100" align="center">
              <template slot-scope="scope">
                ¥{{ ((scope.row.price || scope.row.unitPrice || 0) * (scope.row.quantity || scope.row.num || 1)).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 金额信息 -->
        <div class="detail-section">
          <div class="section-title">金额信息</div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">商品总价：</span>
                <span class="value">¥{{ orderDetail.totalPrice }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">运费：</span>
                <span class="value">¥{{ orderDetail.freightPrice || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="detail-item">
                <span class="label">优惠金额：</span>
                <span class="value">¥{{ orderDetail.discountPrice || 0 }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item" style="margin-top: 10px;">
                <span class="label" style="font-size: 14px; font-weight: bold;">实付金额：</span>
                <span class="value" style="font-size: 18px; color: #f56c6c; font-weight: bold;">¥{{ orderDetail.payPrice }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 物流信息 -->
        <div class="detail-section" v-if="orderDetail.orderStatus >= 3 && (orderDetail.logisticsCompany || orderDetail.logisticsNo)">
          <div class="section-title">物流信息</div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">物流公司：</span>
                <span class="value">{{ orderDetail.logisticsCompany || '-' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">物流单号：</span>
                <span class="value">{{ orderDetail.logisticsNo || '-' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="orderDetail && orderDetail.orderStatus === 2"
          type="primary"
          icon="el-icon-truck"
          @click="handleShipFromDialog"
        >立即发货</el-button>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog
      title="订单发货"
      :visible.sync="shipDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="shipForm" :rules="shipRules" ref="shipForm" label-width="100px" size="small">
        <el-form-item label="订单编号">
          <span>{{ currentOrder.orderNo }}</span>
        </el-form-item>
        <el-form-item label="收货人">
          <span>{{ currentOrder.consignee }}</span>
        </el-form-item>
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-select v-model="shipForm.logisticsCompany" placeholder="请选择物流公司" style="width: 100%;">
            <el-option label="顺丰速运" value="顺丰速运"></el-option>
            <el-option label="中通快递" value="中通快递"></el-option>
            <el-option label="圆通速递" value="圆通速递"></el-option>
            <el-option label="申通快递" value="申通快递"></el-option>
            <el-option label="韵达快递" value="韵达快递"></el-option>
            <el-option label="EMS" value="EMS"></el-option>
            <el-option label="京东物流" value="京东物流"></el-option>
            <el-option label="德邦快递" value="德邦快递"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号" prop="logisticsNo">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip" :loading="shipLoading">确认发货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdminOrderList,
  getAdminOrderListByStatus,
  getAdminOrderDetail,
  shipOrder
} from '@/api/admin/order'
import { getAdminProfile } from '@/api/admin/user'

export default {
  name: 'OrderManage',
  data() {
    return {
      loading: false,
      detailLoading: false,
      shipLoading: false,
      adminId: null,
      tableData: [],
      total: 0,
      searchForm: {
        page: 1,
        pageSize: 10,
        orderNo: '',
        username: '',
        consignee: '',
        orderStatus: null
      },
      detailDialogVisible: false,
      orderDetail: null,
      shipDialogVisible: false,
      currentOrder: {},
      shipForm: {
        logisticsCompany: '',
        logisticsNo: ''
      },
      shipRules: {
        logisticsCompany: [
          { required: true, message: '请选择物流公司', trigger: 'change' }
        ],
        logisticsNo: [
          { required: true, message: '请输入物流单号', trigger: 'blur' },
          { min: 5, max: 30, message: '长度在 5 到 30 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  async created() {
    await this.fetchAdminInfo()
    if (this.$route.query.orderNo) {
      this.searchForm.orderNo = this.$route.query.orderNo
    }
    this.fetchOrderList()
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

    // 获取订单列表
    async fetchOrderList() {
      if (!this.adminId) {
        this.$message.warning('请先登录')
        return
      }

      this.loading = true
      try {
        const params = {
          adminId: this.adminId,
          page: this.searchForm.page,
          pageSize: this.searchForm.pageSize
        }
        if (this.searchForm.orderNo) {
          params.orderNo = this.searchForm.orderNo.trim()
        }
        if (this.searchForm.username) {
          params.username = this.searchForm.username.trim()
        }
        if (this.searchForm.consignee) {
          params.consignee = this.searchForm.consignee.trim()
        }

        let res
        if (this.searchForm.orderStatus !== null && this.searchForm.orderStatus !== '') {
          // 按状态查询
          res = await getAdminOrderListByStatus({
            ...params,
            orderStatus: this.searchForm.orderStatus
          })
        } else {
          // 查询全部
          res = await getAdminOrderList(params)
        }

        const data = res.data || res
        const records = data.records || data.list || data || []
        this.tableData = records
        this.total = data.total || records.length
      } catch (error) {
        console.error('获取订单列表失败', error)
        this.$message.error('获取订单列表失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索
    handleSearch() {
      this.searchForm.page = 1
      this.fetchOrderList()
    },

    // 重置搜索
    resetSearch() {
      this.searchForm = {
        page: 1,
        pageSize: 10,
        orderNo: '',
        username: '',
        consignee: '',
        orderStatus: null
      }
      this.fetchOrderList()
    },

    // 分页大小变化
    handleSizeChange(val) {
      this.searchForm.pageSize = val
      this.fetchOrderList()
    },

    // 页码变化
    handleCurrentChange(val) {
      this.searchForm.page = val
      this.fetchOrderList()
    },

    // 查看订单详情
    async viewDetails(row) {
      this.detailDialogVisible = true
      this.detailLoading = true
      this.currentOrder = row

      try {
        const res = await getAdminOrderDetail({
          adminId: this.adminId,
          orderNo: row.orderNo
        })
        this.orderDetail = res.data || res
      } catch (error) {
        console.error('获取订单详情失败', error)
        this.$message.error('获取订单详情失败')
      } finally {
        this.detailLoading = false
      }
    },

    // 打开发货弹窗
    handleShip(row) {
      this.currentOrder = row
      this.shipForm = {
        logisticsCompany: '',
        logisticsNo: ''
      }
      this.shipDialogVisible = true
    },

    // 从详情弹窗打开发货弹窗
    handleShipFromDialog() {
      this.detailDialogVisible = false
      this.shipForm = {
        logisticsCompany: '',
        logisticsNo: ''
      }
      this.shipDialogVisible = true
    },

    // 提交发货
    submitShip() {
      this.$refs.shipForm.validate(async (valid) => {
        if (valid) {
          this.shipLoading = true
          try {
            await shipOrder({
              userId: this.adminId,
              orderNo: this.currentOrder.orderNo,
              logisticsCompany: this.shipForm.logisticsCompany,
              logisticsNo: this.shipForm.logisticsNo
            })
            this.$message.success('发货成功')
            this.shipDialogVisible = false
            this.fetchOrderList()
          } catch (error) {
            console.error('发货失败', error)
            this.$message.error(error.message || '发货失败')
          } finally {
            this.shipLoading = false
          }
        }
      })
    },

    // 获取订单状态文本
    getStatusText(status) {
      const statusMap = {
        1: '待付款',
        2: '待发货',
        3: '待收货',
        4: '已完成',
        5: '已取消',
        6: '售后中',
        7: '待签收',
        8: '已退款'
      }
      return statusMap[status] || '未知'
    },

    // 获取订单状态标签类型
    getStatusType(status) {
      const typeMap = {
        1: 'info',      // 待付款 - 灰色
        2: 'warning',   // 待发货 - 橙色
        3: 'success',   // 待收货 - 绿色
        4: '',          // 已完成 - 蓝色
        5: 'danger',    // 已取消 - 红色
        6: 'warning',   // 售后中 - 橙色
        7: 'success',   // 待签收 - 绿色
        8: 'danger'     // 已退款 - 红色
      }
      return typeMap[status] || 'info'
    },

    // 获取支付状态文本
    getPayStatusText(status) {
      const statusMap = {
        0: '未支付',
        1: '支付中',
        2: '已支付'
      }
      return statusMap[status] || '未知'
    },

    // 获取支付状态标签类型
    getPayStatusType(status) {
      const typeMap = {
        0: 'danger',    // 未支付 - 红色
        1: 'warning',   // 支付中 - 橙色
        2: 'success'    // 已支付 - 绿色
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
    }
  }
}
</script>

<style scoped>
.order-manage-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
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

.dialog-footer {
  text-align: right;
}
</style>
