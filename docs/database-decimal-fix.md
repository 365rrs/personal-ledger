# 数据库金额字段精度问题修复说明

## 问题描述

在使用 SQLite 数据库时，金额字段（income、expense、balance）使用了 `DECIMAL(15,2)` 类型定义。但是 **SQLite 不支持真正的 DECIMAL 类型**，它会将 DECIMAL 类型的数据存储为浮点数（REAL），导致精度丢失问题。

### 问题示例
- 存入：2.47
- 实际存储：2.4699999999999998

## 解决方案

将金额字段从 `DECIMAL(15,2)` 改为 `TEXT` 类型存储，Java 代码中继续使用 `BigDecimal` 类型处理。

### 优势
1. **精度保证**：TEXT 类型存储字符串，不会有精度丢失
2. **兼容性好**：MyBatis-Plus 自动支持 BigDecimal 与 TEXT 的转换
3. **无需修改代码**：Java 实体类继续使用 BigDecimal 类型

## 修复内容

### 1. 表结构修改

**修改前：**
```sql
CREATE TABLE bill_transaction (
    ...
    income DECIMAL(15,2),
    expense DECIMAL(15,2),
    balance DECIMAL(15,2),
    ...
);
```

**修改后：**
```sql
CREATE TABLE bill_transaction (
    ...
    income TEXT,
    expense TEXT,
    balance TEXT,
    ...
);
```

### 2. 自动迁移

系统已添加自动迁移功能，在应用启动时会自动检测并修复现有数据：

- **DatabaseMigration.java**：自动迁移配置类
  - 检测表结构是否需要迁移
  - 自动备份原表
  - 创建新表结构
  - 迁移数据（保留所有现有数据）
  - 重建索引
  - 删除备份表

- **SqliteConfig.java**：表创建配置
  - 新建表时直接使用 TEXT 类型

### 3. 手动迁移（可选）

如果需要手动执行迁移，可以使用提供的 SQL 脚本：

```bash
sqlite3 ./data/personal-ledger.db < src/main/resources/migration_fix_decimal.sql
```

## 使用说明

### 新用户
- 无需任何操作
- 系统会自动创建正确的表结构

### 现有用户
- **自动方式**：重启应用，系统会自动迁移数据
- **手动方式**：执行迁移脚本后再启动应用

## 验证方法

### 1. 检查表结构
```sql
PRAGMA table_info(bill_transaction);
```

查看 income、expense、balance 字段的 type 应该为 TEXT。

### 2. 检查数据精度
```sql
SELECT income, expense, balance FROM bill_transaction LIMIT 10;
```

金额应该显示为精确的小数，如 `2.47` 而不是 `2.4699999999999998`。

### 3. 应用日志
启动应用时查看日志：
```
✅ bill_transaction表无需迁移
```
或
```
检测到bill_transaction表需要迁移（修复金额字段精度问题）...
步骤1: 备份原表...
步骤2: 创建新表结构...
步骤3: 迁移数据...
步骤4: 重建索引...
步骤5: 删除备份表...
✅ bill_transaction表迁移完成
```

## 注意事项

1. **数据安全**：迁移过程使用事务，失败会自动回滚
2. **备份建议**：重要数据建议先备份数据库文件
3. **性能影响**：TEXT 类型存储对查询性能影响极小
4. **代码无需修改**：Java 实体类继续使用 BigDecimal 类型

## 技术细节

### SQLite 类型系统
SQLite 使用动态类型系统，声明的类型只是建议：
- `DECIMAL` → 实际存储为 `REAL`（浮点数）
- `TEXT` → 实际存储为 `TEXT`（字符串）

### MyBatis-Plus 类型转换
MyBatis-Plus 内置类型处理器支持：
- Java `BigDecimal` ↔ 数据库 `TEXT`
- 自动进行字符串与数值的转换
- 保证精度不丢失

## 相关文件

- `DatabaseMigration.java` - 自动迁移配置
- `SqliteConfig.java` - 表创建配置
- `migration_fix_decimal.sql` - 手动迁移脚本
- `schema.sql` - 表结构文档

## 版本信息

- 修复版本：v1.5.0
- 修复日期：2025-01-XX
- 影响范围：bill_transaction 表的金额字段
