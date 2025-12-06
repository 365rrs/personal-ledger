package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.BillTag;

import java.util.List;

public interface BillTagService {
    List<BillTag> list();
    BillTag getById(Long id);
    void save(BillTag tag);
    void update(BillTag tag);
    void delete(Long id);
    void bindTagsToTransaction(Long transactionId, List<Long> tagIds);
    List<Long> getTransactionTagIds(Long transactionId);
    void batchAddTagsToTransactions(List<Long> transactionIds, List<Long> tagIds);
}
