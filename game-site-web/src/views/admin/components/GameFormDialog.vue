<template>
  <el-dialog
      v-model="openInner"
      :title="mode === 'create' ? '新增游戏' : '编辑游戏'"
      width="980px"
      top="6vh"
      :append-to-body="true"
  >
    <el-form label-width="70px" class="form">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入标题" />
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="form.categoryId" style="width: 100%" placeholder="请选择分类">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="Number(c.id)" />
        </el-select>
      </el-form-item>

      <el-form-item label="平台">
        <el-input v-model="form.platform" placeholder="PC / PC/PS" />
      </el-form-item>

      <el-form-item label="简介">
        <el-input v-model="form.intro" placeholder="一句话介绍" />
      </el-form-item>

      <el-form-item label="详情">
        <MdEditor v-model="form.content" />
      </el-form-item>

      <el-form-item label="封面">
        <div class="cover-box">
          <el-image
              class="cover-preview"
              :src="coverPreview || form.coverUrl || defaultCover"
              fit="cover"
              @error="onImgError"
          >
            <template #error>
              <div class="cover-error">FAILED</div>
            </template>
          </el-image>

          <el-upload
              class="uploader"
              drag
              :show-file-list="false"
              :http-request="uploadCover"
              accept="image/*"
          >
            <div class="upload-inner">
              <div class="upload-icon">☁</div>
              <div>拖拽图片到这里，或点击上传</div>
              <div class="tip">支持 jpg/png/webp，建议 16:9 或 1:1</div>
            </div>
          </el-upload>

          <el-button size="small" @click="clearCover">清空封面</el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="openInner = false">取消</el-button>
      <el-button type="primary" @click="$emit('save')">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import MdEditor from '@/components/MdEditor.vue'
import { uploadApi, buildFileUrl } from '@/api/upload'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  form: { type: Object, required: true },
  categories: { type: Array, default: () => [] },
  defaultCover: { type: String, default: '' },
})
const emit = defineEmits(['update:open', 'save'])

const openInner = computed({
  get: () => props.open,
  set: (v) => emit('update:open', v),
})

/** 本地预览（就算后端图片地址暂时不通，也能先看到你选的图） */
const coverPreview = ref('')
let coverObjectUrl = ''

function setLocalPreview(file) {
  if (coverObjectUrl) URL.revokeObjectURL(coverObjectUrl)
  coverObjectUrl = URL.createObjectURL(file)
  coverPreview.value = coverObjectUrl
}

function onImgError() {
  // 这里不弹错，避免烦；FAILED 会在图里显示
}

function clearCover() {
  props.form.coverUrl = ''
  coverPreview.value = ''
  if (coverObjectUrl) {
    URL.revokeObjectURL(coverObjectUrl)
    coverObjectUrl = ''
  }
}

async function uploadCover({ file }) {
  try {
    setLocalPreview(file)

    const res = await uploadApi.uploadFile(file)
    // uploadApi 已经做了解包，res 可能是：url / filename / {url,filename}
    let url = null

    if (typeof res === 'string') {
      // 可能是 url，也可能是 filename
      url = res.startsWith('http') || res.startsWith('/api/') ? res : buildFileUrl(res)
    } else {
      url = res?.url || (res?.filename ? buildFileUrl(res.filename) : null)
    }

    if (!url) {
      ElMessage.error('上传返回值里没有 url/filename，请把 upload 接口返回贴我一下')
      return
    }

    props.form.coverUrl = url
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  }
}

onBeforeUnmount(() => {
  if (coverObjectUrl) URL.revokeObjectURL(coverObjectUrl)
})
</script>

<style scoped>
.form { padding-right: 6px; }
.cover-box { width: 100%; display: grid; grid-template-columns: 220px 1fr; gap: 14px; align-items: start; }
.cover-preview { width: 220px; height: 220px; border-radius: 14px; border: 1px solid #eef0f6; overflow: hidden; background: #fafbff; }
.cover-error { width:100%; height:100%; display:grid; place-content:center; color:#9aa3b2; font-weight:900; }
.uploader { border-radius: 14px; border: 1px dashed #d8deef; background:#fff; overflow:hidden; }
.upload-inner { padding: 26px; text-align:center; color:#667085; }
.upload-icon { font-size: 34px; margin-bottom: 8px; }
.tip { font-size: 12px; color:#9aa3b2; margin-top: 6px; }
</style>
