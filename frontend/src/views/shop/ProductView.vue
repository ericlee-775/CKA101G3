<script setup>
// 商品詳情頁（不分層：資料抓取、狀態、畫面全寫在這一個元件裡，用原生 fetch）。
// 路由：/products/:productId（見 router/index.js）。點商品卡片會帶著 productId 進來。
//
// 這頁要打的後端 API（都在 /api/products，且 GET 全部 permitAll，不用登入）：
//   1) 商品詳情：GET /api/products/{productId}            → ProductDetailDTO（名稱/價格/描述/分類…）
//   2) 圖片 id 清單：GET /api/products/photo/{productId}   → List<Integer>（沒圖回 404）
//   3) 單張圖片：GET /api/products/photo/image/{imageId}  → 圖片二進位（直接塞給 <img src>）
//
// 加入購物車走 cartStore（訪客存 localStorage、登入後打 /api/shoppingcart），
// 全站唯一入口就在這頁：列表頁只負責導進來，數量在這裡選好再加入。
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import cartStore from '@/stores/cart'
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

// ---------- 數量選擇 ----------
const quantity = ref(1)
const MAX_QTY = 99

// 把任意輸入整理成 1〜99 的整數（打字打出 0、負數、小數、空字串都會被拉回合法值）。
function clampQty(v) {
  const n = Math.floor(Number(v))
  if (!Number.isFinite(n) || n < 1) return 1
  return Math.min(n, MAX_QTY)
}
function decreaseQty() {
  quantity.value = clampQty(quantity.value - 1)
}
function increaseQty() {
  quantity.value = clampQty(quantity.value + 1)
}
// input 失焦時才校正，讓使用者打字過程中不被打斷。
function onQtyBlur(e) {
  quantity.value = clampQty(e.target.value)
  // 校正後同步回 input（quantity 沒變時 Vue 不會重繪，要手動塞回去）。
  e.target.value = quantity.value
}

// 小計 = 單價 × 數量，即時顯示。
const subtotal = computed(() => {
  if (!product.value || product.value.retailPrice == null) return null
  return Number(product.value.retailPrice) * quantity.value
})

// ---------- 加入購物車 ----------
// 'idle' | 'adding' | 'done' | 'error'
const cartStatus = ref('idle')

const cartBtnText = computed(() => {
  switch (cartStatus.value) {
    case 'adding': return '加入中…'
    case 'done': return '已加入購物車 ✓'
    case 'error': return '加入失敗，再試一次'
    default: return '🥬 加入購物車'
  }
})

// 小烏龜把購物車載走的動畫開關:按下加入時播一次,1.5 秒後收工。
const towing = ref(false)
let towTimer = null

async function addToCart() {
  if (!product.value || cartStatus.value === 'adding') return
  cartStatus.value = 'adding'
  // 觸發小烏龜動畫(跟 API 無關,純視覺;連點時先清掉舊計時器)
  towing.value = true
  clearTimeout(towTimer)
  towTimer = setTimeout(() => {
    towing.value = false
  }, 1500)
  try {
    // 登入時 addItem 會打後端 API（async），訪客則寫 localStorage，這裡統一 await。
    await cartStore.addItem(product.value, quantity.value)
    cartStatus.value = 'done'
  } catch {
    cartStatus.value = 'error'
  } finally {
    // 2 秒後讓按鈕文字回到預設。
    setTimeout(() => {
      cartStatus.value = 'idle'
    }, 2000)
  }
}

// 前往購物車頁。
function goCart() {
  router.push({ name: 'cart' })
}

