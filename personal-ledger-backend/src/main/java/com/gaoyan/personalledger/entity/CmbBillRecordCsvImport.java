package com.gaoyan.personalledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 招商银行账单记录实体类（CSV导入格式）
 * 所有字段均为String类型，用于在导入阶段接收原始数据
 * 专门用于处理招商银行导出的CSV格式账单文件
 */
@Data
public class CmbBillRecordCsvImport {
    
    /**
     * 交易日期
     */
    @ExcelProperty("交易日期")
    private String transactionDate;
    
    /**
     * 交易时间
     */
    @ExcelProperty("交易时间")
    private String transactionTime;
    
    /**
     * 收入
     */
    @ExcelProperty("收入")
    private String income;
    
    /**
     * 支出
     */
    @ExcelProperty("支出")
    private String expense;
    
    /**
     * 余额
     */
    @ExcelProperty("余额")
    private String balance;
    
    /**
     * 交易类型
     */
    @ExcelProperty("交易类型")
    private String transactionType;
    
    /**
     * 交易描述/交易备注
     */
    @ExcelProperty("交易备注")
    private String description;
}