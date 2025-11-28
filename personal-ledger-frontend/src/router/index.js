import { createRouter, createWebHistory } from 'vue-router'
import BillImport from '../views/BillImport.vue'
import BillImportSimple from '../views/BillImportSimple.vue'
import BillAnalysis from '../views/BillAnalysis.vue'
import DataAnalysis from '../views/DataAnalysis.vue'
import CategoryAnalysis from '../views/CategoryAnalysis.vue'
import Dashboard from '../views/Dashboard.vue'
import RuleManagement from '../views/RuleManagement.vue'
import CategoryManagement from '../views/CategoryManagement.vue'
import PaymentChannelManagement from '../views/PaymentChannelManagement.vue'

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
    path: '/bill-import-simple',
    name: 'BillImportSimple',
    component: BillImportSimple,
    meta: { title: '快速导入' }
  },
  {
    path: '/bill-analysis/:id',
    name: 'BillAnalysis',
    component: BillAnalysis,
    meta: { title: '账单解析' }
  },
  {
    path: '/bill-analysis',
    name: 'BillAnalysisList',
    component: BillAnalysis,
    meta: { title: '账单解析' }
  },
  {
    path: '/data-analysis/:id',
    name: 'DataAnalysis',
    component: DataAnalysis,
    meta: { title: '数据分析' }
  },
  {
    path: '/data-analysis',
    name: 'DataAnalysisList',
    component: DataAnalysis,
    meta: { title: '数据分析' }
  },
  {
    path: '/category-analysis/:id',
    name: 'CategoryAnalysis',
    component: CategoryAnalysis,
    meta: { title: '分类统计' }
  },
  {
    path: '/category-analysis',
    name: 'CategoryAnalysisList',
    component: CategoryAnalysis,
    meta: { title: '分类统计' }
  },
  {
    path: '/rule-management',
    name: 'RuleManagement',
    component: RuleManagement,
    meta: { title: '规则管理' }
  },
  {
    path: '/category-management',
    name: 'CategoryManagement',
    component: CategoryManagement,
    meta: { title: '分类管理' }
  },
  {
    path: '/payment-channel-management',
    name: 'PaymentChannelManagement',
    component: PaymentChannelManagement,
    meta: { title: '支付渠道管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router