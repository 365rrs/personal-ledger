<template>
  <div class="transaction-list">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>交易记录</span>
        </div>
      </template>

      <el-form label-width="80px" class="filter-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="快捷日期">
              <el-space wrap>
                <el-date-picker v-model="quickYear" type="year" placeholder="选择年份" value-format="YYYY" style="width: 120px" @change="setQuickYear" />
                <el-date-picker v-model="quickMonth" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width: 140px" @change="setQuickMonth" />
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
              <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleDateRangeChange" />
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
              <el-select v-model="displayCategory" filterable allow-create placeholder="全部" style="width: 100%" clearable>
                <el-option label="未分类" value="__UNCATEGORIZED__" />
                <el-option v-for="cat in allCategoryNames" :key="cat" :label="cat" :value="cat" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="二级分类">
              <el-select v-model="displaySubCategory" filterable placeholder="全部" clearable>
                <el-option label="未分类" value="__UNCATEGORIZED__" />
                <el-option v-for="cat in subCategories" :key="cat.name" :label="cat.name" :value="cat.name" />
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
              <el-select v-model="filter.includeInStats" clearable placeholder="全部">
                <el-option label="计入" :value="true" />
                <el-option label="不计入" :value="false" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="是否退款">
              <el-select v-model="filter.isRefund" clearable placeholder="全部">
                <el-option label="退款" :value="true" />
                <el-option label="非退款" :value="false" />
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

      <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
        <el-space>
          <el-button type="warning" @click="cleanData" :loading="cleaning" :icon="Refresh">清洗数据</el-button>
          <el-button type="success" @click="exportData" :loading="exporting" :icon="Download">数据导出</el-button>
          <el-button type="danger" @click="batchDelete" :disabled="selectedRows.length === 0">批量删除 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditNote" :disabled="selectedRows.length === 0">批量编辑备注 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditCategory" :disabled="selectedRows.length === 0">批量分类 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditChannel" :disabled="selectedRows.length === 0">批量渠道 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="batchEditIncludeInStats" :disabled="selectedRows.length === 0">批量设置收支 ({{ selectedRows.length }})</el-button>
          <el-button type="success" @click="batchQuickAddRelated" :disabled="selectedRows.length !== 1">快速补录 ({{ selectedRows.length }})</el-button>
          <el-button type="primary" @click="openManualEntry">手动记账</el-button>
        </el-space>
        <el-button @click="columnSettingsVisible = true" :icon="Setting">列设置</el-button>
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
        <el-table-column type="selection" width="55" resizable />
        <el-table-column v-if="visibleColumns.transactionDate" prop="transactionDate" label="交易日期" width="120" sortable="custom" resizable />
        <el-table-column v-if="visibleColumns.transactionTime" prop="transactionTime" label="交易时间" width="100" resizable />
        <el-table-column v-if="visibleColumns.income" prop="income" label="收入" width="100" sortable="custom" resizable />
        <el-table-column v-if="visibleColumns.expense" prop="expense" label="支出" width="100" sortable="custom" resizable />
        <el-table-column v-if="visibleColumns.transactionType" prop="transactionType" label="交易类型" width="120" show-overflow-tooltip resizable />
        <el-table-column v-if="visibleColumns.paymentChannel" prop="paymentChannel" label="支付渠道" width="100" resizable />
        <el-table-column v-if="visibleColumns.category" prop="category" label="分类" width="100" resizable />
        <el-table-column v-if="visibleColumns.subCategory" prop="subCategory" label="二级分类" width="100" resizable />
        <el-table-column v-if="visibleColumns.description" prop="description" label="交易备注" width="250" show-overflow-tooltip resizable />
        <el-table-column v-if="visibleColumns.userNote" prop="userNote" label="用户备注" width="150" show-overflow-tooltip resizable />
        <el-table-column v-if="visibleColumns.includeInStats" label="计入收支" width="100" align="center" resizable>
          <template #default="{ row }">
            <el-tag size="small" :type="row.includeInStats ? 'success' : 'info'">
              {{ row.includeInStats ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.isRefund" label="退款" width="80" align="center" resizable>
          <template #default="{ row }">
            <el-tag size="small" :type="row.isRefund ? 'warning' : ''" v-if="row.isRefund">
              退款
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="visibleColumns.isManualEntry" label="手工记账" width="100" align="center" resizable>
          <template #default="{ row }">
            <el-tag size="small" type="primary" v-if="row.isManualEntry">
              手工
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
          <el-cascader v-model="batchCategoryValue" :options="categoryTree" :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }" clearable filterable placeholder="请选择分类" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchCategory">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="quickAddDialogVisible" title="关联记录快速补录" width="500px">
      <el-form :model="quickAddForm" label-width="100px">
        <el-form-item label="原记录">
          <el-input :value="quickAddForm.originalDesc" readonly />
        </el-form-item>
        <el-form-item label="原金额">
          <el-input :value="quickAddForm.refundAmount" readonly />
        </el-form-item>
        <el-form-item label="记账类型" required>
          <el-radio-group v-model="quickAddForm.recordType" @change="handleRecordTypeChange">
            <el-radio value="expense">支出（如：退票手续费）</el-radio>
            <el-radio value="income">收入（如：价保返现）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="quickAddForm.recordType === 'expense' ? '扣款金额' : '返现金额'" required>
          <el-input v-model="quickAddForm.deductAmount" :placeholder="quickAddForm.recordType === 'expense' ? '请输入实际扣款金额' : '请输入返现金额'" type="number" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="quickAddForm.note" type="textarea" :rows="3" :placeholder="quickAddForm.recordType === 'expense' ? '如：退票手续费' : '如：京东价保返现'" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader v-model="quickAddForm.category" :options="categoryTree.filter(c => c.type === (quickAddForm.recordType === 'expense' ? 'EXPENSE' : 'INCOME'))" :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }" clearable filterable placeholder="选择分类" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quickAddDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmQuickAdd">确定补录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="columnSettingsVisible" title="列设置" width="400px">
      <div style="max-height: 400px; overflow-y: auto;">
        <el-checkbox-group v-model="selectedColumns">
          <div v-for="col in columnOptions" :key="col.key" style="margin-bottom: 8px;">
            <el-checkbox :label="col.key">{{ col.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="resetColumns">恢复默认</el-button>
        <el-button type="primary" @click="saveColumnSettings">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchChannelDialogVisible" title="批量设置渠道" width="400px">
      <el-form label-width="80px">
        <el-form-item label="支付渠道">
          <el-select v-model="batchChannelValue" filterable placeholder="请选择支付渠道" style="width: 100%">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchChannelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchChannel">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchIncludeInStatsDialogVisible" title="批量设置收支" width="400px">
      <el-form label-width="100px">
        <el-form-item label="是否计入收支">
          <el-radio-group v-model="batchIncludeInStatsValue">
            <el-radio :label="true">计入</el-radio>
            <el-radio :label="false">不计入</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchIncludeInStatsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchIncludeInStats">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="getDrawerTitle()" size="500px">
      <el-form :model="currentRecord" :rules="drawerMode === 'manual' ? manualRules : {}" ref="drawerFormRef" label-width="100px" :disabled="drawerMode === 'view'">
        <el-form-item label="交易日期" :prop="drawerMode === 'manual' ? 'transactionDate' : ''">
          <el-date-picker v-if="drawerMode === 'manual'" v-model="currentRecord.transactionDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
          <el-input v-else v-model="currentRecord.transactionDate" readonly />
        </el-form-item>
        <el-form-item label="交易时间" :prop="drawerMode === 'manual' ? 'transactionTime' : ''">
          <el-time-picker v-if="drawerMode === 'manual'" v-model="currentRecord.transactionTime" placeholder="选择时间" value-format="HH:mm:ss" style="width: 100%" />
          <el-input v-else v-model="currentRecord.transactionTime" readonly />
        </el-form-item>
        <el-form-item v-if="drawerMode === 'manual'" label="收支类型" prop="type">
          <el-radio-group v-model="currentRecord.type">
            <el-radio label="income">收入</el-radio>
            <el-radio label="expense">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="drawerMode === 'manual'" label="金额" prop="amount">
          <el-input v-model="currentRecord.amount" placeholder="请输入金额" type="number" step="0.01">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item v-if="drawerMode !== 'manual'" label="收入">
          <el-input v-model="currentRecord.income" readonly />
        </el-form-item>
        <el-form-item v-if="drawerMode !== 'manual'" label="支出">
          <el-input v-model="currentRecord.expense" readonly />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-input v-model="currentRecord.transactionType" :placeholder="drawerMode === 'manual' ? '如：转账、消费、退款等' : ''" :readonly="drawerMode === 'view'" />
        </el-form-item>
        <el-form-item label="交易描述">
          <el-input v-model="currentRecord.description" type="textarea" :placeholder="drawerMode === 'manual' ? '交易描述' : ''" :readonly="drawerMode === 'view'" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader v-model="currentRecord.category" :options="filteredCategoryTree" :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }" clearable filterable :placeholder="drawerMode === 'manual' ? '请选择分类' : ''" style="width: 100%" :disabled="drawerMode === 'view'" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="currentRecord.paymentChannel" filterable :placeholder="drawerMode === 'manual' ? '请选择支付渠道' : ''">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户备注">
          <el-input v-model="currentRecord.userNote" type="textarea" :rows="3" :placeholder="drawerMode === 'manual' ? '添加备注信息' : ''" />
        </el-form-item>
        <el-form-item label="计入收支">
          <el-switch v-model="currentRecord.includeInStats" active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="是否退款">
          <el-switch v-model="currentRecord.isRefund" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>
      <template #footer v-if="drawerMode !== 'view'">
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord" :loading="submitting">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Search, RefreshRight, Setting } from '@element-plus/icons-vue'

const route = useRoute()

const loading = ref(false)
const cleaning = ref(false)
const exporting = ref(false)
const transactions = ref([])
const selectedRows = ref([])
const categories = ref([])
const categoryTree = ref([])
const parentCategories = ref([])
const subCategories = ref([])
const allCategoryNames = ref([])
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
  category: '__ALL__',
  subCategory: '__ALL__',
  transactionType: '', 
  paymentChannel: '',
  tradeType: '',
  minAmount: '',
  maxAmount: '',
  keyword: '',
  includeInStats: '',
  isRefund: ''
})
const dateRange = ref([thisMonth.startDate, thisMonth.endDate])
const quickYear = ref(new Date().getFullYear().toString())
const quickMonth = ref('')

