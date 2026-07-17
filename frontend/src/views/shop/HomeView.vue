<script setup>
import { ref, computed, onMounted } from 'vue'
import EditorialHero from '@/components/home/EditorialHero.vue'
import FarmerCta from '@/components/home/FarmerCta.vue'
import { listPublicBlogs, listBlogTypes } from '@/api/blog'
import { listFarms, normalizeFarm } from '@/api/farm'
import groupBuyApi from '@/api/groupBuy'
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

/* ========== 首頁各區塊（最新消息 / 農場 / 團購 / 體驗活動 / 部落格）==========
   每區各抓幾筆真實資料，統一整理成 { image, badge, title, desc, meta, to } 卡片格式，
   模板用同一套版面渲染（比照部落格卡片）。各區獨立載入，一區失敗不影響其他。 */
const LIMIT = 4
const newsCards = ref([])
const farmCards = ref([])
const groupBuyCards = ref([])
const tripCards = ref([])
const blogCards = ref([])

const stripHtml = (html) => (html || '').replace(/<[^>]*>/g, '').trim()
const fmtDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d)) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`
}
// 圖片載入失敗就換成佔位圖，讓每張卡都有圖、版面一致
const onImgError = (e) => { e.target.src = noImage }

async function loadNews() {
  const res = await fetch(`/api/news?limit=${LIMIT}&offset=0`, { credentials: 'include' })
  if (!res.ok) return
  const data = await res.json()
  newsCards.value = (data.results || []).map((n) => ({
    key: 'news-' + n.newsId,
    image: `/api/news/${n.newsId}/image`,
    badge: '最新消息',
    title: n.title,
    desc: stripHtml(n.content),
    meta: fmtDate(n.publishTime || n.createdAt),
    to: { name: 'news-detail', params: { newsId: n.newsId } },
  }))
}

async function loadFarms() {
  const farms = (await listFarms()).map(normalizeFarm).slice(0, LIMIT)
  farmCards.value = farms.map((f) => ({
    key: 'farm-' + f.farmerId,
    image: noImage,                      // 農場無專屬圖片端點，統一用佔位圖
    badge: f.region || '合作農場',
    title: f.farmName,
    desc: f.farmDesc,
    meta: f.location ? '📍 ' + f.location : '',
    to: { name: 'farm-detail', params: { farmerId: f.farmerId } },
  }))
}

async function loadGroupBuys() {
  const list = (await groupBuyApi.list('all')).slice(0, LIMIT)
  groupBuyCards.value = list.map((g) => ({
    key: 'gb-' + (g.groupBuyId ?? 'p' + g.productId),
    image: `/api/products/${g.productId}/image`,
    badge: '團購',
    title: g.productName,
    desc: g.description,
    meta: g.groupPrice != null ? `團購價 NT$ ${g.groupPrice}` : '',
    // 已開團的導到團購詳情，還沒開的導到「發起團購」頁
    to: g.groupBuyId != null
      ? { name: 'group-buy-detail', params: { groupBuyId: g.groupBuyId } }
      : { name: 'group-buy-host', params: { productId: g.productId } },
  }))
}

async function loadTrips() {
  const res = await fetch('/api/farm-trips', { credentials: 'include' })
  if (!res.ok) return
  const arr = await res.json()
  tripCards.value = (arr || []).slice(0, LIMIT).map((t) => ({
    key: 'trip-' + t.farmTripId,
    image: `/api/farm-trips/${t.farmTripId}/image`,
    badge: '體驗活動',
    title: t.farmTripTitle,
    desc: t.farmName ? '🏡 ' + t.farmName : '',
    meta: t.location ? '📍 ' + t.location : '',
    to: { name: 'farm-trip-detail', params: { farmTripId: t.farmTripId } },
  }))
}

async function loadBlogs() {
  const types = await listBlogTypes().catch(() => [])
  const typeMap = Object.fromEntries((types || []).map((t) => [t.blogTypeId, t.blogTypeName]))
  const data = await listPublicBlogs(0, LIMIT)
  blogCards.value = (data.results || []).map((b) => ({
    key: 'blog-' + b.blogId,
    image: `/api/blogs/${b.blogId}/image`,
    badge: typeMap[b.blogTypeId] || '文章',
    title: b.blogTitle,
    author: b.authorName ? (b.authorType === 'FARMER' ? '🏡 ' : '👤 ') + b.authorName : '',
    desc: stripHtml(b.blogContent),
    meta: fmtDate(b.blogTime),
    to: `/blogs/${b.blogId}`,
  }))
}

onMounted(() => {
  loadNews().catch(() => {})
  loadFarms().catch(() => {})
  loadGroupBuys().catch(() => {})
  loadTrips().catch(() => {})
  loadBlogs().catch(() => {})
})

// 一份設定描述五個區塊，順序即畫面由上到下的順序
const sections = computed(() => [
  { key: 'news',  title: '最新消息', moreTo: '/news',       moreText: '看全部消息 →', items: newsCards.value },
  { key: 'farms', title: '農場資訊', moreTo: '/farmily',    moreText: '看全部農場 →', items: farmCards.value },
  { key: 'gb',    title: '團購',     moreTo: '/group-buys', moreText: '看全部團購 →', items: groupBuyCards.value },
  { key: 'trips', title: '體驗活動', moreTo: '/farm-trips', moreText: '看全部活動 →', items: tripCards.value },
  { key: 'blogs', title: '部落格',   moreTo: '/blogs',      moreText: '看全部文章 →', items: blogCards.value },
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

    <!-- ========== 最新消息 / 農場 / 團購 / 體驗活動 / 部落格 ========== -->
    <section
      v-for="s in sections"
      v-show="s.items.length"
      :key="s.key"
      class="section"
    >
      <div class="section-head">
        <h2>{{ s.title }}</h2>
        <router-link class="more-link" :to="s.moreTo">{{ s.moreText }}</router-link>
      </div>

      <div class="home-grid">
        <router-link
          v-for="item in s.items"
          :key="item.key"
          class="home-card"
          :to="item.to"
        >
          <img class="home-card__img" :src="item.image" alt="" @error="onImgError" />
          <div class="home-card__body">
            <span class="home-card__badge">{{ item.badge }}</span>
            <h3>{{ item.title }}</h3>
            <p v-if="item.author" class="home-card__author">{{ item.author }}</p>
            <p v-if="item.desc" class="home-card__desc">{{ item.desc }}</p>
            <p v-if="item.meta" class="home-card__meta">{{ item.meta }}</p>
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

/* ========== 首頁區塊卡片（比照部落格卡片，等高對齊）========== */
.home-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.home-card {
  display: flex;
  flex-direction: column;
  height: 100%;                 /* 撐滿格線列高，內容多寡不影響卡片高度 */
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  box-shadow: var(--shadow);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.home-card:hover {
  transform: translateY(-4px);
  border-color: var(--leaf);
  box-shadow: var(--shadow-hover);
}
.home-card__img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  background: var(--leaf-soft);
}
.home-card__body {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 16px 18px;
}
.home-card__badge {
  align-self: flex-start;       /* 不被 flex 拉滿整行 */
  margin-bottom: 8px;
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 12px;
  font-weight: 600;
}
.home-card__body h3 {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 17px;
}
.home-card__author {
  margin: 0 0 8px;
  color: var(--muted);
  font-size: 13px;
}
.home-card__desc {
  margin: 0 0 14px;
  color: var(--ink-soft);
  font-size: 14px;
  line-height: 1.7;
  max-height: 4.4em;
  overflow: hidden;
}
.home-card__meta {
  margin: auto 0 0;             /* 推到卡片底部對齊 */
  color: var(--muted);
  font-size: 12px;
}

/* ========== 響應式 ========== */
@media (max-width: 760px) {
  .features {
    grid-template-columns: 1fr;
    margin-top: 24px;
  }
}
</style>
