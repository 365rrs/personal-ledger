<template>
  <div class="payment-channel-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付渠道管理</span>
          <el-button type="primary" @click="handleAdd">添加渠道</el-button>
        </div>
      </template>

      <el-radio-group v-model="statusFilter" class="status-filter" @change="filterChannels">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button :value="true">启用</el-radio-button>
        <el-radio-button :value="false">禁用</el-radio-button>
      </el-radio-group>

      <el-table :data="filteredChannels" style="width: 100%; margin-top: 20px;" row-key="id">
        <el-table-column label="拖拽" width="60" align="center">
          <template #default>
            <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="渠道名称" width="200" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="渠道名称">
          <el-input v-model="form.name" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
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

const channels = ref([])
const statusFilter = ref('')
const filteredChannels = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  name: '',
  sortOrder: 0,
  enabled: true
})

const loadChannels = async () => {
  try {
    const response = await axios.get('/api/payment-channel/list')
    channels.value = response.data
    filterChannels()
    await nextTick()
    initSortable()
  } catch (error) {
    ElMessage.error('加载支付渠道失败')
  }
}

let draggedData = null

const initSortable = () => {
  const tbody = document.querySelector('.el-table__body-wrapper tbody')
  if (!tbody) return
  
  Sortable.create(tbody, {
    animation: 150,
    handle: '.drag-handle',
    onStart: (evt) => {
      const allRows = Array.from(tbody.children)
      const rowIndex = allRows.indexOf(evt.item)
      draggedData = filteredChannels.value[rowIndex]
    },
    onEnd: async (evt) => {
      if (evt.oldIndex === evt.newIndex || !draggedData) return
      
      const oldIdx = filteredChannels.value.findIndex(c => c.id === draggedData.id)
      if (oldIdx === -1) return
      
      const moved = filteredChannels.value[oldIdx]
      filteredChannels.value.splice(oldIdx, 1)
      filteredChannels.value.splice(evt.newIndex, 0, moved)
      
      const updates = filteredChannels.value.map((item, idx) => ({ ...item, sortOrder: idx + 1 }))
      
      try {
        await Promise.all(updates.map(item => 
          axios.put('/api/payment-channel/update', item)
        ))
        ElMessage.success('排序更新成功')
        loadChannels()
      } catch (error) {
        ElMessage.error('排序失败')
        loadChannels()
      }
      
      draggedData = null
    }
  })
}

const filterChannels = () => {
  if (statusFilter.value === '') {
    filteredChannels.value = channels.value
  } else {
    filteredChannels.value = channels.value.filter(c => c.enabled === statusFilter.value)
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加支付渠道'
  form.value = { id: null, name: '', sortOrder: 0, enabled: true }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑支付渠道'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入渠道名称')
    return
  }

  try {
    if (form.value.id) {
      await axios.put('/api/payment-channel/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/payment-channel/add', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadChannels()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该支付渠道吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/payment-channel/delete/${row.id}`)
    ElMessage.success('删除成功')
    loadChannels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadChannels()
})
</script>

<style scoped>
.payment-channel-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-filter {
  margin-bottom: 20px;
}

.drag-handle {
  cursor: move;
  color: #909399;
}

.drag-handle:hover {
  color: #409eff;
}
</style>
