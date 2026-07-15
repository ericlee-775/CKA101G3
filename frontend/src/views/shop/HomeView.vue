<script setup>
import { ref } from 'vue'
import EditorialHero from '@/components/home/EditorialHero.vue'
import FarmerCta from '@/components/home/FarmerCta.vue'

// 首頁的三個賣點（特色區用 v-for 渲染）
const features = ref([
  { icon: '🚚', title: '產地直送', desc: '當日現採、低溫宅配，從農場到餐桌最短距離。' },
  { icon: '🌿', title: '無毒栽培', desc: '與通過認證的小農合作，安心吃得到原味。' },
  { icon: '🤝', title: '支持在地', desc: '每一筆訂單，都是對台灣土地與小農的支持。' },
])

// 精選商品（示範資料，之後可改成從 /api/products 抓）。
// 加入購物車統一在商品詳情頁進行（可選數量），首頁卡片點了就導過去。
const products = ref([
  { id: 1, name: '有機小番茄', price: 120, image: 'https://picsum.photos/seed/tomato/400/300' },
  { id: 2, name: '溫室水蜜桃', price: 380, image: 'https://picsum.photos/seed/peach/400/300' },
  { id: 3, name: '高山高麗菜', price: 90,  image: 'https://picsum.photos/seed/cabbage/400/300' },
  { id: 4, name: '產地鮮乳',   price: 95,  image: 'https://picsum.photos/seed/milk/400/300' },
])
</script>

<template>
  <main class="home">
    <!-- ========== 主視覺 Hero（編輯風主視覺元件） ========== -->
    <EditorialHero />

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
        <router-link
          v-for="p in products"
          :key="p.id"
          class="product-card"
          :to="{ name: 'product-detail', params: { productId: p.id } }"
        >
          <img :src="p.image" :alt="p.name" />
          <div class="product-body">
            <h3>{{ p.name }}</h3>
            <p class="price">NT$ {{ p.price }}</p>
            <span class="product-cta">查看商品 →</span>
          </div>
        </router-link>
      </div>
    </section>

    <!-- ========== 小農招募廣告（深墨綠面板，對農夫說話） ========== -->
    <FarmerCta />
  </main>
</template>

<style scoped>
/* 首頁專屬的大地色紙感背景（其他頁面維持白底,不上色） */
.home {
  background: var(--paper);
  /* 底部留白:讓小農招募面板和深色 footer 之間隔一段紙色,
     兩塊深綠才不會黏在一起。用 padding 而非 margin(margin 會露出 body 白底) */
  padding-bottom: 72px;
}

/* ========== 三大特色 ========== */
.features {
  max-width: 1100px;
  margin: 8px auto 0;               /* 編輯風 hero 已自帶留白，這裡用小正距即可 */
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
  display: block;
  text-decoration: none;
  color: inherit;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
.product-cta {
  display: inline-block;
  font-size: 13px;
  color: var(--muted);
  transition: color 0.18s ease;
}
.product-card:hover .product-cta {
  color: var(--leaf);
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

/* ========== 響應式 ========== */
@media (max-width: 760px) {
  .features {
    grid-template-columns: 1fr;
    margin-top: 24px;
  }
}
</style>
