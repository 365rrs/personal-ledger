<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside class="sidebar" width="250px">
      <div class="logo">
        <h2>个人账簿系统</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/cmb-bill">
          <el-icon><Document /></el-icon>
          <span>招商银行账单</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 右侧内容区域 -->
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-content">
          <span class="page-title">{{ $route.meta.title || '个人账簿系统' }}</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { House, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b2f3a;
  color: #fff;
  margin: 0;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border: none;
  height: calc(100vh - 60px);
}

.main-container {
  background-color: #f0f2f5;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}
</style>