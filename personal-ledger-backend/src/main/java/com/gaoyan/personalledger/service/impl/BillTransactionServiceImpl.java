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
                    wrapper.orderByAsc(BillTransaction::getIncome);
                } else if ("expense".equals(sortField)) {
                    wrapper.orderByAsc(BillTransaction::getExpense);
                }
            } else {
                if ("transactionDate".equals(sortField)) {
                    wrapper.orderByDesc(BillTransaction::getTransactionDate);
                } else if ("income".equals(sortField)) {
                    wrapper.orderByDesc(BillTransaction::getIncome);
                } else if ("expense".equals(sortField)) {
                    wrapper.orderByDesc(BillTransaction::getExpense);
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
}
