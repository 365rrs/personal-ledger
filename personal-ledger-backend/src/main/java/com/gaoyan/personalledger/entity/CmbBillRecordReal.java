package com.gaoyan.personalledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 招商银行账单记录实体类（真实格式）
 */
@Data
public class CmbBillRecordReal {
    
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
     * true: 计入本月收支（默认）
     * false: 不计入本月收支
     */
    @ExcelProperty("是否计入本月收支标识")
    private Boolean excludeFromMonthly = true;
    
    /**
     * 支付渠道（微信/支付宝/银行卡/京东支付）
     */
    private String paymentChannel;
    
    /**
     * 交易类型（收入/支出）
     */
    private String transactionType;
    
    /**
     * 分类（餐饮/购物/出行）
     */
    private String category;
    
    /**
     * 用户备注
     */
    private String userRemark;
    
    /**
     * 获取格式化后的交易日期 (yyyy-MM-dd)
     * @return 格式化后的日期字符串
     */
    public String getFormattedTradeDate() {
        if (tradeDate == null || tradeDate.trim().isEmpty()) {
            return tradeDate;
        }
        
        String date = tradeDate.trim();
        // 移除可能的前导字符（如制表符或空格）
        date = date.replaceAll("^[\\s\\t]*", "");
        
        if (date.length() == 8) {
            // 将 YYYYMMDD 格式转换为 YYYY-MM-DD 格式
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        
        return tradeDate;
    }
}