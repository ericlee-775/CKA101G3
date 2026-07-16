<script setup>
// 「團購」頁面：串接 GET /api/groupBuy/consumer/list?type=xxx（PublicGroupBuyController，公開不用登入）
// 畫面呈現方式比照 ProductsView.vue：loading / error / empty 三態 + 卡片格線；
// 上方多一排頁籤（總覽/可發起/開團中），切換頁籤即用不同 type 重新打 API。
//
// 注意：後端 type=available 現在改成查「所有支援團購的商品」（ProductRepository.findGroupBuyProducts()），
// 不再是查既有的團購紀錄，所以這批資料的 groupBuyId / status / targetAmount / 時間 / 取貨地點全部是 null，
// 只有 productId / productName / groupPrice / unitPricingMeasure / description 等商品本身的欄位有值。
// 「總覽」頁籤現在要同時看到開團中 + 可發起，但後端沒有一支 API 一次給兩種，所以這裡改成前端併兩支 API：
//   type=all（既有團購紀錄，pending+open）+ type=available（可發起的商品）。
import { ref, onMounted, onUnmounted } from 'vue'
import groupBuyApi from '@/api/groupBuy'
import groupBuyFavoriteApi from '@/api/groupBuyFavorite'
import authStore from '@/stores/auth'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
import { usePagination } from '@/composables/usePagination'
import Pagination from '@/components/Pagination.vue'
// 「暫無圖片」佔位圖（放在 src/assets，Vite 打包會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

// 三個篩選頁籤：總覽（開團中+可發起都給）/ 可發起（沒有 groupBuyId 的商品）/ 開團中（status=open）
const TABS = [
  { key: 'all', label: '總覽' },
  { key: 'available', label: '可發起' },
  { key: 'open', label: '開團中' },
]
const activeTab = ref('all')

// 各頁籤沒有資料時顯示的文字
const EMPTY_TEXT = {
  all: '目前沒有進行中的團購。',
  available: '目前沒有可發起的商品。',
  open: '目前沒有開團中的團購。',
}

// 團購清單。一開始是空陣列，等 API 回來再填進去。
// 元素形狀有兩種（見上面的說明）：有 groupBuyId 的既有團購 / 沒有 groupBuyId 的可發起商品。
const groupBuys = ref([])
// 載入狀態：true 時畫面顯示「載入中…」。
const loading = ref(true)
// 錯誤訊息：抓資料失敗時放錯誤內容，畫面就顯示錯誤區塊。
const error = ref('')

const { page, totalPages, pageItems: pagedGroupBuys } = usePagination(groupBuys, 12)

