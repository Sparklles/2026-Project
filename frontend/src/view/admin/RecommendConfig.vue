<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold; font-size: 16px;">
          <i class="el-icon-setting"></i> 推荐规则配置管理
        </span>
        <span style="color: #909399; font-size: 13px; margin-left: 15px;">
          在此处管理各推荐场景的策略规则，修改后将在下一次定时任务执行时生效。
        </span>
      </div>

      <div class="toolbar">
        <el-button type="primary" icon="el-icon-plus" size="small" @click="openDialog()">
          新增推荐配置
        </el-button>
        <el-button
          type="success"
          icon="el-icon-refresh"
          size="small"
          :loading="refreshLoading"
          @click="handleRefreshRecommend">
          刷新推荐数据
        </el-button>
      </div>

      <el-table :data="configList" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="100" align="center" />
        <el-table-column prop="configKey" label="配置标识" min-width="140" show-overflow-tooltip />
        <el-table-column prop="configName" label="配置名称" min-width="140" />
        <el-table-column prop="sceneCode" label="场景编码" width="130" align="center">
          <template slot-scope="{row}">
            <el-tag size="small" type="info">{{ row.sceneCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="规则JSON" min-width="200" show-overflow-tooltip>
          <template slot-scope="{row}">
            <span style="font-size: 12px; color: #606266; font-family: monospace;">
              {{ truncateJson(row.ruleJson) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template slot-scope="{row}">
            <el-tag size="small" effect="plain">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template slot-scope="{row}">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="{row}">
            <el-button type="primary" size="mini" icon="el-icon-edit" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      :title="form.id ? '编辑推荐配置' : '新增推荐配置'"
      :visible.sync="dialogVisible"
      width="750px"
      :close-on-click-modal="false"
    >
      <el-form ref="configForm" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="配置标识" prop="configKey">
              <el-input v-model="form.configKey" placeholder="如 home_popular_v1" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置名称" prop="configName">
              <el-input v-model="form.configName" placeholder="如 首页畅销榜" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="场景编码" prop="sceneCode">
              <el-select v-model="form.sceneCode" placeholder="请选择场景" style="width: 100%;">
                <el-option label="畅销榜 (POPULAR)" value="POPULAR" />
                <el-option label="新书上架 (NEW)" value="NEW" />
                <el-option label="首页主题 (HOME_TOPIC)" value="HOME_TOPIC" />
                <el-option label="关联推荐 (ALSO_BOUGHT)" value="ALSO_BOUGHT" />
                <el-option label="个性化推荐 (PERSONALIZED)" value="PERSONALIZED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="form.priority" :min="0" :max="999" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="规则描述说明" maxlength="255" show-word-limit />
        </el-form-item>

        <el-form-item label="规则JSON" prop="ruleJson">
          <el-input
            v-model="form.ruleJson"
            type="textarea"
            :rows="12"
            placeholder='请输入合法的JSON，例如：
{
  "strategies": [
    {"strategyType": "POPULAR", "weight": 1.0, "params": {"limit": 6}}
  ]
}'
            style="font-family: Consolas, Monaco, monospace; font-size: 13px;"
          />
          <div style="margin-top: 5px;">
            <el-button size="mini" type="text" icon="el-icon-magic-stick" @click="formatJson">格式化JSON</el-button>
            <el-button size="mini" type="text" icon="el-icon-circle-check" @click="validateJson">校验JSON</el-button>
            <span v-if="jsonValid === true" style="color: #67c23a; font-size: 12px; margin-left: 8px;">
              <i class="el-icon-circle-check"></i> JSON格式正确
            </span>
            <span v-if="jsonValid === false" style="color: #f56c6c; font-size: 12px; margin-left: 8px;">
              <i class="el-icon-circle-close"></i> {{ jsonError }}
            </span>
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listRecommendConfigs,
  createRecommendConfig,
  updateRecommendConfig,
  deleteRecommendConfig,
  toggleRecommendConfigStatus,
  refreshRecommendData
} from '@/api/admin/recommend'

export default {
  name: 'RecommendConfig',
  data() {
    return {
      configList: [],
      loading: false,
      submitLoading: false,
      refreshLoading: false,
      dialogVisible: false,
      jsonValid: null,
      jsonError: '',
      form: {
        id: undefined,
        configKey: '',
        configName: '',
        sceneCode: '',
        ruleJson: '',
        priority: 0,
        status: 1,
        remark: ''
      },
      rules: {
        configKey: [{ required: true, message: '配置标识不能为空', trigger: 'blur' }],
        configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
        sceneCode: [{ required: true, message: '请选择场景编码', trigger: 'change' }],
        ruleJson: [
          { required: true, message: '规则JSON不能为空', trigger: 'blur' },
          { validator: this.validateRuleJson, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    async fetchList() {
      this.loading = true
      try {
        this.configList = await listRecommendConfigs()
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },

    openDialog(row) {
      if (row) {
        this.form = {
          id: row.id,
          configKey: row.configKey,
          configName: row.configName,
          sceneCode: row.sceneCode,
          ruleJson: row.ruleJson,
          priority: row.priority,
          status: row.status,
          remark: row.remark || ''
        }
      } else {
        this.form = {
          id: undefined,
          configKey: '',
          configName: '',
          sceneCode: '',
          ruleJson: '',
          priority: 0,
          status: 1,
          remark: ''
        }
      }
      this.jsonValid = null
      this.jsonError = ''
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.configForm) {
          this.$refs.configForm.clearValidate()
        }
      })
    },

    validateRuleJson(rule, value, callback) {
      if (!value) {
        callback(new Error('规则JSON不能为空'))
        return
      }
      try {
        JSON.parse(value)
        callback()
      } catch (e) {
        callback(new Error('JSON格式不合法: ' + e.message))
      }
    },

    formatJson() {
      try {
        const obj = JSON.parse(this.form.ruleJson)
        this.form.ruleJson = JSON.stringify(obj, null, 2)
        this.jsonValid = true
        this.jsonError = ''
        this.$message.success('JSON格式化成功')
      } catch (e) {
        this.jsonValid = false
        this.jsonError = e.message
        this.$message.error('JSON格式错误，无法格式化: ' + e.message)
      }
    },

    validateJson() {
      try {
        JSON.parse(this.form.ruleJson)
        this.jsonValid = true
        this.jsonError = ''
        this.$message.success('JSON格式校验通过')
      } catch (e) {
        this.jsonValid = false
        this.jsonError = e.message
        this.$message.error('JSON格式错误: ' + e.message)
      }
    },

    submitForm() {
      this.$refs.configForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.form.id) {
            await updateRecommendConfig(this.form)
            this.$message.success('推荐配置更新成功')
          } else {
            await createRecommendConfig(this.form)
            this.$message.success('推荐配置创建成功')
          }
          this.dialogVisible = false
          this.fetchList()
        } catch (e) {
          console.error(e)
        } finally {
          this.submitLoading = false
        }
      })
    },


    async handleRefreshRecommend() {
      try {
        await this.$confirm(
          '确定要立即根据当前推荐规则重新生成推荐数据吗？该操作可能需要一定时间。',
          '确认刷新',
          { type: 'warning' }
        )
        this.refreshLoading = true
        await refreshRecommendData()
        this.$message.success('推荐数据刷新任务已执行')
        this.fetchList()
      } catch (e) {
        if (e !== 'cancel' && e !== 'close') {
          console.error(e)
          this.$message.error('推荐数据刷新失败')
        }
      } finally {
        this.refreshLoading = false
      }
    },
    handleDelete(row) {
      this.$confirm(
        `确定要删除推荐配置 [${row.configName}] 吗？删除后该场景将不再生成推荐数据。`,
        '警告',
        { type: 'warning' }
      ).then(async () => {
        try {
          await deleteRecommendConfig(row.id)
          this.$message.success('删除成功')
          this.fetchList()
        } catch (e) {
          console.error(e)
        }
      }).catch(() => {})
    },

    async handleStatusChange(row) {
      try {
        await toggleRecommendConfigStatus(row.id, row.status)
        this.$message.success(row.status === 1 ? '已启用' : '已禁用')
      } catch (e) {
        row.status = row.status === 1 ? 0 : 1
        console.error(e)
      }
    },

    truncateJson(json) {
      if (!json) return ''
      try {
        const obj = JSON.parse(json)
        const compact = JSON.stringify(obj)
        return compact.length > 60 ? compact.substring(0, 60) + '...' : compact
      } catch (e) {
        return json.length > 60 ? json.substring(0, 60) + '...' : json
      }
    }
  }
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>


