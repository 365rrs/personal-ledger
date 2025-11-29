<template>
  <div class="bill-import-history">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>导入历史</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-table :data="historyList" v-loading="loading" style="width: 100%">
        <el-table-column prop="importName" label="导入名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sourceFile" label="源文件" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="文件类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.fileType === 'CSV' ? 'success' : 'primary'">
              {{ row.fileType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="importTime" label="导入时间" width="180" />
        <el-table-column prop="recordCount" label="总记录" width="100" align="center" />
        <el-table-column prop="newCount" label="新增" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.newCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duplicateCount" label="重复" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.duplicateCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="importStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.importStatus === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.importStatus === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
            <el-button type="danger" size="small" @click="deleteHistory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const historyList = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/bill/import/list', {
      params: {
        current: page.current,
        size: page.size
      }
    })
    historyList.value = res.data.data.records
    page.total = res.data.data.total
  } catch (error) {
    ElMessage.error('加载导入历史失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/transaction-list?importId=${row.id}`)
}

const deleteHistory = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此导入记录吗？删除后相关交易数据也将被删除。', '提示', {
      type: 'warning'
    })
    
    await axios.delete(`http://localhost:8080/api/bill/import/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.bill-import-history {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
