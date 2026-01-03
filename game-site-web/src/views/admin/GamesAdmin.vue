<template>
  <div class="admin-page">
    <!-- 顶部概览卡片 -->
    <div class="card hero" ref="heroRef">
      <div class="hero-left">
        <div class="hero-title">Game Site Admin</div>
        <div class="hero-sub">管理你的游戏：封面 / Markdown / 上下架 / 搜索筛选</div>
      </div>

      <div class="hero-stats">
        <div class="stat" @click="onClickTotal">
          <div class="stat-num">{{ page.total }}</div>
          <div class="stat-label">当前结果</div>
        </div>

        <div class="stat" @click="onClickCategories">
          <div class="stat-num">{{ categories.length }}</div>
          <div class="stat-label">分类数</div>
        </div>
      </div>
    </div>

    <!-- 标题条 -->
    <div class="admin-header">
      <div>
        <div class="admin-title">后台：游戏管理</div>
        <div class="admin-sub">新增 / 编辑 / 上传封面 / 上架下架</div>
      </div>
      <div class="admin-actions">
        <el-button type="primary" @click="openCreate()">新增游戏</el-button>
        <el-button @click="reloadAll()">刷新</el-button>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="card" ref="filterCardRef">
      <div class="card-title">筛选条件</div>

      <el-form :inline="true" class="query" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="title 模糊搜索" clearable />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
              ref="categorySelectRef"
              v-model="query.categoryId"
              clearable
              placeholder="全部"
              style="width: 180px"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="String(c.id)" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 140px" placeholder="全部">
            <el-option label="上架(1)" :value="1" />
            <el-option label="下架(0)" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="平台">
          <el-input v-model="query.platform" placeholder="PC / PC/PS" clearable />
        </el-form-item>

        <div class="query-actions">
          <el-button type="primary" @click="load()">查询</el-button>
          <el-button @click="resetQuery()">重置</el-button>
        </div>
      </el-form>
    </div>

    <!-- 表格 -->
    <div class="card">
      <div class="card-title">游戏列表</div>

      <GameTable
          :records="records"
          :loading="tableLoading"
          :page="page"
          :category-map="categoryMapObj"
          :default-cover="defaultCover"
          @edit="openEdit"
          @toggle="toggle"
          @remove="removeRow"
          @page-change="(p) => { page.page = p; load() }"
      />
    </div>

    <!-- 弹窗 -->
    <GameFormDialog
        v-model:open="dlg.open"
        :mode="dlg.mode"
        :form="form"
        :categories="categories"
        :default-cover="defaultCover"
        @save="save"
        @cancel="dlg.open = false"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";

import GameTable from "./components/GameTable.vue";
import GameFormDialog from "./components/GameFormDialog.vue";

import { gamesApi } from "../../api/games";
import http from "../../api/http";
import defaultCover from "../../assets/placeholders/game-cover.png";

import { unwrap } from "@/utils/unwrap";
import { resolveCoverUrl } from "@/utils/cover";

const route = useRoute();
const router = useRouter();

const heroRef = ref(null);
const filterCardRef = ref(null);
const categorySelectRef = ref(null);

const query = reactive({
  keyword: "",
  categoryId: "",
  status: null,
  platform: "",
});

function resetQuery() {
  query.keyword = "";
  query.categoryId = "";
  query.status = null;
  query.platform = "";
  page.page = 1;
  load();
}

function scrollToFilters() {
  filterCardRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
}
function onClickTotal() {
  resetQuery();
  scrollToFilters();
}
async function onClickCategories() {
  scrollToFilters();
  await nextTick();
  categorySelectRef.value?.focus?.();
  categorySelectRef.value?.toggleMenu?.();
}

const page = reactive({ page: 1, size: 10, total: 0 });
const records = ref([]);
const tableLoading = ref(false);

const categories = ref([]);
async function loadCategories() {
  try {
    const res = await http.get("/api/home/categories");
    const data = unwrap(res);
    categories.value = Array.isArray(data) ? data : [];
  } catch (e) {
    ElMessage.error(e?.message || "加载分类失败");
    categories.value = [];
  }
}

