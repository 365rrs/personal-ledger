<template>
  <div class="category-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Collection /></el-icon>
            <span>分类管理</span>
          </div>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">添加分类</el-button>
        </div>
      </template>

      <el-radio-group v-model="typeFilter" class="type-filter" @change="filterCategories">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="EXPENSE">支出</el-radio-button>
        <el-radio-button value="INCOME">收入</el-radio-button>
      </el-radio-group>

      <el-table :data="filteredCategories" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="name" label="分类名称" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'EXPENSE' ? 'danger' : 'success'">
              {{ row.type === 'EXPENSE' ? '支出' : '收入' }}
            </el-tag>
          </template>
        </el-table-column>
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
            <el-button size="small" @click="editCategory(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '添加分类' : '编辑分类'" width="500px">
      <el-form :model="currentCategory" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="currentCategory.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="currentCategory.type" placeholder="请选择类型">
            <el-option label="支出" value="EXPENSE" />
            <el-option label="收入" value="INCOME" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentCategory.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="currentCategory.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, Plus, Edit, Delete } from '@element-plus/icons-vue'
import axios from 'axios'

const categories = ref([])
const typeFilter = ref('')
const filteredCategories = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentCategory = ref({
  name: '',
  type: 'EXPENSE',
  sortOrder: 0,
  enabled: true
})

const loadCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/category/list')
    categories.value = response.data
    filterCategories()
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const filterCategories = () => {
  if (typeFilter.value === '') {
    filteredCategories.value = categories.value
  } else {
    filteredCategories.value = categories.value.filter(c => c.type === typeFilter.value)
  }
}

const showAddDialog = () => {
  dialogMode.value = 'add'
  currentCategory.value = {
    name: '',
    type: 'EXPENSE',
    sortOrder: 0,
    enabled: true
  }
  dialogVisible.value = true
}

const editCategory = (category) => {
  dialogMode.value = 'edit'
  currentCategory.value = { ...category }
  dialogVisible.value = true
}

const saveCategory = async () => {
  if (!currentCategory.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (dialogMode.value === 'add') {
      await axios.post('http://localhost:8080/api/category/add', currentCategory.value)
      ElMessage.success('添加成功')
    } else {
      await axios.put('http://localhost:8080/api/category/update', currentCategory.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`http://localhost:8080/api/category/delete/${category.id}`)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 18px;
}

.type-filter {
  margin-bottom: 20px;
}
</style>
