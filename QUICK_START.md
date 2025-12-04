# 快速开始指南

## 🚀 5分钟快速启动

### 步骤1：检查环境

运行环境检查脚本：
```bash
cd scripts
check-env.bat
```

确保以下软件已安装：
- ✅ Java 1.8+
- ✅ Maven 3.6+
- ✅ MySQL 5.7+
- ✅ Node.js 16+

### 步骤2：初始化数据库

运行数据库初始化脚本：
```bash
cd scripts
init-mysql.bat
```

按提示输入MySQL root密码，脚本会自动创建数据库和表。

### 步骤3：配置数据库连接

编辑 `personal-ledger-backend/src/main/resources/application-mysql.yml`：

```yaml
spring:
  datasource:
    username: root
    password: your_password  # 修改为你的MySQL密码
```

### 步骤4：启动应用

运行启动脚本：
```bash
cd scripts
start-dev.bat
```

或手动启动：

**后端：**
```bash
cd personal-ledger-backend
mvn spring-boot:run
```

**前端：**
```bash
cd personal-ledger-frontend
npm install
npm run dev
```

### 步骤5：访问应用

打开浏览器访问：
- 前端：http://localhost:5173
- 后端：http://localhost:8080

## 📋 详细步骤

### 1. 环境准备

#### Windows系统

**安装Java：**
1. 下载：https://www.oracle.com/java/technologies/downloads/
2. 安装并配置JAVA_HOME环境变量
3. 验证：`java -version`

**安装Maven：**
1. 下载：https://maven.apache.org/download.cgi
2. 解压并配置PATH环境变量
3. 验证：`mvn -version`

**安装MySQL：**
1. 下载：https://dev.mysql.com/downloads/mysql/
2. 安装并设置root密码
3. 启动MySQL服务
4. 验证：`mysql --version`

**安装Node.js：**
1. 下载：https://nodejs.org/
2. 安装（包含npm）
3. 验证：`node -v` 和 `npm -v`

### 2. 克隆项目

```bash
git clone https://github.com/your-repo/personal-ledger.git
cd personal-ledger
```

### 3. 数据库初始化

#### 方式一：使用脚本（推荐）
```bash
cd scripts
init-mysql.bat
```

#### 方式二：手动执行
```bash
mysql -u root -p
```

```sql
CREATE DATABASE personal_ledger DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE personal_ledger;
SOURCE personal-ledger-backend/src/main/resources/db/schema.sql;
```

### 4. 配置应用

编辑 `personal-ledger-backend/src/main/resources/application-mysql.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_ledger?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改这里
```

### 5. 启动后端

```bash
cd personal-ledger-backend
mvn clean install
mvn spring-boot:run
```

看到以下信息表示启动成功：
```
Started PersonalLedgerApplication in X.XXX seconds
```

### 6. 启动前端

```bash
cd personal-ledger-frontend
npm install
npm run dev
```

看到以下信息表示启动成功：
```
VITE v4.5.0  ready in XXX ms
➜  Local:   http://localhost:5173/
```

### 7. 开始使用

1. 打开浏览器访问 http://localhost:5173
2. 点击"数据导入" → "标准导入"或"快速导入"
3. 上传招商银行CSV账单文件
4. 点击"数据清洗"优化数据
5. 查看"交易记录"和"消费分析"

## 🔧 常见问题

### Q1: 端口被占用
**错误**：`Port 8080 is already in use`

**解决**：
```bash
# Windows查找占用端口的进程
netstat -ano | findstr :8080
# 结束进程
taskkill /PID <进程ID> /F
```

### Q2: MySQL连接失败
**错误**：`Communications link failure`

**解决**：
1. 检查MySQL服务是否启动
2. 验证用户名密码
3. 确认数据库已创建
4. 检查防火墙设置

### Q3: Maven下载慢
**解决**：配置阿里云镜像

编辑 `~/.m2/settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### Q4: npm安装慢
**解决**：使用淘宝镜像
```bash
npm config set registry https://registry.npmmirror.com
```

### Q5: 数据库字符乱码
**解决**：检查字符集
```sql
SHOW VARIABLES LIKE 'character%';
ALTER DATABASE personal_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 📚 下一步

### 基础使用
1. [账单导入指南](README.md#账单导入方式)
2. [数据清洗说明](README.md#智能数据清洗)
3. [数据分析功能](README.md#数据分析功能)

### 高级配置
1. [数据库配置说明](docs/DATABASE_CONFIG.md)
2. [MySQL升级指南](docs/MYSQL_UPGRADE_GUIDE.md)
3. [性能优化建议](docs/MYSQL_UPGRADE_GUIDE.md#性能优化建议)

### 开发文档
1. [API接口文档](README.md#api接口)
2. [项目结构说明](README.md#项目结构)
3. [技术栈介绍](README.md#技术栈)

## 🎯 使用技巧

### 1. 快捷键
- 标准导入 ↔ 快速导入：页面内切换按钮
- 刷新数据：F5
- 导出Excel：交易记录页面"导出"按钮

### 2. 数据管理
- 定期导出Excel备份数据
- 使用分类和渠道管理功能组织数据
- 配置清洗规则自动化数据处理

### 3. 性能优化
- 大量数据时使用日期筛选
- 定期清理无效导入历史
- 合理设置连接池参数

## 💡 最佳实践

### 月度管理流程
1. **月初**：导出上月Excel文件备份
2. **月中**：导入新的CSV账单
3. **月末**：
   - 添加备注和分类
   - 查看消费分析
   - 导出当月Excel文件

### 数据安全
1. 定期备份MySQL数据库
2. 导出Excel文件作为二次备份
3. 重要操作前先备份

### 性能优化
1. 根据实际负载调整连接池
2. 定期执行数据库优化
3. 清理历史无用数据

## 🆘 获取帮助

### 文档
- [README](README.md) - 项目完整说明
- [升级指南](docs/MYSQL_UPGRADE_GUIDE.md) - MySQL升级详细步骤
- [配置说明](docs/DATABASE_CONFIG.md) - 数据库配置参数

### 日志
- 后端日志：`personal-ledger-backend/logs/spring.log`
- 前端日志：浏览器控制台（F12）

### 支持
- 提交Issue：GitHub Issues
- 查看文档：docs目录
- 查看示例：项目README

---

**祝使用愉快！** 🎉
