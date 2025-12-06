package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单交易明细Mapper
 */
@Mapper
public interface BillTransactionMapper extends BaseMapper<BillTransaction> {
    
    List<Long> selectTransactionIdsByTagIds(@Param("tagIds") List<String> tagIds);
}
