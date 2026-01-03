<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="login-title">登录</h2>
      <el-form :model="form" status-icon>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password"></el-input>
        </el-form-item>
        <el-button type="primary" @click="login" :loading="loading" class="login-button">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

export default {
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: ''
      }
    };
  },
  methods: {
    async login() {
      if (!this.form.username || !this.form.password) {
        ElMessage.error('请输入用户名和密码');
        return;
      }
      this.loading = true;
      try {
        const user = await authApi.login({
          username: this.form.username,
          password: this.form.password
        });
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('userInfo', JSON.stringify(user));
        ElMessage.success('登录成功');
        this.$router.push('/');
      } catch (error) {
        ElMessage.error(error.message || '登录失败');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.login-card {
  width: 360px;
}

.login-title {
  text-align: center;
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
}
</style>
