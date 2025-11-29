package com.gaoyan.personalledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillImportHistory;

/**
 * 账单导入历史记录Service
 */
public interface BillImportHistoryService {
    
    /**
     * 创建导入记录
     */
    BillImportHistory createImport(BillImportHistory billImportHistory);
    
    /**
     * 更新导入记录
     */
    void updateImport(BillImportHistory billImportHistory);
    
    /**
     * 获取导入记录
     */
    BillImportHistory getById(Long id);
    
    /**
     * 分页查询导入历史
     */
    Page<BillImportHistory> pageList(int current, int size);
    
    /**
     * 删除导入记录
     */
    void deleteById(Long id);
}
