# 招商银行账单管理 Vue 前端

这是一个基于 Vue 3 + Vite 的前端项目，使用 Element Plus UI 组件库，用于招商银行账单的导入、解析和分析。

## 项目结构

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

## 技术栈

- Vue 3 (Composition API)
- Vite 构建工具
- Element Plus UI 组件库
- Axios HTTP客户端

## 开发环境搭建

### 前置条件

- Node.js (版本 14.18+ 或 16+)
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

默认情况下，开发服务器将在 http://localhost:3000 上运行。

### 构建生产版本

```bash
npm run build
```

构建后的文件将位于 `dist` 目录中。

### 预览生产版本

```bash
npm run serve
```

## 功能说明

- 使用 Element Plus UI 组件提供现代化界面
- 账单文件导入和解析
- 与后端 `/api/cmb/import-full` 接口通信
- 显示导入结果或错误信息

## 代理配置

在 `vite.config.js` 中配置了代理，将 `/api` 请求代理到后端服务：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

确保后端服务在 http://localhost:8080 上运行。

## 使用方法

1. 启动后端服务
2. 运行 `npm run dev` 启动前端开发服务器
3. 在浏览器中访问 http://localhost:3000
4. 选择招商银行导出的CSV文件并导入
5. 查看解析结果

## Element Plus 组件使用

项目中使用了以下 Element Plus 组件：
- el-container, el-header, el-main, el-footer (布局组件)
- el-card (卡片组件)
- el-form, el-form-item (表单组件)
- el-upload (上传组件)
- el-button (按钮组件)
- el-table, el-table-column (表格组件)
- el-alert (警告组件)
- ElMessage (消息提示)

所有组件均已正确配置和引入，无需手动导入单个组件。