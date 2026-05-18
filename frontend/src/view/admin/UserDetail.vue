<template>
  <div class="user-detail-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <el-page-header @back="goBack" content="用户详细信息">
        </el-page-header>
      </div>

      <div v-loading="loading" class="detail-content">
        <div v-if="userInfo" class="user-profile">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
            <h3 class="nickname">{{ userInfo.nickname || userInfo.loginAccount }}</h3>
            <div class="signature">{{ userInfo.signature || '该用户很懒，什么都没有留下' }}</div>
          </div>

          <el-descriptions class="margin-top" title="基本信息" :column="2" border>
            <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
            <el-descriptions-item label="登录账号">{{ userInfo.loginAccount }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">
              <el-tag size="small" type="info" v-if="userInfo.gender === 0">保密</el-tag>
              <el-tag size="small" type="success" v-else-if="userInfo.gender === 1">男</el-tag>
              <el-tag size="small" type="danger" v-else-if="userInfo.gender === 2">女</el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ userInfo.birthday || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-descriptions class="margin-top" title="账号状态" :column="2" border style="margin-top: 20px;">
            <el-descriptions-item label="角色">
              <el-tag size="small" v-if="userInfo.role === 1">普通用户</el-tag>
              <el-tag size="small" type="warning" v-else-if="userInfo.role === 2">管理员</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag size="small" type="success" v-if="userInfo.status === 1">正常</el-tag>
              <el-tag size="small" type="danger" v-else-if="userInfo.status === 0">冻结</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="最后登录时间">{{ formatDate(userInfo.lastLoginTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="未找到用户信息"></el-empty>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getUserProfile } from '@/api/admin/user'

export default {
  name: 'UserDetail',
  data() {
    return {
      userId: null,
      userInfo: null,
      loading: false
    }
  },
  created() {
    this.userId = this.$route.params.id;
    if (this.userId) {
      this.fetchUserDetail();
    }
  },
  methods: {
    async fetchUserDetail() {
      this.loading = true;
      try {
        const res = await getUserProfile(this.userId);
        if (res) {
          this.userInfo = res;
        }
      } catch (error) {
        console.error('获取用户详情失败', error);
        this.$message.error('获取用户详情失败');
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      // 检查是否有历史记录，如果没有则跳回搜索页，有则返回
      if (window.history.length <= 1) {
        this.$router.push('/admin/user-search');
      } else {
        this.$router.back();
      }
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
.user-detail-container {
  padding: 0;
}

.box-card {
  border-radius: 8px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px 0;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.nickname {
  margin: 15px 0 5px;
  font-size: 20px;
  color: #303133;
}

.signature {
  font-size: 14px;
  color: #909399;
}

.margin-top {
  margin-top: 20px;
}
</style>
