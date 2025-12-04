package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gaoyan.personalledger.entity.CleaningRule;
import com.gaoyan.personalledger.mapper.CleaningRuleMapper;
import com.gaoyan.personalledger.service.CleaningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 清洗规则服务实现（基于数据库存储）
 */
@Slf4j
@Service
public class CleaningRuleServiceImpl implements CleaningRuleService {
    
    @Autowired
    private CleaningRuleMapper cleaningRuleMapper;
    
    @Override
    public List<CleaningRule> getAllRules() {
        return cleaningRuleMapper.selectList(null);
    }
    
    @Override
    public List<CleaningRule> getRulesByType(String ruleType) {
        LambdaQueryWrapper<CleaningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CleaningRule::getRuleType, ruleType)
                .eq(CleaningRule::getEnabled, true)
                .orderByDesc(CleaningRule::getPriority);
        return cleaningRuleMapper.selectList(wrapper);
    }
    
    @Override
    public CleaningRule addRule(CleaningRule rule) {
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (rule.getPriority() == null) {
            rule.setPriority(50);
        }
        cleaningRuleMapper.insert(rule);
        // MySQL自动生成ID并回填
        log.info("添加规则: {}", rule);
        return rule;
    }
    
    @Override
    public CleaningRule updateRule(CleaningRule rule) {
        cleaningRuleMapper.updateById(rule);
        log.info("更新规则: {}", rule);
        return rule;
    }
    
    @Override
    public void deleteRule(Long id) {
        cleaningRuleMapper.deleteById(id);
        log.info("删除规则: {}", id);
    }
    
    @Override
    public void toggleRule(Long id, Boolean enabled) {
        CleaningRule rule = cleaningRuleMapper.selectById(id);
        if (rule != null) {
            rule.setEnabled(enabled);
            cleaningRuleMapper.updateById(rule);
            log.info("切换规则状态: id={}, enabled={}", id, enabled);
        }
    }
    
    @Override
    public List<CleaningRule> getEnabledRulesByType(String ruleType) {
        LambdaQueryWrapper<CleaningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CleaningRule::getRuleType, ruleType)
                .eq(CleaningRule::getEnabled, true)
                .orderByDesc(CleaningRule::getPriority);
        return cleaningRuleMapper.selectList(wrapper);
    }
}
