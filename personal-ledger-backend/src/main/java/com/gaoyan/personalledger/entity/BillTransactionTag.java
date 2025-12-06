package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bill_transaction_tag")
public class BillTransactionTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long transactionId;
    private Long tagId;
    private LocalDateTime createTime;
}
