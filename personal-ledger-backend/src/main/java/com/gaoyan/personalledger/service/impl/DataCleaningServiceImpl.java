package com.gaoyan.personalledger.service.impl;

import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import com.gaoyan.personalledger.entity.CleaningRule;
import com.gaoyan.personalledger.service.CleaningRuleService;
import com.gaoyan.personalledger.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataCleaningServiceImpl implements DataCleaningService {

    @Autowired
    private CleaningRuleService cleaningRuleService;

    @Override
    public List<CmbBillRecordReal> cleanBillRecords(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        log.info("开始清洗账单记录数据，原始记录数: {}", records.size());

        List<CmbBillRecordReal> cleanedRecords = removeDuplicates(records);
        cleanRemarks(cleanedRecords);
        cleanedRecords = formatDateFields(cleanedRecords);
        cleanedRecords = removeInvalidData(cleanedRecords);
        cleanedRecords = cleanPaymentChannels(cleanedRecords);
        cleanedRecords = determineTransactionTypes(cleanedRecords);
        cleanedRecords = categorizeRecords(cleanedRecords);
        processExcludedRecords(cleanedRecords);

        log.info("账单记录数据清洗完成，清洗后记录数: {}", cleanedRecords.size());
        return cleanedRecords;
    }

    @Override
    public List<CmbBillRecordReal> removeDuplicates(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        Set<String> uniqueKeys = new LinkedHashSet<>();
        List<CmbBillRecordReal> uniqueRecords = new ArrayList<>();

        for (CmbBillRecordReal record : records) {
            String uniqueKey = String.format("%s_%s_%s_%s_%s",
                    record.getTradeDate(),
                    record.getTradeTime(),
                    record.getIncome() != null ? record.getIncome().toString() : "",
                    record.getExpense() != null ? record.getExpense().toString() : "",
                    record.getTradeType());

            if (!uniqueKeys.contains(uniqueKey)) {
                uniqueKeys.add(uniqueKey);
                uniqueRecords.add(record);
            }
        }

        log.info("移除重复记录: 原始{}条，去重后{}条", records.size(), uniqueRecords.size());
        return uniqueRecords;
    }

    @Override
    public List<CmbBillRecordReal> formatDateFields(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        for (CmbBillRecordReal record : records) {
            if (StringUtils.hasText(record.getTradeDate())) {
                record.setTradeDate(formatDate(record.getTradeDate()));
            }
            if (StringUtils.hasText(record.getTradeTime())) {
                record.setTradeTime(formatTime(record.getTradeTime()));
            }
        }

        return records;
    }

    @Override
    public List<CmbBillRecordReal> removeInvalidData(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        return records.stream()
                .filter(this::isValidRecord)
                .map(this::normalizeRecord)
                .collect(Collectors.toList());
    }

    @Override
    public List<CmbBillRecordReal> cleanPaymentChannels(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            if (record.getPaymentChannel() == null || record.getPaymentChannel().isEmpty()) {
                String paymentChannel = identifyPaymentChannel(record);
                if (paymentChannel != null) {
                    record.setPaymentChannel(paymentChannel);
                    count++;
                }
            }
        }

        log.info("完成支付渠道清洗，共识别{}条记录", count);
        return records;
    }

    @Override
    public List<CmbBillRecordReal> determineTransactionTypes(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            if (record.getTransactionType() == null || record.getTransactionType().isEmpty()) {
                String transactionType = determineTransactionType(record);
                if (transactionType != null) {
                    record.setTransactionType(transactionType);
                    count++;
                }
            }
        }

        log.info("完成收支类型判断，共处理{}条记录", count);
        return records;
    }

    @Override
    public List<CmbBillRecordReal> categorizeRecords(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            if (record.getCategory() == null || record.getCategory().isEmpty()) {
                String category = identifyCategory(record);
                if (category != null) {
                    record.setCategory(category);
                    count++;
                }
            }
        }

        log.info("完成账单分类，共识别{}条记录", count);
        return records;
    }

    @Override
    public List<CmbBillRecordReal> cleanRemarks(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        List<CleaningRule> rules = cleaningRuleService.getEnabledRulesByType("REMARK_CLEANING");
        int count = 0;
        
        for (CmbBillRecordReal record : records) {
            String originalRemark = record.getRemark();
            if (originalRemark != null && !originalRemark.isEmpty()) {
                for (CleaningRule rule : rules) {
                    if (matchText(originalRemark, rule.getKeyword(), rule.getMatchMode())) {
                        record.setRemark(rule.getTargetValue());
                        count++;
                        break;
                    }
                }
            }
        }

        log.info("完成交易备注清洗，共清洗{}条记录", count);
        return records;
    }

    private void processExcludedRecords(List<CmbBillRecordReal> records) {
        for (CmbBillRecordReal record : records) {
            if ("汇入汇款".equals(record.getTradeType()) && "高明希".equals(record.getRemark())) {
                record.setExcludeFromMonthly(false);
            }
        }
    }

    private boolean isValidRecord(CmbBillRecordReal record) {
        if (record.getTradeDate() == null || record.getTradeDate().trim().isEmpty()) {
            return false;
        }
        if ((record.getIncome() == null || record.getIncome().compareTo(BigDecimal.ZERO) == 0) &&
                (record.getExpense() == null || record.getExpense().compareTo(BigDecimal.ZERO) == 0)) {
            return false;
        }
        return true;
    }

    private CmbBillRecordReal normalizeRecord(CmbBillRecordReal record) {
        if (record.getRemark() != null) {
            record.setRemark(record.getRemark().trim());
        } else {
            record.setRemark("");
        }
        if (record.getTradeType() != null) {
            record.setTradeType(record.getTradeType().trim());
        } else {
            record.setTradeType("");
        }
        if (record.getExcludeFromMonthly() == null) {
            record.setExcludeFromMonthly(true);
        }
        return record;
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }
        String date = dateStr.trim().replaceAll("^[\\s\\t]*", "");
        if (date.length() == 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }
        return dateStr;
    }

    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return timeStr;
        }
        String time = timeStr.trim().replaceAll("^[\\s\\t]*", "");
        if (time.length() == 6) {
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        }
        return timeStr;
    }

    private String identifyPaymentChannel(CmbBillRecordReal record) {
        List<CleaningRule> rules = cleaningRuleService.getEnabledRulesByType("PAYMENT_CHANNEL");
        return applyRules(record, rules);
    }

    private String determineTransactionType(CmbBillRecordReal record) {
        if (record.getIncome() != null && record.getIncome().compareTo(BigDecimal.ZERO) > 0) {
            return "收入";
        } else if (record.getExpense() != null && record.getExpense().compareTo(BigDecimal.ZERO) > 0) {
            return "支出";
        }
        return null;
    }

    private String identifyCategory(CmbBillRecordReal record) {
        List<CleaningRule> rules = cleaningRuleService.getEnabledRulesByType("CATEGORY");
        return applyRules(record, rules);
    }

    private String applyRules(CmbBillRecordReal record, List<CleaningRule> rules) {
        String remark = record.getRemark();
        String tradeType = record.getTradeType();
        String userRemark = record.getUserRemark();

        for (CleaningRule rule : rules) {
            if (userRemark != null && !userRemark.isEmpty()) {
                if (matchText(userRemark, rule.getKeyword(), rule.getMatchMode())) {
                    return rule.getTargetValue();
                }
            }
            if (remark != null && !remark.isEmpty()) {
                if (matchText(remark, rule.getKeyword(), rule.getMatchMode())) {
                    return rule.getTargetValue();
                }
            }
            if (tradeType != null && !tradeType.isEmpty()) {
                if (matchText(tradeType, rule.getKeyword(), rule.getMatchMode())) {
                    return rule.getTargetValue();
                }
            }
        }
        return null;
    }

    private boolean matchText(String text, String keyword, String matchMode) {
        if ("EXACT".equals(matchMode)) {
            return text.equals(keyword);
        } else if ("CONTAINS".equals(matchMode)) {
            return text.toLowerCase().contains(keyword.toLowerCase());
        } else if ("REGEX".equals(matchMode)) {
            try {
                return text.matches(keyword);
            } catch (Exception e) {
                log.warn("正则表达式匹配失败: {}", keyword, e);
                return false;
            }
        }
        return false;
    }
}
