<template>
  <div class="cover-uploader">
    <div class="cover-preview">
      <img v-if="previewUrl" :src="previewUrl" />
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
      <div class="upload-hint">{{ hint }}</div>
    </el-upload>

    <div class="cover-actions">
      <el-button size="small" plain @click="clear">清除封面</el-button>
      <div class="muted" v-if="modelValue">已设置：{{ modelValue }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  defaultCover: { type: String, required: true },
  hint: { type: String, default: '支持 jpg/png/webp，建议 16:9 或 1:1' }
})

const emit = defineEmits(['update:modelValue'])

const previewUrl = ref('')
let objectUrl = ''

function setPreview(fileOrUrl) {
  if (objectUrl) {
    URL.revokeObjectURL(objectUrl)
    objectUrl = ''
  }
  if (!fileOrUrl) {
    previewUrl.value = ''
    return
  }
  if (typeof fileOrUrl === 'string') {
    previewUrl.value = fileOrUrl
    return
  }
  objectUrl = URL.createObjectURL(fileOrUrl)
  previewUrl.value = objectUrl
}

watch(
    () => props.modelValue,
    (v) => setPreview(v || ''),
    { immediate: true }
)

function clear() {
  emit('update:modelValue', '')
  setPreview('')
}

async function uploadCover({ file }) {
  try {
    setPreview(file)
    const data = await uploadApi.uploadFile(file)

    const filename = typeof data === 'string' ? data : (data?.filename || data?.fileName)
    const url = data?.url ? data.url : (filename ? `/api/file/${filename}` : null)
    if (!url) return ElMessage.error('上传返回值看不懂，把 upload 接口返回贴我一下')

    emit('update:modelValue', url)
    setPreview(url)
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  }
}

onBeforeUnmount(() => {
  if (objectUrl) URL.revokeObjectURL(objectUrl)
})
</script>

<style scoped>
.cover-uploader{
  width:100%;
  display:grid;
  grid-template-columns: 220px 1fr;
  gap:12px;
  align-items:start;
}
.cover-preview{
  width:220px;
  height:220px;
  border:1px solid #eef0f6;
  border-radius:14px;
  overflow:hidden;
  background:#fafbff;
  display:grid;
  place-items:center;
}
.cover-preview img{
  width:100%;
  height:100%;
  object-fit:cover;
  display:block;
}
.cover-actions{
  grid-column: 1 / -1;
  margin-top:8px;
  display:flex;
  gap:10px;
  align-items:center;
  flex-wrap:wrap;
}
.upload-hint{
  color:#7b8499;
  font-size:12px;
  margin-top:6px;
}
.muted{ color:#999; }

@media (max-width: 900px){
  .cover-uploader{ grid-template-columns: 1fr; }
  .cover-preview{ width:100%; height:240px; }
}
</style>
