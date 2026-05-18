<template>
  <div class="admin-login-page">
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-logo">
          <i class="el-icon-ship"></i>
        </div>
        <h1>航海图书管理后台</h1>
        <p>统一维护商品、用户、订单、推荐与经营数据</p>
        <div class="brand-meta">
          <span><i class="el-icon-reading"></i> 图书商品维护</span>
          <span><i class="el-icon-data-analysis"></i> 数据统计看板</span>
          <span><i class="el-icon-setting"></i> 推荐规则配置</span>
        </div>
      </section>

      <section class="login-card">
        <div class="login-heading">
          <h2>管理员登录</h2>
          <p>SailMart Admin Console</p>
        </div>

        <el-tabs v-model="loginType" class="login-tabs" stretch>
          <el-tab-pane label="用户名登录" name="2"></el-tab-pane>
          <el-tab-pane label="手机号登录" name="1"></el-tab-pane>
        </el-tabs>

        <el-form ref="loginForm" :model="loginForm" :rules="rules" class="login-form">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              :placeholder="loginType === '1' ? '请输入手机号' : '请输入用户名'"
              prefix-icon="el-icon-user"
              class="custom-input"
              clearable
            ></el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              type="password"
              class="custom-input"
              show-password
              @keyup.enter.native="handleLogin"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          © {{ new Date().getFullYear() }} SailMart Admin
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { login } from '../../api/admin/user'
import { getPublicKey } from '@/api/front/user'
import JSEncrypt from 'jsencrypt'

export default {
  name: 'AdminLogin',
  data() {
    return {
      loginType: '2', // 默认用户名登录
      loginForm: {
        account: '',
        password: ''
      },
      loading: false,
      rules: {
        account: [
          { required: true, message: '请输入账号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 3, message: '密码长度不能小于3位', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 1. 获取公钥
            const keyData = await getPublicKey();
            const publicKey = keyData.publicKey;

            // 2. 使用 RSA 加密密码
            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(publicKey);
            const encryptedPassword = encryptor.encrypt(this.loginForm.password);

            const payload = {
              account: this.loginForm.account,
              password: encryptedPassword, // 传输加密后的密码
              type: this.loginType,
              expectedRole: "2"
            };

            const data = await login(payload);

            if (data) {
              this.$message.success('登录成功！');
              // 获取 token 值并存入 localStorage
              const tokenValue = typeof data === 'string' ? data : (data.token || data.data);
              if (tokenValue) {
                localStorage.setItem('admin-token', tokenValue);
              }

              this.$emit('login-success', data);

              // 登录成功后跳转到后台管理首页
              this.$router.push('/admin').catch(err => {
                // 如果已经在该路由，忽略错误
                if (err.name !== 'NavigationDuplicated') {
                  console.error(err);
                }
              });
            } else {
              this.$message.error('登录失败，未获取到凭证');
            }
          } catch (error) {
            console.error(error);
            this.$message.error('登录异常：' + (error.response?.data?.message || error.message));
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
.admin-login-page {
  position: relative;
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(48, 65, 86, 0.10) 0%, rgba(48, 65, 86, 0.02) 38%, rgba(64, 158, 255, 0.08) 100%),
    #e8edf3;
  overflow: hidden;
}

.admin-login-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(48, 65, 86, 0.055) 1px, transparent 1px),
    linear-gradient(90deg, rgba(48, 65, 86, 0.055) 1px, transparent 1px);
  background-size: 42px 42px;
  opacity: 0.8;
  pointer-events: none;
}

.admin-login-page::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(118deg, rgba(48, 65, 86, 0.08) 0%, rgba(48, 65, 86, 0.08) 28%, transparent 28.2%, transparent 100%);
  pointer-events: none;
}

.login-shell {
  position: relative;
  z-index: 1;
  width: 960px;
  min-height: 560px;
  display: grid;
  grid-template-columns: 420px 1fr;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(0, 21, 41, 0.12);
  overflow: hidden;
}

.brand-panel {
  position: relative;
  padding: 56px 44px;
  color: #fff;
  background: #304156;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.22), rgba(43, 54, 67, 0.12));
  pointer-events: none;
}

.brand-panel > * {
  position: relative;
  z-index: 1;
}

.brand-logo {
  width: 68px;
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b3643;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  margin-bottom: 28px;
}

.brand-logo i {
  font-size: 38px;
  color: #409eff;
}

.brand-panel h1 {
  margin: 0 0 14px 0;
  font-size: 28px;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 0;
}

.brand-panel p {
  margin: 0;
  color: #bfcbd9;
  font-size: 14px;
  line-height: 1.8;
}

.brand-meta {
  margin-top: 46px;
  display: grid;
  gap: 14px;
}

.brand-meta span {
  display: flex;
  align-items: center;
  color: #d8e2ee;
  font-size: 14px;
}

.brand-meta i {
  width: 24px;
  margin-right: 10px;
  color: #409eff;
  font-size: 18px;
}

.login-card {
  padding: 70px 64px 36px;
  display: flex;
  flex-direction: column;
}

.login-heading {
  margin-bottom: 26px;
}

.login-heading h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 0;
}

.login-heading p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.login-tabs {
  margin-bottom: 24px;
}

::v-deep .login-tabs .el-tabs__item {
  color: #606266;
  font-size: 15px;
  height: 42px;
  line-height: 42px;
}

::v-deep .login-tabs .el-tabs__item.is-active {
  color: #409eff;
  font-weight: 600;
}

::v-deep .login-tabs .el-tabs__active-bar {
  background-color: #409eff;
}

.login-form {
  flex: 1;
}

::v-deep .custom-input .el-input__inner {
  height: 46px;
  line-height: 46px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  color: #303133;
  background: #fff;
  padding-left: 52px !important;
  transition: border-color 0.2s, box-shadow 0.2s;
}

::v-deep .custom-input .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.12);
}

::v-deep .custom-input .el-input__prefix {
  left: 16px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

::v-deep .custom-input .el-input__icon {
  width: 24px;
  color: #909399;
  font-size: 17px;
  line-height: 46px;
}

::v-deep .custom-input .el-input__suffix {
  right: 12px;
}

.login-btn {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
  background: #409eff;
  border-color: #409eff;
}

.login-btn:hover,
.login-btn:focus {
  background: #66b1ff;
  border-color: #66b1ff;
}

.login-footer {
  color: #c0c4cc;
  font-size: 12px;
  text-align: center;
}

@media (max-width: 900px) {
  .login-shell {
    width: calc(100vw - 32px);
    grid-template-columns: 1fr;
  }

  .brand-panel {
    display: none;
  }

  .login-card {
    padding: 48px 28px 28px;
  }
}
</style>



