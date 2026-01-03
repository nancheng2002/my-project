<template>
  <div class="mdx">
    <div class="md-toolbar">
      <el-button size="small" @click="insertTpl('h1')">标题</el-button>
      <el-button size="small" @click="insertTpl('list')">列表</el-button>
      <el-button size="small" @click="insertTpl('quote')">引用</el-button>
      <el-button size="small" @click="insertTpl('code')">代码块</el-button>

      <div class="spacer"></div>

      <el-button size="small" plain @click="full.open = true">全屏编辑</el-button>
      <el-button size="small" plain @click="copyHtml">复制预览HTML</el-button>
      <el-button size="small" type="danger" plain @click="clear">清空</el-button>
    </div>

    <div class="md-editor">
      <el-input
          ref="mdInputRef"
          :model-value="modelValue"
          @update:modelValue="emitUpdate"
          type="textarea"
          :rows="14"
          placeholder="支持 Markdown：# 标题、- 列表、> 引用、```代码``` ..."
          class="md-input"
      />

      <div ref="previewRef" class="md-preview">
        <div class="md-preview-title">实时预览</div>
        <div v-if="!modelValue" class="md-empty">右侧会实时预览你输入的 Markdown</div>
        <div v-else class="md" v-html="html"></div>
      </div>
    </div>

    <el-dialog v-model="full.open" title="Markdown 全屏编辑" fullscreen :append-to-body="true" @closed="cleanupScrollSync">
      <div class="md-full">
        <div class="md-toolbar">
          <el-button size="small" @click="insertTpl('h1')">标题</el-button>
          <el-button size="small" @click="insertTpl('list')">列表</el-button>
          <el-button size="small" @click="insertTpl('quote')">引用</el-button>
          <el-button size="small" @click="insertTpl('code')">代码块</el-button>

          <div class="spacer"></div>

          <el-button size="small" plain @click="copyHtml">复制预览HTML</el-button>
          <el-button size="small" type="danger" plain @click="clear">清空</el-button>
        </div>

        <div class="md-editor md-editor-full">
          <el-input
              ref="mdInputFullRef"
              :model-value="modelValue"
              @update:modelValue="emitUpdate"
              type="textarea"
              :rows="20"
              class="md-input"
          />

          <div ref="previewFullRef" class="md-preview">
            <div class="md-preview-title">实时预览</div>
            <div v-if="!modelValue" class="md-empty">右侧会实时预览你输入的 Markdown</div>
            <div v-else class="md" v-html="html"></div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="full.open=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onBeforeUnmount, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])

function emitUpdate(v) {
  emit('update:modelValue', v)
}

const full = reactive({ open: false })

const mdInputRef = ref(null)
const previewRef = ref(null)
const mdInputFullRef = ref(null)
const previewFullRef = ref(null)

const html = computed(() => renderMarkdown(props.modelValue || ''))

function getTextareaEl(targetRef) {
  const el = targetRef.value?.textarea
  if (el) return el
  const root = targetRef.value?.$el
  return root ? root.querySelector('textarea') : null
}

async function insertAtCursor(text) {
  const ta = getTextareaEl(full.open ? mdInputFullRef : mdInputRef)
  const old = props.modelValue || ''

  if (!ta) return emitUpdate(old + text)

  const start = ta.selectionStart ?? old.length
  const end = ta.selectionEnd ?? old.length
  emitUpdate(old.slice(0, start) + text + old.slice(end))

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

function clear() {
  emitUpdate('')
}

async function copyHtml() {
  try {
    await navigator.clipboard.writeText(html.value || '')
    ElMessage.success('已复制预览 HTML')
  } catch {
    ElMessage.error('复制失败（浏览器可能不允许）')
  }
}

/** 同步滚动 */
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

/** ✅ 首次进入：绑定主编辑器滚动同步 */
onMounted(async () => {
  await nextTick()
  bindScrollSync(mdInputRef, previewRef.value)
})

/** ✅ 打开/关闭全屏：切换绑定对象 */
watch(() => full.open, async (open) => {
  await nextTick()
  if (open) bindScrollSync(mdInputFullRef, previewFullRef.value)
  else bindScrollSync(mdInputRef, previewRef.value)
})

onBeforeUnmount(() => cleanupScrollSync())
</script>


<style scoped>
.mdx .md-toolbar{ width:100%; display:flex; gap:8px; flex-wrap:wrap; margin-bottom:10px; }
.mdx .md-toolbar .spacer{ flex:1; }
.mdx .md-editor{ width:100%; display:grid; grid-template-columns:1fr 1fr; gap:12px; }
.mdx .md-preview{ border:1px solid #eef0f6; border-radius:12px; background:#fff; padding:12px 14px; min-height:320px; max-height:360px; overflow:auto; }
.mdx .md-preview-title{ font-size:12px; color:#7b8499; margin-bottom:10px; font-weight:700; }
.mdx .md-empty{ color:#9aa3b2; background:#fafbff; border:1px dashed #e3e7f5; border-radius:10px; padding:12px; font-size:13px; }
.mdx .md-full{ padding:4px 2px; }
.mdx .md-editor-full{ height: calc(100vh - 210px); }
.mdx .md-editor-full :deep(.el-textarea__inner){ height: calc(100vh - 270px) !important; resize:none; }
@media (max-width:900px){ .mdx .md-editor{ grid-template-columns:1fr; } }
</style>
