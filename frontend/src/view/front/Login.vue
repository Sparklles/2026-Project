<template>
  <div class="user-login-container">
    <header class="login-header">
      <div class="header-content">
        <div class="logo-area" @click="$router.push('/')">
          <i class="el-icon-ship logo-icon"></i>
          <span class="logo-text">航海时代商城</span>
        </div>
        <span class="welcome-text">欢迎登录</span>
      </div>
    </header>

    <div class="login-main">
      <div class="login-box">
        <div class="login-tabs-wrapper">
          <el-tabs v-model="loginType" class="login-tabs" stretch>
            <el-tab-pane label="密码登录" name="2"></el-tab-pane>
            <el-tab-pane label="手机号登录" name="1"></el-tab-pane>
          </el-tabs>
        </div>

        <el-form ref="loginForm" :model="loginForm" :rules="rules" class="login-form">
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              :placeholder="loginType === '1' ? '请输入手机号' : '会员名/邮箱/手机号'"
              prefix-icon="el-icon-user"
              clearable
              size="large"
            ></el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入登录密码"
              prefix-icon="el-icon-lock"
              type="password"
              show-password
              size="large"
              @keyup.enter.native="handleLogin"
            ></el-input>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
            <el-link type="primary" :underline="false" class="forgot-pwd">忘记密码？</el-link>
          </div>

          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="login-btn" 
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>

          <div class="login-footer">
            <el-link type="primary" :underline="false" @click="$router.push('/register')">免费注册</el-link>
          </div>
        </el-form>
      </div>
    </div>
    
    <footer class="login-footer-bar">
      <p>© {{ new Date().getFullYear() }} 航海时代商城 版权所有</p>
    </footer>
  </div>
</template>

<script>
import { userLogin, getPublicKey } from '@/api/front/user'
import JSEncrypt from 'jsencrypt'

export default {
  name: 'FrontLogin',
  data() {
    return {
      loginType: '2', // 2: 用户名密码, 1: 手机号密码
      rememberMe: false,
      loginForm: {
        account: '',
        password: ''
      },
      loading: false,
      rules: {
        account: [
          { required: true, message: '请输入登录账号', trigger: 'blur' }
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
              expectedRole: '1'
            };
            
            // 3. 发起登录请求
            const token = await userLogin(payload);
            
            if (token) {
              this.$message.success('登录成功！');
              // 如果后端返回的数据直接是 token 字符串或者是包含了 token 的对象
              const tokenValue = typeof token === 'string' ? token : (token.token || token.data);
              
              if (tokenValue) {
                localStorage.setItem('user-token', tokenValue);
              }
              
              // 登录成功后跳转到首页或重定向地址
              const redirect = this.$route.query.redirect || '/';
              this.$router.push(redirect);
            } else {
               this.$message.error('登录失败，未获取到凭证');
            }
          } catch (error) {
            console.error('登录异常', error);
            this.$message.error('登录异常，请检查账号和密码');
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
.user-login-container {
  background-color: #fff;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部简约导航 */
.login-header {
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

/* 登录主体区（带背景图） */
.login-main {
  flex: 1;
  /* 航海主题背景图 */
  background: url('https://images.unsplash.com/photo-1454496522488-7a8e488e8606?q=80&w=1920&auto=format&fit=crop') no-repeat center center;
  background-size: cover;
  position: relative;
}

/* 登录框 */
.login-box {
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

.login-tabs-wrapper {
  margin-bottom: 20px;
}

/* 定制选项卡为商城主题色 */
::v-deep .login-tabs .el-tabs__item.is-active {
  color: #ff5000;
  font-weight: bold;
}
::v-deep .login-tabs .el-tabs__active-bar {
  background-color: #ff5000;
}
::v-deep .login-tabs .el-tabs__item:hover {
  color: #ff5000;
}
::v-deep .login-tabs .el-tabs__item {
  font-size: 16px;
}

.login-form {
  margin-top: 20px;
}

/* 定制输入框焦点颜色 */
::v-deep .el-input__inner:focus {
  border-color: #ff5000;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-pwd {
  color: #666;
  font-size: 14px;
}
.forgot-pwd:hover {
  color: #ff5000;
}

/* 登录大按钮 */
.login-btn {
  width: 100%;
  background-color: #ff5000;
  border-color: #ff5000;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 5px;
  border-radius: 4px;
  transition: all 0.3s;
}
.login-btn:hover {
  background-color: #ff6a26;
  border-color: #ff6a26;
}

.login-footer {
  text-align: right;
  margin-top: 15px;
}
::v-deep .login-footer .el-link {
  color: #ff5000;
  font-size: 14px;
}

/* 底部栏 */
.login-footer-bar {
  text-align: center;
  padding: 30px 0;
  color: #999;
  font-size: 14px;
}
</style>
