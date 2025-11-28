import axios from 'axios'

/**
 * 获取后端支持的分类列表
 */
export async function getCategories() {
  try {
    const response = await axios.get('/api/cmb/categories')
    return response.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
    return []
  }
}
