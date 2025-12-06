<template>
  <div class="category-analysis">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>按分类统计</span>
          <el-space>
            <el-date-picker v-model="startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" @change="loadData" />
            <el-date-picker v-model="endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" @change="loadData" />
          </el-space>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        </el-col>
        <el-col :span="12">
          <el-table :data="categoryData" style="width: 100%" @row-click="loadCategoryDetails">
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="income" label="收入" align="right" width="100">
              <template #default="{ row }">
                <span style="color: #67c23a;">{{ row.income }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="expense" label="支出" align="right" width="100">
              <template #default="{ row }">
                <span style="color: #f56c6c;">{{ row.expense }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="结余" align="right" width="100">
              <template #default="{ row }">
                <span :style="{ color: parseFloat(row.balance) >= 0 ? '#67c23a' : '#f56c6c' }">{{ row.balance }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="笔数" align="center" width="80" />
          </el-table>
        </el-col>
      </el-row>

      <!-- 分类明细 -->
      <div v-if="selectedCategory" style="margin-top: 20px;">
        <div style="margin-bottom: 10px;">
          <el-tag type="primary" closable @close="selectedCategory = ''">{{ selectedCategory }}</el-tag>
        </div>
        <el-table 
          :data="categoryDetails" 
          max-height="400"
          @sort-change="handleSortChange"
        >
          <el-table-column prop="transactionDate" label="交易日期" width="110" sortable="custom" />
          <el-table-column prop="transactionTime" label="交易时间" width="120" sortable="custom" />
          <el-table-column prop="income" label="收入" width="100" sortable="custom">
            <template #default="{ row }">
              <span style="color: #67c23a;">{{ row.income || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="expense" label="支出" width="100" sortable="custom">
            <template #default="{ row }">
              <span style="color: #f56c6c;">{{ row.expense || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="transactionType" label="交易类型" width="140" />
          <el-table-column prop="paymentChannel" label="支付渠道" width="120" />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="description" label="交易备注" min-width="180" show-overflow-tooltip />
          <el-table-column prop="userNote" label="用户备注" width="150" show-overflow-tooltip />
          <el-table-column prop="includeInStats" label="计入收支" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.includeInStats ? 'success' : 'info'" size="small">
                {{ row.includeInStats ? '是' : '否' }}
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
          <el-switch v-model="currentRecord.includeInStats" active-text="是" inactive-text="否" />
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
const startDate = ref('')
const endDate = ref('')
const categoryData = ref([])
const selectedCategory = ref('')
const categoryDetails = ref([])
const drawerVisible = ref(false)
const currentRecord = ref(null)
const categories = ref([])
const channels = ref([])
let chart = null

// 排序字段和顺序
const sortField = ref('')
const sortOrder = ref('')

const loadData = async () => {
  try {
    const params = {
      size: 10000,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
      includeInStats: true
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    const transactions = res.data.data?.records || []
    
    // 按分类聚合数据
    const categoryMap = {}
    transactions.forEach(t => {
      const cat = t.category || '未分类'
      if (!categoryMap[cat]) {
        categoryMap[cat] = { income: 0, expense: 0, count: 0 }
      }
      categoryMap[cat].income += parseFloat(t.income || 0)
      categoryMap[cat].expense += parseFloat(t.expense || 0)
      categoryMap[cat].count++
    })
    
    categoryData.value = Object.entries(categoryMap).map(([category, data]) => ({
      category,
      income: data.income.toFixed(2),
      expense: data.expense.toFixed(2),
      balance: (data.income - data.expense).toFixed(2),
      count: data.count
    })).sort((a, b) => parseFloat(b.expense) - parseFloat(a.expense))
    
    renderChart(categoryData.value)
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

const renderChart = (data) => {
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const option = {
    title: { text: '支出分类占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.map(d => ({ name: d.category, value: parseFloat(d.expense) })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }

  chart.setOption(option)
  
  // 添加点击事件
  chart.off('click')
  chart.on('click', (params) => {
    if (params.componentType === 'series') {
      loadCategoryDetails({ category: params.name })
    }
  })
}

const loadCategoryDetails = async (row) => {
  selectedCategory.value = row.category
  try {
    const params = {
      size: 10000,
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
      category: row.category === '未分类' ? '' : row.category,
      includeInStats: true,
      sortField: sortField.value,
      sortOrder: sortOrder.value
    }
    
    const res = await axios.get('http://localhost:8080/api/bill/transaction/list', { params })
    const transactions = res.data.data?.records || []
    
    categoryDetails.value = transactions
  } catch (error) {
    ElMessage.error('加载明细失败')
  }
}

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop || ''
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  if (selectedCategory.value) {
    loadCategoryDetails({ category: selectedCategory.value })
  }
}

const editRecord = (record) => {
  currentRecord.value = { ...record }
  drawerVisible.value = true
}

const saveRecord = async () => {
  try {
    await axios.put(`http://localhost:8080/api/bill/transaction/${currentRecord.value.id}`, {
      ...currentRecord.value,
      category: currentRecord.value.category,
      paymentChannel: currentRecord.value.paymentChannel,
      userNote: currentRecord.value.userNote,
      includeInStats: currentRecord.value.includeInStats
    })
    ElMessage.success('保存成功')
    drawerVisible.value = false
    loadData()
    if (selectedCategory.value) {
      loadCategoryDetails({ category: selectedCategory.value })
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
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  startDate.value = `${year}-${month}-01`
  endDate.value = `${year}-${month}-${new Date(year, now.getMonth() + 1, 0).getDate()}`
  loadCategories()
  loadChannels()
  loadData()
})
</script>

<style scoped>
.category-analysis {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>