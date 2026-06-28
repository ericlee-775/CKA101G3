<script setup>
import { ref } from 'vue'

// 首頁的三個賣點（特色區用 v-for 渲染）
const features = ref([
  { icon: '🚚', title: '產地直送', desc: '當日現採、低溫宅配，從農場到餐桌最短距離。' },
  { icon: '🌿', title: '無毒栽培', desc: '與通過認證的小農合作，安心吃得到原味。' },
  { icon: '🤝', title: '支持在地', desc: '每一筆訂單，都是對台灣土地與小農的支持。' },
])

// 精選商品（示範資料，之後可改成從 /api/products 抓）
const products = ref([
  { id: 1, name: '有機小番茄', price: 120, image: 'https://picsum.photos/seed/tomato/400/300' },
  { id: 2, name: '溫室水蜜桃', price: 380, image: 'https://picsum.photos/seed/peach/400/300' },
  { id: 3, name: '高山高麗菜', price: 90,  image: 'https://picsum.photos/seed/cabbage/400/300' },
  { id: 4, name: '產地鮮乳',   price: 95,  image: 'https://picsum.photos/seed/milk/400/300' },
])
</script>

<template>
  <main class="home">
    <!-- ========== 主視覺 Hero ========== -->
    <section class="hero">
      <div class="hero-inner">
        <h1>新鮮，從產地開始</h1>
        <p>Farmily 嚴選台灣在地小農，把當季最好的食材直送到你家餐桌。</p>
        <div class="hero-actions">
          <router-link class="btn btn-primary" to="/products">立即選購</router-link>
          <router-link class="btn btn-ghost" to="/farmily">認識農場</router-link>
        </div>
      </div>
    </section>

    <!-- ========== 三大特色 ========== -->
    <section class="features">
      <article v-for="f in features" :key="f.title" class="feature-card">
        <span class="feature-icon">{{ f.icon }}</span>
        <h3>{{ f.title }}</h3>
        <p>{{ f.desc }}</p>
      </article>
    </section>

    <!-- ========== 精選商品 ========== -->
    <section class="section">
      <div class="section-head">
        <h2>本週精選</h2>
        <router-link class="more-link" to="/products">看全部商品 →</router-link>
      </div>

      <div class="product-grid">
        <article v-for="p in products" :key="p.id" class="product-card">
          <img :src="p.image" :alt="p.name" />
          <div class="product-body">
            <h3>{{ p.name }}</h3>
            <p class="price">NT$ {{ p.price }}</p>
            <button class="btn btn-primary btn-sm" type="button">加入購物車</button>
          </div>
        </article>
      </div>
    </section>

    <!-- ========== 行動呼籲 CTA ========== -->
    <section class="cta">
      <h2>加入 Farmily，享受產地直送</h2>
      <p>註冊會員，第一筆訂單即享免運優惠。</p>
      <router-link class="btn btn-primary" to="/register">免費註冊</router-link>
    </section>
  </main>
</template>

<style scoped>
/* ========== 共用按鈕 ========== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 26px;
  border-radius: 999px;
  font-size: 15px;
  text-decoration: none;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.18s ease, color 0.18s ease, border-color 0.18s ease;
}
.btn-sm {
  padding: 8px 0;
  width: 100%;
  font-size: 14px;
}
.btn-primary {
  background: var(--leaf);
  color: #fff;
}
.btn-primary:hover {
  background: var(--leaf-dark);
}
.btn-ghost {
  background: transparent;
  color: #fff;
  border-color: rgba(255, 255, 255, 0.7);
}
.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.15);
}

/* ========== 主視覺 ========== */
.hero {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 440px;
  padding: 48px 18px;
  text-align: center;
  color: #fff;
  /* 綠色漸層疊在農場照片上，讓文字看得清楚 */
  background:
    linear-gradient(rgba(28, 48, 32, 0.55), rgba(28, 48, 32, 0.55)),
    url('https://picsum.photos/seed/farmhero/1600/700') center / cover no-repeat;
}
.hero-inner {
  max-width: 640px;
}
.hero h1 {
  margin: 0 0 16px;
  font-size: clamp(28px, 5vw, 46px);
}
.hero p {
  margin: 0 0 28px;
  font-size: clamp(15px, 2vw, 18px);
  line-height: 1.7;
  color: #eef3ec;
}
.hero-actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  flex-wrap: wrap;
}

/* ========== 三大特色 ========== */
.features {
  max-width: 1100px;
  margin: -48px auto 0;             /* 往上疊一點，蓋住 hero 底緣 */
  padding: 0 clamp(18px, 4vw, 56px);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  position: relative;
  z-index: 2;
}
.feature-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  padding: 26px;
  text-align: center;
}
.feature-icon {
  font-size: 34px;
}
.feature-card h3 {
  margin: 12px 0 8px;
  color: var(--ink);
  font-size: 18px;
}
.feature-card p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

/* ========== 區塊通用 ========== */
.section {
  max-width: 1100px;
  margin: 64px auto 0;
  padding: 0 clamp(18px, 4vw, 56px);
}
.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
}
.section-head h2 {
  margin: 0;
  color: var(--ink);
  font-size: 24px;
}
.more-link {
  color: var(--leaf);
  font-size: 14px;
  text-decoration: none;
}
.more-link:hover {
  text-decoration: underline;
}

/* ========== 商品格線 ========== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.product-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
.product-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.product-body {
  padding: 16px;
}
.product-body h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--ink);
}
.price {
  margin: 0 0 12px;
  color: var(--leaf);
  font-weight: 700;
  font-size: 17px;
}

/* ========== 行動呼籲 ========== */
.cta {
  max-width: 1100px;
  margin: 64px auto 0;
  padding: 48px clamp(18px, 4vw, 56px);
  text-align: center;
  background: var(--leaf-soft);
  border-radius: 18px;
}
.cta h2 {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 26px;
}
.cta p {
  margin: 0 0 24px;
  color: var(--ink-soft);
}

/* ========== 響應式 ========== */
@media (max-width: 760px) {
  .features {
    grid-template-columns: 1fr;
    margin-top: 24px;
  }
}
</style>
