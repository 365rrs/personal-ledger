package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单交易明细Mapper
 */
@Mapper
public interface BillTransactionMapper extends BaseMapper<BillTransaction> {
    
    java.util.List<Long> selectTransactionIdsByTagIds(@org.apache.ibatis.annotations.Param("tagIds") java.util.List<String> tagIds);
}
