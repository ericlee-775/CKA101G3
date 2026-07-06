<script setup>
// 商品詳情頁（不分層：資料抓取、狀態、畫面全寫在這一個元件裡，用原生 fetch）。
// 路由：/products/:productId（見 router/index.js）。點商品卡片會帶著 productId 進來。
//
// 這頁要打的後端 API（都在 /api/products，且 GET 全部 permitAll，不用登入）：
//   1) 商品詳情：GET /api/products/{productId}            → ProductDetailDTO（名稱/價格/描述/分類…）
//   2) 圖片 id 清單：GET /api/products/photo/{productId}   → List<Integer>（沒圖回 404）
//   3) 單張圖片：GET /api/products/photo/image/{imageId}  → 圖片二進位（直接塞給 <img src>）
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 「暫無圖片」佔位圖（放在 src/assets，Vite 打包會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

const route = useRoute()
const router = useRouter()

// 商品詳情（ProductDetailDTO）。還沒抓到前是 null。
const product = ref(null)
// 圖片 id 清單（來自 /photo/{productId}）。
const imageIds = ref([])
// 目前放大顯示的主圖 id（點縮圖會換）。
const activeImageId = ref(null)

const loading = ref(true)
const error = ref('')

const FALLBACK_IMAGE = noImage

// 由圖片 id 組出後端圖片網址。dev 走 Vite proxy，部署後同源，相對路徑 /api 都能用。
function imageSrc(imageId) {
  return `/api/products/photo/image/${imageId}`
}

// 主圖網址：有圖就顯示目前選中的那張，沒圖用佔位圖。
const mainImageSrc = computed(() =>
  activeImageId.value != null ? imageSrc(activeImageId.value) : FALLBACK_IMAGE
)

// 把價格顯示成「NT$ 120」。null/undefined 就顯示 '—'。
function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// 抓商品詳情 + 圖片清單。route 參數變動時也會重抓（見下方 watch）。
async function loadProduct() {
  const id = route.params.productId
  loading.value = true
  error.value = ''
  product.value = null
  imageIds.value = []
  activeImageId.value = null

  try {
    // 詳情：查無會回 404。
    const res = await fetch(`/api/products/${id}`)
    if (!res.ok) {
      throw new Error(res.status === 404 ? '找不到這個商品' : `伺服器回應 ${res.status}`)
    }
    product.value = await res.json()

    // 圖片 id 清單：沒圖會回 404，這裡不當成錯誤，讓畫面顯示佔位圖即可。
    const imgRes = await fetch(`/api/products/photo/${id}`)
    if (imgRes.ok) {
      imageIds.value = await imgRes.json()
      activeImageId.value = imageIds.value[0] ?? null
    }
  } catch (e) {
    error.value = e.message || '無法載入商品，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// 點縮圖 → 換主圖。
function selectImage(imageId) {
  activeImageId.value = imageId
}

// 圖片載入失敗（例如後端該 id 抓不到）→ 換成佔位圖。
function onImageError(e) {
  e.target.src = FALLBACK_IMAGE
}

// 回商品列表。
function goBack() {
  router.push({ name: 'products' })
}

onMounted(loadProduct)
// 在詳情頁之間切換（/products/1 → /products/2）時，元件不會重建，靠 watch 重抓。
watch(() => route.params.productId, loadProduct)
</script>

<template>
  <main class="page">
    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <div class="state__actions">
        <button type="button" @click="loadProduct">重新載入</button>
        <button type="button" class="ghost" @click="goBack">回商品列表</button>
      </div>
    </div>

    <!-- 商品詳情 -->
    <article v-else-if="product" class="detail">
      <!-- 左側：圖片區（主圖 + 縮圖列） -->
      <section class="gallery">
        <img
          class="gallery__main"
          :src="mainImageSrc"
          :alt="product.productName"
          @error="onImageError"
        />
        <div v-if="imageIds.length > 1" class="gallery__thumbs">
          <button
            v-for="id in imageIds"
            :key="id"
            type="button"
            class="thumb"
            :class="{ 'thumb--active': id === activeImageId }"
            @click="selectImage(id)"
          >
            <img :src="imageSrc(id)" :alt="product.productName" @error="onImageError" />
          </button>
        </div>
      </section>

      <!-- 右側：文字資訊 -->
      <section class="info">
        <p v-if="product.subCatClassName" class="info__cat">
          {{ product.subCatClassName }}
        </p>
        <h1 class="info__name">{{ product.productName }}</h1>

        <div class="info__prices">
          <p class="info__price">
            {{ formatPrice(product.retailPrice) }}
            <span v-if="product.unitPricingMeasure" class="info__unit">
              / {{ product.unitPricingMeasure }}
            </span>
          </p>
          <p v-if="product.isGroupBuy && product.groupPrice != null" class="info__group">
            團購價 {{ formatPrice(product.groupPrice) }}
          </p>
        </div>

        <div v-if="product.isGroupBuy" class="badge">可團購</div>

        <div class="info__desc">
          <h2>商品描述</h2>
          <p v-if="product.description">{{ product.description }}</p>
          <p v-else class="info__desc--empty">此商品尚無描述。</p>
        </div>

        <button type="button" class="back-link" @click="goBack">← 回商品列表</button>
      </section>
    </article>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
}

/* ---------- 載入 / 錯誤狀態 ---------- */
.state {
  text-align: center;
  color: var(--muted);
  padding: 60px 0;
}
.state__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 12px;
}
.state button {
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}
.state button.ghost {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--ink);
}

/* ---------- 詳情主體：左圖右字 ---------- */
.detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 40px;
  align-items: start;
}
@media (max-width: 720px) {
  .detail {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

/* ---------- 圖片區 ---------- */
.gallery__main {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 16px;
  background: var(--line);
  display: block;
}
.gallery__thumbs {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.thumb {
  width: 64px;
  height: 64px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: var(--line);
}
.thumb--active {
  border-color: var(--leaf);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* ---------- 文字資訊 ---------- */
.info__cat {
  color: var(--muted);
  font-size: 13px;
  margin: 0 0 6px;
}
.info__name {
  font-size: 26px;
  color: var(--ink);
  margin: 0 0 16px;
}
.info__prices {
  margin-bottom: 16px;
}
.info__price {
  font-size: 26px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0;
}
.info__unit {
  font-size: 14px;
  font-weight: 400;
  color: var(--muted);
}
.info__group {
  margin: 6px 0 0;
  color: #c2410c;
  font-weight: 600;
}
.badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 13px;
  margin-bottom: 20px;
}
.info__desc {
  border-top: 1px solid var(--line);
  padding-top: 20px;
}
.info__desc h2 {
  font-size: 16px;
  color: var(--ink);
  margin: 0 0 8px;
}
.info__desc p {
  color: var(--ink);
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
}
.info__desc--empty {
  color: var(--muted);
}
.back-link {
  margin-top: 28px;
  background: none;
  border: none;
  color: var(--leaf-dark);
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}
.back-link:hover {
  text-decoration: underline;
}
</style>
