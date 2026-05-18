<template>
  <el-container style="height: 100vh; border: 1px solid #eee">
    <el-aside width="220px" style="background-color: #304156">
      <div class="logo-title">
        <i class="el-icon-ship"></i> 航海图书管理后台
      </div>

      <el-menu
          :default-active="$route.path"
          class="el-menu-vertical-demo"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
      >
        <el-menu-item index="/admin/book-list">
          <i class="el-icon-reading"></i>
          <span slot="title">商品数据维护 (书籍)</span>
        </el-menu-item>

        <el-menu-item index="/admin/dict-manage">
          <i class="el-icon-collection"></i>
          <span slot="title">分类与标签维护</span>
        </el-menu-item>

        <el-menu-item index="/admin/order-manage">
          <i class="el-icon-s-order"></i>
          <span slot="title">订单管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/refund-manage">
          <i class="el-icon-money"></i>
          <span slot="title">售后申请处理</span>
        </el-menu-item>

        <el-menu-item index="/admin/user-search">
          <i class="el-icon-user"></i>
          <span slot="title">用户管理与搜索</span>
        </el-menu-item>

        <el-menu-item index="/admin/user-status-audit">
          <i class="el-icon-document-checked"></i>
          <span slot="title">账号状态审计日志</span>
        </el-menu-item>

        <el-submenu index="statistics">
          <template slot="title">
            <i class="el-icon-data-analysis"></i>
            <span>数据统计报表</span>
          </template>
          <el-menu-item index="/admin/statistics-dashboard">
            <i class="el-icon-s-data"></i>
            <span slot="title">销售统计看板</span>
          </el-menu-item>
          <el-menu-item index="/admin/daily-user-report">
            <i class="el-icon-user-solid"></i>
            <span slot="title">每日新增用户</span>
          </el-menu-item>
        </el-submenu>

        <el-menu-item index="/admin/recommend-config">
          <i class="el-icon-setting"></i>
          <span slot="title">推荐规则配置</span>
        </el-menu-item>
        <el-menu-item index="/admin/review-audit">
          <i class="el-icon-chat-line-square"></i>
          <span slot="title">UGC评价风控审核</span>
        </el-menu-item>

        <el-menu-item index="/admin/notification-center">
          <i class="el-icon-bell"></i>
          <span slot="title">
            消息中心
            <el-badge
                v-if="unreadCount > 0"
                :value="unreadCount"
                :max="99"
                class="menu-badge">
            </el-badge>
          </span>
        </el-menu-item>

        <el-menu-item index="/">
          <i class="el-icon-mobile-phone"></i>
          <span slot="title">前往前台商城</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="text-align: right; font-size: 14px; background-color: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08);">
        <div v-if="adminInfo.userId" class="header-user-info">
          <span style="margin-right: 15px; font-weight: bold; color: #606266;">
            欢迎您，{{ adminInfo.nickname || '超级管理员' }}
          </span>
          <el-avatar
              size="small"
              :src="adminInfo.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              style="vertical-align: middle;"
          ></el-avatar>
          <el-button type="text" style="margin-left: 20px; color: #f56c6c;" @click="handleLogout">退出登录</el-button>
        </div>
        <div v-else class="header-user-info">
          <el-link type="primary" :underline="false" icon="el-icon-user" @click="goToLogin">您好，请登录</el-link>
        </div>
      </el-header>

      <el-main style="background-color: #f0f2f5; padding: 20px;">
        <keep-alive>
          <router-view></router-view>
        </keep-alive>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { getAdminProfile } from '@/api/admin/user'
import { getUnreadCount } from '@/api/admin/notification'

export default {
  name: 'AdminLayout',
  components: {},
  data() {
    return {
      adminInfo: {
        userId: null,
        nickname: '',
        avatarUrl: ''
      },
      unreadCount: 0,
      notificationTimer: null
    }
  },
  created() {
    this.fetchAdminInfo()
    this.fetchUnreadCount()
    this.notificationTimer = setInterval(() => {
      this.fetchUnreadCount()
    }, 30000)
  },
  beforeDestroy() {
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer)
    }
  },
  methods: {
    fetchAdminInfo() {
      const token = localStorage.getItem('admin-token')
      if (token) {
        getAdminProfile().then(res => {
          if (res) {
            this.adminInfo = res
          }
        }).catch(error => {
          console.error('获取管理员信息失败', error)
        })
      }
    },
    async fetchUnreadCount() {
      if (!localStorage.getItem('admin-token')) {
        this.unreadCount = 0
        return
      }
      try {
        const count = await getUnreadCount({ receiverType: 2 })
        this.unreadCount = count || 0
      } catch (error) {
        console.error('获取未读消息数失败', error)
      }
    },
    goToLogin() {
      this.$router.push('/admin/login')
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('admin-token')
        this.adminInfo = {
          userId: null,
          nickname: '',
          avatarUrl: ''
        }
        this.unreadCount = 0
        this.$message.success('已退出登录')
        this.$router.push('/admin/login')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.logo-title {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3643;
  border-bottom: 1px solid #1f2d3d;
}

.el-menu {
  border-right: none !important;
}

.el-header {
  line-height: 60px;
}

.header-user-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.menu-badge {
  margin-left: 8px;
}

.menu-badge ::v-deep .el-badge__content {
  border: none;
}
</style>

