import { createRouter, createWebHistory } from 'vue-router'
import CmbBillImport from '../views/CmbBillImport.vue'
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
    path: '/cmb-bill',
    name: 'CmbBill',
    component: CmbBillImport,
    meta: { title: '招商银行账单' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router