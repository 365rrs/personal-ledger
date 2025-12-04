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
                                          String incomeOrExpense, Boolean includeInStats, Boolean isRefund, 
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
        if (includeInStats != null) {
            wrapper.eq(BillTransaction::getIncludeInStats, includeInStats);
        }
        if (isRefund != null) {
            wrapper.eq(BillTransaction::getIsRefund, isRefund);
        }
        
        // 排序
        if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
            boolean isAsc = "asc".equals(sortOrder);
            switch (sortField) {
                case "transactionDate":
                    if (isAsc) wrapper.orderByAsc(BillTransaction::getTransactionDate);
                    else wrapper.orderByDesc(BillTransaction::getTransactionDate);
                    break;
                case "transactionTime":
                    if (isAsc) wrapper.orderByAsc(BillTransaction::getTransactionTime);
                    else wrapper.orderByDesc(BillTransaction::getTransactionTime);
                    break;
                case "income":
                    wrapper.last("ORDER BY CAST(income AS DECIMAL) " + (isAsc ? "ASC" : "DESC"));
                    break;
                case "expense":
                    wrapper.last("ORDER BY CAST(expense AS DECIMAL) " + (isAsc ? "ASC" : "DESC"));
                    break;
                default:
                    wrapper.orderByDesc(BillTransaction::getTransactionDate);
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
                                                     String incomeOrExpense, Boolean includeInStats, Long firstImportId) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        // 汇总时只计算计入收支的数据
        wrapper.eq(BillTransaction::getIncludeInStats, true);
        
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
        wrapper.eq(BillTransaction::getIncludeInStats, true);
        
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
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillTransaction> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 只统计计入收支的数据
        wrapper.eq("include_in_stats", true);
        
        if (startDate != null) {
            wrapper.ge("transaction_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("transaction_date", endDate);
        }
        
        if ("expense".equals(type)) {
            wrapper.isNotNull("expense").gt("expense", java.math.BigDecimal.ZERO);
        } else if ("income".equals(type)) {
            wrapper.isNotNull("income").gt("income", java.math.BigDecimal.ZERO);
        }
        
        wrapper.isNotNull("category").ne("category", "");
        
        wrapper.groupBy("category");
        wrapper.select("category", "COUNT(*) as count", 
                      "SUM(CASE WHEN income IS NOT NULL THEN income ELSE expense END) as amount");
        wrapper.orderByDesc("amount");
        
        java.util.List<java.util.Map<String, Object>> list = billTransactionMapper.selectMaps(wrapper);
        
        // 计算总数用于百分比计算
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (java.util.Map<String, Object> item : list) {
            Object amountObj = item.get("amount");
            if (amountObj != null) {
                if (amountObj instanceof java.math.BigDecimal) {
                    totalAmount = totalAmount.add((java.math.BigDecimal) amountObj);
                } else {
                    totalAmount = totalAmount.add(new java.math.BigDecimal(amountObj.toString()));
                }
            }
        }
        
        // 计算每个分类的百分比
        for (java.util.Map<String, Object> item : list) {
            Object amountObj = item.get("amount");
            java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
            if (amountObj != null) {
                if (amountObj instanceof java.math.BigDecimal) {
                    amount = (java.math.BigDecimal) amountObj;
                } else {
                    amount = new java.math.BigDecimal(amountObj.toString());
                }
            }
            
            if (totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal percent = amount.multiply(java.math.BigDecimal.valueOf(100))
                        .divide(totalAmount, 2, java.math.RoundingMode.HALF_UP);
                item.put("percent", percent);
            } else {
                item.put("percent", java.math.BigDecimal.ZERO);
            }
        }
        
        // 添加"未分类"统计
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillTransaction> unclassifiedWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        unclassifiedWrapper.eq("include_in_stats", true);
        
        if (startDate != null) {
            unclassifiedWrapper.ge("transaction_date", startDate);
        }
        if (endDate != null) {
            unclassifiedWrapper.le("transaction_date", endDate);
        }
        
        if ("expense".equals(type)) {
            unclassifiedWrapper.isNotNull("expense").gt("expense", java.math.BigDecimal.ZERO);
        } else if ("income".equals(type)) {
            unclassifiedWrapper.isNotNull("income").gt("income", java.math.BigDecimal.ZERO);
        }
        
        unclassifiedWrapper.and(w -> w.isNull("category")
                                     .or()
                                     .eq("category", ""));
        
        Long unclassifiedCount = billTransactionMapper.selectCount(unclassifiedWrapper);
        if (unclassifiedCount > 0) {
            // 计算未分类的金额总和
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BillTransaction> unclassifiedAmountWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            unclassifiedAmountWrapper.eq("include_in_stats", true);
            
            if (startDate != null) {
                unclassifiedAmountWrapper.ge("transaction_date", startDate);
            }
            if (endDate != null) {
                unclassifiedAmountWrapper.le("transaction_date", endDate);
            }
            
            if ("expense".equals(type)) {
                unclassifiedAmountWrapper.isNotNull("expense").gt("expense", java.math.BigDecimal.ZERO);
            } else if ("income".equals(type)) {
                unclassifiedAmountWrapper.isNotNull("income").gt("income", java.math.BigDecimal.ZERO);
            }
            
            unclassifiedAmountWrapper.and(w -> w.isNull("category")
                                         .or()
                                         .eq("category", ""));
            
            List<BillTransaction> unclassifiedTransactions = billTransactionMapper.selectList(unclassifiedAmountWrapper);
            java.math.BigDecimal unclassifiedAmount = java.math.BigDecimal.ZERO;
            for (BillTransaction transaction : unclassifiedTransactions) {
                if ("expense".equals(type) && transaction.getExpense() != null) {
                    unclassifiedAmount = unclassifiedAmount.add(transaction.getExpense());
                } else if ("income".equals(type) && transaction.getIncome() != null) {
                    unclassifiedAmount = unclassifiedAmount.add(transaction.getIncome());
                }
            }
            
            java.util.Map<String, Object> unclassifiedItem = new java.util.HashMap<>();
            unclassifiedItem.put("category", "未分类");
            unclassifiedItem.put("count", unclassifiedCount);
            unclassifiedItem.put("amount", unclassifiedAmount);
            
            // 将未分类金额加入总计中，以便正确计算百分比
            totalAmount = totalAmount.add(unclassifiedAmount);
            
            if (totalAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal percent = unclassifiedAmount.multiply(java.math.BigDecimal.valueOf(100))
                        .divide(totalAmount, 2, java.math.RoundingMode.HALF_UP);
                unclassifiedItem.put("percent", percent);
            } else {
                unclassifiedItem.put("percent", java.math.BigDecimal.ZERO);
            }
            
            list.add(unclassifiedItem);
        }
        
        return list;
    }
}
