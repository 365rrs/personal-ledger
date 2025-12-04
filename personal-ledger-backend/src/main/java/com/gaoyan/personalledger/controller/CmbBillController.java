package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.entity.BillImportHistory;
import com.gaoyan.personalledger.entity.CmbBillInfo;
import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import com.gaoyan.personalledger.service.BillImportProcessorService;
import com.gaoyan.personalledger.service.CmbBillService;
import com.gaoyan.personalledger.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 招商银行账单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/cmb")
@CrossOrigin // 允许跨域请求
public class CmbBillController {

    @Autowired
    private CmbBillService cmbBillService;

    @Autowired
    private DataCleaningService dataCleaningService;

    @Autowired
    private BillImportProcessorService billImportProcessorService;

    /**
     * 导入招商银行CSV账单文件
     *
     * @param file CSV文件
     * @return 完整账单信息
     */
    @PostMapping("/import-csv")
    public Map<String, Object> importCmbBillCsv(@RequestParam("file") MultipartFile file) {
        log.info("开始导入招商银行CSV账单文件: {}", file.getOriginalFilename());
        BillImportHistory billImportHistory = billImportProcessorService.processCsvImport(file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("data", billImportHistory);
        return result;
    }

    /**
     * 导入招商银行账单Excel文件
     *
     * @param file Excel文件
     * @return 完整账单信息
     */
    @PostMapping("/import-excel")
    public Map<String, Object> importCmbBillExcel(@RequestParam("file") MultipartFile file) {
        log.info("开始导入招商银行Excel账单文件: {}", file.getOriginalFilename());
        BillImportHistory billImportHistory = billImportProcessorService.processExcelImport(file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("data", billImportHistory);
        return result;
    }

    /**
     * 数据清洗端点 - 清洗账单记录并保存到数据库
     *
     * @param records 原始账单记录列表
     * @return 清洗结果
     */
    @PostMapping("/clean-records")
    public CleanResult cleanBillRecords(@RequestBody List<CmbBillRecordReal> records) {
        log.info("开始清洗账单记录数据，记录数: {}", records.size());
        List<CmbBillRecordReal> cleanedRecords = dataCleaningService.cleanBillRecords(records);
        int updatedCount = billImportProcessorService.updateCleanedRecords(cleanedRecords);
        log.info("清洗完成并已保存到数据库，更新记录数: {}", updatedCount);

        CleanResult result = new CleanResult();
        result.setTotalCount(records.size());
        result.setUpdatedCount(updatedCount);
        result.setRecords(cleanedRecords);
        return result;
    }

    /**
     * 导出招商银行账单数据为Excel文件
     *
     * @param params   查询参数
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void exportCmbBill(
            @RequestBody com.gaoyan.personalledger.entity.TransactionQueryParams params,
            HttpServletResponse response) {
        log.info("开始导出招商银行账单数据");
        try {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileName = "招商银行账单_" + dateStr + "_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            cmbBillService.exportCmbBillFromDatabase(params, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出招商银行账单数据失败", e);
            throw new RuntimeException("导出招商银行账单数据失败: " + e.getMessage());
        }
    }

    /**
     * 清洗结果
     */
    public static class CleanResult {
        private int totalCount;
        private int updatedCount;
        private List<CmbBillRecordReal> records;

        // getters and setters
        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getUpdatedCount() {
            return updatedCount;
        }

        public void setUpdatedCount(int updatedCount) {
            this.updatedCount = updatedCount;
        }

        public List<CmbBillRecordReal> getRecords() {
            return records;
        }

        public void setRecords(List<CmbBillRecordReal> records) {
            this.records = records;
        }
    }
}