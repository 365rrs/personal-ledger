<template>
  <div class="data-analysis">
    <el-card class="analysis-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><DataAnalysis /></el-icon>
            <span>数据分析 - 按天统计</span>
          </div>
          <el-button type="primary" size="small" @click="goBack" :icon="ArrowLeft">返回</el-button>
        </div>
      </template>

      <el-empty v-if="!billData?.data || billData.data.length === 0" description="暂无数据可分析">
        <el-button type="primary" @click="goBack">返回账单解析</el-button>
      </el-empty>

      <div v-else>
        <!-- 视图切换 -->
        <el-radio-group v-model="viewMode" class="view-toggle">
          <el-radio-button value="bar">柱状图</el-radio-button>
          <el-radio-button value="calendar">日历热力图</el-radio-button>
        </el-radio-group>

        <!-- 柱状图 -->
        <div v-show="viewMode === 'bar'" ref="barChart" class="chart-container"></div>

        <!-- 日历热力图 -->
        <div v-show="viewMode === 'calendar'" class="calendar-container">
          <div v-for="month in monthlyCalendars" :key="month.month" class="month-calendar">
            <div class="month-title">{{ month.month }}</div>
            <div class="calendar-grid">
              <div class="weekday-header">
                <div class="weekday-cell">日</div>
                <div class="weekday-cell">一</div>
                <div class="weekday-cell">二</div>
                <div class="weekday-cell">三</div>
                <div class="weekday-cell">四</div>
                <div class="weekday-cell">五</div>
                <div class="weekday-cell">六</div>
              </div>
              <div class="calendar-body">
                <div v-for="(day, index) in month.days" :key="index" 
                     :class="['day-cell', day.value !== null ? getColorClass(day.value) : 'empty', day.date === selectedDate ? 'selected' : '']"
                     @click="day.date && selectDate(day.date)">
                  <template v-if="day.value !== null">
                    <div class="day-number">{{ day.day }}</div>
                    <div class="day-value">{{ formatValue(day.value) }}</div>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计表格 -->
        <div v-if="selectedDate" class="selected-date-info">
          <el-tag type="primary" closable @close="selectedDate = ''">已选择: {{ selectedDate }}</el-tag>
        </div>
        <el-table :data="displayStats" class="stats-table" max-height="400">
          <el-table-column prop="date" label="日期" width="120" sortable></el-table-column>
          <el-table-column prop="income" label="收入" width="120" sortable>
            <template #default="scope">
              <span class="income-text">{{ scope.row.income.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="expense" label="支出" width="120" sortable>
            <template #default="scope">
              <span class="expense-text">{{ scope.row.expense.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="结余" width="120" sortable>
            <template #default="scope">
              <span :class="scope.row.balance >= 0 ? 'income-text' : 'expense-text'">
                {{ scope.row.balance.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="交易笔数" width="100" sortable></el-table-column>
        </el-table>
        
        <!-- 当日明细 -->
        <div v-if="selectedDate && selectedDayDetails.length > 0" class="day-details">
          <div class="details-title">当日交易明细</div>
          <el-table :data="selectedDayDetails" max-height="300">
            <el-table-column prop="formattedTradeDate" label="交易日期" min-width="110"></el-table-column>
            <el-table-column prop="tradeTime" label="交易时间" min-width="90"></el-table-column>
            <el-table-column prop="income" label="收入" min-width="90">
              <template #default="scope">
                <span class="income-text">{{ scope.row.income || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="expense" label="支出" min-width="90">
              <template #default="scope">
                <span class="expense-text">{{ scope.row.expense || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tradeType" label="交易类型" min-width="140"></el-table-column>
            <el-table-column prop="category" label="分类" min-width="90">
              <template #default="scope">
                <span>{{ scope.row.category || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="交易备注" min-width="180" show-overflow-tooltip></el-table-column>
            <el-table-column prop="userRemark" label="用户备注" min-width="150" show-overflow-tooltip>
              <template #default="scope">
                <span>{{ scope.row.userRemark || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center" fixed="right">
              <template #default="scope">
                <el-button type="warning" size="small" @click="editRecord(scope.row)" :icon="Edit">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
    
    <!-- 交易记录编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="编辑交易记录"
      direction="rtl"
      size="500px"
    >
      <div v-if="currentRecord" class="record-detail">
        <el-form :model="currentRecord" label-width="120px">
          <el-form-item label="交易日期">
            <el-input v-model="currentRecord.formattedTradeDate" readonly />
          </el-form-item>
          <el-form-item label="交易时间">
            <el-input v-model="currentRecord.tradeTime" readonly />
          </el-form-item>
          <el-form-item label="收入">
            <el-input v-model="currentRecord.income" readonly />
          </el-form-item>
          <el-form-item label="支出">
            <el-input v-model="currentRecord.expense" readonly />
          </el-form-item>
          <el-form-item label="交易类型">
            <el-input v-model="currentRecord.tradeType" readonly />
          </el-form-item>
          <el-form-item label="交易备注">
            <el-input v-model="currentRecord.remark" readonly />
          </el-form-item>
          <el-form-item label="支付渠道">
            <el-input v-model="currentRecord.paymentChannel" placeholder="请输入支付渠道" />
          </el-form-item>
          <el-form-item label="收支类型">
            <el-input v-model="currentRecord.transactionType" placeholder="请输入收支类型" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="currentRecord.category" placeholder="请输入分类" />
          </el-form-item>
          <el-form-item label="用户备注">
            <el-input 
              v-model="currentRecord.userRemark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入用户备注"
            />
          </el-form-item>
          <el-form-item label="计入本月收支">
            <el-switch
              v-model="currentRecord.excludeFromMonthly"
              active-text="是"
              inactive-text="否"
            />
          </el-form-item>
        </el-form>
        
        <div class="drawer-footer">
          <el-space>
            <el-button @click="drawerVisible = false">取消</el-button>
            <el-button type="primary" @click="saveRecord">保存</el-button>
          </el-space>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { DataAnalysis, ArrowLeft, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import billStore from '@/store/billStore'

const route = useRoute()
const router = useRouter()
const billData = computed(() => billStore.state.currentBillData)
const viewMode = ref('bar')
const barChart = ref(null)
const selectedDate = ref('')
const drawerVisible = ref(false)
const currentRecord = ref(null)
let barChartInstance = null

// 加载账单数据
const loadBillData = () => {
  const importId = route.params.id
  billStore.loadBillData(importId)
}

// 按天统计数据
const dailyStats = computed(() => {
  if (!billData.value?.data) return []
  
  const statsMap = new Map()
  
  billData.value.data.forEach(item => {
    if (item.excludeFromMonthly === false) return // 不计入统计
    
    const date = item.formattedTradeDate || item.tradeDate
    if (!date) return
    
    if (!statsMap.has(date)) {
      statsMap.set(date, { date, income: 0, expense: 0, balance: 0, count: 0 })
    }
    
    const stat = statsMap.get(date)
    const income = parseFloat(item.income) || 0
    const expense = parseFloat(item.expense) || 0
    
    stat.income += income
    stat.expense += expense
    stat.balance += (income - expense)
    stat.count++
  })
  
  return Array.from(statsMap.values()).sort((a, b) => a.date.localeCompare(b.date))
})

// 显示的统计数据
const displayStats = computed(() => {
  if (selectedDate.value) {
    return dailyStats.value.filter(d => d.date === selectedDate.value)
  }
  return dailyStats.value
})

// 选中日期的明细
const selectedDayDetails = computed(() => {
  if (!selectedDate.value || !billData.value?.data) return []
  
  return billData.value.data.filter(item => {
    if (item.excludeFromMonthly === false) return false
    const date = item.formattedTradeDate || item.tradeDate
    return date === selectedDate.value
  })
})

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date
}

// 编辑记录
const editRecord = (record) => {
  currentRecord.value = { ...record }
  drawerVisible.value = true
}

// 保存记录
const saveRecord = () => {
  if (billStore.updateRecord(currentRecord.value)) {
    ElMessage.success('保存成功')
    drawerVisible.value = false
  } else {
    ElMessage.error('保存失败')
  }
}

// 初始化柱状图
const initBarChart = () => {
  if (!barChart.value || dailyStats.value.length === 0) return
  
  if (barChartInstance) {
    barChartInstance.dispose()
  }
  
  barChartInstance = echarts.init(barChart.value)
  
  const dates = dailyStats.value.map(d => d.date)
  const incomeData = dailyStats.value.map(d => d.income)
  const expenseData = dailyStats.value.map(d => d.expense)
  
  // 计算累计支出
  const cumulativeExpense = []
  let sum = 0
  expenseData.forEach(expense => {
    sum += expense
    cumulativeExpense.push(sum)
  })
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        let result = `${params[0].axisValue}<br/>`
        params.forEach(item => {
          result += `${item.marker}${item.seriesName}: ${typeof item.value === 'number' ? item.value.toFixed(2) : item.value}元<br/>`
        })
        return result
      }
    },
    onClick: (params) => {
      selectDate(params.name)
    },
    legend: {
      data: ['收入', '支出', '本月累计支出'],
      selected: {
        '收入': false,
        '支出': true,
        '本月累计支出': true
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '金额(元)'
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: incomeData,
        itemStyle: { color: '#67c23a' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => params.value > 0 ? params.value.toFixed(2) : ''
        }
      },
      {
        name: '支出',
        type: 'bar',
        data: expenseData,
        itemStyle: { color: '#f56c6c' },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => params.value > 0 ? params.value.toFixed(2) : ''
        }
      },
      {
        name: '本月累计支出',
        type: 'line',
        data: cumulativeExpense,
        itemStyle: { color: '#409eff' },
        lineStyle: { width: 2 },
        label: {
          show: true,
          formatter: (params) => params.value.toFixed(2)
        }
      }
    ]
  }
  
  barChartInstance.setOption(option)
  
  // 添加点击事件
  barChartInstance.on('click', (params) => {
    if (params.componentType === 'series') {
      selectDate(params.name)
    }
  })
}

// 按月生成日历数据
const monthlyCalendars = computed(() => {
  if (!dailyStats.value || dailyStats.value.length === 0) return []
  
  const monthlyData = {}
  dailyStats.value.forEach(d => {
    const month = d.date.substring(0, 7)
    if (!monthlyData[month]) {
      monthlyData[month] = {}
    }
    monthlyData[month][d.date] = d.balance
  })
  
  const result = []
  Object.keys(monthlyData).sort().forEach(month => {
    const [year, monthNum] = month.split('-')
    const firstDay = new Date(year, parseInt(monthNum) - 1, 1)
    const lastDay = new Date(year, parseInt(monthNum), 0)
    const startWeekday = firstDay.getDay()
    const daysInMonth = lastDay.getDate()
    
    const days = []
    // 填充月初空白
    for (let i = 0; i < startWeekday; i++) {
      days.push({ day: null, value: null })
    }
    // 填充每天数据
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${month}-${String(day).padStart(2, '0')}`
      days.push({
        day,
        date: dateStr,
        value: monthlyData[month][dateStr] || 0
      })
    }
    
    result.push({ month, days })
  })
  
  return result
})

// 获取颜色类
const getColorClass = (value) => {
  if (value > 500) return 'profit-high'
  if (value > 0) return 'profit-low'
  if (value > -500) return 'loss-low'
  return 'loss-high'
}

// 格式化数值
const formatValue = (value) => {
  if (value === 0) return '0'
  return (value > 0 ? '+' : '') + value.toFixed(2)
}

// 返回
const goBack = () => {
  const importId = billStore.getImportId()
  if (importId) {
    router.push(`/bill-analysis/${importId}`)
  } else {
    router.push('/bill-analysis')
  }
}

// 监听视图模式切换
watch(viewMode, async (newMode) => {
  await nextTick()
  if (newMode === 'bar') {
    initBarChart()
  }
})

// 监听窗口大小变化
const handleResize = () => {
  barChartInstance?.resize()
}

onMounted(() => {
  loadBillData()
  nextTick(() => {
    initBarChart()
  })
  window.addEventListener('resize', handleResize)
})

// 清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  barChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.data-analysis {
  padding: 0;
}

.analysis-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 18px;
}

.view-toggle {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 500px;
  margin-bottom: 20px;
}

.calendar-container {
  width: 100%;
  margin-bottom: 20px;
}

.month-calendar {
  margin-bottom: 30px;
}

.month-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
}

.calendar-grid {
  display: inline-block;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 60px);
  gap: 4px;
  margin-bottom: 4px;
}

.weekday-cell {
  width: 60px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 60px);
  gap: 4px;
}

.day-cell {
  width: 60px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.day-cell.empty {
  background: transparent;
  cursor: default;
}

.day-cell.profit-high {
  background: #67c23a;
  color: #fff;
}

.day-cell.profit-low {
  background: #95d475;
  color: #fff;
}

.day-cell.loss-low {
  background: #ffa39e;
  color: #fff;
}

.day-cell.loss-high {
  background: #f56c6c;
  color: #fff;
}

.day-cell:not(.empty):hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.day-cell.selected {
  border: 3px solid #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 2px;
}

.day-value {
  font-size: 12px;
  font-weight: 500;
}

.stats-table {
  margin-top: 20px;
}

.income-text {
  color: #67c23a;
  font-weight: 600;
}

.expense-text {
  color: #f56c6c;
  font-weight: 600;
}

.selected-date-info {
  margin-bottom: 15px;
}

.day-details {
  margin-top: 30px;
}

.details-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
}

.record-detail {
  padding: 20px 0;
}

.drawer-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}
</style>
