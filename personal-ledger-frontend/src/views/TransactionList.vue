<template>
  <div class="transaction-list">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>交易记录</span>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-form label-width="80px" class="filter-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="快捷日期">
              <el-space wrap>
                <el-button @click="setQuickDate('today')">今天</el-button>
                <el-button @click="setQuickDate('yesterday')">昨天</el-button>
                <el-button @click="setQuickDate('thisWeek')">本周</el-button>
                <el-button @click="setQuickDate('thisMonth')">本月</el-button>
                <el-button @click="setQuickDate('lastMonth')">上月</el-button>
                <el-button @click="setQuickDate('last3Months')">近3月</el-button>
                <el-button @click="setQuickDate('thisYear')">今年</el-button>
                <el-button @click="setQuickDate('lastYear')">去年</el-button>
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
            <el-form-item label="收支类型">
              <el-select v-model="filter.transactionType" clearable placeholder="全部">
                <el-option label="收入" value="income" />
                <el-option label="支出" value="expense" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="支付渠道">
              <el-select v-model="filter.paymentChannel" clearable filterable placeholder="全部">
                <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="交易类型">
              <el-input v-model="filter.tradeType" placeholder="交易类型" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分类">
              <el-select v-model="filter.category" clearable filterable placeholder="全部">
                <el-option label="未分类" value="__UNCATEGORIZED__" />
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额范围">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-input v-model="filter.minAmount" placeholder="最小金额" clearable style="flex: 1" />
                <span>至</span>
                <el-input v-model="filter.maxAmount" placeholder="最大金额" clearable style="flex: 1" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="filter.keyword" placeholder="搜索备注" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="计入收支">
              <el-select v-model="filter.excludeFromStats" clearable placeholder="全部">
                <el-option label="计入" :value="false" />
                <el-option label="不计入" :value="true" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label=" ">
              <el-button type="primary" @click="loadData" :icon="Search">查询</el-button>
              <el-button @click="resetFilter" :icon="RefreshRight">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div style="margin-bottom: 16px;">
        <el-space>
          <el-button type="warning" @click="cleanData" :loading="cleaning" :icon="Refresh">清洗数据</el-button>
          <el-button type="success" @click="exportData" :loading="exporting" :icon="Download">数据导出</el-button>
          <el-button type="danger" @click="batchDelete" :disabled="selectedRows.length === 0">批量删除 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditNote" :disabled="selectedRows.length === 0">批量编辑备注 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditCategory" :disabled="selectedRows.length === 0">批量分类 ({{ selectedRows.length }})</el-button>
        </el-space>
      </div>

      <div class="summary-box" style="margin-bottom: 16px;">
        <div class="summary-row">
          <div class="summary-item balance">
            <div class="summary-label">结余</div>
            <div class="summary-value">{{ summary.balance }}</div>
          </div>
          <div class="summary-operator">=</div>
          <div class="summary-item income">
            <div class="summary-label">收入</div>
            <div class="summary-value">{{ summary.income }}</div>
          </div>
          <div class="summary-operator">-</div>
          <div class="summary-item expense">
            <div class="summary-label">支出</div>
            <div class="summary-value">{{ summary.expense }}</div>
          </div>
        </div>
      </div>

      <el-table :data="transactions" v-loading="loading" @selection-change="handleSelectionChange" @sort-change="handleSortChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="transactionDate" label="交易日期" width="120" sortable="custom" />
        <el-table-column prop="transactionTime" label="交易时间" width="100" />
        <el-table-column prop="income" label="收入" width="100" sortable="custom" />
        <el-table-column prop="expense" label="支出" width="100" sortable="custom" />
        <el-table-column prop="transactionType" label="交易类型" width="120" show-overflow-tooltip />
        <el-table-column prop="paymentChannel" label="支付渠道" width="100" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="description" label="交易备注" show-overflow-tooltip />
        <el-table-column prop="userNote" label="用户备注" width="150" show-overflow-tooltip />
        <el-table-column label="计入收支" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.excludeFromStats ? 'info' : 'success'">
              {{ row.excludeFromStats ? '否' : '是' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewRecord(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editRecord(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <el-dialog v-model="batchCategoryDialogVisible" title="批量分类" width="400px">
      <el-form label-width="80px">
        <el-form-item label="选择分类">
          <el-select v-model="batchCategoryValue" placeholder="请选择分类" filterable style="width: 100%">
            <el-option label="未分类" value="" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchCategory">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="drawerMode === 'view' ? '查看交易' : '编辑交易'" size="500px">
      <el-form :model="currentRecord" label-width="100px" :disabled="drawerMode === 'view'">
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
        <el-form-item label="交易备注">
          <el-input v-model="currentRecord.description" type="textarea" readonly />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="currentRecord.category" filterable>
            <el-option label="未分类" value="" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="currentRecord.paymentChannel" filterable>
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户备注">
          <el-input v-model="currentRecord.userNote" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="计入统计">
          <el-switch v-model="currentRecord.excludeFromStats" :active-value="false" :inactive-value="true" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>
      <template #footer v-if="drawerMode === 'edit'">
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Search, RefreshRight } from '@element-plus/icons-vue'

const route = useRoute()

const loading = ref(false)
const cleaning = ref(false)
const exporting = ref(false)
const transactions = ref([])
const selectedRows = ref([])
const categories = ref([])
const channels = ref([])
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
const filter = reactive({ 
  startDate: thisMonth.startDate,
  endDate: thisMonth.endDate,
  category: '', 
  transactionType: '', 
  paymentChannel: '',
  tradeType: '',
  minAmount: '',
  maxAmount: '',
  keyword: '',
  excludeFromStats: ''
})
const sortField = ref('')
const sortOrder = ref('')
const summary = ref({ income: '0.00', expense: '0.00', balance: '0.00' })
const page = reactive({ current: 1, size: 20, total: 0 })
const drawerVisible = ref(false)
const drawerMode = ref('view')
const currentRecord = ref({})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: page.current,
      size: page.size,
      startDate: filter.startDate,
      endDate: filter.endDate,
      paymentChannel: filter.paymentChannel,
      transactionType: filter.tradeType,
      keyword: filter.keyword,
      minAmount: filter.minAmount,
      maxAmount: filter.maxAmount,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      firstImportId: route.query.importId
    }
    
    // 分类参数
    if (filter.category) {
      params.category = filter.category === '__UNCATEGORIZED__' ? '' : filter.category
    }
    
    // 收支类型参数
    if (filter.transactionType) {
      params.incomeOrExpense = filter.transactionType
    }
    
    // 计入收支参数
    if (filter.excludeFromStats !== '') {
      params.excludeFromStats = filter.excludeFromStats
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    transactions.value = res.data.data.records
    page.total = res.data.data.total
    
    // 加载汇总数据
    loadSummary()
  } catch (error) {
    ElMessage.error('加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadSummary = async () => {
  try {
    const params = {
      startDate: filter.startDate,
      endDate: filter.endDate,
      paymentChannel: filter.paymentChannel,
      transactionType: filter.tradeType,
      keyword: filter.keyword,
      minAmount: filter.minAmount,
      maxAmount: filter.maxAmount,
      firstImportId: route.query.importId
    }
    
    if (filter.category) {
      params.category = filter.category === '__UNCATEGORIZED__' ? '' : filter.category
    }
    if (filter.transactionType) {
      params.incomeOrExpense = filter.transactionType
    }
    if (filter.excludeFromStats !== '') {
      params.excludeFromStats = filter.excludeFromStats
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/summary', { params })
    summary.value = res.data.data
  } catch (error) {
    console.error('加载汇总失败', error)
  }
}

const handleSortChange = ({ prop, order }) => {
  sortField.value = prop || ''
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadData()
}

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
      filter.startDate = filter.endDate = formatDate(today)
      break
    case 'yesterday':
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)
      filter.startDate = filter.endDate = formatDate(yesterday)
      break
    case 'thisWeek':
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay())
      filter.startDate = formatDate(weekStart)
      filter.endDate = formatDate(today)
      break
    case 'thisMonth':
      filter.startDate = formatDate(new Date(today.getFullYear(), today.getMonth(), 1))
      filter.endDate = formatDate(today)
      break
    case 'lastMonth':
      filter.startDate = formatDate(new Date(today.getFullYear(), today.getMonth() - 1, 1))
      filter.endDate = formatDate(new Date(today.getFullYear(), today.getMonth(), 0))
      break
    case 'last3Months':
      filter.startDate = formatDate(new Date(today.getFullYear(), today.getMonth() - 2, 1))
      filter.endDate = formatDate(today)
      break
    case 'thisYear':
      filter.startDate = formatDate(new Date(today.getFullYear(), 0, 1))
      filter.endDate = formatDate(today)
      break
    case 'lastYear':
      filter.startDate = formatDate(new Date(today.getFullYear() - 1, 0, 1))
      filter.endDate = formatDate(new Date(today.getFullYear() - 1, 11, 31))
      break
  }
}

