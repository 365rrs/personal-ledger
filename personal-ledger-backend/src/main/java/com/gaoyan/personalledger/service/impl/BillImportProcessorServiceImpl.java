package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaoyan.personalledger.entity.*;
import com.gaoyan.personalledger.mapper.BillTransactionImportMapper;
import com.gaoyan.personalledger.mapper.BillTransactionMapper;
import com.gaoyan.personalledger.service.BillImportProcessorService;
import com.gaoyan.personalledger.service.BillImportService;
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
    private BillImportService billImportService;
    
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
    public BillImport processCsvImport(MultipartFile file) {
        try {
            // 1. 解析CSV文件
            CmbBillInfo billInfo = cmbBillService.parseCmbBillInfo(file);
            List<CmbBillRecordReal> records = dataCleaningService.cleanBillRecords(billInfo.getRecords());
            
            // 2. 创建导入记录
            BillImport billImport = new BillImport();
            billImport.setId(System.currentTimeMillis());
            billImport.setImportName(file.getOriginalFilename());
            billImport.setSourceFile(file.getOriginalFilename());
            billImport.setFileType("CSV");
            billImport.setImportTime(LocalDateTime.now());
            billImport.setRecordCount(records.size());
            billImport.setAccountNumber(billInfo.getExportInfo().getAccount());
            billImport.setPeriodStart(parseDate(billInfo.getExportInfo().getStartDate()));
            billImport.setPeriodEnd(parseDate(billInfo.getExportInfo().getEndDate()));
            billImport.setImportStatus("PROCESSING");
            billImportService.createImport(billImport);
            
            // 3. 处理记录（去重、保存）
            ImportResult result = processRecords(records, billImport.getId());
            
            // 4. 更新导入统计
            billImport.setNewCount(result.newCount);
            billImport.setDuplicateCount(result.duplicateCount);
            billImport.setUpdateCount(result.updateCount);
            billImport.setImportStatus("SUCCESS");
            billImportService.updateImport(billImport);
            
            log.info("CSV导入完成: 新增={}, 重复={}", result.newCount, result.duplicateCount);
            return billImport;
            
        } catch (Exception e) {
            log.error("CSV导入失败", e);
            throw new RuntimeException("CSV导入失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public BillImport processExcelImport(MultipartFile file) {
        try {
            // 1. 解析Excel文件
            CmbBillInfo billInfo = cmbBillService.parseExcelBillInfo(file);
            List<CmbBillRecordReal> records = dataCleaningService.cleanBillRecords(billInfo.getRecords());
            
            // 2. 创建导入记录
            BillImport billImport = new BillImport();
            billImport.setId(System.currentTimeMillis() + 1);
            billImport.setImportName(file.getOriginalFilename());
            billImport.setSourceFile(file.getOriginalFilename());
            billImport.setFileType("EXCEL");
            billImport.setImportTime(LocalDateTime.now());
            billImport.setRecordCount(records.size());
            billImport.setAccountNumber(billInfo.getExportInfo().getAccount());
            billImport.setPeriodStart(parseDate(billInfo.getExportInfo().getStartDate()));
            billImport.setPeriodEnd(parseDate(billInfo.getExportInfo().getEndDate()));
            billImport.setImportStatus("PROCESSING");
            billImportService.createImport(billImport);
            
            // 3. 处理记录（去重、保存）
            ImportResult result = processRecords(records, billImport.getId());
            
            // 4. 更新导入统计
            billImport.setNewCount(result.newCount);
            billImport.setDuplicateCount(result.duplicateCount);
            billImport.setUpdateCount(result.updateCount);
            billImport.setImportStatus("SUCCESS");
            billImportService.updateImport(billImport);
            
            log.info("Excel导入完成: 新增={}, 重复={}", result.newCount, result.duplicateCount);
            return billImport;
            
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
            record.getFormattedTradeDate(),
            record.getTradeTime(),
            record.getExpense() != null ? record.getExpense() : record.getIncome(),
            record.getRemark());
    }
    
    /**
     * 根据去重key查询交易
     */
    private BillTransaction findByDeduplicateKey(String key, CmbBillRecordReal record) {
        LambdaQueryWrapper<BillTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillTransaction::getTransactionDate, parseDate(record.getFormattedTradeDate()))
               .eq(BillTransaction::getTransactionTime, parseTime(record.getTradeTime()))
               .eq(BillTransaction::getDescription, record.getRemark());
        
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
        
        String dateStr = record.getFormattedTradeDate();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            dateStr = record.getTradeDate();
        }
        
        transaction.setTransactionDate(parseDate(dateStr));
        transaction.setTransactionTime(parseTime(record.getTradeTime()));
        transaction.setIncome(record.getIncome());
        transaction.setExpense(record.getExpense());
        transaction.setBalance(record.getBalance());
        transaction.setTransactionType(record.getTransactionType());
        transaction.setDescription(record.getRemark());
        transaction.setPaymentChannel(record.getPaymentChannel());
        transaction.setCategory(record.getCategory());
        transaction.setUserNote(record.getUserRemark());
        transaction.setExcludeFromStats(record.getExcludeFromMonthly() != null ? !record.getExcludeFromMonthly() : false);
        transaction.setFirstImportId(importId);
        transaction.setLastImportId(importId);
        transaction.setImportCount(1);
        return transaction;
    }
    
    /**
     * 保存交易导入关联
     */
    private void saveTransactionImport(Long transactionId, Long importId, boolean isNew) {
        BillTransactionImport relation = new BillTransactionImport();
        relation.setId(System.currentTimeMillis() + transactionId); // 生成唯一ID
        relation.setTransactionId(transactionId);
        relation.setImportId(importId);
        relation.setIsNew(isNew);
        billTransactionImportMapper.insert(relation);
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
}
