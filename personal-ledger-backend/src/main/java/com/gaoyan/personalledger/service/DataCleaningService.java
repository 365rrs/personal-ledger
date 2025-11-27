package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.CmbBillRecordReal;

import java.util.List;

/**
 * 数据清洗服务接口
 */
public interface DataCleaningService {
    
    /**
     * 清洗账单记录数据
     * @param records 原始账单记录列表
     * @return 清洗后的账单记录列表
     */
    List<CmbBillRecordReal> cleanBillRecords(List<CmbBillRecordReal> records);
    
    /**
     * 移除重复记录
     * @param records 原始账单记录列表
     * @return 去重后的账单记录列表
     */
    List<CmbBillRecordReal> removeDuplicates(List<CmbBillRecordReal> records);
    
    /**
     * 格式化日期字段
     * @param records 原始账单记录列表
     * @return 日期格式化后的账单记录列表
     */
    List<CmbBillRecordReal> formatDateFields(List<CmbBillRecordReal> records);
    
    /**
     * 清理空值和无效数据
     * @param records 原始账单记录列表
     * @return 清理后的账单记录列表
     */
    List<CmbBillRecordReal> removeInvalidData(List<CmbBillRecordReal> records);
    
    /**
     * 清洗支付渠道
     * @param records 原始账单记录列表
     * @return 支付渠道清洗后的账单记录列表
     */
    List<CmbBillRecordReal> cleanPaymentChannels(List<CmbBillRecordReal> records);
    
    /**
     * 确定收支类型
     * @param records 原始账单记录列表
     * @return 收支类型确定后的账单记录列表
     */
    List<CmbBillRecordReal> determineTransactionTypes(List<CmbBillRecordReal> records);
    
    /**
     * 分类账单记录
     * @param records 原始账单记录列表
     * @return 分类后的账单记录列表
     */
    List<CmbBillRecordReal> categorizeRecords(List<CmbBillRecordReal> records);
    
    /**
     * 清洗交易备注
     * @param records 原始账单记录列表
     * @return 交易备注清洗后的账单记录列表
     */
    List<CmbBillRecordReal> cleanRemarks(List<CmbBillRecordReal> records);
}