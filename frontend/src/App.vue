<script setup>
  import IndexHeader from './components/layout/IndexHeader.vue';
  import IndexFooter from './components/layout/IndexFooter.vue';
</script>

<template>
    <!-- 導覽列：固定顯示在每一頁的最上方，不會被換掉 -->
    <IndexHeader></IndexHeader>

    <!-- 「畫框」：router 會根據目前網址，把對應的頁面元件畫在這裡 -->
    <router-view></router-view>

    <!-- 頁尾：固定顯示在每一頁的最底部 -->
    <IndexFooter></IndexFooter>
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
