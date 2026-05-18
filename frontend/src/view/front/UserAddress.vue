<template>
  <div class="user-address-page">
    <el-card class="address-card">
      <div slot="header" class="clearfix header-area">
        <span class="title">我的收货地址</span>
        <el-button type="default" size="small" @click="showAddDialog">添加地址</el-button>
      </div>

      <el-table :data="addressList" style="width: 100%" class="address-table">
        <el-table-column prop="consigneeName" label="收货人" width="100"></el-table-column>
        <el-table-column prop="phone" label="电话/手机" width="140"></el-table-column>
        <el-table-column label="所在地区" width="220">
          <template slot-scope="scope">
            {{ scope.row.province }} {{ scope.row.city }} {{ scope.row.district }}
          </template>
        </el-table-column>
        <el-table-column prop="detailAddress" label="详细地址"></el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="editAddress(scope.row)">修改</el-button>
            <el-button type="text" size="small" style="color: #999;" @click="deleteAddress(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="移动设置" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.isDefault === 1" class="default-tag">默认地址</span>
            <el-button v-else type="text" size="small" style="color: #666;" @click="setDefault(scope.row)">设为默认</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/修改地址弹窗 -->
    <el-dialog
      :title="isEdit ? '修改收货地址' : '添加收货地址'"
      :visible.sync="dialogVisible"
      width="600px"
      custom-class="address-dialog"
      :close-on-click-modal="false"
    >
      <div class="delivery-to">
        <span class="label">仅支持配送至</span>
        <span class="value">中国大陆以及港澳台地区</span>
      </div>

      <el-form :model="form" :rules="rules" ref="addressForm" label-width="110px" class="addr-form">
        <el-form-item label="地址信息:" prop="region">
          <el-cascader
            v-model="form.region"
            :options="regionOptions"
            :props="{ value: 'label', label: 'label', children: 'children' }"
            placeholder="请选择省/市/区/街道"
            style="width: 100%"
          ></el-cascader>
        </el-form-item>

        <el-form-item label="详细地址:" prop="detailAddress">
          <el-input
            type="textarea"
            v-model="form.detailAddress"
            :rows="3"
            placeholder="请输入详细地址信息，如道路、门牌号、小区、楼栋号、单元等信息"
          ></el-input>
        </el-form-item>

        <el-form-item label="收货人姓名:" prop="consigneeName">
          <el-input v-model="form.consigneeName" placeholder="长度不超过25个字符" maxlength="25"></el-input>
        </el-form-item>

        <el-form-item label="手机号码:" prop="phone">
          <el-input v-model="form.phone" placeholder="电话号码、手机号码必须填一项" class="phone-input">
            <el-select v-model="form.phonePrefix" slot="prepend" placeholder="请选择" style="width: 130px;">
              <el-option label="中国大陆 +86" value="86"></el-option>
              <el-option label="中国香港 +852" value="852"></el-option>
              <el-option label="中国澳门 +853" value="853"></el-option>
              <el-option label="中国台湾 +886" value="886"></el-option>
            </el-select>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.isDefault">设置为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" class="btn-cancel">取消</el-button>
        <el-button type="primary" @click="submitForm" class="btn-confirm">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/front/address'
import { regionData } from 'element-china-area-data'

