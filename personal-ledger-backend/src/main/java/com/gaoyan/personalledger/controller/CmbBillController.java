package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.entity.CmbBillInfo;
import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import com.gaoyan.personalledger.service.CmbBillService;
import com.gaoyan.personalledger.service.DataCleaningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    
    /**
     * 导入招商银行真实格式账单CSV文件（完整信息）
     * @param file CSV文件
     * @return 完整账单信息
     */
    @PostMapping("/import-full")
    public CmbBillInfo importCmbBillFull(@RequestParam("file") MultipartFile file) {
        log.info("开始导入招商银行真实格式账单文件（完整信息）: {}", file.getOriginalFilename());
        return cmbBillService.parseCmbBillInfo(file);
    }
    
    /**
     * 数据清洗端点 - 清洗账单记录
     * @param records 原始账单记录列表
     * @return 清洗后的账单记录列表
     */
    @PostMapping("/clean-records")
    public List<CmbBillRecordReal> cleanBillRecords(@RequestBody List<CmbBillRecordReal> records) {
        log.info("开始清洗账单记录数据，记录数: {}", records.size());
        return dataCleaningService.cleanBillRecords(records);
    }
    
    /**
     * 导入招商银行账单Excel文件
     * @param file Excel文件
     * @return 完整账单信息
     */
    @PostMapping("/import-excel")
    public CmbBillInfo importCmbBillExcel(@RequestParam("file") MultipartFile file) {
        log.info("开始导入招商银行Excel账单文件: {}", file.getOriginalFilename());
        return cmbBillService.parseExcelBillInfo(file);
    }
    
    /**
     * 导出招商银行账单数据为Excel文件
     * @param billInfo 账单信息
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void exportCmbBill(@RequestBody CmbBillInfo billInfo, HttpServletResponse response) {
        log.info("开始导出招商银行账单数据");
        try {
            // 设置响应头
            String fileName = "招商银行账单_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            
            // 调用服务导出数据
            cmbBillService.exportCmbBill(billInfo, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出招商银行账单数据失败", e);
            throw new RuntimeException("导出招商银行账单数据失败: " + e.getMessage());
        }
    }
}