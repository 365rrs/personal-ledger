package com.gaoyan.personalledger.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 监听启动完成事件，打印自定义日志
 *
 * @author gaomingxi@haier.com
 * @version 1.0
 * @date 2025/10/19 14:01
 */
@Component
public class StartupInfoListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 获取应用上下文信息
        String appName = event.getApplicationContext().getEnvironment()
                .getProperty("spring.application.name", "未知应用");
        String port = event.getApplicationContext().getEnvironment()
                .getProperty("server.port", "8080");

        // 打印自定义启动信息
        System.out.println("\n=============================================");
        System.out.println("🎉 " + appName + " 启动成功！");
        System.out.println("🕒 启动时间：" + LocalDateTime.now());
        System.out.println("🌐 访问地址：http://localhost:" + port);
        System.out.println("=============================================\n");
    }
}
