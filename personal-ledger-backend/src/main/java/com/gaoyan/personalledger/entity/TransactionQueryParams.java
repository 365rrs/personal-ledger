package com.gaoyan.personalledger.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 交易查询参数
 */
@Data
public class TransactionQueryParams {
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private String category;
    
    private String paymentChannel;
    
    private String transactionType;
    
    private String keyword;
    
    private String minAmount;
    
    private String maxAmount;
    
    private String incomeOrExpense;
    
    private Boolean includeInStats;
    
    private String sortField;
    
    private String sortOrder;
    
    private Long firstImportId;
}
