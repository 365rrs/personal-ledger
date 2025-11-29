package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillImportHistory;
import com.gaoyan.personalledger.mapper.BillImportHistoryMapper;
import com.gaoyan.personalledger.service.BillImportHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 账单导入历史记录Service实现
 */
@Slf4j
@Service
public class BillImportHistoryServiceImpl implements BillImportHistoryService {
    
    @Autowired
    private BillImportHistoryMapper billImportHistoryMapper;
    
    @Override
    public BillImportHistory createImport(BillImportHistory billImportHistory) {
        billImportHistoryMapper.insert(billImportHistory);
        return billImportHistory;
    }
    
    @Override
    public void updateImport(BillImportHistory billImportHistory) {
        billImportHistoryMapper.updateById(billImportHistory);
    }
    
    @Override
    public BillImportHistory getById(Long id) {
        return billImportHistoryMapper.selectById(id);
    }
    
    @Override
    public Page<BillImportHistory> pageList(int current, int size) {
        Page<BillImportHistory> page = new Page<>(current, size);
        LambdaQueryWrapper<BillImportHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BillImportHistory::getImportTime);
        return billImportHistoryMapper.selectPage(page, wrapper);
    }
    
    @Override
    public void deleteById(Long id) {
        billImportHistoryMapper.deleteById(id);
    }
}
