<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-content">
        <h1>招商银行账单管理系统</h1>
      </div>
    </el-header>
    <el-main class="app-main">
      <CmbBillImport />
    </el-main>
    <el-footer class="app-footer">
      <div class="footer-content">
        <p>© 2025 招商银行账单管理系统. All rights reserved.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import CmbBillImport from './components/CmbBillImport.vue'
</script>

<style>
#app {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  height: 100vh;
}

.app-container {
  height: 100%;
}

.app-header {
  background-color: #409EFF;
  color: #fff;
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.app-footer {
  background-color: #f5f5f5;
  color: #666;
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

body {
  margin: 0;
  padding: 0;
}

html, body {
  height: 100%;
}
</style>