const handleDateRangeChange = (value) => {
  if (value) {
    filter.startDate = value[0]
    filter.endDate = value[1]
  } else {
    filter.startDate = ''
    filter.endDate = ''
  }
}

const setQuickYear = (year) => {
  if (year) {
    filter.startDate = `${year}-01-01`
    filter.endDate = `${year}-12-31`
    dateRange.value = [filter.startDate, filter.endDate]
  }
}

const setQuickMonth = (month) => {
  if (month) {
    const [year, mon] = month.split('-')
    const lastDay = new Date(parseInt(year), parseInt(mon), 0).getDate()
    filter.startDate = `${month}-01`
    filter.endDate = `${month}-${String(lastDay).padStart(2, '0')}`
    dateRange.value = [filter.startDate, filter.endDate]
  }
}
const sortField = ref('')
const sortOrder = ref('')
const summary = ref({ income: '0.00', expense: '0.00', balance: '0.00' })
const page = reactive({ current: 1, size: 20, total: 0 })
const drawerVisible = ref(false)
const drawerMode = ref('view')
const currentRecord = ref({})
const drawerFormRef = ref(null)
const submitting = ref(false)

const manualRules = {
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }],
  transactionTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }],
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效金额', trigger: 'blur' }
  ]
}

const filteredCategories = computed(() => {
  if (drawerMode.value !== 'manual') return categories.value
  const typeMap = { income: 'INCOME', expense: 'EXPENSE' }
  return categories.value.filter(cat => cat.type === typeMap[currentRecord.value.type])
})

