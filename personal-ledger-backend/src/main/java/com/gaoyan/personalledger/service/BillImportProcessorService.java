package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.BillImportHistory;
import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 账单导入处理Service
 */
public interface BillImportProcessorService {
    
    /**
     * 处理CSV导入
     * @param file CSV文件
     * @return 导入记录
     */
    BillImportHistory processCsvImport(MultipartFile file);
    
    /**
     * 处理Excel导入
     * @param file Excel文件
     * @return 导入记录
     */
    BillImportHistory processExcelImport(MultipartFile file);
    
    /**
     * 处理账单记录列表（核心去重逻辑）
     * @param records 账单记录列表
     * @param importId 导入记录ID
     * @return 导入统计信息
     */
    ImportResult processRecords(List<CmbBillRecordReal> records, Long importId);
    
    /**
     * 更新清洗后的记录到数据库
     * @param records 清洗后的记录
     * @return 更新的记录数
     */
    int updateCleanedRecords(List<CmbBillRecordReal> records);
    
    /**
     * 导入结果统计
     */
    class ImportResult {
        public int newCount = 0;
        public int duplicateCount = 0;
        public int updateCount = 0;
    }
}
