<template>
  <div class="category-analysis">
    <el-card class="analysis-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><PieChart /></el-icon>
            <span>数据分析 - 按分类统计</span>
          </div>
          <el-button type="primary" size="small" @click="goBack" :icon="ArrowLeft">返回</el-button>
        </div>
      </template>

      <el-empty v-if="!billData?.data || billData.data.length === 0" description="暂无数据可分析">
        <el-button type="primary" @click="goBack">返回账单解析</el-button>
      </el-empty>

      <div v-else>
        <!-- 收支类型切换 -->
        <el-radio-group v-model="transactionType" class="type-toggle">
          <el-radio-button value="expense">支出分类</el-radio-button>
          <el-radio-button value="income">收入分类</el-radio-button>
        </el-radio-group>

        <!-- 饼图 -->
        <div ref="pieChart" class="chart-container"></div>

        <!-- 分类统计表格 -->
        <el-table :data="categoryStats" class="stats-table" @row-click="selectCategory">
          <el-table-column prop="category" label="分类" width="200"></el-table-column>
          <el-table-column prop="amount" label="金额" width="150" sortable>
            <template #default="scope">
              <span :class="transactionType === 'income' ? 'income-text' : 'expense-text'">
                {{ scope.row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="笔数" width="100" sortable></el-table-column>
          <el-table-column prop="percentage" label="占比" width="100" sortable>
            <template #default="scope">
              {{ scope.row.percentage.toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column prop="avgAmount" label="平均金额" width="120" sortable>
            <template #default="scope">
              {{ scope.row.avgAmount.toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 分类明细 -->
        <div v-if="selectedCategory && categoryDetails.length > 0" class="category-details">
          <div class="details-title">
            <el-tag type="primary" closable @close="selectedCategory = ''">{{ selectedCategory }}</el-tag>
            分类明细
          </div>
          <el-table :data="categoryDetails" max-height="300">
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
import { PieChart, ArrowLeft, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import billStore from '@/store/billStore'

const route = useRoute()
const router = useRouter()
const billData = computed(() => billStore.state.currentBillData)
const transactionType = ref('expense')
const pieChart = ref(null)
const selectedCategory = ref('')
const drawerVisible = ref(false)
const currentRecord = ref(null)
let pieChartInstance = null

// 加载账单数据
const loadBillData = () => {
  const importId = route.params.id
  billStore.loadBillData(importId)
}

// 按分类统计数据
const categoryStats = computed(() => {
  if (!billData.value?.data) return []
  
  const statsMap = new Map()
  let total = 0
  
  billData.value.data.forEach(item => {
    if (item.excludeFromMonthly === false) return
    
    const isIncome = item.income && parseFloat(item.income) > 0
    const isExpense = item.expense && parseFloat(item.expense) > 0
    
    if (transactionType.value === 'income' && !isIncome) return
    if (transactionType.value === 'expense' && !isExpense) return
    
    const category = item.category || '未分类'
    const amount = parseFloat(isIncome ? item.income : item.expense) || 0
    
    if (!statsMap.has(category)) {
      statsMap.set(category, { category, amount: 0, count: 0 })
    }
    
    const stat = statsMap.get(category)
    stat.amount += amount
    stat.count++
    total += amount
  })
  
  const result = Array.from(statsMap.values()).map(stat => ({
    ...stat,
    percentage: total > 0 ? (stat.amount / total) * 100 : 0,
    avgAmount: stat.amount / stat.count
  }))
  
  return result.sort((a, b) => b.amount - a.amount)
})

// 初始化饼图
const initPieChart = () => {
  if (!pieChart.value || categoryStats.value.length === 0) return
  
  if (pieChartInstance) {
    pieChartInstance.dispose()
  }
  
  pieChartInstance = echarts.init(pieChart.value)
  
  const data = categoryStats.value.map(stat => ({
    name: stat.category,
    value: stat.amount
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>金额: ${params.value.toFixed(2)}元<br/>占比: ${params.percent.toFixed(2)}%`
      }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      type: 'scroll'
    },
    series: [{
      name: transactionType.value === 'income' ? '收入分类' : '支出分类',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data: data
    }]
  }
  
  pieChartInstance.setOption(option)
  
  pieChartInstance.on('click', (params) => {
    selectedCategory.value = params.name
  })
}

// 选中分类的明细
const categoryDetails = computed(() => {
  if (!selectedCategory.value || !billData.value?.data) return []
  
  return billData.value.data.filter(item => {
    if (item.excludeFromMonthly === false) return false
    
    const category = item.category || '未分类'
    if (category !== selectedCategory.value) return false
    
    const isIncome = item.income && parseFloat(item.income) > 0
    const isExpense = item.expense && parseFloat(item.expense) > 0
    
    if (transactionType.value === 'income') return isIncome
    if (transactionType.value === 'expense') return isExpense
    
    return false
  })
})

// 选择分类
const selectCategory = (row) => {
  selectedCategory.value = row.category
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

// 返回
const goBack = () => {
  router.push('/bill-analysis')
}

// 监听收支类型切换
watch(transactionType, async () => {
  selectedCategory.value = ''
  await nextTick()
  initPieChart()
})

// 监听窗口大小变化
const handleResize = () => {
  pieChartInstance?.resize()
}

onMounted(() => {
  loadBillData()
  nextTick(() => {
    initPieChart()
  })
  window.addEventListener('resize', handleResize)
})

// 清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  pieChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.category-analysis {
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

.type-toggle {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 500px;
  margin-bottom: 20px;
}

.stats-table {
  margin-top: 20px;
}

.stats-table :deep(.el-table__row) {
  cursor: pointer;
}

.stats-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.income-text {
  color: #67c23a;
  font-weight: 600;
}

.expense-text {
  color: #f56c6c;
  font-weight: 600;
}

.category-details {
  margin-top: 30px;
}

.details-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
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
