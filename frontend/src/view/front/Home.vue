<template>
  <div class="mall-home">
    <header class="header-bar">
      <div class="header-content">
        <div class="logo-area">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代商城</span>
        </div>
        <div class="search-area">
          <el-input
              placeholder="搜索航海书籍、海图、器材..."
              v-model="searchKeyword"
              class="search-input"
              @keyup.enter.native="handleSearch">
            <el-button slot="append" icon="el-icon-search" class="search-btn" @click="handleSearch">搜索</el-button>
          </el-input>
          <div class="hot-search">
            <span>热门：</span>
            <a href="javascript:;" v-for="(word, idx) in hotWordList" :key="idx" @click.prevent="setHotWord(word)">{{ word }}</a>
          </div>
        </div>
        <div class="user-action">
          <el-button
              type="text"
              icon="el-icon-shopping-cart-2"
              @click="goToCart">
            购物车
          </el-button>
          <el-button v-if="!isLoggedIn" type="text" icon="el-icon-user" @click="$router.push('/login')">登录/注册</el-button>
          <el-dropdown v-else @command="handleCommand" trigger="hover">
            <el-button type="text" icon="el-icon-user" @click="openInNewTab('/user')" class="dropdown-link">
              Hi, {{ userInfo.nickname || userInfo.account || '用户' }} <i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="userCenter">账号管理</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div class="main-visual-container">
      <el-row :gutter="15">

        <el-col :span="4">
          <div class="category-menu">
            <div class="menu-title"><i class="el-icon-s-unfold"></i> 商品分类</div>
            <ul class="menu-list">
              <li v-for="(cat, index) in categoryList" :key="index" @click="handleCategory(cat)">
                <span>{{ getCategoryName(cat) }}</span>
                <i class="el-icon-arrow-right"></i>
              </li>
            </ul>
          </div>
        </el-col>

        <el-col :span="15">
          <div class="carousel-wrapper">
            <el-carousel trigger="click" height="380px" style="border-radius: 12px; overflow: hidden;">
              <el-carousel-item v-for="(item, index) in carouselList" :key="index">
                <div class="carousel-content" :style="{ backgroundImage: 'url(' + getBookImage(item) + ')' }" @click="goToDetail(getBookId(item), item)">
                  <div class="carousel-text">
                    <h2>{{ item.title }}</h2>
                    <p>{{ item.description || item.subtitle || item.author || '精选航海图书推荐' }}</p>
                    <el-button type="primary" round size="small">立即查看</el-button>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </el-col>

        <el-col :span="5">
          <div class="user-sidebar">
            <div class="user-card">
              <el-avatar :size="60" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"></el-avatar>
              <p class="greeting">Hi，水手欢迎起航！</p>
              <div class="user-btns">
                <el-button type="primary" size="mini" round @click="requireLogin('/user')">会员中心</el-button>
                <el-button size="mini" round @click="requireLogin('/user/orders')">我的订单</el-button>
              </div>
            </div>
            <div class="ad-card ad-orange">
              <h4>品质海图</h4>
              <p>官方认证 航海必备</p>
            </div>
            <div class="ad-card ad-blue">
              <h4>考证专区</h4>
              <p>船长/大副 备考指南</p>
            </div>
          </div>
        </el-col>

      </el-row>
    </div>

    <div class="recommend-container">
      <div class="theme-header">
        <h3 class="section-title"><i class="el-icon-star-on" style="color: #ff5000;"></i> 为您推荐</h3>
        <el-tabs v-model="activeTheme" @tab-click="handleThemeChange" class="theme-tabs">
          <el-tab-pane label="猜你喜欢" name="guess"></el-tab-pane>
          <el-tab-pane label="航海畅销榜" name="bestseller"></el-tab-pane>
          <el-tab-pane label="新书上架" name="newest"></el-tab-pane>
        </el-tabs>
      </div>

      <el-row :gutter="20" class="product-grid" v-loading="loading">
        <el-col :span="4" v-for="book in recommendBooks" :key="getBookKey(book)" style="margin-bottom: 20px;">
          <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }" @click.native="goToDetail(getBookId(book), book)">
            <div class="image-wrapper">
              <el-image :src="getBookImage(book)" fit="cover" class="product-img">
                <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
              </el-image>
            </div>
            <div class="product-info">
              <div class="product-title">{{ book.title }}</div>
              <div class="product-author" v-if="book.author">{{ book.author }}</div>
              <div class="product-tags">
                <span class="difficulty-tag" v-if="book.difficultyTag != null" :class="'level-' + book.difficultyTag">{{ getDifficultyText(book.difficultyTag) }}</span>
                <el-tag v-for="tag in (book.tags || [])" :key="tag" size="mini" effect="plain" class="mini-tag">{{ tag }}</el-tag>
              </div>
              <div class="product-bottom">
                <span class="price"><i>¥</i>{{ book.price }}</span>
                <span class="sales" v-if="book.sales != null">{{ book.sales }} 人付款</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="load-more">
        <el-button plain style="width: 200px;" @click="goToMoreProducts">发现更多好物</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserProfile, userLogout } from '@/api/front/user'
