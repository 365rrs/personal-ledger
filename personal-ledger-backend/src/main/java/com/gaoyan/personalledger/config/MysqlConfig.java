package com.gaoyan.personalledger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * MySQL配置 - 数据库初始化管理
 */
@Slf4j
@Configuration
@Profile("mysql")
public class MysqlConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            log.info("开始检查MySQL数据库表...");
            
            // 检查表是否存在
            boolean tablesExist = checkTablesExist(conn);
            
            if (!tablesExist) {
                log.info("检测到数据库表不存在，开始执行初始化脚本...");
                executeInitScript();
                log.info("✅ MySQL数据库初始化完成");
            } else {
                log.info("✅ MySQL数据库表已存在，跳过初始化");
            }
            
        } catch (Exception e) {
            log.error("❌ MySQL数据库初始化失败", e);
            throw new RuntimeException("MySQL数据库初始化失败", e);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean checkTablesExist(Connection conn) throws Exception {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "bill_transaction", null)) {
            return rs.next();
        }
    }
    
    /**
     * 执行初始化脚本
     */
    private void executeInitScript() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("db/schema.sql"));
            populator.setSeparator(";");
            populator.execute(dataSource);
            log.info("数据库初始化脚本执行成功");
        } catch (Exception e) {
            log.error("执行数据库初始化脚本失败", e);
            throw new RuntimeException("执行数据库初始化脚本失败", e);
        }
    }
}
