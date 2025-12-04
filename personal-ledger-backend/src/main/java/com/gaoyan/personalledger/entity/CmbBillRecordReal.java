package com.gaoyan.personalledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 招商银行账单记录实体类（真实格式）
 */
@Data
public class CmbBillRecordReal {
    
    /**
     * ID
     */
    private Long id;
    
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
     * 是否计入收支(true-计入 false-不计入)
     */
    @ExcelProperty("是否计入收支标识")
    private Boolean includeInStats = true;
    
    /**
     * 支付渠道（微信/支付宝/银行卡/京东支付）
     */
    private String paymentChannel;
    
    /**
     * 交易分类
     */
    private String category;
    
    /**
     * 用户备注
     */
    private String userNote;
    
    /**
     * 格式化后的交易日期 (yyyy-MM-dd)
     */
    private String formattedTradeDate;
    
    /**
     * 设置格式化后的交易日期
     * @param formattedTradeDate 格式化后的日期
     */
    public void setFormattedTradeDate(String formattedTradeDate) {
        this.formattedTradeDate = formattedTradeDate;
    }
    
    /**
     * 获取格式化后的交易日期 (yyyy-MM-dd)
     * @return 格式化后的日期字符串
     */
    public String getFormattedTradeDate() {
        if (formattedTradeDate != null) {
            return formattedTradeDate;
        }
        
        if (transactionDate == null) {
            return null;
        }
        
        return transactionDate.toString();
    }
}