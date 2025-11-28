package com.gaoyan.personalledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gaoyan.personalledger.entity.PaymentChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付渠道Mapper
 */
@Mapper
public interface PaymentChannelMapper extends BaseMapper<PaymentChannel> {
}
