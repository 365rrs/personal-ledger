import { reactive, readonly } from 'vue'

// 账单数据状态
const state = reactive({
  currentBillData: null,
  currentImportId: null
})

// 从 localStorage 加载账单数据
const loadBillData = (importId) => {
  if (!importId) {
    // 加载最新的账单数据
    const history = localStorage.getItem('billImportHistory')
    if (history) {
      const importHistory = JSON.parse(history)
      const successfulImports = importHistory.filter(item => item.status === 'success')
      if (successfulImports.length > 0) {
        const latestImport = successfulImports[0]
        importId = latestImport.id
      }
    }
  }
  
  if (importId) {
    const data = localStorage.getItem(`billData_${importId}`)
    if (data) {
      state.currentBillData = JSON.parse(data)
      state.currentImportId = importId
      return true
    }
  }
  
  return false
}

// 保存账单数据到 localStorage
const saveBillData = () => {
  if (state.currentBillData && state.currentImportId) {
    localStorage.setItem(`billData_${state.currentImportId}`, JSON.stringify(state.currentBillData))
    return true
  }
  return false
}

// 更新账单数据
const updateBillData = (data) => {
  state.currentBillData = data
  saveBillData()
}

// 更新单条记录
const updateRecord = (record) => {
  if (!state.currentBillData?.data) return false
  
  const index = state.currentBillData.data.findIndex(item => 
    item.formattedTradeDate === record.formattedTradeDate &&
    item.tradeTime === record.tradeTime &&
    item.income === record.income &&
    item.expense === record.expense
  )
  
  if (index !== -1) {
    state.currentBillData.data[index] = { ...record }
    saveBillData()
    return true
  }
  
  return false
}

// 清空当前数据
const clearBillData = () => {
  state.currentBillData = null
  state.currentImportId = null
}

// 获取当前账单数据
const getBillData = () => {
  return state.currentBillData
}

// 获取当前导入ID
const getImportId = () => {
  return state.currentImportId
}

export default {
  state: readonly(state),
  loadBillData,
  saveBillData,
  updateBillData,
  updateRecord,
  clearBillData,
  getBillData,
  getImportId
}
