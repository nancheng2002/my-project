<template>
  <header class="topbar">
    <div class="topbar-inner">
      <div class="brand" @click="$emit('home')">
        <div class="logo"></div>
        <div>
          <div class="brand-title">{{ title }}</div>
          <div class="brand-sub">{{ subTitle }}</div>
        </div>
      </div>

      <div class="topbar-actions">
        <el-input
            v-if="showSearch"
            v-model="kw"
            class="search"
            :placeholder="searchPlaceholder"
            clearable
            @keyup.enter="$emit('search', kw)"
            @clear="$emit('search', kw)"
        >
          <template #append>
            <el-button @click="$emit('search', kw)">搜索</el-button>
          </template>
        </el-input>

        <slot name="actions" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  title: { type: String, default: "Game Site" },
  subTitle: { type: String, default: "游戏展示与管理平台" },

  showSearch: { type: Boolean, default: false },
  keyword: { type: String, default: "" },
  searchPlaceholder: { type: String, default: "搜索游戏标题…（回车搜索）" },
});

const emit = defineEmits(["update:keyword", "home", "search"]);

const kw = computed({
  get: () => props.keyword || "",
  set: (v) => emit("update:keyword", v || ""),
});
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eee;
}
.topbar-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.brand {
  display: flex;
  gap: 10px;
  align-items: center;
  cursor: pointer;
  user-select: none;
}
.logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #5b8cff, #8a5bff);
}
.brand-title {
  font-weight: 900;
  font-size: 16px;
}
.brand-sub {
  font-size: 12px;
  color: #777;
  margin-top: 2px;
}
.topbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search {
  width: 360px;
}
@media (max-width: 980px) {
  .search {
    width: 260px;
  }
}
@media (max-width: 640px) {
  .topbar-inner {
    flex-direction: column;
    align-items: stretch;
  }
  .search {
    width: 100%;
  }
}
</style>
