-- ============================================
-- 数据库迁移脚本：修复金额字段精度问题
-- 问题：SQLite不支持真正的DECIMAL类型，会导致浮点数精度丢失
-- 解决：将金额字段从DECIMAL改为TEXT类型存储
-- 执行时间：需要在应用启动前手动执行
-- ============================================

-- 备份原表
ALTER TABLE bill_transaction RENAME TO bill_transaction_backup;

-- 创建新表（金额字段使用VARCHAR类型）
CREATE TABLE bill_transaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date DATE,
    transaction_time TIME,
    income VARCHAR(20),
    expense VARCHAR(20),
    balance VARCHAR(20),
    transaction_type VARCHAR(100),
    description TEXT,
    payment_channel VARCHAR(50),
    category VARCHAR(50),
    user_note TEXT,
    exclude_from_stats BOOLEAN DEFAULT 0,
    first_import_id INTEGER,
    last_import_id INTEGER,
    import_count INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 迁移数据（将数值转换为文本）
INSERT INTO bill_transaction (
    id, transaction_date, transaction_time, 
    income, expense, balance,
    transaction_type, description, payment_channel, category, user_note,
    exclude_from_stats, first_import_id, last_import_id, import_count,
    create_time, update_time
)
SELECT 
    id, transaction_date, transaction_time,
    CASE WHEN income IS NOT NULL THEN CAST(income AS TEXT) ELSE NULL END,
    CASE WHEN expense IS NOT NULL THEN CAST(expense AS TEXT) ELSE NULL END,
    CASE WHEN balance IS NOT NULL THEN CAST(balance AS TEXT) ELSE NULL END,
    transaction_type, description, payment_channel, category, user_note,
    exclude_from_stats, first_import_id, last_import_id, import_count,
    create_time, update_time
FROM bill_transaction_backup;

-- 重建索引
CREATE INDEX idx_bill_transaction_date ON bill_transaction(transaction_date);
CREATE INDEX idx_bill_transaction_category ON bill_transaction(category);
CREATE INDEX idx_bill_transaction_first_import ON bill_transaction(first_import_id);

-- 删除备份表（确认数据无误后执行）
-- DROP TABLE bill_transaction_backup;

-- 验证数据
SELECT '迁移完成，请检查数据：' as message;
SELECT COUNT(*) as total_records FROM bill_transaction;
SELECT * FROM bill_transaction LIMIT 5;
