package com.gaoyan.personalledger.service.impl;

import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import com.gaoyan.personalledger.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据清洗服务实现类
 */
@Slf4j
@Service
public class DataCleaningServiceImpl implements DataCleaningService {

    // 支付渠道关键词
    private static final Map<String, String> PAYMENT_CHANNEL_KEYWORDS = new HashMap<>();

    static {
        PAYMENT_CHANNEL_KEYWORDS.put("微信", "微信");
        PAYMENT_CHANNEL_KEYWORDS.put("支付宝", "支付宝");
        PAYMENT_CHANNEL_KEYWORDS.put("财付通", "微信");
        PAYMENT_CHANNEL_KEYWORDS.put("京东支付", "京东支付");
    }

    // 分类关键词
    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // 餐饮类
        CATEGORY_KEYWORDS.put("餐厅", "餐饮");
        CATEGORY_KEYWORDS.put("饭店", "餐饮");
        CATEGORY_KEYWORDS.put("餐饮", "餐饮");
        CATEGORY_KEYWORDS.put("coffee", "餐饮");
        CATEGORY_KEYWORDS.put("coffeebean", "餐饮");
        CATEGORY_KEYWORDS.put("星巴克", "餐饮");
        CATEGORY_KEYWORDS.put("瑞幸", "餐饮");
        CATEGORY_KEYWORDS.put("kfc", "餐饮");
        CATEGORY_KEYWORDS.put("麦当劳", "餐饮");
        CATEGORY_KEYWORDS.put("肯德基", "餐饮");
        CATEGORY_KEYWORDS.put("海底捞", "餐饮");
        CATEGORY_KEYWORDS.put("火锅", "餐饮");
        CATEGORY_KEYWORDS.put("快餐", "餐饮");
        CATEGORY_KEYWORDS.put("烧烤", "餐饮");
        CATEGORY_KEYWORDS.put("奶茶", "餐饮");
        CATEGORY_KEYWORDS.put("茶饮", "餐饮");
        CATEGORY_KEYWORDS.put("面馆", "餐饮");
        CATEGORY_KEYWORDS.put("小吃", "餐饮");
        CATEGORY_KEYWORDS.put("食堂", "餐饮");

        // 外卖类
        CATEGORY_KEYWORDS.put("外卖", "外卖");
        CATEGORY_KEYWORDS.put("饿了么", "外卖");
        CATEGORY_KEYWORDS.put("美团外卖", "外卖");
        CATEGORY_KEYWORDS.put("拉扎斯", "外卖");

        // 购物类
        CATEGORY_KEYWORDS.put("购物", "购物");
        CATEGORY_KEYWORDS.put("商场", "购物");
        CATEGORY_KEYWORDS.put("超市", "购物");
        CATEGORY_KEYWORDS.put("京东", "购物");
        CATEGORY_KEYWORDS.put("天猫", "购物");
        CATEGORY_KEYWORDS.put("淘宝", "购物");
        CATEGORY_KEYWORDS.put("拼多多", "购物");
        CATEGORY_KEYWORDS.put("苏宁", "购物");
        CATEGORY_KEYWORDS.put("小米", "购物");
        CATEGORY_KEYWORDS.put("华为", "购物");
        CATEGORY_KEYWORDS.put("苹果", "购物");
        CATEGORY_KEYWORDS.put("唯品会", "购物");
        CATEGORY_KEYWORDS.put("便利店", "购物");
        CATEGORY_KEYWORDS.put("7-11", "购物");
        CATEGORY_KEYWORDS.put("全家", "购物");

