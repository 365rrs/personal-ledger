<template>
  <div class="rule-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Setting /></el-icon>
            <span>清洗规则管理</span>
          </div>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">添加规则</el-button>
        </div>
      </template>

      <el-radio-group v-model="ruleType" class="type-filter" @change="loadRules">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PAYMENT_CHANNEL">支付渠道</el-radio-button>
        <el-radio-button value="CATEGORY">分类识别</el-radio-button>
        <el-radio-button value="REMARK_CLEANING">备注清洗</el-radio-button>
      </el-radio-group>

      <el-table :data="rules" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #header>
            <span>规则类型</span>
            <el-tooltip content="规则应用的场景：支付渠道识别、分类识别、备注清洗" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="scope">
            <el-tag v-if="scope.row.ruleType === 'PAYMENT_CHANNEL'" type="success">支付渠道</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'CATEGORY'" type="primary">分类识别</el-tag>
            <el-tag v-else-if="scope.row.ruleType === 'REMARK_CLEANING'" type="warning">备注清洗</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keyword" label="匹配关键词" min-width="150">
          <template #header>
            <span>匹配关键词</span>
            <el-tooltip content="用于匹配交易备注或交易类型的关键词" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="targetValue" label="目标值" min-width="120">
          <template #header>
            <span>目标值</span>
            <el-tooltip content="匹配成功后设置的值，如：微信、餐饮、出行等" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="matchMode" label="匹配模式" width="100">
          <template #header>
            <span>匹配模式</span>
            <el-tooltip content="精确：完全相等；包含：包含关键词；正则：正则表达式" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="scope">
            <span v-if="scope.row.matchMode === 'EXACT'">精确</span>
            <span v-else-if="scope.row.matchMode === 'CONTAINS'">包含</span>
            <span v-else-if="scope.row.matchMode === 'REGEX'">正则</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" sortable>
          <template #header>
            <span>优先级</span>
            <el-tooltip content="数字越大优先级越高，多个规则匹配时优先使用高优先级规则" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #header>
            <span>状态</span>
            <el-tooltip content="开启后规则生效，关闭后规则不生效" placement="top">
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" @change="toggleRule(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-space :size="5">
              <el-button type="primary" size="small" @click="editRule(scope.row)" :icon="Edit">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteRule(scope.row)" :icon="Delete">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '添加规则' : '编辑规则'" width="600px">
      <el-form :model="currentRule" label-width="120px">
        <el-form-item label="规则类型">
          <el-select v-model="currentRule.ruleType" placeholder="请选择规则类型">
            <el-option label="支付渠道" value="PAYMENT_CHANNEL"></el-option>
            <el-option label="分类识别" value="CATEGORY"></el-option>
            <el-option label="备注清洗" value="REMARK_CLEANING"></el-option>
          </el-select>
          <div class="form-tip">选择规则应用的场景</div>
        </el-form-item>
        <el-form-item label="匹配关键词">
          <el-input v-model="currentRule.keyword" placeholder="如：微信、星巴克、滴滴"></el-input>
          <div class="form-tip">用于匹配交易备注或交易类型的关键词</div>
        </el-form-item>
        <el-form-item label="目标值">
          <el-select 
            v-if="currentRule.ruleType === 'PAYMENT_CHANNEL'" 
            v-model="currentRule.targetValue" 
            placeholder="请选择支付渠道" 
            clearable 
            filterable
            allow-create
          >
            <el-option v-for="channel in paymentChannelOptions" :key="channel" :label="channel" :value="channel" />
          </el-select>
          <el-select 
            v-else-if="currentRule.ruleType === 'CATEGORY'" 
            v-model="currentRule.targetValue" 
            placeholder="请选择分类" 
            clearable 
            filterable
            allow-create
          >
            <el-option v-for="cat in categoryOptions" :key="cat" :label="cat" :value="cat" />
          </el-select>
          <el-input v-else v-model="currentRule.targetValue" placeholder="如：微信、餐饮、出行"></el-input>
          <div class="form-tip">匹配成功后设置的值</div>
        </el-form-item>
        <el-form-item label="匹配模式">
          <el-select v-model="currentRule.matchMode" placeholder="请选择匹配模式">
            <el-option label="精确匹配" value="EXACT"></el-option>
            <el-option label="包含匹配" value="CONTAINS"></el-option>
            <el-option label="正则表达式" value="REGEX"></el-option>
          </el-select>
          <div class="form-tip">推荐使用"包含匹配"，适用于大多数场景</div>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="currentRule.priority" :min="1" :max="100"></el-input-number>
          <div class="form-tip">数字越大优先级越高（1-100），多个规则匹配时优先使用高优先级</div>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="currentRule.enabled"></el-switch>
          <div class="form-tip">开启后规则立即生效</div>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="currentRule.description" type="textarea" :rows="3" placeholder="可选，用于记录规则用途"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus, Edit, Delete, QuestionFilled } from '@element-plus/icons-vue'

