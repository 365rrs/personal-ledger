import { createRouter, createWebHistory } from 'vue-router'
import BillImport from '../views/BillImport.vue'
import BillImportSimple from '../views/BillImportSimple.vue'
import BillImportHistory from '../views/BillImportHistory.vue'
import DataAnalysis from '../views/DataAnalysis.vue'
import CategoryAnalysis from '../views/CategoryAnalysis.vue'
import Dashboard from '../views/Dashboard.vue'
import RuleManagement from '../views/RuleManagement.vue'
import CategoryManagement from '../views/CategoryManagement.vue'
import PaymentChannelManagement from '../views/PaymentChannelManagement.vue'
import TransactionList from '../views/TransactionList.vue'
import ManualEntry from '../views/ManualEntry.vue'
import MonthlyComparison from '../views/MonthlyComparison.vue'
import ConsumptionHabits from '../views/ConsumptionHabits.vue'

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
    path: '/bill-import-history',
    name: 'BillImportHistory',
    component: BillImportHistory,
    meta: { title: '导入历史' }
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
  },

  {
    path: '/transaction-list',
    name: 'TransactionList',
    component: TransactionList,
    meta: { title: '交易记录' }
  },
  {
    path: '/manual-entry',
    name: 'ManualEntry',
    component: ManualEntry,
    meta: { title: '手动记账' }
  },
  {
    path: '/data-analysis',
    name: 'DataAnalysis',
    component: DataAnalysis,
    meta: { title: '按天统计' }
  },
  {
    path: '/category-analysis',
    name: 'CategoryAnalysis',
    component: CategoryAnalysis,
    meta: { title: '按分类统计' }
  },
  {
    path: '/monthly-comparison',
    name: 'MonthlyComparison',
    component: MonthlyComparison,
    meta: { title: '月度对比' }
  },
  {
    path: '/consumption-habits',
    name: 'ConsumptionHabits',
    component: ConsumptionHabits,
    meta: { title: '消费习惯' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router