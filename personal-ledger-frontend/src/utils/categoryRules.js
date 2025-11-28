import { getCategories } from '../api/category'

// 预设分类模板（从后端获取后会被替换）
export const CATEGORY_TEMPLATES = {
  expense: [],
  income: []
}

// 从后端加载分类列表
export async function loadCategoriesFromBackend() {
  const categories = await getCategories()
  if (categories && categories.length > 0) {
    // 将后端分类分配到支出类别（后端主要是支出分类）
    CATEGORY_TEMPLATES.expense = categories
    // 保留一些常见的收入分类
    CATEGORY_TEMPLATES.income = ['工资收入', '奖金补贴', '投资收益', '兼职收入', '红包转账', '退款返现', '其他收入']
  }
  return CATEGORY_TEMPLATES
}

// 智能分类规则
export const CATEGORY_RULES = [
  { category: '餐饮美食', keywords: ['餐', '饭', '食', '吃', '喝', '外卖', '美团', '饿了么', '肯德基', 'KFC', '麦当劳', '星巴克', '咖啡', '奶茶', '火锅', '烧烤', '小吃', '快餐', '食堂', '超市'], type: 'expense' },
  { category: '交通出行', keywords: ['滴滴', '打车', '出租', '地铁', '公交', '高铁', '火车', '飞机', '机票', '加油', '停车', '过路', '违章', 'ETC', '共享单车', '摩拜', '哈啰'], type: 'expense' },
  { category: '购物消费', keywords: ['淘宝', '天猫', '京东', '拼多多', '唯品会', '苏宁', '商场', '超市', '便利店', '服装', '鞋', '包', '化妆品', '电器', '手机', '电脑', '数码'], type: 'expense' },
  { category: '生活缴费', keywords: ['水费', '电费', '燃气', '物业', '宽带', '话费', '流量', '充值', '缴费', '电信', '移动', '联通'], type: 'expense' },
  { category: '医疗健康', keywords: ['医院', '药店', '体检', '挂号', '医疗', '药', '健康', '诊所', '牙科', '眼科'], type: 'expense' },
  { category: '娱乐休闲', keywords: ['电影', 'KTV', '游戏', '网吧', '健身', '运动', '旅游', '酒店', '民宿', '景区', '门票', '会员', '视频', '音乐', '直播'], type: 'expense' },
  { category: '学习教育', keywords: ['培训', '课程', '学费', '书', '教育', '学习', '考试', '报名', '辅导'], type: 'expense' },
  { category: '人情往来', keywords: ['红包', '转账', '礼物', '礼金', '份子'], type: 'expense' },
  { category: '房租房贷', keywords: ['房租', '租金', '房贷', '按揭', '中介'], type: 'expense' },
  { category: '投资理财', keywords: ['理财', '基金', '股票', '证券', '保险', '投资'], type: 'expense' },
  { category: '工资收入', keywords: ['工资', '薪资', '薪水', '代发'], type: 'income' },
  { category: '奖金补贴', keywords: ['奖金', '补贴', '津贴', '年终奖', '绩效'], type: 'income' },
  { category: '投资收益', keywords: ['分红', '利息', '收益', '理财'], type: 'income' },
  { category: '退款返现', keywords: ['退款', '返现', '退货', '撤销'], type: 'income' }
]

export function autoClassify(record) {
  const isIncome = record.income && parseFloat(record.income) > 0
  const type = isIncome ? 'income' : 'expense'
  
  if (record.category && record.category !== '未分类') {
    return record.category
  }
  
  const text = `${record.remark || ''} ${record.tradeType || ''} ${record.userRemark || ''}`.toLowerCase()
  
  for (const rule of CATEGORY_RULES) {
    if (rule.type !== type) continue
    for (const keyword of rule.keywords) {
      if (text.includes(keyword.toLowerCase())) {
        return rule.category
      }
    }
  }
  
  return '未分类'
}

export function batchAutoClassify(records) {
  return records.map(record => ({
    ...record,
    category: autoClassify(record)
  }))
}
