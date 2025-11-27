package com.gaoyan.personalledger.entity;

import lombok.Data;

/**
 * 招商银行账单统计信息实体类
 */
@Data
public class CmbSummaryInfo {
    
    /**
     * 收入笔数
     */
    private Integer incomeCount;
    
    /**
     * 收入金额
     */
    private String incomeAmount;
    
    /**
     * 支出笔数
     */
    private Integer expenseCount;
    
    /**
     * 支出金额
     */
    private String expenseAmount;
}