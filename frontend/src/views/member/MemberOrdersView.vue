<script setup>
// 會員中心：我的訂單（待接後端 API）
</script>

<template>
  <section class="soon-card">
    <span class="soon-icon">🧾</span>
    <h1>我的訂單</h1>
    <p>這個功能正在開發中，敬請期待！</p>
    <router-link class="soon-btn" to="/products">先去逛逛商品 →</router-link>
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
