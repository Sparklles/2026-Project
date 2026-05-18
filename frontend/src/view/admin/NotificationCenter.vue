<template>
  <div class="notification-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <i class="el-icon-bell"></i>
        消息中心
        <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
      </h2>
      <div class="header-actions">
        <el-button type="primary" size="small" icon="el-icon-check" @click="handleReadAll" :disabled="unreadCount === 0">
          全部已读
        </el-button>
        <el-button size="small" icon="el-icon-refresh" @click="fetchNotifications">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
        <el-radio-button label="all">全部消息</el-radio-button>
        <el-radio-button label="unread">
          未读消息
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="filter-badge" />
        </el-radio-button>
        <el-radio-button label="read">已读消息</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 消息列表 -->
    <div class="notification-list" v-loading="loading">
      <el-empty v-if="filteredList.length === 0" description="暂无消息" />
      
      <div 
        v-for="item in filteredList" 
        :key="item.id" 
        class="notification-item"
        :class="{ 'unread': item.isRead === 0 }"
        @click="handleItemClick(item)"
      >
        <!-- 左侧：头像/图标 -->
        <div class="item-left">
          <el-avatar 
            :src="item.senderAvatar || defaultAvatar" 
            :size="48"
            :icon="!item.senderAvatar ? getTypeIcon(item.type) : ''"
          />
          <div v-if="item.isRead === 0" class="unread-dot"></div>
        </div>

        <!-- 中间：内容 -->
        <div class="item-content">
          <div class="content-header">
            <span class="title">{{ item.title }}</span>
            <el-tag size="mini" :type="getTypeTagType(item.type)" class="type-tag">
              {{ getTypeText(item.type) }}
            </el-tag>
          </div>
          <div class="content-body">{{ item.content }}</div>
          <div class="content-footer">
            <span class="time">
              <i class="el-icon-time"></i>
              {{ item.createTime }}
            </span>
            <span v-if="item.bizId" class="biz-id">
              关联单号：{{ item.bizId }}
            </span>
          </div>
        </div>

        <!-- 右侧：操作 -->
        <div class="item-actions">
          <el-button 
            v-if="item.isRead === 0" 
            type="text" 
            size="small"
            @click.stop="handleReadOne(item)"
          >
            标记已读
          </el-button>
          <el-button 
            v-if="item.bizId" 
            type="primary" 
            size="small"
            plain
            @click.stop="handleViewDetail(item)"
          >
            查看详情
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-section" v-if="total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { 
  getUnreadCount, 
  getNotificationList, 
  markAsRead, 
  markAllAsRead 
} from '@/api/admin/notification'

