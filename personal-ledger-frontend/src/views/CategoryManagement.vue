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

      <el-table :data="filteredCategories" style="width: 100%; margin-top: 20px;" row-key="id" :tree-props="{ children: 'children' }">
        <el-table-column label="拖拽" width="60" align="center">
          <template #default>
            <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" width="200" />
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
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="editCategory(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="addSubCategory(row)" v-if="!row.parentId">添加子分类</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '添加分类' : '编辑分类'" width="500px">
      <el-form :model="currentCategory" label-width="100px">
        <el-form-item label="父分类">
          <el-select v-model="currentCategory.parentId" placeholder="选择父分类(可选)" clearable>
            <el-option label="无(一级分类)" :value="null" />
            <el-option v-for="cat in parentCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
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
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, Plus, Edit, Delete, Rank } from '@element-plus/icons-vue'
import axios from 'axios'
import Sortable from 'sortablejs'

const categories = ref([])
const typeFilter = ref('')
const filteredCategories = ref([])
const parentCategories = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentCategory = ref({
  name: '',
  type: 'EXPENSE',
  sortOrder: 0,
  enabled: true,
  parentId: null
})

const loadCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/category/list')
    categories.value = response.data
    updateParentCategories()
    filterCategories()
    await nextTick()
    initSortable()
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const updateParentCategories = () => {
  parentCategories.value = categories.value.filter(c => !c.parentId || c.parentId === 0)
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
  const maxSort = categories.value.length > 0 ? Math.max(...categories.value.map(c => c.sortOrder || 0)) : 0
  currentCategory.value = {
    name: '',
    type: 'EXPENSE',
    sortOrder: maxSort + 1,
    enabled: true,
    parentId: null
  }
  dialogVisible.value = true
}

const editCategory = (category) => {
  dialogMode.value = 'edit'
  currentCategory.value = { ...category }
  dialogVisible.value = true
}

const addSubCategory = (parentCategory) => {
  dialogMode.value = 'add'
  const siblings = parentCategory.children || []
  const maxSort = siblings.length > 0 ? Math.max(...siblings.map(c => c.sortOrder || 0)) : 0
  currentCategory.value = {
    name: '',
    type: parentCategory.type,
    sortOrder: maxSort + 1,
    enabled: true,
    parentId: parentCategory.id
  }
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

let draggedRowData = null

const initSortable = () => {
  const tbody = document.querySelector('.el-table__body-wrapper tbody')
  if (!tbody) return
  
  Sortable.create(tbody, {
    animation: 150,
    handle: '.drag-handle',
    onStart: (evt) => {
      const allRows = Array.from(tbody.children)
      const rowIndex = allRows.indexOf(evt.item)
      const isChild = evt.item.classList.contains('el-table__row--level-1')
      
      if (isChild) {
        let parentIdx = rowIndex - 1
        while (parentIdx >= 0 && allRows[parentIdx].classList.contains('el-table__row--level-1')) {
          parentIdx--
        }
        if (parentIdx >= 0) {
          const parentRowIdx = allRows.filter((r, i) => i <= parentIdx && !r.classList.contains('el-table__row--level-1')).length - 1
          const parent = filteredCategories.value.filter(c => !c.parentId)[parentRowIdx]
          if (parent && parent.children) {
            const childIdx = rowIndex - parentIdx - 1
            draggedRowData = { category: parent.children[childIdx], isChild: true, parent }
          }
        }
      } else {
        const parentIdx = allRows.filter((r, i) => i <= rowIndex && !r.classList.contains('el-table__row--level-1')).length - 1
        const parents = filteredCategories.value.filter(c => !c.parentId)
        draggedRowData = { category: parents[parentIdx], isChild: false }
      }
    },
    onEnd: async (evt) => {
      if (evt.oldIndex === evt.newIndex || !draggedRowData) return
      
      if (draggedRowData.isChild) {
        await handleChildDrag(draggedRowData, evt.oldIndex, evt.newIndex)
      } else {
        await handleParentDrag(draggedRowData, evt.oldIndex, evt.newIndex)
      }
      
      draggedRowData = null
    }
  })
}

const handleParentDrag = async (draggedData, oldDomIndex, newDomIndex) => {
  const tbody = document.querySelector('.el-table__body-wrapper tbody')
  const allRows = Array.from(tbody.children)
  
  const parentRows = allRows.filter(row => !row.classList.contains('el-table__row--level-1'))
  const newIdx = parentRows.indexOf(allRows[newDomIndex])
  
  if (newIdx === -1) return
  
  const parents = filteredCategories.value.filter(c => !c.parentId)
  const oldRealIdx = parents.findIndex(p => p.id === draggedData.category.id)
  
  if (oldRealIdx === -1) return
  
  const moved = parents[oldRealIdx]
  parents.splice(oldRealIdx, 1)
  parents.splice(newIdx, 0, moved)
  
  const updates = parents.map((item, idx) => ({ ...item, sortOrder: idx + 1 }))
  await batchUpdateSort(updates)
}

const handleChildDrag = async (draggedData, oldDomIndex, newDomIndex) => {
  const tbody = document.querySelector('.el-table__body-wrapper tbody')
  const allRows = Array.from(tbody.children)
  
  let parentRow = null
  for (let i = newDomIndex - 1; i >= 0; i--) {
    if (!allRows[i].classList.contains('el-table__row--level-1')) {
      parentRow = allRows[i]
      break
    }
  }
  
  if (!parentRow) return
  
  const childRows = []
  let idx = allRows.indexOf(parentRow) + 1
  while (idx < allRows.length && allRows[idx].classList.contains('el-table__row--level-1')) {
    childRows.push(allRows[idx])
    idx++
  }
  
  const newIdx = childRows.indexOf(allRows[newDomIndex])
  
  if (newIdx === -1) return
  
  const parent = draggedData.parent
  if (!parent || !parent.children) return
  
  const oldRealIdx = parent.children.findIndex(c => c.id === draggedData.category.id)
  if (oldRealIdx === -1) return
  
  const moved = parent.children[oldRealIdx]
  parent.children.splice(oldRealIdx, 1)
  parent.children.splice(newIdx, 0, moved)
  
  const updates = parent.children.map((item, idx) => ({ ...item, sortOrder: idx + 1 }))
  await batchUpdateSort(updates)
}

const batchUpdateSort = async (updates) => {
  try {
    await Promise.all(updates.map(item => 
      axios.put('http://localhost:8080/api/category/update', item)
    ))
    ElMessage.success('排序更新成功')
    loadCategories()
  } catch (error) {
    ElMessage.error('排序失败')
    loadCategories()
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

.drag-handle {
  cursor: move;
  color: #909399;
}

.drag-handle:hover {
  color: #409eff;
}
</style>
