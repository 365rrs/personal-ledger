package com.gaoyan.personalledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 招商银行账单记录实体类（导出格式）
 */
@Data
public class CmbBillRecordExport {
    
    /**
     * 交易日期
     */
    @ExcelProperty("交易日期")
    private String tradeDate;
    
    /**
     * 交易时间
     */
    @ExcelProperty("交易时间")
    private String tradeTime;
    
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
    private String tradeType;
    
    /**
     * 交易备注
     */
    @ExcelProperty("交易备注")
    private String remark;
    
    /**
     * 是否计入本月收支标识
     * 转换为可读文本: "是" 或 "否"
     */
    @ExcelProperty("是否计入本月收支")
    private String excludeFromMonthlyText;
    
    /**
     * 支付渠道（微信/支付宝/银行卡/京东支付）
     */
    @ExcelProperty("支付渠道")
    private String paymentChannel;
    
    /**
     * 交易类型（收入/支出）
     */
    @ExcelProperty("交易类型")
    private String transactionType;
    
    /**
     * 分类（餐饮/购物/出行）
     */
    @ExcelProperty("分类")
    private String category;
    
    /**
     * 用户备注
     */
    @ExcelProperty("用户备注")
    private String userRemark;
    
    /**
     * 将CmbBillRecordReal转换为CmbBillRecordExport
     * @param record 原始记录
     * @return 导出记录
     */
    public static CmbBillRecordExport fromCmbBillRecordReal(CmbBillRecordReal record) {
        CmbBillRecordExport export = new CmbBillRecordExport();
        export.setTradeDate(record.getTradeDate());
        export.setTradeTime(record.getTradeTime());
        export.setIncome(record.getIncome());
        export.setExpense(record.getExpense());
        export.setBalance(record.getBalance());
        export.setTradeType(record.getTradeType());
        export.setRemark(record.getRemark());
        
        // 新增字段
        export.setPaymentChannel(record.getPaymentChannel());
        export.setTransactionType(record.getTransactionType());
        export.setCategory(record.getCategory());
        export.setUserRemark(record.getUserRemark());
        
        // 将布尔值转换为可读文本
        if (record.getExcludeFromMonthly() != null && record.getExcludeFromMonthly()) {
            export.setExcludeFromMonthlyText("是");
        } else {
            export.setExcludeFromMonthlyText("否");
        }
        
        return export;
    }
}