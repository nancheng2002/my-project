<template>
  <div class="cu">
    <div class="preview">
      <el-image
          v-if="previewUrl"
          :src="previewUrl"
          fit="cover"
          class="img"
      />
      <div v-else class="ph">
        <div class="t">No Cover</div>
        <div class="s">拖拽或点击上传</div>
      </div>
    </div>

    <el-upload
        class="uploader"
        drag
        :show-file-list="false"
        :http-request="onUpload"
        accept="image/*"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽图片到这里，或点击上传</div>
      <template #tip>
        <div class="el-upload__tip">支持 jpg/png/webp，建议 16:9 或 1:1</div>
      </template>
    </el-upload>

    <div class="ops">
      <el-button size="small" @click="emit('update:modelValue', '')" plain>清空封面</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadApi } from '@/api/upload'
import { unwrap } from '@/utils/unwrap'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const previewUrl = computed(() => props.modelValue || '')

async function onUpload({ file }) {
  try {
    const res = await uploadApi.uploadFile(file)
    const data = unwrap(res)

    // 兼容：后端可能返回 string / {filename} / {url}
    const filename = typeof data === 'string' ? data : (data?.filename || data?.fileName)
    const url = data?.url ? data.url : (filename ? `/api/file/${filename}` : '')

    if (!url) {
      ElMessage.error('上传返回值看不懂：请把 upload 接口返回贴我一下')
      return
    }

    emit('update:modelValue', url)
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  }
}
</script>

<style scoped>
.cu{ display:grid; gap:10px; }
.preview{ width:100%; border:1px solid #eef0f6; border-radius:14px; overflow:hidden; background:#fafbff; }
.img{ width:100%; height:220px; }
.ph{ height:220px; display:grid; place-content:center; text-align:center; color:#7b8499; }
.ph .t{ font-weight:900; font-size:16px; margin-bottom:6px; color:#3a4055; }
.ph .s{ font-size:12px; }
.ops{ display:flex; justify-content:flex-end; }
</style>
