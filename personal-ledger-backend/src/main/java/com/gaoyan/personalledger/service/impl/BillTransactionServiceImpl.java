package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillTransaction;
import com.gaoyan.personalledger.mapper.BillTransactionMapper;
import com.gaoyan.personalledger.service.BillTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单交易明细Service实现
 */
@Slf4j
@Service
public class BillTransactionServiceImpl implements BillTransactionService {
    
    @Autowired
    private BillTransactionMapper billTransactionMapper;
    
    @Override
    public void save(BillTransaction transaction) {
        billTransactionMapper.insert(transaction);
    }
    
    @Override
    public void saveBatch(List<BillTransaction> transactions) {
        transactions.forEach(this::save);
    }
    
    @Override
    public void updateById(BillTransaction transaction) {
        billTransactionMapper.updateById(transaction);
    }
    
    @Override
    public BillTransaction getById(Long id) {
        return billTransactionMapper.selectById(id);
    }
    
    @Override
    public Page<BillTransaction> pageList(int current, int size, LocalDate startDate, LocalDate endDate, 
                                          String category, String paymentChannel, String transactionType, 
                                          String keyword, String minAmount, String maxAmount, 
                                          String incomeOrExpense, Boolean excludeFromStats, 
                                          String sortField, String sortOrder, Long firstImportId) {
        Page<BillTransaction> page = new Page<>(current, size);
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(BillTransaction::getCategory, category);
        } else if (category != null && category.isEmpty()) {
            wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
        }
        if (paymentChannel != null && !paymentChannel.isEmpty()) {
            wrapper.eq(BillTransaction::getPaymentChannel, paymentChannel);
        }
        if (transactionType != null && !transactionType.isEmpty()) {
            wrapper.like(BillTransaction::getTransactionType, transactionType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BillTransaction::getDescription, keyword)
                             .or().like(BillTransaction::getUserNote, keyword));
        }
        if (incomeOrExpense != null && !incomeOrExpense.isEmpty()) {
            if ("income".equals(incomeOrExpense)) {
                wrapper.isNotNull(BillTransaction::getIncome)
                       .gt(BillTransaction::getIncome, java.math.BigDecimal.ZERO);
            } else if ("expense".equals(incomeOrExpense)) {
                wrapper.isNotNull(BillTransaction::getExpense)
                       .gt(BillTransaction::getExpense, java.math.BigDecimal.ZERO);
            }
        }
        if (excludeFromStats != null) {
            wrapper.eq(BillTransaction::getExcludeFromStats, excludeFromStats);
        }
        
        // 排序
        if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
            if ("asc".equals(sortOrder)) {
                if ("transactionDate".equals(sortField)) {
                    wrapper.orderByAsc(BillTransaction::getTransactionDate);
                } else if ("income".equals(sortField)) {
                    wrapper.last("ORDER BY CAST(income AS DECIMAL) ASC");
                } else if ("expense".equals(sortField)) {
                    wrapper.last("ORDER BY CAST(expense AS DECIMAL) ASC");
                }
            } else {
                if ("transactionDate".equals(sortField)) {
                    wrapper.orderByDesc(BillTransaction::getTransactionDate);
                } else if ("income".equals(sortField)) {
                    wrapper.last("ORDER BY CAST(income AS DECIMAL) DESC");
                } else if ("expense".equals(sortField)) {
                    wrapper.last("ORDER BY CAST(expense AS DECIMAL) DESC");
                }
            }
        } else {
            wrapper.orderByDesc(BillTransaction::getTransactionDate);
        }
        
        return billTransactionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public BillTransaction findByDeduplicateKey(String key) {
        // 去重key格式：日期_时间_金额_描述
        // 这里简化实现，实际应该解析key并查询
        return null;
    }
    
    @Override
    public void deleteById(Long id) {
        billTransactionMapper.deleteById(id);
    }
    
    @Override
    public java.util.Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate, 
                                                     String category, String paymentChannel, String transactionType, 
                                                     String keyword, String minAmount, String maxAmount, 
                                                     String incomeOrExpense, Boolean excludeFromStats, Long firstImportId) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        // 汇总时只计算计入收支的数据
        wrapper.eq(BillTransaction::getExcludeFromStats, false);
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(BillTransaction::getCategory, category);
        } else if (category != null && category.isEmpty()) {
            wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
        }
        if (paymentChannel != null && !paymentChannel.isEmpty()) {
            wrapper.eq(BillTransaction::getPaymentChannel, paymentChannel);
        }
        if (transactionType != null && !transactionType.isEmpty()) {
            wrapper.like(BillTransaction::getTransactionType, transactionType);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BillTransaction::getDescription, keyword)
                             .or().like(BillTransaction::getUserNote, keyword));
        }
        if (incomeOrExpense != null && !incomeOrExpense.isEmpty()) {
            if ("income".equals(incomeOrExpense)) {
                wrapper.isNotNull(BillTransaction::getIncome)
                       .gt(BillTransaction::getIncome, java.math.BigDecimal.ZERO);
            } else if ("expense".equals(incomeOrExpense)) {
                wrapper.isNotNull(BillTransaction::getExpense)
                       .gt(BillTransaction::getExpense, java.math.BigDecimal.ZERO);
            }
        }
        
        List<BillTransaction> list = billTransactionMapper.selectList(wrapper);
        
        java.math.BigDecimal totalIncome = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalExpense = java.math.BigDecimal.ZERO;
        
        for (BillTransaction t : list) {
            if (t.getIncome() != null) {
                totalIncome = totalIncome.add(t.getIncome());
            }
            if (t.getExpense() != null) {
                totalExpense = totalExpense.add(t.getExpense());
            }
        }
        
        java.math.BigDecimal balance = totalIncome.subtract(totalExpense);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("income", totalIncome.toPlainString());
        result.put("expense", totalExpense.toPlainString());
        result.put("balance", balance.toPlainString());
        return result;
    }
    
    @Override
    public List<java.util.Map<String, Object>> getDailyStats(LocalDate startDate, LocalDate endDate, 
                                                              String category, String paymentChannel) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        // 只统计计入收支的数据
        wrapper.eq(BillTransaction::getExcludeFromStats, false);
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(BillTransaction::getCategory, category);
        } else if (category != null && category.isEmpty()) {
            wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
        }
        if (paymentChannel != null && !paymentChannel.isEmpty()) {
            wrapper.eq(BillTransaction::getPaymentChannel, paymentChannel);
        }
        
        wrapper.orderByAsc(BillTransaction::getTransactionDate);
        
        List<BillTransaction> list = billTransactionMapper.selectList(wrapper);
        
        // 按日期分组统计
        java.util.Map<LocalDate, java.util.Map<String, Object>> dailyMap = new java.util.LinkedHashMap<>();
        
        for (BillTransaction t : list) {
            LocalDate date = t.getTransactionDate();
            if (date == null) continue;
            
            dailyMap.putIfAbsent(date, new java.util.HashMap<>());
            java.util.Map<String, Object> stat = dailyMap.get(date);
            
            if (!stat.containsKey("date")) {
                stat.put("date", date.toString());
                stat.put("income", java.math.BigDecimal.ZERO);
                stat.put("expense", java.math.BigDecimal.ZERO);
                stat.put("count", 0);
            }
            
            java.math.BigDecimal income = (java.math.BigDecimal) stat.get("income");
            java.math.BigDecimal expense = (java.math.BigDecimal) stat.get("expense");
            int count = (int) stat.get("count");
            
            if (t.getIncome() != null) {
                income = income.add(t.getIncome());
            }
            if (t.getExpense() != null) {
                expense = expense.add(t.getExpense());
            }
            
            stat.put("income", income);
            stat.put("expense", expense);
            stat.put("balance", income.subtract(expense));
            stat.put("count", count + 1);
        }
        
        return new java.util.ArrayList<>(dailyMap.values());
    }
    
    @Override
    public List<java.util.Map<String, Object>> getCategoryStats(LocalDate startDate, LocalDate endDate, String type) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        // 只统计计入收支的数据
        wrapper.eq(BillTransaction::getExcludeFromStats, false);
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        
        // 根据类型过滤
        if ("expense".equals(type)) {
            wrapper.isNotNull(BillTransaction::getExpense)
                   .gt(BillTransaction::getExpense, java.math.BigDecimal.ZERO);
        } else if ("income".equals(type)) {
            wrapper.isNotNull(BillTransaction::getIncome)
                   .gt(BillTransaction::getIncome, java.math.BigDecimal.ZERO);
        }
        
        List<BillTransaction> list = billTransactionMapper.selectList(wrapper);
        
        // 按分类分组统计
        java.util.Map<String, java.util.Map<String, Object>> categoryMap = new java.util.LinkedHashMap<>();
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        
        for (BillTransaction t : list) {
            String category = t.getCategory();
            if (category == null || category.isEmpty()) {
                category = "未分类";
            }
            
            java.math.BigDecimal amount = "expense".equals(type) ? t.getExpense() : t.getIncome();
            if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                continue;
            }
            
            categoryMap.putIfAbsent(category, new java.util.HashMap<>());
            java.util.Map<String, Object> stat = categoryMap.get(category);
            
            if (!stat.containsKey("category")) {
                stat.put("category", category);
                stat.put("amount", java.math.BigDecimal.ZERO);
                stat.put("count", 0);
            }
            
            java.math.BigDecimal currentAmount = (java.math.BigDecimal) stat.get("amount");
            int count = (int) stat.get("count");
            
            stat.put("amount", currentAmount.add(amount));
            stat.put("count", count + 1);
            
            total = total.add(amount);
        }
        
        // 计算占比
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> stat : categoryMap.values()) {
            java.math.BigDecimal amount = (java.math.BigDecimal) stat.get("amount");
            if (total.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal percent = amount.divide(total, 4, java.math.BigDecimal.ROUND_HALF_UP)
                                                     .multiply(new java.math.BigDecimal(100));
                stat.put("percent", percent);
            } else {
                stat.put("percent", java.math.BigDecimal.ZERO);
            }
            result.add(stat);
        }
        
        // 按金额降序排序
        result.sort((a, b) -> {
            java.math.BigDecimal amountA = (java.math.BigDecimal) a.get("amount");
            java.math.BigDecimal amountB = (java.math.BigDecimal) b.get("amount");
            return amountB.compareTo(amountA);
        });
        
        return result;
    }
}
