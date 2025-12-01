<template>
  <div class="consumption-habits">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>消费习惯分析</span>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          分析您的消费习惯，发现消费模式和特征
        </template>
      </el-alert>

      <!-- 时间范围选择 -->
      <el-form label-width="80px" style="margin-bottom: 20px;">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分析时段">
              <el-radio-group v-model="timeRange" @change="loadData">
                <el-radio-button label="thisMonth">本月</el-radio-button>
                <el-radio-button label="last3Months">近3月</el-radio-button>
                <el-radio-button label="thisYear">今年</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 核心指标 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-label">总消费</div>
            <div class="metric-value">¥{{ metrics.total }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-label">消费笔数</div>
            <div class="metric-value">{{ metrics.count }}笔</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-label">日均消费</div>
            <div class="metric-value">¥{{ metrics.daily }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-label">单笔均值</div>
            <div class="metric-value">¥{{ metrics.average }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-divider>消费时段分布</el-divider>
      <div ref="timeChartRef" style="width: 100%; height: 300px;"></div>

      <el-divider>单笔消费金额分布</el-divider>
      <div ref="amountChartRef" style="width: 100%; height: 300px;"></div>

      <el-divider>高频商户TOP10</el-divider>
      <el-table :data="topMerchants" style="width: 100%">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="merchant" label="商户/描述" show-overflow-tooltip />
        <el-table-column prop="count" label="消费次数" width="120" align="right" />
        <el-table-column prop="amount" label="消费金额" width="150" align="right">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgAmount" label="单笔均值" width="120" align="right" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const timeRange = ref('thisMonth')
const timeChartRef = ref(null)
const amountChartRef = ref(null)
const metrics = reactive({
  total: '0.00',
  count: 0,
  daily: '0.00',
  average: '0.00'
})
const topMerchants = ref([])

let timeChart = null
let amountChart = null

const getDateRange = () => {
  const now = new Date()
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }

  switch (timeRange.value) {
    case 'thisMonth':
      return {
        startDate: formatDate(new Date(now.getFullYear(), now.getMonth(), 1)),
        endDate: formatDate(now),
        days: now.getDate()
      }
    case 'last3Months':
      return {
        startDate: formatDate(new Date(now.getFullYear(), now.getMonth() - 2, 1)),
        endDate: formatDate(now),
        days: 90
      }
    case 'thisYear':
      return {
        startDate: formatDate(new Date(now.getFullYear(), 0, 1)),
        endDate: formatDate(now),
        days: Math.ceil((now - new Date(now.getFullYear(), 0, 1)) / (1000 * 60 * 60 * 24))
      }
  }
}

const loadData = async () => {
  try {
    const { startDate, endDate, days } = getDateRange()

    // 获取交易列表
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', {
      params: {
        startDate,
        endDate,
        size: 10000,
        incomeOrExpense: 'expense'
      }
    })

    const transactions = res.data.data.records || []
    
    // 计算指标
    const total = transactions.reduce((sum, t) => sum + parseFloat(t.expense || 0), 0)
    metrics.total = total.toFixed(2)
    metrics.count = transactions.length
    metrics.daily = (total / days).toFixed(2)
    metrics.average = transactions.length > 0 ? (total / transactions.length).toFixed(2) : '0.00'

    // 分析时段分布
    analyzeTimeDistribution(transactions)
    
    // 分析金额分布
    analyzeAmountDistribution(transactions)
    
    // 分析高频商户
    analyzeTopMerchants(transactions)

  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const analyzeTimeDistribution = (transactions) => {
  const hourCounts = new Array(24).fill(0)
  
  transactions.forEach(t => {
    if (t.transactionTime) {
      const hour = parseInt(t.transactionTime.split(':')[0])
      hourCounts[hour]++
    }
  })

  if (!timeChart) {
    timeChart = echarts.init(timeChartRef.value)
  }

  const option = {
    title: { text: '24小时消费分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 24 }, (_, i) => `${i}:00`)
    },
    yAxis: { type: 'value', name: '消费笔数' },
    series: [{
      data: hourCounts,
      type: 'bar',
      itemStyle: { color: '#409eff' }
    }]
  }

  timeChart.setOption(option)
}

const analyzeAmountDistribution = (transactions) => {
  const ranges = [
    { label: '0-50', min: 0, max: 50, count: 0 },
    { label: '50-100', min: 50, max: 100, count: 0 },
    { label: '100-200', min: 100, max: 200, count: 0 },
    { label: '200-500', min: 200, max: 500, count: 0 },
    { label: '500-1000', min: 500, max: 1000, count: 0 },
    { label: '1000+', min: 1000, max: Infinity, count: 0 }
  ]

  transactions.forEach(t => {
    const amount = parseFloat(t.expense || 0)
    const range = ranges.find(r => amount >= r.min && amount < r.max)
    if (range) range.count++
  })

  if (!amountChart) {
    amountChart = echarts.init(amountChartRef.value)
  }

  const option = {
    title: { text: '单笔消费金额分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: ranges.map(r => ({ name: r.label, value: r.count })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }

  amountChart.setOption(option)
}

const analyzeTopMerchants = (transactions) => {
  const merchantMap = {}

  transactions.forEach(t => {
    const merchant = t.description || '未知商户'
    if (!merchantMap[merchant]) {
      merchantMap[merchant] = { count: 0, amount: 0 }
    }
    merchantMap[merchant].count++
    merchantMap[merchant].amount += parseFloat(t.expense || 0)
  })

  topMerchants.value = Object.entries(merchantMap)
    .map(([merchant, data]) => ({
      merchant,
      count: data.count,
      amount: data.amount.toFixed(2),
      avgAmount: (data.amount / data.count).toFixed(2)
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
}

onMounted(async () => {
  await nextTick()
  loadData()
})
</script>

<style scoped>
.consumption-habits {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-card {
  text-align: center;
  padding: 10px;
}

.metric-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
</style>
