<template>
  <div class="category-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>按分类统计</span>
          <el-space>
            <el-radio-group v-model="type" @change="loadData">
              <el-radio-button label="expense">支出</el-radio-button>
              <el-radio-button label="income">收入</el-radio-button>
            </el-radio-group>
            <el-date-picker v-model="dateRange" type="monthrange" value-format="YYYY-MM" @change="loadData" />
          </el-space>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        </el-col>
        <el-col :span="12">
          <el-table :data="categoryData" style="width: 100%">
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="amount" label="金额" align="right" />
            <el-table-column prop="count" label="笔数" align="center" width="80" />
            <el-table-column prop="percent" label="占比" align="right" width="80" />
          </el-table>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const chartRef = ref(null)
const type = ref('expense')
const dateRange = ref([])
const categoryData = ref([])
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
    
    processData(transactions)
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const processData = (transactions) => {
  const categoryMap = {}
  let total = 0

  transactions.forEach(t => {
    const isExpense = type.value === 'expense'
    const amount = isExpense ? t.expense : t.income
    if (!amount || amount <= 0) return

    const category = t.category || '未分类'
    if (!categoryMap[category]) {
      categoryMap[category] = { amount: 0, count: 0 }
    }
    categoryMap[category].amount += parseFloat(amount)
    categoryMap[category].count++
    total += parseFloat(amount)
  })

  categoryData.value = Object.entries(categoryMap).map(([category, data]) => ({
    category,
    amount: data.amount.toFixed(2),
    count: data.count,
    percent: ((data.amount / total) * 100).toFixed(1) + '%'
  })).sort((a, b) => parseFloat(b.amount) - parseFloat(a.amount))

  renderChart(categoryData.value)
}

const renderChart = (data) => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    title: { text: type.value === 'expense' ? '支出分类' : '收入分类', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.map(d => ({ name: d.category, value: parseFloat(d.amount) })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
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
.category-analysis {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
