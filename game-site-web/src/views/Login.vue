<template>
  <div>
    <el-form :model="form" status-icon>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="form.password"></el-input>
      </el-form-item>
      <el-button type="primary" @click="login">登录</el-button>
    </el-form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      }
    };
  },
  methods: {
    login() {
      this.$axios.post('http://localhost:8080/perform_login', {
        username: this.form.username,
        password: this.form.password
      })
          .then(response => {
            // 登录成功，跳转到主页
            this.$router.push('/home');
          })
          .catch(error => {
            console.error('登录失败', error);
          });
    }
  }
}
</script>
