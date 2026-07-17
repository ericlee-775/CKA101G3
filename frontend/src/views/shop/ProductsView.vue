<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 「暫無圖片」佔位圖（放在 src/assets，打包後 Vite 會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'
import authStore from '@/stores/auth' // 收藏功能：判斷使用者是否已登入、是不是會員身分
import { toast } from '@/composables/useToast'
import { requireMemberLogin } from '@/composables/useAuthGuard'
import { categoryIcon } from '@/constants/categoryIcons'

// 從網址帶進來的農場篩選：農場專頁點「查看全部商品」時會帶 ?farmerId=&farmName=。
// 有 farmerId → 只顯示該農場的商品；farmName 只是拿來顯示標題用。
const route = useRoute()
const router = useRouter()
const farmerId = computed(() => route.query.farmerId || '')
const farmName = computed(() => route.query.farmName || '')

const PAGE_SIZE = 12

// 商品清單（無限捲動：一批一批往後累加，不覆蓋）。
const products = ref([])
const loading = ref(true) // 首次載入（第 0 頁）
const loadingMore = ref(false) // 捲到底載下一批
const error = ref('')

// 分頁狀態（無限捲動）。
const nextPage = ref(0) // 下一個要載的頁碼（0 起算）
const hasMore = ref(true) // 還有沒有更多可載

// ---------- 複合查詢：篩選條件 ----------
// draft = 表單當下輸入值；active = 真正送出查詢用的值。
// 分兩份是因為：使用者邊打字不該邊查，要按「搜尋」才把 draft 套用成 active 並重載。
const draft = ref({ keyword: '', subCatClassId: '', minPrice: '', maxPrice: '' })
const active = ref({ keyword: '', subCatClassId: '', minPrice: '', maxPrice: '' })

// 分類選項（供下拉選單）。從 /api/products/categories 撈，依主分類分組。
const categories = ref([])
const groupedCategories = computed(() => {
  const groups = new Map() // 主分類名 → [{ id, name }]
  for (const c of categories.value) {
    if (!groups.has(c.productMainCatName)) groups.set(c.productMainCatName, [])
    groups.get(c.productMainCatName).push({ id: c.subCatClassId, name: c.subCatClassName })
  }
  return groups
})

// 是否正在套用篩選（給空狀態文案用：有篩選卻沒結果 vs 本來就沒商品）。
const hasActiveFilter = computed(() =>
  Object.values(active.value).some((v) => String(v).trim() !== ''),
)

// 依 active 篩選 + 頁碼組出查詢字串；只帶「有填」的條件（空的不帶→後端 IS NULL 略過）。
function buildQuery(page) {
  const params = new URLSearchParams()
  params.set('page', page)
  params.set('size', PAGE_SIZE)
  const { keyword, subCatClassId, minPrice, maxPrice } = active.value
  if (keyword.trim()) params.set('keyword', keyword.trim())
  if (subCatClassId) params.set('subCatClassId', subCatClassId)
  if (minPrice !== '') params.set('minPrice', minPrice)
  if (maxPrice !== '') params.set('maxPrice', maxPrice)
  if (farmerId.value) params.set('farmerId', farmerId.value) // 農場篩選：只帶有值時才送
  return params.toString()
}

// 按下「搜尋」：把 draft 套成 active，回到第一頁重載。
function applyFilters() {
  active.value = { ...draft.value }
  reload()
}

// 清除所有條件並重載。
function clearFilters() {
  draft.value = { keyword: '', subCatClassId: '', minPrice: '', maxPrice: '' }
  active.value = { ...draft.value }
  reload()
}

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
    // 統一走 /search：無條件時等同「全部商品」，帶條件時就是複合查詢。
    const res = await fetch(`/api/products/search?${buildQuery(page)}`)
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

// ---------- 收藏 ----------
// 存放「目前登入會員已經收藏過的商品 id」，用 Set 方便快速判斷 has(id)。
// 訪客／小農身分不會有值，愛心一律顯示未收藏。
const favoriteIds = ref(new Set())

