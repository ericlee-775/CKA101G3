<script setup>
// 小農管理後台版型：左側「側邊欄」 + 右側內容區
import FarmerSidebar from './FarmerSidebar.vue'
</script>

<template>
  <div class="farmer-shell">
    <!-- 側邊欄：固定在左邊，放管理選單 -->
    <FarmerSidebar />

    <!-- 右側內容區：router 會把對應的管理頁面畫在這裡 -->
    <main class="farmer-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.farmer-shell {
  display: grid;
  grid-template-columns: 240px 1fr;   /* 左:側邊欄固定寬  右:內容撐滿 */
  min-height: 100vh;
  background: var(--cream, #f7f5ef);
}

.farmer-content {
  padding: clamp(20px, 3vw, 40px);
  overflow-x: auto;
}

/* 窄螢幕：側邊欄改成上方橫向，內容往下堆疊 */
@media (max-width: 820px) {
  .farmer-shell {
    grid-template-columns: 1fr;
  }
}
</style>