const rules = ref([])
const ruleType = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentRule = ref({
  ruleType: '',
  keyword: '',
  targetValue: '',
  matchMode: 'CONTAINS',
  priority: 50,
  enabled: true,
  description: ''
})

const categoryOptions = ref([])
const paymentChannelOptions = ref([])

const loadCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/category/list')
    categoryOptions.value = response.data.map(c => c.name)
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadPaymentChannels = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/payment-channel/list')
    paymentChannelOptions.value = response.data.map(c => c.name)
  } catch (error) {
    console.error('加载支付渠道失败', error)
  }
}

const loadRules = async () => {
  try {
    const url = ruleType.value 
      ? `/api/cleaning-rules/type/${ruleType.value}` 
      : '/api/cleaning-rules'
    const response = await axios.get(url)
    rules.value = response.data
  } catch (error) {
    ElMessage.error('加载规则失败: ' + (error.message || '未知错误'))
  }
}

const showAddDialog = () => {
  dialogMode.value = 'add'
  currentRule.value = {
    ruleType: '',
    keyword: '',
    targetValue: '',
    matchMode: 'CONTAINS',
    priority: 50,
    enabled: true,
    description: ''
  }
  dialogVisible.value = true
}

const editRule = (rule) => {
  dialogMode.value = 'edit'
  currentRule.value = { ...rule }
  dialogVisible.value = true
}

const saveRule = async () => {
  try {
    if (dialogMode.value === 'add') {
      await axios.post('/api/cleaning-rules', currentRule.value)
      ElMessage.success('添加规则成功')
    } else {
      await axios.put(`/api/cleaning-rules/${currentRule.value.id}`, currentRule.value)
      ElMessage.success('更新规则成功')
    }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    ElMessage.error('保存规则失败: ' + (error.message || '未知错误'))
  }
}

const deleteRule = async (rule) => {
  try {
    await ElMessageBox.confirm('确定要删除这条规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await axios.delete(`/api/cleaning-rules/${rule.id}`)
    ElMessage.success('删除规则成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除规则失败: ' + (error.message || '未知错误'))
    }
  }
}

const toggleRule = async (rule) => {
  try {
    await axios.put(`/api/cleaning-rules/${rule.id}/toggle?enabled=${rule.enabled}`)
    ElMessage.success(rule.enabled ? '已启用规则' : '已禁用规则')
  } catch (error) {
    ElMessage.error('切换规则状态失败: ' + (error.message || '未知错误'))
    rule.enabled = !rule.enabled
  }
}

onMounted(() => {
  loadRules()
  loadCategories()
  loadPaymentChannels()
})
</script>

<style scoped>
.rule-management {
  padding: 0;
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
  gap: 10px;
}

.header-icon {
  font-size: 18px;
}

.type-filter {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.5;
}
</style>
