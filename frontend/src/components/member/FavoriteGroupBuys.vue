<script setup>
// 會員中心：我的收藏 → 團購收藏（預設佔位元件）
// 團購收藏的所有程式碼（打 API、清單畫面、取消收藏）都寫在這個元件裡。
// 清單載入完成後記得 emit('count', 筆數)，讓外層 tab 顯示數字。
const emit = defineEmits(['count'])
</script>

<template>
  <section class="soon-card">
    <span class="soon-icon">👥</span>
    <h1>團購收藏</h1>
    <p>這個功能正在開發中，敬請期待！</p>
    <router-link class="soon-btn" to="/group-buys">先去看看團購 →</router-link>
  </section>
</template>

<style scoped>
.soon-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 64px 24px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  text-align: center;
}
.soon-icon {
  font-size: 44px;
  line-height: 1;
}
.soon-card h1 {
  margin: 0;
  font-size: 22px;
  color: var(--ink);
}
.soon-card p {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
}
.soon-btn {
  margin-top: 10px;
  padding: 9px 20px;
  border-radius: 999px;
  border: 1px solid var(--leaf);
  color: var(--leaf);
  font-size: 14px;
  text-decoration: none;
  transition: background 0.18s ease, color 0.18s ease;
}
.soon-btn:hover {
  background: var(--leaf);
  color: #fff;
}
</style>
