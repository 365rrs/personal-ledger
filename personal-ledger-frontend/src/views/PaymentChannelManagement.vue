<template>
  <div class="payment-channel-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付渠道管理</span>
          <el-button type="primary" @click="handleAdd">添加渠道</el-button>
        </div>
      </template>

      <el-table :data="channels" style="width: 100%">
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const channels = ref([])
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
    const response = await axios.get('http://localhost:8080/api/payment-channel/list')
    channels.value = response.data
  } catch (error) {
    ElMessage.error('加载支付渠道失败')
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
      await axios.put('http://localhost:8080/api/payment-channel/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('http://localhost:8080/api/payment-channel/add', form.value)
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
    await axios.delete(`http://localhost:8080/api/payment-channel/delete/${row.id}`)
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
</style>
