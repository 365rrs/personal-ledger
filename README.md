# Personal Ledger 个人账簿系统

这是一个基于 Spring Boot 的个人账簿管理系统，用于记录、管理和分析个人财务信息。

## 项目结构

```
personal-ledger/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/gaoyan/personalledger/
│       │       ├── PersonalLedgerApplication.java  # Spring Boot 启动类
│       │       ├── controller/                     # 控制器目录
│       │       │   └── CmbBillController.java      # 招商银行账单控制器
│       │       ├── entity/                         # 实体类目录
│       │       │   ├── CmbBillInfo.java            # 账单信息实体
│       │       │   ├── CmbBillRecordExport.java    # 账单记录导出实体
│       │       │   ├── CmbBillRecordReal.java      # 账单记录实体
│       │       │   ├── CmbExportInfo.java          # 导出信息实体
│       │       │   └── CmbSummaryInfo.java         # 汇总信息实体
│       │       ├── exception/                      # 异常处理类目录
│       │       │   ├── BusinessException.java     # 业务异常类
│       │       │   └── GlobalExceptionHandler.java # 全局异常处理器
│       │       ├── service/                        # 业务逻辑层目录
│       │       │   ├── impl/                       # 业务逻辑实现目录
│       │       │   │   ├── CmbBillServiceImpl.java # 账单服务实现
│       │       │   │   └── DataCleaningServiceImpl.java # 数据清洗服务实现
│       │       │   ├── CmbBillService.java         # 账单服务接口
│       │       │   └── DataCleaningService.java    # 数据清洗服务接口
│       │       ├── util/                           # 工具类目录
│       │       │   └── EasyExcelExportUtil.java    # Excel导出工具类
│       │       ├── demos/                          # 示例代码目录
│       │       │   └── web/                        # Web示例
│       │       └── listener/                       # 监听器目录
│       │           └── StartupInfoListener.java    # 启动信息监听器
│       └── resources/
│           ├── application.yml                     # 配置文件
│           ├── banner.txt                          # 启动横幅
│           └── static/                             # 静态资源目录
│               ├── index.html                      # 主页面
│               └── cmb-import.html                 # 招商银行导入页面
├── cmb-bill-management/                            # 前端项目目录
├── pom.xml                                         # Maven配置文件
└── README.md                                       # 项目说明文档
```

## 技术栈

- Java 8
- Spring Boot 2.6.13
- Maven 项目构建工具
- Lombok 代码简化工具
- Hutool 工具库 5.8.16
- EasyExcel 3.1.1 (Excel处理)

## 功能特性

- 银行账单导入（支持招商银行等）
- 账单数据解析
- 数据清洗功能
- 财务数据分析
- 数据可视化展示
- 用户权限管理

## 环境要求

- JDK 1.8 或更高版本
- Maven 3.6+

## 快速开始

### 克隆项目

```bash
git clone <项目地址>
```

### 配置说明

当前项目配置在 `application.yml` 中：

```yaml
# 应用服务 WEB 访问端口
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 10MB      # 最大文件上传大小
      max-request-size: 10MB   # 最大请求大小
```

### 构建项目

```bash
mvn clean install
```

### 运行项目

```bash
mvn spring-boot:run
```

或者

```bash
java -jar target/personal-ledger-0.0.1-SNAPSHOT.jar
```

## 前端项目

前端项目位于 `cmb-bill-management` 目录中，是一个基于 Vue 3 + Vite 的招商银行账单管理系统前端。

### 前端项目结构

```
cmb-bill-management/
├── src/
│   ├── components/
│   │   └── CmbBillImport.vue  # 招商银行账单导入组件
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
├── index.html                 # HTML模板
├── vite.config.js             # Vite配置文件
└── package.json               # 项目配置文件
```

### 前端开发环境搭建

1. 进入前端项目目录：
   ```bash
   cd cmb-bill-management
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm run dev
   ```

默认情况下，前端开发服务器将在 http://localhost:3000 上运行。

## 访问地址

项目启动后，可通过以下地址访问：
- 主页面: http://localhost:8080/
- 招商银行账单导入: http://localhost:8080/cmb-import.html

## 数据清洗功能

本系统新增了数据清洗功能，可以对导入的银行账单数据进行清洗处理，包括：

1. 去除重复记录
2. 格式化日期和时间字段
3. 清理空值和无效数据
4. 标准化字段内容

### 使用方法

1. 在前端界面导入银行账单CSV文件
2. 点击"数据清洗"按钮对已导入的数据进行清洗处理
3. 清洗后的数据会自动更新到表格中

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化实体类代码
- 统一使用 YAML 格式配置文件

### 项目特点

- 支持大文件上传（最大10MB）
- 基于EasyExcel的高效Excel处理
- 使用Hutool工具库简化开发
- 前后端分离架构
- 内置静态资源服务

## 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。
