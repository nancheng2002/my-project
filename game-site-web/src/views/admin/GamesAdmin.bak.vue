<template>
  <div class="admin-page">
    <!-- 顶部概览卡片（点击可操作） -->
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

    <!-- 顶部标题条 -->
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

    <!-- 查询卡片 -->
    <div class="card" ref="filterCardRef">
      <div class="card-title">筛选条件</div>

      <el-form :inline="true" class="query" @submit.prevent>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="title 模糊搜索" clearable />
        </el-form-item>

        <!-- ✅ 分类下拉（替代分类ID输入框） -->
        <el-form-item label="分类">
          <el-select
              ref="categorySelectRef"
              v-model="query.categoryId"
              clearable
              placeholder="全部"
              style="width:180px"
          >
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="String(c.id)" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width:140px" placeholder="全部">
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

    <!-- 表格卡片 -->
    <div class="card">
      <div class="card-title">游戏列表</div>

      <el-table :data="records" border v-loading="tableLoading" class="table">
        <el-table-column prop="id" label="ID" width="220" />
        <el-table-column prop="title" label="标题" />

        <el-table-column label="分类" width="160">
          <template #default="{ row }">
            {{ categoryMap.get(Number(row.categoryId)) || row.categoryName || row.categoryId || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="platform" label="平台" width="120" />

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- ✅ 封面裂图修复：永远有图（没封面就用 defaultCover） -->
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
                :src="row.coverUrl || defaultCover"
                class="cover"
                fit="cover"
                :preview-src-list="[row.coverUrl || defaultCover]"
                preview-teleported
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>

            <el-button
                size="small"
                type="warning"
                :loading="row.__updating === true"
                @click="toggle(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>

            <el-popconfirm title="确定删除？" @confirm="removeRow(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
            background
            layout="prev, pager, next"
            :page-size="page.size"
            :current-page="page.page"
            :total="page.total"
            @current-change="(p)=>{page.page=p; load()}"
        />
      </div>
    </div>

    <!-- 弹窗：新增/编辑 -->
    <el-dialog v-model="dlg.open" :title="dlg.mode==='create'?'新增游戏':'编辑游戏'" width="920px">
      <el-form label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>

        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="Number(c.id)" />
          </el-select>
        </el-form-item>

        <el-form-item label="平台">
          <el-input v-model="form.platform" placeholder="PC / PC/PS" />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="form.intro" />
        </el-form-item>

        <!-- Markdown 左写右预览 + 一键模板 + 同步滚动 -->
        <el-form-item label="详情">
          <MdEditor v-model="form.content" />
        </el-form-item>


        <!-- 封面拖拽上传 + 立即预览 -->
        <el-form-item label="封面">
          <div class="cover-uploader">
            <div class="cover-preview">
              <img v-if="coverPreviewUrl" :src="coverPreviewUrl" />
              <img v-else :src="defaultCover" />
            </div>

            <el-upload
                drag
                :show-file-list="false"
                accept="image/*"
                :http-request="uploadCover"
            >
              <el-icon style="font-size:28px; margin-bottom:6px">
                <UploadFilled />
              </el-icon>
              <div>拖拽图片到这里，或点击上传</div>
              <div class="upload-hint">支持 jpg/png/webp，建议 16:9 或 1:1</div>
            </el-upload>

            <div class="cover-actions">
              <el-button size="small" plain @click="clearCover">清除封面</el-button>
              <div class="muted" v-if="form.coverUrl">已设置：{{ form.coverUrl }}</div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dlg.open=false">取消</el-button>
        <el-button type="primary" @click="save()">保存</el-button>
      </template>
    </el-dialog>

    <!-- 全屏 Markdown 编辑器 -->
    <el-dialog v-model="mdFull.open" title="Markdown 全屏编辑" fullscreen :append-to-body="true">
      <div class="md-full">
        <div class="md-toolbar">
          <el-button size="small" @click="insertTpl('h1')">标题</el-button>
          <el-button size="small" @click="insertTpl('list')">列表</el-button>
          <el-button size="small" @click="insertTpl('quote')">引用</el-button>
          <el-button size="small" @click="insertTpl('code')">代码块</el-button>

          <div class="spacer"></div>

          <el-button size="small" plain @click="copyPreviewHtml">复制预览HTML</el-button>
          <el-button size="small" type="danger" plain @click="clearMd">清空</el-button>
        </div>

        <div class="md-editor md-editor-full">
          <el-input
              ref="mdInputFullRef"
              v-model="form.content"
              type="textarea"
              :rows="20"
              class="md-input"
          />

          <div ref="previewFullRef" class="md-preview">
            <div class="md-preview-title">实时预览</div>
            <div v-if="!form.content" class="md-empty">右侧会实时预览你输入的 Markdown</div>
            <div v-else class="md" v-html="contentHtml"></div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="mdFull.open=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { marked } from 'marked'

import { gamesApi } from '../../api/games'
import { uploadApi } from '../../api/upload'
import http from '../../api/http'

// ✅ 默认封面（避免裂图）—— 用相对路径最稳
import defaultCover from '../../assets/placeholders/game-cover.png'

/** 路由 */
const route = useRoute()
const router = useRouter()

/** 顶部/筛选 refs（用于滚动和“自动打开下拉”） */
const heroRef = ref(null)
const filterCardRef = ref(null)
const categorySelectRef = ref(null)

/** 查询 */
const query = reactive({
  keyword: '',
  categoryId: '',     // ✅ 用 string，和 el-select 对齐
  status: null,
  platform: ''
})

function resetQuery() {
  query.keyword = ''
  query.categoryId = ''
  query.status = null
  query.platform = ''
  page.page = 1
  load()
}

/** 顶部卡片点击交互 */
function scrollToFilters() {
  filterCardRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
function onClickTotal() {
  resetQuery()
  scrollToFilters()
}
async function onClickCategories() {
  scrollToFilters()
  await nextTick()
  // Element Plus: 有的版本有 toggleMenu，有的没有；安全调用
  categorySelectRef.value?.focus?.()
  categorySelectRef.value?.toggleMenu?.()
}

/** 分页 & 列表 */
const page = reactive({ page: 1, size: 10, total: 0 })
const records = ref([])
const tableLoading = ref(false)

/** 分类 */
const categories = ref([])
async function loadCategories() {
  try {
    const res = await http.get('/api/home/categories')
    const data = (res && res.data && res.data.data) ? res.data.data : res
    categories.value = Array.isArray(data) ? data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载分类失败')
  }
}

const categoryMap = computed(() => {
  const m = new Map()
  for (const c of categories.value || []) m.set(Number(c.id), c.name)
  return m
})

async function load() {
  tableLoading.value = true
  try {
    const params = {
      page: page.page,
      size: page.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId ? Number(query.categoryId) : undefined,
      status: query.status ?? undefined,
      platform: query.platform || undefined,
    }

    const p = await gamesApi.page(params)
    const list = Array.isArray(p?.records) ? p.records : []

    records.value = list.map(x => ({
      ...x,
      id: String(x.id), // 避免雪花ID丢精度
      __updating: false,
      coverUrl: x.coverUrl ? x.coverUrl : ''
    }))

    page.total = Number(p?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载列表失败')
  } finally {
    tableLoading.value = false
  }
}

async function reloadAll() {
  await loadCategories()
  await load()
}

/** 弹窗 & 表单 */
const dlg = reactive({ open: false, mode: 'create' })
const form = reactive({
  id: null,
  title: '',
  categoryId: 1,
  coverUrl: '',
  intro: '',
  content: '',
  platform: 'PC',
  status: 1
})

/** Markdown 全屏 */
const mdFull = reactive({ open: false })
const mdInputRef = ref(null)
const previewRef = ref(null)
const mdInputFullRef = ref(null)
const previewFullRef = ref(null)

const contentHtml = computed(() => marked.parse(form.content || ''))

/** ===== 封面拖拽上传 + 立即预览 ===== */
const coverPreviewUrl = ref('')
let coverObjectUrl = ''

function setCoverPreview(fileOrUrl) {
  if (coverObjectUrl) {
    URL.revokeObjectURL(coverObjectUrl)
    coverObjectUrl = ''
  }

  if (!fileOrUrl) {
    coverPreviewUrl.value = ''
    return
  }

  if (typeof fileOrUrl === 'string') {
    coverPreviewUrl.value = fileOrUrl
    return
  }

  coverObjectUrl = URL.createObjectURL(fileOrUrl)
  coverPreviewUrl.value = coverObjectUrl
}

function clearCover() {
  form.coverUrl = ''
  setCoverPreview('')
}

async function uploadCover({ file }) {
  try {
    setCoverPreview(file)

    const data = await uploadApi.uploadFile(file)
    const filename = typeof data === 'string' ? data : (data?.filename || data?.fileName)
    const url = data?.url ? data.url : (filename ? `/api/file/${filename}` : null)
    if (!url) return ElMessage.error('上传返回值看不懂，把 upload 接口返回贴我一下')

    form.coverUrl = url
    setCoverPreview(url)
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  }
}

/** ===== Markdown：模板按钮（插入到光标） ===== */
function getTextareaEl(targetRef) {
  const el = targetRef.value?.textarea
  if (el) return el
  const root = targetRef.value?.$el
  return root ? root.querySelector('textarea') : null
}

async function insertAtCursor(text) {
  const ta = getTextareaEl(mdFull.open ? mdInputFullRef : mdInputRef)
  const old = form.content || ''

  if (!ta) {
    form.content = old + text
    return
  }

  const start = ta.selectionStart ?? old.length
  const end = ta.selectionEnd ?? old.length
  form.content = old.slice(0, start) + text + old.slice(end)

  await nextTick()
  const pos = start + text.length
  ta.focus()
  ta.setSelectionRange(pos, pos)
}

function insertTpl(type) {
  const map = {
    h1: '# 标题\n\n这里写正文…\n\n',
    list: '- 项目 1\n- 项目 2\n- 项目 3\n\n',
    quote: '> 这里是一段引用（适合写项目说明/亮点）\n\n',
    code: '```js\nconsole.log("Hello Game Site")\n```\n\n',
  }
  insertAtCursor(map[type] || '')
}

function clearMd() {
  form.content = ''
}

async function copyPreviewHtml() {
  try {
    await navigator.clipboard.writeText(contentHtml.value || '')
    ElMessage.success('已复制预览 HTML')
  } catch {
    ElMessage.error('复制失败（浏览器可能不允许）')
  }
}

/** ===== 同步滚动（编辑区 <-> 预览区） ===== */
let cleanupFns = []
let lockFromEditor = false
let lockFromPreview = false

function calcRatio(el) {
  const max = el.scrollHeight - el.clientHeight
  if (max <= 0) return 0
  return el.scrollTop / max
}
function setByRatio(el, ratio) {
  const max = el.scrollHeight - el.clientHeight
  el.scrollTop = ratio * (max <= 0 ? 0 : max)
}

function cleanupScrollSync() {
  cleanupFns.forEach(fn => fn())
  cleanupFns = []
}

function bindScrollSync(editorRef, previewDom) {
  cleanupScrollSync()

  const ta = getTextareaEl(editorRef)
  const pv = previewDom
  if (!ta || !pv) return

  const onEditorScroll = () => {
    if (lockFromPreview) return
    lockFromEditor = true
    setByRatio(pv, calcRatio(ta))
    requestAnimationFrame(() => (lockFromEditor = false))
  }

  const onPreviewScroll = () => {
    if (lockFromEditor) return
    lockFromPreview = true
    setByRatio(ta, calcRatio(pv))
    requestAnimationFrame(() => (lockFromPreview = false))
  }

  ta.addEventListener('scroll', onEditorScroll, { passive: true })
  pv.addEventListener('scroll', onPreviewScroll, { passive: true })

  cleanupFns.push(() => ta.removeEventListener('scroll', onEditorScroll))
  cleanupFns.push(() => pv.removeEventListener('scroll', onPreviewScroll))
}

watch(() => dlg.open, async (open) => {
  if (open) {
    await nextTick()
    bindScrollSync(mdInputRef, previewRef.value)
  } else {
    cleanupScrollSync()
  }
})

watch(() => mdFull.open, async (open) => {
  if (open) {
    await nextTick()
    bindScrollSync(mdInputFullRef, previewFullRef.value)
  } else {
    cleanupScrollSync()
  }
})

onBeforeUnmount(() => {
  cleanupScrollSync()
  if (coverObjectUrl) URL.revokeObjectURL(coverObjectUrl)
})

/** ===== CRUD ===== */
function resetForm() {
  form.id = null
  form.title = ''
  form.categoryId = 1
  form.coverUrl = ''
  form.intro = ''
  form.content = ''
  form.platform = 'PC'
  form.status = 1
  setCoverPreview('')
}

function openCreate() {
  resetForm()
  dlg.mode = 'create'
  dlg.open = true
}

function openEdit(row) {
  dlg.mode = 'edit'
  dlg.open = true
  form.id = row.id
  form.title = row.title
  form.categoryId = Number(row.categoryId || 1)
  form.coverUrl = row.coverUrl || ''
  form.intro = row.intro || ''
  form.content = row.content || ''
  form.platform = row.platform || 'PC'
  form.status = row.status ?? 1
  setCoverPreview(form.coverUrl || '')
}

async function save() {
  try {
    if (!form.title || !form.title.trim()) return ElMessage.error('标题不能为空')
    if (!form.categoryId) return ElMessage.error('分类不能为空')

    const payload = {
      title: form.title,
      categoryId: form.categoryId,
      coverUrl: form.coverUrl || null,
      intro: form.intro || null,
      content: form.content || null,
      platform: form.platform || null,
      status: form.status ?? 1
    }

    if (dlg.mode === 'create') {
      const id = await gamesApi.add(payload)
      ElMessage.success('新增成功：' + id)
    } else {
      await gamesApi.update(form.id, payload)
      ElMessage.success('更新成功')
    }

    // ✅ 你问的：mdFull.open = false 放哪？
    // 保存成功就把全屏关掉，避免“保存后还留在全屏编辑”的体验问题
    mdFull.open = false

    dlg.open = false
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  }
}

async function removeRow(id) {
  try {
    await gamesApi.remove(String(id))
    ElMessage.success('删除成功')
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  row.__updating = true
  try {
    await gamesApi.setStatus(String(row.id), next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    await load()
  } catch (e) {
    ElMessage.error(e?.message || '上架/下架失败')
  } finally {
    row.__updating = false
  }
}

/** URL ?editId=xxx 自动打开编辑 */
async function openEditFromQuery() {
  const editId = route.query.editId
  if (!editId) return

  const idStr = String(editId)
  const row = records.value.find(r => String(r.id) === idStr)

  if (row) {
    openEdit(row)
  } else {
    ElMessage.warning('当前页没找到要编辑的游戏：' + idStr + '（可翻页或用关键词搜 ID）')
  }

  const q = { ...route.query }
  delete q.editId
  router.replace({ query: q })
}

onMounted(async () => {
  await loadCategories()
  await load()
  await nextTick()
  await openEditFromQuery()
})
</script>

<!-- ✅ 样式拆出去：更像真实项目 -->
<style scoped src="./GamesAdmin.css"></style>
