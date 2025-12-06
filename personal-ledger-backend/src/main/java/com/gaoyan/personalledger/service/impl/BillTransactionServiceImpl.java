package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillTag;
import com.gaoyan.personalledger.entity.BillTransaction;
import com.gaoyan.personalledger.entity.BillTransactionTag;
import com.gaoyan.personalledger.entity.Category;
import com.gaoyan.personalledger.mapper.BillTagMapper;
import com.gaoyan.personalledger.mapper.BillTransactionMapper;
import com.gaoyan.personalledger.mapper.BillTransactionTagMapper;
import com.gaoyan.personalledger.service.BillTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BillTransactionServiceImpl implements BillTransactionService {
    
    @Autowired
    private BillTransactionMapper billTransactionMapper;
    
    @Autowired
    private com.gaoyan.personalledger.mapper.CategoryMapper categoryMapper;
    
    @Autowired
    private BillTransactionTagMapper billTransactionTagMapper;
    
    @Autowired
    private BillTagMapper billTagMapper;
    
    @Override
    public void save(BillTransaction transaction) {
//        splitCategory(transaction);
        billTransactionMapper.insert(transaction);
    }
    
    @Override
    public void saveBatch(List<BillTransaction> transactions) {
        transactions.forEach(this::save);
    }
    
    private void splitCategory(BillTransaction transaction) {
        if (transaction.getCategory() == null || transaction.getCategory().isEmpty()) {
            transaction.setSubCategory(null);
            return;
        }
        
        QueryWrapper<Category> wrapper =             new QueryWrapper<>();
        wrapper.eq("name", transaction.getCategory());
        Category category = categoryMapper.selectOne(wrapper);
        
        if (category == null) {
            transaction.setSubCategory(null);
            return;
        }
        
        if (category.getParentId() == null || category.getParentId() == 0) {
            // 一级分类：category保持不变，subCategory设为null
            transaction.setSubCategory(null);
        } else {
            // 二级分类：category设为父分类名，subCategory设为二级分类名
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent != null) {
                transaction.setCategory(parent.getName());
                transaction.setSubCategory(category.getName());
            } else {
                transaction.setSubCategory(null);
            }
        }
    }
    
    @Override
    public void updateById(BillTransaction transaction) {
//        splitCategory(transaction);
        UpdateWrapper<BillTransaction> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", transaction.getId())
                .set("transaction_date", transaction.getTransactionDate())
                .set("transaction_time", transaction.getTransactionTime())
                .set("income", transaction.getIncome())
                .set("expense", transaction.getExpense())
                .set("category", transaction.getCategory())
                .set("sub_category", transaction.getSubCategory())
                .set("payment_channel", transaction.getPaymentChannel())
                .set("transaction_type", transaction.getTransactionType())
                .set("description", transaction.getDescription())
                .set("user_note", transaction.getUserNote())
                .set("include_in_stats", transaction.getIncludeInStats())
                .set("is_refund", transaction.getIsRefund());
        billTransactionMapper.update(null, wrapper);
    }
    
    @Override
    public BillTransaction getById(Long id) {
        return billTransactionMapper.selectById(id);
    }
    
    @Override
    public Page<BillTransaction> pageList(int current, int size, LocalDate startDate, LocalDate endDate, 
                                          String category, String subCategory,
                                          String paymentChannel, String transactionType, 
                                          String keyword, String minAmount, String maxAmount, 
                                          String incomeOrExpense, Boolean includeInStats, Boolean isRefund, 
                                          String sortField, String sortOrder, Long firstImportId, String tagIds) {
        Page<BillTransaction> page = new Page<>(current, size);
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !"__ALL__".equals(category)) {
            if (category.isEmpty()) {
                wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
            } else {
                wrapper.eq(BillTransaction::getCategory, category);
            }
        }
        if (subCategory != null && !"__ALL__".equals(subCategory)) {
            if (subCategory.isEmpty()) {
                wrapper.and(w -> w.isNull(BillTransaction::getSubCategory).or().eq(BillTransaction::getSubCategory, ""));
            } else {
                wrapper.eq(BillTransaction::getSubCategory, subCategory);
            }
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
        if (minAmount != null && !minAmount.isEmpty()) {
            java.math.BigDecimal min = new java.math.BigDecimal(minAmount);
            wrapper.and(w -> w.ge(BillTransaction::getIncome, min).or().ge(BillTransaction::getExpense, min));
        }
        if (maxAmount != null && !maxAmount.isEmpty()) {
            java.math.BigDecimal max = new java.math.BigDecimal(maxAmount);
            wrapper.and(w -> w.le(BillTransaction::getIncome, max).or().le(BillTransaction::getExpense, max));
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
        if (tagIds != null && !tagIds.isEmpty()) {
            String[] tagIdArray = tagIds.split(",");
            wrapper.in(BillTransaction::getId, 
                billTransactionMapper.selectTransactionIdsByTagIds(java.util.Arrays.asList(tagIdArray)));
        }
        
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
        
        Page<BillTransaction> result = billTransactionMapper.selectPage(page, wrapper);
        
        // 批量加载标签
        loadTagsForTransactions(result.getRecords());
        
        return result;
    }
    
    @Override
    public BillTransaction findByDeduplicateKey(String key) {
        return null;
    }
    
    @Override
    public void deleteById(Long id) {
        billTransactionMapper.deleteById(id);
    }
    
    @Override
    public java.util.Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate, 
                                                     String category, String subCategory,
                                                     String paymentChannel, String transactionType, 
                                                     String keyword, String minAmount, String maxAmount, 
                                                     String incomeOrExpense, Boolean includeInStats, Boolean isRefund, Long firstImportId, String tagIds) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(BillTransaction::getIncludeInStats, true);
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !"__ALL__".equals(category)) {
            if (category.isEmpty()) {
                wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
            } else {
                wrapper.eq(BillTransaction::getCategory, category);
            }
        }
        if (subCategory != null && !"__ALL__".equals(subCategory)) {
            if (subCategory.isEmpty()) {
                wrapper.and(w -> w.isNull(BillTransaction::getSubCategory).or().eq(BillTransaction::getSubCategory, ""));
            } else {
                wrapper.eq(BillTransaction::getSubCategory, subCategory);
            }
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
        if (minAmount != null && !minAmount.isEmpty()) {
            java.math.BigDecimal min = new java.math.BigDecimal(minAmount);
            wrapper.and(w -> w.ge(BillTransaction::getIncome, min).or().ge(BillTransaction::getExpense, min));
        }
        if (maxAmount != null && !maxAmount.isEmpty()) {
            java.math.BigDecimal max = new java.math.BigDecimal(maxAmount);
            wrapper.and(w -> w.le(BillTransaction::getIncome, max).or().le(BillTransaction::getExpense, max));
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
        if (isRefund != null) {
            wrapper.eq(BillTransaction::getIsRefund, isRefund);
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            String[] tagIdArray = tagIds.split(",");
            wrapper.in(BillTransaction::getId, 
                billTransactionMapper.selectTransactionIdsByTagIds(java.util.Arrays.asList(tagIdArray)));
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
        
        wrapper.eq(BillTransaction::getIncludeInStats, true);
        
        if (startDate != null) {
            wrapper.ge(BillTransaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(BillTransaction::getTransactionDate, endDate);
        }
        if (category != null && !"__ALL__".equals(category)) {
            if (category.isEmpty()) {
                wrapper.and(w -> w.isNull(BillTransaction::getCategory).or().eq(BillTransaction::getCategory, ""));
            } else {
                wrapper.eq(BillTransaction::getCategory, category);
            }
        }
        if (paymentChannel != null && !paymentChannel.isEmpty()) {
            wrapper.eq(BillTransaction::getPaymentChannel, paymentChannel);
        }
        
        wrapper.orderByAsc(BillTransaction::getTransactionDate);
        
        List<BillTransaction> list = billTransactionMapper.selectList(wrapper);
        
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
        
        return list;
    }
    
    /**
     * 批量加载交易的标签
     */
    private void loadTagsForTransactions(List<BillTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        // 获取所有交易ID
        List<Long> transactionIds = transactions.stream()
                .map(BillTransaction::getId)
                .collect(Collectors.toList());
        
        // 批量查询交易标签关联
        List<BillTransactionTag> relations = billTransactionTagMapper.selectByTransactionIds(transactionIds);
        
        // 获取所有标签ID
        Set<Long> tagIds = relations.stream()
                .map(BillTransactionTag::getTagId)
                .collect(Collectors.toSet());
        
        // 批量查询标签信息
        Map<Long, BillTag> tagMap = new HashMap<>();
        if (!tagIds.isEmpty()) {
            List<BillTag> tags = billTagMapper.selectBatchIds(tagIds);
            tagMap = tags.stream().collect(Collectors.toMap(BillTag::getId, tag -> tag));
        }
        
        // 构建交易ID到标签列表的映射
        Map<Long, List<BillTag>> transactionTagMap = new HashMap<>();
        for (BillTransactionTag relation : relations) {
            BillTag tag = tagMap.get(relation.getTagId());
            if (tag != null) {
                transactionTagMap.computeIfAbsent(relation.getTransactionId(), k -> new ArrayList<>()).add(tag);
            }
        }
        
        // 设置每个交易的标签列表
        for (BillTransaction transaction : transactions) {
            transaction.setTags(transactionTagMap.getOrDefault(transaction.getId(), new ArrayList<>()));
        }
    }
}
