<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import cartStore from '@/stores/cart'
import { confirm } from '@/composables/useConfirm'
// 「暫無圖片」佔位圖（放在 src/assets，打包後 Vite 會處理成正確路徑）。
import noImage from '@/assets/no-image.svg'

const FALLBACK_IMAGE = noImage
const router = useRouter()

// 直接讀 store 的購物車內容與統計數字
const items = computed(() => cartStore.state.items)
const totalCount = computed(() => cartStore.totalCount.value)
const totalPrice = computed(() => cartStore.totalPrice.value)

// 把價格顯示成「NT$ 120」這種格式
function formatPrice(price) {
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// ---------- 商品圖片：跟 ProductsView 一樣用 fetch 抓回來 ----------
// 每個商品的圖片網址，用 productId 當 key，值是 blob 網址。
const imageUrls = ref({})

async function fetchImage(id) {
  try {
    const res = await fetch(`/api/products/${id}/image`)
    // 沒圖會回 404，就讓畫面用預設圖。
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[id] = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗維持預設圖，不影響其他商品。
  }
}

function loadImages() {
  for (const item of items.value) {
    if (!imageUrls.value[item.productId]) fetchImage(item.productId)
  }
}

function productImage(id) {
  return imageUrls.value[id] || FALLBACK_IMAGE
}

// ---------- 數量與移除 ----------
function inc(item) {
  cartStore.setQuantity(item.productId, item.quantity + 1)
}
function dec(item) {
  cartStore.setQuantity(item.productId, item.quantity - 1)
}
function onQtyInput(item, event) {
  cartStore.setQuantity(item.productId, event.target.value)
}

async function remove(item) {
  const ok = await confirm({
    title: '移除商品',
    message: `確定要把「${item.productName}」從購物車移除嗎？`,
    confirmText: '移除',
    danger: true,
  })
  if (ok) cartStore.removeItem(item.productId)
}

async function clearCart() {
  const ok = await confirm({
    title: '清空購物車',
    message: '確定要清空整個購物車嗎？',
    confirmText: '清空',
    danger: true,
  })
  if (ok) cartStore.clear()
}

// 後端 API 串接結帳預覽頁
function checkout() {
  if (items.value.length === 0){
    window.alert('購物車是空的，先去逛逛全部商品!')
    return
  }
  router.push({ name: 'checkout' })
}

// 進頁面時把目前購物車裡商品的圖片抓回來
onMounted(loadImages)
// 離開時釋放建立的 blob 網址，避免記憶體洩漏
onUnmounted(() => {
  for (const url of Object.values(imageUrls.value)) URL.revokeObjectURL(url)
})
</script>

<template>
  <main class="page">
    <section class="hero">
      <h1>🥬 購物車</h1>
    </section>

    <!-- 空購物車 -->
    <div v-if="items.length === 0" class="empty">
      <p>你的購物車是空的。</p>
      <router-link class="empty__link" to="/products">去逛逛全部商品 →</router-link>
    </div>

    <!-- 有商品：左邊明細 + 右邊摘要 -->
    <div v-else class="cart-layout">
      <section class="cart-items">
        <article
          v-for="item in items"
          :key="item.productId"
          class="cart-item"
        >
          <img
            class="cart-item__img"
            :src="productImage(item.productId)"
            :alt="item.productName"
          />

          <div class="cart-item__info">
            <router-link
              class="cart-item__name"
              :to="{ name: 'product-detail', params: { productId: item.productId } }"
            >
              {{ item.productName }}
            </router-link>
            <p class="cart-item__unit">{{ item.unitPricingMeasure }}</p>
            <p class="cart-item__price">{{ formatPrice(item.retailPrice) }}</p>
          </div>

          <!-- 數量調整 -->
          <div class="qty">
            <button
              class="qty__btn"
              type="button"
              aria-label="減少數量"
              :disabled="item.quantity <= 1"
              @click="dec(item)"
            >−</button>
            <input
              class="qty__input"
              type="number"
              min="1"
              :value="item.quantity"
              @input="onQtyInput(item, $event)"
            />
            <button
              class="qty__btn"
              type="button"
              aria-label="增加數量"
              @click="inc(item)"
            >＋</button>
          </div>

          <!-- 小計 -->
          <p class="cart-item__subtotal">
            {{ formatPrice(item.retailPrice * item.quantity) }}
          </p>

          <!-- 移除 -->
          <button
            class="cart-item__remove"
            type="button"
            aria-label="移除商品"
            @click="remove(item)"
          >✕</button>
        </article>
      </section>

      <!-- 訂單摘要 -->
      <aside class="summary">
        <h2 class="summary__title">訂單摘要</h2>
        <div class="summary__row">
          <span>商品數量</span>
          <span>{{ totalCount }} 件</span>
        </div>
        <div class="summary__row summary__row--total">
          <span>總計</span>
          <span>{{ formatPrice(totalPrice) }}</span>
        </div>
        <button class="summary__checkout" type="button" @click="checkout">
          前往結帳
        </button>
        <button class="summary__clear" type="button" @click="clearCart">
          清空購物車
        </button>
        <div class="summary__shipping">
          <p class="summary__shipping-title">🚚 運費說明</p>
          <div class="summary__shipping-row">
            <span>本島地區（未滿 $800）</span>
            <span>$100元</span>
          </div>
          <div class="summary__shipping-row">
            <span>離島地區（未滿 $2500）</span>
            <span>$300元</span>
          </div>
        </div>
      </aside>
    </div>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
}

