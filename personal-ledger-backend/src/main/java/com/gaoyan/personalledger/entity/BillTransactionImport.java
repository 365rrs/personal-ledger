package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易导入关联实体
 */
@Data
@TableName("bill_transaction_import")
public class BillTransactionImport {
    
    @TableId(type = IdType.INPUT)
    private Long id;
    
    /**
     * 交易ID
     */
    private Long transactionId;
    
    /**
     * 导入ID
     */
    private Long importId;
    
    /**
     * 是否为新增(1-新增 0-重复)
     */
    private Boolean isNew;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
