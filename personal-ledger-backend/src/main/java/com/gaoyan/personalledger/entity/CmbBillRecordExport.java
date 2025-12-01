package com.gaoyan.personalledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 招商银行账单记录实体类（导出格式）
 */
@Data
public class CmbBillRecordExport {
    
    /**
     * 交易日期
     */
    @ExcelProperty("交易日期")
    private LocalDate transactionDate;
    
    /**
     * 交易时间
     */
    @ExcelProperty("交易时间")
    private LocalTime transactionTime;
    
    /**
     * 收入
     */
    @ExcelProperty("收入")
    private BigDecimal income;
    
    /**
     * 支出
     */
    @ExcelProperty("支出")
    private BigDecimal expense;
    
    /**
     * 余额
     */
    @ExcelProperty("余额")
    private BigDecimal balance;
    
    /**
     * 交易类型
     */
    @ExcelProperty("交易类型")
    private String transactionType;
    
    /**
     * 交易描述
     */
    @ExcelProperty("交易备注")
    private String description;
    
    /**
     * 是否排除统计(0-计入 1-排除)
     * 转换为可读文本: "是" 或 "否"
     */
    @ExcelProperty("是否计入本月收支")
    private String excludeFromStatsText;
    
    /**
     * 支付渠道（微信/支付宝/银行卡/京东支付）
     */
    @ExcelProperty("支付渠道")
    private String paymentChannel;
    
    /**
     * 收支类型
     */
    @ExcelProperty("收支类型")
    private String incomeOrExpense;
    
    /**
     * 交易分类
     */
    @ExcelProperty("分类")
    private String category;
    
    /**
     * 用户备注
     */
    @ExcelProperty("用户备注")
    private String userNote;
    
    /**
     * 将真实格式记录转换为导出格式记录
     */
    public static CmbBillRecordExport fromCmbBillRecordReal(CmbBillRecordReal real) {
        CmbBillRecordExport export = new CmbBillRecordExport();
        export.setTransactionDate(real.getTransactionDate());
        export.setTransactionTime(real.getTransactionTime());
        export.setIncome(real.getIncome());
        export.setExpense(real.getExpense());
        export.setBalance(real.getBalance());
        export.setTransactionType(real.getTransactionType());
        export.setDescription(real.getDescription());
        export.setPaymentChannel(real.getPaymentChannel());
        export.setCategory(real.getCategory());
        export.setUserNote(real.getUserNote());
        
        // 设置收支类型
        if (real.getIncome() != null && real.getIncome().compareTo(BigDecimal.ZERO) > 0) {
            export.setIncomeOrExpense("收入");
        } else if (real.getExpense() != null && real.getExpense().compareTo(BigDecimal.ZERO) > 0) {
            export.setIncomeOrExpense("支出");
        } else {
            export.setIncomeOrExpense("");
        }
        
        // 转换是否排除统计标识为可读文本
        if (real.getExcludeFromStats() != null && !real.getExcludeFromStats()) {
            export.setExcludeFromStatsText("是");
        } else {
            export.setExcludeFromStatsText("否");
        }
        
        return export;
    }
}