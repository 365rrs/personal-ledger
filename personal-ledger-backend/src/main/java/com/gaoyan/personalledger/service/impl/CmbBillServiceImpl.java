package com.gaoyan.personalledger.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.gaoyan.personalledger.entity.*;
import com.gaoyan.personalledger.exception.BusinessException;
import com.gaoyan.personalledger.service.CmbBillService;
import com.gaoyan.personalledger.service.DataCleaningService;
import com.gaoyan.personalledger.util.EasyExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 招商银行账单服务实现类
 */
@Slf4j
@Service
public class CmbBillServiceImpl implements CmbBillService {

    @Autowired
    private DataCleaningService dataCleaningService;
    
    @Autowired
    private com.gaoyan.personalledger.mapper.BillTransactionMapper billTransactionMapper;

    /**
     * 解析招商银行真实格式的账单CSV文件（完整信息）
     *
     * @param file CSV文件
     * @return 完整账单信息
     */
    @Override
    public CmbBillInfo parseCmbBillInfo(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            CmbBillInfo billInfo = new CmbBillInfo();

            // 读取前几行的导出信息
            String[] headerLines = new String[8];
            for (int i = 0; i < 8; i++) {
                headerLines[i] = reader.readLine();
                log.debug("读取第{}行: {}", i, headerLines[i]);
            }

            // 解析导入信息
            CmbImportInfo importInfo = parseImportInfo(headerLines);
            billInfo.setImportInfo(importInfo);

            log.info("解析导入信息结果: {}", importInfo);

            // 跳过第8行的空行，然后使用EasyExcel解析交易记录
            List<CmbBillRecordReal> records = EasyExcel.read(file.getInputStream())
                    .excelType(ExcelTypeEnum.CSV)
                    .head(CmbBillRecordReal.class)
                    .headRowNumber(8)  // 跳过前8行注释（包括空行）
                    .sheet()
                    .doReadSync();

            // 只进行基础数据处理，不进行清洗
            records = processRawData(records);
            billInfo.setRecords(records);

            // 继续读取文件以获取统计信息
            // 跳过交易记录部分和后面的空行
            String line;
            List<String> remainingLines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                remainingLines.add(line);
            }

            // 解析统计信息（最后两行）
            CmbSummaryInfo summaryInfo = parseSummaryInfo(remainingLines);
            billInfo.setSummaryInfo(summaryInfo);

