package com.gaoyan.personalledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gaoyan.personalledger.entity.BillTransaction;
import com.gaoyan.personalledger.service.BillTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 账单交易Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/bill/transaction")
@CrossOrigin
public class BillTransactionController {
    
    @Autowired
    private BillTransactionService billTransactionService;
    
    /**
     * 获取交易列表
     */
    @GetMapping("/list")
    public Map<String, Object> getTransactionList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String category) {
        
        Page<BillTransaction> page = billTransactionService.pageList(current, size, startDate, endDate, category);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", page);
        return result;
    }
    
    /**
     * 获取交易详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getTransactionDetail(@PathVariable Long id) {
        BillTransaction transaction = billTransactionService.getById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", transaction);
        return result;
    }
    
    /**
     * 更新交易
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateTransaction(@PathVariable Long id, @RequestBody BillTransaction transaction) {
        transaction.setId(id);
        billTransactionService.updateById(transaction);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "更新成功");
        return result;
    }
    
    /**
     * 删除交易
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteTransaction(@PathVariable Long id) {
        billTransactionService.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
}
