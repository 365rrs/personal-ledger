package com.gaoyan.personalledger.entity;

import lombok.Data;
import java.util.List;

/**
 * 招商银行账单完整信息实体类
 */
@Data
public class CmbBillInfo {
    
    /**
     * 导入信息
     */
    private CmbImportInfo importInfo;
    
    /**
     * 交易记录列表
     */
    private List<CmbBillRecordReal> records;
    
    /**
     * 统计信息
     */
    private CmbSummaryInfo summaryInfo;
}