            log.info("成功解析招商银行真实格式账单文件，共{}条记录", records.size());
            return billInfo;
        } catch (IOException e) {
            log.error("解析招商银行真实格式账单文件失败", e);
            throw new BusinessException("解析招商银行真实格式账单文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析导入信息
     *
     * @param headerLines 文件前几行内容
     * @return 导入信息对象
     */
    private CmbImportInfo parseImportInfo(String[] headerLines) {
        CmbImportInfo importInfo = new CmbImportInfo();

        // 解析导入时间
        if (headerLines[1] != null) {
            Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]");
            Matcher matcher = pattern.matcher(headerLines[1]);
            if (matcher.find()) {
                importInfo.setExportTime(matcher.group(1));
            }
        }

        // 解析账号信息
        if (headerLines[2] != null) {
            Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]");
            Matcher matcher = pattern.matcher(headerLines[2]);
            if (matcher.find()) {
                importInfo.setAccount(matcher.group(1));
            }
        }

        // 解析币种
        if (headerLines[3] != null) {
            Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]");
            Matcher matcher = pattern.matcher(headerLines[3]);
            if (matcher.find()) {
                importInfo.setCurrency(matcher.group(1));
            }
        }

        // 解析起始和终止日期
        if (headerLines[4] != null) {
            log.debug("解析第5行内容: {}", headerLines[4]);

            // 匹配整行中的所有日期信息
            Pattern pattern = Pattern.compile("\\[\\s*([^\\[\\]]*?)\\s*\\]");
            Matcher matcher = pattern.matcher(headerLines[4]);

            // 查找第一个匹配项（起始日期）
            if (matcher.find()) {
                importInfo.setStartDate(matcher.group(1).trim());
                log.debug("解析到起始日期: {}", matcher.group(1).trim());

                // 查找第二个匹配项（终止日期）
                if (matcher.find()) {
                    importInfo.setEndDate(matcher.group(1).trim());
                    log.debug("解析到终止日期: {}", matcher.group(1).trim());
                }
            }
        }

        // 解析过滤设置
        if (headerLines[5] != null) {
            Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]|无");
            Matcher matcher = pattern.matcher(headerLines[5]);
            if (matcher.find()) {
                importInfo.setFilterSetting(matcher.group().trim());
            } else {
                importInfo.setFilterSetting("无");
            }
        }

        return importInfo;
    }

    /**
     * 解析统计信息
     *
     * @param remainingLines 文件剩余行内容
     * @return 统计信息对象
     */
    private CmbSummaryInfo parseSummaryInfo(List<String> remainingLines) {
        CmbSummaryInfo summaryInfo = new CmbSummaryInfo();

        // 从剩余行中找到收入和支出统计信息
        for (int i = remainingLines.size() - 2; i < remainingLines.size(); i++) {
            if (i >= 0 && remainingLines.get(i) != null) {
                String line = remainingLines.get(i);
                log.debug("解析统计信息行: {}", line);

                // 解析收入统计
                if (line.contains("收入合计")) {
                    Pattern pattern = Pattern.compile("收入合计:\\s*(\\d+)\\s*笔，共\\s*([\\d.]+)\\s*元");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        summaryInfo.setIncomeCount(Integer.valueOf(matcher.group(1)));
                        summaryInfo.setIncomeAmount(matcher.group(2) + "元");
                        log.debug("解析到收入统计: {}笔，共{}元", matcher.group(1), matcher.group(2));
                    }
                }

                // 解析支出统计
                if (line.contains("支出合计")) {
                    Pattern pattern = Pattern.compile("支出合计:\\s*(\\d+)\\s*笔，共\\s*([\\d.]+)\\s*元");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        summaryInfo.setExpenseCount(Integer.valueOf(matcher.group(1)));
                        summaryInfo.setExpenseAmount(matcher.group(2) + "元");
                        log.debug("解析到支出统计: {}笔，共{}元", matcher.group(1), matcher.group(2));
                    }
                }
            }
        }

        return summaryInfo;
    }

    /**
     * 处理原始数据（仅进行基础格式化）
     *
     * @param records 原始账单记录列表
     * @return 处理后的账单记录列表
     */
    private List<CmbBillRecordReal> processRawData(List<CmbBillRecordReal> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }

        log.info("开始处理原始数据，记录数: {}", records.size());

        for (CmbBillRecordReal record : records) {
            // 格式化交易日期
            if (record.getTransactionDate() != null) {
                record.setFormattedTradeDate(formatDate(record.getTransactionDate().toString()));
            }

            // 格式化交易时间
            if (record.getTransactionTime() != null) {
                record.setTransactionTime(LocalTime.parse(formatTime(record.getTransactionTime().toString())));
            }

            // 设置默认值
            if (record.getExcludeFromStats() == null) {
                record.setExcludeFromStats(true); // 默认计入收支
            }

            // 初始化空字段
            if (record.getUserNote() == null) {
                record.setUserNote("");
            }
            if (record.getPaymentChannel() == null) {
                record.setPaymentChannel("");
            }
            if (record.getTransactionType() == null) {
                record.setTransactionType("");
            }
            if (record.getCategory() == null) {
                record.setCategory("");
            }
        }

        log.info("原始数据处理完成，记录数: {}", records.size());
        return records;
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

        String date = dateStr.trim().replaceAll("^[\\s\\t]*", "");
        if (date.length() == 8) {
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

        String time = timeStr.trim().replaceAll("^[\\s\\t]*", "");
        if (time.length() == 6) {
            return time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        }
        return timeStr;
    }

    /**
     * 解析招商银行Excel账单文件
     *
     * @param file Excel文件
     * @return 完整账单信息
     */
    @Override
    public CmbBillInfo parseExcelBillInfo(MultipartFile file) {
        try {
            log.info("开始解析Excel账单文件: {}", file.getOriginalFilename());
            
            // 使用EasyExcel读取Excel文件
            List<CmbBillRecordExport> exportRecords = EasyExcel.read(file.getInputStream())
                    .head(CmbBillRecordExport.class)
                    .sheet()
                    .doReadSync();
            
            if (exportRecords == null || exportRecords.isEmpty()) {
                throw new BusinessException("Excel文件中没有找到有效数据");
            }
            
            // 将导出格式记录转换为内部格式记录
            List<CmbBillRecordReal> records = exportRecords.stream()
                    .map(this::convertExportToReal)
                    .collect(Collectors.toList());
            
            // 创建账单信息
            CmbBillInfo billInfo = new CmbBillInfo();
            
            // 设置导入信息，尝试从数据中推断账号信息
            CmbImportInfo importInfo = new CmbImportInfo();
            importInfo.setExportTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            
            // 尝试从文件名中推断账号信息
            String inferredAccount = inferAccountFromFileName(file.getOriginalFilename());
            importInfo.setAccount(inferredAccount);
            
            importInfo.setCurrency("人民币");
            importInfo.setStartDate("");
            importInfo.setEndDate("");
            importInfo.setFilterSetting("无");
            billInfo.setImportInfo(importInfo);
            
            // 设置记录
            billInfo.setRecords(records);
            
            // 计算统计信息
            CmbSummaryInfo summaryInfo = calculateSummaryFromRecords(records);
            billInfo.setSummaryInfo(summaryInfo);
            
            log.info("成功解析Excel账单文件，共{}条记录", records.size());
            return billInfo;
            
        } catch (Exception e) {
            log.error("解析Excel账单文件失败", e);
            throw new BusinessException("解析Excel账单文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 将导出格式记录转换为真实格式记录
     */
    private CmbBillRecordReal convertExportToReal(CmbBillRecordExport export) {
        CmbBillRecordReal real = new CmbBillRecordReal();
        real.setTransactionDate(export.getTransactionDate());
        real.setTransactionTime(export.getTransactionTime());
        real.setIncome(export.getIncome());
        real.setExpense(export.getExpense());
        real.setBalance(export.getBalance());
        real.setTransactionType(export.getTransactionType());
        real.setDescription(export.getDescription());
        real.setPaymentChannel(export.getPaymentChannel() != null ? export.getPaymentChannel() : "");
        real.setCategory(export.getCategory() != null ? export.getCategory() : "");
        real.setUserNote(export.getUserNote() != null ? export.getUserNote() : "");
        
        // 转换是否排除统计文本为布尔值
        if ("是".equals(export.getExcludeFromStatsText())) {
            real.setExcludeFromStats(true);
        } else {
            real.setExcludeFromStats(false);
        }
        
        return real;
    }
    
    /**
     * 从文件名推断账号信息
     */
    private String inferAccountFromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "招商银行账户";
        }
        
        // 如果文件名包含账号信息，尝试提取
        if (fileName.contains("招商银行账单")) {
            // 对于系统导出的文件，使用通用账号名称
            return "招商银行账户";
        }
        
        // 其他情况也使用通用账号名称
        return "招商银行账户";
    }
    
    /**
     * 从记录中计算统计信息
     */
    private CmbSummaryInfo calculateSummaryFromRecords(List<CmbBillRecordReal> records) {
        CmbSummaryInfo summaryInfo = new CmbSummaryInfo();
        
        int incomeCount = 0;
        int expenseCount = 0;
        BigDecimal incomeAmount = BigDecimal.ZERO;
        BigDecimal expenseAmount = BigDecimal.ZERO;
        
        for (CmbBillRecordReal record : records) {
            if (record.getIncome() != null && record.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                incomeCount++;
                incomeAmount = incomeAmount.add(record.getIncome());
            }
            if (record.getExpense() != null && record.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                expenseCount++;
                expenseAmount = expenseAmount.add(record.getExpense());
            }
        }
        
        summaryInfo.setIncomeCount(incomeCount);
        summaryInfo.setIncomeAmount(incomeAmount.toPlainString() + "元");
        summaryInfo.setExpenseCount(expenseCount);
        summaryInfo.setExpenseAmount(expenseAmount.toPlainString() + "元");
        
        return summaryInfo;
    }
    
    /**
     * 导出招商银行账单为Excel文件
     *
     * @param billInfo     账单信息
     * @param outputStream 输出流
     */
    @Override
    public void exportCmbBillFromDatabase(com.gaoyan.personalledger.entity.TransactionQueryParams params, ServletOutputStream outputStream) {
        try {
            log.info("从数据库导出账单数据: {}", params);
            
            // 从数据库查询数据
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.gaoyan.personalledger.entity.BillTransaction> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            
            if (params.getStartDate() != null) {
                wrapper.ge(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate, params.getStartDate());
            }
            if (params.getEndDate() != null) {
                wrapper.le(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate, params.getEndDate());
            }
            if (params.getCategory() != null && !params.getCategory().isEmpty()) {
                wrapper.eq(com.gaoyan.personalledger.entity.BillTransaction::getCategory, params.getCategory());
            }
            if (params.getPaymentChannel() != null && !params.getPaymentChannel().isEmpty()) {
                wrapper.eq(com.gaoyan.personalledger.entity.BillTransaction::getPaymentChannel, params.getPaymentChannel());
            }
            if (params.getTransactionType() != null && !params.getTransactionType().isEmpty()) {
                wrapper.like(com.gaoyan.personalledger.entity.BillTransaction::getTransactionType, params.getTransactionType());
            }
            if (params.getKeyword() != null && !params.getKeyword().isEmpty()) {
                wrapper.and(w -> w.like(com.gaoyan.personalledger.entity.BillTransaction::getDescription, params.getKeyword())
                                 .or().like(com.gaoyan.personalledger.entity.BillTransaction::getUserNote, params.getKeyword()));
            }
            if (params.getIncomeOrExpense() != null && !params.getIncomeOrExpense().isEmpty()) {
                if ("income".equals(params.getIncomeOrExpense())) {
                    wrapper.isNotNull(com.gaoyan.personalledger.entity.BillTransaction::getIncome)
                           .gt(com.gaoyan.personalledger.entity.BillTransaction::getIncome, BigDecimal.ZERO);
                } else if ("expense".equals(params.getIncomeOrExpense())) {
                    wrapper.isNotNull(com.gaoyan.personalledger.entity.BillTransaction::getExpense)
                           .gt(com.gaoyan.personalledger.entity.BillTransaction::getExpense, BigDecimal.ZERO);
                }
            }
            if (params.getExcludeFromStats() != null) {
                wrapper.eq(com.gaoyan.personalledger.entity.BillTransaction::getExcludeFromStats, params.getExcludeFromStats());
            }
            
            // 排序
            String sortField = params.getSortField();
            String sortOrder = params.getSortOrder();
            if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
                boolean isAsc = "asc".equals(sortOrder);
                switch (sortField) {
                    case "transactionDate":
                        if (isAsc) wrapper.orderByAsc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate);
                        else wrapper.orderByDesc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate);
                        break;
                    case "transactionTime":
                        if (isAsc) wrapper.orderByAsc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionTime);
                        else wrapper.orderByDesc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionTime);
                        break;
                    case "income":
                        wrapper.last("ORDER BY CAST(income AS DECIMAL) " + (isAsc ? "ASC" : "DESC"));
                        break;
                    case "expense":
                        wrapper.last("ORDER BY CAST(expense AS DECIMAL) " + (isAsc ? "ASC" : "DESC"));
                        break;
                    default:
                        wrapper.orderByDesc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate);
                }
            } else {
                wrapper.orderByDesc(com.gaoyan.personalledger.entity.BillTransaction::getTransactionDate);
            }
            
            List<com.gaoyan.personalledger.entity.BillTransaction> transactions = billTransactionMapper.selectList(wrapper);
            
            // 转换为CmbBillRecordReal
            List<CmbBillRecordReal> records = transactions.stream()
                .map(this::convertTransactionToRecord)
                .collect(Collectors.toList());
            
            // 转换为导出格式
            List<CmbBillRecordExport> exportRecords = records.stream()
                    .map(CmbBillRecordExport::fromCmbBillRecordReal)
                    .collect(Collectors.toList());
            
            // 导出
            EasyExcel.write(outputStream, CmbBillRecordExport.class)
                    .registerConverter(new com.gaoyan.personalledger.util.LocalDateConverter())
                    .registerConverter(new com.gaoyan.personalledger.util.LocalTimeConverter())
                    .registerWriteHandler(EasyExcelExportUtil.generatorHorizontalCellStyleStrategy())
                    .sheet("招商银行账单")
                    .doWrite(exportRecords);
            
            log.info("成功导出{}\u6761记录", exportRecords.size());
        } catch (Exception e) {
            log.error("从数据库导出账单数据失败", e);
            throw new BusinessException("导出账单数据失败: " + e.getMessage());
        }
    }
    
    private CmbBillRecordReal convertTransactionToRecord(com.gaoyan.personalledger.entity.BillTransaction transaction) {
        CmbBillRecordReal record = new CmbBillRecordReal();
        record.setTransactionDate(transaction.getTransactionDate());
        record.setTransactionTime(transaction.getTransactionTime());
        record.setIncome(transaction.getIncome());
        record.setExpense(transaction.getExpense());
        record.setBalance(transaction.getBalance());
        record.setTransactionType(transaction.getTransactionType());
        record.setDescription(transaction.getDescription());
        record.setPaymentChannel(transaction.getPaymentChannel());
        record.setCategory(transaction.getCategory());
        record.setUserNote(transaction.getUserNote());
        record.setExcludeFromStats(transaction.getExcludeFromStats());
        return record;
    }
}