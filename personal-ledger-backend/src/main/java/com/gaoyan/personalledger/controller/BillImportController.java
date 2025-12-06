package com.gaoyan.personalledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.dto.CleanResult;
import com.gaoyan.personalledger.entity.BillImportHistory;
import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import com.gaoyan.personalledger.service.BillImportProcessorService;
import com.gaoyan.personalledger.service.BillImportHistoryService;
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
 * 账单导入Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/bill/import")
@CrossOrigin
public class BillImportController {
    
    @Autowired
    private BillImportHistoryService billImportHistoryService;
    
    @Autowired
    private BillImportProcessorService billImportProcessorService;
    
    @Autowired
    private DataCleaningService dataCleaningService;
    
    @Autowired
    private CmbBillService cmbBillService;
    
    /**
     * CSV导入
     */
    @PostMapping("/csv")
    public Map<String, Object> importCsv(@RequestParam("file") MultipartFile file) {
        log.info("开始导入CSV文件: {}", file.getOriginalFilename());
        
        BillImportHistory billImportHistory = billImportProcessorService.processCsvImport(file);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("data", billImportHistory);
        return result;
    }
    
    /**
     * Excel导入
     */
    @PostMapping("/excel")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        log.info("开始导入Excel文件: {}", file.getOriginalFilename());
        
        BillImportHistory billImportHistory = billImportProcessorService.processExcelImport(file);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "导入成功");
        result.put("data", billImportHistory);
        return result;
    }
    
    /**
     * 获取导入历史列表
     */
    @GetMapping("/list")
    public Map<String, Object> getImportList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BillImportHistory> page = billImportHistoryService.pageList(current, size);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", page);
        return result;
    }
    
    /**
     * 获取导入详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getImportDetail(@PathVariable Long id) {
        BillImportHistory billImportHistory = billImportHistoryService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", billImportHistory);
        return result;
    }
    
    /**
     * 删除导入记录
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteImport(@PathVariable Long id) {
        billImportHistoryService.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
    
    /**
     * 数据清洗端点 - 清洗账单记录并保存到数据库
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
     * 导出账单数据为Excel文件
     */
    @PostMapping("/export")
    public void exportBill(
            @RequestBody com.gaoyan.personalledger.entity.TransactionQueryParams params,
            HttpServletResponse response) {
        log.info("开始导出账单数据");
        try {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileName = "招商银行账单_" + dateStr + "_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            cmbBillService.exportCmbBillFromDatabase(params, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出账单数据失败", e);
            throw new RuntimeException("导出账单数据失败: " + e.getMessage());
        }
    }
}
