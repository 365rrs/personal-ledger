package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.dto.BatchAddTagRequest;
import com.gaoyan.personalledger.dto.BindTagRequest;
import com.gaoyan.personalledger.entity.BillTag;
import com.gaoyan.personalledger.service.BillTagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/bill/tag")
@CrossOrigin
public class BillTagController {
    
    @Resource
    private BillTagService billTagService;
    
    @GetMapping("/list")
    public List<BillTag> list() {
        return billTagService.list();
    }
    
    @PostMapping("/add")
    public void add(@RequestBody BillTag tag) {
        billTagService.save(tag);
    }
    
    @PutMapping("/update")
    public void update(@RequestBody BillTag tag) {
        billTagService.update(tag);
    }
    
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        billTagService.delete(id);
    }
    
    @PostMapping("/bind")
    public void bindTags(@RequestBody BindTagRequest request) {
        billTagService.bindTagsToTransaction(request.getTransactionId(), request.getTagIds());
    }
    
    @GetMapping("/transaction/{transactionId}")
    public List<Long> getTransactionTags(@PathVariable Long transactionId) {
        return billTagService.getTransactionTagIds(transactionId);
    }
    
    @PostMapping("/batch/add")
    public void batchAddTags(@RequestBody BatchAddTagRequest request) {
        billTagService.batchAddTagsToTransactions(request.getTransactionIds(), request.getTagIds());
    }
}
