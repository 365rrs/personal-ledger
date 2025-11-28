package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据清洗规则实体
 */
@Data
@TableName("cleaning_rule")
public class CleaningRule {
    
    /**
     * 规则ID
     */
    @TableId(type = IdType.NONE)
    private Long id;
    
    /**
     * 规则类型：REMARK_CLEANING(备注清洗), PAYMENT_CHANNEL(支付渠道), CATEGORY(分类)
     */
    private String ruleType;
    
    /**
     * 匹配关键词
     */
    private String keyword;
    
    /**
     * 目标值
     */
    private String targetValue;
    
    /**
     * 匹配模式：EXACT(精确匹配), CONTAINS(包含), REGEX(正则表达式)
     */
    private String matchMode;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 备注说明
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
