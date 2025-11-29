package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillImport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单导入记录Mapper
 */
@Mapper
public interface BillImportMapper extends BaseMapper<BillImport> {
}