import { getCategories, getHomeRecommend, getPersonalized, listBooks } from '@/api/front/book'

export default {
  name: 'FrontHome',
  data() {
    return {
      searchKeyword: '',
      activeTheme: 'guess',
      loading: false,
      isLoggedIn: false,
      userInfo: {},

      categoryList: [],

      hotWordList: ['航海基础', '航海气象', '避碰', '海图', '值班', '考试', '船舶安全'],

      carouselList: [],

      allThemeData: {
        guess: [],
        bestseller: [],
        newest: []
      },

      recommendBooks: []
    }
  },
  created() {
    this.loadCategories()
    this.loadHomeData()
    this.checkLoginStatus()
  },
  methods: {
    async checkLoginStatus() {
      const token = localStorage.getItem('user-token');
      if (token) {
        try {
          const profile = await getUserProfile({ silentError: true });
          if (profile) {
            this.isLoggedIn = true;
            this.userInfo = profile;
          }
        } catch (error) {
          console.error('获取用户信息失败', error);
          localStorage.removeItem('user-token');
          this.isLoggedIn = false;
        }
      }
    },
    requireLogin(path) {
      if (!localStorage.getItem('user-token')) {
        this.$message.warning('您还未登录，请先登录！');
      } else {
        this.$router.push(path);
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
    handleSearch() {
      if (!this.searchKeyword || this.searchKeyword.trim() === '') {
        this.$message.warning('请输入搜索内容');
        return;
      }
      const route = this.$router.resolve({
        path: '/search',
        query: { keyword: this.searchKeyword }
      });
      window.open(route.href, '_blank');
    },

    setHotWord(word) {
      this.searchKeyword = word;
    },

    handleCategory(cat) {
      const route = this.$router.resolve({
        path: '/search',
        query: { categoryId: this.getCategoryId(cat), categoryName: this.getCategoryName(cat) }
      });
      window.open(route.href, '_blank');
    },

    getCategoryId(cat) {
      return cat.id || cat.categoryId || cat.value
    },

    getCategoryName(cat) {
      return cat.name || cat.categoryName || cat.label || cat.title
    },

    getFallbackCategories() {
      return [
        { id: 1, name: '基础航海理论' },
        { id: 2, name: '船舶驾驶与操纵' },
        { id: 3, name: '海洋气象学' },
        { id: 4, name: '航海仪器与应用' },
        { id: 5, name: '海图与航线设计' },
        { id: 6, name: '海事法律与法规' },
        { id: 7, name: '轮机工程基础' },
        { id: 8, name: '水手/机工实操' }
      ]
    },

    normalizeCategoryList(data) {
      const list = Array.isArray(data)
        ? data
        : (data && (data.records || data.list || data.rows || data.data))
      return Array.isArray(list) ? list.filter(item => this.getCategoryName(item)) : []
    },

    applyCategoryData(data) {
      const categories = this.normalizeCategoryList(data)
      this.categoryList = categories.length > 0 ? categories : this.getFallbackCategories()
    },

    loadCategories() {
      getCategories().then(data => {
        this.applyCategoryData(data)
      }).catch(error => {
        console.warn('商品分类接口暂无可用数据，已使用兜底分类', error)
        this.applyCategoryData([])
      })
    },
    handleThemeChange(tab) {
      this.recommendBooks = this.allThemeData[tab.name] || []
    },

    goToMoreProducts() {
      const queryMap = {
        bestseller: { sortField: 'sales', sortOrder: 'desc' },
        newest: { sortField: 'publish_date', sortOrder: 'desc' },
        guess: {}
      }
      const route = this.$router.resolve({
        path: '/search',
        query: queryMap[this.activeTheme] || {}
      })
      window.open(route.href, '_blank')
    },

    goToDetail(bookId, book) {
      if (!bookId || (book && book.__fallback)) {
        this.$message.warning('当前为首页示例推荐数据，暂无真实商品详情')
        return
      }
      const routeUrl = this.$router.resolve({
        name: 'ProductDetail',
        params: { id: bookId }
      });
      window.open(routeUrl.href, '_blank');
    },

    getDifficultyText(tag) {
      if (tag == null) return ''
      const map = { 0: '全水平', 1: '入门', 2: '中级', 3: '高级' }
      return map[tag] || ''
    },

    getBookImage(book) {
      return book.coverImageUrl || book.image || book.imageUrl || 'https://images.unsplash.com/photo-1582921017967-79d1cb6702e0?w=400&q=80'
    },

    getBookId(book) {
      return book.id || book.bookId || book.targetId
    },

    getBookKey(book) {
      return this.getBookId(book) || book.title
    },

    normalizeBookList(list) {
      return Array.isArray(list) ? list.filter(item => this.getBookId(item)) : []
    },

    getFallbackCarouselList() {
      return [
        {
          targetId: 101,
          __fallback: true,
          title: '春暖航海季 新书上架',
          subtitle: '《现代航海学基础》第4版 首发',
          image: 'https://images.unsplash.com/photo-1505228395891-9a51e7e86bf6?q=80&w=1200&auto=format&fit=crop'
        },
        {
          targetId: 102,
          __fallback: true,
          title: '探索深蓝 远洋气象',
          subtitle: '掌握海洋气候，规避航行风险',
          image: 'https://images.unsplash.com/photo-1454496522488-7a8e488e8606?q=80&w=1200&auto=format&fit=crop'
        }
      ]
    },

    getFallbackBooks() {
      return [
        { id: 101, __fallback: true, title: '现代航海学基础（第4版）', price: '68.50', sales: 1205, coverImageUrl: 'https://images.unsplash.com/photo-1582921017967-79d1cb6702e0?w=400&q=80', tags: ['年度畅销', '新手必读'] },
        { id: 102, __fallback: true, title: '远洋航海气象与海洋学', price: '88.00', sales: 842, coverImageUrl: 'https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=400&q=80', tags: ['全彩图解'] },
        { id: 103, __fallback: true, title: '船舶避碰规则精解', price: '45.00', sales: 2310, coverImageUrl: 'https://images.unsplash.com/photo-1534067783941-51c9c23ecefd?w=400&q=80', tags: ['考证必备'] },
        { id: 104, __fallback: true, title: '高级海图作业习题集', price: '32.50', sales: 450, coverImageUrl: 'https://images.unsplash.com/photo-1473186578172-c141e6798cf4?w=400&q=80', tags: ['带附件'] },
        { id: 105, __fallback: true, title: '国际海事公约汇编', price: '120.00', sales: 128, coverImageUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=400&q=80', tags: ['船长推荐'] },
        { id: 106, __fallback: true, title: '水手长实操手册', price: '55.00', sales: 960, coverImageUrl: 'https://images.unsplash.com/photo-1605281317010-e2583ffc6178?w=400&q=80', tags: ['包邮'] }
      ]
    },

    hasHomeRecommendData(data) {
      if (!data) return false
      return [
        data.popular,
        data.bestseller,
        data.bestSeller,
        data.newBooks,
        data.newest,
        data.newBookList,
        data.homeTopic,
        data.carouselList,
        data.bannerList
      ].some(list => this.normalizeBookList(list).length > 0)
    },

    getListData(data) {
      return data && (data.records || data.list || data.rows || data.data || data)
    },

    async loadCatalogFallbackData() {
      try {
        const data = await listBooks({ current: 1, size: 6 })
        const books = this.normalizeBookList(this.getListData(data))
        if (books.length > 0) {
          this.carouselList = books.slice(0, 2)
          this.allThemeData = {
            guess: books,
            bestseller: books.slice().sort((a, b) => (b.sales || 0) - (a.sales || 0)),
            newest: books.slice().reverse()
          }
          this.recommendBooks = this.allThemeData[this.activeTheme] || []
          return
        }
      } catch (error) {
        console.warn('商品列表接口暂无可用数据，已使用首页示例数据', error)
      }
      this.applyHomeData({})
    },

    applyHomeData(data) {
      const fallbackBooks = this.getFallbackBooks()
      const popular = this.normalizeBookList(data && (data.popular || data.bestseller || data.bestSeller))
      const newest = this.normalizeBookList(data && (data.newBooks || data.newest || data.newBookList))
      const homeTopic = this.normalizeBookList(data && (data.homeTopic || data.carouselList || data.bannerList))
      const publicGuess = popular.length > 0 ? popular : newest

      this.carouselList = homeTopic.length > 0 ? homeTopic : this.getFallbackCarouselList()
      this.allThemeData = {
        guess: publicGuess.length > 0 ? publicGuess : fallbackBooks,
        bestseller: popular.length > 0 ? popular : fallbackBooks.slice().sort((a, b) => b.sales - a.sales),
        newest: newest.length > 0 ? newest : fallbackBooks.slice().reverse()
      }
      this.recommendBooks = this.allThemeData[this.activeTheme] || []
    },

    async loadPersonalizedRecommend() {
      if (!localStorage.getItem('user-token')) {
        return
      }

      try {
        const data = await getPersonalized()
        const personalized = this.normalizeBookList(this.getListData(data))
        if (personalized.length > 0) {
          this.allThemeData.guess = personalized
          if (this.activeTheme === 'guess') {
            this.recommendBooks = personalized
          }
        }
      } catch (error) {
        console.warn('个性化推荐接口暂无可用数据，已使用公开推荐数据', error)
      }
    },

    async loadHomeData() {
      this.loading = true
      try {
        const data = await getHomeRecommend()
        if (this.hasHomeRecommendData(data)) {
          this.applyHomeData(data)
        } else {
          await this.loadCatalogFallbackData()
        }
        await this.loadPersonalizedRecommend()
      } catch (error) {
        console.warn('首页推荐接口暂无可用数据，已使用兜底数据', error)
        await this.loadCatalogFallbackData()
        await this.loadPersonalizedRecommend()
      } finally {
        this.loading = false
      }
    },
    async handleCommand(command) {
      if (command === 'userCenter') {
        this.openInNewTab('/user');
      } else if (command === 'logout') {
        try {
          await this.$confirm('确定要退出登录吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });

          await userLogout().catch(() => {});

          localStorage.removeItem('user-token');

          this.isLoggedIn = false;
          this.userInfo = {};
          this.$message.success('已成功退出登录');

          if (this.$route.path !== '/') {
            this.$router.push('/');
          } else {
            location.reload();
          }
        } catch (cancel) {
          // 用户取消退出
        }
      }
    }
  }
}
</script>

<style scoped>
.mall-home {
  background-color: #f4f4f4;
  min-height: 100vh;
  padding-bottom: 40px;
}

.header-bar {
  background-color: #fff;
  padding: 20px 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.header-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo-area {
  display: flex;
  align-items: center;
  color: #1890ff;
  cursor: pointer;
}
.logo-icon {
  font-size: 36px;
  margin-right: 10px;
}
.logo-text {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 1px;
}
.user-action {
  display: flex;
  align-items: center;
}
.dropdown-link {
  margin-left: 15px;
  cursor: pointer;
}
.search-area {
  width: 500px;
}
.search-input {
  border: 2px solid #ff5000;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow: none;
}
.search-input ::v-deep .el-input__inner {
  border: none;
  border-radius: 0;
  height: 40px;
  box-shadow: none;
}
.search-input ::v-deep .el-input-group__append {
  background: #ff5000;
  border: none;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  width: 58px;
}
.search-btn {
  background-color: #ff5000 !important;
  color: #fff !important;
  border: none !important;
  border-radius: 0;
  height: 40px;
  width: 78px;
  font-size: 15px;
  box-shadow: none !important;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  overflow: hidden;
  white-space: nowrap;
}
.search-btn ::v-deep i {
  margin-right: 0;
}
.hot-search {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}
.hot-search a {
  color: #666;
  text-decoration: none;
  margin-right: 10px;
}
.hot-search a:hover {
  color: #ff5000;
}

.main-visual-container {
  width: 1200px;
  margin: 20px auto;
}
.category-menu {
  background-color: #fff;
  border-radius: 12px;
  height: 380px;
  padding: 10px 0;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.menu-title {
  padding: 10px 20px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}
.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
.menu-list li {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
}
.menu-list li:hover {
  background-color: #e6f7ff;
  color: #1890ff;
  padding-left: 25px;
}

.carousel-content {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  cursor: pointer;
}
.carousel-text {
  position: absolute;
  top: 50px;
  left: 50px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}
.carousel-text h2 {
  font-size: 36px;
  margin: 0 0 10px 0;
}
.carousel-text p {
  font-size: 18px;
  margin: 0 0 20px 0;
}

.user-sidebar {
  display: flex;
  flex-direction: column;
  height: 380px;
  justify-content: space-between;
}
.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.greeting {
  font-size: 14px;
  color: #333;
  margin: 10px 0 15px 0;
  font-weight: bold;
}
.ad-card {
  height: 85px;
  border-radius: 12px;
  padding: 15px;
  color: #fff;
  box-sizing: border-box;
  cursor: pointer;
  transition: transform 0.2s;
}
.ad-card:hover {
  transform: translateY(-3px);
}
.ad-card h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}
.ad-card p {
  margin: 0;
  font-size: 12px;
  opacity: 0.9;
}
.ad-orange {
  background: linear-gradient(135deg, #ff7e5f, #feb47b);
}
.ad-blue {
  background: linear-gradient(135deg, #00c6ff, #0072ff);
}

.recommend-container {
  width: 1200px;
  margin: 0 auto;
}
.theme-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.section-title {
  margin: 0 30px 0 0;
  font-size: 22px;
  color: #333;
}
.theme-tabs ::v-deep .el-tabs__nav-wrap::after {
  display: none;
}
.theme-tabs ::v-deep .el-tabs__item {
  font-size: 16px;
  height: 50px;
  line-height: 50px;
}

.product-card {
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1) !important;
}
.image-wrapper {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background-color: #f9f9f9;
}
.product-img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}
.product-card:hover .product-img {
  transform: scale(1.05);
}
.product-info {
  padding: 12px;
}
.product-title {
  font-size: 14px;
  color: #333;
  line-height: 20px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 4px;
}
.product-author {
  font-size: 12px;
  color: #888;
  margin-bottom: 6px;
}
.product-tags {
  height: 24px;
  overflow: hidden;
  margin-bottom: 8px;
}
.mini-tag {
  color: #ff5000;
  border-color: #ffcccc;
  background-color: #fff5f0;
  margin-right: 4px;
  border-radius: 4px;
}
.difficulty-tag {
  display: inline-block;
  font-size: 11px;
  padding: 0 4px;
  border-radius: 2px;
  color: #fff;
  line-height: 16px;
  margin-right: 4px;
}
.level-0 { background-color: #909399; }
.level-1 { background-color: #67c23a; }
.level-2 { background-color: #e6a23c; }
.level-3 { background-color: #f56c6c; }
.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: bottom;
  margin-top: 5px;
}
.price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
}
.price i {
  font-size: 12px;
  font-style: normal;
  margin-right: 2px;
}
.sales {
  color: #999;
  font-size: 12px;
  line-height: 24px;
}
.load-more {
  text-align: center;
  margin-top: 30px;
}
</style>








