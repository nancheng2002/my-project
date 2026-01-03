<template>
  <div>
    <AppTopbar :showSearch="true" @search="onSearch" />
    <div class="games-container">
      <el-button @click="goToAdmin">去后台管理</el-button>
      <div class="game-list">
        <!-- 渲染游戏列表 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import AppTopbar from "../components/AppTopbar.vue";
import { useRouter } from "vue-router";

const router = useRouter();

const onSearch = (keyword) => {
  console.log("Searching for:", keyword);
  // 调用搜索 API
};

const goToAdmin = () => {
  router.push("/admin/games");
};
</script>
