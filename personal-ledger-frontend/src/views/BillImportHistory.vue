<template>
  <div class="bill-import-history">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>导入历史</span>
          <el-button type="primary" @click="refreshList">刷新</el-button>
        </div>
      </template>

      <el-table :data="importList" style="width: 100%" v-loading="loading">
        <el-table-column prop="importName" label="导入名称" width="200" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column prop="importTime" label="导入时间" width="180" />
        <el-table-column prop="recordCount" label="总记录数" width="100" align="center" />
        <el-table-column prop="newCount" label="新增" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.newCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duplicateCount" label="重复" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.duplicateCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="importStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.importStatus)">
              {{ getStatusText(row.importStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="text" size="small" @click="deleteImport(row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { billImportApi } from '../api/bill'

const router = useRouter()

const loading = ref(false)
const importList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadImportList()
})

const loadImportList = async () => {
  loading.value = true
  try {
    const res = await billImportApi.getImportList(currentPage.value, pageSize.value)
    if (res.data.success) {
      importList.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (error) {
    ElMessage.error('加载导入历史失败')
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  loadImportList()
}

const handleSizeChange = () => {
  loadImportList()
}

const handleCurrentChange = () => {
  loadImportList()
}

const viewDetail = (row) => {
  router.push(`/transaction-list?importId=${row.id}`)
}

const deleteImport = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此导入记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await billImportApi.deleteImport(row.id)
    ElMessage.success('删除成功')
    loadImportList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getStatusType = (status) => {
  const map = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'PROCESSING': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PROCESSING': '处理中'
  }
  return map[status] || status
}
</script>

<style scoped>
.bill-import-history {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
