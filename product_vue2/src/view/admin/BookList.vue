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
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />

      <el-table-column label="封面" width="100" align="center">
        <template slot-scope="{row}">
          <el-image
              style="width: 60px; height: 80px"
              :src="row.coverImageUrl"
              fit="cover">
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline" style="font-size: 30px; color: #ccc; margin-top: 25px;"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="categoryName" label="所属分类" width="120" align="center" />

      <el-table-column label="价格" width="100" align="center">
        <template slot-scope="{row}">
          <span style="color: #f56c6c; font-weight: bold;">¥ {{ row.price }}</span>
        </template>
      </el-table-column>

      <el-table-column label="标签" min-width="180">
        <template slot-scope="{row}">
          <el-tag
              v-for="tag in row.tags"
              :key="tag"
              size="small"
              style="margin-right: 5px; margin-bottom: 5px;">
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" :close-on-click-modal="false">
      <el-form ref="dataForm" :rules="rules" :model="tempBook" label-width="100px">
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
// 引入在 api 目录中封装好的请求方法
import { addBook, updateBook, changeBookStatus, deleteBook } from '../../api/admin/book'
import request from '../../utils/request'

export default {
  name: 'BookList',
  data() {
    return {
      // 列表相关数据
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        current: 1,
        size: 10,
        keyword: undefined,
        categoryId: undefined
      },

      // 字典数据 (用于下拉框)
      categoryOptions: [],
      tagOptions: [],

      // 弹窗相关数据
      dialogVisible: false,
      dialogTitle: '录入新书',
      submitLoading: false,

      // 表单绑定的数据模型 (对应后端的 BookDTO)
      tempBook: {
        id: undefined,
        title: '',
        author: '',
        isbn: '',
        price: 0,
        categoryId: undefined,
        tagIds: [],
        description: ''
      },

      // 表单验证规则
      rules: {
        title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
        price: [{ required: true, message: '价格不能为空', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    }
  },
  created() {
    // 页面初始化时拉取字典和表格数据
    this.fetchDictData()
    this.getList()
  },
  methods: {
    // 获取字典数据 (分类、标签)
    // 获取字典数据 (分类、标签)
    async fetchDictData() {
      try {
        // 🌟 重点：路径中加上 /books
        const catRes = await request.get('/api/admin/books/categories/listAll')
        this.categoryOptions = catRes

        // 🌟 重点：路径中加上 /books
        const tagRes = await request.get('/api/admin/books/tags/listAll')
        this.tagOptions = tagRes
      } catch (e) {
        console.warn('获取字典失败', e)
      }
    },

    // 获取表格数据
    async getList() {
      this.listLoading = true
      try {
        // 请求我们在 Mapper 中写的 searchBooksDynamic 对应的接口
        const res = await request.get('/api/admin/books/page', { params: this.listQuery })
        this.list = res.records // MyBatis-Plus 分页返回的数据列表在 records 属性中
        this.total = res.total  // 总条数
      } catch (error) {
        console.error(error)
      } finally {
        this.listLoading = false
      }
    },

    // 触发搜索
    handleFilter() {
      this.listQuery.current = 1 // 搜索时重置回第一页
      this.getList()
    },

    // 分页：改变每页条数
    handleSizeChange(val) {
      this.listQuery.size = val
      this.getList()
    },

    // 分页：改变当前页
    handleCurrentChange(val) {
      this.listQuery.current = val
      this.getList()
    },

    // 重置表单
    resetTemp() {
      this.tempBook = {
        id: undefined,
        title: '',
        author: '',
        isbn: '',
        price: 0,
        categoryId: undefined,
        tagIds: [], // 初始化为空数组，用于多选
        description: ''
      }
    },

    // 打开新增弹窗
    handleCreate() {
      this.resetTemp()
      this.dialogTitle = '录入新书'
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },

    // 打开编辑弹窗
    handleUpdate(row) {
      // 将当前行的数据拷贝到表单对象中 (注意深拷贝防联动)
      this.tempBook = Object.assign({}, row)
      this.dialogTitle = '编辑书籍信息'
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },

    // 保存数据 (新增或编辑)
    saveData() {
      this.$refs['dataForm'].validate(async (valid) => {
        if (valid) {
          this.submitLoading = true
          try {
            if (this.tempBook.id) {
              // 有 ID 走编辑逻辑
              await updateBook(this.tempBook)
            } else {
              // 无 ID 走新增逻辑
              await addBook(this.tempBook)
            }
            this.dialogVisible = false
            this.$message.success('保存成功')
            this.getList() // 刷新列表
          } catch (error) {
            // 拦截器已经处理过报错，这里不做处理
          } finally {
            this.submitLoading = false
          }
        }
      })
    },

    // 状态切换 (上架/下架)
    async handleStatusChange(row) {
      try {
        await changeBookStatus(row.id, row.status)
        this.$message.success('商品状态已更新')
      } catch (error) {
        // 如果后端报错（例如乐观锁或无权限），把前端 Switch 开关的状态拨回去
        row.status = row.status === 1 ? 0 : 1
      }
    },

    // 删除书籍
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
          // 如果后端抛出“有历史订单禁止删除”的异常，Axios拦截器会自动弹出 Error Message
        }
      }).catch(() => {
        this.$message.info('已取消删除')
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
</style>
