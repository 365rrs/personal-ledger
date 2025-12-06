package com.gaoyan.personalledger.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchAddTagRequest {
    private List<Long> transactionIds;
    private List<Long> tagIds;
}
