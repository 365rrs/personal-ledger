package com.gaoyan.personalledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gaoyan.personalledger.entity.PaymentChannel;
import com.gaoyan.personalledger.mapper.PaymentChannelMapper;
import com.gaoyan.personalledger.service.PaymentChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付渠道服务实现
 */
@Slf4j
@Service
public class PaymentChannelServiceImpl implements PaymentChannelService {
    
    @Autowired
    private PaymentChannelMapper paymentChannelMapper;
    
    @Override
    public List<PaymentChannel> getAllChannels() {
        return paymentChannelMapper.selectList(new QueryWrapper<PaymentChannel>()
                .eq("enabled", true)
                .orderByAsc("sort_order"));
    }
    
    @Override
    public PaymentChannel addChannel(PaymentChannel channel) {
        channel.setCreateTime(LocalDateTime.now());
        channel.setUpdateTime(LocalDateTime.now());
        if (channel.getEnabled() == null) {
            channel.setEnabled(true);
        }
        if (channel.getSortOrder() == null) {
            channel.setSortOrder(0);
        }
        paymentChannelMapper.insert(channel);
        return channel;
    }
    
    @Override
    public PaymentChannel updateChannel(PaymentChannel channel) {
        channel.setUpdateTime(LocalDateTime.now());
        paymentChannelMapper.updateById(channel);
        return channel;
    }
    
    @Override
    public void deleteChannel(Long id) {
        paymentChannelMapper.deleteById(id);
    }
}
