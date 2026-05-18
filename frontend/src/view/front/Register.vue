<template>
  <div class="user-register-container">
    <header class="register-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代商城</span>
        </div>
        <span class="welcome-text">欢迎注册</span>
      </div>
    </header>

    <div class="register-main">
      <div class="register-box">
        <div class="register-tabs-wrapper">
          <el-tabs v-model="registerType" class="register-tabs" stretch>
            <el-tab-pane label="用户名注册" name="2"></el-tab-pane>
            <el-tab-pane label="手机号注册" name="1"></el-tab-pane>
          </el-tabs>
        </div>

        <el-form ref="registerForm" :model="registerForm" :rules="rules" class="register-form">
          <el-form-item prop="account">
            <el-input
              v-model="registerForm.account"
              :placeholder="registerType === '1' ? '请输入11位手机号' : '请设置会员名（至少4个字符）'"
              prefix-icon="el-icon-user"
              clearable
              size="large"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              placeholder="请设置登录密码"
              prefix-icon="el-icon-lock"
              type="password"
              show-password
              size="large"
            ></el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              placeholder="请再次确认登录密码"
              prefix-icon="el-icon-lock"
              type="password"
              show-password
              size="large"
              @keyup.enter.native="handleRegister"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="register-btn" 
              @click="handleRegister"
            >
              立即注册
            </el-button>
          </el-form-item>

          <div class="register-footer">
            <span style="color: #666; font-size: 14px;">已有账号？</span>
            <el-link type="primary" :underline="false" @click="$router.push('/login')">直接登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
    
    <footer class="register-footer-bar">
      <p>© {{ new Date().getFullYear() }} 航海时代商城 版权所有</p>
    </footer>
  </div>
</template>

<script>
import { userRegister, getPublicKey } from '@/api/front/user'
import JSEncrypt from 'jsencrypt'

export default {
  name: 'FrontRegister',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致!'));
      } else {
        callback();
      }
    };

    const validateAccount = (rule, value, callback) => {
      if (!value) {
        return callback(new Error(this.registerType === '1' ? '请输入手机号' : '请输入会员名'));
      }
      if (this.registerType === '1') {
        if (!/^1[3-9]\d{9}$/.test(value)) {
          return callback(new Error('请输入有效的11位手机号码'));
        }
      } else {
        if (value.length < 4) {
          return callback(new Error('会员名长度不能小于4位'));
        }
      }
      callback();
    };

    return {
      registerType: '2', // 2: 用户名注册, 1: 手机号注册
      registerForm: {
        account: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      rules: {
        account: [
          { required: true, validator: validateAccount, trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请设置密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    // 切换注册类型时，清空账号输入框并重置校验
    registerType() {
      this.registerForm.account = '';
      this.$refs.registerForm.clearValidate(['account']);
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 1. 获取公钥
            const keyData = await getPublicKey();
            const publicKey = keyData.publicKey;

            // 2. 使用 RSA 加密密码
            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(publicKey);
            const encryptedPassword = encryptor.encrypt(this.registerForm.password);

            const payload = {
              account: this.registerForm.account,
              password: encryptedPassword, // 传输加密后的密码
              type: Number(this.registerType)
            };
            
            // 3. 发起注册请求
            await userRegister(payload);
            
            this.$message.success('注册成功！即将跳转到登录页...');
            
            // 延迟跳转，让用户看清提示
            setTimeout(() => {
              this.$router.push({
                path: '/login',
                query: { account: this.registerForm.account }
              });
            }, 1500);
            
          } catch (error) {
            console.error('注册异常', error);
            // 错误提示由 request.js 拦截器统一处理，如果需要个性化提示也可在这里补充
            // this.$message.error('注册失败，请稍后重试');
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
.user-register-container {
  background-color: #fff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部简约导航 */
.register-header {
  height: 80px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}
.header-content {
  width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
}
.logo-area {
  display: flex;
  align-items: center;
  color: #ff5000;
  cursor: pointer;
  margin-right: 20px;
}
.logo-icon {
  font-size: 40px;
  margin-right: 10px;
}
.logo-text {
  font-size: 26px;
  font-weight: bold;
  letter-spacing: 1px;
}
.welcome-text {
  font-size: 20px;
  color: #333;
  margin-left: 20px;
  border-left: 1px solid #ddd;
  padding-left: 20px;
  line-height: 28px;
}

/* 注册主体区（带背景图） */
.register-main {
  flex: 1;
  /* 航海主题背景图 */
  background: url('https://images.unsplash.com/photo-1454496522488-7a8e488e8606?q=80&w=1920&auto=format&fit=crop') no-repeat center center;
  background-size: cover;
  position: relative;
}

/* 注册框 */
.register-box {
  width: 380px;
  background: #fff;
  position: absolute;
  right: 15%;
  top: 50%;
  transform: translateY(-50%);
  border-radius: 8px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
  padding: 30px;
}

.register-tabs-wrapper {
  margin-bottom: 20px;
}

/* 定制选项卡为商城主题色 */
::v-deep .register-tabs .el-tabs__item.is-active {
  color: #ff5000;
  font-weight: bold;
}
::v-deep .register-tabs .el-tabs__active-bar {
  background-color: #ff5000;
}
::v-deep .register-tabs .el-tabs__item:hover {
  color: #ff5000;
}
::v-deep .register-tabs .el-tabs__item {
  font-size: 16px;
}

.register-form {
  margin-top: 20px;
}

/* 定制输入框焦点颜色 */
::v-deep .el-input__inner:focus {
  border-color: #ff5000;
}

/* 注册大按钮 */
.register-btn {
  width: 100%;
  background-color: #ff5000;
  border-color: #ff5000;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 5px;
  border-radius: 4px;
  margin-top: 10px;
  transition: all 0.3s;
}
.register-btn:hover {
  background-color: #ff6a26;
  border-color: #ff6a26;
}

.register-footer {
  text-align: right;
  margin-top: 15px;
}
::v-deep .register-footer .el-link {
  color: #ff5000;
  font-size: 14px;
}

/* 底部栏 */
.register-footer-bar {
  text-align: center;
  padding: 30px 0;
  color: #999;
  font-size: 14px;
}
</style>
