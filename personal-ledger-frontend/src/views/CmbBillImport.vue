<template>
  <div class="cmb-bill-import">
    <!-- 文件上传区域 -->
    <el-card class="upload-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Upload /></el-icon>
          <span>文件上传</span>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="CSV文件">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            action="/api/cmb/import-full"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            :limit="1"
            accept=".csv"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持招商银行CSV格式，文件大小不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item>
          <el-space>
            <el-button 
              type="primary" 
              @click="submitForm"
              :loading="uploading"
              :disabled="!selectedFile"
              :icon="uploading ? Loading : DocumentAdd"
            >
              {{ uploading ? '导入中...' : '导入并解析' }}
            </el-button>
            <el-button @click="clearForm" :icon="Delete">清空</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 解析结果区域 -->
    <el-card v-if="result" class="result-card" :class="{ error: result.error }" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon" :class="result.error ? 'error-icon' : 'success-icon'">
              <WarningFilled v-if="result.error" />
              <SuccessFilled v-else />
            </el-icon>
            <span>{{ result.error ? '错误信息' : '解析结果' }}</span>
          </div>
          <div class="header-actions" v-if="!result.error">
            <el-space>
              <el-button 
                type="warning" 
                @click="cleanData"
                :disabled="!result.data || result.data.length === 0"
                :icon="Refresh"
                size="small"
              >
                数据清洗
              </el-button>
              <el-button 
                type="success" 
                @click="exportData"
                :disabled="!result.data || result.data.length === 0"
                :icon="Download"
                size="small"
              >
                导出数据
              </el-button>
            </el-space>
          </div>
        </div>
      </template>
      
      <div v-if="!result.error" class="result-content">
        <!-- 账单信息展示 -->
        <el-card v-if="result.exportInfo" class="info-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon><InfoFilled /></el-icon>
              <span>账单信息</span>
            </div>
          </template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="导出时间">{{ result.exportInfo.exportTime }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ result.exportInfo.account }}</el-descriptions-item>
            <el-descriptions-item label="币种">{{ result.exportInfo.currency }}</el-descriptions-item>
            <el-descriptions-item label="起始日期">{{ result.exportInfo.startDate }}</el-descriptions-item>
            <el-descriptions-item label="终止日期">{{ result.exportInfo.endDate }}</el-descriptions-item>
            <el-descriptions-item label="过滤设置">{{ result.exportInfo.filterSetting }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <!-- 筛选条件 -->
        <el-card class="filter-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon><Filter /></el-icon>
              <span>筛选条件</span>
            </div>
          </template>
          <el-form :model="filterForm" label-width="120px" inline>
            <el-form-item label="交易日期区间">
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
            <el-form-item>
              <el-space>
                <el-button type="primary" @click="applyFilter" :icon="Search" size="small">筛选</el-button>
                <el-button @click="resetFilter" :icon="RefreshRight" size="small">重置</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 交易记录表格 -->
        <el-table :data="filteredData" style="width: 100%" max-height="400" class="transaction-table" :default-sort="{prop: 'formattedTradeDate', order: 'ascending'}">
          <el-table-column prop="formattedTradeDate" label="交易日期" width="120" sortable></el-table-column>
          <el-table-column prop="tradeTime" label="交易时间" width="120"></el-table-column>
          <el-table-column prop="income" label="收入" width="100" sortable></el-table-column>
          <el-table-column prop="expense" label="支出" width="100" sortable></el-table-column>
          <el-table-column prop="balance" label="余额" width="100" sortable></el-table-column>
          <el-table-column prop="tradeType" label="交易类型" width="150">
            <template #default="scope">
              <el-tag>{{ scope.row.tradeType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="交易备注"></el-table-column>
          <el-table-column prop="paymentChannel" label="支付渠道" width="120">
            <template #default="scope">
              <span>{{ scope.row.paymentChannel || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="transactionType" label="收支类型" width="120">
            <template #default="scope">
              <span>{{ scope.row.transactionType || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类" width="120">
            <template #default="scope">
              <span>{{ scope.row.category || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="userRemark" label="用户备注" width="150">
            <template #default="scope">
              <el-input 
                v-model="scope.row.userRemark" 
                placeholder="请输入备注" 
                size="small" 
                @change="handleFieldChange(scope.row, 'userRemark')"
                clearable
              />
            </template>
          </el-table-column>
          <el-table-column label="是否计入本月收支" width="120" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.excludeFromMonthly"
                active-text="是"
                inactive-text="否"
                @change="handleExcludeChange(scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>
        <div class="result-summary">
          共解析 {{ result.data.length }} 条记录，筛选后显示 {{ filteredData.length }} 条记录
        </div>
        
        <!-- 收支统计信息 -->
        <el-card v-if="result.summary" class="summary-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon><TrendCharts /></el-icon>
              <span>收支统计</span>
            </div>
          </template>
          <el-row :gutter="20" class="summary-content">
            <el-col :span="12">
              <div class="summary-item income">
                <div class="summary-label">收入</div>
                <div class="summary-value">{{ filteredSummary.incomeAmount }}</div>
                <div class="summary-count">{{ filteredSummary.incomeCount }} 笔</div>
                <div class="summary-exclude">
                  (不计入: {{ filteredSummary.excludeIncomeAmount }}, {{ filteredSummary.excludeIncomeCount }} 笔)
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="summary-item expense">
                <div class="summary-label">支出</div>
                <div class="summary-value">{{ filteredSummary.expenseAmount }}</div>
                <div class="summary-count">{{ filteredSummary.expenseCount }} 笔</div>
                <div class="summary-exclude">
                  (不计入: {{ filteredSummary.excludeExpenseAmount }}, {{ filteredSummary.excludeExpenseCount }} 笔)
                </div>
              </div>
            </el-col>
          </el-row>
          <div class="summary-note" v-if="filterForm.startDate || filterForm.endDate">
            * 以上统计基于当前筛选条件
          </div>
        </el-card>
      </div>
      
      <div v-else class="error-content">
        <el-alert
          :title="result.data"
          type="error"
          show-icon
          :closable="false"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  Upload, UploadFilled, Loading, DocumentAdd, Delete, 
  WarningFilled, SuccessFilled, Refresh, Download,
  InfoFilled, Filter, Search, RefreshRight, TrendCharts
} from '@element-plus/icons-vue'

const form = reactive({})
const selectedFile = ref(null)
const uploading = ref(false)
const result = ref(null)
const uploadRef = ref(null)

// 筛选表单
const filterForm = reactive({
  startDate: '',
  endDate: ''
})

// 筛选后的数据
const filteredData = computed(() => {
  if (!result.value || !result.value.data) {
    return []
  }
  
  let filtered = result.value.data
  
  // 日期筛选
  if (filterForm.startDate || filterForm.endDate) {
    filtered = filtered.filter(item => {
      // 直接使用 formattedTradeDate 字段进行比较
      if (!item.formattedTradeDate) return false
      
      const tradeDate = new Date(item.formattedTradeDate)
      
      // 检查是否在指定日期范围内
      if (filterForm.startDate && tradeDate < new Date(filterForm.startDate)) {
        return false
      }
      
      if (filterForm.endDate && tradeDate > new Date(filterForm.endDate)) {
        return false
      }
      
      return true
    })
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
    // 判断是否计入本月收支 (true表示计入，false表示不计入)
    const isExcluded = item.excludeFromMonthly === false;
    
    // 收入统计
    if (item.income && item.income > 0) {
      if (isExcluded) {
        excludeIncomeCount++
        excludeIncomeAmount += parseFloat(item.income)
      } else {
        incomeCount++
        incomeAmount += parseFloat(item.income)
      }
    }
    
    // 支出统计
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
  
  return {
    incomeCount,
    incomeAmount: incomeAmount.toFixed(2) + '元',
    expenseCount,
    expenseAmount: expenseAmount.toFixed(2) + '元',
    excludeIncomeCount,
    excludeIncomeAmount: excludeIncomeAmount.toFixed(2) + '元',
    excludeExpenseCount,
    excludeExpenseAmount: excludeExpenseAmount.toFixed(2) + '元'
  }
})

// 为交易记录添加格式化日期属性
const processTransactionData = (data) => {
  if (!data || !Array.isArray(data)) return data;
  
  return data.map(item => ({
    ...item,
    // 直接使用后端格式化好的日期
    formattedTradeDate: item.formattedTradeDate || item.tradeDate,
    // 确保 excludeFromMonthly 字段存在，默认为 false
    excludeFromMonthly: item.excludeFromMonthly !== undefined ? item.excludeFromMonthly : false,
    // 确保新增字段存在
    paymentChannel: item.paymentChannel || '',
    transactionType: item.transactionType || '',
    category: item.category || '',
    userRemark: item.userRemark || ''
  }));
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const submitForm = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  result.value = null

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    // 使用新的API端点获取完整信息
    const response = await axios.post('/api/cmb/import-full', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    result.value = {
      error: false,
      data: processTransactionData(response.data.records),
      exportInfo: response.data.exportInfo,
      summary: response.data.summaryInfo
    }
    
    // 重置筛选条件
    resetFilter()
    
    ElMessage.success('账单导入并解析成功')
  } catch (error) {
    result.value = {
      error: true,
      data: error.response?.data?.message || error.message || '未知错误'
    }
    
    ElMessage.error('账单导入失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 数据清洗功能
const cleanData = async () => {
  if (!result.value || !result.value.data || result.value.data.length === 0) {
    ElMessage.warning('没有可清洗的数据')
    return
  }

  try {
    // 发送数据到清洗接口
    const response = await axios.post('/api/cmb/clean-records', result.value.data);
    
    // 更新数据
    result.value.data = processTransactionData(response.data);
    
    ElMessage.success('数据清洗完成')
  } catch (error) {
    ElMessage.error('数据清洗失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

const clearForm = () => {
  selectedFile.value = null
  result.value = null
  // 清空上传组件中的文件
  if (uploadRef.value && uploadRef.value.clearFiles) {
    uploadRef.value.clearFiles()
  }
}

// 应用筛选
const applyFilter = () => {
  // 筛选逻辑已经在 computed 属性中实现
  ElMessage.success('筛选条件已应用')
}

// 重置筛选
const resetFilter = () => {
  filterForm.startDate = ''
  filterForm.endDate = ''
}

// 处理是否计入本月收支开关变化
const handleExcludeChange = (row) => {
  // 触发重新计算统计信息
  // 由于使用了 computed 属性，数据变化会自动触发重新计算
  ElMessage.info(`已更新交易记录的"是否计入本月收支"状态`);
}

// 处理字段变更
const handleFieldChange = (row, field) => {
  // 当字段发生变化时的处理逻辑
  ElMessage.info(`已更新交易记录的"${field}"字段`);
}

// 导出数据功能
const exportData = async () => {
  try {
    // 准备导出的数据
    const exportPayload = {
      records: filteredData.value,
      exportInfo: result.value.exportInfo,
      summaryInfo: result.value.summary
    };
    
    // 发送请求到后端导出接口
    const response = await axios.post('/api/cmb/export', exportPayload, {
      responseType: 'blob' // 重要：设置响应类型为 blob 以处理文件下载
    });
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    // 修改文件名，增加时间戳
    link.setAttribute('download', `招商银行账单_${new Date().toISOString().slice(0, 10)}_${new Date().getTime()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('数据导出失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};
</script>

<style scoped>
.cmb-bill-import {
  padding: 0;
}

.upload-card {
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
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-icon {
  font-size: 18px;
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.result-card {
  margin-bottom: 20px;
}

.result-card.error {
  border-color: #f56c6c;
}

.result-content {
  text-align: left;
}

.result-summary {
  margin-top: 15px;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
  text-align: right;
  font-weight: 500;
  color: #606266;
  border-left: 4px solid #409eff;
}

.error-content {
  text-align: left;
}

.el-upload__tip {
  margin-top: 10px;
}

.info-card,
.summary-card,
.filter-card {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.info-card :deep(.el-card__header),
.filter-card :deep(.el-card__header),
.summary-card :deep(.el-card__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.transaction-table {
  margin: 15px 0;
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
}

.summary-item.income {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
}

.summary-item.expense {
  background-color: #fef0f0;
  border: 1px solid #fbc4c4;
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

.summary-count {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.summary-exclude {
  font-size: 12px;
  color: #909399;
}

.summary-note {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style>