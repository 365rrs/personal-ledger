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
    public Page<BillTransaction> pageList(int current, int size, LocalDate startDate, LocalDate endDate, String category) {
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
        }
        
        wrapper.orderByDesc(BillTransaction::getTransactionDate);
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
}
