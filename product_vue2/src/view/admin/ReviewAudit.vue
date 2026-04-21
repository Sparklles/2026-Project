<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form :inline="true" :model="listQuery" class="demo-form-inline">
        <el-form-item label="星级评价">
          <el-select v-model="listQuery.rating" placeholder="全部星级" clearable style="width: 150px">
            <el-option label="5星 (极好)" :value="5" />
            <el-option label="4星 (满意)" :value="4" />
            <el-option label="3星 (一般)" :value="3" />
            <el-option label="2星 (较差)" :value="2" />
            <el-option label="1星 (极差)" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="当前状态">
          <el-select v-model="listQuery.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="正常显示" :value="1" />
            <el-option label="已隐藏 (违规)" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">筛选查阅</el-button>
          <el-button icon="el-icon-refresh" @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table
        v-loading="listLoading"
        :data="list"
        border
        stripe
        style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />

      <el-table-column prop="bookTitle" label="被评书籍" min-width="150" show-overflow-tooltip>
        <template slot-scope="{row}">
          <span style="font-weight: bold; color: #409EFF;">《{{ row.bookTitle }}》</span>
        </template>
      </el-table-column>

      <el-table-column prop="reviewerName" label="评价用户" width="120" align="center" />

      <el-table-column label="评分" width="150" align="center">
        <template slot-scope="{row}">
          <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}">
          </el-rate>
        </template>
      </el-table-column>

      <el-table-column prop="content" label="评价内容" min-width="200">
        <template slot-scope="{row}">
          <div class="content-box">{{ row.content }}</div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="medium">
            {{ row.status === 1 ? '正常' : '已隐藏' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="adminReply" label="官方回复" min-width="180">
        <template slot-scope="{row}">
          <span v-if="row.adminReply" style="color: #67C23A; font-size: 13px;">
            <i class="el-icon-service"></i> {{ row.adminReply }}
          </span>
          <span v-else style="color: #909399; font-size: 12px; font-style: italic;">暂无回复</span>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="发布时间" width="160" align="center" />

      <el-table-column label="风控操作" width="180" align="center" fixed="right">
        <template slot-scope="{row}">
          <el-button
              type="primary"
              size="mini"
              plain
              @click="openReplyDialog(row)">
            回复
          </el-button>

          <el-button
              v-if="row.status === 1"
              type="danger"
              size="mini"
              @click="handleHide(row)">
            隐藏
          </el-button>
          <el-button
              v-else
              type="info"
              size="mini"
              disabled>
            已隐藏
          </el-button>
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

    <el-dialog title="官方回复评价" :visible.sync="replyDialogVisible" width="500px">
      <div v-if="currentReview" style="margin-bottom: 20px; padding: 15px; background: #f4f4f5; border-radius: 4px;">
        <p style="margin: 0 0 10px 0; font-size: 13px; color: #606266;">
          <strong>用户评价：</strong>{{ currentReview.content }}
        </p>
      </div>

      <el-form :model="replyForm" :rules="replyRules" ref="replyForm">
        <el-form-item prop="content">
          <el-input
              type="textarea"
              :rows="4"
              placeholder="请输入官方/客服视角的回复内容，安抚用户或解答疑问..."
              v-model="replyForm.content"
              maxlength="200"
              show-word-limit>
          </el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="submitReply">发送回复</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
// 引入在 api/admin/review.js 中封装好的请求方法
import { getAdminReviewList, hideReview, replyReview } from '../../api/admin/review'

export default {
  name: 'ReviewAudit',
  data() {
    return {
      // 列表相关
      list: [],
      total: 0,
      listLoading: false,
      listQuery: {
        current: 1,
        size: 10,
        rating: undefined,
        status: undefined
      },

      // 回复弹窗相关
      replyDialogVisible: false,
      replyLoading: false,
      currentReview: null, // 暂存当前正在回复的评价对象
      replyForm: {
        id: null,
        content: ''
      },
      replyRules: {
        content: [{ required: true, message: '回复内容不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // 获取评价列表
    async getList() {
      this.listLoading = true
      try {
        const res = await getAdminReviewList(this.listQuery)
        this.list = res.records
        this.total = res.total
      } catch (error) {
        console.error(error)
      } finally {
        this.listLoading = false
      }
    },

    // 触发筛选
    handleFilter() {
      this.listQuery.current = 1
      this.getList()
    },

    // 重置筛选条件
    resetFilter() {
      this.listQuery = { current: 1, size: 10, rating: undefined, status: undefined }
      this.getList()
    },

    // 分页处理
    handleSizeChange(val) {
      this.listQuery.size = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.listQuery.current = val
      this.getList()
    },

    // 隐藏评价操作 (带二次确认防误触)
    handleHide(row) {
      this.$confirm('隐藏后该评价将在前台商品详情页中彻底消失。是否确认隐藏该违规评价？', '风控操作警告', {
        type: 'warning',
        confirmButtonText: '立即隐藏',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        try {
          await hideReview(row.id)
          this.$message.success('评价已成功隐藏')
          // 不用重新调取整个列表，直接在前端修改状态，体验更流畅
          row.status = 0
        } catch (error) {
          console.warn("隐藏失败:", error)
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },

    // 打开官方回复弹窗
    openReplyDialog(row) {
      this.currentReview = row
      this.replyForm.id = row.id
      this.replyForm.content = row.adminReply || '' // 如果已经有回复，则回显出来以便修改
      this.replyDialogVisible = true
      this.$nextTick(() => {
        this.$refs['replyForm'].clearValidate()
      })
    },

    // 提交官方回复
    submitReply() {
      this.$refs['replyForm'].validate(async (valid) => {
        if (valid) {
          this.replyLoading = true
          try {
            await replyReview(this.replyForm.id, this.replyForm.content)
            this.$message.success('官方回复发布成功')
            this.replyDialogVisible = false

            // 同样优化体验，前端直接渲染，无需重刷列表
            this.currentReview.adminReply = this.replyForm.content
          } catch (error) {
            console.warn("回复失败:", error)
          } finally {
            this.replyLoading = false
          }
        }
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
/* 限制评价内容的高度，过长时显示滚动条，避免撑爆表格 */
.content-box {
  max-height: 80px;
  overflow-y: auto;
  font-size: 13px;
  line-height: 1.5;
  color: #303133;
}
/* 美化滚动条 */
.content-box::-webkit-scrollbar {
  width: 6px;
}
.content-box::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}
</style>