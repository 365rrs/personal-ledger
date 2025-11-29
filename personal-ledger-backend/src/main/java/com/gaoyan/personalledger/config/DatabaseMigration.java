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

/**
 * 数据库迁移配置
 * 用于修复已存在表的结构问题
 */
@Slf4j
@Configuration
@Order(1) // 确保在SqliteConfig之前执行
public class DatabaseMigration {
    
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void migrate() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 检查是否需要迁移bill_transaction表
            if (needsMigration(conn)) {
                log.info("检测到bill_transaction表需要迁移（修复金额字段精度问题）...");
                migrateBillTransaction(conn, stmt);
                log.info("✅ bill_transaction表迁移完成");
            } else {
                log.info("✅ bill_transaction表无需迁移");
            }
            
        } catch (Exception e) {
            log.error("❌ 数据库迁移失败", e);
            throw new RuntimeException("数据库迁移失败", e);
        }
    }
    
    /**
     * 检查是否需要迁移
     */
    private boolean needsMigration(Connection conn) throws Exception {
        // 检查表是否存在
        try (ResultSet rs = conn.getMetaData().getTables(null, null, "bill_transaction", null)) {
            if (!rs.next()) {
                return false; // 表不存在，无需迁移
            }
        }
        
        // 检查income字段的类型
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "bill_transaction", "income")) {
            if (rs.next()) {
                String typeName = rs.getString("TYPE_NAME");
                // SQLite中DECIMAL会被识别为REAL或NUMERIC
                return "REAL".equalsIgnoreCase(typeName) || "NUMERIC".equalsIgnoreCase(typeName);
            }
        }
        
        return false;
    }
    
    /**
     * 迁移bill_transaction表
     */
    private void migrateBillTransaction(Connection conn, Statement stmt) throws Exception {
        // 开始事务
        conn.setAutoCommit(false);
        
        try {
            // 1. 备份原表
            log.info("步骤1: 备份原表...");
            stmt.execute("ALTER TABLE bill_transaction RENAME TO bill_transaction_backup");
            
            // 2. 创建新表
            log.info("步骤2: 创建新表结构...");
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
                "exclude_from_stats BOOLEAN DEFAULT 0," +
                "first_import_id INTEGER," +
                "last_import_id INTEGER," +
                "import_count INTEGER DEFAULT 1," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            // 3. 迁移数据
            log.info("步骤3: 迁移数据...");
            stmt.execute("INSERT INTO bill_transaction (" +
                "id, transaction_date, transaction_time, " +
                "income, expense, balance, " +
                "transaction_type, description, payment_channel, category, user_note, " +
                "exclude_from_stats, first_import_id, last_import_id, import_count, " +
                "create_time, update_time) " +
                "SELECT " +
                "id, transaction_date, transaction_time, " +
                "CASE WHEN income IS NOT NULL THEN CAST(income AS TEXT) ELSE NULL END, " +
                "CASE WHEN expense IS NOT NULL THEN CAST(expense AS TEXT) ELSE NULL END, " +
                "CASE WHEN balance IS NOT NULL THEN CAST(balance AS TEXT) ELSE NULL END, " +
                "transaction_type, description, payment_channel, category, user_note, " +
                "exclude_from_stats, first_import_id, last_import_id, import_count, " +
                "create_time, update_time " +
                "FROM bill_transaction_backup");
            
            // 4. 重建索引
            log.info("步骤4: 重建索引...");
            stmt.execute("CREATE INDEX idx_bill_transaction_date ON bill_transaction(transaction_date)");
            stmt.execute("CREATE INDEX idx_bill_transaction_category ON bill_transaction(category)");
            stmt.execute("CREATE INDEX idx_bill_transaction_first_import ON bill_transaction(first_import_id)");
            
            // 5. 删除备份表
            log.info("步骤5: 删除备份表...");
            stmt.execute("DROP TABLE bill_transaction_backup");
            
            // 提交事务
            conn.commit();
            log.info("数据迁移成功完成");
            
        } catch (Exception e) {
            // 回滚事务
            conn.rollback();
            log.error("数据迁移失败，已回滚", e);
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
