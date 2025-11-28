package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.CleaningRule;

import java.util.List;

/**
 * 清洗规则服务接口
 */
public interface CleaningRuleService {
    
    /**
     * 获取所有规则
     */
    List<CleaningRule> getAllRules();
    
    /**
     * 根据类型获取规则
     */
    List<CleaningRule> getRulesByType(String ruleType);
    
    /**
     * 添加规则
     */
    CleaningRule addRule(CleaningRule rule);
    
    /**
     * 更新规则
     */
    CleaningRule updateRule(CleaningRule rule);
    
    /**
     * 删除规则
     */
    void deleteRule(Long id);
    
    /**
     * 启用/禁用规则
     */
    void toggleRule(Long id, Boolean enabled);
    
    /**
     * 获取启用的规则（按类型和优先级排序）
     */
    List<CleaningRule> getEnabledRulesByType(String ruleType);
}