const cleanData = async () => {
  const dataToClean = selectedRows.value.length > 0 ? selectedRows.value : transactions.value
  
  if (dataToClean.length === 0) {
    ElMessage.warning('没有可清洗的数据')
    return
  }
  
  cleaning.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/cmb/clean-records', dataToClean)
    const result = res.data
    ElMessage.success(`数据清洗完成：总记录 ${result.totalCount} 条，更新 ${result.updatedCount} 条`)
    loadData()
  } catch (error) {
    ElMessage.error('清洗失败: ' + error.message)
  } finally {
    cleaning.value = false
  }
}

const exportData = async () => {
  if (transactions.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  exporting.value = true
  try {
    const exportPayload = {
      records: transactions.value,
      exportInfo: { account: '数据库', exportTime: new Date().toLocaleString() },
      summaryInfo: {}
    }
    
    const res = await axios.post('http://localhost:8080/api/cmb/export', exportPayload, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `交易记录_${new Date().toISOString().slice(0, 10)}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  } finally {
    exporting.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条记录吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const deletePromises = selectedRows.value.map(row => 
      axios.delete(`http://localhost:8080/api/bill/transaction/${row.id}`)
    )
    
    await Promise.all(deletePromises)
    ElMessage.success(`成功删除 ${selectedRows.value.length} 条记录`)
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const resetFilter = () => {
  filter.startDate = ''
  filter.endDate = ''
  filter.category = ''
  filter.transactionType = ''
  filter.paymentChannel = ''
  filter.tradeType = ''
  filter.minAmount = ''
  filter.maxAmount = ''
  filter.keyword = ''
  filter.excludeFromStats = ''
  page.current = 1
  loadData()
}

const loadCategories = async () => {
  const res = await axios.get('http://localhost:8080/api/category/list')
  categories.value = res.data
}

const loadChannels = async () => {
  const res = await axios.get('http://localhost:8080/api/payment-channel/list')
  channels.value = res.data
}

const viewRecord = (row) => {
  currentRecord.value = { ...row }
  drawerMode.value = 'view'
  drawerVisible.value = true
}

const editRecord = (row) => {
  currentRecord.value = { ...row }
  drawerMode.value = 'edit'
  drawerVisible.value = true
}

const saveRecord = async () => {
  try {
    await axios.put(`http://localhost:8080/api/bill/transaction/${currentRecord.value.id}`, currentRecord.value)
    ElMessage.success('保存成功')
    drawerVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  }
}

const batchEditNote = async () => {
  try {
    const { value: note } = await ElMessageBox.prompt('请输入用户备注', '批量编辑备注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '输入备注内容'
    })
    
    const updatePromises = selectedRows.value.map(row => 
      axios.put(`http://localhost:8080/api/bill/transaction/${row.id}`, {
        ...row,
        userNote: note
      })
    )
    
    await Promise.all(updatePromises)
    ElMessage.success(`成功更新 ${selectedRows.value.length} 条记录`)
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量编辑失败: ' + error.message)
    }
  }
}

const batchCategoryDialogVisible = ref(false)
const batchCategoryValue = ref('')

const batchEditCategory = () => {
  batchCategoryValue.value = ''
  batchCategoryDialogVisible.value = true
}

const confirmBatchCategory = async () => {
  try {
    const updatePromises = selectedRows.value.map(row => 
      axios.put(`http://localhost:8080/api/bill/transaction/${row.id}`, {
        ...row,
        category: batchCategoryValue.value
      })
    )
    
    await Promise.all(updatePromises)
    ElMessage.success(`成功更新 ${selectedRows.value.length} 条记录`)
    selectedRows.value = []
    batchCategoryDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('批量分类失败: ' + error.message)
  }
}

onMounted(() => {
  loadData()
  loadCategories()
  loadChannels()
})
</script>

<style scoped>
.transaction-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

.summary-box {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.summary-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
  background: white;
  flex: 1;
  max-width: 300px;
}

.summary-operator {
  font-size: 32px;
  font-weight: bold;
  color: #606266;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
}

.summary-item.balance .summary-value {
  color: #409eff;
}

.summary-item.income .summary-value {
  color: #67c23a;
}

.summary-item.expense .summary-value {
  color: #f56c6c;
}
</style>
