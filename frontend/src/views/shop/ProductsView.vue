<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
// 「暫無圖片」佔位圖（放在 src/assets，打包後 Vite 會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

const PAGE_SIZE = 12

// 商品清單（無限捲動：一批一批往後累加，不覆蓋）。
const products = ref([])
const loading = ref(true) // 首次載入（第 0 頁）
const loadingMore = ref(false) // 捲到底載下一批
const error = ref('')

// 分頁狀態（無限捲動）。
const nextPage = ref(0) // 下一個要載的頁碼（0 起算）
const hasMore = ref(true) // 還有沒有更多可載

// 載入「下一頁」。用 nextPage 決定載哪頁，載完往後推一頁。
// loading / loadingMore / hasMore 當鎖，避免重複載入或載過頭。
async function loadNext() {
  // 用 loadingMore 當「載入中」鎖（哨兵要等第 0 頁載完才觀察，第 0 頁不會併發）。
  // 注意：不要用 loading 當鎖 —— 它初始就是 true，會擋掉第一次載入。
  if (loadingMore.value || !hasMore.value) return
  const page = nextPage.value
  if (page === 0) loading.value = true
  else loadingMore.value = true
  error.value = ''
  try {
    const res = await fetch(`/api/products?page=${page}&size=${PAGE_SIZE}`)
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    const data = await res.json()
    // 相容三種後端回傳：純陣列(List) / 舊版 Page(欄位在外層) / 新版 PagedModel(欄位在 page 裡)
    const list = Array.isArray(data) ? data : (data.content ?? [])
    const meta = data.page ?? data // 新版分頁資訊在 data.page，舊版在 data 本身
    const totalPages = Array.isArray(data) ? 1 : (meta.totalPages ?? 1)

    products.value.push(...list) // 往後接（首次時 products 本來就是空的）
    nextPage.value = page + 1
    hasMore.value = nextPage.value < totalPages
    loadImagesFor(list) // 只抓這批新商品的圖
  } catch (e) {
    error.value = e.message || '無法載入商品，請稍後再試。'
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 重新載入（錯誤重試用）：清空、回到第一頁。
function reload() {
  products.value = []
  nextPage.value = 0
  hasMore.value = true
  loadNext()
}

// 把 retailPrice 顯示成「NT$ 120」這種格式。
function formatPrice(price) {
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// ---------- 圖片 ----------
const FALLBACK_IMAGE = noImage
const imageUrls = ref({})

// 只抓「這批新商品」且還沒抓過的圖，避免重複請求。
function loadImagesFor(list) {
  for (const product of list) {
    if (!imageUrls.value[product.productId]) fetchImage(product.productId)
  }
}

async function fetchImage(id) {
  try {
    const res = await fetch(`/api/products/${id}/image`)
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[id] = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗維持預設圖，不影響其他商品。
  }
}

function productImage(product) {
  return imageUrls.value[product.productId] || FALLBACK_IMAGE
}

function revokeImageUrls() {
  for (const url of Object.values(imageUrls.value)) URL.revokeObjectURL(url)
}

// ---------- 無限捲動：IntersectionObserver 盯住底部哨兵 ----------
// 只觀察一次；使用者往下捲、哨兵進入視窗就載下一批（loadNext 內建鎖，不會重複）。
const sentinel = ref(null)
let observer = null

onMounted(async () => {
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) loadNext()
    },
    { rootMargin: '200px' }, // 距底部 200px 就先載，捲動較順
  )
  await loadNext() // 先載第 0 頁
  await nextTick() // 等哨兵渲染出來
  if (sentinel.value) observer.observe(sentinel.value)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  revokeImageUrls()
})
</script>

<template>
  <main class="page">
    <section class="hero">
      <h1>🥬 全部商品</h1>
      <p>嚴選合作農場的當季好物，直接從產地送到你家。</p>
    </section>

    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗：顯示錯誤訊息與重試按鈕 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <button type="button" @click="reload">重新載入</button>
    </div>

    <!-- 成功但沒有資料 -->
    <p v-else-if="products.length === 0" class="state">目前沒有商品。</p>

    <!-- 商品格線：整張卡片都是連結，點進商品詳情頁後才選數量、加入購物車 -->
    <section v-else class="product-grid">
      <RouterLink
        v-for="product in products"
        :key="product.productId"
        class="product-card"
        :to="{ name: 'product-detail', params: { productId: product.productId } }"
      >
        <div class="product-card__imgwrap">
          <img
            class="product-card__img"
            :src="productImage(product)"
            :alt="product.productName"
            loading="lazy"
          />
        </div>
        <div class="product-card__body">
          <h2 class="product-card__name">{{ product.productName }}</h2>
          <p class="product-card__unit">{{ product.unitPricingMeasure }}</p>
          <div class="product-card__footer">
            <p class="product-card__price">{{ formatPrice(product.retailPrice) }}</p>
            <span class="product-card__cta">查看商品 →</span>
          </div>
        </div>
      </RouterLink>
    </section>

    <!-- 無限捲動：底部提示 + 哨兵（哨兵滑進視窗就自動載下一批） -->
    <div v-if="!loading && !error && products.length > 0" class="scroll-foot">
      <p v-if="loadingMore" class="state">載入更多…</p>
      <p v-else-if="!hasMore" class="state">已經到底囉 🌾</p>
      <div ref="sentinel" class="sentinel" aria-hidden="true"></div>
    </div>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1280px;
  margin: 0 auto;
}

/* ---------- 開頭介紹 ---------- */
.hero {
  text-align: center;
  margin-bottom: 32px;
}
.hero h1 {
  font-size: 28px;
  color: var(--ink);
  margin: 0 0 8px;
}
.hero p {
  color: var(--muted);
  margin: 0;
}

/* ---------- 載入 / 錯誤 / 空狀態 ---------- */
.state {
  text-align: center;
  color: var(--muted);
  padding: 40px 0;
}

/* ---------- 無限捲動底部 ---------- */
.scroll-foot {
  padding: 8px 0 4px;
}
/* 哨兵：看不見的偵測點，滑到它就載下一批 */
.sentinel {
  height: 1px;
}
.state--error button {
  margin-top: 12px;
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}

/* ---------- 商品格線：一行 5 個 ---------- */
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr); /* 固定一行 5 個 */
  gap: 22px;
}
/* 螢幕變窄時自動減少每行數量，避免卡片太擠 */
@media (max-width: 1080px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 860px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ---------- 單張商品卡（整張是連結） ---------- */
.product-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-hover);
  border-color: var(--leaf-soft);
}
.product-card__imgwrap {
  overflow: hidden;
  background: var(--line);
}
.product-card__img {
  width: 100%;
  height: 170px;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}
.product-card:hover .product-card__img {
  transform: scale(1.06);
}
.product-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.product-card__name {
  font-size: 17px;
  color: var(--ink);
  margin: 0;
  transition: color 0.18s ease;
}
.product-card:hover .product-card__name {
  color: var(--leaf-dark);
}
.product-card__unit {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
  flex: 1;
}
.product-card__footer {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-top: 6px;
}
.product-card__price {
  font-size: 18px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0;
}
.product-card__cta {
  font-size: 13px;
  color: var(--muted);
  white-space: nowrap;
  opacity: 0;
  transform: translateX(-4px);
  transition: opacity 0.2s ease, transform 0.2s ease, color 0.2s ease;
}
.product-card:hover .product-card__cta {
  opacity: 1;
  transform: translateX(0);
  color: var(--leaf);
}
</style>
