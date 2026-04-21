<template>
  <div id="app">
    <el-container style="height: 100vh; border: 1px solid #eee">
      <el-aside width="220px" style="background-color: #304156">
        <div class="logo-title">
          <i class="el-icon-ship"></i> 航海图书管理后台
        </div>

        <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
            @select="handleSelect"
        >
          <el-menu-item index="BookList">
            <i class="el-icon-reading"></i>
            <span slot="title">商品数据维护 (书籍)</span>
          </el-menu-item>

          <el-menu-item index="ReviewAudit">
            <i class="el-icon-chat-line-square"></i>
            <span slot="title">UGC评价风控审核</span>
          </el-menu-item>

          <el-menu-item index="FrontPreview" disabled>
            <i class="el-icon-mobile-phone"></i>
            <span slot="title">前往前台商城 (开发中)</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header style="text-align: right; font-size: 14px; background-color: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08);">
          <span style="margin-right: 15px; font-weight: bold; color: #606266;">
            欢迎您，超级管理员
          </span>
          <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="vertical-align: middle;"></el-avatar>
        </el-header>

        <el-main style="background-color: #f0f2f5; padding: 20px;">
          <keep-alive>
            <component :is="activeMenu"></component>
          </keep-alive>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
// 引入我们之前写好的两个核心页面组件
import BookList from '@/view/admin/BookList.vue'
import ReviewAudit from '@/view/admin/ReviewAudit.vue'

export default {
  name: 'App',
  components: {
    BookList,
    ReviewAudit
  },
  data() {
    return {
      // 默认打开的页面是商品管理页
      activeMenu: 'BookList'
    }
  },
  methods: {
    // 处理菜单点击切换事件
    handleSelect(key) {
      this.activeMenu = key
    }
  }
}
</script>

<style>
/* 全局样式重置与基础配置 */
body {
  margin: 0;
  padding: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

#app {
  height: 100vh;
}

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
</style>