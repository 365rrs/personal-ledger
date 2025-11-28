package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.entity.CleaningRule;
import com.gaoyan.personalledger.service.CleaningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 清洗规则管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/cleaning-rules")
@CrossOrigin(origins = "*")
public class CleaningRuleController {
    
    @Autowired
    private CleaningRuleService cleaningRuleService;
    
    /**
     * 获取所有规则
     */
    @GetMapping
    public ResponseEntity<List<CleaningRule>> getAllRules() {
        return ResponseEntity.ok(cleaningRuleService.getAllRules());
    }
    
    /**
     * 根据类型获取规则
     */
    @GetMapping("/type/{ruleType}")
    public ResponseEntity<List<CleaningRule>> getRulesByType(@PathVariable String ruleType) {
        return ResponseEntity.ok(cleaningRuleService.getRulesByType(ruleType));
    }
    
    /**
     * 添加规则
     */
    @PostMapping
    public ResponseEntity<CleaningRule> addRule(@RequestBody CleaningRule rule) {
        return ResponseEntity.ok(cleaningRuleService.addRule(rule));
    }
    
    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<CleaningRule> updateRule(@PathVariable Long id, @RequestBody CleaningRule rule) {
        rule.setId(id);
        return ResponseEntity.ok(cleaningRuleService.updateRule(rule));
    }
    
    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        cleaningRuleService.deleteRule(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 启用/禁用规则
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleRule(@PathVariable Long id, @RequestParam Boolean enabled) {
        cleaningRuleService.toggleRule(id, enabled);
        return ResponseEntity.ok().build();
    }
}
