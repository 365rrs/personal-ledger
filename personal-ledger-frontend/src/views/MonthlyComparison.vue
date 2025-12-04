<template>
  <div class="monthly-comparison">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>月度消费对比</span>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          对比本月与上月的消费情况，了解消费变化趋势
        </template>
      </el-alert>

      <div class="comparison-cards">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-label">本月消费</div>
              <div class="stat-value current">¥{{ currentMonth.total }}</div>
              <div class="stat-change" :class="changeClass">
                <el-icon v-if="changePercent > 0"><CaretTop /></el-icon>
                <el-icon v-else-if="changePercent < 0"><CaretBottom /></el-icon>
                {{ Math.abs(changePercent) }}% vs上月
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-label">上月消费</div>
              <div class="stat-value last">¥{{ lastMonth.total }}</div>
              <div class="stat-info">{{ lastMonth.count }}笔交易</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-label">日均消费</div>
              <div class="stat-value">¥{{ currentMonth.daily }}</div>
              <div class="stat-info">本月已过{{ daysInMonth }}天</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div ref="chartRef" style="width: 100%; height: 400px; margin-top: 20px;"></div>

      <el-divider>分类消费对比</el-divider>

      <el-table :data="categoryComparison" style="width: 100%">
        <el-table-column prop="category" label="分类" width="150" />
        <el-table-column prop="currentAmount" label="本月金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #409eff;">¥{{ row.currentAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastAmount" label="上月金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #909399;">¥{{ row.lastAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="change" label="变化金额" width="120" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.change > 0 ? '#f56c6c' : '#67c23a' }">
              {{ row.change > 0 ? '+' : '' }}¥{{ row.change }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="changePercent" label="变化率" width="100" align="right">
          <template #default="{ row }">
            <el-tag :type="row.changePercent > 0 ? 'danger' : 'success'" size="small">
              {{ row.changePercent > 0 ? '+' : '' }}{{ row.changePercent }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="趋势" width="200">
          <template #default="{ row }">
            <el-progress 
              :percentage="Math.abs(row.changePercent)" 
              :color="row.changePercent > 0 ? '#f56c6c' : '#67c23a'"
              :show-text="false"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const chartRef = ref(null)
const currentMonth = ref({ total: '0.00', count: 0, daily: '0.00' })
const lastMonth = ref({ total: '0.00', count: 0 })
const categoryComparison = ref([])
const daysInMonth = ref(0)

let chart = null

const changePercent = computed(() => {
  const current = parseFloat(currentMonth.value.total)
  const last = parseFloat(lastMonth.value.total)
  if (last === 0) return 0
  return ((current - last) / last * 100).toFixed(1)
})

const changeClass = computed(() => {
  return changePercent.value > 0 ? 'increase' : changePercent.value < 0 ? 'decrease' : ''
})

const loadData = async () => {
  try {
    const now = new Date()
    const currentYear = now.getFullYear()
    const currentMonthNum = now.getMonth() + 1
    const lastMonthNum = currentMonthNum === 1 ? 12 : currentMonthNum - 1
    const lastYear = currentMonthNum === 1 ? currentYear - 1 : currentYear

    // 本月数据
    const currentStart = `${currentYear}-${String(currentMonthNum).padStart(2, '0')}-01`
    const currentEnd = `${currentYear}-${String(currentMonthNum).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
    
    // 上月数据
    const lastMonthDays = new Date(lastYear, lastMonthNum, 0).getDate()
    const lastStart = `${lastYear}-${String(lastMonthNum).padStart(2, '0')}-01`
    const lastEnd = `${lastYear}-${String(lastMonthNum).padStart(2, '0')}-${lastMonthDays}`

    daysInMonth.value = now.getDate()

    // 获取本月汇总
    const currentRes = await axios.get('http://localhost:8080/api/bill/transaction/summary', {
      params: { startDate: currentStart, endDate: currentEnd, includeInStats: true }
    })
    
    // 获取上月汇总
    const lastRes = await axios.get('http://localhost:8080/api/bill/transaction/summary', {
      params: { startDate: lastStart, endDate: lastEnd, includeInStats: true }
    })

    currentMonth.value = {
      total: parseFloat(currentRes.data.data.expense).toFixed(2),
      count: 0,
      daily: (parseFloat(currentRes.data.data.expense) / daysInMonth.value).toFixed(2)
    }

    lastMonth.value = {
      total: parseFloat(lastRes.data.data.expense).toFixed(2),
      count: 0
    }

    // 获取分类对比数据
    await loadCategoryComparison(currentStart, currentEnd, lastStart, lastEnd)
    
    renderChart()
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const loadCategoryComparison = async (currentStart, currentEnd, lastStart, lastEnd) => {
  try {
    const [currentStats, lastStats] = await Promise.all([
      axios.get('http://localhost:8080/api/bill/transaction/category-stats', {
        params: { startDate: currentStart, endDate: currentEnd, type: 'expense', includeInStats: true }
      }),
      axios.get('http://localhost:8080/api/bill/transaction/category-stats', {
        params: { startDate: lastStart, endDate: lastEnd, type: 'expense', includeInStats: true }
      })
    ])

    const currentMap = {}
    const lastMap = {}

    currentStats.data.data.forEach(item => {
      currentMap[item.category] = parseFloat(item.amount)
    })

    lastStats.data.data.forEach(item => {
      lastMap[item.category] = parseFloat(item.amount)
    })

    const allCategories = new Set([...Object.keys(currentMap), ...Object.keys(lastMap)])
    
    categoryComparison.value = Array.from(allCategories).map(category => {
      const current = currentMap[category] || 0
      const last = lastMap[category] || 0
      const change = current - last
      const changePercent = last === 0 ? (current > 0 ? 100 : 0) : ((change / last) * 100).toFixed(1)

      return {
        category,
        currentAmount: current.toFixed(2),
        lastAmount: last.toFixed(2),
        change: change.toFixed(2),
        changePercent: parseFloat(changePercent)
      }
    }).sort((a, b) => parseFloat(b.currentAmount) - parseFloat(a.currentAmount))

  } catch (error) {
    console.error('加载分类对比失败', error)
  }
}

const renderChart = () => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    title: { text: '本月 vs 上月消费对比' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['本月', '上月'] },
    xAxis: {
      type: 'category',
      data: categoryComparison.value.map(c => c.category)
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '本月',
        type: 'bar',
        data: categoryComparison.value.map(c => parseFloat(c.currentAmount)),
        itemStyle: { color: '#409eff' }
      },
      {
        name: '上月',
        type: 'bar',
        data: categoryComparison.value.map(c => parseFloat(c.lastAmount)),
        itemStyle: { color: '#909399' }
      }
    ]
  }

  chart.setOption(option)
}

onMounted(async () => {
  await nextTick()
  loadData()
})
</script>

<style scoped>
.monthly-comparison {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comparison-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-value.current {
  color: #409eff;
}

.stat-value.last {
  color: #909399;
}

.stat-change {
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.stat-change.increase {
  color: #f56c6c;
}

.stat-change.decrease {
  color: #67c23a;
}

.stat-info {
  font-size: 12px;
  color: #909399;
}
</style>
