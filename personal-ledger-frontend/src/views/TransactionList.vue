<template>
  <div class="transaction-list">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>交易记录</span>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filter.category" clearable filterable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="transactions" v-loading="loading">
        <el-table-column prop="transactionDate" label="日期" width="120" />
        <el-table-column prop="transactionTime" label="时间" width="100" />
        <el-table-column prop="income" label="收入" width="100" />
        <el-table-column prop="expense" label="支出" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="paymentChannel" label="渠道" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="editRecord(row)">编辑</el-button>
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
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑交易" width="500px">
      <el-form :model="currentRecord" label-width="100px">
        <el-form-item label="分类">
          <el-select v-model="currentRecord.category" filterable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="currentRecord.paymentChannel" filterable>
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户备注">
          <el-input v-model="currentRecord.userNote" type="textarea" />
        </el-form-item>
        <el-form-item label="计入统计">
          <el-switch v-model="currentRecord.excludeFromStats" :active-value="false" :inactive-value="true" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const transactions = ref([])
const categories = ref([])
const channels = ref([])
const dateRange = ref([])
const filter = reactive({ category: '' })
const page = reactive({ current: 1, size: 20, total: 0 })
const dialogVisible = ref(false)
const currentRecord = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: page.current,
      size: page.size,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      category: filter.category
    }
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    transactions.value = res.data.data.records
    page.total = res.data.data.total
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await axios.get('http://localhost:8080/api/category/list')
  categories.value = res.data
}

const loadChannels = async () => {
  const res = await axios.get('http://localhost:8080/api/payment-channel/list')
  channels.value = res.data
}

const editRecord = (row) => {
  currentRecord.value = { ...row }
  dialogVisible.value = true
}

const saveRecord = async () => {
  try {
    await axios.put(`http://localhost:8080/api/bill/transaction/${currentRecord.value.id}`, currentRecord.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  }
}

onMounted(() => {
  loadData()
  loadCategories()
  loadChannels()
})
</script>

<style scoped>
.transaction-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
