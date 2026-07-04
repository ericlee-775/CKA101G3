<script setup>
  import { onMounted } from 'vue';
  import IndexHeader from './components/layout/IndexHeader.vue';
  import IndexFooter from './components/layout/IndexFooter.vue';
  import ConfirmDialog from './components/ConfirmDialog.vue';
  import authStore from '@/stores/auth';

  // App 一啟動就以「後端 session」為準重新確認登入狀態：
  // 有記住身分就向後端 /me 驗證，session 失效(401)會自動清掉。
  onMounted(() => {
    authStore.hydrate();
  });
</script>

<template>
    <!-- 導覽列：固定顯示在每一頁的最上方，不會被換掉 -->
    <IndexHeader></IndexHeader>

    <!-- 「畫框」：router 會根據目前網址，把對應的頁面元件畫在這裡 -->
    <router-view></router-view>

    <!-- 頁尾：固定顯示在每一頁的最底部 -->
    <IndexFooter></IndexFooter>

    <!-- 全域確認彈窗：任何地方呼叫 confirm() 都由這個元件顯示 -->
    <ConfirmDialog></ConfirmDialog>
</template>

<style scoped>
  
</style>


<style>
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
}
</style>
