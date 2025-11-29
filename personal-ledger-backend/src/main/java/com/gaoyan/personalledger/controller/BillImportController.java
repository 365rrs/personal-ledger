package com.gaoyan.personalledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillImportHistory;
import com.gaoyan.personalledger.service.BillImportProcessorService;
import com.gaoyan.personalledger.service.BillImportHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
}
