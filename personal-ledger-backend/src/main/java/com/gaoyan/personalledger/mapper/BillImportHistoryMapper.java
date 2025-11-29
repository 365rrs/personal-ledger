package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.BillImportHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单导入历史记录Mapper
 */
@Mapper
public interface BillImportHistoryMapper extends BaseMapper<BillImportHistory> {
}
