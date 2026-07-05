<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
// 「暫無圖片」佔位圖（放在 src/assets，打包後 Vite 會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

// 商品清單。一開始是空陣列，等 API 回來再填進去。
const products = ref([])
// 載入狀態：true 時畫面顯示「載入中…」。
const loading = ref(true)
// 錯誤訊息：抓資料失敗時放錯誤內容，畫面就顯示錯誤區塊。
const error = ref('')

// 跟後端要商品資料。
// dev 模式下 /api 會被 Vite proxy 轉發到 http://localhost:8080（見 vite.config.js）。
async function loadProducts() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/products')
    // fetch 不會因為 404/500 而 throw，要自己檢查 res.ok。
    if (!res.ok) {
      throw new Error(`伺服器回應 ${res.status}`)
    }
    products.value = await res.json()
    // 商品清單拿到後，接著把每個商品的圖片也用 fetch 抓回來。
    loadImages()
  } catch (e) {
    error.value = e.message || '無法載入商品，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// 把 retailPrice 顯示成「NT$ 120」這種格式。
function formatPrice(price) {
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// 後端該商品沒有圖（回 404）或抓圖失敗時用的「暫無圖片」佔位圖。
const FALLBACK_IMAGE = noImage

// 每個商品的圖片網址，用 productId 當 key。
// 值是 fetch 回來的圖片轉成的 blob 網址（URL.createObjectURL）。
const imageUrls = ref({})

// 用 fetch 把每個商品的圖片抓回來。
// 後端用獨立端點以二進位回傳圖片：GET /api/products/{productId}/image。
// dev 模式下 /api 會被 Vite proxy 轉到 http://localhost:8080；
// 打包進 Spring Boot 後是同源（同一個服務），相對路徑 /api 一樣能用。
function loadImages() {
  for (const product of products.value) {
    fetchImage(product.productId)
  }
}

async function fetchImage(id) {
  try {
    const res = await fetch(`/api/products/${id}/image`)
    // 沒圖會回 404，res.ok 為 false，就讓畫面用預設圖。
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[id] = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗就維持預設圖，不影響其他商品。
  }
}

// 取得要顯示的圖片網址：有抓到就用 blob 網址，否則用預設圖。
function productImage(product) {
  return imageUrls.value[product.productId] || FALLBACK_IMAGE
}

// 元件卸載時，把建立的 blob 網址釋放掉，避免記憶體洩漏。
function revokeImageUrls() {
  for (const url of Object.values(imageUrls.value)) {
    URL.revokeObjectURL(url)
  }
}
onUnmounted(revokeImageUrls)

// 記錄每個商品按鈕的狀態，用 productId 當 key。
// 值是 'adding'（加入中）/ 'done'（成功）/ 'error'（失敗）。
const cartStatus = ref({})

// 加入購物車。
// 後端還沒寫，這裡先用合理的預設：POST /api/cart，body 送 { productId, quantity }。
// 之後後端網址或欄位若不同，改這個函式即可，其他都不用動。
async function addToCart(product) {
  const id = product.productId
  // 加入中就不再重複送。
  if (cartStatus.value[id] === 'adding') return

  cartStatus.value[id] = 'adding'
  try {
    const res = await fetch('/api/cart', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      // 靠 session(cookie) 辨識使用者，所以要帶上 cookie。
      credentials: 'include',
      body: JSON.stringify({ productId: id, quantity: 1 }),
    })
    if (!res.ok) {
      throw new Error(`伺服器回應 ${res.status}`)
    }
    cartStatus.value[id] = 'done'
  } catch (e) {
    cartStatus.value[id] = 'error'
  } finally {
    // 2 秒後把狀態清掉，按鈕文字回到「加入購物車」。
    setTimeout(() => {
      delete cartStatus.value[id]
    }, 2000)
  }
}

// 依狀態決定按鈕文字。
function cartBtnText(id) {
  switch (cartStatus.value[id]) {
    case 'adding': return '加入中…'
    case 'done': return '已加入 ✓'
    case 'error': return '失敗，再試一次'
    default: return '加入購物車'
  }
}

// 元件掛載到畫面後就去抓資料。
onMounted(loadProducts)
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
      <button type="button" @click="loadProducts">重新載入</button>
    </div>

    <!-- 成功但沒有資料 -->
    <p v-else-if="products.length === 0" class="state">目前沒有商品。</p>

    <!-- 商品格線 -->
    <section v-else class="product-grid">
      <article
        v-for="product in products"
        :key="product.productId"
        class="product-card"
      >
        <img
          class="product-card__img"
          :src="productImage(product)"
          :alt="product.productName"
          loading="lazy"
        />
        <div class="product-card__body">
          <h2 class="product-card__name">{{ product.productName }}</h2>
          <p class="product-card__unit">{{ product.unitPricingMeasure }}</p>
          <p class="product-card__price">{{ formatPrice(product.retailPrice) }}</p>
          <button
            class="product-card__btn"
            type="button"
            :disabled="cartStatus[product.productId] === 'adding'"
            @click="addToCart(product)"
          >
            {{ cartBtnText(product.productId) }}
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
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
.state--error button {
  margin-top: 12px;
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}

/* ---------- 商品格線 ---------- */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

/* ---------- 單張商品卡 ---------- */
.product-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
.product-card__img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  background: var(--line);
}
.product-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.product-card__name {
  font-size: 17px;
  color: var(--ink);
  margin: 0;
}
.product-card__unit {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
}
.product-card__price {
  font-size: 18px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0;
  flex: 1;
}
.product-card__btn {
  margin-top: 4px;
  padding: 9px 0;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.product-card__btn:hover {
  background: var(--leaf-dark);
}
.product-card__btn:disabled {
  opacity: 0.6;
  cursor: default;
}
</style>
