package com.gaoyan.personalledger.dto;

import com.gaoyan.personalledger.entity.CmbBillRecordReal;
import lombok.Data;

import java.util.List;

@Data
public class CleanResult {
    private int totalCount;
    private int updatedCount;
    private List<CmbBillRecordReal> records;
}
