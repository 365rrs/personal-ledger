package com.gaoyan.personalledger.service;

import com.gaoyan.personalledger.entity.PaymentChannel;

import java.util.List;

/**
 * 支付渠道服务接口
 */
public interface PaymentChannelService {
    
    /**
     * 获取所有支付渠道
     */
    List<PaymentChannel> getAllChannels();
    
    /**
     * 添加支付渠道
     */
    PaymentChannel addChannel(PaymentChannel channel);
    
    /**
     * 更新支付渠道
     */
    PaymentChannel updateChannel(PaymentChannel channel);
    
    /**
     * 删除支付渠道
     */
    void deleteChannel(Long id);
}