        // 出行类
        CATEGORY_KEYWORDS.put("滴滴", "出行");
        CATEGORY_KEYWORDS.put("出行", "出行");
        CATEGORY_KEYWORDS.put("打车", "出行");
        CATEGORY_KEYWORDS.put("地铁", "出行");
        CATEGORY_KEYWORDS.put("公交", "出行");
        CATEGORY_KEYWORDS.put("火车票", "出行");
        CATEGORY_KEYWORDS.put("机票", "出行");
        CATEGORY_KEYWORDS.put("uber", "出行");
        CATEGORY_KEYWORDS.put("出租车", "出行");
        CATEGORY_KEYWORDS.put("高铁", "出行");
        CATEGORY_KEYWORDS.put("中铁网络", "出行");
        CATEGORY_KEYWORDS.put("加油", "出行");
        CATEGORY_KEYWORDS.put("停车", "出行");
        CATEGORY_KEYWORDS.put("etc", "出行");
        CATEGORY_KEYWORDS.put("共享单车", "出行");
        CATEGORY_KEYWORDS.put("哈啰", "出行");
        CATEGORY_KEYWORDS.put("美团单车", "出行");

        // 娱乐类
        CATEGORY_KEYWORDS.put("电影", "娱乐");
        CATEGORY_KEYWORDS.put("影院", "娱乐");
        CATEGORY_KEYWORDS.put("ktv", "娱乐");
        CATEGORY_KEYWORDS.put("游戏", "娱乐");
        CATEGORY_KEYWORDS.put("网吧", "娱乐");
        CATEGORY_KEYWORDS.put("健身", "娱乐");
        CATEGORY_KEYWORDS.put("运动", "娱乐");
        CATEGORY_KEYWORDS.put("旅游", "娱乐");
        CATEGORY_KEYWORDS.put("酒店", "娱乐");

        // 生活服务类
        CATEGORY_KEYWORDS.put("理发", "生活服务");
        CATEGORY_KEYWORDS.put("美容", "生活服务");
        CATEGORY_KEYWORDS.put("美发", "生活服务");
        CATEGORY_KEYWORDS.put("洗衣", "生活服务");
        CATEGORY_KEYWORDS.put("维修", "生活服务");
        CATEGORY_KEYWORDS.put("快递", "生活服务");
        CATEGORY_KEYWORDS.put("物业", "生活服务");
        CATEGORY_KEYWORDS.put("水电费", "生活服务");
        CATEGORY_KEYWORDS.put("话费", "生活服务");
        CATEGORY_KEYWORDS.put("充值", "生活服务");

        // 医疗健康类
        CATEGORY_KEYWORDS.put("医院", "医疗健康");
        CATEGORY_KEYWORDS.put("药店", "医疗健康");
        CATEGORY_KEYWORDS.put("体检", "医疗健康");
        CATEGORY_KEYWORDS.put("挂号", "医疗健康");
        CATEGORY_KEYWORDS.put("医疗", "医疗健康");

        // 教育类
        CATEGORY_KEYWORDS.put("培训", "教育");
        CATEGORY_KEYWORDS.put("课程", "教育");
        CATEGORY_KEYWORDS.put("书店", "教育");
        CATEGORY_KEYWORDS.put("教育", "教育");

        // 转账类
        CATEGORY_KEYWORDS.put("转账", "转账");
        CATEGORY_KEYWORDS.put("红包", "转账");
        CATEGORY_KEYWORDS.put("汇款", "转账");

