<template>
  <div class="bill-import">
    <!-- 页面标题和切换按钮 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="header-text">
          <h2>快速导入</h2>
          <p>简化版账单导入，支持同时上传CSV和Excel文件</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="switchToStandard" :icon="Expand">
            切换到标准版
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="upload-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Upload /></el-icon>
          <span>招商银行账单导入</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="CSV文件">
            <el-upload
                ref="uploadRef"
                class="upload-demo"
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-exceed="handleExceed"
                :limit="1"
                accept=".csv"
                drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将CSV文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">支持招商银行CSV格式</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Excel文件">
            <el-upload
                ref="excelUploadRef"
                class="upload-demo"
                :auto-upload="false"
                :on-change="handleExcelFileChange"
                :on-exceed="handleExceed"
                :limit="1"
                accept=".xlsx,.xls"
                drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                导入之前导出的Excel文件
              </div>
              <template #tip>
                <div class="el-upload__tip">支持系统导出Excel文件的重新导入</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>

      <div class="button-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-button
                type="primary"
                @click="importFile"
                :loading="uploading"
                :disabled="!selectedFile"
                :icon="uploading ? Loading : DocumentAdd"
                style="width: 100%;"
            >
              {{ uploading ? '导入中...' : '导入CSV账单' }}
            </el-button>
          </el-col>
          <el-col :span="12">
            <el-button
                type="success"
                @click="importExcelFile"
                :loading="excelUploading"
                :disabled="!selectedExcelFile"
                :icon="excelUploading ? Loading : DocumentAdd"
                style="width: 100%;"
            >
              {{ excelUploading ? '导入中...' : '导入Excel账单' }}
            </el-button>
          </el-col>
        </el-row>
        <div class="clear-button">
          <el-button @click="clearForm" :icon="Delete">清空所有</el-button>
        </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 设置全局确认对话框
ElMessage.confirm = ElMessageBox.confirm
import {
  Upload, UploadFilled, Loading, DocumentAdd, Delete, Clock, Expand
} from '@element-plus/icons-vue'

const router = useRouter()

const selectedFile = ref(null)
const selectedExcelFile = ref(null)
const uploading = ref(false)
const excelUploading = ref(false)
const uploadRef = ref(null)
const excelUploadRef = ref(null)
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



const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleExcelFileChange = (file) => {
  selectedExcelFile.value = file.raw
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

  try {
    const response = await axios.post('/api/cmb/import-csv', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // 确保先加载历史记录
    loadImportHistory()

    ElMessage.success('账单导入成功')
    clearForm()

  } catch (error) {
    ElMessage.error('账单导入失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const importExcelFile = async () => {
  if (!selectedExcelFile.value) {
    ElMessage.warning('请选择Excel文件')
    return
  }

  excelUploading.value = true
  const formData = new FormData()
  formData.append('file', selectedExcelFile.value)

  try {
    const response = await axios.post('/api/cmb/import-excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // 确保先加载历史记录
    loadImportHistory()

    ElMessage.success('Excel账单导入成功')
    clearForm()

  } catch (error) {
    ElMessage.error('Excel账单导入失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    excelUploading.value = false
  }
}

const clearForm = () => {
  selectedFile.value = null
  selectedExcelFile.value = null
  if (uploadRef.value && uploadRef.value.clearFiles) {
    uploadRef.value.clearFiles()
  }
  if (excelUploadRef.value && excelUploadRef.value.clearFiles) {
    excelUploadRef.value.clearFiles()
  }
}



// 切换到标准版导入
const switchToStandard = () => {
  router.push('/bill-import')
}

onMounted(() => {
  loadImportHistory()
})
</script>

<style scoped>
.bill-import {
  padding: 20px;
}

.upload-card, .history-card {
  margin-bottom: 20px;
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

.el-upload__tip {
  margin-top: 10px;
}

.button-container {
  margin-top: 20px;
}

.clear-button {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

/* 头部样式 */
.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.header-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>