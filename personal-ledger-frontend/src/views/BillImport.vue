<template>
  <div class="bill-import">
    <!-- 导入指导卡片 -->
    <el-card class="guide-card" shadow="never">
      <div class="guide-content">
        <div class="guide-text">
          <h2>标准导入</h2>
          <p>引导式账单导入，支持招商银行CSV原始账单和系统导出Excel文件的导入，实现数据的持续累加和管理</p>
        </div>
        <div class="guide-actions">
          <el-button type="info" @click="switchToSimple" :icon="Lightning">
            切换到快速导入
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 导入方式选择 -->
    <el-row :gutter="24" class="import-options">
      <el-col :span="12">
        <el-card class="import-card csv-card" :class="{ active: importMode === 'csv' }" shadow="hover" @click="selectImportMode('csv')">
          <div class="import-card-content">
            <div class="import-icon">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="import-info">
              <h3>CSV 原始账单</h3>
              <p>导入招商银行导出的CSV账单文件</p>
              <div class="import-features">
                <el-tag size="small" type="info">原始数据</el-tag>
                <el-tag size="small" type="warning">需要清洗</el-tag>
              </div>
            </div>
            <div class="import-status">
              <el-icon v-if="importMode === 'csv'" class="selected-icon"><Check /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="import-card excel-card" :class="{ active: importMode === 'excel' }" shadow="hover" @click="selectImportMode('excel')">
          <div class="import-card-content">
            <div class="import-icon">
              <el-icon size="32"><Grid /></el-icon>
            </div>
            <div class="import-info">
              <h3>Excel 数据载体</h3>
              <p>导入之前导出的Excel文件，保留用户修改</p>
              <div class="import-features">
                <el-tag size="small" type="success">保留备注</el-tag>
                <el-tag size="small" type="primary">数据合并</el-tag>
              </div>
            </div>
            <div class="import-status">
              <el-icon v-if="importMode === 'excel'" class="selected-icon"><Check /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 文件上传区域 -->
    <el-card v-if="importMode" class="upload-card" shadow="hover">
      <div class="upload-area">
        <el-upload
          ref="uploadRef"
          class="file-uploader"
          :class="{ 'has-file': selectedFile }"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          :limit="1"
          :accept="importMode === 'csv' ? '.csv' : '.xlsx,.xls'"
          drag
        >
          <div class="upload-content">
            <div class="upload-icon">
              <el-icon v-if="!selectedFile" size="48"><UploadFilled /></el-icon>
              <el-icon v-else size="48" class="success-icon"><CircleCheck /></el-icon>
            </div>
            <div class="upload-text">
              <h3 v-if="!selectedFile">
                将{{ importMode === 'csv' ? 'CSV' : 'Excel' }}文件拖到此处，或点击选择文件
              </h3>
              <h3 v-else class="file-selected">
                ✓ 已选择文件：{{ selectedFile.name }}
              </h3>
              <p v-if="importMode === 'csv'">支持招商银行CSV格式，文件大小不超过10MB</p>
              <p v-else>支持系统导出Excel文件，自动保留用户修改</p>
            </div>
          </div>
        </el-upload>
      </div>
      
      <div class="upload-actions">
        <el-button 
          type="primary" 
          size="large"
          @click="importFile"
          :loading="uploading"
          :disabled="!selectedFile"
          :icon="uploading ? Loading : DocumentAdd"
        >
          {{ uploading ? '导入中...' : `开始导入${importMode === 'csv' ? 'CSV' : 'Excel'}账单` }}
        </el-button>
        <el-button @click="clearSelection" :icon="Delete">重新选择</el-button>
      </div>
    </el-card>
    
    <!-- 导入历史 -->
    <el-card v-if="importHistory.length > 0" class="history-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Clock /></el-icon>
          <span>导入历史</span>
        </div>
      </template>
      
      <el-table :data="importHistory" style="width: 100%">
        <el-table-column prop="fileName" label="导入名称" width="200"></el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
        <el-table-column prop="importTime" label="导入时间" width="180"></el-table-column>
        <el-table-column prop="recordCount" label="总记录数" width="100" align="center"></el-table-column>
        <el-table-column prop="newCount" label="新增" width="80" align="center">
          <template #default="scope">
            <el-tag type="success" size="small">{{ scope.row.newCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duplicateCount" label="重复" width="80" align="center">
          <template #default="scope">
            <el-tag type="warning" size="small">{{ scope.row.duplicateCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 设置全局确认对话框
ElMessage.confirm = ElMessageBox.confirm
import { 
  Upload, UploadFilled, Loading, DocumentAdd, Delete, Clock,
  Document, Grid, Check, CircleCheck, Lightning
} from '@element-plus/icons-vue'

const router = useRouter()
const form = reactive({})
const importMode = ref('')
const selectedFile = ref(null)
const uploading = ref(false)
const uploadRef = ref(null)
const importHistory = ref([])

// 从API加载导入历史
const loadImportHistory = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/bill/import/list?size=10')
    if (res.data.success) {
      importHistory.value = res.data.data.records.map(item => ({
        id: item.id,
        fileName: item.importName,
        fileType: item.fileType,
        importTime: item.importTime,
        recordCount: item.recordCount,
        newCount: item.newCount,
        duplicateCount: item.duplicateCount,
        status: item.importStatus === 'SUCCESS' ? 'success' : 'failed'
      }))
    }
  } catch (error) {
    console.error('加载导入历史失败', error)
  }
}

// 刷新导入历史
const saveImportHistory = async () => {
  await loadImportHistory()
}

const selectImportMode = (mode) => {
  importMode.value = mode
  clearSelection()
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const clearSelection = () => {
  selectedFile.value = null
  if (uploadRef.value && uploadRef.value.clearFiles) {
    uploadRef.value.clearFiles()
  }
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const importFile = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  const formData = new FormData()
  formData.append('file', selectedFile.value)

  const apiUrl = importMode.value === 'csv' ? '/api/cmb/import-full' : '/api/cmb/import-excel'
  
  try {
    const response = await axios.post(apiUrl, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 确保先加载历史记录
    loadImportHistory()
    
    // 检查是否有相同账号的数据需要合并
    const existingData = findExistingBillData(response.data.exportInfo.account)
    
    let importId, billData
    
    if (existingData) {
      // 合并数据
      importId = existingData.id
      const mergedRecords = mergeRecords(existingData.data, response.data.records)
      
      billData = {
        ...existingData,
        fileName: `${existingData.fileName} + ${selectedFile.value.name}`,
        importTime: `${existingData.importTime} (更新: ${new Date().toLocaleString()})`,
        data: mergedRecords,
        exportInfo: response.data.exportInfo, // 使用最新的导出信息
        summary: response.data.summaryInfo // 使用最新的统计信息
      }
    } else {
      // 新建数据
      importId = Date.now().toString()
      billData = {
        id: importId,
        fileName: selectedFile.value.name,
        importTime: new Date().toLocaleString(),
        data: response.data.records,
        exportInfo: response.data.exportInfo,
        summary: response.data.summaryInfo
      }
    }
    
    localStorage.setItem(`billData_${importId}`, JSON.stringify(billData))
    
    // 刷新导入历史
    await saveImportHistory()
    
    ElMessage.success(`${importMode.value === 'csv' ? 'CSV' : 'Excel'}账单导入成功`)
    clearSelection()
    importMode.value = ''
    
  } catch (error) {
    ElMessage.error('账单导入失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}



const goToAnalysis = (importId) => {
  // 检查数据是否存在
  const billData = localStorage.getItem(`billData_${importId}`)
  if (!billData) {
    ElMessage.error('账单数据不存在，可能已被删除')
    // 清理无效的历史记录
    cleanInvalidHistory()
    return
  }
  router.push(`/bill-analysis/${importId}`)
}

const deleteImportRecord = async (importId, index) => {
  try {
    await ElMessageBox.confirm('确定要删除这条导入记录吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`http://localhost:8080/api/bill/import/${importId}`)
    ElMessage.success('删除成功')
    loadImportHistory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}



// 查找现有的账单数据（智能匹配账号）
const findExistingBillData = (account) => {
  for (const historyItem of importHistory.value) {
    if (historyItem.status === 'success') {
      const existingData = localStorage.getItem(`billData_${historyItem.id}`)
      if (existingData) {
        const billData = JSON.parse(existingData)
        const existingAccount = billData.exportInfo?.account
        
        // 智能匹配逻辑：
        // 1. 完全匹配
        // 2. 都是招商银行相关账号（包含“招商”或“银行”）
        // 3. Excel导入与CSV导入的数据合并
        if (existingAccount === account || 
            (isRelatedAccount(existingAccount) && isRelatedAccount(account))) {
          return billData
        }
      }
    }
  }
  return null
}

// 判断是否为相关账号（招商银行相关）
const isRelatedAccount = (account) => {
  if (!account) return false
  const lowerAccount = account.toLowerCase()
  return lowerAccount.includes('招商') || 
         lowerAccount.includes('银行') || 
         lowerAccount.includes('excel') ||
         lowerAccount.includes('cmb')
}

// 合并记录（去重合并）
const mergeRecords = (existingRecords, newRecords) => {
  const merged = [...existingRecords]
  
  for (const newRecord of newRecords) {
    // 创建唯一键用于去重
    const newKey = createRecordKey(newRecord)
    
    // 检查是否已存在
    const existingIndex = merged.findIndex(record => createRecordKey(record) === newKey)
    
    if (existingIndex === -1) {
      // 新记录，直接添加
      merged.push(newRecord)
    } else {
      // 已存在的记录，保留用户的手动修改
      const existingRecord = merged[existingIndex]
      merged[existingIndex] = {
        ...newRecord,
        userRemark: existingRecord.userRemark || newRecord.userRemark,
        paymentChannel: existingRecord.paymentChannel || newRecord.paymentChannel,
        transactionType: existingRecord.transactionType || newRecord.transactionType,
        category: existingRecord.category || newRecord.category,
        excludeFromMonthly: existingRecord.excludeFromMonthly !== undefined ? existingRecord.excludeFromMonthly : newRecord.excludeFromMonthly
      }
    }
  }
  
  // 按日期排序
  return merged.sort((a, b) => {
    const dateA = new Date(a.formattedTradeDate || a.tradeDate)
    const dateB = new Date(b.formattedTradeDate || b.tradeDate)
    return dateA - dateB
  })
}



// 创建记录唯一键
const createRecordKey = (record) => {
  // 使用格式化后的日期，如果没有则使用原始日期
  const date = record.formattedTradeDate || record.tradeDate
  const time = record.tradeTime || ''
  const income = record.income ? parseFloat(record.income).toFixed(2) : '0.00'
  const expense = record.expense ? parseFloat(record.expense).toFixed(2) : '0.00'
  const type = record.tradeType || ''
  const remark = record.remark || ''
  
  return `${date}_${time}_${income}_${expense}_${type}_${remark}`
}

// 切换到简化版导入
const switchToSimple = () => {
  router.push('/bill-import-simple')
}

onMounted(() => {
  loadImportHistory()
})
</script>

<style scoped>
.bill-import {
  padding: 0;
  min-height: 100vh;
  width: 100%;
  box-sizing: border-box;
}

/* 导入指导卡片 */
.guide-card {
  margin-bottom: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 16px;
  color: white;
}

.guide-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.guide-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.guide-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 导入方式选择 */
.import-options {
  margin-bottom: 32px;
}

.import-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  border: 2px solid transparent;
  height: 160px;
}

.import-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.import-card.active {
  border-color: #409eff;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.2);
}

.import-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
  padding: 20px;
}

.import-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #f5f7fa;
}

.csv-card .import-icon {
  color: #e6a23c;
  background: #fdf6ec;
}

.excel-card .import-icon {
  color: #67c23a;
  background: #f0f9ff;
}

.import-info {
  flex: 1;
}

.import-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.import-info p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.import-features {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.import-status {
  flex-shrink: 0;
}

.selected-icon {
  color: #409eff;
  font-size: 24px;
}

/* 文件上传区域 */
.upload-card {
  border-radius: 12px;
  overflow: hidden;
}

.upload-area {
  margin-bottom: 24px;
}

.file-uploader {
  width: 100%;
}

.file-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  background: #fafbfc;
  transition: all 0.3s ease;
}

.file-uploader :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: #f0f9ff;
}

.file-uploader.has-file :deep(.el-upload-dragger) {
  border-color: #67c23a;
  background: #f0f9ff;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 24px;
}

.upload-icon {
  margin-bottom: 16px;
  color: #c0c4cc;
}

.success-icon {
  color: #67c23a;
}

.upload-text {
  text-align: center;
}

.upload-text h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #606266;
  font-weight: 500;
}

.file-selected {
  color: #67c23a !important;
}

.upload-text p {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.4;
}

.upload-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: #fafbfc;
  border-top: 1px solid #ebeef5;
}

/* 历史记录 */
.history-card {
  margin-top: 32px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.header-icon {
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guide-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .import-options .el-col {
    margin-bottom: 16px;
  }
  
  .import-card-content {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .upload-actions {
    flex-direction: column;
  }
}
</style>