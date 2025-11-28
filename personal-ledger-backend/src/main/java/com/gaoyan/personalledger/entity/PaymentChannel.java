package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付渠道实体
 */
@Data
@TableName("payment_channel")
public class PaymentChannel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 渠道名称
     */
    private String name;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
