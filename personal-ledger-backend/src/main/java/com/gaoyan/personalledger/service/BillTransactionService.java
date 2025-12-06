package com.gaoyan.personalledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillTransaction;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单交易明细Service
 */
public interface BillTransactionService {
    
    /**
     * 保存交易记录
     */
    void save(BillTransaction transaction);
    
    /**
     * 批量保存交易记录
     */
    void saveBatch(List<BillTransaction> transactions);
    
    /**
     * 更新交易记录
     */
    void updateById(BillTransaction transaction);
    
    /**
     * 根据ID获取交易
     */
    BillTransaction getById(Long id);
    
    /**
     * 分页查询交易列表
     */
    Page<BillTransaction> pageList(int current, int size, LocalDate startDate, LocalDate endDate, 
                                   String category, String subCategory,
                                   String paymentChannel, String transactionType, 
                                   String keyword, String minAmount, String maxAmount, 
                                   String incomeOrExpense, Boolean includeInStats, Boolean isRefund, 
                                   String sortField, String sortOrder, Long firstImportId, String tagIds);
    
    /**
     * 根据去重key查询交易
     */
    BillTransaction findByDeduplicateKey(String key);
    
    /**
     * 删除交易记录
     */
    void deleteById(Long id);
    
    /**
     * 获取交易汇总
     */
    java.util.Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate, 
                                              String category, String subCategory,
                                              String paymentChannel, String transactionType, 
                                              String keyword, String minAmount, String maxAmount, 
                                              String incomeOrExpense, Boolean includeInStats, Boolean isRefund, Long firstImportId, String tagIds);
    
    /**
     * 按天统计
     */
    List<java.util.Map<String, Object>> getDailyStats(LocalDate startDate, LocalDate endDate, 
                                                       String category, String paymentChannel);
    
    /**
     * 按分类统计
     */
    List<java.util.Map<String, Object>> getCategoryStats(LocalDate startDate, LocalDate endDate, String type);
}
