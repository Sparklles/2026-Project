<template>
  <div class="audit-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">用户状态变更审计日志</span>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form" size="small" @submit.native.prevent>
        <el-form-item label="目标用户账号">
          <el-input v-model="searchForm.targetLoginAccount" placeholder="请输入账号模糊搜索" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="操作管理员账号">
          <el-input v-model="searchForm.adminLoginAccount" placeholder="请输入管理员账号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="禁用" value="FREEZE"></el-option>
            <el-option label="解冻" value="UNFREEZE"></el-option>
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
        <el-table-column prop="id" label="日志ID" width="100" align="center"></el-table-column>
        <el-table-column prop="targetLoginAccount" label="目标账号" min-width="130" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="user-account">{{ scope.row.targetLoginAccount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="adminLoginAccount" label="操作人(管理员)" min-width="130" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.adminLoginAccount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="danger" v-if="scope.row.action === 'FREEZE'">禁用</el-tag>
            <el-tag size="small" type="success" v-else-if="scope.row.action === 'UNFREEZE'">解冻</el-tag>
            <span v-else>{{ scope.row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reasonTypeName" label="操作原因" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="reasonDetail" label="备注说明" min-width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ scope.row.reasonDetail || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="searchForm.pageNum"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="searchForm.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { pageUserStatusAudit } from '@/api/admin/user'

export default {
  name: 'UserStatusAudit',
  data() {
    return {
      searchForm: {
        targetLoginAccount: '',
        adminLoginAccount: '',
        action: '',
        pageNum: 1,
        pageSize: 10
      },
      tableData: [],
      total: 0,
      loading: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await pageUserStatusAudit(this.searchForm)
        if (res) {
          this.tableData = res.records || []
          this.total = res.total || 0
        }
      } catch (error) {
        console.error('获取审计日志失败', error)
        // message is usually handled by request.js interceptor
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.searchForm.pageNum = 1
      this.fetchData()
    },
    resetSearch() {
      this.searchForm = {
        targetLoginAccount: '',
        adminLoginAccount: '',
        action: '',
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.searchForm.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.searchForm.pageNum = val
      this.fetchData()
    },
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }
}
</script>

<style scoped>
.audit-container {
  padding: 0;
}

.box-card {
  border-radius: 8px;
}

.search-form {
  margin-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-account {
  color: #409EFF;
  font-weight: 500;
}
</style>