// 收藏功能：登入的會員才需要知道「我收藏了哪些商品」，用來決定每張卡片愛心的樣子。
// 重複利用「我的收藏」頁在用的同一支 GET /api/member/wishlists，不用多開一支後端 API。
async function loadFavoriteIds() {
  await authStore.ensureHydrated() // 確保登入狀態已經跟後端確認過，不是猜的
  if (!authStore.isLoggedIn || !authStore.isMember) return
  try {
    const res = await fetch('/api/member/wishlists', { credentials: 'include' })
    if (!res.ok) return
    const list = await res.json()
    favoriteIds.value = new Set(list.map((p) => p.productId))
  } catch {
    // 收藏狀態載入失敗不影響商品瀏覽，愛心先當作都沒收藏
  }
}

// 收藏功能：點愛心切換收藏狀態；已收藏就取消(DELETE)，沒收藏就加入(POST)。
async function toggleFavorite(product) {
  // 未登入會員：共用守門彈窗（useAuthGuard），按「前往登入」自動導登入頁
  if (!(await requireMemberLogin(router, '登入會員後即可收藏喜歡的商品，隨時回來查看。'))) return
  const id = product.productId
  const alreadyFavorited = favoriteIds.value.has(id)
  try {
    const res = await fetch(`/api/member/wishlists/${id}`, {
      method: alreadyFavorited ? 'DELETE' : 'POST',
      credentials: 'include',
    })
    if (!res.ok) throw new Error(`伺服器回應${res.status}`)
    if (alreadyFavorited) favoriteIds.value.delete(id)
    else favoriteIds.value.add(id)
  } catch (e) {
    toast(alreadyFavorited ? '取消收藏失敗，請稍後再試' : '加入收藏失敗，請稍後再試', 'error')
  }
}

// ---------- 無限捲動：IntersectionObserver 盯住底部哨兵 ----------
// 只觀察一次；使用者往下捲、哨兵進入視窗就載下一批（loadNext 內建鎖，不會重複）。
const sentinel = ref(null)
let observer = null

// 載入分類選項（失敗不擋商品瀏覽，只是下拉會是空的）。
async function loadCategories() {
  try {
    const res = await fetch('/api/products/categories')
    if (!res.ok) return
    categories.value = await res.json()
  } catch {
    // 忽略：分類載入失敗不影響商品清單。
  }
}

onMounted(async () => {
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) loadNext()
    },
    { rootMargin: '200px' }, // 距底部 200px 就先載，捲動較順
  )
  loadCategories() // 不 await：分類與商品並行載入
  loadFavoriteIds() // 不 await：收藏狀態與商品並行載入，不阻塞商品顯示
  await loadNext() // 先載第 0 頁
  await nextTick() // 等哨兵渲染出來
  if (sentinel.value) observer.observe(sentinel.value)
})

// 網址上的 farmerId 變了（換農場、或清掉篩選回全部商品）就整個重載。
watch(farmerId, () => reload())

onUnmounted(() => {
  if (observer) observer.disconnect()
  revokeImageUrls()
})
</script>

