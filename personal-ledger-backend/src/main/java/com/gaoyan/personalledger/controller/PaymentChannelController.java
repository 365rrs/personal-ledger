package com.gaoyan.personalledger.controller;

import com.gaoyan.personalledger.entity.PaymentChannel;
import com.gaoyan.personalledger.service.PaymentChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付渠道管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment-channel")
@CrossOrigin
public class PaymentChannelController {
    
    @Autowired
    private PaymentChannelService paymentChannelService;
    
    /**
     * 获取所有支付渠道
     */
    @GetMapping("/list")
    public List<PaymentChannel> getAllChannels() {
        log.info("获取所有支付渠道");
        return paymentChannelService.getAllChannels();
    }
    
    /**
     * 添加支付渠道
     */
    @PostMapping("/add")
    public PaymentChannel addChannel(@RequestBody PaymentChannel channel) {
        log.info("添加支付渠道: {}", channel.getName());
        return paymentChannelService.addChannel(channel);
    }
    
    /**
     * 更新支付渠道
     */
    @PutMapping("/update")
    public PaymentChannel updateChannel(@RequestBody PaymentChannel channel) {
        log.info("更新支付渠道: {}", channel.getId());
        return paymentChannelService.updateChannel(channel);
    }
    
    /**
     * 删除支付渠道
     */
    @DeleteMapping("/delete/{id}")
    public void deleteChannel(@PathVariable Long id) {
        log.info("删除支付渠道: {}", id);
        paymentChannelService.deleteChannel(id);
    }
}
