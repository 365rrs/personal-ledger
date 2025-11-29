<template>
  <div class="data-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>按天统计分析</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form label-width="80px" class="filter-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="快捷日期">
              <el-space wrap>
                <el-button @click="setQuickDate('today')">今天</el-button>
                <el-button @click="setQuickDate('thisMonth')">本月</el-button>
                <el-button @click="setQuickDate('lastMonth')">上月</el-button>
                <el-button @click="setQuickDate('last3Months')">近3月</el-button>
                <el-button @click="setQuickDate('thisYear')">今年</el-button>
              </el-space>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期范围">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-date-picker v-model="filter.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="flex: 1" />
                <span>至</span>
                <el-date-picker v-model="filter.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分类">
              <el-select v-model="filter.category" clearable filterable placeholder="全部">
                <el-option label="未分类" value="__UNCATEGORIZED__" />
                <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付渠道">
              <el-select v-model="filter.paymentChannel" clearable filterable placeholder="全部">
                <el-option v-for="ch in channels" :key="ch" :label="ch" :value="ch" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label=" ">
              <el-button type="primary" @click="loadData">应用筛选</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div ref="chartRef" style="width: 100%; height: 500px;"></div>

      <!-- 当日明细 -->
      <div v-if="selectedDate" style="margin-top: 20px;">
        <div style="margin-bottom: 10px;">
          <el-tag type="primary" closable @close="selectedDate = ''">{{ selectedDate }}</el-tag>
        </div>
        <el-table :data="dayDetails" max-height="400">
          <el-table-column prop="transactionDate" label="交易日期" width="110" />
          <el-table-column prop="transactionTime" label="交易时间" width="90" />
          <el-table-column prop="income" label="收入" width="100">
            <template #default="{ row }">
              <span style="color: #67c23a;">{{ row.income || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="expense" label="支出" width="100">
            <template #default="{ row }">
              <span style="color: #f56c6c;">{{ row.expense || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="transactionType" label="交易类型" width="140" />
          <el-table-column prop="paymentChannel" label="支付渠道" width="120" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="description" label="交易备注" min-width="180" show-overflow-tooltip />
          <el-table-column prop="userNote" label="用户备注" width="150" show-overflow-tooltip />
          <el-table-column prop="excludeFromStats" label="计入收支" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.excludeFromStats ? 'info' : 'success'" size="small">
                {{ row.excludeFromStats ? '否' : '是' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editRecord(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 编辑抽屉 -->
    <el-drawer v-model="drawerVisible" title="编辑交易记录" size="500px">
      <el-form v-if="currentRecord" label-width="100px">
        <el-form-item label="交易日期">
          <el-input v-model="currentRecord.transactionDate" readonly />
        </el-form-item>
        <el-form-item label="交易时间">
          <el-input v-model="currentRecord.transactionTime" readonly />
        </el-form-item>
        <el-form-item label="收入">
          <el-input v-model="currentRecord.income" readonly />
        </el-form-item>
        <el-form-item label="支出">
          <el-input v-model="currentRecord.expense" readonly />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-input v-model="currentRecord.transactionType" readonly />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentRecord.description" readonly />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="currentRecord.paymentChannel" clearable filterable placeholder="请选择">
            <el-option v-for="ch in channels" :key="ch" :label="ch" :value="ch" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="currentRecord.category" clearable filterable placeholder="请选择">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户备注">
          <el-input v-model="currentRecord.userNote" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="计入收支">
          <el-switch v-model="currentRecord.includeInStats" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const chartRef = ref(null)
const categories = ref([])
const channels = ref([])
const selectedDate = ref('')
const dayDetails = ref([])
const drawerVisible = ref(false)
const currentRecord = ref(null)
let chart = null

const getThisMonthDates = () => {
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
  return {
    startDate: formatDate(firstDay),
    endDate: formatDate(today)
  }
}

const thisMonth = getThisMonthDates()
const filter = ref({
  startDate: thisMonth.startDate,
  endDate: thisMonth.endDate,
  category: '',
  paymentChannel: ''
})

const setQuickDate = (type) => {
  const today = new Date()
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  
  switch(type) {
    case 'today':
      filter.value.startDate = filter.value.endDate = formatDate(today)
      break
    case 'thisMonth':
      filter.value.startDate = formatDate(new Date(today.getFullYear(), today.getMonth(), 1))
      filter.value.endDate = formatDate(today)
      break
    case 'lastMonth':
      filter.value.startDate = formatDate(new Date(today.getFullYear(), today.getMonth() - 1, 1))
      filter.value.endDate = formatDate(new Date(today.getFullYear(), today.getMonth(), 0))
      break
    case 'last3Months':
      filter.value.startDate = formatDate(new Date(today.getFullYear(), today.getMonth() - 2, 1))
      filter.value.endDate = formatDate(today)
      break
    case 'thisYear':
      filter.value.startDate = formatDate(new Date(today.getFullYear(), 0, 1))
      filter.value.endDate = formatDate(today)
      break
  }
}

const resetFilter = () => {
  filter.value = {
    startDate: thisMonth.startDate,
    endDate: thisMonth.endDate,
    category: '',
    paymentChannel: ''
  }
  loadData()
}

const loadData = async () => {
  try {
    const params = {
      startDate: filter.value.startDate,
      endDate: filter.value.endDate
    }
    
    if (filter.value.category) {
      params.category = filter.value.category === '__UNCATEGORIZED__' ? '' : filter.value.category
    }
    if (filter.value.paymentChannel) {
      params.paymentChannel = filter.value.paymentChannel
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/daily-stats', { params })
    const stats = res.data.data || []
    
    renderChart(stats)
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const renderChart = (stats) => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const dates = stats.map(s => s.date)
  const incomeData = stats.map(s => parseFloat(s.income) || 0)
  const expenseData = stats.map(s => parseFloat(s.expense) || 0)
  
  // 计算累计支出
  const cumulativeExpense = []
  let sum = 0
  expenseData.forEach(expense => {
    sum += expense
    cumulativeExpense.push(sum)
  })

  const option = {
    title: { text: '每日收支统计' },
    tooltip: { trigger: 'axis' },
    legend: { 
      data: ['收入', '支出', '累计支出'],
      selected: {
        '收入': false,
        '支出': true,
        '累计支出': true
      }
    },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
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
        name: '累计支出',
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

  chart.setOption(option)
  
  // 添加点击事件
  chart.off('click')
  chart.on('click', (params) => {
    if (params.componentType === 'series') {
      loadDayDetails(params.name)
    }
  })
}

const loadDayDetails = async (date) => {
  selectedDate.value = date
  try {
    const params = {
      size: 10000,
      startDate: date,
      endDate: date,
      excludeFromStats: false
    }
    
    if (filter.value.category) {
      params.category = filter.value.category === '__UNCATEGORIZED__' ? '' : filter.value.category
    }
    if (filter.value.paymentChannel) {
      params.paymentChannel = filter.value.paymentChannel
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    dayDetails.value = res.data.data?.records || []
  } catch (error) {
    ElMessage.error('加载明细失败')
  }
}

const editRecord = (record) => {
  currentRecord.value = { 
    ...record,
    includeInStats: !record.excludeFromStats
  }
  drawerVisible.value = true
}

const saveRecord = async () => {
  try {
    await axios.put(`http://localhost:8080/api/bill/transaction/${currentRecord.value.id}`, {
      category: currentRecord.value.category,
      paymentChannel: currentRecord.value.paymentChannel,
      userNote: currentRecord.value.userNote,
      excludeFromStats: !currentRecord.value.includeInStats
    })
    ElMessage.success('保存成功')
    drawerVisible.value = false
    loadData()
    if (selectedDate.value) {
      loadDayDetails(selectedDate.value)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/category/list')
    categories.value = res.data.map(c => c.name)
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadChannels = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/payment-channel/list')
    channels.value = res.data.map(c => c.name)
  } catch (error) {
    console.error('加载支付渠道失败', error)
  }
}

onMounted(async () => {
  await nextTick()
  loadCategories()
  loadChannels()
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

.filter-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 8px;
}
</style>
