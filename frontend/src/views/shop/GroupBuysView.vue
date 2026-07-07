<script setup>
// 「團購」頁面：串接 GET /api/groupBuy/all（PublicGroupBuyController，公開不用登入）
// 畫面呈現方式比照 ProductsView.vue：loading / error / empty 三態 + 卡片格線。
import { ref, onMounted, onUnmounted } from 'vue'
import groupBuyApi from '@/api/groupBuy'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
// 「暫無圖片」佔位圖（放在 src/assets，Vite 打包會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

// 團購清單（ProductGroupBuyDTO[]）。一開始是空陣列，等 API 回來再填進去。
const groupBuys = ref([])
// 載入狀態：true 時畫面顯示「載入中…」。
const loading = ref(true)
// 錯誤訊息：抓資料失敗時放錯誤內容，畫面就顯示錯誤區塊。
const error = ref('')

// 跟後端要團購清單。
async function loadGroupBuys() {
  loading.value = true
  error.value = ''
  try {
    groupBuys.value = await groupBuyApi.list()
    // 清單拿到後，接著把每筆團購對應商品的圖片也抓回來（沿用商品圖片端點）。
    loadImages()
  } catch (e) {
    error.value = e.message || '無法載入團購清單，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// 把價格顯示成「NT$ 220」。null/undefined 就顯示 '—'。
function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// 把時間戳顯示成「2026/03/12」。
function formatDate(datetime) {
  if (!datetime) return '—'
  return new Date(datetime).toLocaleDateString('zh-TW')
}

// 沒圖或抓圖失敗時用的「暫無圖片」佔位圖。
const FALLBACK_IMAGE = noImage

// 每筆團購對應商品的圖片網址，用 productId 當 key。
// 值是 fetch 回來的圖片轉成的 blob 網址（URL.createObjectURL）。
const imageUrls = ref({})

// 用 fetch 把每筆團購對應商品的圖片抓回來。
// 後端用獨立端點以二進位回傳圖片：GET /api/products/{productId}/image（與商品頁共用）。
function loadImages() {
  for (const gb of groupBuys.value) {
    fetchImage(gb.productId)
  }
}

async function fetchImage(productId) {
  if (productId == null || imageUrls.value[productId]) return
  try {
    const res = await fetch(`/api/products/${productId}/image`)
    // 沒圖會回 404，res.ok 為 false，就讓畫面用預設圖。
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[productId] = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗就維持預設圖，不影響其他卡片。
  }
}

// 取得要顯示的圖片網址：有抓到就用 blob 網址，否則用預設圖。
function groupBuyImage(gb) {
  return imageUrls.value[gb.productId] || FALLBACK_IMAGE
}

// 元件卸載時，把建立的 blob 網址釋放掉，避免記憶體洩漏。
function revokeImageUrls() {
  for (const url of Object.values(imageUrls.value)) {
    URL.revokeObjectURL(url)
  }
}
onUnmounted(revokeImageUrls)

// 元件掛載到畫面後就去抓資料。
onMounted(loadGroupBuys)
</script>

<template>
  <main class="page">
    <section class="hero">
      <h1>🛒 全部團購</h1>
      <p>揪團一起買，達到目標數量就能用團購價入手當季好物。</p>
    </section>

    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗：顯示錯誤訊息與重試按鈕 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <button type="button" @click="loadGroupBuys">重新載入</button>
    </div>

    <!-- 成功但沒有資料 -->
    <p v-else-if="groupBuys.length === 0" class="state">目前沒有進行中的團購。</p>

    <!-- 團購格線 -->
    <section v-else class="groupbuy-grid">
      <article
        v-for="gb in groupBuys"
        :key="gb.groupBuyId"
        class="groupbuy-card"
      >
        <!-- 點卡片 → 進團購詳情頁（/group-buys/:groupBuyId）；帶 productId 讓詳情頁能抓商品圖 -->
        <RouterLink
          class="groupbuy-card__link"
          :to="{ name: 'group-buy-detail', params: { groupBuyId: gb.groupBuyId }, query: { productId: gb.productId } }"
        >
          <div class="groupbuy-card__img-wrap">
            <img
              class="groupbuy-card__img"
              :src="groupBuyImage(gb)"
              :alt="gb.productName"
              loading="lazy"
            />
            <span class="groupbuy-card__status" :class="groupBuyStatusInfo(gb.status).className">
              {{ groupBuyStatusInfo(gb.status).text }}
            </span>
          </div>
          <div class="groupbuy-card__body">
            <h2 class="groupbuy-card__name">{{ gb.productName }}</h2>
            <p v-if="gb.unitPricingMeasure" class="groupbuy-card__unit">{{ gb.unitPricingMeasure }}</p>
            <p class="groupbuy-card__price">{{ formatPrice(gb.groupPrice) }}</p>
            <p class="groupbuy-card__meta">目標數量：{{ gb.targetAmount ?? '—' }}</p>
            <p class="groupbuy-card__meta">截止日期：{{ formatDate(gb.ddlDatetime) }}</p>
            <p v-if="gb.pickupAddress" class="groupbuy-card__meta">取貨地點：{{ gb.pickupAddress }}</p>
          </div>
        </RouterLink>
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

/* ---------- 團購格線 ---------- */
.groupbuy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

/* ---------- 單張團購卡 ---------- */
.groupbuy-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.groupbuy-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}
.groupbuy-card__link {
  display: flex;
  flex-direction: column;
  flex: 1;
  color: inherit;
  text-decoration: none;
}
.groupbuy-card__img-wrap {
  position: relative;
}
.groupbuy-card__img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  background: var(--line);
}
.groupbuy-card__status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: #fff7ed;
  color: #c2410c;
}
.status--open { background: #ecfdf3; color: #15803d; }
.status--success { background: #eff6ff; color: #1d4ed8; }
.status--failed { background: #f3f4f6; color: #6b7280; }
.status--cancelled { background: #f3f4f6; color: #6b7280; }
.status--pending { background: #fff7ed; color: #c2410c; }

.groupbuy-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.groupbuy-card__name {
  font-size: 17px;
  color: var(--ink);
  margin: 0;
}
.groupbuy-card__unit {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
}
.groupbuy-card__price {
  font-size: 18px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0;
}
.groupbuy-card__meta {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
}
</style>