// 抓商品詳情 + 圖片清單。route 參數變動時也會重抓（見下方 watch）。
async function loadProduct() {
  const id = route.params.productId
  loading.value = true
  error.value = ''
  product.value = null
  imageIds.value = []
  activeImageId.value = null
  quantity.value = 1
  cartStatus.value = 'idle'

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
    <article v-else-if="product" class="detail-wrap">
      <!-- 麵包屑 -->
      <nav class="crumbs">
        <button type="button" class="crumbs__link" @click="goBack">全部商品</button>
        <span class="crumbs__sep">›</span>
        <span class="crumbs__here">{{ product.productName }}</span>
      </nav>

      <div class="detail">
        <!-- 左側：圖片區（主圖 + 縮圖列） -->
        <section class="gallery">
          <div class="gallery__frame">
            <img
              class="gallery__main"
              :src="mainImageSrc"
              :alt="product.productName"
              @error="onImageError"
            />
          </div>
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

        <!-- 右側：文字資訊 + 購買區 -->
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

          <!-- 購買區：選數量 → 加入購物車 -->
          <div class="buybox">
            <div class="buybox__row">
              <span class="buybox__label">數量</span>
              <div class="qty">
                <button
                  type="button"
                  class="qty__btn"
                  :disabled="quantity <= 1"
                  aria-label="減少數量"
                  @click="decreaseQty"
                >−</button>
                <input
                  class="qty__input"
                  type="number"
                  min="1"
                  :max="MAX_QTY"
                  :value="quantity"
                  aria-label="數量"
                  @blur="onQtyBlur"
                  @keyup.enter="onQtyBlur"
                />
                <button
                  type="button"
                  class="qty__btn"
                  :disabled="quantity >= MAX_QTY"
                  aria-label="增加數量"
                  @click="increaseQty"
                >+</button>
              </div>
              <span v-if="subtotal != null" class="buybox__subtotal">
                小計 <strong>{{ formatPrice(subtotal) }}</strong>
              </span>
            </div>

            <div class="buybox__actions">
              <button
                type="button"
                class="btn-cart"
                :class="{
                  'btn-cart--done': cartStatus === 'done',
                  'btn-cart--error': cartStatus === 'error',
                  'btn-cart--towing': towing,
                }"
                :disabled="cartStatus === 'adding'"
                @click="addToCart"
              >
                <span class="btn-cart__label">{{ cartBtnText }}</span>
                <!-- 小烏龜拉著木推車載蔬菜走(烏龜 emoji 面朝左,所以往左開) -->
                <span v-if="towing" class="tow" aria-hidden="true">
                  <span class="tow__turtle">🐢</span>
                  <span class="tow__wagon">
                    <span class="tow__veggies">🥬🥕🌽</span>
                    <span class="tow__bed"></span>
                    <span class="tow__wheel tow__wheel--front"></span>
                    <span class="tow__wheel tow__wheel--back"></span>
                  </span>
                </span>
              </button>
              <button
                v-if="cartStatus === 'done'"
                type="button"
                class="btn-gocart"
                @click="goCart"
              >
                前往結帳 →
              </button>
            </div>
          </div>

          <div class="info__desc">
            <h2>商品描述</h2>
            <p v-if="product.description">{{ product.description }}</p>
            <p v-else class="info__desc--empty">此商品尚無描述。</p>
          </div>

          <button type="button" class="back-link" @click="goBack">← 回商品列表</button>
        </section>
      </div>
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

