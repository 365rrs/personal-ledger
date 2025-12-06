package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaoyan.personalledger.entity.BillTag;
import com.gaoyan.personalledger.entity.BillTransactionTag;
import com.gaoyan.personalledger.mapper.BillTagMapper;
import com.gaoyan.personalledger.mapper.BillTransactionTagMapper;
import com.gaoyan.personalledger.service.BillTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillTagServiceImpl implements BillTagService {
    
    @Resource
    private BillTagMapper billTagMapper;
    
    @Resource
    private BillTransactionTagMapper billTransactionTagMapper;
    
    @Override
    public List<BillTag> list() {
        return billTagMapper.selectList(new LambdaQueryWrapper<BillTag>().orderByAsc(BillTag::getSortOrder));
    }
    
    @Override
    public BillTag getById(Long id) {
        return billTagMapper.selectById(id);
    }
    
    @Override
    public void save(BillTag tag) {
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        billTagMapper.insert(tag);
    }
    
    @Override
    public void update(BillTag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        billTagMapper.updateById(tag);
    }
    
    @Override
    public void delete(Long id) {
        billTagMapper.deleteById(id);
        // 删除关联关系
        billTransactionTagMapper.delete(new LambdaQueryWrapper<BillTransactionTag>()
                .eq(BillTransactionTag::getTagId, id));
    }
    
    @Override
    @Transactional
    public void bindTagsToTransaction(Long transactionId, List<Long> tagIds) {
        // 删除原有关联
        billTransactionTagMapper.delete(new LambdaQueryWrapper<BillTransactionTag>()
                .eq(BillTransactionTag::getTransactionId, transactionId));
        
        // 添加新关联
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                BillTransactionTag relation = new BillTransactionTag();
                relation.setTransactionId(transactionId);
                relation.setTagId(tagId);
                relation.setCreateTime(LocalDateTime.now());
                billTransactionTagMapper.insert(relation);
            }
        }
    }
    
    @Override
    public List<Long> getTransactionTagIds(Long transactionId) {
        return billTransactionTagMapper.selectTagIdsByTransactionId(transactionId);
    }
}
