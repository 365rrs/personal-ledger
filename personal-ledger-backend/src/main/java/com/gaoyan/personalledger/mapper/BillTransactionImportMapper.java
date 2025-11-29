package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillTransactionImport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易导入关联Mapper
 */
@Mapper
public interface BillTransactionImportMapper extends BaseMapper<BillTransactionImport> {
}
