import { createRouter, createWebHistory } from 'vue-router'
import BillImport from '../views/BillImport.vue'
import BillAnalysis from '../views/BillAnalysis.vue'
import Dashboard from '../views/Dashboard.vue'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { title: '仪表盘' }
  },
  {
    path: '/bill-import',
    name: 'BillImport',
    component: BillImport,
    meta: { title: '账单导入' }
  },
  {
    path: '/bill-analysis/:id',
    name: 'BillAnalysis',
    component: BillAnalysis,
    meta: { title: '账单解析' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router