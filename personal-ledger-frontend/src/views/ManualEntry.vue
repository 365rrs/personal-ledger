<template>
  <div class="manual-entry">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>手动记账</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="交易日期" prop="transactionDate">
          <el-date-picker v-model="form.transactionDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="交易时间" prop="transactionTime">
          <el-time-picker v-model="form.transactionTime" placeholder="选择时间" value-format="HH:mm:ss" />
        </el-form-item>

        <el-form-item label="收支类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="income">收入</el-radio>
            <el-radio label="expense">支出</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="金额" prop="amount">
          <el-input v-model="form.amount" placeholder="请输入金额" type="number" step="0.01">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" filterable placeholder="请选择分类">
            <el-option v-for="cat in filteredCategories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>

        <el-form-item label="支付渠道" prop="paymentChannel">
          <el-select v-model="form.paymentChannel" filterable placeholder="请选择支付渠道">
            <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
          </el-select>
        </el-form-item>

        <el-form-item label="交易类型">
          <el-input v-model="form.transactionType" placeholder="如：转账、消费、退款等" />
        </el-form-item>

        <el-form-item label="交易描述">
          <el-input v-model="form.description" placeholder="交易描述" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="用户备注">
          <el-input v-model="form.userNote" placeholder="添加备注信息" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="计入收支">
          <el-switch v-model="form.includeInStats" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item label="是否退款">
          <el-switch v-model="form.isRefund" active-text="是" inactive-text="否" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const channels = ref([])

const form = reactive({
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
})

const rules = {
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }],
  transactionTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }],
  type: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { pattern: /^\d+(\.\d{1,2})?$/, message: '请输入有效金额', trigger: 'blur' }
  ]
}

const filteredCategories = computed(() => {
  const typeMap = { income: 'INCOME', expense: 'EXPENSE' }
  return categories.value.filter(cat => cat.type === typeMap[form.type])
})

const loadCategories = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/category/list')
    categories.value = res.data.success ? res.data.data : res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadChannels = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/payment-channel/list')
    channels.value = res.data.success ? res.data.data : res.data
  } catch (error) {
    console.error('加载支付渠道失败:', error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        transactionDate: form.transactionDate,
        transactionTime: form.transactionTime,
        income: form.type === 'income' ? form.amount : '0',
        expense: form.type === 'expense' ? form.amount : '0',
        category: form.category,
        paymentChannel: form.paymentChannel,
        transactionType: form.transactionType,
        description: form.description,
        userNote: form.userNote,
        includeInStats: form.includeInStats,
        isRefund: form.isRefund,
        isManualEntry: true
      }
      
      const res = await axios.post('http://localhost:8080/api/bill/transaction', data)
      if (res.data.success) {
        ElMessage.success('记账成功')
        resetForm()
      }
    } catch (error) {
      ElMessage.error('记账失败：' + (error.response?.data?.message || error.message))
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.transactionDate = new Date().toISOString().split('T')[0]
  form.transactionTime = new Date().toTimeString().split(' ')[0]
  form.type = 'expense'
  form.includeInStats = true
  form.isRefund = false
}

onMounted(() => {
  loadCategories()
  loadChannels()
})
</script>

<style scoped>
.manual-entry {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
