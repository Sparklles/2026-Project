<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">
          <i class="el-icon-collection"></i> 基础字典数据维护
        </span>
        <span style="color: #909399; font-size: 13px; margin-left: 15px;">
          在此处添加的分类和标签，将实时同步到【商品数据维护】的录入下拉框中，数字越大排名越前。
        </span>
      </div>

      <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabClick">

        <el-tab-pane label="图书分类管理" name="category">
          <div class="toolbar">
            <el-button type="primary" icon="el-icon-plus" size="small" @click="openCategoryDialog()">
              新增主分类
            </el-button>
          </div>

          <el-table :data="categoryList" v-loading="catLoading" border stripe style="width: 100%">
            <el-table-column prop="id" label="分类ID" width="80" align="center" />
            <el-table-column prop="name" label="分类名称" min-width="150" />
            <el-table-column prop="sortOrder" label="排序权重" width="100" align="center">
              <template slot-scope="{row}">
                <el-tag type="info" effect="plain">{{ row.sortOrder || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
            <el-table-column label="操作" width="180" align="center">
              <template slot-scope="{row}">
                <el-button type="text" icon="el-icon-edit" @click="openCategoryDialog(row)">编辑</el-button>
                <el-button type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDeleteCategory(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="商品营销标签管理" name="tag">
          <div class="toolbar">
            <el-button type="success" icon="el-icon-collection-tag" size="small" @click="openTagDialog()">
              新增营销标签
            </el-button>
          </div>

          <el-table :data="tagList" v-loading="tagLoading" border stripe style="width: 100%">
            <el-table-column prop="id" label="标签ID" width="80" align="center" />
            <el-table-column label="标签名称" min-width="150">
              <template slot-scope="{row}">
                <el-tag :type="getRandomTagType(row.id)" effect="dark">
                  {{ row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序权重" width="100" align="center">
              <template slot-scope="{row}">
                <el-tag type="info" effect="plain">{{ row.sortOrder || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
            <el-table-column label="操作" width="180" align="center">
              <template slot-scope="{row}">
                <el-button type="text" icon="el-icon-edit" @click="openTagDialog(row)">编辑</el-button>
                <el-button type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDeleteTag(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <el-dialog :title="catForm.id ? '编辑分类' : '新增分类'" :visible.sync="catDialogVisible" width="400px" :close-on-click-modal="false">
      <el-form ref="catForm" :model="catForm" :rules="catRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="catForm.name" placeholder="例如：基础航海理论" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="catForm.sortOrder" :min="0" :max="999" style="width: 100%;" />
          <div style="font-size: 12px; color: #999;">数字越大，在下拉框中越靠前</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="catDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCategory">确认保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="tagForm.id ? '编辑标签' : '新增标签'" :visible.sync="tagDialogVisible" width="400px" :close-on-click-modal="false">
      <el-form ref="tagForm" :model="tagForm" :rules="tagRules" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="例如：年度畅销、新手必读" maxlength="15" show-word-limit />
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="tagForm.sortOrder" :min="0" :max="999" style="width: 100%;" />
          <div style="font-size: 12px; color: #999;">数字越大，在下拉框中越靠前</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitTag">确认保存</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import request from '../../utils/request'

export default {
  name: 'DictManage',
  data() {
    return {
      activeTab: 'category',
      submitLoading: false,

      // === 分类 ===
      categoryList: [],
      catLoading: false,
      catDialogVisible: false,
      catForm: {
        id: undefined,
        name: '',
        sortOrder: 0
      },
      catRules: {
        name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
      },

      // === 标签 ===
      tagList: [],
      tagLoading: false,
      tagDialogVisible: false,
      tagForm: {
        id: undefined,
        name: '',
        sortOrder: 0 // 🌟 初始化权值
      },
      tagRules: {
        name: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.fetchCategoryList()
  },
  methods: {
    handleTabClick(tab) {
      if (tab.name === 'category' && this.categoryList.length === 0) {
        this.fetchCategoryList()
      } else if (tab.name === 'tag' && this.tagList.length === 0) {
        this.fetchTagList()
      }
    },

    // --- 分类操作 ---
    async fetchCategoryList() {
      this.catLoading = true
      try {
        this.categoryList = await request.get('/api/admin/books/categories/listAll')
      } catch (error) {console.log(error.data())} finally { this.catLoading = false }
    },
    openCategoryDialog(row) {
      if (row) {
        this.catForm = Object.assign({}, row)
      } else {
        this.catForm = { id: undefined, name: '', sortOrder: 0 }
      }
      this.catDialogVisible = true
      this.$nextTick(() => { this.$refs.catForm.clearValidate() })
    },
    submitCategory() {
      this.$refs.catForm.validate(async valid => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.catForm.id) {
            await request.put('/api/admin/books/categories', this.catForm)
          } else {
            await request.post('/api/admin/books/categories', this.catForm)
          }
          this.$message.success('保存成功')
          this.catDialogVisible = false
          this.fetchCategoryList()
        } catch (error) {console.log(error.data())} finally { this.submitLoading = false }
      })
    },
    handleDeleteCategory(row) {
      this.$confirm(`确定要删除分类 [${row.name}] 吗？请确保该分类下没有绑定的书籍！`, '警告', { type: 'warning' })
          .then(async () => {
            await request.delete(`/api/admin/books/categories/${row.id}`)
            this.$message.success('删除成功')
            this.fetchCategoryList()
          }).catch(() => {})
    },

    // --- 标签操作 ---
    async fetchTagList() {
      this.tagLoading = true
      try {
        this.tagList = await request.get('/api/admin/books/tags/listAll')
      } catch (error) {console.log(error.data())} finally { this.tagLoading = false }
    },
    openTagDialog(row) {
      if (row) {
        this.tagForm = Object.assign({}, row)
      } else {
        this.tagForm = { id: undefined, name: '', sortOrder: 0 } // 🌟 包含权值
      }
      this.tagDialogVisible = true
      this.$nextTick(() => { this.$refs.tagForm.clearValidate() })
    },
    submitTag() {
      this.$refs.tagForm.validate(async valid => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.tagForm.id) {
            await request.put('/api/admin/books/tags', this.tagForm)
          } else {
            await request.post('/api/admin/books/tags', this.tagForm)
          }
          this.$message.success('保存成功')
          this.tagDialogVisible = false
          this.fetchTagList()
        } catch (error) {console.log(error.data())} finally { this.submitLoading = false }
      })
    },
    handleDeleteTag(row) {
      this.$confirm(`确定要删除标签 [${row.name}] 吗？`, '提示', { type: 'warning' })
          .then(async () => {
            await request.delete(`/api/admin/books/tags/${row.id}`)
            this.$message.success('删除成功')
            this.fetchTagList()
          }).catch(() => {})
    },

    getRandomTagType(id) {
      const types = ['primary', 'success', 'warning', 'danger']
      return types[id % 4]
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}
.box-card {
  border-radius: 8px;
}
.toolbar {
  margin-bottom: 15px;
}
</style>