/* ---------- 開頭 ---------- */
.hero {
  text-align: center;
  margin-bottom: 32px;
}
.hero h1 {
  font-size: 28px;
  color: var(--ink);
  margin: 0;
}

/* ---------- 空購物車 ---------- */
.empty {
  text-align: center;
  color: var(--muted);
  padding: 48px 0;
}
.empty__link {
  display: inline-block;
  margin-top: 12px;
  color: var(--leaf-dark);
  font-weight: 600;
  text-decoration: none;
}
.empty__link:hover {
  text-decoration: underline;
}

/* ---------- 版面：明細 + 摘要 ---------- */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
  align-items: start;
}

/* ---------- 商品明細 ---------- */
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.cart-item {
  display: grid;
  grid-template-columns: 88px 1fr auto auto auto;
  align-items: center;
  gap: 16px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px;
}
.cart-item__img {
  width: 88px;
  height: 88px;
  object-fit: cover;
  border-radius: 10px;
  background: var(--line);
}
.cart-item__info {
  min-width: 0;
}
.cart-item__name {
  display: inline-block;
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
  text-decoration: none;
  margin-bottom: 4px;
}
.cart-item__name:hover {
  color: var(--leaf-dark);
}
.cart-item__unit {
  font-size: 13px;
  color: var(--muted);
  margin: 0 0 2px;
}
.cart-item__price {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

/* 數量調整器 */
.qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 999px;
  overflow: hidden;
}
.qty__btn {
  width: 34px;
  height: 34px;
  border: none;
  background: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.qty__btn:hover:not(:disabled) {
  background: var(--leaf-soft);
}
.qty__btn:disabled {
  opacity: 0.4;
  cursor: default;
}
.qty__input {
  width: 46px;
  height: 34px;
  border: none;
  border-left: 1px solid var(--line);
  border-right: 1px solid var(--line);
  text-align: center;
  font-size: 15px;
  -moz-appearance: textfield;
}
.qty__input::-webkit-outer-spin-button,
.qty__input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.cart-item__subtotal {
  font-size: 16px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0;
  min-width: 96px;
  text-align: right;
}
.cart-item__remove {
  width: 32px;
  height: 32px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.cart-item__remove:hover {
  border-color: #c0392b;
  color: #c0392b;
}

/* ---------- 訂單摘要 ---------- */
.summary {
  position: sticky;
  top: 90px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 20px;
}
.summary__title {
  font-size: 18px;
  color: var(--ink);
  margin: 0 0 16px;
}
.summary__row {
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 14px;
  margin-bottom: 12px;
}
.summary__row--total {
  padding-top: 12px;
  border-top: 1px solid var(--line);
  color: var(--ink);
  font-size: 18px;
  font-weight: 700;
}
.summary__row--total span:last-child {
  color: var(--leaf-dark);
}
.summary__checkout {
  width: 100%;
  margin-top: 8px;
  padding: 12px 0;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease;
}
.summary__checkout:hover {
  background: var(--leaf-dark);
}
.summary__clear {
  width: 100%;
  margin-top: 10px;
  padding: 10px 0;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.summary__clear:hover {
  border-color: #c0392b;
  color: #c0392b;
}
.summary__shipping {
  margin-top: 16px;
  padding: 12px 14px;
  background: #f7faf5;
  border: 1px dashed var(--line);
  border-radius: 10px;
}
.summary__shipping-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}
.summary__shipping-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.6;
}
.summary__shipping-row span:last-child {
  font-weight: 600;
  color: var(--leaf-dark);
  white-space: nowrap;
}

/* ---------- 響應式 ---------- */
@media (max-width: 820px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
  .cart-item {
    grid-template-columns: 72px 1fr auto;
    grid-template-areas:
      'img info remove'
      'img qty  subtotal';
    row-gap: 10px;
  }
  .cart-item__img {
    grid-area: img;
    width: 72px;
    height: 72px;
  }
  .cart-item__info {
    grid-area: info;
  }
  .cart-item__remove {
    grid-area: remove;
    justify-self: end;
  }
  .qty {
    grid-area: qty;
  }
  .cart-item__subtotal {
    grid-area: subtotal;
    align-self: center;
  }
  .summary {
    position: static;
  }
}
</style>