// ✅ 注意：只保留这一份（不允许重复声明）
const categoryMapObj = computed(() => {
  const obj = {};
  for (const c of categories.value || []) obj[Number(c.id)] = c.name;
  return obj;
});

async function load() {
  tableLoading.value = true;
  try {
    const params = {
      page: page.page,
      size: page.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId ? Number(query.categoryId) : undefined,
      status: query.status ?? undefined,
      platform: query.platform || undefined,
    };

    const res = await gamesApi.page(params);
    const p = unwrap(res);

    const list = Array.isArray(p?.records) ? p.records : [];
    records.value = list.map((x) => {
      const rawCover = x.coverUrl ?? x.cover ?? x.cover_url ?? "";
      return {
        ...x,
        id: String(x.id),
        __updating: false,
        coverUrl: resolveCoverUrl(rawCover),
      };
    });

    page.total = Number(p?.total || 0);
  } catch (e) {
    ElMessage.error(e?.message || "加载列表失败");
    records.value = [];
    page.total = 0;
  } finally {
    tableLoading.value = false;
  }
}

async function reloadAll() {
  await loadCategories();
  await load();
}

const dlg = reactive({ open: false, mode: "create" });
const form = reactive({
  id: null,
  title: "",
  categoryId: 1,
  coverUrl: "",
  intro: "",
  content: "",
  platform: "PC",
  status: 1,
});

function resetForm() {
  form.id = null;
  form.title = "";
  form.categoryId = 1;
  form.coverUrl = "";
  form.intro = "";
  form.content = "";
  form.platform = "PC";
  form.status = 1;
}

function openCreate() {
  resetForm();
  dlg.mode = "create";
  dlg.open = true;
}

function openEdit(row) {
  dlg.mode = "edit";
  dlg.open = true;

  form.id = row.id;
  form.title = row.title;
  form.categoryId = Number(row.categoryId || 1);

  // ✅ 编辑时也做兼容
  form.coverUrl = resolveCoverUrl(row.coverUrl || "");
  form.intro = row.intro || "";
  form.content = row.content || "";
  form.platform = row.platform || "PC";
  form.status = row.status ?? 1;
}

async function save() {
  try {
    if (!form.title || !form.title.trim()) return ElMessage.error("标题不能为空");
    if (!form.categoryId) return ElMessage.error("分类不能为空");

    // ✅ 保存时：把 coverUrl 还原成你后端能识别的值（如果是 /api/file/xxx，直接存即可）
    const payload = {
      title: form.title,
      categoryId: form.categoryId,
      coverUrl: form.coverUrl || null,
      intro: form.intro || null,
      content: form.content || null,
      platform: form.platform || null,
      status: form.status ?? 1,
    };

    if (dlg.mode === "create") {
      const res = await gamesApi.add(payload);
      const id = unwrap(res);
      ElMessage.success("新增成功：" + id);
    } else {
      await gamesApi.update(form.id, payload);
      ElMessage.success("更新成功");
    }

    dlg.open = false;
    await load();
  } catch (e) {
    ElMessage.error(e?.message || "保存失败");
  }
}

async function removeRow(id) {
  try {
    await gamesApi.remove(String(id));
    ElMessage.success("删除成功");
    await load();
  } catch (e) {
    ElMessage.error(e?.message || "删除失败");
  }
}

async function toggle(row) {
  const next = row.status === 1 ? 0 : 1;
  row.__updating = true;
  try {
    await gamesApi.setStatus(String(row.id), next);
    ElMessage.success(next === 1 ? "已上架" : "已下架");
    await load();
  } catch (e) {
    ElMessage.error(e?.message || "上架/下架失败");
  } finally {
    row.__updating = false;
  }
}

async function openEditFromQuery() {
  const editId = route.query.editId;
  if (!editId) return;

  const idStr = String(editId);
  const row = records.value.find((r) => String(r.id) === idStr);

  if (row) openEdit(row);
  else ElMessage.warning("当前页没找到要编辑的游戏：" + idStr + "（可翻页或用关键词搜 ID）");

  const q = { ...route.query };
  delete q.editId;
  router.replace({ query: q });
}

onMounted(async () => {
  await reloadAll();
  await nextTick();
  await openEditFromQuery();
});
</script>

<style scoped src="./GamesAdmin.css"></style>
