<template>
  <div class="transaction-analysis">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div class="header">
          <span>大额交易分析</span>
        </div>
      </template>

      <el-form inline>
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

    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>高频交易分析</span>
        </div>
      </template>

      <el-form inline>
        <el-form-item label="分组维度">
          <el-select v-model="groupBy" @change="loadData">
            <el-option label="按商户" value="description" />
            <el-option label="按分类" value="category" />
            <el-option label="按支付渠道" value="paymentChannel" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const topN = ref(20)
const groupBy = ref('description')
const largeTransactions = ref([])
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

const loadData = async () => {
  loading.value = true
  try {
    const start = startDate.value || ''
    const end = endDate.value || ''
    
    const res = await axios.get('/api/bill/transaction/list', {
      params: {
        current: 1,
        size: 10000,
        startDate: start,
        endDate: end,
        includeInStats: true
      }
    })
    
    if (res.data.success) {
      const transactions = res.data.data.records
      processLargeTransactions(transactions)
      processFrequentTransactions(transactions)
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
    amount: parseFloat(t.expense || t.income || 0)
  }))
  .filter(t => t.amount > 0)
  .sort((a, b) => b.amount - a.amount)
  .slice(0, topN.value)
  
  largeTransactions.value = list
}

const processFrequentTransactions = (transactions) => {
  const map = {}
  
  transactions.forEach(t => {
    const amount = parseFloat(t.expense || t.income || 0)
    if (amount <= 0) return
    
    let key = ''
    if (groupBy.value === 'description') {
      key = t.description || '未知'
    } else if (groupBy.value === 'category') {
      key = t.category || '未分类'
    } else if (groupBy.value === 'paymentChannel') {
      key = t.paymentChannel || '未知'
    }
    
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
    .slice(0, 20)
  
  frequentTransactions.value = list
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.transaction-analysis {
  padding: 20px;
}

.header {
  font-size: 18px;
  font-weight: bold;
}
</style>
