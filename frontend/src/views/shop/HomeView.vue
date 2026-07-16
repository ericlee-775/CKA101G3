<script setup>
import { ref, onMounted } from 'vue'
import EditorialHero from '@/components/home/EditorialHero.vue'
import noImage from '@/assets/no-image.svg'

// 首頁的三個賣點（特色區用 v-for 渲染）
const features = ref([
  { icon: '🚚', title: '產地直送', desc: '當日現採、低溫宅配，從農場到餐桌最短距離。' },
  { icon: '🌿', title: '無毒栽培', desc: '與通過認證的小農合作，安心吃得到原味。' },
  { icon: '🤝', title: '支持在地', desc: '每一筆訂單，都是對台灣土地與小農的支持。' },
])

// 熱門商品（管理員結算的點擊排行前 4 名）。還沒結算過時是空陣列，整個區塊會隱藏。
// 加入購物車統一在商品詳情頁進行（可選數量），首頁卡片點了就導過去。
const products = ref([])

async function loadHotProducts() {
  try {
    const res = await fetch('/api/products/hot')
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    products.value = await res.json()
  } catch (e) {
    // 抓不到就維持空陣列讓區塊隱藏，不影響首頁其他內容
    console.error('無法載入熱門商品', e)
  }
}

onMounted(loadHotProducts)
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

    <!-- ========== 熱門商品（點擊排行，管理員結算後顯示；沒資料整區隱藏） ========== -->
    <section v-if="products.length > 0" class="section">
      <div class="section-head">
        <h2>熱門商品</h2>
        <router-link class="more-link" to="/products">看全部商品 →</router-link>
      </div>

      <div class="product-grid">
        <router-link
          v-for="p in products"
          :key="p.productId"
          class="product-card"
          :to="{ name: 'product-detail', params: { productId: p.productId } }"
        >
          <!-- 用後端圖片端點當 src；沒圖（404）時 @error 換成預設圖，同 FavoriteProducts 做法 -->
          <img :src="`/api/products/${p.productId}/image`" :alt="p.productName"
               loading="lazy" @error="$event.target.src = noImage" />
          <div class="product-body">
            <h3>{{ p.productName }}</h3>
            <p class="price">NT$ {{ p.retailPrice }}</p>
            <span class="product-cta">查看商品 →</span>
          </div>
        </router-link>
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
/* 首頁專屬的大地色紙感背景（其他頁面維持白底,不上色） */
.home {
  background: var(--paper);
}

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
