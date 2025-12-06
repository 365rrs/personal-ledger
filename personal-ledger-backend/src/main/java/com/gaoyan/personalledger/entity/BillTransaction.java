package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 账单交易明细实体
 */
@Data
@TableName("bill_transaction")
public class BillTransaction {
    
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /**
     * 交易日期
     */
    private LocalDate transactionDate;
    
    /**
     * 交易时间
     */
    private LocalTime transactionTime;
    
    /**
     * 收入金额
     */
    private BigDecimal income;
    
    /**
     * 支出金额
     */
    private BigDecimal expense;
    
    /**
     * 账户余额
     */
    private BigDecimal balance;
    
    /**
     * 交易类型
     */
    private String transactionType;
    
    /**
     * 交易描述
     */
    private String description;
    
    /**
     * 支付渠道
     */
    private String paymentChannel;
    
    /**
     * 一级分类
     */
    private String category;
    
    /**
     * 二级分类
     */
    private String subCategory;
    
    /**
     * 用户备注
     */
    private String userNote;
    
    /**
     * 是否计入收支(true-计入 false-不计入)
     */
    private Boolean includeInStats;
    
    /**
     * 是否退款(true-是 false-否)
     */
    private Boolean isRefund;
    
    /**
     * 是否手工记账(true-是 false-否)
     */
    private Boolean isManualEntry;
    
    /**
     * 首次导入记录ID
     */
    private Long firstImportId;
    
    /**
     * 最后更新导入ID
     */
    private Long lastImportId;
    
    /**
     * 导入次数
     */
    private Integer importCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
