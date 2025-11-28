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
