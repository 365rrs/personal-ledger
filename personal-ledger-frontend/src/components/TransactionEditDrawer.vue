<template>
  <el-drawer v-model="visible" :title="getDrawerTitle()" size="500px" @close="handleClose">
    <el-form :model="formData" :rules="drawerMode === 'manual' ? manualRules : {}" ref="formRef" label-width="100px" :disabled="drawerMode === 'view'">
      <el-form-item label="交易日期" :prop="drawerMode === 'manual' ? 'transactionDate' : ''">
        <el-date-picker v-if="drawerMode === 'manual'" v-model="formData.transactionDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        <el-input v-else v-model="formData.transactionDate" readonly />
      </el-form-item>
      <el-form-item label="交易时间" :prop="drawerMode === 'manual' ? 'transactionTime' : ''">
        <el-time-picker v-if="drawerMode === 'manual'" v-model="formData.transactionTime" placeholder="选择时间" value-format="HH:mm:ss" style="width: 100%" />
        <el-input v-else v-model="formData.transactionTime" readonly />
      </el-form-item>
      <el-form-item v-if="drawerMode === 'manual'" label="收支类型" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio label="income">收入</el-radio>
          <el-radio label="expense">支出</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="drawerMode === 'manual'" label="金额" prop="amount">
        <el-input v-model="formData.amount" placeholder="请输入金额" type="number" step="0.01">
          <template #prepend>¥</template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="drawerMode !== 'manual'" label="收入">
        <el-input v-model="formData.income" readonly />
      </el-form-item>
      <el-form-item v-if="drawerMode !== 'manual'" label="支出">
        <el-input v-model="formData.expense" readonly />
      </el-form-item>
      <el-form-item label="交易类型">
        <el-input v-model="formData.transactionType" :placeholder="drawerMode === 'manual' ? '如：转账、消费、退款等' : ''" :readonly="drawerMode === 'view'" />
      </el-form-item>
      <el-form-item label="交易描述">
        <el-input v-model="formData.description" type="textarea" :placeholder="drawerMode === 'manual' ? '交易描述' : ''" :readonly="drawerMode === 'view'" />
      </el-form-item>
      <el-form-item label="分类">
        <el-cascader v-model="formData.category" :options="filteredCategoryTree" :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }" clearable filterable :placeholder="drawerMode === 'manual' ? '请选择分类' : ''" style="width: 100%" :disabled="drawerMode === 'view'" />
      </el-form-item>
      <el-form-item label="支付渠道">
        <el-select v-model="formData.paymentChannel" filterable :placeholder="drawerMode === 'manual' ? '请选择支付渠道' : ''">
          <el-option v-for="ch in channels" :key="ch.id" :label="ch.name" :value="ch.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户备注">
        <el-input v-model="formData.userNote" type="textarea" :rows="3" :placeholder="drawerMode === 'manual' ? '添加备注信息' : ''" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="formData.tagIds" multiple filterable placeholder="选择标签" style="width: 100%" :disabled="drawerMode === 'view'">
          <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id">
            <span style="display: flex; align-items: center; gap: 8px;">
              <span :style="{ width: '12px', height: '12px', borderRadius: '2px', backgroundColor: tag.color }"></span>
              <span>{{ tag.name }}</span>
            </span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="计入收支">
        <el-switch v-model="formData.includeInStats" active-text="是" inactive-text="否" />
      </el-form-item>
      <el-form-item label="是否退款">
        <el-switch v-model="formData.isRefund" active-text="是" inactive-text="否" />
      </el-form-item>
    </el-form>
    <template #footer v-if="drawerMode !== 'view'">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="submitting">保存</el-button>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  modelValue: Boolean,
  mode: String, // 'view', 'edit', 'manual'
  record: Object,
  categories: Array,
  channels: Array,
  allTags: Array
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const drawerMode = computed(() => props.mode)
const formData = ref({})
const formRef = ref(null)
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

const filteredCategoryTree = computed(() => {
  if (drawerMode.value === 'view') return props.categories
  if (!formData.value.type) return props.categories
  const typeMap = { income: 'INCOME', expense: 'EXPENSE' }
  return props.categories.filter(cat => cat.type === typeMap[formData.value.type])
})

const getDrawerTitle = () => {
  if (drawerMode.value === 'view') return '查看交易'
  if (drawerMode.value === 'edit') return '编辑交易'
  if (drawerMode.value === 'manual') return '手动记账'
  return '交易记录'
}

const handleClose = () => {
  formData.value = {}
}

const handleSave = async () => {
  if (drawerMode.value === 'manual') {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      submitting.value = true
      try {
        const data = {
          transactionDate: formData.value.transactionDate,
          transactionTime: formData.value.transactionTime,
          income: formData.value.type === 'income' ? formData.value.amount : null,
          expense: formData.value.type === 'expense' ? formData.value.amount : null,
          category: formData.value.category,
          paymentChannel: formData.value.paymentChannel,
          transactionType: formData.value.transactionType,
          description: formData.value.description,
          userNote: formData.value.userNote,
          includeInStats: formData.value.includeInStats,
          isRefund: formData.value.isRefund,
          isManualEntry: true
        }
        const result = await axios.post('/api/bill/transaction', data)
        if (formData.value.tagIds && formData.value.tagIds.length > 0) {
          await axios.post('/api/bill/tag/bind', {
            transactionId: result.data.data,
            tagIds: formData.value.tagIds
          })
        }
        ElMessage.success('记账成功')
        visible.value = false
        emit('saved')
      } catch (error) {
        ElMessage.error('记账失败：' + (error.response?.data?.message || error.message))
      } finally {
        submitting.value = false
      }
    })
  } else {
    try {
      const selectedCategory = props.categories.flat().find(c => c.id === formData.value.category)
      const updateData = { ...formData.value }
      if (selectedCategory) {
        if (selectedCategory.parentId === null || selectedCategory.parentId === 0) {
          updateData.category = selectedCategory.name
          updateData.subCategory = null
        } else {
          const parent = props.categories.flat().find(c => c.id === selectedCategory.parentId)
          if (parent) {
            updateData.category = parent.name
            updateData.subCategory = selectedCategory.name
          }
        }
      }
      await axios.put(`/api/bill/transaction/${formData.value.id}`, updateData)
      await axios.post('/api/bill/tag/bind', {
        transactionId: formData.value.id,
        tagIds: formData.value.tagIds || []
      })
      ElMessage.success('保存成功')
      visible.value = false
      emit('saved')
    } catch (error) {
      ElMessage.error('保存失败: ' + error.message)
    }
  }
}

watch(() => props.record, async (newRecord) => {
  if (newRecord) {
    formData.value = { ...newRecord }
    if (drawerMode.value === 'edit' || drawerMode.value === 'view') {
      formData.value.type = newRecord.income && parseFloat(newRecord.income) > 0 ? 'income' : 'expense'
      if (newRecord.subCategory) {
        const subCat = props.categories.flat().find(c => c.name === newRecord.subCategory && c.parentId)
        if (subCat) formData.value.category = subCat.id
      } else if (newRecord.category) {
        const parentCat = props.categories.flat().find(c => c.name === newRecord.category && (c.parentId === null || c.parentId === 0))
        if (parentCat) formData.value.category = parentCat.id
      }
      try {
        const tagIds = await axios.get(`/api/bill/tag/transaction/${newRecord.id}`)
        formData.value.tagIds = tagIds.data
      } catch (error) {
        formData.value.tagIds = []
      }
    }
  }
}, { immediate: true })
</script>
