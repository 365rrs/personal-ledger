<template>
  <div class="bill-analysis">
    <!-- 账单选择列表 -->
    <el-card v-if="showBillList" class="bill-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><List /></el-icon>
          <span>选择要查看的账单</span>
        </div>
      </template>
      
      <div v-if="billList.length === 0" class="empty-state">
        <el-empty description="暂无可查看的账单数据">
          <el-button type="primary" @click="router.push('/bill-import')">去导入账单</el-button>
        </el-empty>
      </div>
      
      <el-table v-else :data="billList" style="width: 100%" @row-click="selectBill">
        <el-table-column prop="fileName" label="文件名" width="300"></el-table-column>
        <el-table-column prop="importTime" label="导入时间" width="200"></el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="100"></el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click.stop="selectBill(scope.row.id)">
              查看解析
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 账单信息展示 -->
    <el-card v-if="billData?.exportInfo" class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><InfoFilled /></el-icon>
            <span>账单信息</span>
          </div>
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
          <div class="header-left">
            <el-icon class="header-icon"><TrendCharts /></el-icon>
            <span>收支统计</span>
          </div>
        </div>
      </template>
      <el-row :gutter="20" class="summary-content">
        <el-col :xs="24" :sm="8">
          <div class="summary-item balance">
            <div class="summary-header">
              <span class="summary-label">结余</span>
              <el-tag size="small" type="info">{{ filteredSummary.totalCount }}笔</el-tag>
            </div>
            <div class="summary-value">{{ filteredSummary.balanceAmount }}</div>
            <div class="summary-detail">
              <div class="detail-row">
                <span class="detail-label">计入:</span>
                <span class="detail-value">{{ filteredSummary.balanceAmount }}</span>
              </div>
              <div class="detail-row exclude">
                <span class="detail-label">不计入:</span>
                <span class="detail-value">{{ filteredSummary.excludeBalanceAmount }} ({{ filteredSummary.excludeTotalCount }}笔)</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8">
          <div class="summary-item income">
            <div class="summary-header">
              <span class="summary-label">收入</span>
              <el-tag size="small" type="success">{{ filteredSummary.incomeCount }}笔</el-tag>
            </div>
            <div class="summary-value">{{ filteredSummary.incomeAmount }}</div>
            <div class="summary-detail">
              <div class="detail-row">
                <span class="detail-label">计入:</span>
                <span class="detail-value">{{ filteredSummary.incomeAmount }}</span>
              </div>
              <div class="detail-row exclude">
                <span class="detail-label">不计入:</span>
                <span class="detail-value">{{ filteredSummary.excludeIncomeAmount }} ({{ filteredSummary.excludeIncomeCount }}笔)</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8">
          <div class="summary-item expense">
            <div class="summary-header">
              <span class="summary-label">支出</span>
              <el-tag size="small" type="danger">{{ filteredSummary.expenseCount }}笔</el-tag>
            </div>
            <div class="summary-value">{{ filteredSummary.expenseAmount }}</div>
            <div class="summary-detail">
              <div class="detail-row">
                <span class="detail-label">计入:</span>
                <span class="detail-value">{{ filteredSummary.expenseAmount }}</span>
              </div>
              <div class="detail-row exclude">
                <span class="detail-label">不计入:</span>
                <span class="detail-value">{{ filteredSummary.excludeExpenseAmount }} ({{ filteredSummary.excludeExpenseCount }}笔)</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <div class="summary-note" v-if="filterForm.startDate || filterForm.endDate">
        <el-icon><InfoFilled /></el-icon> 当前统计基于筛选条件
      </div>
    </el-card>
    
    <!-- 操作工具栏 -->
    <el-card class="toolbar-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Operation /></el-icon>
            <span>数据操作</span>
          </div>
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
          <div class="header-left">
            <el-icon class="header-icon"><Filter /></el-icon>
            <span>筛选条件</span>
          </div>
        </div>
      </template>
      <el-form :model="filterForm" label-width="70px">
        <!-- 快捷日期 -->
        <el-row :gutter="10" class="quick-date-row">
          <el-col :span="24">
            <el-space wrap>
              <el-button @click="setQuickDate('today')">今天</el-button>
              <el-button @click="setQuickDate('thisMonth')">本月</el-button>
              <el-button @click="setQuickDate('lastMonth')">上月</el-button>
              <el-button @click="setQuickDate('last3Months')">近3月</el-button>
              <el-button @click="setQuickDate('thisYear')">今年</el-button>
            </el-space>
          </el-col>
        </el-row>
        
        <el-row :gutter="10">
          <el-col :xs="24" :sm="12" :md="10">
            <el-form-item label="日期">
              <el-date-picker
                v-model="filterForm.startDate"
                type="date"
                placeholder="开始"
                value-format="YYYY-MM-DD"
                style="width: 140px;"
              />
              <span style="margin: 0 8px;">-</span>
              <el-date-picker
                v-model="filterForm.endDate"
                type="date"
                placeholder="结束"
                value-format="YYYY-MM-DD"
                style="width: 140px;"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="4">
            <el-form-item label="收支">
              <el-select v-model="filterForm.transactionType" placeholder="全部" clearable style="width: 100%;">
                <el-option label="收入" value="收入" />
                <el-option label="支出" value="支出" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6" :md="5">
            <el-form-item label="渠道">
              <el-select v-model="filterForm.paymentChannel" placeholder="全部" clearable style="width: 100%;">
                <el-option label="微信" value="微信" />
                <el-option label="支付宝" value="支付宝" />
                <el-option label="京东" value="京东支付" />
                <el-option label="未知" value="__UNKNOWN__" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="5">
            <el-form-item label="类型">
              <el-select v-model="filterForm.tradeType" placeholder="全部" clearable style="width: 100%;">
                <el-option v-for="type in tradeTypeOptions" :key="type" :label="type" :value="type" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :xs="12" :sm="8" :md="6">
            <el-form-item label="金额">
              <el-input v-model="filterForm.minAmount" placeholder="最小" style="width: 90px;" />
              <span style="margin: 0 8px;">-</span>
              <el-input v-model="filterForm.maxAmount" placeholder="最大" style="width: 90px;" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="6">
            <el-form-item label="关键词">
              <el-input v-model="filterForm.keyword" placeholder="搜索" clearable style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="8" :md="6">
            <el-form-item label="统计">
              <el-select v-model="filterForm.includeInSummary" placeholder="全部" clearable style="width: 100%;">
                <el-option label="计入" :value="true" />
                <el-option label="不计入" :value="false" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="6">
            <el-form-item label=" ">
              <el-space>
                <el-button type="primary" @click="applyFilter" :icon="Search">筛选</el-button>
                <el-button @click="resetFilter" :icon="RefreshRight">重置</el-button>
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
          <div class="header-left">
            <el-icon class="header-icon"><List /></el-icon>
            <span>交易记录</span>
            <div class="record-count">
              共 {{ billData?.data?.length || 0 }} 条，显示 {{ paginatedData.length }} 条
            </div>
          </div>
          <el-popover placement="bottom" :width="200" trigger="click">
            <template #reference>
              <el-button size="small" :icon="Setting">列设置</el-button>
            </template>
            <div class="column-config">
              <el-checkbox v-for="col in columnConfig" :key="col.prop" v-model="col.visible" style="display: block; margin: 8px 0;">
                {{ col.label }}
              </el-checkbox>
            </div>
          </el-popover>
        </div>
      </template>
      
      <el-table 
        :data="paginatedData" 
        style="width: 100%" 
        max-height="600"
        class="transaction-table"
        :default-sort="{prop: 'formattedTradeDate', order: 'ascending'}"
      >
        <el-table-column v-if="getColumnVisible('formattedTradeDate')" prop="formattedTradeDate" label="交易日期" min-width="110" sortable></el-table-column>
        <el-table-column v-if="getColumnVisible('tradeTime')" prop="tradeTime" label="交易时间" min-width="100"></el-table-column>
        <el-table-column v-if="getColumnVisible('income')" prop="income" label="收入" min-width="90" sortable></el-table-column>
        <el-table-column v-if="getColumnVisible('expense')" prop="expense" label="支出" min-width="90" sortable></el-table-column>
        <el-table-column v-if="getColumnVisible('tradeType')" prop="tradeType" label="交易类型" min-width="140">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.tradeType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="getColumnVisible('paymentChannel')" prop="paymentChannel" label="支付渠道" min-width="100">
          <template #default="scope">
            <span>{{ scope.row.paymentChannel || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="getColumnVisible('category')" prop="category" label="分类" min-width="90">
          <template #default="scope">
            <span>{{ scope.row.category || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="getColumnVisible('remark')" prop="remark" label="交易备注" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column v-if="getColumnVisible('userRemark')" prop="userRemark" label="用户备注" min-width="140">
          <template #default="scope">
            <span>{{ scope.row.userRemark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="getColumnVisible('excludeFromMonthly')" label="计入统计" min-width="90" align="center">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.excludeFromMonthly ? 'success' : 'info'">
              {{ scope.row.excludeFromMonthly ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-space :size="5">
              <el-button type="primary" size="small" @click="viewRecord(scope.row)" :icon="View">查看</el-button>
              <el-button type="warning" size="small" @click="editRecord(scope.row)" :icon="Edit">编辑</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100, 200]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
          :prev-text="'上一页'"
          :next-text="'下一页'"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 数据不存在提示 -->
    <el-card v-if="!billData && !showBillList" class="empty-card" shadow="hover">
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  InfoFilled, Operation, Filter, List, TrendCharts, ArrowLeft,
  Refresh, Download, Search, RefreshRight, View, Edit, Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const billData = ref(null)
const drawerVisible = ref(false)
const drawerMode = ref('view')
const currentRecord = ref(null)

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 20
})

// 列显示配置
const columnConfig = ref([
  { prop: 'formattedTradeDate', label: '交易日期', visible: true },
  { prop: 'tradeTime', label: '交易时间', visible: true },
  { prop: 'income', label: '收入', visible: true },
  { prop: 'expense', label: '支出', visible: true },
  { prop: 'tradeType', label: '交易类型', visible: true },
  { prop: 'paymentChannel', label: '支付渠道', visible: true },
  { prop: 'category', label: '分类', visible: false },
  { prop: 'remark', label: '交易备注', visible: true },
  { prop: 'userRemark', label: '用户备注', visible: true },
  { prop: 'excludeFromMonthly', label: '计入统计', visible: true }
])

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

// 账单列表
const billList = ref([])
const showBillList = ref(false)

// 从localStorage加载账单数据
const loadBillData = () => {
  const importId = route.params.id
  if (!importId) {
    // 没有ID参数，自动加载最新的账单数据
    loadLatestBillData()
    return
  }
  
  const data = localStorage.getItem(`billData_${importId}`)
  if (data) {
    billData.value = JSON.parse(data)
    // 处理数据格式
    if (billData.value.data) {
      billData.value.data = processTransactionData(billData.value.data)
    }
    showBillList.value = false
  } else {
    ElMessage.error('未找到账单数据')
    router.push('/bill-import')
  }
}

// 加载最新的账单数据
const loadLatestBillData = () => {
  const history = localStorage.getItem('billImportHistory')
  if (history) {
    const importHistory = JSON.parse(history)
    const successfulImports = importHistory.filter(item => item.status === 'success')
    
    if (successfulImports.length > 0) {
      // 加载最新的成功导入的账单
      const latestImport = successfulImports[0]
      const data = localStorage.getItem(`billData_${latestImport.id}`)
      if (data) {
        billData.value = JSON.parse(data)
        if (billData.value.data) {
          billData.value.data = processTransactionData(billData.value.data)
        }
        showBillList.value = false
        return
      }
    }
  }
  
  // 没有找到任何账单数据
  showBillList.value = true
  loadBillList()
}

// 加载账单列表
const loadBillList = () => {
  const history = localStorage.getItem('billImportHistory')
  if (history) {
    const importHistory = JSON.parse(history)
    billList.value = importHistory.filter(item => item.status === 'success')
  }
}

// 选择账单
const selectBill = (billId) => {
  router.push(`/bill-analysis/${billId}`)
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

// 快捷日期设置
const setQuickDate = (type) => {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  
  switch(type) {
    case 'today':
      filterForm.startDate = filterForm.endDate = formatDate(today)
      break
    case 'thisMonth':
      filterForm.startDate = formatDate(new Date(year, month, 1))
      filterForm.endDate = formatDate(today)
      break
    case 'lastMonth':
      filterForm.startDate = formatDate(new Date(year, month - 1, 1))
      filterForm.endDate = formatDate(new Date(year, month, 0))
      break
    case 'last3Months':
      filterForm.startDate = formatDate(new Date(year, month - 2, 1))
      filterForm.endDate = formatDate(today)
      break
    case 'thisYear':
      filterForm.startDate = formatDate(new Date(year, 0, 1))
      filterForm.endDate = formatDate(today)
      break
  }
}

// 获取列显示状态
const getColumnVisible = (prop) => {
  const col = columnConfig.value.find(c => c.prop === prop)
  return col ? col.visible : true
}



// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
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

// 分页后的数据
const paginatedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredData.value.slice(start, end)
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

// 监听路由参数变化
watch(() => route.params.id, () => {
  loadBillData()
})
</script>

<style scoped>
.bill-analysis {
  padding: 0;
}

.info-card, .toolbar-card, .filter-card, .table-card, .summary-card, .empty-card, .bill-list-card {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .info-card, .toolbar-card, .filter-card, .table-card, .summary-card {
    margin-bottom: 15px;
  }
}

.empty-state {
  padding: 40px 0;
}

.bill-list-card :deep(.el-table__row) {
  cursor: pointer;
}

.bill-list-card :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  flex-wrap: wrap;
  gap: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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
  padding: 20px;
  border-radius: 8px;
  min-height: 140px;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s;
}

.summary-item:hover {
  transform: translateY(-2px);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.summary-value {
  font-size: 28px;
  font-weight: bold;
  margin: 10px 0;
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

.summary-detail {
  font-size: 12px;
  color: #909399;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin: 4px 0;
}

.detail-row.exclude {
  color: #c0c4cc;
}

.detail-label {
  font-weight: 500;
}

.detail-value {
  font-weight: 600;
}

.summary-note {
  margin-top: 15px;
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.quick-date-row {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .summary-item {
    min-height: 120px;
    padding: 15px;
    margin-bottom: 10px;
  }
  
  .summary-value {
    font-size: 22px;
  }
  
  .pagination-container {
    overflow-x: auto;
  }
  
  .pagination-container :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
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