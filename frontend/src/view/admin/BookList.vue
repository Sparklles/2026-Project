<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="关键字">
          <el-input v-model="listQuery.keyword" placeholder="书名或作者" clearable @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="listQuery.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
          <el-button type="success" icon="el-icon-plus" @click="handleCreate">录入新书</el-button>

          <el-button
              type="warning"
              icon="el-icon-top"
              :disabled="multipleSelection.length === 0"
              @click="handleBatchStatus(1)">
            批量上架
          </el-button>

          <el-button
              type="info"
              icon="el-icon-bottom"
              :disabled="multipleSelection.length === 0"
              @click="handleBatchStatus(0)">
            批量下架
          </el-button>

          <el-tooltip content="若前台 AI 搜不到最新商品，可点击此按钮让大模型强制重新学习" placement="top">
            <el-button
                type="danger"
                icon="el-icon-cpu"
                :loading="aiSyncing"
                @click="handleForceSyncAI"
                style="margin-left: 10px;">
              {{ aiSyncing ? '大模型处理中...' : '重构 AI 知识库' }}
            </el-button>
          </el-tooltip>

        </el-form-item>
      </el-form>
    </div>

    <el-table
        v-loading="listLoading"
        :data="list"
        border
        stripe
        style="width: 100%"
        highlight-current-row
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="封面" width="100" align="center">
        <template slot-scope="{row}">
          <el-image style="width: 60px; height: 80px" :src="row.coverImageUrl" fit="cover">
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline" style="font-size: 30px; color: #ccc; margin-top: 25px;"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="categoryName" label="所属分类" width="120" align="center" />

      <el-table-column label="库存" width="100" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.stock > 10 ? 'success' : 'danger'">{{ row.stock }} 件</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="100" align="center">
        <template slot-scope="{row}">
          <span style="color: #f56c6c; font-weight: bold;">¥ {{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="180">
        <template slot-scope="{row}">
          <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 5px; margin-bottom: 5px;">
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上架状态" width="100" align="center">
        <template slot-scope="{row}">
          <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatusChange(row)">
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" icon="el-icon-edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px; text-align: right;">
      <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="listQuery.current"
          :page-sizes="[10, 20, 50]"
          :page-size="listQuery.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" :close-on-click-modal="false">
      <el-form ref="dataForm" :rules="rules" :model="tempBook" label-width="100px">

        <el-form-item label="商品图集" prop="imageUrls">
          <el-upload
              action="http://localhost:8083/api/admin/upload"
              list-type="picture-card"
              :file-list="fileList"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemoveImage"
              :on-preview="handlePictureCardPreview">
            <i class="el-icon-plus"></i>
          </el-upload>
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            第一张将自动作为商品主封面，支持 JPG/PNG 格式。
          </div>
        </el-form-item>

        <el-dialog :visible.sync="previewVisible" append-to-body>
          <img width="100%" :src="previewImageUrl" alt="">
        </el-dialog>

        <el-form-item label="书名" prop="title">
          <el-input v-model="tempBook.title" placeholder="请输入航海书籍名称" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="tempBook.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="tempBook.isbn" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="价格 (元)" prop="price">
              <el-input-number v-model="tempBook.price" :min="0" :precision="2" :step="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="tempBook.categoryId" placeholder="请选择" style="width: 100%;">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="8">
            <el-form-item label="库存数量" prop="stock">
              <el-input-number v-model="tempBook.stock" :min="0" :step="1" controls-position="right" class="number-input" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="页数" prop="pages">
              <el-input-number v-model="tempBook.pages" :min="1" :step="1" placeholder="选填" controls-position="right" class="number-input" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker
                  v-model="tempBook.publishDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="yyyy-MM-dd"
                  style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品标签" prop="tagIds">
          <el-select v-model="tempBook.tagIds" multiple placeholder="可选择多个关联标签" style="width: 100%;">
            <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容简介">
          <el-input type="textarea" :rows="3" v-model="tempBook.description" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="saveData">确认保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addBook, updateBook, changeBookStatus, deleteBook } from '../../api/admin/book'
import request from '../../utils/request'

export default {
  name: 'BookList',
  data() {
    return {
      list: [],
      total: 0,
      listLoading: false,
      aiSyncing: false, // 🌟 新增：控制大模型同步按钮的 loading 状态
      listQuery: {
        current: 1,
        size: 10,
        keyword: undefined,
        categoryId: undefined
      },
      categoryOptions: [],
      tagOptions: [],
      dialogVisible: false,
      dialogTitle: '录入新书',
      submitLoading: false,
      fileList: [],
      previewVisible: false,
      previewImageUrl: '',

      tempBook: {
        id: undefined,
        title: '',
        author: '',
        isbn: '',
        price: 0,
        stock: 0,
        pages: undefined,
        publishDate: '',
        categoryId: undefined,
        tagIds: [],
        description: '',
        imageUrls: []
      },
      multipleSelection: [],
      rules: {
        title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
        price: [{ required: true, message: '价格不能为空', trigger: 'blur' }],
        stock: [{ required: true, message: '库存数量不能为空', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    }
  },
  created() {
    this.fetchDictData()
    this.getList()
  },
  methods: {
    // 🌟 新增：手动触发后台 AI 知识库重构
    handleForceSyncAI() {
      this.$confirm('此操作将读取所有上架商品并调用【智谱大模型】重新生成向量，可能需要耗费几十秒，确定执行吗？', '系统提示', {
        type: 'warning',
        confirmButtonText: '立即重构',
        cancelButtonText: '取消'
      }).then(async () => {
        this.aiSyncing = true
        try {
          // 调用我们在 BookAdminController.java 新增的接口
          const res = await request.get('/api/admin/books/sync-ai')
          // 这里显示接口返回的提示文字（比如：指令已下发）
          this.$message.success(typeof res === 'string' ? res : 'AI 知识库重构指令已下发！')
        } catch (error) {
          console.error(error)
          this.$message.error('大模型接口调用失败，请查看控制台日志')
        } finally {
          // 因为后台是异步执行的，所以前端很快就能解除 loading
          this.aiSyncing = false
        }
      }).catch(() => {
        this.$message.info('已取消重构')
      })
    },

    async fetchDictData() {
      try {
        const catRes = await request.get('/api/admin/books/categories/listAll')
        this.categoryOptions = catRes

        const tagRes = await request.get('/api/admin/books/tags/listAll')
        this.tagOptions = tagRes
      } catch (e) {
        console.warn('获取字典失败', e)
      }
    },

    async getList() {
      this.listLoading = true
      try {
        const res = await request.get('/api/admin/books/page', { params: this.listQuery })
        this.list = res.records
        this.total = res.total
      } catch (error) {
        console.error(error)
      } finally {
        this.listLoading = false
      }
    },

    handleFilter() {
      this.listQuery.current = 1
      this.getList()
    },

    handleSizeChange(val) {
      this.listQuery.size = val
      this.getList()
    },

    handleCurrentChange(val) {
      this.listQuery.current = val
      this.getList()
    },

    resetTemp() {
      this.tempBook = {
        id: undefined,
        title: '',
        author: '',
        isbn: '',
        price: 0,
        stock: 0,
        pages: undefined,
        publishDate: '',
        categoryId: undefined,
        tagIds: [],
        description: '',
        imageUrls: []
      }
      this.fileList = []
    },

    handleCreate() {
      this.resetTemp()
      this.dialogTitle = '录入新书'
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },

    handleUpdate(row) {
      this.tempBook = Object.assign({}, row)
      this.tempBook.tagIds = Array.isArray(row.tagIds) ? row.tagIds : []

      if (row.images && row.images.length > 0) {
        this.tempBook.imageUrls = [...row.images]
        this.fileList = row.images.map((url, index) => {
          return { name: `image_${index}`, url: url }
        })
      } else {
        this.tempBook.imageUrls = []
        this.fileList = []
      }

      this.dialogTitle = '编辑书籍信息'
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },

    handleUploadSuccess(res, file, fileList) {
      if (res.code === 200) {
        this.fileList = fileList;
        this.syncImageUrls();
      } else {
        this.$message.error('上传图片失败');
      }
    },

    async handleRemoveImage(file, fileList) {
      let targetUrl = '';

      if (file.response && file.response.data) {
        targetUrl = file.response.data;
      } else if (file.url) {
        targetUrl = file.url;
      }

      if (targetUrl && targetUrl.startsWith('http')) {
        try {
          console.log('正在请求后端删除文件:', targetUrl);
          await request.delete('/api/admin/upload', {
            params: { imageUrl: targetUrl }
          });
          this.$message.success('本地磁盘文件已清理');
        } catch (error) {
          console.error('磁盘清理失败:', error);
        }
      }

      this.fileList = fileList;
      this.syncImageUrls();
    },

    handlePictureCardPreview(file) {
      this.previewImageUrl = file.url;
      this.previewVisible = true;
    },

    syncImageUrls() {
      this.tempBook.imageUrls = [];

      this.fileList.forEach(item => {
        if (item.response && item.response.code === 200) {
          let realUrl = item.response.message || item.response.data;
          if (realUrl) {
            this.tempBook.imageUrls.push(realUrl);
          }
        }
        else if (item.url && !item.url.startsWith('blob:')) {
          this.tempBook.imageUrls.push(item.url);
        }
      });
    },

    saveData() {
      const isUploading = this.fileList.some(item => item.status === 'uploading' || item.status === 'ready');
      if (isUploading) {
        this.$message.warning('图片正在上传中，请等待上传完成后再保存！');
        return;
      }

      this.syncImageUrls();

      this.$refs['dataForm'].validate(async (valid) => {
        if (valid) {
          this.submitLoading = true
          try {
            if (this.tempBook.id) {
              await updateBook(this.tempBook)
            } else {
              await addBook(this.tempBook)
            }
            this.dialogVisible = false
            this.$message.success('保存成功，AI 大脑已后台静默学习更新')
            this.getList()
          } catch (error) {
            console.error(error)
          } finally {
            this.submitLoading = false
          }
        }
      })
    },

    async handleStatusChange(row) {
      try {
        await changeBookStatus(row.id, row.status)
        this.$message.success('商品状态已更新')
      } catch (error) {
        row.status = row.status === 1 ? 0 : 1
      }
    },

    handleDelete(row) {
      this.$confirm(`确认要删除书籍《${row.title}》吗？`, '危险操作提示', {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await deleteBook(row.id)
          this.$message.success('删除成功')
          this.getList()
        } catch (error) {
          console.error('删除操作异常:', error)
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    handleSelectionChange(val) {
      this.multipleSelection = val
    },

    handleBatchStatus(status) {
      if (this.multipleSelection.length === 0) return

      const ids = this.multipleSelection.map(item => item.id)
      const actionText = status === 1 ? '上架' : '下架'

      this.$confirm(`确认要将选中的 ${ids.length} 本书批量${actionText}吗？`, '批量操作提示', {
        type: 'warning',
        confirmButtonText: `确认${actionText}`,
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request.put(`/api/admin/books/batch-status?status=${status}`, ids)
          this.$message.success(`成功批量${actionText} ${ids.length} 本书！`)
          this.getList()
        } catch (error) {
          console.error('批量操作异常:', error)
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background-color: #fff;
}
.filter-container {
  margin-bottom: 20px;
}
.number-input {
  width: 140px;
}
</style>
