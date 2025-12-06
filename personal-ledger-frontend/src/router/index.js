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
import TagManagement from '../views/TagManagement.vue'
import TransactionList from '../views/TransactionList.vue'

import MonthlyBalance from '../views/MonthlyBalance.vue'
import CumulativeBalance from '../views/CumulativeBalance.vue'
import LargeTransactionAnalysis from '../views/LargeTransactionAnalysis.vue'
import FrequentTransactionAnalysis from '../views/FrequentTransactionAnalysis.vue'
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
    path: '/tag-management',
    name: 'TagManagement',
    component: TagManagement,
    meta: { title: '标签管理' }
  },

  {
    path: '/transaction-list',
    name: 'TransactionList',
    component: TransactionList,
    meta: { title: '交易记录' }
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
    path: '/monthly-balance',
    name: 'MonthlyBalance',
    component: MonthlyBalance,
    meta: { title: '月度结余' }
  },
  {
    path: '/cumulative-balance',
    name: 'CumulativeBalance',
    component: CumulativeBalance,
    meta: { title: '累计结余' }
  },
  {
    path: '/large-transaction-analysis',
    name: 'LargeTransactionAnalysis',
    component: LargeTransactionAnalysis,
    meta: { title: '大额交易分析' }
  },
  {
    path: '/frequent-transaction-analysis',
    name: 'FrequentTransactionAnalysis',
    component: FrequentTransactionAnalysis,
    meta: { title: '高频交易分析' }
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