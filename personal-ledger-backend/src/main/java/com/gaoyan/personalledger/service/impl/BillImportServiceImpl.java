package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillImport;
import com.gaoyan.personalledger.mapper.BillImportMapper;
import com.gaoyan.personalledger.service.BillImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 账单导入记录Service实现
 */
@Slf4j
@Service
public class BillImportServiceImpl implements BillImportService {
    
    @Autowired
    private BillImportMapper billImportMapper;
    
    @Override
    public BillImport createImport(BillImport billImport) {
        billImportMapper.insert(billImport);
        return billImport;
    }
    
    @Override
    public void updateImport(BillImport billImport) {
        billImportMapper.updateById(billImport);
    }
    
    @Override
    public BillImport getById(Long id) {
        return billImportMapper.selectById(id);
    }
    
    @Override
    public Page<BillImport> pageList(int current, int size) {
        Page<BillImport> page = new Page<>(current, size);
        return billImportMapper.selectPage(page, null);
    }
    
    @Override
    public void deleteById(Long id) {
        billImportMapper.deleteById(id);
    }
}
