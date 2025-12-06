package com.gaoyan.personalledger.dto;

import lombok.Data;

import java.util.List;

@Data
public class BindTagRequest {
    private Long transactionId;
    private List<Long> tagIds;
}
