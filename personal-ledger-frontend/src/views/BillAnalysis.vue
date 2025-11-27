<template>
  <div class="bill-analysis">
    <!-- 账单信息展示 -->
    <el-card v-if="billData?.exportInfo" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><InfoFilled /></el-icon>
          <span>账单信息</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="goBack" :icon="ArrowLeft">
              返回导入
            </el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="2" size="small" border>
        <el-descriptions-item label="文件名">{{ billData.fileName }}</el-descriptions-item>
        <el-descriptions-item label="导入时间">{{ billData.importTime }}</el-descriptions-item>
        <el-descriptions-item label="导出时间">{{ billData.exportInfo.exportTime }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ billData.exportInfo.account }}</el-descriptions-item>
        <el-descriptions-item label="币种">{{ billData.exportInfo.currency }}</el-descriptions-item>
        <el-descriptions-item label="起始日期">{{ billData.exportInfo.startDate }}</el-descriptions-item>
        <el-descriptions-item label="终止日期">{{ billData.exportInfo.endDate }}</el-descriptions-item>
        <el-descriptions-item label="过滤设置">{{ billData.exportInfo.filterSetting }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 收支统计信息 -->
    <el-card v-if="billData?.summary" class="summary-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><TrendCharts /></el-icon>
          <span>收支统计</span>
        </div>
      </template>
      <el-row :gutter="20" class="summary-content">
        <el-col :span="8">
          <div class="summary-item balance">
            <div class="summary-label">结余</div>
            <div class="summary-value">{{ filteredSummary.balanceAmount }}</div>
            <div class="summary-formula">{{ filteredSummary.incomeAmount }} - {{ filteredSummary.expenseAmount }}</div>
            <div class="summary-count">{{ filteredSummary.totalCount }} 笔</div>
            <div class="summary-exclude">
              (不计入: {{ filteredSummary.excludeBalanceAmount }}, {{ filteredSummary.excludeTotalCount }} 笔)
            </div>
            <div class="summary-original" v-if="billData?.summary?.incomeAmount && billData?.summary?.expenseAmount">
              原始数据: {{ filteredSummary.originalBalanceAmount }}, {{ filteredSummary.originalTotalCount }} 笔
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="summary-item income">
            <div class="summary-label">收入</div>
            <div class="summary-value">{{ filteredSummary.incomeAmount }}</div>
            <div class="summary-formula summary-placeholder">&nbsp;</div>
            <div class="summary-count">{{ filteredSummary.incomeCount }} 笔</div>
            <div class="summary-exclude">
              (不计入: {{ filteredSummary.excludeIncomeAmount }}, {{ filteredSummary.excludeIncomeCount }} 笔)
            </div>
            <div class="summary-original" v-if="billData?.summary?.incomeAmount">
              原始数据: {{ billData.summary.incomeAmount }}, {{ billData.summary.incomeCount }} 笔
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="summary-item expense">
            <div class="summary-label">支出</div>
            <div class="summary-value">{{ filteredSummary.expenseAmount }}</div>
            <div class="summary-formula summary-placeholder">&nbsp;</div>
            <div class="summary-count">{{ filteredSummary.expenseCount }} 笔</div>
            <div class="summary-exclude">
              (不计入: {{ filteredSummary.excludeExpenseAmount }}, {{ filteredSummary.excludeExpenseCount }} 笔)
            </div>
            <div class="summary-original" v-if="billData?.summary?.expenseAmount">
              原始数据: {{ billData.summary.expenseAmount }}, {{ billData.summary.expenseCount }} 笔
            </div>
          </div>
        </el-col>
      </el-row>
      <div class="summary-note" v-if="filterForm.startDate || filterForm.endDate">
        * 以上统计基于当前筛选条件
      </div>
    </el-card>
    
    <!-- 操作工具栏 -->
    <el-card class="toolbar-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Operation /></el-icon>
          <span>数据操作</span>
        </div>
      </template>
      <el-space>
        <el-button 
          type="warning" 
          @click="cleanData"
          :disabled="!billData?.data || billData.data.length === 0"
          :icon="Refresh"
        >
          数据清洗加工
        </el-button>
        <el-button 
          type="success" 
          @click="exportData"
          :disabled="!billData?.data || billData.data.length === 0"
          :icon="Download"
        >
          导出数据
        </el-button>
      </el-space>
    </el-card>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
      </template>
      <el-form :model="filterForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易日期">
              <el-date-picker
                v-model="filterForm.startDate"
                type="date"
                placeholder="开始日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 140px;"
              />
              <span style="margin: 0 10px;">-</span>
              <el-date-picker
                v-model="filterForm.endDate"
                type="date"
                placeholder="结束日期"
                value-format="YYYY-MM-DD"
                format="YYYY-MM-DD"
                style="width: 140px;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收支类型">
              <el-select v-model="filterForm.transactionType" placeholder="请选择" clearable style="width: 120px;">
                <el-option label="收入" value="收入" />
                <el-option label="支出" value="支出" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="交易类型">
              <el-select v-model="filterForm.tradeType" placeholder="请选择" clearable style="width: 300px;">
                <el-option v-for="type in tradeTypeOptions" :key="type" :label="type" :value="type" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付渠道">
              <el-select v-model="filterForm.paymentChannel" placeholder="请选择" clearable style="width: 120px;">
                <el-option label="微信" value="微信" />
                <el-option label="支付宝" value="支付宝" />
                <el-option label="京东支付" value="京东支付" />
                <el-option label="未知" value="__UNKNOWN__" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额范围">
              <el-input v-model="filterForm.minAmount" placeholder="最小金额" style="width: 100px;" />
              <span style="margin: 0 10px;">-</span>
              <el-input v-model="filterForm.maxAmount" placeholder="最大金额" style="width: 100px;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关键词">
              <el-input v-model="filterForm.keyword" placeholder="交易备注关键词" style="width: 150px;" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计入统计">
              <el-select v-model="filterForm.includeInSummary" placeholder="请选择" clearable style="width: 120px;">
                <el-option label="计入" :value="true" />
                <el-option label="不计入" :value="false" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item>
              <el-space>
                <el-button type="primary" @click="applyFilter" :icon="Search" size="small">筛选</el-button>
                <el-button @click="resetFilter" :icon="RefreshRight" size="small">重置</el-button>
              </el-space>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 交易记录表格 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><List /></el-icon>
          <span>交易记录</span>
          <div class="record-count">
            共 {{ billData?.data?.length || 0 }} 条记录，筛选后显示 {{ filteredData.length }} 条记录
          </div>
        </div>
      </template>
      
      <el-table 
        :data="filteredData" 
        style="width: 100%" 
        max-height="600" 
        class="transaction-table"
        :default-sort="{prop: 'formattedTradeDate', order: 'ascending'}"
      >
        <el-table-column prop="formattedTradeDate" label="交易日期" width="120" sortable></el-table-column>
        <el-table-column prop="tradeTime" label="交易时间" width="120"></el-table-column>
        <el-table-column prop="income" label="收入" width="100" sortable></el-table-column>
        <el-table-column prop="expense" label="支出" width="100" sortable></el-table-column>
        <el-table-column prop="tradeType" label="交易类型" width="150">
          <template #default="scope">
            <el-tag>{{ scope.row.tradeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentChannel" label="支付渠道" width="100">
          <template #default="scope">
            <span>{{ scope.row.paymentChannel || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <span>{{ scope.row.category || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="交易备注" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userRemark" label="用户备注" width="150">
          <template #default="scope">
            <span>{{ scope.row.userRemark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="是否计入本月收支" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.excludeFromMonthly ? 'success' : 'info'">
              {{ scope.row.excludeFromMonthly ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button type="primary" size="small" @click="viewRecord(scope.row)" :icon="View">
                查看
              </el-button>
              <el-button type="warning" size="small" @click="editRecord(scope.row)" :icon="Edit">
                编辑
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 数据不存在提示 -->
    <el-card v-if="!billData" class="empty-card" shadow="hover">
      <el-empty description="未找到账单数据">
        <el-button type="primary" @click="goBack">返回导入页面</el-button>
      </el-empty>
    </el-card>
    
    <!-- 交易记录查看/编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerMode === 'view' ? '查看交易记录' : '编辑交易记录'"
      direction="rtl"
      size="500px"
    >
      <div v-if="currentRecord" class="record-detail">
        <el-form :model="currentRecord" label-width="120px" :disabled="drawerMode === 'view'">
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
          <el-form-item label="余额">
            <el-input v-model="currentRecord.balance" readonly />
          </el-form-item>
          <el-form-item label="交易类型">
            <el-input v-model="currentRecord.tradeType" readonly />
          </el-form-item>
          <el-form-item label="交易备注">
            <el-input v-model="currentRecord.remark" readonly />
          </el-form-item>
          <el-form-item label="支付渠道">
            <el-input v-model="currentRecord.paymentChannel" :placeholder="drawerMode === 'edit' ? '请输入支付渠道' : ''" />
          </el-form-item>
          <el-form-item label="收支类型">
            <el-input v-model="currentRecord.transactionType" :placeholder="drawerMode === 'edit' ? '请输入收支类型' : ''" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="currentRecord.category" :placeholder="drawerMode === 'edit' ? '请输入分类' : ''" />
          </el-form-item>
          <el-form-item label="用户备注">
            <el-input 
              v-model="currentRecord.userRemark" 
              type="textarea" 
              :rows="3" 
              :placeholder="drawerMode === 'edit' ? '请输入用户备注' : ''"
            />
          </el-form-item>
          <el-form-item label="计入本月收支">
            <el-switch
              v-model="currentRecord.excludeFromMonthly"
              active-text="是"
              inactive-text="否"
              :disabled="drawerMode === 'view'"
            />
          </el-form-item>
        </el-form>
        
        <div v-if="drawerMode === 'edit'" class="drawer-footer">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  InfoFilled, Operation, Filter, List, TrendCharts, ArrowLeft,
  Refresh, Download, Search, RefreshRight, View, Edit
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const billData = ref(null)
const drawerVisible = ref(false)
const drawerMode = ref('view') // 'view' 或 'edit'
const currentRecord = ref(null)

// 筛选表单
const filterForm = reactive({
  startDate: '',
  endDate: '',
  transactionType: '',
  tradeType: '',
  paymentChannel: '',
  minAmount: '',
  maxAmount: '',
  keyword: '',
  includeInSummary: ''
})

// 交易类型选项
const tradeTypeOptions = computed(() => {
  if (!billData.value?.data) return []
  const types = [...new Set(billData.value.data.map(item => item.tradeType).filter(Boolean))]
  return types.sort()
})

// 从localStorage加载账单数据
const loadBillData = () => {
  const importId = route.params.id
  if (!importId) {
    ElMessage.error('缺少账单ID')
    router.push('/bill-import')
    return
  }
  
  const data = localStorage.getItem(`billData_${importId}`)
  if (data) {
    billData.value = JSON.parse(data)
    // 处理数据格式
    if (billData.value.data) {
      billData.value.data = processTransactionData(billData.value.data)
    }
  } else {
    ElMessage.error('未找到账单数据')
    router.push('/bill-import')
  }
}

// 为交易记录添加格式化日期属性
const processTransactionData = (data) => {
  if (!data || !Array.isArray(data)) return data
  
  return data.map(item => ({
    ...item,
    formattedTradeDate: item.formattedTradeDate || item.tradeDate,
    excludeFromMonthly: item.excludeFromMonthly !== undefined ? item.excludeFromMonthly : false,
    userRemark: item.userRemark || ''
  }))
}

// 筛选后的数据
const filteredData = computed(() => {
  if (!billData.value?.data) {
    return []
  }
  
  let filtered = billData.value.data
  
  // 日期筛选
  if (filterForm.startDate || filterForm.endDate) {
    filtered = filtered.filter(item => {
      if (!item.formattedTradeDate) return false
      
      const tradeDate = new Date(item.formattedTradeDate)
      
      if (filterForm.startDate && tradeDate < new Date(filterForm.startDate)) {
        return false
      }
      
      if (filterForm.endDate && tradeDate > new Date(filterForm.endDate)) {
        return false
      }
      
      return true
    })
  }
  
  // 收支类型筛选
  if (filterForm.transactionType) {
    filtered = filtered.filter(item => {
      if (filterForm.transactionType === '收入') {
        return item.income && parseFloat(item.income) > 0
      } else if (filterForm.transactionType === '支出') {
        return item.expense && parseFloat(item.expense) > 0
      }
      return true
    })
  }
  
  // 交易类型筛选
  if (filterForm.tradeType) {
    filtered = filtered.filter(item => item.tradeType === filterForm.tradeType)
  }
  
  // 支付渠道筛选
  if (filterForm.paymentChannel) {
    if (filterForm.paymentChannel === '__UNKNOWN__') {
      // 筛选未知（空值）的支付渠道
      filtered = filtered.filter(item => !item.paymentChannel || item.paymentChannel.trim() === '')
    } else {
      // 筛选具体的支付渠道
      filtered = filtered.filter(item => item.paymentChannel === filterForm.paymentChannel)
    }
  }
  
  // 金额范围筛选
  if (filterForm.minAmount || filterForm.maxAmount) {
    filtered = filtered.filter(item => {
      const amount = parseFloat(item.income || item.expense || 0)
      if (filterForm.minAmount && amount < parseFloat(filterForm.minAmount)) {
        return false
      }
      if (filterForm.maxAmount && amount > parseFloat(filterForm.maxAmount)) {
        return false
      }
      return true
    })
  }
  
  // 关键词筛选
  if (filterForm.keyword) {
    filtered = filtered.filter(item => {
      const keyword = filterForm.keyword.toLowerCase()
      return (item.remark && item.remark.toLowerCase().includes(keyword)) ||
             (item.userRemark && item.userRemark.toLowerCase().includes(keyword))
    })
  }
  
  // 计入统计筛选
  if (filterForm.includeInSummary !== '') {
    filtered = filtered.filter(item => item.excludeFromMonthly === filterForm.includeInSummary)
  }
  
  return filtered
})

// 计算筛选后的统计信息
const filteredSummary = computed(() => {
  if (!filteredData.value || filteredData.value.length === 0) {
    return {
      incomeCount: 0,
      incomeAmount: '0.00元',
      expenseCount: 0,
      expenseAmount: '0.00元',
      excludeIncomeCount: 0,
      excludeIncomeAmount: '0.00元',
      excludeExpenseCount: 0,
      excludeExpenseAmount: '0.00元'
    }
  }
  
  let incomeCount = 0
  let expenseCount = 0
  let incomeAmount = 0
  let expenseAmount = 0
  
  let excludeIncomeCount = 0
  let excludeExpenseCount = 0
  let excludeIncomeAmount = 0
  let excludeExpenseAmount = 0
  
  filteredData.value.forEach(item => {
    const isExcluded = item.excludeFromMonthly === false
    
    if (item.income && item.income > 0) {
      if (isExcluded) {
        excludeIncomeCount++
        excludeIncomeAmount += parseFloat(item.income)
      } else {
        incomeCount++
        incomeAmount += parseFloat(item.income)
      }
    }
    
    if (item.expense && item.expense > 0) {
      if (isExcluded) {
        excludeExpenseCount++
        excludeExpenseAmount += parseFloat(item.expense)
      } else {
        expenseCount++
        expenseAmount += parseFloat(item.expense)
      }
    }
  })
  
  const balanceAmount = incomeAmount - expenseAmount
  const excludeBalanceAmount = excludeIncomeAmount - excludeExpenseAmount
  const totalCount = incomeCount + expenseCount
  const excludeTotalCount = excludeIncomeCount + excludeExpenseCount
  
  // 计算原始结余
  let originalBalanceAmount = '0.00元'
  let originalTotalCount = 0
  if (billData.value?.summary) {
    const originalIncome = parseFloat(billData.value.summary.incomeAmount?.replace('元', '') || '0')
    const originalExpense = parseFloat(billData.value.summary.expenseAmount?.replace('元', '') || '0')
    const originalBalance = originalIncome - originalExpense
    originalBalanceAmount = originalBalance.toFixed(2) + '元'
    originalTotalCount = (billData.value.summary.incomeCount || 0) + (billData.value.summary.expenseCount || 0)
  }
  
  return {
    incomeCount,
    incomeAmount: incomeAmount.toFixed(2) + '元',
    expenseCount,
    expenseAmount: expenseAmount.toFixed(2) + '元',
    excludeIncomeCount,
    excludeIncomeAmount: excludeIncomeAmount.toFixed(2) + '元',
    excludeExpenseCount,
    excludeExpenseAmount: excludeExpenseAmount.toFixed(2) + '元',
    balanceAmount: balanceAmount.toFixed(2) + '元',
    excludeBalanceAmount: excludeBalanceAmount.toFixed(2) + '元',
    totalCount,
    excludeTotalCount,
    originalBalanceAmount,
    originalTotalCount
  }
})

// 保存数据到localStorage
const saveBillData = () => {
  if (billData.value) {
    const importId = route.params.id
    localStorage.setItem(`billData_${importId}`, JSON.stringify(billData.value))
  }
}

// 数据清洗功能
const cleanData = async () => {
  if (!billData.value?.data || billData.value.data.length === 0) {
    ElMessage.warning('没有可清洗的数据')
    return
  }

  try {
    const response = await axios.post('/api/cmb/clean-records', billData.value.data)
    billData.value.data = processTransactionData(response.data)
    saveBillData()
    ElMessage.success('数据清洗完成')
  } catch (error) {
    ElMessage.error('数据清洗失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 导出数据功能
const exportData = async () => {
  try {
    const exportPayload = {
      records: filteredData.value,
      exportInfo: billData.value.exportInfo,
      summaryInfo: billData.value.summary
    }
    
    const response = await axios.post('/api/cmb/export', exportPayload, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `招商银行账单_${new Date().toISOString().slice(0, 10)}_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    ElMessage.error('数据导出失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// 应用筛选
const applyFilter = () => {
  ElMessage.success('筛选条件已应用')
}

// 重置筛选
const resetFilter = () => {
  filterForm.startDate = ''
  filterForm.endDate = ''
  filterForm.transactionType = ''
  filterForm.tradeType = ''
  filterForm.paymentChannel = ''
  filterForm.minAmount = ''
  filterForm.maxAmount = ''
  filterForm.keyword = ''
  filterForm.includeInSummary = ''
}



// 查看记录
const viewRecord = (record) => {
  currentRecord.value = { ...record }
  drawerMode.value = 'view'
  drawerVisible.value = true
}

// 编辑记录
const editRecord = (record) => {
  currentRecord.value = { ...record }
  drawerMode.value = 'edit'
  drawerVisible.value = true
}

// 保存记录
const saveRecord = () => {
  // 找到原始记录并更新
  const index = billData.value.data.findIndex(item => 
    item.formattedTradeDate === currentRecord.value.formattedTradeDate &&
    item.tradeTime === currentRecord.value.tradeTime &&
    item.income === currentRecord.value.income &&
    item.expense === currentRecord.value.expense
  )
  
  if (index !== -1) {
    billData.value.data[index] = { ...currentRecord.value }
    saveBillData()
    ElMessage.success('保存成功')
    drawerVisible.value = false
  }
}

// 返回导入页面
const goBack = () => {
  router.push('/bill-import')
}

onMounted(() => {
  loadBillData()
})
</script>

<style scoped>
.bill-analysis {
  padding: 0;
}

.info-card, .toolbar-card, .filter-card, .table-card, .summary-card, .empty-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  font-size: 18px;
  margin-right: 8px;
}

.record-count {
  font-size: 14px;
  color: #606266;
  font-weight: normal;
}

.transaction-table {
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.transaction-table :deep(.el-table__header) {
  background-color: #f8f9fa;
}

.summary-content {
  margin-bottom: 10px;
}

.summary-item {
  text-align: center;
  padding: 20px 0;
  border-radius: 4px;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.summary-item.income {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
}

.summary-item.expense {
  background-color: #fef0f0;
  border: 1px solid #fbc4c4;
}

.summary-item.balance {
  background-color: #f0f9ff;
  border: 1px solid #bfdbfe;
}

.summary-label {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.summary-item.income .summary-value {
  color: #67c23a;
}

.summary-item.expense .summary-value {
  color: #f56c6c;
}

.summary-item.balance .summary-value {
  color: #409eff;
}

.summary-count {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.summary-exclude {
  font-size: 12px;
  color: #909399;
}

.summary-original {
  font-size: 11px;
  color: #606266;
  margin-top: 3px;
  font-style: italic;
}

.summary-formula {
  font-size: 12px;
  color: #909399;
  margin-bottom: 3px;
  font-style: italic;
}

.summary-placeholder {
  visibility: hidden;
}

.summary-note {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
  text-align: right;
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