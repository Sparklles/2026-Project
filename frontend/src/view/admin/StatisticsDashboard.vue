<template>
  <div class="statistics-page">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="dateRange" size="small" @submit.native.prevent>
        <el-form-item label="统计日期">
          <el-date-picker
              v-model="dateRange.startDate"
              type="date"
              placeholder="开始日期"
              value-format="yyyy-MM-dd"
              clearable>
          </el-date-picker>
        </el-form-item>
        <el-form-item label="至">
          <el-date-picker
              v-model="dateRange.endDate"
              type="date"
              placeholder="结束日期"
              value-format="yyyy-MM-dd"
              clearable>
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="hasDateRange && dateRangeSummary.totalOrders !== undefined" class="range-card" shadow="never">
      <div slot="header" class="card-header">
        <span>选定日期范围汇总</span>
        <div>
          <el-tag v-if="dateRange.startDate" size="mini">从 {{ dateRange.startDate }}</el-tag>
          <el-tag v-if="dateRange.endDate" size="mini" class="date-tag">到 {{ dateRange.endDate }}</el-tag>
        </div>
      </div>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="12" :lg="6">
          <div class="metric-card highlight">
            <div class="metric-icon icon-money"><i class="el-icon-money"></i></div>
            <div class="metric-content">
              <div class="metric-label">区间销售额</div>
              <div class="metric-value">¥ {{ dateRangeSummary.totalSales || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6">
          <div class="metric-card highlight">
            <div class="metric-icon icon-order"><i class="el-icon-s-order"></i></div>
            <div class="metric-content">
              <div class="metric-label">区间订单数</div>
              <div class="metric-value">{{ dateRangeSummary.totalOrders || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6">
          <div class="metric-card highlight">
            <div class="metric-icon icon-goods"><i class="el-icon-goods"></i></div>
            <div class="metric-content">
              <div class="metric-label">区间实付金额</div>
              <div class="metric-value">¥ {{ dateRangeSummary.paidAmount || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :lg="6">
          <div class="metric-card highlight">
            <div class="metric-icon icon-user"><i class="el-icon-user"></i></div>
            <div class="metric-content">
              <div class="metric-label">区间客单价</div>
              <div class="metric-value">¥ {{ dateRangeSummary.averageOrderValue || 0 }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-money"><i class="el-icon-money"></i></div>
          <div class="metric-content">
            <div class="metric-label">总销售额</div>
            <div class="metric-value">¥ {{ summary.totalSales || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-order"><i class="el-icon-s-order"></i></div>
          <div class="metric-content">
            <div class="metric-label">总订单数</div>
            <div class="metric-value">{{ summary.totalOrders || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-goods"><i class="el-icon-goods"></i></div>
          <div class="metric-content">
            <div class="metric-label">总销量</div>
            <div class="metric-value">{{ summary.totalQuantity || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-user"><i class="el-icon-user"></i></div>
          <div class="metric-content">
            <div class="metric-label">客单价</div>
            <div class="metric-value">¥ {{ summary.averageOrderValue || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never">
      <div slot="header" class="card-header">
        <span>销售趋势（按日期）</span>
        <el-tag v-if="hasDateRange" type="primary" size="mini">已筛选</el-tag>
      </div>
      <div ref="salesTrendChart" class="large-chart"></div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>订单状态分布</span>
            <el-radio-group v-model="chartType.orderStatus" size="mini" @change="initOrderStatusChart">
              <el-radio-button label="pie">饼图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="orderStatusChart" class="normal-chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="chart-card" shadow="never">
          <div slot="header" class="card-header">
            <span>支付方式分布</span>
            <el-radio-group v-model="chartType.payType" size="mini" @change="initPayTypeChart">
              <el-radio-button label="pie">饼图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="payTypeChart" class="normal-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never">
      <div slot="header" class="card-header">
        <span>分类销量趋势（按月）</span>
        <el-button type="primary" size="mini" icon="el-icon-download" :loading="exportLoading.categoryTrend" @click="handleExportCategoryTrend">导出Excel</el-button>
      </div>
      <div ref="categoryTrendChart" class="large-chart"></div>
    </el-card>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="table-card" shadow="never">
          <div slot="header" class="card-header">
            <span>书籍销量 TOP10</span>
            <el-button type="primary" size="mini" icon="el-icon-download" :loading="exportLoading.topBooks" @click="handleExportTopBooks">导出Excel</el-button>
          </div>
          <el-table :data="topBooks" stripe border style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center">
              <template slot-scope="{ $index }">
                <el-tag v-if="$index === 0" type="danger" size="mini">1</el-tag>
                <el-tag v-else-if="$index === 1" type="warning" size="mini">2</el-tag>
                <el-tag v-else-if="$index === 2" type="success" size="mini">3</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip></el-table-column>
            <el-table-column prop="totalQuantity" label="销量" width="90" align="center"></el-table-column>
            <el-table-column prop="totalSales" label="销售额" width="110" align="center">
              <template slot-scope="{ row }">
                <span class="money-text">¥{{ row.totalSales || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="12">
        <el-card class="table-card" shadow="never">
          <div slot="header" class="card-header">
            <span>分类销量排行</span>
            <el-button type="primary" size="mini" icon="el-icon-download" :loading="exportLoading.topCategories" @click="handleExportTopCategories">导出Excel</el-button>
          </div>
          <el-table :data="topCategories" stripe border style="width: 100%">
            <el-table-column type="index" label="排名" width="60" align="center"></el-table-column>
            <el-table-column prop="category" label="分类" min-width="140"></el-table-column>
            <el-table-column prop="totalQuantity" label="销量" width="90" align="center"></el-table-column>
            <el-table-column prop="totalSales" label="销售额" width="110" align="center">
              <template slot-scope="{ row }">
                <span class="money-text">¥{{ row.totalSales || 0 }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getSalesSummary,
  getSalesByDate,
  getSalesSummaryByDateRange,
  getTopBooks,
  getTopCategories,
  getCategorySalesTrend,
  getOrderStatusDistribution,
  getPayTypeDistribution,
  exportTopBooks,
  exportTopCategories,
  exportCategoryTrend
} from '@/api/admin/statistics'

export default {
  name: 'StatisticsDashboard',
  data() {
    return {
      loading: false,
      dateRange: { startDate: '', endDate: '' },
      summary: {},
      dateRangeSummary: {},
      salesByDate: [],
      topBooks: [],
      topCategories: [],
      categoryTrend: [],
      orderStatusDistribution: [],
      payTypeDistribution: [],
      charts: {},
      chartType: { orderStatus: 'pie', payType: 'pie' },
      exportLoading: {
        topBooks: false,
        topCategories: false,
        categoryTrend: false
      }
    }
  },
  computed: {
    hasDateRange() {
      return Boolean(this.dateRange.startDate || this.dateRange.endDate)
    }
  },
  created() {
    if (this.ensureAdminLogin()) {
      this.fetchData()
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  activated() {
    this.$nextTick(this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(chart => chart && chart.dispose())
    this.charts = {}
  },
  methods: {
    ensureAdminLogin() {
      if (localStorage.getItem('admin-token')) {
        return true
      }
      this.$message.warning('请先登录后台管理系统')
      this.$router.push('/admin/login')
      return false
    },
    async fetchData() {
      this.loading = true
      try {
        const [summaryRes, salesRes, booksRes, categoriesRes, trendRes, statusRes, payRes] = await Promise.all([
          getSalesSummary(),
          getSalesByDate(this.dateRange.startDate, this.dateRange.endDate),
          getTopBooks(10),
          getTopCategories(),
          getCategorySalesTrend(),
          getOrderStatusDistribution(),
          getPayTypeDistribution()
        ])

        this.summary = summaryRes || {}
        this.salesByDate = Array.isArray(salesRes) ? salesRes : []
        this.topBooks = Array.isArray(booksRes) ? booksRes : []
        this.topCategories = Array.isArray(categoriesRes) ? categoriesRes : []
        this.categoryTrend = Array.isArray(trendRes) ? trendRes : []
        this.orderStatusDistribution = Array.isArray(statusRes) ? statusRes : []
        this.payTypeDistribution = Array.isArray(payRes) ? payRes : []

        if (this.hasDateRange) {
          this.dateRangeSummary = await getSalesSummaryByDateRange(this.dateRange.startDate, this.dateRange.endDate) || {}
        } else {
          this.dateRangeSummary = {}
        }

        this.$nextTick(() => {
          this.initSalesTrendChart()
          this.initOrderStatusChart()
          this.initPayTypeChart()
          this.initCategoryTrendChart()
        })
      } catch (error) {
        console.error('获取统计数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.fetchData()
    },
    handleReset() {
      this.dateRange = { startDate: '', endDate: '' }
      this.dateRangeSummary = {}
      this.fetchData()
    },
    initSalesTrendChart() {
      const dom = this.$refs.salesTrendChart
      if (!dom) return
      const chart = this.recreateChart('salesTrend', dom)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: this.salesByDate.map(item => item.date || item.statDate), boundaryGap: false },
        yAxis: { type: 'value', name: '销售额 (元)' },
        series: [{
          name: '销售额',
          type: 'line',
          smooth: true,
          data: this.salesByDate.map(item => item.amount || item.totalSales || 0),
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.28)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.04)' }
            ])
          },
          itemStyle: { color: '#409EFF' },
          lineStyle: { width: 3 }
        }]
      })
    },
      initOrderStatusChart() {
        const dom = this.$refs.orderStatusChart
        if (!dom) return
        const chart = this.recreateChart('orderStatus', dom)
        const statusMap = { 1: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', 5: '已取消', 6: '售后中', 7: '待签收', 8: '已退款' }
        const data = this.orderStatusDistribution.map(item => ({
          name: item.statusName || item.status_name || statusMap[item.orderStatus] || statusMap[item.order_status] || '未知',
          value: item.count || item.orderCount || item.order_count || item.bookCount || 0
        }))
        this.renderDistributionChart(chart, this.chartType.orderStatus, '订单状态', data, '#409EFF')
      },
      initPayTypeChart() {
        const dom = this.$refs.payTypeChart
        if (!dom) return
        const chart = this.recreateChart('payType', dom)
        const payTypeMap = { 1: '支付宝', 2: '微信', 3: '银行卡' }
        const data = this.payTypeDistribution.map(item => ({
          name: item.payTypeName || item.pay_type_name || payTypeMap[item.payType] || payTypeMap[item.pay_type] || '未知',
          value: item.count || item.orderCount || item.order_count || item.bookCount || 0
        }))
        this.renderDistributionChart(chart, this.chartType.payType, '支付方式', data, '#67C23A')
      },
    initCategoryTrendChart() {
      const dom = this.$refs.categoryTrendChart
      if (!dom) return
      const chart = this.recreateChart('categoryTrend', dom)
      const monthSet = new Set()
      const categorySet = new Set()

      this.categoryTrend.forEach(item => {
        monthSet.add(item.stat_month || item.statMonth)
        categorySet.add(item.category)
      })

      const months = Array.from(monthSet).filter(Boolean).sort()
      const categories = Array.from(categorySet).filter(Boolean)
      const series = categories.map(category => {
        const data = months.map(month => {
          const found = this.categoryTrend.find(item => (item.stat_month || item.statMonth) === month && item.category === category)
          return found ? (found.total_quantity || found.totalQuantity || 0) : 0
        })
        return { name: category, type: 'bar', stack: 'total', emphasis: { focus: 'series' }, data }
      })

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { type: 'scroll', bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', data: months },
        yAxis: { type: 'value', name: '销量' },
        series: series.length ? series : [{ name: '暂无数据', type: 'bar', data: [] }]
      })
    },
    renderDistributionChart(chart, type, name, data, color) {
      const chartData = data.length ? data : [{ name: '暂无数据', value: 0 }]
      if (type === 'pie') {
        chart.setOption({
          tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', top: 'center' },
          series: [{
            name,
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
            data: chartData
          }]
        })
        return
      }

      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: data.map(item => item.name), axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', name: '数量' },
        series: [{
          name,
          type: 'bar',
          data: data.map(item => item.value),
          itemStyle: { color, borderRadius: [4, 4, 0, 0] }
        }]
      })
    },
    recreateChart(name, dom) {
      if (this.charts[name]) {
        this.charts[name].dispose()
      }
      this.charts[name] = echarts.init(dom)
      return this.charts[name]
    },
    handleResize() {
      Object.values(this.charts).forEach(chart => chart && chart.resize())
    },
    async handleExportTopBooks() {
      await this.exportFile('topBooks', () => exportTopBooks(10), '书籍销量TOP10.xlsx')
    },
    async handleExportTopCategories() {
      await this.exportFile('topCategories', exportTopCategories, '分类销量排行.xlsx')
    },
    async handleExportCategoryTrend() {
      await this.exportFile('categoryTrend', exportCategoryTrend, '分类销量趋势.xlsx')
    },
    async exportFile(key, requestFn, defaultName) {
      this.exportLoading[key] = true
      try {
        const response = await requestFn()
        const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/octet-stream' })
        const disposition = response.headers['content-disposition'] || ''
        const match = disposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/)
        const filename = match ? decodeURIComponent(match[1] || match[2]) : defaultName
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = filename
        link.click()
        URL.revokeObjectURL(link.href)
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败，请稍后重试')
      } finally {
        this.exportLoading[key] = false
      }
    }
  }
}
</script>

<style scoped>
.statistics-page {
  padding: 0;
}

.filter-card,
.range-card,
.chart-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.filter-card ::v-deep .el-card__body {
  padding-bottom: 2px;
}

.panel-group {
  margin-bottom: 20px;
}

.metric-card {
  height: 108px;
  padding: 18px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-sizing: border-box;
}

.metric-card.highlight {
  border-color: #b3d8ff;
  background: #f5faff;
}

.metric-icon {
  width: 58px;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 32px;
  flex-shrink: 0;
}

.metric-content {
  margin-left: 16px;
  min-width: 0;
}

.metric-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.metric-value {
  color: #303133;
  font-size: 22px;
  font-weight: 600;
  white-space: nowrap;
}

.icon-money { color: #f56c6c; background: #fef0f0; }
.icon-order { color: #409eff; background: #ecf5ff; }
.icon-goods { color: #67c23a; background: #f0f9eb; }
.icon-user { color: #e6a23c; background: #fdf6ec; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #303133;
}

.date-tag {
  margin-left: 6px;
}

.large-chart {
  width: 100%;
  height: 350px;
}

.normal-chart {
  width: 100%;
  height: 300px;
}

.money-text {
  color: #f56c6c;
  font-weight: 600;
}
</style>
