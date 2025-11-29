<template>
  <div class="data-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>按天统计分析</span>
          <el-date-picker v-model="dateRange" type="monthrange" value-format="YYYY-MM" @change="loadData" />
        </div>
      </template>

      <div ref="chartRef" style="width: 100%; height: 500px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const chartRef = ref(null)
const dateRange = ref([])
let chart = null

const loadData = async () => {
  try {
    const getMonthEnd = (yearMonth) => {
      const [year, month] = yearMonth.split('-')
      return new Date(year, month, 0).getDate()
    }
    
    const params = {
      current: 1,
      size: 10000,
      startDate: dateRange.value?.[0] ? `${dateRange.value[0]}-01` : undefined,
      endDate: dateRange.value?.[1] ? `${dateRange.value[1]}-${getMonthEnd(dateRange.value[1])}` : undefined
    }
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    const transactions = res.data.data?.records || []
    
    renderChart(transactions)
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const renderChart = (transactions) => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const dateMap = {}
  transactions.forEach(t => {
    const date = t.transactionDate
    if (!dateMap[date]) {
      dateMap[date] = { income: 0, expense: 0 }
    }
    if (t.income) dateMap[date].income += parseFloat(t.income)
    if (t.expense) dateMap[date].expense += parseFloat(t.expense)
  })

  const dates = Object.keys(dateMap).sort()
  const incomeData = dates.map(d => dateMap[d].income.toFixed(2))
  const expenseData = dates.map(d => dateMap[d].expense.toFixed(2))

  const option = {
    title: { text: '每日收支统计' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'bar', data: incomeData, itemStyle: { color: '#67c23a' } },
      { name: '支出', type: 'bar', data: expenseData, itemStyle: { color: '#f56c6c' } }
    ]
  }

  chart.setOption(option)
}

onMounted(async () => {
  await nextTick()
  const now = new Date()
  dateRange.value = [
    `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`,
    `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  ]
  loadData()
})
</script>

<style scoped>
.data-analysis {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
