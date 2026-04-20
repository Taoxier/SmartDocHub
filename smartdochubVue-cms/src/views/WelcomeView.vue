<template>
  <div class="welcome-container">
    <div class="left-section">
      <div class="welcome-title">
        <div style="font-size: 36px;font-weight: bold">SmartDocHub</div>
        <div style="margin-top: 16px;font-size: 16px;opacity: 0.9">智能原创文档文库系统</div>
        <div style="margin-top: 8px;font-size: 14px;opacity: 0.7">上传、管理、搜索、分享你的文档</div>
      </div>
    </div>
    <div class="right-card">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  background: linear-gradient(135deg, #fff5eb 0%, #ffe4cc 50%, #ffcc80 100%);
}

.left-section {
  flex: 1;
  display: flex;
  align-items: flex-end;
  padding: 40px;
}

.welcome-title {
  color: #e65100;
}

.right-card {
  width: 420px;
  z-index: 1;
  background-color: #fff;
  box-shadow: -4px 0 20px rgba(255, 152, 0, 0.1);
}
</style>