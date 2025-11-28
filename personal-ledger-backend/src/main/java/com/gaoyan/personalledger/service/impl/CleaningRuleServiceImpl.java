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
    
    @PostConstruct
    public void init() {
        try {
            // 初始化默认规则
            initDefaultRules();
        } catch (Exception e) {
            log.error("初始化规则失败", e);
        }
    }
    
    /**
     * 初始化默认规则
     */
    private void initDefaultRules() {
        // 检查是否已有规则
        long count = cleaningRuleMapper.selectCount(null);
        if (count > 0) {
            log.info("数据库中已存在{}条规则，跳过初始化", count);
            return;
        }
        
        // 支付渠道规则
        addDefaultRule("PAYMENT_CHANNEL", "微信", "微信", "CONTAINS", 100);
        addDefaultRule("PAYMENT_CHANNEL", "支付宝", "支付宝", "CONTAINS", 100);
        addDefaultRule("PAYMENT_CHANNEL", "财付通", "微信", "CONTAINS", 90);
        addDefaultRule("PAYMENT_CHANNEL", "京东支付", "京东支付", "CONTAINS", 100);
        
        // 分类规则 - 餐饮
        addDefaultRule("CATEGORY", "餐厅", "餐饮", "CONTAINS", 80);
        addDefaultRule("CATEGORY", "星巴克", "餐饮", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "瑞幸", "餐饮", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "麦当劳", "餐饮", "CONTAINS", 90);
        
        // 分类规则 - 外卖
        addDefaultRule("CATEGORY", "饿了么", "外卖", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "美团外卖", "外卖", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "拉扎斯", "外卖", "CONTAINS", 90);
        
        // 分类规则 - 出行
        addDefaultRule("CATEGORY", "滴滴", "出行", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "地铁", "出行", "CONTAINS", 90);
        addDefaultRule("CATEGORY", "加油", "出行", "CONTAINS", 80);
        
        log.info("已初始化默认规则");
    }
    
    private void addDefaultRule(String type, String keyword, String target, String mode, int priority) {
        CleaningRule rule = new CleaningRule();
        rule.setId(null); // SQLite自动生成
        rule.setRuleType(type);
        rule.setKeyword(keyword);
        rule.setTargetValue(target);
        rule.setMatchMode(mode);
        rule.setPriority(priority);
        rule.setEnabled(true);
        rule.setDescription("系统默认规则");
        cleaningRuleMapper.insert(rule);
    }
    
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
        rule.setId(null); // 让SQLite自动生成
        cleaningRuleMapper.insert(rule);
        // 查询最新插入的记录
        CleaningRule inserted = cleaningRuleMapper.selectOne(
            new LambdaQueryWrapper<CleaningRule>()
                .orderByDesc(CleaningRule::getId)
                .last("limit 1")
        );
        if (inserted != null) {
            rule.setId(inserted.getId());
        }
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
