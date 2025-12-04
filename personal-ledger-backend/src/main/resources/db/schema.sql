-- Personal Ledger MySQL数据库初始化脚本
-- 版本: v2.0.0
-- 数据库: MySQL 5.7+

-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_ledger DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_ledger;

-- 1. 清洗规则表
CREATE TABLE IF NOT EXISTS `cleaning_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型: PAYMENT_CHANNEL-支付渠道, CATEGORY-分类, DESCRIPTION-描述清洗',
  `keyword` VARCHAR(200) NOT NULL COMMENT '匹配关键词',
  `target_value` VARCHAR(200) NOT NULL COMMENT '目标值',
  `match_mode` VARCHAR(20) NOT NULL DEFAULT 'CONTAINS' COMMENT '匹配模式: CONTAINS-包含, EQUALS-完全匹配, STARTS_WITH-开头匹配',
  `priority` INT NOT NULL DEFAULT 50 COMMENT '优先级(数字越大优先级越高)',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据清洗规则表';

-- 2. 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `type` VARCHAR(20) NOT NULL COMMENT '分类类型: INCOME-收入, EXPENSE-支出',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_type` (`type`),
  KEY `idx_category_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 3. 支付渠道表
CREATE TABLE IF NOT EXISTS `payment_channel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '渠道名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_channel_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付渠道表';

-- 4. 账单导入历史记录表
CREATE TABLE IF NOT EXISTS `bill_import_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `import_name` VARCHAR(200) NOT NULL COMMENT '导入名称',
  `source_file` VARCHAR(200) DEFAULT NULL COMMENT '源文件名',
  `file_type` VARCHAR(20) DEFAULT NULL COMMENT '文件类型: CSV, EXCEL',
  `import_time` DATETIME DEFAULT NULL COMMENT '导入时间',
  `record_count` INT DEFAULT 0 COMMENT '记录总数',
  `new_count` INT DEFAULT 0 COMMENT '新增记录数',
  `update_count` INT DEFAULT 0 COMMENT '更新记录数',
  `duplicate_count` INT DEFAULT 0 COMMENT '重复记录数',
  `account_number` VARCHAR(50) DEFAULT NULL COMMENT '账号',
  `period_start` DATE DEFAULT NULL COMMENT '账期开始日期',
  `period_end` DATE DEFAULT NULL COMMENT '账期结束日期',
  `import_status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '导入状态: SUCCESS-成功, FAILED-失败',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_import_time` (`import_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单导入历史记录表';

-- 5. 账单交易明细表
CREATE TABLE IF NOT EXISTS `bill_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transaction_date` DATE DEFAULT NULL COMMENT '交易日期',
  `transaction_time` TIME DEFAULT NULL COMMENT '交易时间',
  `income` DECIMAL(15,2) DEFAULT NULL COMMENT '收入金额',
  `expense` DECIMAL(15,2) DEFAULT NULL COMMENT '支出金额',
  `balance` DECIMAL(15,2) DEFAULT NULL COMMENT '账户余额',
  `transaction_type` VARCHAR(100) DEFAULT NULL COMMENT '交易类型',
  `description` TEXT DEFAULT NULL COMMENT '交易描述',
  `payment_channel` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `user_note` TEXT DEFAULT NULL COMMENT '用户备注',
  `exclude_from_stats` TINYINT(1) DEFAULT 0 COMMENT '是否排除统计: 0-计入统计, 1-排除统计',
  `first_import_id` BIGINT DEFAULT NULL COMMENT '首次导入ID',
  `last_import_id` BIGINT DEFAULT NULL COMMENT '最后导入ID',
  `import_count` INT DEFAULT 1 COMMENT '导入次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_bill_transaction_date` (`transaction_date`),
  KEY `idx_bill_transaction_category` (`category`),
  KEY `idx_bill_transaction_first_import` (`first_import_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单交易明细表';

-- 6. 交易导入关联表
CREATE TABLE IF NOT EXISTS `bill_transaction_import` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transaction_id` BIGINT NOT NULL COMMENT '交易ID',
  `import_id` BIGINT NOT NULL COMMENT '导入ID',
  `is_new` TINYINT(1) DEFAULT 1 COMMENT '是否新记录: 0-否, 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_import` (`transaction_id`,`import_id`),
  KEY `idx_transaction_import_transaction` (`transaction_id`),
  KEY `idx_transaction_import_import` (`import_id`),
  CONSTRAINT `fk_transaction_import_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `bill_transaction` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_transaction_import_import` FOREIGN KEY (`import_id`) REFERENCES `bill_import_history` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易导入关联表';

-- 初始化默认分类数据
INSERT INTO `category` (`name`, `type`, `sort_order`, `enabled`) VALUES
('餐饮', 'EXPENSE', 0, 1),
('购物', 'EXPENSE', 1, 1),
('出行', 'EXPENSE', 2, 1),
('娱乐', 'EXPENSE', 3, 1),
('医疗', 'EXPENSE', 4, 1),
('教育', 'EXPENSE', 5, 1),
('住房', 'EXPENSE', 6, 1),
('通讯', 'EXPENSE', 7, 1),
('其他支出', 'EXPENSE', 8, 1),
('工资', 'INCOME', 0, 1),
('奖金', 'INCOME', 1, 1),
('投资收益', 'INCOME', 2, 1),
('兼职', 'INCOME', 3, 1),
('其他收入', 'INCOME', 4, 1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 初始化默认支付渠道数据
INSERT INTO `payment_channel` (`name`, `sort_order`, `enabled`) VALUES
('微信', 0, 1),
('支付宝', 1, 1),
('银行卡', 2, 1),
('京东支付', 3, 1),
('现金', 4, 1),
('其他', 5, 1)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);
