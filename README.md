# Personal Ledger 个人账簿系统

基于 Spring Boot + Vue 3 的个人账簿管理系统，专门用于招商银行账单数据的导入、清洗、分析和管理。

## 项目结构

```
personal-ledger/
├── personal-ledger-backend/        # 后端服务模块
│   ├── src/main/java/             # Java源码
│   ├── src/main/resources/        # 配置文件和资源
│   └── pom.xml                    # Maven配置
├── personal-ledger-frontend/       # 前端应用模块
│   └── cmb-bill-management/       # Vue应用
├── docs/                          # 项目文档
├── scripts/                       # 构建和部署脚本
├── docker/                        # Docker配置(待添加)
├── pom.xml                        # 根模块Maven配置
└── README.md                      # 项目说明
```

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 开发环境启动

#### 方式一：使用脚本启动
```bash
# Windows
scripts\start-dev.bat

# Linux/Mac
chmod +x scripts/start-dev.sh
./scripts/start-dev.sh
```

#### 方式二：手动启动

**启动后端服务：**
```bash
cd personal-ledger-backend
mvn spring-boot:run
```

**启动前端开发服务器：**
```bash
cd personal-ledger-frontend/cmb-bill-management
npm install
npm run dev
```

### 构建部署

```bash
# Windows
scripts\build.bat

# Linux/Mac
./scripts/build.sh
```

## 核心功能

- ✅ 招商银行CSV账单导入
- ✅ 智能数据清洗和分类
- ✅ 交易记录管理和筛选
- ✅ 收支统计分析
- ✅ Excel数据导出
- 🔄 多银行支持(规划中)
- 🔄 数据可视化(规划中)

## 技术栈

**后端：**
- Spring Boot 2.6.13
- Maven
- Lombok
- Hutool
- EasyExcel

**前端：**
- Vue 3
- Vite
- Element Plus
- Axios

## 文档

- [开发指南](docs/README.md)
- [API文档](docs/api.md)
- [部署指南](docs/deployment.md)
