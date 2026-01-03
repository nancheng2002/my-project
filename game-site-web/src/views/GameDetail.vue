<template>
  <div class="page">
    <!-- 顶部 -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand" @click="goHome">
          <div class="logo"></div>
          <div>
            <div class="brand-title">Game Site</div>
            <div class="brand-sub">游戏展示与管理平台</div>
          </div>
        </div>

        <div class="topbar-actions">
          <el-input
              v-model="keyword"
              class="search"
              placeholder="搜索游戏标题…（回车回首页搜索）"
              clearable
              @keyup.enter="doSearch"
              @clear="doSearch"
          >
            <template #append>
              <el-button @click="doSearch">搜索</el-button>
            </template>
          </el-input>

          <el-button type="primary" plain @click="goAdmin">后台管理</el-button>
        </div>
      </div>
    </header>

    <main class="container">
      <!-- 面包屑 -->
      <div class="crumbs">
        <span class="crumb-link" @click="goHome">首页</span>
        <span class="sep">/</span>
        <span class="crumb-link" @click="goHome">{{ game?.categoryName || categoryName || "未分类" }}</span>
        <span class="sep">/</span>
        <span class="crumb-current">{{ game?.title || "详情" }}</span>
      </div>

      <el-skeleton :rows="10" animated v-if="loading" />

      <template v-else>
        <el-empty v-if="!game" description="未找到该游戏（可能已删除）">
          <el-button type="primary" @click="goHome">返回首页</el-button>
        </el-empty>

        <template v-else>
          <!-- 头部卡片 -->
          <section class="detail">
            <!-- 左侧：封面 -->
            <div class="poster">
              <template v-if="game.coverUrl">
                <el-image class="poster-img" :src="game.coverUrl" fit="cover" />
              </template>
              <template v-else>
                <div class="poster-ph">
                  <div class="ph-title">No Cover</div>
                  <div class="ph-sub">暂无封面</div>
                </div>
              </template>
            </div>

            <!-- 右侧：信息 -->
            <div class="info">
              <div class="title-row">
                <div class="game-title">{{ game.title }}</div>
                <div class="tags">
                  <el-tag size="small" :type="game.status === 1 ? 'success' : 'info'">
                    {{ game.status === 1 ? "上架" : "下架" }}
                  </el-tag>
                  <el-tag size="small" type="warning" v-if="game.platform">{{ game.platform }}</el-tag>
                  <el-tag size="small" type="info" v-if="game.categoryName || categoryName">
                    {{ game.categoryName || categoryName }}
                  </el-tag>
                </div>
              </div>

              <div class="cards">
                <div class="mini">
                  <div class="mini-label">游戏ID</div>
                  <div class="mini-value">{{ game.id }}</div>
                </div>
                <div class="mini">
                  <div class="mini-label">分类</div>
                  <div class="mini-value">{{ game.categoryName || categoryName || "-" }}</div>
                </div>
                <div class="mini">
                  <div class="mini-label">平台</div>
                  <div class="mini-value">{{ game.platform || "-" }}</div>
                </div>
              </div>

              <div class="intro">
                <div class="intro-label">一句话介绍</div>
                <div class="intro-text">
                  {{ game.intro || "暂无简介" }}
                </div>
              </div>

              <div class="actions">
                <el-button type="primary" @click="goAdminEdit">去后台编辑</el-button>
                <el-button @click="goHome">返回首页</el-button>
                <el-button plain @click="reloadDetail">刷新详情</el-button>
              </div>
            </div>
          </section>

          <!-- 详细介绍 -->
          <section class="section">
            <div class="section-title">详细介绍</div>

            <div v-if="!game.content" class="empty-content">
              暂无详细介绍（可以去后台编辑写 Markdown 内容）
            </div>

            <div v-else class="md-wrap">
              <div class="md" v-html="contentHtml"></div>
            </div>
          </section>
        </template>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import http from "../api/http";

import { marked } from "marked";
import DOMPurify from "dompurify";
import hljs from "highlight.js";
import "highlight.js/styles/github.css";

/** 统一解包：兼容 axios / 拦截器直返 / {code,data} */
function unwrap(res) {
  if (res == null) return res;

  // axios response
  if (res && typeof res === "object" && "data" in res) {
    const d = res.data;
    if (d && typeof d === "object" && "data" in d) return d.data;
    return d;
  }

  // 后端包装：{ code, data, msg } 或 { data: xxx }
  if (res && typeof res === "object" && "data" in res) return res.data;

  return res;
}

/** Markdown 渲染（带高亮 + 清理） */
marked.setOptions({
  breaks: true,
  gfm: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value;
    }
    return hljs.highlightAuto(code).value;
  },
});

function renderMarkdown(md) {
  const raw = marked.parse(md || "");
  return DOMPurify.sanitize(raw);
}

// ---------- 基础状态 ----------
const router = useRouter();
const route = useRoute();

const loading = ref(false);
const keyword = ref("");

const game = ref(null);
const categories = ref([]);

// ---------- 路由动作 ----------
function goHome() {
  router.push("/");
}
function goAdmin() {
  router.push("/admin/games");
}
function doSearch() {
  router.push({ path: "/", query: { keyword: keyword.value || "" } });
}
function goAdminEdit() {
  router.push({ path: "/admin/games", query: { editId: String(game.value?.id || "") } });
}

// ---------- 分类 ----------
const categoryName = computed(() => {
  const id = Number(game.value?.categoryId);
  if (!id) return "";
  const hit = (categories.value || []).find((c) => Number(c.id) === id);
  return hit?.name || "";
});

async function loadCategories() {
  const res = await http.get("/api/home/categories");
  const data = unwrap(res);
  categories.value = Array.isArray(data) ? data : [];
}

