<template>
  <div class="large-transaction-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>大额交易分析</span>
        </div>
      </template>

      <el-form inline>
        <el-form-item label="快捷选择">
          <el-button @click="setThisYear">今年</el-button>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" @change="loadData" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" @change="loadData" />
        </el-form-item>
        <el-form-item label="TOP">
          <el-input-number v-model="topN" :min="5" :max="50" @change="loadData" />
        </el-form-item>
      </el-form>

      <el-table :data="largeTransactions" v-loading="loading">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="transactionDate" label="交易日期" width="120" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="transactionType" label="交易类型" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="paymentChannel" label="支付渠道" width="100" />
        <el-table-column prop="description" label="交易描述" show-overflow-tooltip />
        <el-table-column prop="userNote" label="用户备注" width="150" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const topN = ref(20)
const largeTransactions = ref([])

const today = new Date()
const year = today.getFullYear()
const month = today.getMonth()
const firstDay = new Date(year, month, 1)
const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const startDate = ref(formatDate(firstDay))
const endDate = ref(formatDate(today))

const setThisYear = () => {
  const now = new Date()
  startDate.value = `${now.getFullYear()}-01-01`
  endDate.value = formatDate(now)
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/bill/transaction/list', {
      params: {
        current: 1,
        size: 10000,
        startDate: startDate.value,
        endDate: endDate.value,
        includeInStats: true,
        incomeOrExpense: 'expense'
      }
    })
    
    if (res.data.success) {
      const transactions = res.data.data.records
      processLargeTransactions(transactions)
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const processLargeTransactions = (transactions) => {
  const list = transactions.map(t => ({
    ...t,
    amount: parseFloat(t.expense || 0)
  }))
  .filter(t => t.amount > 0)
  .sort((a, b) => b.amount - a.amount)
  .slice(0, topN.value)
  
  largeTransactions.value = list
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.large-transaction-analysis {
  padding: 20px;
}

.header {
  font-size: 18px;
  font-weight: bold;
}
</style>