const filteredCategoryTree = computed(() => {
  if (drawerMode.value === 'view') return categoryTree.value
  if (!currentRecord.value.type) return categoryTree.value
  const typeMap = { income: 'INCOME', expense: 'EXPENSE' }
  return categoryTree.value.filter(cat => cat.type === typeMap[currentRecord.value.type])
})

const displayCategory = computed({
  get: () => filter.category === '__ALL__' ? null : filter.category,
  set: (val) => { filter.category = val || '__ALL__' }
})

const displaySubCategory = computed({
  get: () => filter.subCategory === '__ALL__' ? null : filter.subCategory,
  set: (val) => { filter.subCategory = val || '__ALL__' }
})

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
    
    // 分类参数：__ALL__=不查询，__UNCATEGORIZED__=查未分类，其他=查具体分类
    params.category = filter.category === '__UNCATEGORIZED__' ? '' : filter.category
    params.subCategory = filter.subCategory === '__UNCATEGORIZED__' ? '' : filter.subCategory
    
    // 收支类型参数
    if (filter.transactionType) {
      params.incomeOrExpense = filter.transactionType
    }
    
    // 计入收支参数
    if (filter.includeInStats !== '') {
      params.includeInStats = filter.includeInStats
    }
    
    // 退款参数
    if (filter.isRefund !== '') {
      params.isRefund = filter.isRefund
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
    
    // 分类参数：__ALL__=不查询，__UNCATEGORIZED__=查未分类，其他=查具体分类
    params.category = filter.category === '__UNCATEGORIZED__' ? '' : filter.category
    params.subCategory = filter.subCategory === '__UNCATEGORIZED__' ? '' : filter.subCategory
    if (filter.transactionType) {
      params.incomeOrExpense = filter.transactionType
    }
    if (filter.includeInStats !== '') {
      params.includeInStats = filter.includeInStats
    }
    if (filter.isRefund !== '') {
      params.isRefund = filter.isRefund
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
  dateRange.value = [filter.startDate, filter.endDate]
}

const cleanData = async () => {
  const dataToClean = selectedRows.value.length > 0 ? selectedRows.value : transactions.value
  
  if (dataToClean.length === 0) {
    ElMessage.warning('没有可清洗的数据')
    return
  }
  
  // 转换前端字段到后端期望的字段
  const convertedData = dataToClean.map(item => ({
    id: item.id,
    transactionDate: item.transactionDate,
    transactionTime: item.transactionTime,
    income: item.income,
    expense: item.expense,
    balance: item.balance,
    transactionType: item.transactionType,
    description: item.description,
    includeInStats: item.includeInStats,
    paymentChannel: item.paymentChannel,
    category: item.category,
    userNote: item.userNote
  }))
  
  cleaning.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/bill/import/clean-records', convertedData)
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
  exporting.value = true
  try {
    const params = {
      startDate: filter.startDate,
      endDate: filter.endDate,
      category: filter.category === '__UNCATEGORIZED__' ? '' : filter.category,
      paymentChannel: filter.paymentChannel,
      transactionType: filter.tradeType,
      keyword: filter.keyword,
      minAmount: filter.minAmount,
      maxAmount: filter.maxAmount,
      incomeOrExpense: filter.transactionType,
      includeInStats: filter.includeInStats,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      firstImportId: route.query.importId
    }
    
    const res = await axios.post('http://localhost:8080/api/bill/import/export', params, {
      responseType: 'blob'
    })
    
    const now = new Date()
    const dateStr = now.toISOString().slice(0, 10)
    const timestamp = now.getTime()
    const fileName = `招商银行账单_${dateStr}_${timestamp}.xlsx`
    
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
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
  filter.category = '__ALL__'
  filter.subCategory = '__ALL__'
  filter.transactionType = ''
  filter.paymentChannel = ''
  filter.tradeType = ''
  filter.minAmount = ''
  filter.maxAmount = ''
  filter.keyword = ''
  filter.includeInStats = ''
  filter.isRefund = ''
  dateRange.value = []
  quickYear.value = ''
  quickMonth.value = ''
  page.current = 1
  loadData()
}

const loadCategories = async () => {
  const res = await axios.get('http://localhost:8080/api/category/list')
  categoryTree.value = res.data
  flattenCategories(res.data)
  extractParentAndSubCategories(res.data)
}

const flattenCategories = (tree) => {
  const result = []
  const traverse = (nodes) => {
    nodes.forEach(node => {
      result.push(node)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  traverse(tree)
  categories.value = result
}

const extractParentAndSubCategories = (tree) => {
  const parents = []
  const subs = []
  const allNames = []
  tree.forEach(node => {
    parents.push({ name: node.name })
    allNames.push(node.name)
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => {
        subs.push({ name: child.name })
        allNames.push(child.name)
      })
    }
  })
  parentCategories.value = parents
  subCategories.value = subs
  allCategoryNames.value = allNames
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
  currentRecord.value.type = row.income && parseFloat(row.income) > 0 ? 'income' : 'expense'
  
  // 根据原数据匹配分类ID
  if (row.subCategory) {
    // 有二级分类，查找二级分类的ID
    const subCat = categories.value.find(c => c.name === row.subCategory && c.parentId)
    if (subCat) {
      currentRecord.value.category = subCat.id
    }
  } else if (row.category) {
    // 只有一级分类，查找一级分类的ID
    const parentCat = categories.value.find(c => c.name === row.category && (c.parentId === null || c.parentId === 0))
    if (parentCat) {
      currentRecord.value.category = parentCat.id
    }
  }
  
  drawerMode.value = 'edit'
  drawerVisible.value = true
}

const getDrawerTitle = () => {
  if (drawerMode.value === 'view') return '查看交易'
  if (drawerMode.value === 'edit') return '编辑交易'
  if (drawerMode.value === 'manual') return '手动记账'
  return '交易记录'
}

const openManualEntry = () => {
  currentRecord.value = {
    transactionDate: new Date().toISOString().split('T')[0],
    transactionTime: new Date().toTimeString().split(' ')[0],
    type: 'expense',
    amount: '',
    category: '',
    paymentChannel: '',
    transactionType: '',
    description: '',
    userNote: '',
    includeInStats: true,
    isRefund: false
  }
  drawerMode.value = 'manual'
  drawerVisible.value = true
}

const saveRecord = async () => {
  if (drawerMode.value === 'manual') {
    if (!drawerFormRef.value) return
    
    await drawerFormRef.value.validate(async (valid) => {
      if (!valid) return
      
      submitting.value = true
      try {
        const data = {
          transactionDate: currentRecord.value.transactionDate,
          transactionTime: currentRecord.value.transactionTime,
          income: currentRecord.value.type === 'income' ? currentRecord.value.amount : null,
          expense: currentRecord.value.type === 'expense' ? currentRecord.value.amount : null,
          category: currentRecord.value.category,
          paymentChannel: currentRecord.value.paymentChannel,
          transactionType: currentRecord.value.transactionType,
          description: currentRecord.value.description,
          userNote: currentRecord.value.userNote,
          includeInStats: currentRecord.value.includeInStats,
          isRefund: currentRecord.value.isRefund,
          isManualEntry: true
        }
        
        await axios.post('http://localhost:8080/api/bill/transaction', data)
        ElMessage.success('记账成功')
        drawerVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error('记账失败：' + (error.response?.data?.message || error.message))
      } finally {
        submitting.value = false
      }
    })
  } else {
    try {
      const selectedCategory = categories.value.find(c => c.id === currentRecord.value.category)
      const updateData = { ...currentRecord.value }
      
      if (selectedCategory) {
        if (selectedCategory.parentId === null || selectedCategory.parentId === 0) {
          updateData.category = selectedCategory.name
          updateData.subCategory = null
        } else {
          const parent = categories.value.find(c => c.id === selectedCategory.parentId)
          if (parent) {
            updateData.category = parent.name
            updateData.subCategory = selectedCategory.name
          }
        }
      }
      
      await axios.put(`http://localhost:8080/api/bill/transaction/${currentRecord.value.id}`, updateData)
      ElMessage.success('保存成功')
      drawerVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('保存失败: ' + error.message)
    }
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
    const selectedCategory = categories.value.find(c => c.id === batchCategoryValue.value)
    let categoryUpdate = {}
    
    if (selectedCategory) {
      if (selectedCategory.parentId === null || selectedCategory.parentId === 0) {
        categoryUpdate = {
          category: selectedCategory.name,
          subCategory: null
        }
      } else {
        const parent = categories.value.find(c => c.id === selectedCategory.parentId)
        if (parent) {
          categoryUpdate = {
            category: parent.name,
            subCategory: selectedCategory.name
          }
        }
      }
    }
    
    const updatePromises = selectedRows.value.map(row => 
      axios.put(`http://localhost:8080/api/bill/transaction/${row.id}`, {
        ...row,
        ...categoryUpdate
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

const quickAddDialogVisible = ref(false)
const quickAddForm = reactive({
  originalId: null,
  originalDesc: '',
  refundAmount: '',
  recordType: 'expense',
  deductAmount: '',
  note: '',
  category: '',
  transactionDate: '',
  paymentChannel: ''
})

const batchQuickAddRelated = () => {
  if (selectedRows.value.length !== 1) {
    ElMessage.warning('请选择一条记录进行快速补录')
    return
  }
  quickAddRelated(selectedRows.value[0])
}

const quickAddRelated = (row) => {
  quickAddForm.originalId = row.id
  quickAddForm.originalDesc = row.description
  quickAddForm.refundAmount = row.income || row.expense || '0.00'
  quickAddForm.recordType = 'expense'
  quickAddForm.deductAmount = ''
  quickAddForm.note = `退款扣款 - ${row.description}`
  quickAddForm.category = ''
  quickAddForm.transactionDate = row.transactionDate
  quickAddForm.paymentChannel = row.paymentChannel
  quickAddDialogVisible.value = true
}

const handleRecordTypeChange = () => {
  quickAddForm.category = ''
  const desc = quickAddForm.originalDesc
  quickAddForm.note = quickAddForm.recordType === 'expense' 
    ? `退款扣款 - ${desc}`
    : `价保返现 - ${desc}`
}

const confirmQuickAdd = async () => {
  if (!quickAddForm.deductAmount || parseFloat(quickAddForm.deductAmount) <= 0) {
    ElMessage.warning('请输入有效的金额')
    return
  }
  
  try {
    const isExpense = quickAddForm.recordType === 'expense'
    const newRecord = {
      transactionDate: quickAddForm.transactionDate,
      transactionTime: new Date().toTimeString().slice(0, 8),
      income: isExpense ? null : parseFloat(quickAddForm.deductAmount),
      expense: isExpense ? parseFloat(quickAddForm.deductAmount) : null,
      balance: null,
      transactionType: isExpense ? '退款扣款' : '价保返现',
      description: `关联记录: ${quickAddForm.originalDesc}`,
      paymentChannel: quickAddForm.paymentChannel,
      category: quickAddForm.category,
      userNote: quickAddForm.note,
      includeInStats: true,
      isRefund: false,
      isManualEntry: true
    }
    
    await axios.post('http://localhost:8080/api/bill/transaction', newRecord)
    ElMessage.success('补录成功')
    quickAddDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('补录失败: ' + error.message)
  }
}

const batchChannelDialogVisible = ref(false)
const batchChannelValue = ref('')

const batchEditChannel = () => {
  batchChannelValue.value = ''
  batchChannelDialogVisible.value = true
}

const confirmBatchChannel = async () => {
  try {
    const updatePromises = selectedRows.value.map(row => 
      axios.put(`http://localhost:8080/api/bill/transaction/${row.id}`, {
        ...row,
        paymentChannel: batchChannelValue.value
      })
    )
    
    await Promise.all(updatePromises)
    ElMessage.success(`成功更新 ${selectedRows.value.length} 条记录`)
    selectedRows.value = []
    batchChannelDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('批量设置渠道失败: ' + error.message)
  }
}

const batchIncludeInStatsDialogVisible = ref(false)
const batchIncludeInStatsValue = ref(true)

const columnSettingsVisible = ref(false)
const columnOptions = [
  { key: 'transactionDate', label: '交易日期' },
  { key: 'transactionTime', label: '交易时间' },
  { key: 'income', label: '收入' },
  { key: 'expense', label: '支出' },
  { key: 'transactionType', label: '交易类型' },
  { key: 'paymentChannel', label: '支付渠道' },
  { key: 'category', label: '分类' },
  { key: 'subCategory', label: '二级分类' },
  { key: 'description', label: '交易备注' },
  { key: 'userNote', label: '用户备注' },
  { key: 'includeInStats', label: '计入收支' },
  { key: 'isRefund', label: '退款' },
  { key: 'isManualEntry', label: '手工记账' }
]

const defaultColumns = columnOptions.map(col => col.key)
const selectedColumns = ref([...defaultColumns])
const visibleColumns = computed(() => {
  const result = {}
  columnOptions.forEach(col => {
    result[col.key] = selectedColumns.value.includes(col.key)
  })
  return result
})

const saveColumnSettings = () => {
  localStorage.setItem('transactionListColumns', JSON.stringify(selectedColumns.value))
  columnSettingsVisible.value = false
  ElMessage.success('列设置已保存')
}

const resetColumns = () => {
  selectedColumns.value = [...defaultColumns]
}

const loadColumnSettings = () => {
  const saved = localStorage.getItem('transactionListColumns')
  if (saved) {
    try {
      selectedColumns.value = JSON.parse(saved)
    } catch (e) {
      selectedColumns.value = [...defaultColumns]
    }
  }
}

const batchEditIncludeInStats = () => {
  batchIncludeInStatsValue.value = true
  batchIncludeInStatsDialogVisible.value = true
}

const confirmBatchIncludeInStats = async () => {
  try {
    const updatePromises = selectedRows.value.map(row => 
      axios.put(`http://localhost:8080/api/bill/transaction/${row.id}`, {
        ...row,
        includeInStats: batchIncludeInStatsValue.value
      })
    )
    
    await Promise.all(updatePromises)
    ElMessage.success(`成功设置 ${selectedRows.value.length} 条记录${batchIncludeInStatsValue.value ? '计入' : '不计入'}收支`)
    selectedRows.value = []
    batchIncludeInStatsDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('批量设置失败: ' + error.message)
  }
}

onMounted(() => {
  loadData()
  loadCategories()
  loadChannels()
  loadColumnSettings()
})
</script>

<style scoped>
.transaction-list {
  padding: 0;
}

.transaction-list .el-card {
  border-radius: 0;
  border: none;
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
