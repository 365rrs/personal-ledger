-- ============================================
-- 数据库表结构文档
-- 注意：此文件仅作为文档参考，实际表创建由 SqliteConfig.java 管理
-- ============================================

-- 清洗规则表
CREATE TABLE IF NOT EXISTS cleaning_rule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_type VARCHAR(50) NOT NULL,
    keyword VARCHAR(200) NOT NULL,
    target_value VARCHAR(200) NOT NULL,
    match_mode VARCHAR(20) NOT NULL DEFAULT 'CONTAINS',
    priority INTEGER NOT NULL DEFAULT 50,
    enabled INTEGER NOT NULL DEFAULT 1,
    description VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_rule_type ON cleaning_rule(rule_type);
CREATE INDEX IF NOT EXISTS idx_enabled ON cleaning_rule(enabled);

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    enabled INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_category_type ON category(type);
CREATE INDEX IF NOT EXISTS idx_category_enabled ON category(enabled);

-- 支付渠道表
CREATE TABLE IF NOT EXISTS payment_channel (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    enabled INTEGER DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_payment_channel_enabled ON payment_channel(enabled);

-- 账单导入历史记录表
CREATE TABLE IF NOT EXISTS bill_import_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    import_name VARCHAR(200) NOT NULL,
    source_file VARCHAR(200),
    file_type VARCHAR(20),
    import_time DATETIME,
    record_count INTEGER DEFAULT 0,
    new_count INTEGER DEFAULT 0,
    update_count INTEGER DEFAULT 0,
    duplicate_count INTEGER DEFAULT 0,
    account_number VARCHAR(50),
    period_start DATE,
    period_end DATE,
    import_status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 账单交易明细表
-- 注意：金额字段使用VARCHAR类型存储字符串，避免浮点数精度问题
CREATE TABLE IF NOT EXISTS bill_transaction (
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

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_bill_transaction_date ON bill_transaction(transaction_date);
CREATE INDEX IF NOT EXISTS idx_bill_transaction_category ON bill_transaction(category);
CREATE INDEX IF NOT EXISTS idx_bill_transaction_first_import ON bill_transaction(first_import_id);

-- 交易导入关联表
CREATE TABLE IF NOT EXISTS bill_transaction_import (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id INTEGER NOT NULL,
    import_id INTEGER NOT NULL,
    is_new BOOLEAN DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES bill_transaction(id) ON DELETE CASCADE,
    FOREIGN KEY (import_id) REFERENCES bill_import_history(id) ON DELETE CASCADE,
    UNIQUE(transaction_id, import_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_transaction_import_transaction ON bill_transaction_import(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_import_import ON bill_transaction_import(import_id);
