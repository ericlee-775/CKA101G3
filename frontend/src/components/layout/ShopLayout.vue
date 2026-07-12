<script setup>
// 商城前台版型：頂部橫向導覽列 + 內容 + 頁尾
import ShopHeader from './ShopHeader.vue'
import ShopFooter from './ShopFooter.vue'
</script>

<template>
  <!-- 頂部導覽列：sticky 固定在每一頁最上方 -->
  <ShopHeader />

  <!-- 「畫框」：router 會把對應的前台頁面畫在這裡 -->
  <main class="shop-main">
    <router-view />
  </main>

  <!-- 頁尾：固定顯示在每一頁最底部 -->
  <ShopFooter />
</template>

<style scoped>
.shop-main {
  /* 讓內容區至少撐滿一個視窗高，頁尾才不會浮在中間 */
  min-height: 60vh;
  /* 全站前台統一米白紙底（theme --paper 原意就是整站背景），讓各頁與首頁一致 */
  background: var(--paper);
}
</style>
