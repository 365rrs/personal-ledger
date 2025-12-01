package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.CmbBillInfo;
import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * 招商银行账单服务接口
 */
public interface CmbBillService {
    
    /**
     * 解析招商银行真实格式的账单CSV文件（完整信息）
     * @param file CSV文件
     * @return 完整账单信息
     */
    CmbBillInfo parseCmbBillInfo(MultipartFile file);
    
    /**
     * 解析招商银行Excel账单文件
     * @param file Excel文件
     * @return 完整账单信息
     */
    CmbBillInfo parseExcelBillInfo(MultipartFile file);
    
    /**
     * 导出招商银行账单为Excel文件
     * @param billInfo 账单信息
     * @param outputStream 输出流
     */
    void exportCmbBill(CmbBillInfo billInfo, ServletOutputStream outputStream);
    
    /**
     * 从数据库导出招商银行账单为Excel文件
     * @param params 查询参数
     * @param outputStream 输出流
     */
    void exportCmbBillFromDatabase(com.gaoyan.personalledger.entity.TransactionQueryParams params, ServletOutputStream outputStream);
}