/* ---------- 麵包屑 ---------- */
.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--muted);
}
.crumbs__link {
  background: none;
  border: none;
  padding: 0;
  font-size: 13px;
  color: var(--leaf-dark);
  cursor: pointer;
}
.crumbs__link:hover {
  text-decoration: underline;
}
.crumbs__here {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 320px;
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
.gallery {
  position: sticky;
  top: 96px;
}
@media (max-width: 720px) {
  .gallery {
    position: static;
  }
}
.gallery__frame {
  border-radius: 18px;
  overflow: hidden;
  background: var(--line);
  box-shadow: var(--shadow);
}
.gallery__main {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
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
  transition: border-color 0.15s ease, transform 0.15s ease;
}
.thumb:hover {
  transform: translateY(-2px);
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
  letter-spacing: 0.05em;
}
.info__name {
  font-size: 28px;
  color: var(--ink);
  margin: 0 0 16px;
  line-height: 1.3;
}
.info__prices {
  margin-bottom: 12px;
}
.info__price {
  font-size: 28px;
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
  border: 1px solid #fed7aa;
  color: #c2410c;
  font-size: 13px;
  margin-bottom: 20px;
}

/* ---------- 購買區（數量 + 加入購物車） ---------- */
.buybox {
  background: var(--leaf-soft);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 18px 20px;
  margin: 4px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.buybox__row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.buybox__label {
  font-size: 14px;
  color: var(--ink-soft);
}
.buybox__subtotal {
  margin-left: auto;
  font-size: 14px;
  color: var(--ink-soft);
}
.buybox__subtotal strong {
  font-size: 17px;
  color: var(--leaf-dark);
}

/* 數量選擇器：− [數字] + */
.qty {
  display: inline-flex;
  align-items: stretch;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 999px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(55, 45, 28, 0.06);
}
.qty__btn {
  width: 38px;
  border: none;
  background: transparent;
  color: var(--leaf-dark);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s ease;
}
.qty__btn:hover:not(:disabled) {
  background: var(--leaf-soft);
}
.qty__btn:disabled {
  color: var(--line);
  cursor: default;
}
.qty__input {
  width: 52px;
  padding: 9px 0;
  border: none;
  border-left: 1px solid var(--line);
  border-right: 1px solid var(--line);
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  outline: none;
  /* 拿掉 number input 預設的上下小箭頭，跟自訂的 −/+ 按鈕重複 */
  -moz-appearance: textfield;
  appearance: textfield;
}
.qty__input::-webkit-outer-spin-button,
.qty__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 加入購物車按鈕 */
.buybox__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.btn-cart {
  flex: 1;
  min-width: 180px;
  padding: 13px 24px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--leaf), var(--leaf-dark));
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(59, 138, 87, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease, filter 0.15s ease;
  /* 小烏龜動畫需要:烏龜以按鈕為座標系、走出按鈕範圍就被裁掉 */
  position: relative;
  overflow: hidden;
}

/* ---------- 小烏龜載走購物車動畫 ---------- */
/* 播動畫時按鈕文字先淡出,讓舞台乾淨 */
.btn-cart__label {
  display: inline-block;
  transition: opacity 0.2s ease;
}
.btn-cart--towing .btn-cart__label {
  opacity: 0;
}

/* 車隊(烏龜+購物車):從按鈕右緣外開進來,一路開出左緣 */
.tow {
  position: absolute;
  top: 50%;
  left: 100%; /* 起點:藏在右邊界外 */
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 20px;
  line-height: 1;
  pointer-events: none; /* 純裝飾,不擋按鈕點擊 */
  animation: tow-across 1.5s linear forwards;
}
/* 烏龜:一邊走一邊左右搖(走路的感覺) */
.tow__turtle {
  animation: tow-waddle 0.35s ease-in-out infinite;
}

/* ----- 小木推車(CSS 手工畫:木斗 + 輪子 + 拉桿,上面載蔬菜) ----- */
.tow__wagon {
  position: relative;
  width: 38px;
  height: 26px;
  animation: tow-bump 0.3s ease-in-out infinite alternate; /* 被拖著顛簸 */
}
/* 拉桿:從車頭斜斜接向前面的烏龜 */
.tow__wagon::before {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 10px;
  width: 11px;
  height: 2px;
  border-radius: 2px;
  background: #8a5a33;
  transform: rotate(16deg);
}
/* 蔬菜:從車斗探出頭(letter-spacing 負值讓三顆擠在一起) */
.tow__veggies {
  position: absolute;
  top: -9px;
  left: 3px;
  font-size: 12px;
  letter-spacing: -4px;
  line-height: 1;
}
/* 木製車斗:木頭色 + 直條木板紋 */
.tow__bed {
  position: absolute;
  left: 0;
  bottom: 5px;
  width: 38px;
  height: 13px;
  box-sizing: border-box;
  border: 1.5px solid #6f4a26;
  border-radius: 3px 3px 5px 5px;
  background: repeating-linear-gradient(
    90deg,
    #b07c46 0 7px,
    #9a6a3a 7px 8px
  ); /* 一條條的木板 */
}
/* 輪子:深木色圓 + 一根淺色輻條(有輻條,轉動才看得出來) */
.tow__wheel {
  position: absolute;
  bottom: 0;
  width: 10px;
  height: 10px;
  box-sizing: border-box;
  border-radius: 50%;
  background: #4a3421;
  border: 2px solid #2c1f12;
  animation: tow-wheel-roll 0.45s linear infinite;
}
.tow__wheel::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 1.5px;
  margin-top: -1px;
  background: #d8b98c; /* 輻條 */
}
.tow__wheel--front {
  left: 4px;
}
.tow__wheel--back {
  right: 4px;
}
@keyframes tow-wheel-roll {
  to {
    transform: rotate(-360deg); /* 往左走 → 輪子逆時針滾 */
  }
}

@keyframes tow-across {
  from {
    left: 100%;
  }
  to {
    left: -90px; /* 終點:整隊開出左邊界外 */
  }
}
@keyframes tow-waddle {
  0%,
  100% {
    transform: rotate(-6deg);
  }
  50% {
    transform: rotate(6deg) translateY(-2px);
  }
}
@keyframes tow-bump {
  from {
    transform: translateY(0) rotate(2deg);
  }
  to {
    transform: translateY(-3px) rotate(-4deg);
  }
}

/* 使用者系統設定「減少動態效果」時:不播動畫、文字照常顯示 */
@media (prefers-reduced-motion: reduce) {
  .tow {
    display: none;
  }
  .btn-cart--towing .btn-cart__label {
    opacity: 1;
  }
}
.btn-cart:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(59, 138, 87, 0.42);
  filter: brightness(1.05);
}
.btn-cart:active:not(:disabled) {
  transform: translateY(0);
}
.btn-cart:disabled {
  opacity: 0.7;
  cursor: default;
}
.btn-cart--done {
  background: var(--leaf-dark);
  box-shadow: none;
}
.btn-cart--error {
  background: #b91c1c;
  box-shadow: 0 6px 16px rgba(185, 28, 28, 0.3);
}
.btn-gocart {
  padding: 13px 22px;
  border: 1px solid var(--leaf);
  border-radius: 999px;
  background: #fff;
  color: var(--leaf-dark);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease;
}
.btn-gocart:hover {
  background: var(--leaf-soft);
}

/* ---------- 商品描述 ---------- */
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
