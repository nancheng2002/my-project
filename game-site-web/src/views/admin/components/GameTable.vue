<template>
  <el-table :data="records" v-loading="loading" border style="width: 100%">
    <el-table-column prop="id" label="ID" width="220" />
    <el-table-column prop="title" label="标题" min-width="160" />

    <el-table-column label="分类" width="140">
      <template #default="{ row }">
        {{ categoryMap?.[Number(row.categoryId)] || row.categoryName || '-' }}
      </template>
    </el-table-column>

    <el-table-column prop="platform" label="平台" width="120" />

    <el-table-column label="状态" width="120">
      <template #default="{ row }">
        <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
          {{ row.status === 1 ? '上架' : '下架' }}
        </el-tag>
      </template>
    </el-table-column>

    <el-table-column label="封面" width="140">
      <template #default="{ row }">
        <el-image
            :src="row.coverUrl || defaultCover"
            fit="cover"
            style="width: 88px; height: 56px; border-radius: 10px; border:1px solid #eef0f6;"
        />
      </template>
    </el-table-column>

    <el-table-column label="操作" width="220" fixed="right">
      <template #default="{ row }">
        <el-button size="small" @click="$emit('edit', row)">编辑</el-button>
        <el-button size="small" type="warning" @click="$emit('toggle', row)">
          {{ row.status === 1 ? '下架' : '上架' }}
        </el-button>
        <el-button size="small" type="danger" @click="$emit('remove', row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <div class="pager">
    <el-pagination
        background
        layout="prev, pager, next"
        :total="page.total"
        :page-size="page.size"
        :current-page="page.page"
        @current-change="(p)=>$emit('page-change', p)"
    />
  </div>
</template>

<script setup>
defineProps({
  records: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  page: { type: Object, required: true },
  categoryMap: { type: Object, default: () => ({}) },
  defaultCover: { type: String, default: '' },
})
defineEmits(['edit', 'toggle', 'remove', 'page-change'])
</script>

<style scoped>
.pager{ display:flex; justify-content:flex-end; margin-top:12px; }
</style>
