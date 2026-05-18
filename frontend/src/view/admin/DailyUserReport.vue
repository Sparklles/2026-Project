<template>
  <div class="statistics-page">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="listQuery" size="small" @submit.native.prevent>
        <el-form-item label="统计日期">
          <el-date-picker
              v-model="listQuery.startDate"
              type="date"
              placeholder="开始日期"
              value-format="yyyy-MM-dd"
              clearable>
          </el-date-picker>
        </el-form-item>
        <el-form-item label="至">
          <el-date-picker
              v-model="listQuery.endDate"
              type="date"
              placeholder="结束日期"
              value-format="yyyy-MM-dd"
              clearable>
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="panel-group">
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-user"><i class="el-icon-user"></i></div>
          <div class="metric-content">
            <div class="metric-label">筛选区间新增</div>
            <div class="metric-value">{{ summary.newUsers || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-order"><i class="el-icon-s-custom"></i></div>
          <div class="metric-content">
            <div class="metric-label">当前累计用户</div>
            <div class="metric-value">{{ summary.cumulativeUsers || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-goods"><i class="el-icon-top-right"></i></div>
          <div class="metric-content">
            <div class="metric-label">日均新增</div>
            <div class="metric-value">{{ summary.avgNewUsers || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :lg="6">
        <div class="metric-card">
          <div class="metric-icon icon-money"><i class="el-icon-data-line"></i></div>
          <div class="metric-content">
            <div class="metric-label">峰值日新增</div>
            <div class="metric-value">{{ summary.maxNewUsers || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="never">
      <div slot="header" class="card-header">
        <span>每日新增用户趋势</span>
      </div>
      <div ref="userTrendChart" class="chart-box"></div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div slot="header" class="card-header">
        <span>每日新增用户明细</span>
        <el-button type="primary" size="mini" icon="el-icon-download" @click="handleExport">导出CSV</el-button>
      </div>
      <el-table v-loading="listLoading" :data="displayList" stripe border style="width: 100%">
        <el-table-column prop="statDate" label="日期" width="130" align="center" sortable></el-table-column>
        <el-table-column prop="newUsers" label="新增用户" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag type="primary" effect="plain" size="mini">{{ row.newUsers || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cumulativeUsers" label="累计用户" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="number-text">{{ row.cumulativeUsers || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="momRate" label="环比" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="rateType(row.momRate)" :type="rateType(row.momRate)" size="mini">{{ row.momRate }}</el-tag>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="yoyRate" label="同比" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="rateType(row.yoyRate)" :type="rateType(row.yoyRate)" size="mini">{{ row.yoyRate }}</el-tag>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="listQuery.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="listQuery.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDailyUserReport } from '@/api/admin/statistics'

export default {
  name: 'DailyUserReport',
  data() {
    return {
      listQuery: {
        startDate: '',
        endDate: '',
        current: 1,
        size: 20
      },
      listLoading: false,
      allList: [],
      filteredList: [],
      displayList: [],
      total: 0,
      summary: {},
      chart: null
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
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
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
      this.listLoading = true
      try {
        const res = await getDailyUserReport()
        this.allList = Array.isArray(res) ? res : []
        this.applyFilter()
      } catch (error) {
        console.error('获取每日新增用户报表失败:', error)
      } finally {
        this.listLoading = false
      }
    },
    applyFilter() {
      let filtered = this.allList.slice()

      if (this.listQuery.startDate) {
        filtered = filtered.filter(item => item.statDate >= this.listQuery.startDate)
      }
      if (this.listQuery.endDate) {
        filtered = filtered.filter(item => item.statDate <= this.listQuery.endDate)
      }

      this.filteredList = filtered
      this.calcSummary(filtered)

      this.total = filtered.length
      const start = (this.listQuery.current - 1) * this.listQuery.size
      this.displayList = filtered.slice(start, start + this.listQuery.size)

      this.$nextTick(() => {
        this.initChart(filtered)
      })
    },
    calcSummary(list) {
      let totalNew = 0
      let maxNew = 0
      let lastCumulative = 0

      list.forEach(item => {
        const newUsers = Number(item.newUsers || 0)
        totalNew += newUsers
        maxNew = Math.max(maxNew, newUsers)
        lastCumulative = Number(item.cumulativeUsers || lastCumulative || 0)
      })

      const days = list.length || 1
      this.summary = {
        newUsers: totalNew,
        cumulativeUsers: lastCumulative,
        avgNewUsers: (totalNew / days).toFixed(1),
        maxNewUsers: maxNew
      }
    },
    initChart(list) {
      const dom = this.$refs.userTrendChart
      if (!dom) return
      if (this.chart) {
        this.chart.dispose()
      }

      this.chart = echarts.init(dom)
      this.chart.setOption({
        color: ['#409EFF', '#67C23A'],
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(64, 158, 255, 0.08)' } }
        },
        legend: {
          data: ['新增用户', '累计用户'],
          top: 8,
          itemGap: 24
        },
        grid: { left: 48, right: 56, top: 72, bottom: 36, containLabel: true },
        xAxis: {
          type: 'category',
          data: list.map(item => item.statDate),
          boundaryGap: true,
          axisTick: { alignWithLabel: true },
          axisLine: { lineStyle: { color: '#dcdfe6' } },
          axisLabel: { color: '#606266' }
        },
        yAxis: [
          {
            type: 'value',
            name: '新增用户',
            position: 'left',
            minInterval: 1,
            splitLine: { lineStyle: { color: '#edf2f7' } },
            axisLabel: { color: '#606266' }
          },
          {
            type: 'value',
            name: '累计用户',
            position: 'right',
            minInterval: 1,
            splitLine: { show: false },
            axisLabel: { color: '#606266' }
          }
        ],
        series: [
          {
            name: '新增用户',
            type: 'bar',
            data: list.map(item => item.newUsers || 0),
            barMaxWidth: 44,
            itemStyle: {
              color: '#409EFF',
              borderRadius: [6, 6, 0, 0]
            }
          },
          {
            name: '累计用户',
            type: 'line',
            yAxisIndex: 1,
            data: list.map(item => item.cumulativeUsers || 0),
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: { color: '#67C23A', borderColor: '#fff', borderWidth: 2 },
            lineStyle: { width: 3, color: '#67C23A' },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(103, 194, 58, 0.18)' },
                  { offset: 1, color: 'rgba(103, 194, 58, 0.02)' }
                ]
              }
            }
          }
        ]
      })
    },
    rateType(value) {
      const text = String(value || '')
      if (text.startsWith('+')) return 'danger'
      if (text.startsWith('-') && text !== '-') return 'success'
      return ''
    },
    handleQuery() {
      this.listQuery.current = 1
      this.applyFilter()
    },
    handleReset() {
      this.listQuery = { startDate: '', endDate: '', current: 1, size: 20 }
      this.applyFilter()
    },
    handleSizeChange(val) {
      this.listQuery.size = val
      this.listQuery.current = 1
      this.applyFilter()
    },
    handleCurrentChange(val) {
      this.listQuery.current = val
      this.applyFilter()
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    handleExport() {
      if (!this.filteredList.length) {
        this.$message.warning('暂无可导出的数据')
        return
      }

      const header = ['日期', '新增用户', '累计用户', '环比', '同比']
      const rows = this.filteredList.map(item => [
        item.statDate || '',
        item.newUsers || 0,
        item.cumulativeUsers || 0,
        item.momRate || '-',
        item.yoyRate || '-'
      ])
      const csv = [header].concat(rows).map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\n')
      const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '每日新增用户报表.csv'
      link.click()
      URL.revokeObjectURL(link.href)
    }
  }
}
</script>

<style scoped>
.statistics-page {
  padding: 0;
}

.filter-card,
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
}

.icon-user { color: #f56c6c; background: #fef0f0; }
.icon-order { color: #409eff; background: #ecf5ff; }
.icon-goods { color: #67c23a; background: #f0f9eb; }
.icon-money { color: #e6a23c; background: #fdf6ec; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.number-text {
  color: #409eff;
  font-weight: 600;
}

.empty-text {
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
