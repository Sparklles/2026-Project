<!-- Search.vue -->
<template>
  <div class="search-page">
    <top-nav></top-nav>
    <header class="search-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代</span>
        </div>

        <div class="search-box-wrap">
          <div class="search-input-group">
            <el-input
                v-model="keyword"
                placeholder="搜索航海书籍、海图、仪器..."
                clearable
                @keyup.enter.native="handleSearch">
            </el-input>
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>

          <div class="suggest-keywords">
            <span class="suggest-label">猜你想搜：</span>
            <span
                class="s-word"
                v-for="(word, index) in hotWords"
                :key="index"
                @click="fillKeywordOnly(word)">
              {{ word }}
            </span>
          </div>
        </div>
      </div>
    </header>

    <div class="main-container">
      <div class="filter-header">
        <el-button type="text" icon="el-icon-s-grid" @click="resetFilter">查看全部</el-button>
      </div>

      <div class="filter-panel">
        <el-button type="text" @click="filterVisible = !filterVisible" style="margin-bottom:10px;">
          <i :class="filterVisible ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
          高级筛选
        </el-button>
        <el-form v-if="filterVisible" :inline="true" size="small" class="filter-form">
          <el-form-item label="分类">
            <el-select v-model="filters.categoryId" placeholder="全部分类" clearable>
              <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="适用水平">
            <el-select v-model="filters.difficultyTag" placeholder="全部水平" clearable>
              <el-option label="全年龄段/所有水平" :value="0"></el-option>
              <el-option label="入门" :value="1"></el-option>
              <el-option label="中级" :value="2"></el-option>
              <el-option label="高级" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <!-- 出版年份：两个互斥复选框 -->
          <el-form-item label="出版年份">
            <el-checkbox
                :value="yearMode === 'range'"
                @change="(val) => toggleYearMode('range', val)">
              年份范围
            </el-checkbox>
            <el-checkbox
                :value="yearMode === 'single'"
                @change="(val) => toggleYearMode('single', val)"
                style="margin-left:12px;">
              单一年份
            </el-checkbox>
            <!-- 年份范围模式 -->
            <template v-if="yearMode === 'range'">
              <el-select v-model="filters.minYear" placeholder="起始年" clearable style="width:100px;margin-left:10px;">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y"></el-option>
              </el-select>
              <span style="margin:0 5px;">-</span>
              <el-select v-model="filters.maxYear" placeholder="结束年" clearable style="width:100px;">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y"></el-option>
              </el-select>
            </template>
            <!-- 单一年份模式 -->
            <template v-if="yearMode === 'single'">
              <el-select v-model="filters.singleYear" placeholder="选择年份" clearable style="width:120px;margin-left:10px;">
                <el-option v-for="y in yearOptions" :key="y" :label="y" :value="y"></el-option>
              </el-select>
            </template>
          </el-form-item>
          <el-form-item label="价格">
            <el-input-number v-model="filters.minPrice" :min="0" placeholder="最低价" controls-position="right" style="width:120px;"></el-input-number>
            <span style="margin:0 5px;">-</span>
            <el-input-number v-model="filters.maxPrice" :min="0" placeholder="最高价" controls-position="right" style="width:120px;"></el-input-number>
          </el-form-item>
          <el-form-item label="书名">
            <el-input v-model="filters.title" placeholder="输入书名关键字" clearable></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="filters.author" placeholder="输入作者名" clearable></el-input>
          </el-form-item>
          <el-form-item label="航行地区">
            <el-input v-model="filters.region" placeholder="如太平洋、加勒比海" clearable></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="applyFilter">确定筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="filter-bar">
        <div class="sort-group">
          <span :class="['sort-item', currentSortIndex === index ? 'active' : '']"
                v-for="(sort, index) in sortOptions" :key="index"
                @click="toggleSort(index)">
            {{ sort.label }}
            <i v-if="currentSortIndex === index" :class="sortOrder === 'asc' ? 'el-icon-caret-top' : 'el-icon-caret-bottom'"></i>
          </span>
        </div>
        <div class="filter-right">
          <span class="total-count">共找到 <span class="highlight">{{ total }}</span> 件相关商品</span>
        </div>
      </div>

      <div class="product-grid" v-loading="loading">
        <div class="product-card" v-for="item in productList" :key="item.id" @click="goToDetail(item.id)">
          <div class="p-img-wrap">
            <el-image :src="item.image" fit="cover" lazy>
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
            </el-image>
          </div>
          <div class="p-info">
            <div class="p-title">
              <span class="mall-tag" v-if="item.isSelf">官方直营</span>
              {{ item.title }}
            </div>
            <div class="p-price-row">
              <div class="price-box">
                <span class="symbol">¥</span>
                <span class="int-part">{{ getPriceParts(item.price).intPart }}</span>
                <span class="dec-part">.{{ getPriceParts(item.price).decPart }}</span>
              </div>
              <div class="sales-box">{{ item.sales }}人付款</div>
              <div class="loc-box">{{ item.location }}</div>
            </div>
            <div class="p-stats" v-if="item.avgRating != null || item.reviewCount != null || item.favoriteCount != null">
              <span class="stat-item rating-stat" v-if="item.avgRating != null" :class="{ 'highlight-stat': isRatingSorted }">
                <i class="el-icon-star-on"></i> {{ item.avgRating.toFixed(1) }}
              </span>
              <span class="stat-item review-count" v-if="item.reviewCount != null">({{ item.reviewCount }}条评价)</span>
              <span class="stat-item favorite-count" v-if="item.favoriteCount != null" :class="{ 'highlight-stat': isFavoriteSorted }">
                <i class="el-icon-star-off"></i> {{ item.favoriteCount }}
              </span>
            </div>
            <div class="p-tags" v-if="(item.tags && item.tags.length) || (item.difficultyTag != null)">
              <span class="difficulty-tag" v-if="item.difficultyTag != null" :class="'level-' + item.difficultyTag">
                {{ getDifficultyText(item.difficultyTag) }}
              </span>
              <span class="service-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</span>
            </div>
            <div class="p-shop-row">
              <div class="shop-name"><i class="el-icon-s-shop"></i> {{ item.shopName }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="pageSizes"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { searchBooks, listBooks, getCategories } from '@/api/front/book'
import TopNav from "@/components/TopNav.vue";

export default {
  name: 'SearchPage',
  components: {
    TopNav
  },
  data() {
    return {
      keyword: '',
      currentCategoryName: '',
      filterVisible: false,
      yearMode: null,
      loading: false,
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50],
      total: 0,
      productList: [],
      categoryOptions: [],
      filters: {
        categoryId: null,
        difficultyTag: null,
        minYear: null,
        maxYear: null,
        singleYear: null,
        minPrice: null,
        maxPrice: null,
        title: '',
        author: '',
        region: ''
      },
      currentSortIndex: 0,
      sortOrder: 'desc',
      sortOptions: [
        { label: '综合排序', field: '' },
        { label: '销量', field: 'sales' },
        { label: '价格', field: 'price' },
        { label: '评分', field: 'avg_rating' },
        { label: '收藏量', field: 'favorite_count' },
        { label: '新品', field: 'publish_date' }
      ],
      hotWords: ['航海基础', '航海气象', '避碰', '海图', '值班', '考试', '船舶安全'],
      yearOptions: [],
      queryType: 'list' // 'search' 或 'list'
    }
  },
  computed: {
    isRatingSorted() {
      return this.currentSortIndex === 3 && this.sortOrder === 'desc'
    },
    isFavoriteSorted() {
      return this.currentSortIndex === 4 && this.sortOrder === 'desc'
    }
  },
  created() {
    this.keyword = this.$route.query.keyword || ''
    const categoryId = this.$route.query.categoryId
    if (categoryId) {
      this.filters.categoryId = Number(categoryId)
      this.queryType = 'list'
    } else if (this.keyword) {
      this.queryType = 'search'
    }
    this.applyRouteSort()
    this.initYearOptions()
    this.fetchCategories()
    this.loadData()
  },
  methods: {
    applyRouteSort() {
      const sortField = this.$route.query.sortField
      const sortOrder = this.$route.query.sortOrder
      if (!sortField) return

      const sortIndex = this.sortOptions.findIndex(item => item.field === sortField)
      if (sortIndex === -1) return

      this.currentSortIndex = sortIndex
      this.sortOrder = sortOrder === 'asc' ? 'asc' : 'desc'
      this.queryType = 'list'
    },

    initYearOptions() {
      const currentYear = new Date().getFullYear()
      for (let y = currentYear; y >= 1900; y--) {
        this.yearOptions.push(y)
      }
    },
    async fetchCategories() {
      try {
        const data = await getCategories()
        this.categoryOptions = data || []
      } catch (e) {
        console.warn('获取分类失败', e)
      }
    },
    // 切换出版年份模式（两个复选框互斥）
    toggleYearMode(mode, val) {
      if (val) {
        // 选中当前模式，自动取消另一个
        this.yearMode = mode
      } else {
        // 取消选中
        this.yearMode = null
      }
    },
    // 检查高级筛选条件是否有值（排除 title，因为 title 会被 keyword 替代）
    hasAdvancedFilter() {
      const { categoryId, difficultyTag, minYear, maxYear, minPrice, maxPrice, author, region } = this.filters
      return !!(categoryId !== null && categoryId !== '') ||
          !!(difficultyTag !== null && difficultyTag !== '') ||
          !!(minYear !== null && minYear !== '') ||
          !!(maxYear !== null && maxYear !== '') ||
          !!(minPrice !== null && minPrice !== '') ||
          !!(maxPrice !== null && maxPrice !== '') ||
          !!(author && author.trim()) ||
          !!(region && region.trim())
    },
    // 清空所有高级筛选条件（UI 和内部数据）
    clearAdvancedFilters() {
      this.filters = {
        categoryId: null,
        difficultyTag: null,
        minYear: null,
        maxYear: null,
        singleYear: null,
        minPrice: null,
        maxPrice: null,
        title: '',
        author: '',
        region: ''
      }
      this.yearMode = null
      // 清空分类名称显示
      this.currentCategoryName = ''
    },
    // 仅填充关键词到搜索框，不发起请求
    fillKeywordOnly(word) {
      this.keyword = word
    },
    // 执行搜索框搜索（先清空高级筛选，再调用 searchBooks）
    handleSearch() {
      if (!this.keyword.trim()) return
      // 如果存在任何高级筛选条件，先清空
      if (this.hasAdvancedFilter()) {
        this.clearAdvancedFilters()
      }
      // 重置页码
      this.currentPage = 1
      // 标记为搜索类型
      this.queryType = 'search'
      // 发起搜索
      this.loadData()
      // 更新 URL
      this.$router.push({ path: '/search', query: { keyword: this.keyword } }).catch(() => {})
    },
    // 高级筛选确定按钮
    applyFilter() {
      // 使用高级筛选时，清空关键词搜索框的值
      this.keyword = ''
      this.queryType = 'list'
      this.currentPage = 1
      this.loadData()
      // 清除 URL 中的 keyword 参数
      if (this.$route.query.keyword) {
        this.$router.push({ path: '/search', query: {} }).catch(() => {})
      }
    },
    // 重置所有筛选（高级筛选 + 关键词）
    resetFilter() {
      this.clearAdvancedFilters()
      this.keyword = ''
      this.queryType = 'list'
      this.currentPage = 1
      this.loadData()
      this.$router.push({ path: '/search' }).catch(() => {})
    },
    // 排序切换
    toggleSort(index) {
      if (this.currentSortIndex === index) {
        this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc'
      } else {
        this.currentSortIndex = index
        this.sortOrder = this.sortOptions[index].field === 'price' ? 'asc' : 'desc'
      }
      this.currentPage = 1
      this.loadData()
    },
    // 构建分页与排序基础参数
    buildBaseParams() {
      return {
        current: this.currentPage,
        size: this.pageSize,
        sortField: this.sortOptions[this.currentSortIndex].field,
        sortOrder: this.sortOrder
      }
    },
    // 构建 searchBooks 参数
    buildSearchParams() {
      const params = this.buildBaseParams()
      params.keyword = this.keyword
      return params
    },
    // 构建 listBooks 参数（高级筛选 + 排序）
    buildListParams() {
      const params = this.buildBaseParams()
      // 只有当 keyword 存在且没有高级筛选时，listBooks 才可能带 keyword，但目前根据逻辑，调用 listBooks 时 keyword 应为空
      if (this.keyword && !this.hasAdvancedFilter()) {
        params.keyword = this.keyword
      }
      // 添加筛选条件
      if (this.filters.categoryId !== null && this.filters.categoryId !== '') params.categoryId = this.filters.categoryId
      if (this.filters.difficultyTag !== null && this.filters.difficultyTag !== '') params.difficultyTag = this.filters.difficultyTag
      if (this.yearMode === 'range') {
        if (this.filters.minYear !== null && this.filters.minYear !== '') params.minYear = this.filters.minYear
        if (this.filters.maxYear !== null && this.filters.maxYear !== '') params.maxYear = this.filters.maxYear
      } else if (this.yearMode === 'single') {
        if (this.filters.singleYear !== null && this.filters.singleYear !== '') {
          params.minYear = this.filters.singleYear
          params.maxYear = this.filters.singleYear
        }
      }
      if (this.filters.minPrice !== null && this.filters.minPrice !== '' && this.filters.minPrice !== 0) params.minPrice = this.filters.minPrice
      if (this.filters.maxPrice !== null && this.filters.maxPrice !== '' && this.filters.maxPrice !== 0) params.maxPrice = this.filters.maxPrice
      if (this.filters.title) params.title = this.filters.title
      if (this.filters.author) params.author = this.filters.author
      if (this.filters.region) params.region = this.filters.region
      return params
    },
    loadData() {
      // 校验年份范围（仅范围模式需要校验）
      if (this.yearMode === 'range' && this.filters.minYear && this.filters.maxYear && this.filters.minYear >
          this.filters.maxYear) {
        this.$message.warning('起始年份不能大于结束年份')
        return
      }
      // 非范围模式下，清空范围字段以避免干扰
      if (this.yearMode !== 'range') {
        this.filters.minYear = null
        this.filters.maxYear = null
      }
      // 非单一年份模式下，清空单一年份字段
      if (this.yearMode !== 'single') {
        this.filters.singleYear = null
      }

      this.loading = true
      let requestPromise

      if (this.queryType === 'search' && this.keyword) {
        const searchParams = this.buildSearchParams()
        requestPromise = searchBooks(searchParams)
      } else {
        const listParams = this.buildListParams()
        requestPromise = listBooks(listParams)
      }

      requestPromise.then(data => {
        const records = data.records || []
        if (records.length === 0) {
          this.productList = []
          this.total = 0
        } else {
          this.productList = records.map(this.mapBookToProduct)
          this.total = data.total || 0
        }
      }).catch(() => {
        // 模拟降级数据
        this.productList = this.generateMockData(this.keyword)
        this.total = this.productList.length
      }).finally(() => {
        this.loading = false
        window.scrollTo({ top: 0, behavior: 'smooth' })
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.loadData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadData()
    },
    goToDetail(id) {
      const routeUrl = this.$router.resolve({
        name: 'ProductDetail',
        params: { id: id }
      });
      window.open(routeUrl.href, '_blank');
    },
    mapBookToProduct(item) {
      return {
        id: item.id,
        title: item.title,
        image: item.coverImageUrl || '',
        price: Number(item.price || 0).toFixed(2),
        sales: item.sales || 0,
        location: item.location || '出版社',
        isSelf: item.isSelf || false,
        tags: item.tags || [],
        shopName: item.shopName || '官方旗舰店',
        difficultyTag: item.difficultyTag != null ? item.difficultyTag : null,
        avgRating: item.avgRating != null ? Number(item.avgRating) : null,
        reviewCount: item.reviewCount != null ? Number(item.reviewCount) : null,
        favoriteCount: item.favoriteCount != null ? Number(item.favoriteCount) : null
      }
    },
    getDifficultyText(tag) {
      if (tag == null) return ''
      const map = { 0: '全水平', 1: '入门', 2: '中级', 3: '高级' }
      return map[tag] || ''
    },
    getPriceParts(price) {
      const parts = Number(price).toFixed(2).split('.')
      return {
        intPart: parts[0],
        decPart: parts[1]
      }
    },
    generateMockData(keyword) {
      const locations = ['江苏 扬州', '上海', '浙江 舟山', '山东 青岛', '广东 广州']
      const shops = ['海大出版社专营店', '润金图书专营店', '远洋航海官方旗舰店', '水手之家企业店']
      let arr = []
      for (let i = 0; i < 20; i++) {
        arr.push({
          id: 1000 + i,
          title: `【官方正版新书】${keyword || '航海'} 全彩图解第${(i%5)+1}版 附微课视频与习题解析 考证必刷`,
          image: `https://images.unsplash.com/photo-1590486803833-1c5dc8ddd4c8?w=300&q=80`,
          price: (Math.random() * 80 + 20).toFixed(2),
          sales: Math.floor(Math.random() * 5000) + 10,
          location: locations[i % locations.length],
          isSelf: i % 3 === 0,
          tags: ['退货宝', '包邮', i % 4 === 0 ? '回头客3千' : '正版险'].filter(Boolean),
          shopName: shops[i % shops.length],
          difficultyTag: Math.floor(Math.random() * 4),
          avgRating: parseFloat((Math.random() * 2 + 3).toFixed(1)),
          reviewCount: Math.floor(Math.random() * 500) + 10,
          favoriteCount: Math.floor(Math.random() * 2000) + 100
        })
      }
      return arr
    }
  }
}
</script>

<style scoped>
/* 样式保持不变，与原 Search.vue 完全一致 */
.search-page {
  background-color: #f4f4f4;
  min-height: 100vh;
  padding-bottom: 60px;
}
.search-header {
  background-color: #fff;
  padding: 25px 0 15px 0;
  box-shadow: 0 1px 5px rgba(0,0,0,0.05);
}
.header-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
}
.logo-area {
  display: flex;
  align-items: center;
  color: #1890ff;
  cursor: pointer;
  margin-right: 80px;
  margin-top: 5px;
}
.logo-icon {
  font-size: 40px;
  margin-right: 12px;
}
.logo-text {
  font-size: 28px;
  font-weight: bold;
}
.search-box-wrap {
  flex: 1;
  max-width: 700px;
}
.search-input-group {
  display: flex;
  border: 2px solid #ff5000;
  border-radius: 12px;
  overflow: hidden;
  height: 44px;
}
.search-input-group ::v-deep .el-input__inner {
  border: none;
  height: 100%;
  font-size: 16px;
  padding-left: 20px;
}
.search-input-group ::v-deep .el-input__inner:focus {
  outline: none;
}
.search-btn {
  width: 90px;
  background-color: #ff5000;
  color: #fff;
  border: none;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  letter-spacing: 2px;
}
.search-btn:hover {
  background-color: #e64800;
}
.suggest-keywords {
  margin-top: 10px;
  font-size: 13px;
  color: #666;
}
.suggest-label {
  color: #999;
}
.s-word {
  margin-right: 15px;
  cursor: pointer;
  transition: color 0.2s;
}
.s-word:hover {
  color: #ff5000;
}
.main-container {
  width: 1200px;
  margin: 20px auto;
}
.filter-header {
  margin-bottom: 8px;
}
.filter-panel {
  background: #fff;
  padding: 10px 20px;
  border: 1px solid #e8e8e8;
  margin-bottom: 20px;
}
.filter-form {
  margin-top: 10px;
}
.sort-group {
  display: flex;
  align-items: center;
}
.filter-bar {
  background: #fff;
  padding: 12px 20px;
  border: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-bottom: 20px;
}
.sort-item {
  margin-right: 30px;
  cursor: pointer;
  padding: 4px 8px;
}
.sort-item:hover {
  color: #ff5000;
}
.sort-item.active {
  color: #fff;
  background-color: #ff5000;
  border-radius: 2px;
}
.filter-right {
  margin-left: auto;
}
.highlight {
  color: #ff5000;
  font-weight: bold;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
}
.product-card {
  background: #fff;
  border: 1px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
  cursor: pointer;
  padding-bottom: 10px;
}
.product-card:hover {
  border-color: #ff5000;
  box-shadow: 0 4px 12px rgba(255, 80, 0, 0.15);
}
.p-img-wrap {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  background: #f9f9f9;
}
.p-img-wrap .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s;
}
.product-card:hover .el-image {
  transform: scale(1.03);
}
.p-info {
  padding: 10px;
}
.p-title {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 2px;
}
.mall-tag {
  background-color: #e61414;
  color: #fff;
  font-size: 11px;
  padding: 1px 4px;
  border-radius: 2px;
  margin-right: 4px;
  vertical-align: top;
}
.p-title:hover {
  color: #ff5000;
  text-decoration: underline;
}
.p-price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}
.price-box {
  color: #ff5000;
  margin-right: 8px;
}
.price-box .symbol {
  font-size: 14px;
}
.price-box .int-part {
  font-size: 22px;
  font-weight: bold;
}
.price-box .dec-part {
  font-size: 14px;
  font-weight: bold;
}
.sales-box {
  font-size: 12px;
  color: #999;
}
.loc-box {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}
.p-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}
.stat-item {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}
.rating-stat {
  color: #ff9600;
}
.rating-stat i,
.favorite-count i {
  font-size: 14px;
  margin-right: 2px;
}
.review-count {
  color: #999;
}
.favorite-count {
  color: #999;
}
.highlight-stat {
  font-weight: bold;
  color: #ff5000 !important;
}
.favorite-count.highlight-stat {
  color: #ff5000 !important;
}
.p-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 10px;
}
.difficulty-tag {
  display: inline-block;
  font-size: 11px;
  padding: 0 4px;
  border-radius: 2px;
  color: #fff;
  line-height: 16px;
}
.level-0 { background-color: #909399; }
.level-1 { background-color: #67c23a; }
.level-2 { background-color: #e6a23c; }
.level-3 { background-color: #f56c6c; }
.service-tag {
  font-size: 11px;
  color: #ff5000;
  border: 1px solid #ff5000;
  padding: 0 4px;
  border-radius: 2px;
  line-height: 16px;
}
.p-shop-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}
.shop-name:hover {
  color: #ff5000;
  text-decoration: underline;
}
.pagination-wrap {
  margin-top: 40px;
  text-align: center;
}
</style>