<template>
  <main class="page">
    <section class="hero">
      <template v-if="farmerId">
        <h1>🏡 {{ farmName || '這個農場' }} 的商品</h1>
        <p>
          目前只顯示這個農場上架的商品。
          <RouterLink :to="{ name: 'products' }" class="hero__all">← 看全部農場的商品</RouterLink>
        </p>
      </template>
      <template v-else>
        <h1>🥬 全部商品</h1>
        <p>嚴選合作農場的當季好物，直接從產地送到你家。</p>
      </template>
    </section>

    <!-- 複合查詢篩選列：關鍵字 + 分類 + 價格區間。按 Enter 或「搜尋」才送出。 -->
    <form class="filters" @submit.prevent="applyFilters">
      <input
        v-model="draft.keyword"
        class="filters__input"
        type="search"
        placeholder="搜尋商品名稱…"
      />

      <select v-model="draft.subCatClassId" class="filters__input">
        <option value="">全部分類</option>
        <!-- 主分類群組標題掛 icon（emoji），對照表在 constants/categoryIcons.js -->
        <optgroup
          v-for="[mainName, subs] in groupedCategories"
          :key="mainName"
          :label="`${categoryIcon(mainName)} ${mainName}`"
        >
          <option v-for="sub in subs" :key="sub.id" :value="sub.id">{{ sub.name }}</option>
        </optgroup>
      </select>

      <div class="filters__price">
        <input
          v-model="draft.minPrice"
          class="filters__input filters__input--price"
          type="number"
          min="0"
          placeholder="最低價"
        />
        <span class="filters__dash">—</span>
        <input
          v-model="draft.maxPrice"
          class="filters__input filters__input--price"
          type="number"
          min="0"
          placeholder="最高價"
        />
      </div>

      <button type="submit" class="filters__btn filters__btn--go">搜尋</button>
      <button
        v-if="hasActiveFilter"
        type="button"
        class="filters__btn filters__btn--clear"
        @click="clearFilters"
      >
        清除
      </button>
    </form>

    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗：顯示錯誤訊息與重試按鈕 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <button type="button" @click="reload">重新載入</button>
    </div>

    <!-- 成功但沒有資料：區分「篩選查無」與「本來就沒商品」 -->
    <div v-else-if="products.length === 0" class="state">
      <template v-if="hasActiveFilter">
        <p>😢 找不到符合條件的商品。</p>
        <button type="button" class="filters__btn filters__btn--clear" @click="clearFilters">
          清除篩選
        </button>
      </template>
      <p v-else>目前沒有商品。</p>
    </div>

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
          <!-- 收藏愛心：@click.stop.prevent 擋住冒泡，不然按愛心會連帶觸發整張卡片的連結跳轉 -->
          <button
            type="button"
            class="product-card__fav"
            :class="{ 'product-card__fav--active': favoriteIds.has(product.productId) }"
            :aria-label="favoriteIds.has(product.productId) ? '取消收藏' : '加入收藏'"
            @click.stop.prevent="toggleFavorite(product)"
          >
            {{ favoriteIds.has(product.productId) ? '♥' : '♡' }}
          </button>
        </div>
        <div class="product-card__body">
          <h2 class="product-card__name">{{ product.productName }}</h2>
          <p v-if="product.farmName" class="product-card__farm">🏡 {{ product.farmName }}</p>
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
.hero__all {
  margin-left: 8px;
  color: var(--leaf-dark);
  font-weight: 700;
  text-decoration: none;
}
.hero__all:hover {
  text-decoration: underline;
}

/* ---------- 篩選列 ---------- */
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  justify-content: center;
  margin-bottom: 28px;
}
.filters__input {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink);
  font-size: 14px;
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.filters__input:focus {
  border-color: var(--leaf);
  box-shadow: 0 0 0 3px var(--leaf-soft);
}
.filters__input[type='search'] {
  min-width: 220px;
}
.filters__price {
  display: flex;
  align-items: center;
  gap: 6px;
}
.filters__input--price {
  width: 96px;
}
.filters__dash {
  color: var(--muted);
}
.filters__btn {
  height: 42px;
  padding: 0 22px;
  border: none;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.18s ease, background 0.18s ease;
}
.filters__btn--go {
  background: var(--leaf);
  color: #fff;
}
.filters__btn--go:hover {
  opacity: 0.9;
}
.filters__btn--clear {
  background: transparent;
  color: var(--muted);
  border: 1px solid var(--line);
}
.filters__btn--clear:hover {
  border-color: var(--leaf-soft);
  color: var(--ink);
}
@media (max-width: 600px) {
  .filters__input[type='search'] {
    flex: 1 1 100%;
    min-width: 0;
  }
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
  position: relative; /* 收藏愛心用 absolute 定位，要靠這層當基準 */
  overflow: hidden;
  background: var(--line);
}

/* 收藏愛心：浮在圖片右上角，白色半透明圓底，未收藏是灰色空心 */
.product-card__fav {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  color: var(--ink-soft);
  font-size: 17px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  transition: color 0.18s ease, transform 0.15s ease, background 0.18s ease;
}
.product-card__fav:hover {
  transform: scale(1.12);
}
/* 已收藏：實心愛心，紅色 */
.product-card__fav--active {
  color: #e0455f;
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
.product-card__farm {
  font-size: 13px;
  color: var(--leaf-dark);
  margin: 0;
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
