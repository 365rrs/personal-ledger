package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.CleaningRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清洗规则Mapper
 */
@Mapper
public interface CleaningRuleMapper extends BaseMapper<CleaningRule> {
}
