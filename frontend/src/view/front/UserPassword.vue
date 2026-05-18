<template>
  <div class="user-password-page">
    <el-card class="password-card">
      <div slot="header" class="clearfix">
        <span>修改密码</span>
      </div>
      <div class="password-tip">
        <el-alert
          title="为了您的账号安全，请定期修改密码。密码修改成功后需要重新登录。"
          type="warning"
          show-icon
          :closable="false"
          class="mb-20">
        </el-alert>
      </div>

      <el-form ref="pwdForm" :model="pwdForm" :rules="rules" label-width="120px" class="pwd-form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入当前登录密码"
          ></el-input>
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="请设置新密码（至少6个字符）"
          ></el-input>
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
            @keyup.enter.native="handleSubmit"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-btn">确认修改</el-button>
          <el-button @click="$refs.pwdForm.resetFields()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { updatePassword, getPublicKey } from '@/api/front/user'
import JSEncrypt from 'jsencrypt'

export default {
  name: 'UserPassword',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'));
      } else if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的新密码不一致!'));
      } else {
        callback();
      }
    };
    const validateNewPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'));
      } else if (value === this.pwdForm.oldPassword) {
        callback(new Error('新密码不能与原密码相同!'));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validateNewPassword, trigger: 'blur' },
          { min: 6, message: '新密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.pwdForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 获取公钥进行 RSA 加密
            const keyData = await getPublicKey();
            const publicKey = keyData.publicKey;

            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(publicKey);
            
            const payload = {
              oldPassword: encryptor.encrypt(this.pwdForm.oldPassword),
              newPassword: encryptor.encrypt(this.pwdForm.newPassword)
            };

            await updatePassword(payload);
            
            this.$message.success('密码修改成功，请重新登录！');
            
            // 修改密码成功后通常需要清空 Token 并跳转到登录页
            localStorage.removeItem('user-token');
            setTimeout(() => {
              this.$router.push('/login');
            }, 1500);

          } catch (error) {
            console.error('密码修改失败', error);
          } finally {
            this.loading = false;
          }
        }
      });
    }
  }
}
</script>

<style scoped>
.user-password-page {
  padding: 20px 0;
}
.password-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.mb-20 {
  margin-bottom: 20px;
}
.pwd-form {
  max-width: 500px;
  margin: 40px auto 20px;
}
.submit-btn {
  background-color: #ff5000;
  border-color: #ff5000;
}
.submit-btn:hover {
  background-color: #ff6a26;
  border-color: #ff6a26;
}
</style>