        // 投资理财类
        CATEGORY_KEYWORDS.put("理财", "投资理财");
        CATEGORY_KEYWORDS.put("基金", "投资理财");
        CATEGORY_KEYWORDS.put("股票", "投资理财");
        CATEGORY_KEYWORDS.put("保险", "投资理财");
    }

    // 交易备注清洗规则
    private static final Map<String, String> REMARK_CLEANING_RULES = new HashMap<>();

    static {
        REMARK_CLEANING_RULES.put("测试", "测试");
    }

    /**
     * 清洗账单记录数据
     *
     * @param records 原始账单记录列表
     * @return 清洗后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> cleanBillRecords(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        log.info("开始清洗账单记录数据，原始记录数: {}", records.size());

        // 1. 移除重复记录
        List<CmbBillRecordReal> cleanedRecords = removeDuplicates(records);

        // 2. 清洗交易备注（按用户要求，首先执行）
        cleanRemarks(cleanedRecords);

        // 3. 格式化日期字段
        cleanedRecords = formatDateFields(cleanedRecords);

        // 4. 清理空值和无效数据
        cleanedRecords = removeInvalidData(cleanedRecords);

        // 5. 清洗支付渠道
        cleanedRecords = cleanPaymentChannels(cleanedRecords);

        // 6. 确定收支类型
        cleanedRecords = determineTransactionTypes(cleanedRecords);

        // 7. 分类账单记录
        cleanedRecords = categorizeRecords(cleanedRecords);

        // 8. 处理是否计入本月收支的记录
        processExcludedRecords(cleanedRecords);

        log.info("账单记录数据清洗完成，清洗后记录数: {}", cleanedRecords.size());
        return cleanedRecords;
    }

    /**
     * 移除重复记录
     *
     * @param records 原始账单记录列表
     * @return 去重后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> removeDuplicates(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        // 使用LinkedHashSet保持顺序并去重
        Set<String> uniqueKeys = new LinkedHashSet<>();
        List<CmbBillRecordReal> uniqueRecords = new ArrayList<>();

        for (CmbBillRecordReal record : records) {
            // 创建唯一键：交易日期+交易时间+收入+支出+交易类型
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

    /**
     * 格式化日期字段
     *
     * @param records 原始账单记录列表
     * @return 日期格式化后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> formatDateFields(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        for (CmbBillRecordReal record : records) {
            // 格式化交易日期
            if (StringUtils.hasText(record.getTradeDate())) {
                record.setTradeDate(formatDate(record.getTradeDate()));
            }

            // 格式化交易时间
            if (StringUtils.hasText(record.getTradeTime())) {
                record.setTradeTime(formatTime(record.getTradeTime()));
            }
        }

        return records;
    }

    /**
     * 清理空值和无效数据
     *
     * @param records 原始账单记录列表
     * @return 清理后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> removeInvalidData(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        return records.stream()
                .filter(record -> isValidRecord(record))
                .map(this::normalizeRecord)
                .collect(Collectors.toList());
    }

    /**
     * 清洗支付渠道
     *
     * @param records 原始账单记录列表
     * @return 支付渠道清洗后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> cleanPaymentChannels(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            // 只有在支付渠道为空时才进行识别
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

    /**
     * 确定收支类型
     *
     * @param records 原始账单记录列表
     * @return 收支类型确定后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> determineTransactionTypes(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            // 根据收入、支出金额判断收支类型
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

    /**
     * 分类账单记录
     *
     * @param records 原始账单记录列表
     * @return 分类后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> categorizeRecords(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            // 只有在分类为空时才进行识别
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

    /**
     * 清洗交易备注
     *
     * @param records 原始账单记录列表
     * @return 交易备注清洗后的账单记录列表
     */
    @Override
    public List<CmbBillRecordReal> cleanRemarks(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        int count = 0;
        for (CmbBillRecordReal record : records) {
            String originalRemark = record.getRemark();
            if (originalRemark != null && !originalRemark.isEmpty()) {
                // 应用清洗规则
                for (Map.Entry<String, String> entry : REMARK_CLEANING_RULES.entrySet()) {
                    if (originalRemark.equals(entry.getKey())) {
                        record.setRemark(entry.getValue());
                        count++;
                        break;
                    }
                }
            }
        }

        log.info("完成交易备注清洗，共清洗{}条记录", count);
        return records;
    }

    /**
     * 处理是否计入本月收支的记录
     *
     * @param records 账单记录列表
     */
    private void processExcludedRecords(List<CmbBillRecordReal> records) {
        // 处理是否计入本月收支的记录
        for (CmbBillRecordReal record : records) {
            // 将交易类型为"汇入汇款"且交易备注是"高明希"的记录标记为不计入本月收支
            if ("汇入汇款".equals(record.getTradeType()) && "高明希".equals(record.getRemark())) {
                record.setExcludeFromMonthly(false);
            }
        }

        log.info("完成特殊记录处理，共处理{}条记录", records.size());
    }

    /**
     * 检查记录是否有效
     *
     * @param record 账单记录
     * @return 是否有效
     */
    private boolean isValidRecord(CmbBillRecordReal record) {
        // 检查必要字段是否为空
        if (record.getTradeDate() == null || record.getTradeDate().trim().isEmpty()) {
            return false;
        }

        // 检查收入和支出是否都为空
        if ((record.getIncome() == null || record.getIncome().compareTo(BigDecimal.ZERO) == 0) &&
                (record.getExpense() == null || record.getExpense().compareTo(BigDecimal.ZERO) == 0)) {
            return false;
        }

        return true;
    }

    /**
     * 标准化记录数据
     *
     * @param record 原始记录
     * @return 标准化后的记录
     */
    private CmbBillRecordReal normalizeRecord(CmbBillRecordReal record) {
        // 标准化交易备注
        if (record.getRemark() != null) {
            record.setRemark(record.getRemark().trim());
        } else {
            record.setRemark("");
        }

        // 标准化交易类型
        if (record.getTradeType() != null) {
            record.setTradeType(record.getTradeType().trim());
        } else {
            record.setTradeType("");
        }

        // 确保excludeFromMonthly字段有默认值
        if (record.getExcludeFromMonthly() == null) {
            record.setExcludeFromMonthly(true);
        }

        return record;
    }

    /**
     * 格式化日期
     *
     * @param dateStr 原始日期字符串
     * @return 格式化后的日期字符串
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return dateStr;
        }

        String date = dateStr.trim();
        // 移除可能的前导字符（如制表符或空格）
        date = date.replaceAll("^[\\s\\t]*", "");

        if (date.length() == 8) {
            // 将 YYYYMMDD 格式转换为 YYYY-MM-DD 格式
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        }

        return dateStr;
    }

    /**
     * 格式化时间
     *
     * @param timeStr 原始时间字符串
     * @return 格式化后的时间字符串
     */
    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return timeStr;
        }

        String time = timeStr.trim();
        // 移除可能的前导字符
        time = time.replaceAll("^[\\s\\t]*", "");

        if (time.length() == 6) {
            // 将 HHMMSS 格式转换为 HH:MM:SS 格式
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        }

        return timeStr;
    }

    /**
     * 识别支付渠道
     *
     * @param record 账单记录
     * @return 支付渠道
     */
    private String identifyPaymentChannel(CmbBillRecordReal record) {
        // 检查交易备注中是否包含支付渠道关键词
        String remark = record.getRemark();
        if (remark != null && !remark.isEmpty()) {
            for (Map.Entry<String, String> entry : PAYMENT_CHANNEL_KEYWORDS.entrySet()) {
                if (remark.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        // 检查交易类型中是否包含支付渠道关键词
        String tradeType = record.getTradeType();
        if (tradeType != null && !tradeType.isEmpty()) {
            for (Map.Entry<String, String> entry : PAYMENT_CHANNEL_KEYWORDS.entrySet()) {
                if (tradeType.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 确定收支类型
     *
     * @param record 账单记录
     * @return 收支类型
     */
    private String determineTransactionType(CmbBillRecordReal record) {
        // 根据收入、支出金额判断
        if (record.getIncome() != null && record.getIncome().compareTo(BigDecimal.ZERO) > 0) {
            return "收入";
        } else if (record.getExpense() != null && record.getExpense().compareTo(BigDecimal.ZERO) > 0) {
            return "支出";
        }
        return null;
    }

    /**
     * 识别分类
     *
     * @param record 账单记录
     * @return 分类
     */
    private String identifyCategory(CmbBillRecordReal record) {
        // 优先检查用户备注中是否包含分类关键词
        String userRemark = record.getUserRemark();
        if (userRemark != null && !userRemark.isEmpty()) {
            for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
                if (userRemark.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    return entry.getValue();
                }
            }
        }
        
        // 检查交易备注中是否包含分类关键词
        String remark = record.getRemark();
        if (remark != null && !remark.isEmpty()) {
            for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
                if (remark.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    return entry.getValue();
                }
            }
        }

        // 检查交易类型中是否包含分类关键词
        String tradeType = record.getTradeType();
        if (tradeType != null && !tradeType.isEmpty()) {
            for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
                if (tradeType.toLowerCase().contains(entry.getKey().toLowerCase())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }
}