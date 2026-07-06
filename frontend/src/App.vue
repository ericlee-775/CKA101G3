<script setup>
  import { onMounted } from 'vue';
  import ConfirmDialog from './components/ConfirmDialog.vue';
  import authStore from '@/stores/auth';

  // App 一啟動就以「後端 session」為準重新確認登入狀態：
  // 有記住身分就向後端 /me 驗證，session 失效(401)會自動清掉。
  onMounted(() => {
    authStore.ensureHydrated();
  });
</script>

<template>
    <!--
      App 只當「最外層外殼」：header / footer / 側邊欄都交給各自的 Layout
      元件（ShopLayout、FarmerLayout）處理，這裡只放全頁面共用的東西。
      router 依網址決定要套哪一個 Layout，畫在這個 <router-view> 裡。
    -->
    <router-view></router-view>

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
