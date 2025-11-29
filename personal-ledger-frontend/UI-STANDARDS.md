# 前端 UI 开发规范

## 页面布局标准

### 1. 页面容器
所有页面的根容器必须使用统一的 padding：

```vue
<template>
  <div class="page-name">
    <el-card shadow="hover">
      <!-- 页面内容 -->
    </el-card>
  </div>
</template>

<style scoped>
.page-name {
  padding: 20px;
}
</style>
```

### 2. 卡片组件
- 使用 `<el-card shadow="hover">` 作为主要内容容器
- 卡片之间间距：`margin-bottom: 20px`

### 3. 表单筛选区域
```vue
<el-form label-width="80px" class="filter-form">
  <!-- 表单内容 -->
</el-form>

<style scoped>
.filter-form {
  margin-bottom: 20px;
}
</style>
```

**注意**：不要给 filter-form 添加额外的 padding 或 background

### 4. 卡片头部
```vue
<template #header>
  <div class="header">
    <span>页面标题</span>
    <el-button type="primary">操作按钮</el-button>
  </div>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

## 完整示例

```vue
<template>
  <div class="example-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header">
          <span>示例页面</span>
          <el-button type="primary">操作</el-button>
        </div>
      </template>

      <el-form label-width="80px" class="filter-form">
        <!-- 筛选条件 -->
      </el-form>

      <!-- 页面主要内容 -->
    </el-card>
  </div>
</template>

<script setup>
// 页面逻辑
</script>

<style scoped>
.example-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}
</style>
```

## 参考页面
- `TransactionList.vue` - 标准布局参考
- `DataAnalysis.vue` - 统计页面参考
- `CategoryManagement.vue` - 管理页面参考