// 跟後端要團購清單，依目前選中的頁籤帶 type 參數。
// 「總覽」要把 type=all（既有團購）跟 type=available（可發起商品）兩批資料合併顯示。
async function loadGroupBuys() {
  loading.value = true
  error.value = ''
  try {
    if (activeTab.value === 'all') {
      const [existing, available] = await Promise.all([
        groupBuyApi.list('all'),
        groupBuyApi.list('available'),
      ])
      groupBuys.value = [...existing, ...available]
    } else {
      groupBuys.value = await groupBuyApi.list(activeTab.value)
    }
    // 清單拿到後，接著把每筆團購對應商品的圖片也抓回來（沿用商品圖片端點）。
    loadImages()
  } catch (e) {
    error.value = e.message || '無法載入團購清單，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// 卡片狀態標籤：可發起商品沒有 status（null），用固定的「可發起」標籤；其餘照原本的狀態對照表。
function cardStatus(gb) {
  return gb.groupBuyId == null
    ? { text: '可發起', className: 'status--pending' }
    : groupBuyStatusInfo(gb.status)
}

// 切換頁籤 → 重新打 API 拿該頁籤的清單。
function selectTab(key) {
  if (activeTab.value === key) return
  activeTab.value = key
  loadGroupBuys()
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

// ========== 收藏愛心（只有 status=open 的團購能收藏，後端 groupBuyFavorite() 只收 open 狀態）==========
// POST/DELETE /api/member/groupBuyLists/{groupBuyId}；清單本身（GET）在會員中心「我的收藏」頁用，
// 這裡只需要知道「我收藏了哪些 id」，用來決定愛心是空心還是實心。
const favoriteIds = ref(new Set())

async function loadFavorites() {
  if (!authStore.isMember) return
  try {
    const list = await groupBuyFavoriteApi.list()
    favoriteIds.value = new Set(list.map((w) => w.groupBuyId))
  } catch {
    // 收藏清單載入失敗不影響團購列表本身，安靜忽略即可。
  }
}
function isFavorited(gb) {
  return favoriteIds.value.has(gb.groupBuyId)
}

const favoriteBusyIds = ref(new Set())
async function toggleFavorite(gb) {
  // 卡片點擊本來會被 RouterLink 導去詳情頁，這個按鈕要單獨處理，不能讓導頁一起發生。
  if (!authStore.isMember) {
    alert('請先登入會員')
    return
  }
  if (favoriteBusyIds.value.has(gb.groupBuyId)) return

  const busy = new Set(favoriteBusyIds.value)
  busy.add(gb.groupBuyId)
  favoriteBusyIds.value = busy
  try {
    if (isFavorited(gb)) {
      await groupBuyFavoriteApi.remove(gb.groupBuyId)
      const next = new Set(favoriteIds.value)
      next.delete(gb.groupBuyId)
      favoriteIds.value = next
    } else {
      await groupBuyFavoriteApi.add(gb.groupBuyId)
      const next = new Set(favoriteIds.value)
      next.add(gb.groupBuyId)
      favoriteIds.value = next
    }
  } catch (e) {
    alert(e.message || '操作失敗，請稍後再試')
  } finally {
    const done = new Set(favoriteBusyIds.value)
    done.delete(gb.groupBuyId)
    favoriteBusyIds.value = done
  }
}

// 元件掛載到畫面後就去抓資料。
onMounted(() => {
  loadGroupBuys()
  loadFavorites()
})
</script>

<template>
  <main class="page">
    <section class="hero">
      <h1>🛒 團購專區</h1>
      <p>揪團一起買，達標即成團，用更優惠的價格享受產地好物。</p>
    </section>

    <!-- 篩選頁籤：總覽 / 可發起 / 開團中 -->
    <nav class="tabs">
      <button
        v-for="tab in TABS"
        :key="tab.key"
        type="button"
        class="tabs__btn"
        :class="{ 'tabs__btn--active': activeTab === tab.key }"
        @click="selectTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗：顯示錯誤訊息與重試按鈕 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <button type="button" @click="loadGroupBuys">重新載入</button>
    </div>

    <!-- 成功但沒有資料 -->
    <p v-else-if="groupBuys.length === 0" class="state">{{ EMPTY_TEXT[activeTab] }}</p>

    <!-- 團購格線 -->
    <template v-else>
    <section class="groupbuy-grid">
      <article
        v-for="gb in pagedGroupBuys"
        :key="gb.groupBuyId ?? `product-${gb.productId}`"
        class="groupbuy-card"
      >
        <!--
          有 groupBuyId → 已經有團購紀錄，進 /group-buys/:groupBuyId（GroupBuyDetailDTO 現在自己帶 productId，不用再靠 query 傳）
          沒有 groupBuyId → 只是「可發起」的商品，進 /group-buys/product/:productId（走商品資料，不是團購資料）
        -->
        <RouterLink
          class="groupbuy-card__link"
          :to="gb.groupBuyId != null
            ? { name: 'group-buy-detail', params: { groupBuyId: gb.groupBuyId } }
            : { name: 'group-buy-host', params: { productId: gb.productId } }"
        >
          <div class="groupbuy-card__img-wrap">
            <img
              class="groupbuy-card__img"
              :src="groupBuyImage(gb)"
              :alt="gb.productName"
              loading="lazy"
            />
            <span class="groupbuy-card__status" :class="cardStatus(gb).className">
              {{ cardStatus(gb).text }}
            </span>
            <!-- 收藏愛心：只有開團中的團購能收藏（後端規定），可發起的商品沒有這顆心 -->
            <button
              v-if="gb.status === 'open'"
              type="button"
              class="groupbuy-card__heart"
              :class="{ 'groupbuy-card__heart--active': isFavorited(gb) }"
              :disabled="favoriteBusyIds.has(gb.groupBuyId)"
              :aria-label="isFavorited(gb) ? '取消收藏' : '收藏這個團購'"
              @click.stop.prevent="toggleFavorite(gb)"
            >
              {{ isFavorited(gb) ? '♥' : '♡' }}
            </button>
          </div>
          <div class="groupbuy-card__body">
            <h2 class="groupbuy-card__name">{{ gb.productName }}</h2>
            <p v-if="gb.subCatClassName" class="groupbuy-card__unit">{{ gb.subCatClassName }}</p>
            <p v-if="gb.unitPricingMeasure" class="groupbuy-card__unit">{{ gb.unitPricingMeasure }}</p>
            <p class="groupbuy-card__price">{{ formatPrice(gb.groupPrice) }}</p>
            <template v-if="gb.groupBuyId != null">
              <p class="groupbuy-card__meta">達標金額：{{ formatPrice(gb.targetAmount) }}</p>
              <p class="groupbuy-card__meta">截止日期：{{ formatDate(gb.ddlDatetime) }}</p>
              <p v-if="gb.pickupAddress" class="groupbuy-card__meta">取貨地點：{{ gb.pickupAddress }}</p>
            </template>
            <!-- 可發起商品還沒有目標數量/截止日期這些資料，改顯示商品描述 -->
            <p v-else-if="gb.description" class="groupbuy-card__meta">{{ gb.description }}</p>
          </div>
        </RouterLink>
      </article>
    </section>
    <Pagination v-model:page="page" :total-pages="totalPages" />
    </template>
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

/* ---------- 篩選頁籤 ---------- */
.tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 28px;
}
.tabs__btn {
  padding: 8px 22px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-soft);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.tabs__btn:hover {
  border-color: var(--leaf);
}
.tabs__btn--active {
  background: var(--leaf);
  border-color: var(--leaf);
  color: #fff;
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

/* 收藏愛心：右上角，空心/實心切換 */
.groupbuy-card__heart {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #b03434;
  font-size: 17px;
  line-height: 1;
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  transition: transform 0.15s ease, background 0.15s ease;
}
.groupbuy-card__heart:hover:not(:disabled) {
  transform: scale(1.1);
  background: #fff;
}
.groupbuy-card__heart:disabled {
  opacity: 0.6;
  cursor: default;
}
.groupbuy-card__heart--active {
  color: #fff;
  background: #e0435b;
}

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
