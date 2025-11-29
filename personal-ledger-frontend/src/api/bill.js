import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api/bill'

/**
 * 账单导入API
 */
export const billImportApi = {
  // CSV导入
  importCsv(file) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post(`${API_BASE_URL}/import/csv`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // Excel导入
  importExcel(file) {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post(`${API_BASE_URL}/import/excel`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取导入历史列表
  getImportList(current = 1, size = 10) {
    return axios.get(`${API_BASE_URL}/import/list`, {
      params: { current, size }
    })
  },

  // 获取导入详情
  getImportDetail(id) {
    return axios.get(`${API_BASE_URL}/import/${id}`)
  },

  // 删除导入记录
  deleteImport(id) {
    return axios.delete(`${API_BASE_URL}/import/${id}`)
  }
}

/**
 * 账单交易API
 */
export const billTransactionApi = {
  // 获取交易列表
  getTransactionList(params) {
    return axios.get(`${API_BASE_URL}/transaction/list`, { params })
  },

  // 获取交易详情
  getTransactionDetail(id) {
    return axios.get(`${API_BASE_URL}/transaction/${id}`)
  },

  // 更新交易
  updateTransaction(id, data) {
    return axios.put(`${API_BASE_URL}/transaction/${id}`, data)
  },

  // 删除交易
  deleteTransaction(id) {
    return axios.delete(`${API_BASE_URL}/transaction/${id}`)
  }
}
