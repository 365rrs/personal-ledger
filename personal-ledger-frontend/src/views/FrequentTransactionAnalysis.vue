<template>
  <div class="frequent-transaction-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>高频交易分析</span>
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

      <el-table :data="frequentTransactions" v-loading="loading">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="count" label="交易次数" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.count }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="累计金额" width="150">
          <template #default="{ row }">
            <span style="font-weight: bold;">{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgAmount" label="平均金额" width="120" />
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
const frequentTransactions = ref([])

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
      processFrequentTransactions(transactions)
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const processFrequentTransactions = (transactions) => {
  const map = {}
  
  transactions.forEach(t => {
    const amount = parseFloat(t.expense || 0)
    if (amount <= 0) return
    
    const key = t.description || '未知'
    
    if (!map[key]) {
      map[key] = { name: key, count: 0, totalAmount: 0 }
    }
    map[key].count++
    map[key].totalAmount += amount
  })
  
  const list = Object.values(map)
    .map(item => ({
      ...item,
      totalAmount: item.totalAmount.toFixed(2),
      avgAmount: (item.totalAmount / item.count).toFixed(2)
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, topN.value)
  
  frequentTransactions.value = list
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.frequent-transaction-analysis {
  padding: 20px;
}

.header {
  font-size: 18px;
  font-weight: bold;
}
</style>
