package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaoyan.personalledger.entity.*;
import com.gaoyan.personalledger.mapper.BillTransactionImportMapper;
import com.gaoyan.personalledger.mapper.BillTransactionMapper;
import com.gaoyan.personalledger.service.BillImportProcessorService;
import com.gaoyan.personalledger.service.BillImportHistoryService;
import com.gaoyan.personalledger.service.CmbBillService;
import com.gaoyan.personalledger.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 账单导入处理Service实现
 */
@Slf4j
@Service
public class BillImportProcessorServiceImpl implements BillImportProcessorService {
    
    @Autowired
    private BillImportHistoryService billImportHistoryService;
    
    @Autowired
    private BillTransactionMapper billTransactionMapper;
    
    @Autowired
    private BillTransactionImportMapper billTransactionImportMapper;
    
    @Autowired
    private CmbBillService cmbBillService;
    
    @Autowired
    private DataCleaningService dataCleaningService;
    
    @Override
    @Transactional
    public BillImportHistory processCsvImport(MultipartFile file) {
        try {
            // 1. 解析CSV文件
            CmbBillInfo billInfo = cmbBillService.parseCmbBillInfo(file);
            List<CmbBillRecordReal> records = dataCleaningService.cleanBillRecords(billInfo.getRecords());
            
            // 2. 创建导入记录
            BillImportHistory billImportHistory = new BillImportHistory();
            billImportHistory.setId(System.currentTimeMillis());
            billImportHistory.setImportName(file.getOriginalFilename());
            billImportHistory.setSourceFile(file.getOriginalFilename());
            billImportHistory.setFileType("CSV");
            billImportHistory.setImportTime(LocalDateTime.now());
            billImportHistory.setRecordCount(records.size());
            billImportHistory.setAccountNumber(billInfo.getExportInfo().getAccount());
            billImportHistory.setPeriodStart(parseDate(billInfo.getExportInfo().getStartDate()));
            billImportHistory.setPeriodEnd(parseDate(billInfo.getExportInfo().getEndDate()));
            billImportHistory.setImportStatus("PROCESSING");
            billImportHistoryService.createImport(billImportHistory);
            
            // 3. 处理记录（去重、保存）
            ImportResult result = processRecords(records, billImportHistory.getId());
            
            // 4. 更新导入统计
            billImportHistory.setNewCount(result.newCount);
            billImportHistory.setDuplicateCount(result.duplicateCount);
            billImportHistory.setUpdateCount(result.updateCount);
            billImportHistory.setImportStatus("SUCCESS");
            billImportHistoryService.updateImport(billImportHistory);
            
            log.info("CSV导入完成: 新增={}, 重复={}", result.newCount, result.duplicateCount);
            return billImportHistory;
            
        } catch (Exception e) {
            log.error("CSV导入失败", e);
            throw new RuntimeException("CSV导入失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BillImportHistory processExcelImport(MultipartFile file) {
        try {
            // 1. 解析Excel文件
            CmbBillInfo billInfo = cmbBillService.parseExcelBillInfo(file);
            List<CmbBillRecordReal> records = dataCleaningService.cleanBillRecords(billInfo.getRecords());
            
            // 2. 创建导入记录
            BillImportHistory billImportHistory = new BillImportHistory();
            billImportHistory.setId(System.currentTimeMillis() + 1);
            billImportHistory.setImportName(file.getOriginalFilename());
            billImportHistory.setSourceFile(file.getOriginalFilename());
            billImportHistory.setFileType("EXCEL");
            billImportHistory.setImportTime(LocalDateTime.now());
            billImportHistory.setRecordCount(records.size());
            billImportHistory.setAccountNumber(billInfo.getExportInfo().getAccount());
            billImportHistory.setPeriodStart(parseDate(billInfo.getExportInfo().getStartDate()));
            billImportHistory.setPeriodEnd(parseDate(billInfo.getExportInfo().getEndDate()));
            billImportHistory.setImportStatus("PROCESSING");
            billImportHistoryService.createImport(billImportHistory);
            
            // 3. 处理记录（去重、保存）
            ImportResult result = processRecords(records, billImportHistory.getId());
            
            // 4. 更新导入统计
            billImportHistory.setNewCount(result.newCount);
            billImportHistory.setDuplicateCount(result.duplicateCount);
            billImportHistory.setUpdateCount(result.updateCount);
            billImportHistory.setImportStatus("SUCCESS");
            billImportHistoryService.updateImport(billImportHistory);
            
            log.info("Excel导入完成: 新增={}, 重复={}", result.newCount, result.duplicateCount);
            return billImportHistory;
            
        } catch (Exception e) {
            log.error("Excel导入失败", e);
            throw new RuntimeException("Excel导入失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ImportResult processRecords(List<CmbBillRecordReal> records, Long importId) {
        ImportResult result = new ImportResult();
        
        for (CmbBillRecordReal record : records) {
            // 构建去重key
            String deduplicateKey = buildDeduplicateKey(record);
            
            // 查询是否已存在
            BillTransaction existing = findByDeduplicateKey(deduplicateKey, record);
            
            if (existing == null) {
                // 新增交易
                BillTransaction transaction = convertToTransaction(record, importId);
                transaction.setId(System.currentTimeMillis() + result.newCount); // 生成唯一ID
                billTransactionMapper.insert(transaction);
                
                // 记录关联关系
                saveTransactionImport(transaction.getId(), importId, true);
                result.newCount++;
                
            } else {
                // 重复交易，更新追踪字段
                existing.setLastImportId(importId);
                existing.setImportCount(existing.getImportCount() + 1);
                existing.setUpdateTime(LocalDateTime.now());
                billTransactionMapper.updateById(existing);
                
                // 记录关联关系
                saveTransactionImport(existing.getId(), importId, false);
                result.duplicateCount++;
            }
        }
        
        return result;
    }
    
    /**
     * 构建去重key
     */
    private String buildDeduplicateKey(CmbBillRecordReal record) {
        return String.format("%s_%s_%s_%s",
            record.getTransactionDate(),
            record.getTransactionTime(),
            record.getExpense() != null ? record.getExpense() : record.getIncome(),
            record.getDescription());
    }
    
    /**
     * 根据去重key查询交易
     */
    private BillTransaction findByDeduplicateKey(String key, CmbBillRecordReal record) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillTransaction::getTransactionDate, record.getTransactionDate())
               .eq(BillTransaction::getTransactionTime, record.getTransactionTime())
               .eq(BillTransaction::getDescription, record.getDescription());
        
        if (record.getExpense() != null) {
            wrapper.eq(BillTransaction::getExpense, record.getExpense());
        } else {
            wrapper.eq(BillTransaction::getIncome, record.getIncome());
        }
        
        return billTransactionMapper.selectOne(wrapper);
    }
    
    /**
     * 转换为BillTransaction
     */
    private BillTransaction convertToTransaction(CmbBillRecordReal record, Long importId) {
        BillTransaction transaction = new BillTransaction();
        
        transaction.setTransactionDate(record.getTransactionDate());
        transaction.setTransactionTime(record.getTransactionTime());
        transaction.setIncome(record.getIncome());
        transaction.setExpense(record.getExpense());
        transaction.setBalance(record.getBalance());
        transaction.setTransactionType(record.getTransactionType());
        transaction.setDescription(record.getDescription());
        transaction.setPaymentChannel(record.getPaymentChannel());
        transaction.setCategory(record.getCategory());
        transaction.setUserNote(record.getUserNote());
        transaction.setExcludeFromStats(record.getExcludeFromStats());
        transaction.setFirstImportId(importId);
        transaction.setLastImportId(importId);
        transaction.setImportCount(1);
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());
        
        return transaction;
    }
    
    /**
     * 解析日期
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        // 支持两种格式：yyyyMMdd 和 yyyy-MM-dd
        if (dateStr.length() == 8 && !dateStr.contains("-")) {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 解析时间
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    @Override
    @Transactional
    public int updateCleanedRecords(List<CmbBillRecordReal> records) {
        log.info("开始更新清洗后的记录到数据库，记录数: {}", records.size());
        
        int updatedCount = 0;
        for (CmbBillRecordReal record : records) {
            // 如果记录有ID，则直接通过ID更新
            if (record.getId() != null) {
                BillTransaction existing = billTransactionMapper.selectById(record.getId());
                if (existing != null) {
                    // 更新记录
                    existing.setPaymentChannel(record.getPaymentChannel());
                    existing.setTransactionType(record.getTransactionType());
                    existing.setCategory(record.getCategory());
                    existing.setUserNote(record.getUserNote());
                    existing.setExcludeFromStats(record.getExcludeFromStats());
                    existing.setUpdateTime(LocalDateTime.now());
                    
                    billTransactionMapper.updateById(existing);
                    updatedCount++;
                }
            } else {
                // 没有ID的情况下，使用原有的去重逻辑
                LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BillTransaction::getTransactionDate, record.getTransactionDate())
                       .eq(BillTransaction::getTransactionTime, record.getTransactionTime())
                       .eq(BillTransaction::getDescription, record.getDescription());
                
                if (record.getExpense() != null) {
                    wrapper.eq(BillTransaction::getExpense, record.getExpense());
                } else {
                    wrapper.eq(BillTransaction::getIncome, record.getIncome());
                }
                
                BillTransaction existing = billTransactionMapper.selectOne(wrapper);
                if (existing != null) {
                    // 更新记录
                    existing.setPaymentChannel(record.getPaymentChannel());
                    existing.setTransactionType(record.getTransactionType());
                    existing.setCategory(record.getCategory());
                    existing.setUserNote(record.getUserNote());
                    existing.setExcludeFromStats(record.getExcludeFromStats());
                    existing.setUpdateTime(LocalDateTime.now());
                    
                    billTransactionMapper.updateById(existing);
                    updatedCount++;
                }
            }
        }
        
        log.info("完成更新清洗后的记录到数据库，共更新{}条记录", updatedCount);
        return updatedCount;
    }
    
    /**
     * 记录交易与导入的关联关系
     */
    private void saveTransactionImport(Long transactionId, Long importId, Boolean isNew) {
        BillTransactionImport relation = new BillTransactionImport();
        relation.setId(System.currentTimeMillis() + transactionId); // 生成唯一ID
        relation.setTransactionId(transactionId);
        relation.setImportId(importId);
        relation.setIsNew(isNew);
        billTransactionImportMapper.insert(relation);
    }
}