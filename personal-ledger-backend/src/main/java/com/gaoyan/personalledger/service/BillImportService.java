package com.gaoyan.personalledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillImport;

/**
 * 账单导入记录Service
 */
public interface BillImportService {
    
    /**
     * 创建导入记录
     */
    BillImport createImport(BillImport billImport);
    
    /**
     * 更新导入记录
     */
    void updateImport(BillImport billImport);
    
    /**
     * 获取导入记录
     */
    BillImport getById(Long id);
    
    /**
     * 分页查询导入历史
     */
    Page<BillImport> pageList(int current, int size);
    
    /**
     * 删除导入记录
     */
    void deleteById(Long id);
}