// ---------- 详情 ----------
async function loadDetail() {
  const id = route.params.id;
  if (!id) {
    game.value = null;
    return;
  }

  loading.value = true;
  try {
    const res = await http.get(`/api/games/${id}`);
    const data = unwrap(res);
    if (!data) {
      game.value = null;
      return;
    }

    game.value = {
      ...data,
      categoryName: data.categoryName || categoryName.value || "",
    };
  } finally {
    loading.value = false;
  }
}

async function reloadDetail() {
  await loadCategories();
  await loadDetail();
}

// Markdown HTML
const contentHtml = computed(() => renderMarkdown(game.value?.content || ""));

// ---------- 初始化 ----------
onMounted(async () => {
  await reloadDetail();
});

// 详情页复用时切 id 自动刷新
watch(
    () => route.params.id,
    async () => {
      await loadDetail();
    }
);
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f7fb;
}

/* topbar */
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

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 18px;
}

/* crumbs */
.crumbs {
  color: #7b8499;
  font-size: 12px;
  margin-bottom: 12px;
  user-select: none;
}
.crumb-link {
  color: #5b8cff;
  cursor: pointer;
}
.crumb-link:hover {
  text-decoration: underline;
}
.sep {
  margin: 0 6px;
  color: #c0c6d4;
}
.crumb-current {
  color: #3a4055;
  font-weight: 700;
}

/* detail header */
.detail {
  display: grid;
  grid-template-columns: 520px 1fr;
  gap: 14px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 16px;
  padding: 16px;
}

/* poster */
.poster {
  border-radius: 16px;
  overflow: hidden;
  background: #f6f7fb;
  border: 1px solid #eef0f6;
}
.poster-img {
  width: 100%;
  height: 100%;
  min-height: 320px;
}
.poster-ph {
  min-height: 320px;
  display: grid;
  place-content: center;
  text-align: center;
  background: linear-gradient(135deg, #eef2ff, #f6f7fb);
}
.ph-title {
  font-weight: 900;
  font-size: 16px;
  margin-bottom: 6px;
}
.ph-sub {
  font-size: 12px;
  color: #777;
}

/* info */
.info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.game-title {
  font-size: 22px;
  font-weight: 900;
  color: #1f2330;
}
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.mini {
  border: 1px solid #eef0f6;
  border-radius: 14px;
  padding: 12px;
  background: #fafbff;
}
.mini-label {
  font-size: 12px;
  color: #7b8499;
}
.mini-value {
  margin-top: 6px;
  font-weight: 900;
  color: #1f2330;
  word-break: break-all;
}

.intro {
  border: 1px dashed #e3e7f5;
  background: #fff;
  border-radius: 14px;
  padding: 12px;
}
.intro-label {
  font-size: 12px;
  color: #7b8499;
  margin-bottom: 6px;
}
.intro-text {
  color: #2f3548;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 4px;
}

/* section */
.section {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 16px;
  padding: 16px;
  margin-top: 14px;
}
.section-title {
  font-size: 18px;
  font-weight: 900;
  margin-bottom: 12px;
}
.empty-content {
  color: #7b8499;
  background: #fafbff;
  border: 1px dashed #e3e7f5;
  border-radius: 14px;
  padding: 14px;
}

/* Markdown 容器 */
.md-wrap {
  border: 1px solid #eef0f6;
  background: #ffffff;
  border-radius: 14px;
  padding: 16px 18px;
}

.md :deep(h1),
.md :deep(h2),
.md :deep(h3) {
  margin: 18px 0 12px;
  color: #1f2330;
  font-weight: 900;
  line-height: 1.25;
}
.md :deep(h1) { font-size: 22px; }
.md :deep(h2) { font-size: 18px; }
.md :deep(h3) { font-size: 16px; }

.md :deep(p) {
  margin: 10px 0;
  color: #2f3548;
  line-height: 1.9;
  font-size: 14px;
}

.md :deep(blockquote) {
  margin: 12px 0;
  padding: 10px 12px;
  border-left: 4px solid #5b8cff;
  background: #f5f8ff;
  border-radius: 10px;
  color: #3a4055;
}

.md :deep(ul),
.md :deep(ol) {
  margin: 10px 0 10px 22px;
  color: #2f3548;
  line-height: 1.9;
  font-size: 14px;
}
.md :deep(li) { margin: 6px 0; }

.md :deep(code) {
  padding: 2px 6px;
  border-radius: 8px;
  background: #f2f4ff;
  color: #3b3f66;
  font-size: 13px;
}

.md :deep(pre) {
  margin: 14px 0;
  padding: 12px;
  border-radius: 12px;
  background: #0f172a;
  color: #e5e7eb;
  overflow: auto;
  border: 1px solid #111827;
  line-height: 1.7;
  font-size: 13px;
}
.md :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}

.md :deep(img){
  max-width: 100%;
  border-radius: 12px;
  display: block;
  margin: 12px auto;
  border: 1px solid #eef0f6;
}

.md :deep(a){
  color: #5b8cff;
  font-weight: 700;
  text-decoration: none;
}
.md :deep(a:hover){
  text-decoration: underline;
}

.md :deep(table){
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid #eef0f6;
}
.md :deep(th),
.md :deep(td){
  border-bottom: 1px solid #eef0f6;
  padding: 10px 12px;
  font-size: 13px;
}
.md :deep(th){
  background: #fafbff;
  color: #1f2330;
  text-align: left;
}

/* responsive */
@media (max-width: 980px) {
  .detail { grid-template-columns: 1fr; }
  .search { width: 260px; }
}
@media (max-width: 640px) {
  .topbar-inner { flex-direction: column; align-items: stretch; }
  .search { width: 100%; }
  .cards { grid-template-columns: 1fr; }
}
</style>
