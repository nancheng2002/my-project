<template>
  <div style="padding:16px">
    <div style="display:flex; gap:12px; align-items:center; margin-bottom:12px">
      <router-link to="/">首页</router-link>
      <router-link to="/admin/games">后台：游戏管理</router-link>
    </div>

    <!-- 路由视图 -->
    <router-view />

    <!-- 登录按钮 -->
    <div>
      <el-button type="primary" @click="goLogin">去登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 跳转到登录页面
function goLogin() {
  router.push('/login');
}
</script>
