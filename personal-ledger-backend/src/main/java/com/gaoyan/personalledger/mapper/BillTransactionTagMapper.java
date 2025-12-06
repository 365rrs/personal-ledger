package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillTransactionTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BillTransactionTagMapper extends BaseMapper<BillTransactionTag> {
    
    List<Long> selectTagIdsByTransactionId(@Param("transactionId") Long transactionId);
    
    List<BillTransactionTag> selectByTransactionIds(@Param("transactionIds") List<Long> transactionIds);
}