export default {
  name: 'NotificationCenter',
  data() {
    return {
      loading: false,
      unreadCount: 0,
      notificationList: [],
      filteredList: [],
      filterType: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      // 接收者类型：2-卖家/管理员
      receiverType: 2
    }
  },
  created() {
    this.fetchUnreadCount()
    this.fetchNotifications()
  },
  methods: {
    // 获取未读消息数量
    async fetchUnreadCount() {
      try {
        const res = await getUnreadCount({ receiverType: this.receiverType })
        this.unreadCount = res || 0
      } catch (error) {
        console.error('获取未读消息数失败', error)
      }
    },

    // 获取消息列表
    async fetchNotifications() {
      this.loading = true
      try {
        const params = {
          receiverType: this.receiverType,
          page: this.currentPage,
          size: this.pageSize
        }
        const res = await getNotificationList(params)
        const data = res || {}
        this.notificationList = data.records || []
        this.total = data.total || 0
        this.applyFilter()
      } catch (error) {
        console.error('获取消息列表失败', error)
        this.$message.error('获取消息列表失败')
      } finally {
        this.loading = false
      }
    },

    // 应用筛选
    applyFilter() {
      if (this.filterType === 'all') {
        this.filteredList = this.notificationList
      } else if (this.filterType === 'unread') {
        this.filteredList = this.notificationList.filter(item => item.isRead === 0)
      } else if (this.filterType === 'read') {
        this.filteredList = this.notificationList.filter(item => item.isRead === 1)
      }
    },

    // 筛选切换
    handleFilterChange() {
      this.applyFilter()
    },

    // 分页切换
    handlePageChange(page) {
      this.currentPage = page
      this.fetchNotifications()
    },

    // 标记单条已读
    async handleReadOne(item) {
      try {
        await markAsRead(item.id)
        item.isRead = 1
        this.unreadCount = Math.max(0, this.unreadCount - 1)
        this.$message.success('已标记为已读')
        this.applyFilter()
      } catch (error) {
        console.error('标记已读失败', error)
        this.$message.error('操作失败')
      }
    },

    // 标记全部已读
    async handleReadAll() {
      try {
        await this.$confirm('确定将所有消息标记为已读吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await markAllAsRead({ receiverType: this.receiverType })
        this.notificationList.forEach(item => {
          item.isRead = 1
        })
        this.unreadCount = 0
        this.$message.success('已全部标记为已读')
        this.applyFilter()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('标记全部已读失败', error)
          this.$message.error('操作失败')
        }
      }
    },

    // 点击消息项
    handleItemClick(item) {
      if (item.isRead === 0) {
        this.handleReadOne(item)
      }
    },

    // 查看详情
    handleViewDetail(item) {
      // 根据消息类型跳转到不同页面
      if (item.type === 1 && item.bizId) {
        // 物流通知 -> 订单详情
        this.$router.push({
          path: '/admin/order-manage',
          query: { orderNo: item.bizId }
        })
      } else if (item.type === 2 && item.bizId) {
        // 退款通知 -> 退款管理
        this.$router.push(`/admin/refund-manage`)
      } else if (item.type === 3) {
        // 系统公告
        this.$message.info('系统公告：' + item.content)
      }
    },

    // 获取消息类型图标
    getTypeIcon(type) {
      const icons = {
        1: 'el-icon-truck',      // 物流通知
        2: 'el-icon-money',      // 退款通知
        3: 'el-icon-s-platform', // 系统公告
        4: 'el-icon-chat-dot-square' // 私信聊天
      }
      return icons[type] || 'el-icon-bell'
    },

    // 获取消息类型标签文字
    getTypeText(type) {
      const texts = {
        1: '物流',
        2: '退款',
        3: '系统',
        4: '私信'
      }
      return texts[type] || '其他'
    },

    // 获取消息类型标签样式
    getTypeTagType(type) {
      const types = {
        1: 'success',  // 物流 - 绿色
        2: 'warning',  // 退款 - 橙色
        3: 'info',     // 系统 - 灰色
        4: 'primary'   // 私信 - 蓝色
      }
      return types[type] || 'info'
    }
  }
}
</script>

<style scoped>
.notification-center {
  padding: 20px;
  background: #fff;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #409EFF;
  font-size: 24px;
}

.unread-badge {
  margin-left: 5px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-badge {
  margin-left: 5px;
}

.filter-badge >>> .el-badge__content {
  position: relative;
  top: -1px;
  transform: none;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
  font-size: 11px;
}

.notification-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
}

.notification-item:hover {
  background: #e4e7ed;
  transform: translateX(5px);
}

.notification-item.unread {
  background: #ecf5ff;
  border-left: 4px solid #409EFF;
}

.notification-item.unread:hover {
  background: #d9ecff;
}

.item-left {
  position: relative;
  margin-right: 15px;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #f56c6c;
  border-radius: 50%;
  border: 2px solid #fff;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.notification-item.unread .title {
  color: #409EFF;
}

.type-tag {
  flex-shrink: 0;
}

.content-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.content-footer {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.time i {
  margin-right: 4px;
}

.biz-id {
  color: #409EFF;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-left: 15px;
}

.pagination-section {
  margin-top: 30px;
  text-align: center;
}
</style>
