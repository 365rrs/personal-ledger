<template>
  <div class="cumulative-balance">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>累计结余分析</span>
        </div>
      </template>

      <el-form inline>
        <el-form-item label="年份">
          <el-date-picker v-model="year" type="year" placeholder="选择年份" value-format="YYYY" @change="loadData" />
        </el-form-item>
      </el-form>

      <div ref="chartRef" style="width: 100%; height: 500px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const chartRef = ref(null)
let chartInstance = null
const year = ref(new Date().getFullYear().toString())

const loadData = async () => {
  try {
    const startDate = `${year.value}-01-01`
    const endDate = `${year.value}-12-31`
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/daily-stats', {
      params: { startDate, endDate }
    })
    
    if (res.data.success) {
      const dailyData = res.data.data
      const monthlyData = processMonthlyData(dailyData)
      renderChart(monthlyData)
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const processMonthlyData = (dailyData) => {
  const monthlyMap = {}
  
  dailyData.forEach(item => {
    const month = item.date.substring(0, 7)
    if (!monthlyMap[month]) {
      monthlyMap[month] = { income: 0, expense: 0 }
    }
    const income = parseFloat(item.income) || 0
    const expense = parseFloat(item.expense) || 0
    monthlyMap[month].income += income
    monthlyMap[month].expense += expense
  })
  
  const months = []
  const balances = []
  const cumulativeBalances = []
  let cumulative = 0
  
  for (let i = 1; i <= 12; i++) {
    const month = `${year.value}-${String(i).padStart(2, '0')}`
    months.push(`${i}月`)
    
    const data = monthlyMap[month] || { income: 0, expense: 0 }
    const balance = -(data.income - data.expense)
    balances.push(Number(balance.toFixed(2)))
    
    cumulative += balance
    cumulativeBalances.push(Number(cumulative.toFixed(2)))
  }
  
  return { months, balances, cumulativeBalances }
}

const renderChart = (data) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  
  const option = {
    title: {
      text: `${year.value}年累计结余统计`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = `${params[0].name}<br/>`
        params.forEach(item => {
          const value = parseFloat(item.value)
          result += `${item.seriesName}: ${value.toFixed(2)} 元<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['当月结余', '累计结余'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.months
    },
    yAxis: {
      type: 'value',
      name: '金额(元)'
    },
    series: [
      {
        name: '当月结余',
        type: 'bar',
        data: data.balances,
        itemStyle: {
          color: '#67c23a'
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => {
            return parseFloat(params.value).toFixed(2)
          }
        }
      },
      {
        name: '累计结余',
        type: 'line',
        data: data.cumulativeBalances,
        itemStyle: {
          color: '#409eff'
        },
        lineStyle: {
          width: 3
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return parseFloat(params.value).toFixed(2)
          }
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
})
</script>

<style scoped>
.cumulative-balance {
  padding: 20px;
}

.header {
  font-size: 18px;
  font-weight: bold;
}
</style>
