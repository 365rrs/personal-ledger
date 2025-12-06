package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillTransactionTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BillTransactionTagMapper extends BaseMapper<BillTransactionTag> {
    
    @Select("SELECT tag_id FROM bill_transaction_tag WHERE transaction_id = #{transactionId}")
    List<Long> selectTagIdsByTransactionId(@Param("transactionId") Long transactionId);
}
