# Personal Ledger 个人账簿系统

基于 Spring Boot + Vue 3 的个人账簿管理系统，专门用于招商银行账单数据的导入、清洗、分析和管理。

## 项目结构

```
personal-ledger/
├── personal-ledger-backend/        # 后端服务模块
│   ├── src/main/java/             # Java源码
│   │   └── com/gaoyan/personalledger/
│   │       ├── controller/        # REST API控制器
│   │       ├── entity/           # 数据实体类
│   │       ├── service/          # 业务逻辑服务
│   │       │   └── impl/         # 服务实现类
│   │       ├── util/             # 工具类
│   │       ├── exception/        # 异常处理
│   │       ├── listener/         # 事件监听器
│   │       └── demos/            # 演示代码
│   ├── src/main/resources/        # 配置文件和资源
│   │   ├── static/               # 静态资源
│   │   ├── application.yml       # 应用配置
│   │   └── banner.txt            # 启动横幅
│   └── pom.xml                    # Maven配置
├── personal-ledger-frontend/       # 前端应用模块 (Vue 3 + Vite)
│   ├── src/
│   │   ├── components/           # Vue组件
│   │   │   └── Layout.vue        # 布局组件
│   │   ├── views/                # 页面视图
│   │   │   ├── CmbBillImport.vue # 招商银行账单导入页面
│   │   │   └── Dashboard.vue     # 仪表板页面
│   │   ├── router/               # 路由配置
│   │   │   └── index.js          # 路由定义
│   │   ├── App.vue               # 根组件
│   │   └── main.js               # 应用入口
│   ├── index.html                # HTML模板
│   ├── package.json              # 前端依赖配置
│   ├── package-lock.json         # 依赖锁定文件
│   └── vite.config.js            # Vite构建配置
├── docs/                          # 项目文档
├── scripts/                       # 构建和部署脚本
│   ├── start-dev.bat             # Windows开发环境启动脚本
│   └── build.bat                 # Windows构建脚本
└── README.md                      # 项目说明
```

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 开发环境启动

#### 方式一：使用脚本启动（推荐）
```bash
# Windows
scripts\start-dev.bat
```

#### 方式二：手动启动

**启动后端服务：**
```bash
cd personal-ledger-backend
mvn spring-boot:run
```
后端服务将在 http://localhost:8080 启动

**启动前端开发服务器：**
```bash
cd personal-ledger-frontend
npm install
npm run dev
```
前端开发服务器将在 http://localhost:5173 启动

### 构建部署

```bash
# Windows
scripts\build.bat
```

构建完成后：
- 后端JAR包位于：`personal-ledger-backend/target/`
- 前端静态文件位于：`personal-ledger-frontend/dist/`

## 核心功能

### 已实现功能
- ✅ **招商银行CSV账单导入** - 支持招商银行标准CSV格式文件上传
- ✅ **智能数据清洗** - 自动清洗和标准化交易记录数据
- ✅ **交易记录管理** - 完整的交易记录展示和管理
- ✅ **灵活筛选功能** - 支持按日期范围筛选交易记录
- ✅ **收支统计分析** - 实时计算收入、支出统计信息
- ✅ **Excel数据导出** - 支持将处理后的数据导出为Excel文件
- ✅ **用户备注功能** - 支持为交易记录添加自定义备注
- ✅ **收支计算控制** - 可设置交易记录是否计入月度收支统计
- ✅ **响应式界面** - 基于Element Plus的现代化用户界面

### 规划中功能
- 🔄 多银行支持（工商银行、建设银行等）
- 🔄 数据可视化图表
- 🔄 预算管理功能
- 🔄 分类标签系统

## 技术栈

**后端技术：**
- **Spring Boot 2.6.13** - 主框架
- **Maven** - 项目构建管理
- **Lombok** - 简化Java代码
- **Hutool 5.8.16** - Java工具库
- **EasyExcel 3.1.1** - Excel文件处理
- **Java 1.8** - 运行环境

**前端技术：**
- **Vue 3.3.0** - 前端框架
- **Vite 4.5.0** - 构建工具
- **Element Plus 2.4.1** - UI组件库
- **Vue Router 4.2.0** - 路由管理
- **Axios 1.12.2** - HTTP客户端
- **@element-plus/icons-vue** - 图标库

**开发工具：**
- **@vitejs/plugin-vue 4.4.0** - Vue单文件组件支持
- **unplugin-auto-import 0.16.7** - 自动导入
- **unplugin-vue-components 0.25.2** - 组件自动导入

## API接口

### 主要接口
- `POST /api/cmb/import-full` - 导入招商银行CSV账单文件
- `POST /api/cmb/clean-records` - 清洗账单记录数据
- `GET /api/cmb/records` - 获取账单记录列表
- `POST /api/cmb/export` - 导出账单数据为Excel文件
- `PUT /api/cmb/records/{id}/remark` - 更新交易记录备注
- `PUT /api/cmb/records/{id}/include-in-summary` - 设置是否计入统计

### 接口特性
- 支持跨域请求（CORS）
- 文件上传限制：10MB
- 自动异常处理和错误响应
- 支持中文文件名导出

## 使用说明

1. **导入账单**：在"招商银行账单导入"页面上传CSV文件
2. **数据清洗**：点击"数据清洗"按钮优化数据质量
3. **筛选数据**：使用日期筛选器查看特定时间段的交易
4. **添加备注**：为交易记录添加个人备注信息
5. **统计分析**：查看收支统计和分析结果
6. **导出数据**：将处理后的数据导出为Excel文件

## 项目特色

### 数据处理能力
- **智能CSV解析** - 自动识别招商银行CSV格式，支持中文字段
- **数据去重清洗** - 基于交易时间、金额、商户等多维度去重
- **异常数据处理** - 自动识别和标记异常交易记录
- **批量数据导入** - 支持大文件批量导入，内存优化处理

### 用户体验
- **响应式设计** - 适配桌面和移动端设备
- **实时数据更新** - 前后端数据实时同步
- **操作反馈** - 完整的成功/错误提示机制
- **数据可视化** - 直观的收支统计展示

## 文档

- [开发指南](docs/README.md)
- 更多文档正在完善中...

## 版本历史

### v1.0.0 (当前版本)
- ✅ 招商银行CSV账单导入功能
- ✅ 数据清洗和去重功能
- ✅ 交易记录管理和筛选
- ✅ Excel数据导出功能
- ✅ 用户备注和统计控制
- ✅ 响应式Web界面

## 贡献指南

欢迎提交Issue和Pull Request来改进项目！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 许可证

[Apache License 2.0](LICENSE) © 个人账簿项目