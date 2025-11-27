# 项目文档

## 目录结构

```
personal-ledger/
├── personal-ledger-backend/     # 后端服务模块
├── personal-ledger-frontend/    # 前端应用模块
├── docs/                       # 项目文档
├── scripts/                    # 构建和部署脚本
├── docker/                     # Docker配置文件
└── README.md                   # 项目说明
```

## 开发指南

### 后端开发
- 进入 `personal-ledger-backend` 目录
- 运行 `mvn spring-boot:run` 启动后端服务

### 前端开发
- 进入 `personal-ledger-frontend/cmb-bill-management` 目录
- 运行 `npm install` 安装依赖
- 运行 `npm run dev` 启动开发服务器

## API文档

详见 [API文档](api.md)

## 部署指南

详见 [部署文档](deployment.md)