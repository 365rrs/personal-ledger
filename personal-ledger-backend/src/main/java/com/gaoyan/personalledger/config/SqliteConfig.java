package com.gaoyan.personalledger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * SQLite配置 - 统一数据库初始化管理
 */
@Slf4j
@Configuration
@Order(2) // 在DatabaseMigration之后执行
public class SqliteConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            log.info("开始检查并初始化数据库表...");
            
            // 获取已存在的表
            Set<String> existingTables = getExistingTables(conn);
            log.info("当前数据库中已存在的表: {}", existingTables);
            
            // 初始化清洗规则表
            if (!existingTables.contains("cleaning_rule")) {
                log.info("检测到 cleaning_rule 表不存在，开始创建...");
                stmt.execute("CREATE TABLE cleaning_rule (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "rule_type VARCHAR(50) NOT NULL," +
                    "keyword VARCHAR(200) NOT NULL," +
                    "target_value VARCHAR(200) NOT NULL," +
                    "match_mode VARCHAR(20) NOT NULL DEFAULT 'CONTAINS'," +
                    "priority INTEGER NOT NULL DEFAULT 50," +
                    "enabled INTEGER NOT NULL DEFAULT 1," +
                    "description VARCHAR(500)," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                stmt.execute("CREATE INDEX idx_rule_type ON cleaning_rule(rule_type)");
                stmt.execute("CREATE INDEX idx_enabled ON cleaning_rule(enabled)");
                log.info("✅ cleaning_rule 表创建成功");
            } else {
                log.info("✅ cleaning_rule 表已存在，跳过创建");
            }
            
            // 初始化分类表
            if (!existingTables.contains("category")) {
                log.info("检测到 category 表不存在，开始创建...");
                stmt.execute("CREATE TABLE category (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(50) NOT NULL," +
                    "type VARCHAR(20) NOT NULL," +
                    "sort_order INTEGER DEFAULT 0," +
                    "enabled INTEGER DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                stmt.execute("CREATE INDEX idx_category_type ON category(type)");
                stmt.execute("CREATE INDEX idx_category_enabled ON category(enabled)");
                log.info("✅ category 表创建成功");
                
                // 初始化默认分类数据
                initDefaultCategories(stmt);
            } else {
                log.info("✅ category 表已存在，跳过创建");
            }
            
            // 初始化支付渠道表
            if (!existingTables.contains("payment_channel")) {
                log.info("检测到 payment_channel 表不存在，开始创建...");
                stmt.execute("CREATE TABLE payment_channel (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(50) NOT NULL," +
                    "sort_order INTEGER DEFAULT 0," +
                    "enabled INTEGER DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                stmt.execute("CREATE INDEX idx_payment_channel_enabled ON payment_channel(enabled)");
                log.info("✅ payment_channel 表创建成功");
                
                // 初始化默认支付渠道数据
                initDefaultPaymentChannels(stmt);
            } else {
                log.info("✅ payment_channel 表已存在，跳过创建");
            }
            
            // 初始化账单导入历史记录表 (v1.5.0)
            if (!existingTables.contains("bill_import_history")) {
                log.info("检测到 bill_import_history 表不存在，开始创建...");
                stmt.execute("CREATE TABLE bill_import_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "import_name VARCHAR(200) NOT NULL," +
                    "source_file VARCHAR(200)," +
                    "file_type VARCHAR(20)," +
                    "import_time DATETIME," +
                    "record_count INTEGER DEFAULT 0," +
                    "new_count INTEGER DEFAULT 0," +
                    "update_count INTEGER DEFAULT 0," +
                    "duplicate_count INTEGER DEFAULT 0," +
                    "account_number VARCHAR(50)," +
                    "period_start DATE," +
                    "period_end DATE," +
                    "import_status VARCHAR(20) DEFAULT 'SUCCESS'," +
                    "error_message TEXT," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                log.info("✅ bill_import_history 表创建成功");
            } else {
                log.info("✅ bill_import_history 表已存在，跳过创建");
            }
            
            // 初始化账单交易明细表 (v1.5.0)
            if (!existingTables.contains("bill_transaction")) {
                log.info("检测到 bill_transaction 表不存在，开始创建...");
                stmt.execute("CREATE TABLE bill_transaction (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "transaction_date DATE," +
                    "transaction_time TIME," +
                    "income VARCHAR(20)," +
                    "expense VARCHAR(20)," +
                    "balance VARCHAR(20)," +
                    "transaction_type VARCHAR(100)," +
                    "description TEXT," +
                    "payment_channel VARCHAR(50)," +
                    "category VARCHAR(50)," +
                    "user_note TEXT," +
                    "include_in_stats BOOLEAN DEFAULT 1," +
                    "first_import_id INTEGER," +
                    "last_import_id INTEGER," +
                    "import_count INTEGER DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                stmt.execute("CREATE INDEX idx_bill_transaction_date ON bill_transaction(transaction_date)");
                stmt.execute("CREATE INDEX idx_bill_transaction_category ON bill_transaction(category)");
                stmt.execute("CREATE INDEX idx_bill_transaction_first_import ON bill_transaction(first_import_id)");
                log.info("✅ bill_transaction 表创建成功");
            } else {
                log.info("✅ bill_transaction 表已存在，跳过创建");
            }
            
            // 初始化交易导入关联表 (v1.5.0)
            if (!existingTables.contains("bill_transaction_import")) {
                log.info("检测到 bill_transaction_import 表不存在，开始创建...");
                stmt.execute("CREATE TABLE bill_transaction_import (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "transaction_id INTEGER NOT NULL," +
                    "import_id INTEGER NOT NULL," +
                    "is_new BOOLEAN DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (transaction_id) REFERENCES bill_transaction(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (import_id) REFERENCES bill_import_history(id) ON DELETE CASCADE," +
                    "UNIQUE(transaction_id, import_id))");
                stmt.execute("CREATE INDEX idx_transaction_import_transaction ON bill_transaction_import(transaction_id)");
                stmt.execute("CREATE INDEX idx_transaction_import_import ON bill_transaction_import(import_id)");
                log.info("✅ bill_transaction_import 表创建成功");
            } else {
                log.info("✅ bill_transaction_import 表已存在，跳过创建");
            }
            
            log.info("✅ 数据库初始化完成，共检查 6 张表");
            
        } catch (Exception e) {
            log.error("❌ 数据库初始化失败", e);
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    private Set<String> getExistingTables(Connection conn) throws Exception {
        Set<String> tables = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }
        return tables;
    }
    
    private void initDefaultCategories(Statement stmt) throws Exception {
        log.info("初始化默认分类数据...");
        
        String[] expenseCategories = {"餐饮", "购物", "出行", "娱乐", "医疗", "教育", "住房", "通讯", "其他支出"};
        for (int i = 0; i < expenseCategories.length; i++) {
            stmt.execute(String.format(
                "INSERT INTO category (name, type, sort_order, enabled) VALUES ('%s', 'EXPENSE', %d, 1)",
                expenseCategories[i], i));
        }
        
        String[] incomeCategories = {"工资", "奖金", "投资收益", "兼职", "其他收入"};
        for (int i = 0; i < incomeCategories.length; i++) {
            stmt.execute(String.format(
                "INSERT INTO category (name, type, sort_order, enabled) VALUES ('%s', 'INCOME', %d, 1)",
                incomeCategories[i], i));
        }
        
        log.info("默认分类数据初始化完成");
    }
    
    private void initDefaultPaymentChannels(Statement stmt) throws Exception {
        log.info("初始化默认支付渠道数据...");
        
        String[] channels = {"微信", "支付宝", "银行卡", "京东支付", "现金", "其他"};
        for (int i = 0; i < channels.length; i++) {
            stmt.execute(String.format(
                "INSERT INTO payment_channel (name, sort_order, enabled) VALUES ('%s', %d, 1)",
                channels[i], i));
        }
        
        log.info("默认支付渠道数据初始化完成");
    }
}
