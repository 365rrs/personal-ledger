-- Personal Ledger v1.5.0 数据库升级脚本
-- 功能：账单数据持久化
-- 创建时间：2025年

-- =====================================================
-- 1. 账单导入记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS bill_import (
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

-- =====================================================
-- 2. 账单交易明细表
-- =====================================================
CREATE TABLE IF NOT EXISTS bill_transaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_date DATE NOT NULL,
    transaction_time TIME,
    income DECIMAL(15,2),
    expense DECIMAL(15,2),
    balance DECIMAL(15,2),
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

-- =====================================================
-- 3. 交易导入关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS bill_transaction_import (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id INTEGER NOT NULL,
    import_id INTEGER NOT NULL,
    is_new BOOLEAN DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES bill_transaction(id) ON DELETE CASCADE,
    FOREIGN KEY (import_id) REFERENCES bill_import(id) ON DELETE CASCADE,
    UNIQUE(transaction_id, import_id)
);

-- =====================================================
-- 4. 创建索引
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_bill_transaction_date ON bill_transaction(transaction_date);
CREATE INDEX IF NOT EXISTS idx_bill_transaction_category ON bill_transaction(category);
CREATE INDEX IF NOT EXISTS idx_bill_transaction_first_import ON bill_transaction(first_import_id);
CREATE INDEX IF NOT EXISTS idx_transaction_import_transaction ON bill_transaction_import(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_import_import ON bill_transaction_import(import_id);
