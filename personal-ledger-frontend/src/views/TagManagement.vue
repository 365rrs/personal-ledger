<template>
  <div class="tag-management">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>标签管理</span>
          <el-button type="primary" @click="showAddDialog">添加标签</el-button>
        </div>
      </template>

      <el-table :data="tags" style="width: 100%" row-key="id">
        <el-table-column label="拖拽" width="60" align="center">
          <template #default>
            <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="标签名称" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="color" label="颜色" width="150">
          <template #default="{ row }">
            <el-tag :style="{ backgroundColor: row.color, borderColor: row.color, color: '#fff' }">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editTag(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '添加标签' : '编辑标签'" width="450px">
      <el-form :model="currentTag" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="currentTag.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentTag.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="标签颜色">
          <div class="color-picker">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-box"
              :class="{ active: currentTag.color === color }"
              :style="{ backgroundColor: color }"
              @click="currentTag.color = color"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import axios from 'axios'
import Sortable from 'sortablejs'

const tags = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentTag = ref({ name: '', color: '#a0cfff', sortOrder: 0 })

const presetColors = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#c71585', '#ff1493', '#ff69b4',
  '#9370db', '#4169e1', '#00bfff', '#20b2aa',
  '#32cd32', '#ffd700', '#ff8c00', '#dc143c'
]

const loadTags = async () => {
  try {
    const res = await axios.get('/api/bill/tag/list')
    tags.value = res.data
    return Promise.resolve()
  } catch (error) {
    ElMessage.error('加载标签失败')
    return Promise.reject(error)
  }
}

const showAddDialog = () => {
  dialogMode.value = 'add'
  const maxOrder = tags.value.length > 0 ? Math.max(...tags.value.map(t => t.sortOrder || 0)) : 0
  currentTag.value = { name: '', color: '#a0cfff', sortOrder: maxOrder + 1 }
  dialogVisible.value = true
}

const editTag = (tag) => {
  dialogMode.value = 'edit'
  currentTag.value = { ...tag }
  dialogVisible.value = true
}

const saveTag = async () => {
  if (!currentTag.value.name) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    if (dialogMode.value === 'add') {
      await axios.post('/api/bill/tag/add', currentTag.value)
      ElMessage.success('添加成功')
    } else {
      await axios.put('/api/bill/tag/update', currentTag.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadTags()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm('确定要删除该标签吗？', '提示', { type: 'warning' })
    await axios.delete(`/api/bill/tag/delete/${tag.id}`)
    ElMessage.success('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const initSortable = () => {
  nextTick(() => {
    const table = document.querySelector('.el-table__body-wrapper tbody')
    if (!table) return
    Sortable.create(table, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: async ({ oldIndex, newIndex }) => {
        if (oldIndex === newIndex) return
        const movedTag = tags.value.splice(oldIndex, 1)[0]
        tags.value.splice(newIndex, 0, movedTag)
        
        // 更新所有标签的sortOrder
        const updates = tags.value.map((tag, index) => {
          tag.sortOrder = index
          return axios.put('/api/bill/tag/update', tag)
        })
        
        try {
          await Promise.all(updates)
          ElMessage.success('排序更新成功')
        } catch (error) {
          ElMessage.error('排序更新失败')
          loadTags()
        }
      }
    })
  })
}

onMounted(() => {
  loadTags().then(() => initSortable())
})
</script>

<style scoped>
.tag-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.color-picker {
  display: grid;
  grid-template-columns: repeat(8, 30px);
  gap: 6px;
}

.color-box {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-box:hover {
  transform: scale(1.1);
}

.color-box.active {
  border-color: #303133;
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px #303133;
}
</style>
