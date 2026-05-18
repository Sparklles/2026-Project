<template>
  <div class="user-profile-page">
    <el-card class="profile-card">
      <div slot="header" class="clearfix">
        <span>个人信息</span>
      </div>
      <el-form ref="profileForm" :model="profileForm" label-width="100px" class="profile-form">
        <!-- 头像展示区 -->
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="profileForm.avatarUrl || defaultAvatar"></el-avatar>
          <div class="avatar-tip">支持 jpg/jpeg/png/webp/gif，大小不超过 5MB</div>
          <el-upload
            class="avatar-upload"
            action="#"
            :show-file-list="false"
            :http-request="handleAvatarUpload"
            :before-upload="beforeAvatarUpload"
            :disabled="avatarUploading">
            <el-button size="small" type="primary" :loading="avatarUploading">选择头像</el-button>
          </el-upload>
        </div>

        <el-divider></el-divider>

        <!-- 只读信息区 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="登录账号">
              <el-input v-model="profileForm.loginAccount" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码">
              <el-input :value="maskPhone(profileForm.phone)" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入电子邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="注册时间">
              <el-input :value="formatDate(profileForm.createTime)" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">可编辑信息</el-divider>

        <!-- 可编辑信息区 -->
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="profileForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">保密</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="profileForm.birthday"
            type="date"
            placeholder="选择出生日期"
            value-format="yyyy-MM-dd"
            style="width: 100%;">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="个性签名" prop="signature">
          <el-input
            type="textarea"
            :rows="3"
            placeholder="写一句喜欢的话作为签名吧..."
            v-model="profileForm.signature"
            maxlength="100"
            show-word-limit>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="loading" class="save-btn">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getUserProfile, updateUserProfile, updateEmail, uploadAvatar } from '@/api/front/user'

export default {
  name: 'UserProfile',
  data() {
    return {
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      loading: false,
      avatarUploading: false,
      profileForm: {
        loginAccount: '',
        phone: '',
        email: '',
        createTime: '',
        nickname: '',
        avatarUrl: '',
        gender: 0,
        birthday: '',
        signature: ''
      },
      rules: {
        email: [
          { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
        ]
      }
    }
  },
  created() {
    this.fetchProfile();
  },
  methods: {
    // 获取个人信息
    async fetchProfile() {
      try {
        const data = await getUserProfile();
        if (data) {
          // 只保留需要展示和编辑的字段
          this.profileForm = {
            loginAccount: data.loginAccount || '',
            phone: data.phone || '',
            email: data.email || '',
            createTime: data.createTime || '',
            nickname: data.nickname || '',
            avatarUrl: data.avatarUrl || '',
            gender: data.gender || 0,
            birthday: data.birthday || '',
            signature: data.signature || ''
          };
        }
      } catch (error) {
        console.error('获取个人信息失败', error);
      }
    },
    beforeAvatarUpload(file) {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
      const allowedExts = ['jpg', 'jpeg', 'png', 'webp', 'gif']
      const ext = file.name && file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
      const validType = allowedTypes.includes(file.type) || allowedExts.includes(ext)
      const validSize = file.size / 1024 / 1024 <= 5

      if (!validType) {
        this.$message.error('头像仅支持 jpg/jpeg/png/webp/gif 格式')
        return false
      }
      if (!validSize) {
        this.$message.error('头像大小不能超过 5MB')
        return false
      }
      return true
    },

    async handleAvatarUpload({ file }) {
      this.avatarUploading = true
      try {
        const avatarUrl = await uploadAvatar(file)
        if (!avatarUrl) {
          this.$message.error('头像上传失败，未获取到图片地址')
          return
        }
        this.profileForm.avatarUrl = avatarUrl
      } catch (error) {
        console.error('头像上传失败', error)
        this.$message.error(error.message || '头像上传失败')
      } finally {
        this.avatarUploading = false
      }
    },
    // 保存修改
    async saveProfile() {
      this.$refs.profileForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 1. 修改基本个人信息
            const profileData = {
              nickname: this.profileForm.nickname,
              avatarUrl: this.profileForm.avatarUrl,
              gender: this.profileForm.gender,
              birthday: this.profileForm.birthday,
              signature: this.profileForm.signature
            };
            
            // 2. 修改邮箱 (独立接口)
            // 根据您的描述，参数仅为 email
            const emailValue = this.profileForm.email;

            // 同时调用两个接口
            await Promise.all([
              updateUserProfile(profileData),
              updateEmail(emailValue)
            ]);

            this.$message.success('个人资料更新成功！');
          } catch (error) {
            console.error('更新失败', error);
          } finally {
            this.loading = false;
          }
        }
      });
    },
    // 手机号掩码脱敏 (前端防御性展示)
    maskPhone(phone) {
      if (!phone) return '未绑定';
      return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
    },
    // 格式化时间
    formatDate(timeStr) {
      if (!timeStr) return '';
      // 简单截取 T 前面的日期部分，或更复杂的格式化
      return timeStr.split('T')[0];
    }
  }
}
</script>

<style scoped>
.user-profile-page {
  padding: 20px 0;
}
.profile-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}
.avatar-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}
.profile-form {
  max-width: 800px;
  margin: 0 auto;
}
.save-btn {
  background-color: #ff5000;
  border-color: #ff5000;
  width: 150px;
}
.save-btn:hover {
  background-color: #ff6a26;
  border-color: #ff6a26;
}
</style>





