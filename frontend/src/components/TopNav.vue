<template>
  <div class="site-top-nav">
    <div class="nav-container">
      <div class="nav-left">
        <span class="nav-item location">
          <i class="el-icon-location-outline"></i> 中国大陆
        </span>
        <span class="nav-item welcome" v-if="!isLoggedIn">
          <router-link to="/login" class="login-link">亲，请登录</router-link>
          <router-link to="/register" class="register-link">免费注册</router-link>
        </span>
        <span class="nav-item" v-else>
          欢迎回来，航海家！
        </span>
      </div>
      
      <div class="nav-right">
        <router-link to="/" class="nav-item">商城首页</router-link>
        
        <el-dropdown class="nav-item-dropdown" @command="handleCommand" trigger="hover">
          <span class="nav-item el-dropdown-link">
            我的商城 <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="orders" style="font-size: 12px; line-height: 28px; padding: 0 15px;">已买到的宝贝</el-dropdown-item>
            <el-dropdown-item command="favorites" style="font-size: 12px; line-height: 28px; padding: 0 15px;">我的收藏</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>

        <span class="nav-item cart" @click="goToCart">
          <i class="el-icon-shopping-cart-2"></i> 购物车
        </span>

        <!-- 用户名下拉组件放置在最右侧 -->
        <div class="nav-item user-dropdown" v-if="isLoggedIn">
          <el-dropdown @command="handleCommand" trigger="hover">
            <span class="el-dropdown-link user-name">
              <i class="el-icon-user"></i> {{ userInfo.nickname || userInfo.account || '用户' }} <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="userCenter" style="font-size: 12px; line-height: 28px; padding: 0 15px;">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" style="font-size: 12px; line-height: 28px; padding: 0 15px; border-top: 1px solid #ebeef5;">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserProfile } from '@/api/front/user';

export default {
  name: 'TopNav',
  data() {
    return {
      isLoggedIn: false,
      userInfo: {}
    }
  },
  created() {
    this.checkLoginStatus();
  },
  methods: {
    async checkLoginStatus() {
      const token = localStorage.getItem('user-token');
      if (token) {
        try {
          const res = await getUserProfile();
          if (res) {
            this.isLoggedIn = true;
            this.userInfo = res;
          }
        } catch (error) {
          localStorage.removeItem('user-token');
          this.isLoggedIn = false;
        }
      }
    },
    goToCart() {
      const routeData = this.$router.resolve({ path: '/cart' });
      window.open(routeData.href, '_blank');
    },
    openInNewTab(path) {
      const routeData = this.$router.resolve({ path });
      window.open(routeData.href, '_blank');
    },
    handleCommand(command) {
      if (command === 'logout') {
        localStorage.removeItem('user-token');
        this.$message.success('已退出登录');
        this.isLoggedIn = false;
        this.userInfo = {};
        if (this.$route.path !== '/') {
          this.$router.push('/');
        }
      } else if (command === 'userCenter') {
        this.openInNewTab('/user');
      } else if (command === 'orders') {
        this.openInNewTab('/user/orders');
      } else if (command === 'favorites') {
        this.openInNewTab('/user/favorites');
      }
    }
  }
}
</script>

<style scoped>
.site-top-nav {
  height: 35px;
  line-height: 35px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #eee;
  font-size: 12px;
  color: #6c6c6c;
}
.nav-container {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
}
.nav-item {
  margin: 0 10px;
  color: #6c6c6c;
  text-decoration: none;
  cursor: pointer;
}
.nav-item:hover {
  color: #ff5000;
}
.nav-left .login-link {
  color: #ff5000;
  margin-right: 10px;
  text-decoration: none;
}
.nav-left .register-link {
  color: #6c6c6c;
  text-decoration: none;
}
.nav-left .register-link:hover {
  color: #ff5000;
}
.nav-right {
  display: flex;
  align-items: center;
}
.nav-item-dropdown {
  margin: 0 10px;
}
.el-dropdown-link {
  font-size: 12px;
  color: #6c6c6c;
  cursor: pointer;
}
.el-dropdown-link:hover {
  color: #ff5000;
}
.user-dropdown {
  margin-left: 15px;
}
.user-name {
  font-weight: bold;
}
.cart {
  color: #ff5000;
}
.cart i {
  margin-right: 3px;
}
</style>
