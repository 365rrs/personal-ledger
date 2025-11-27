package com.gaoyan.personalledger.entity;

import lombok.Data;

/**
 * 招商银行账单导出信息实体类
 */
@Data
public class CmbExportInfo {
    
    /**
     * 导出时间
     */
    private String exportTime;
    
    /**
     * 账号信息
     */
    private String account;
    
    /**
     * 币种
     */
    private String currency;
    
    /**
     * 起始日期
     */
    private String startDate;
    
    /**
     * 终止日期
     */
    private String endDate;
    
    /**
     * 过滤设置
     */
    private String filterSetting;
}