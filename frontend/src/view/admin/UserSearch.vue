<template>
  <div class="user-search-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">用户管理</span>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form" size="small" @submit.native.prevent>
        <el-form-item label="登录账号">
          <el-input v-model="searchForm.loginAccount" placeholder="请输入账号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable @keyup.enter.native="handleSearch"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable @keyup.enter.native="handleSearch"></el-input>
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
        <el-table-column prop="userId" label="用户ID" min-width="110" align="center"></el-table-column>
        <el-table-column label="头像" width="80" align="center">
          <template slot-scope="scope">
            <el-avatar size="medium" :src="scope.row.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="loginAccount" label="登录账号" min-width="130" show-overflow-tooltip></el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="130" show-overflow-tooltip></el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" align="center"></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column label="性别" width="70" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="info" v-if="scope.row.gender === 0">保密</el-tag>
            <el-tag size="small" type="success" v-else-if="scope.row.gender === 1">男</el-tag>
            <el-tag size="small" type="danger" v-else-if="scope.row.gender === 2">女</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="small" v-if="scope.row.role === 1">普通用户</el-tag>
            <el-tag size="small" type="warning" v-else-if="scope.row.role === 2">管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                active-color="#13ce66"
                inactive-color="#ff4949"
                @change="handleStatusChange(scope.row)"
            >
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
                type="text"
                size="small"
                icon="el-icon-view"
                @click="viewDetails(scope.row)"
            >查看详情</el-button>
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

    <!-- 状态修改确认弹窗 -->
    <el-dialog
        :title="targetStatus === 0 ? '冻结用户' : '解冻用户'"
        :visible.sync="dialogVisible"
        width="450px"
        @close="resetStatusForm"
    >
      <el-form ref="statusForm" :model="statusForm" :rules="statusRules" label-width="90px">
        <el-form-item label="原因类型" prop="reasonType">
          <el-select v-model="statusForm.reasonType" placeholder="请选择原因" style="width: 100%;">
            <el-option
                v-for="item in currentReasonOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="原因备注" prop="reasonDetail">
          <el-input
              type="textarea"
              v-model="statusForm.reasonDetail"
              placeholder="请输入备注说明（选填，选择'其他'时必填）"
              :rows="3"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="confirmStatusChange">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageUsers, updateUserStatus } from '@/api/admin/user'

export default {
  name: 'UserSearch',
  data() {
    return {
      searchForm: {
        loginAccount: '',
        email: '',
        phone: '',
        pageNum: 1,
        pageSize: 10
      },
      tableData: [],
      total: 0,
      loading: false,

      // 状态修改弹窗相关数据
      dialogVisible: false,
      submitLoading: false,
      targetStatus: null,
      currentRow: null,
      statusForm: {
        reasonType: '',
        reasonDetail: ''
      },
      freezeOptions: [
        { label: '违规评论/违规内容', value: 'VIOLATION_REVIEW' },
        { label: '恶意下单/刷单', value: 'MALICIOUS_ORDER' },
        { label: '账号风险异常', value: 'ACCOUNT_RISK' },
        { label: '投诉核实成立', value: 'COMPLAINT_VERIFIED' },
        { label: '人工风控处置', value: 'MANUAL_CONTROL' },
        { label: '其他', value: 'OTHER' }
      ],
      unfreezeOptions: [
        { label: '申诉通过', value: 'APPEAL_APPROVED' },
        { label: '人工复核通过', value: 'MANUAL_REVIEW_PASSED' },
        { label: '误封修正', value: 'MISJUDGMENT_CORRECTION' },
        { label: '风险解除', value: 'RISK_RELEASED' },
        { label: '人工恢复', value: 'MANUAL_RECOVERY' },
        { label: '其他', value: 'OTHER' }
      ],
      statusRules: {
        reasonType: [{ required: true, message: '请选择原因类型', trigger: 'change' }],
        reasonDetail: [
          {
            validator: (rule, value, callback) => {
              if (this.statusForm.reasonType === 'OTHER' && (!value || !value.trim())) {
                callback(new Error('选择“其他”原因时，必须填写备注说明'));
              } else {
                callback();
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  computed: {
    currentReasonOptions() {
      return this.targetStatus === 0 ? this.freezeOptions : this.unfreezeOptions;
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await pageUsers(this.searchForm)
        if (res) {
          this.tableData = res.records || []
          this.total = res.total || 0
        }
      } catch (error) {
        console.error('获取用户列表失败', error)
        this.$message.error('获取用户列表失败，请稍后重试')
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
        loginAccount: '',
        email: '',
        phone: '',
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
    viewDetails(row) {
      // 在新标签页中打开用户详情，沿用后台布局
      const routeUrl = this.$router.resolve({
        path: `/admin/user-detail/${row.userId}`
      });
      window.open(routeUrl.href, '_blank');
    },
    handleStatusChange(row) {
      // 拦截直接切换，将其复原，然后打开弹窗让用户填写原因
      const newStatus = row.status;
      row.status = newStatus === 1 ? 0 : 1; // 恢复UI

      this.targetStatus = newStatus;
      this.currentRow = row;
      this.dialogVisible = true;
    },
    confirmStatusChange() {
      this.$refs.statusForm.validate(async valid => {
        if (!valid) return;

        this.submitLoading = true;
        try {
          const payload = {
            status: this.targetStatus,
            reasonType: this.statusForm.reasonType,
            reasonDetail: this.statusForm.reasonDetail
          };

          await updateUserStatus(this.currentRow.userId, payload);
          this.$message.success(`${this.targetStatus === 1 ? '解冻' : '冻结'}操作成功`);

          // 真正更新列表中的状态
          this.currentRow.status = this.targetStatus;
          this.dialogVisible = false;
        } catch (error) {
          console.error('修改状态失败', error);
          const errorMsg = error.message || error.msg || '';
          if (errorMsg.includes('未发生变化')) {
            this.fetchData(); // 刷新列表
            this.dialogVisible = false;
          } else if (errorMsg.includes('不能为空') || errorMsg.includes('备注')) {
            this.$message.warning(errorMsg);
          } else {
            // request.js 会抛出通用错误，此处只做保底
          }
        } finally {
          this.submitLoading = false;
        }
      });
    },
    resetStatusForm() {
      if (this.$refs.statusForm) {
        this.$refs.statusForm.resetFields();
      }
      this.statusForm = { reasonType: '', reasonDetail: '' };
      this.currentRow = null;
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
.user-search-container {
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
</style>