export default {
  name: 'UserAddress',
  data() {
    return {
      dialogVisible: false,
      isEdit: false,
      editIndex: -1,
      form: {
        region: [],
        detailAddress: '',
        consigneeName: '',
        phonePrefix: '86',
        phone: '',
        isDefault: false
      },
      rules: {
        region: [{ required: true, message: '请选择省/市/区/街道', trigger: 'change' }],
        detailAddress: [{ required: true, message: '请输入详细地址信息', trigger: 'blur' }],
        consigneeName: [
          { required: true, message: '请输入收货人姓名', trigger: 'blur' },
          { max: 25, message: '长度不超过 25 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^\d{8,11}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },
      regionOptions: regionData,
      addressList: []
    }
  },
  created() {
    this.fetchAddressList();
  },
  methods: {
    async fetchAddressList() {
      try {
        const data = await getAddressList();
        // 根据后端返回的省市区组装级联选择器所需的数组
        if (data && Array.isArray(data)) {
          this.addressList = data.map(item => ({
            ...item,
            region: [item.province, item.city, item.district].filter(Boolean)
          }));
        }
      } catch (error) {
        console.error('获取收货地址失败', error);
      }
    },
    showAddDialog() {
      this.isEdit = false;
      this.form = {
        region: [],
        detailAddress: '',
        consigneeName: '',
        phonePrefix: '86',
        phone: '',
        isDefault: false
      };
      if (this.$refs.addressForm) {
        this.$refs.addressForm.clearValidate();
      }
      this.dialogVisible = true;
    },
    editAddress(row) {
      this.isEdit = true;
      // 深拷贝，并将数值型 isDefault 转换为 boolean 给 checkbox 使用
      this.form = JSON.parse(JSON.stringify(row));
      this.form.isDefault = this.form.isDefault === 1;
      
      // 解析电话前缀和号码（假设后端存的是 86-13800000000）
      if (row.phone && row.phone.includes('-')) {
        const parts = row.phone.split('-');
        this.form.phonePrefix = parts[0];
        this.form.phone = parts[1];
      } else {
        this.form.phonePrefix = '86'; // 默认保底
        this.form.phone = row.phone || '';
      }
      
      // 关键回显逻辑：确保 region 是一个完整的数组 [province, city, district]
      this.form.region = [row.province, row.city, row.district].filter(Boolean);
      
      if (this.$refs.addressForm) {
        this.$refs.addressForm.clearValidate();
      }
      this.dialogVisible = true;
    },
    deleteAddress(row) {
      this.$confirm('确定要删除该收货地址吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteAddress(row.id);
          this.$message.success('删除成功');
          this.fetchAddressList();
        } catch (error) {
          console.error('删除失败', error);
        }
      }).catch(() => {});
    },
    async setDefault(row) {
      try {
        await setDefaultAddress(row.id);
        this.$message.success('设置默认地址成功');
        this.fetchAddressList();
      } catch (error) {
        console.error('设置默认地址失败', error);
      }
    },
    submitForm() {
      this.$refs.addressForm.validate(async (valid) => {
        if (valid) {
          try {
            const submitData = {
              consigneeName: this.form.consigneeName,
              phone: `${this.form.phonePrefix}-${this.form.phone}`,
              province: this.form.region[0] || '',
              city: this.form.region[1] || '',
              district: this.form.region[2] || '',
              detailAddress: this.form.detailAddress,
              isDefault: this.form.isDefault ? 1 : 0
            };

            if (this.isEdit) {
              await updateAddress(this.form.id, submitData);
              this.$message.success('修改地址成功');
            } else {
              await addAddress(submitData);
              this.$message.success('添加地址成功');
            }
            this.dialogVisible = false;
            this.fetchAddressList();
          } catch (error) {
            console.error('保存地址失败', error);
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.user-address-page {
  padding: 20px 0;
}
.address-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.header-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: bold;
}
.address-table ::v-deep th {
  background-color: #f5f7fa;
  color: #333;
  font-weight: normal;
}
.address-table ::v-deep td {
  padding: 15px 0;
  color: #666;
}
.default-tag {
  background-color: #ffefe6;
  color: #ff5000;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

/* 弹窗样式调整 */
.delivery-to {
  background-color: #f5f7fa;
  padding: 10px 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.delivery-to .label {
  color: #999;
  margin-right: 15px;
}
.delivery-to .value {
  color: #333;
  flex: 1;
}

.addr-form ::v-deep .el-form-item__label {
  color: #666;
}
.addr-form ::v-deep .el-form-item__label::before {
  color: #ff5000 !important; /* 必填红星号颜色 */
}
.phone-input ::v-deep .el-input-group__prepend {
  background-color: #fff;
}
.dialog-footer {
  text-align: center;
}
.btn-cancel {
  width: 120px;
}
.btn-confirm {
  width: 120px;
  background-color: #ff5000;
  border-color: #ff5000;
}
.btn-confirm:hover {
  background-color: #ff6a26;
  border-color: #ff6a26;
}
</style>
