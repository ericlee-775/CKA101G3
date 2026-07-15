<script setup>
// 會員中心「我的團購」：已成團的訂單清單（獨立元件，內容都寫在這裡）
// 資料來源：GET /api/member/groupBuy/mySuccessOrders
// 回傳欄位（GroupBuyOrderDTO）：orderId / groupBuyId / groupPrice / shippingAddress / shippedStatus /
//   shippedAt / createdAt / orderStatus / paidStatus / hostUserName / hostUserPhone / hostUserEmail /
//   buyQty（這位會員自己訂購的數量）/ paidAmount（這位會員自己已付的金額）—
//   數量跟金額用 buyQty/paidAmount，不是 totalQuantity/totalAmount（那是整團的總數，這頁只看自己的消費紀錄）。
//
// 後端 ShippedStatus / OrderStatus / PaidStatus 三個 enum 用「全大寫」序列化
//（跟 GroupBuyStatus / RequestStatus 的全小寫不同），對照表要對到大寫的代碼，
// 不然畫面上會直接顯示英文代碼原文（例如以前這裡曾經對錯大小寫，畫面上會看到 "PENDING"）。
//
// 商品圖片：GroupBuyOrderDTO 現在直接帶 productId，打 GET /api/products/{productId}/image 就好。
//
// 確認收貨：PATCH /api/member/groupBuy/orders/{orderId}/confirm-receipt，只有「這筆團購的發起人（團購主）」
// 能呼叫，後端會擋非本人。DTO 沒有 hostUserId，只有 hostUserEmail，前端用信箱比對目前登入的會員是不是本人
// 來決定要不要顯示按鈕；後端仍會再做一次真正的權限檢查，這裡只是先把不相關的人畫面上就不要有按鈕。
// 確認過一次（orderStatus 變成 COMPLETED）就不能再按。實測過：只有本人能按、按過不能重按、非本人會被後端擋。
import { ref, onMounted, computed } from 'vue'
import memberGroupBuyApi from '@/api/memberGroupBuy'
import authStore from '@/stores/auth'
import { shippedStatusInfo, orderStatusInfo } from '@/utils/orderStatus'
import { usePagination } from '@/composables/usePagination'
import Pagination from '@/components/Pagination.vue'
import noImage from '@/assets/no-image.svg'

// 通知父層（MemberGroupBuysView）目前有幾筆，顯示在 tab 的數字小徽章
const emit = defineEmits(['count'])

const orders = ref([])
const loading = ref(true)
const error = ref('')

const { page, totalPages, pageItems: pagedOrders } = usePagination(orders, 10)

// orderStatus.js 的 className 是共用團購狀態那套的 class 名稱，這裡另外對照一份 tone
// （跟本檔案既有的 .badge--xxx 顏色系統一致），維持這個元件原本的視覺風格。
const TONE_MAP = { 'status--pending': 'amber', 'status--success': 'green' }
const toneOf = (info) => TONE_MAP[info.className] || 'gray'

function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

const formatMoney = (n) => (n == null ? '—' : `NT$ ${Number(n).toLocaleString('zh-TW')}`)

// 目前登入會員是不是這筆訂單的團購主（用信箱比對，DTO 沒有 id 可以比）。
function isHost(order) {
  const me = authStore.state.user
  return !!me?.email && me.email === order.hostUserEmail
}

const FALLBACK_IMAGE = noImage
const imageUrls = ref({})

async function loadImages() {
  for (const order of orders.value) {
    fetchImage(order.productId)
  }
}

async function fetchImage(productId) {
  if (productId == null || imageUrls.value[productId]) return
  try {
    const res = await fetch(`/api/products/${productId}/image`)
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[productId] = URL.createObjectURL(blob)
  } catch {
    // 沒圖就維持預設圖，不影響其他卡片。
  }
}
function orderImage(order) {
  return imageUrls.value[order.productId] || FALLBACK_IMAGE
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    orders.value = (await memberGroupBuyApi.mySuccessOrders()) || []
    emit('count', orders.value.length)
    loadImages()
  } catch (e) {
    error.value = e.message || '載入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// 確認收貨送出中的 orderId 集合，避免重複點擊；送出中/送出後都直接刷新該筆的狀態。
const confirmingIds = ref(new Set())
function isConfirming(id) {
  return confirmingIds.value.has(id)
}
async function confirmReceipt(order) {
  const next = new Set(confirmingIds.value)
  next.add(order.orderId)
  confirmingIds.value = next
  try {
    await memberGroupBuyApi.confirmReceipt(order.orderId)
    await load()
  } catch (e) {
    error.value = e.message || '確認收貨失敗，請稍後再試'
  } finally {
    const done = new Set(confirmingIds.value)
    done.delete(order.orderId)
    confirmingIds.value = done
  }
}

onMounted(load)
</script>

<template>
  <div class="order-list">
    <!-- 載入中：骨架卡片 -->
    <template v-if="loading">
      <div v-for="i in 2" :key="i" class="order-card skeleton">
        <div class="sk-line sk-w40"></div>
        <div class="sk-line sk-w70"></div>
        <div class="sk-line sk-w55"></div>
      </div>
    </template>

    <!-- 載入失敗 -->
    <div v-else-if="error" class="state-box">
      <span class="state-icon">😵</span>
      <p>{{ error }}</p>
      <button class="state-btn" type="button" @click="load">重新載入</button>
    </div>

    <!-- 還沒有成團的訂單 -->
    <div v-else-if="orders.length === 0" class="state-box">
      <span class="state-icon">🧾</span>
      <p>還沒有已成團的訂單</p>
      <router-link class="state-btn" to="/group-buys">去參加團購 →</router-link>
    </div>

    <!-- 訂單卡片 -->
    <template v-else>
      <article v-for="order in pagedOrders" :key="order.orderId" class="order-card">
        <div class="order-img-wrap">
          <img class="order-img" :src="orderImage(order)" alt="" loading="lazy" />
        </div>
        <div class="order-body">
          <header class="order-head">
            <div class="order-id-group">
              <span class="order-id">訂單編號 #{{ order.orderId }}</span>
              <time class="order-date">{{ formatDate(order.createdAt) }}</time>
            </div>
            <div class="order-badges">
              <!-- 只顯示出貨狀態（待出貨／已送達） -->
              <span class="badge" :class="`badge--${toneOf(shippedStatusInfo(order.shippedStatus))}`">
                {{ shippedStatusInfo(order.shippedStatus).text }}
              </span>
            </div>
          </header>

          <dl class="order-grid">
            <div class="order-cell">
              <dt>我訂購</dt>
              <dd>{{ order.buyQty ?? '—' }} 件</dd>
            </div>
            <div class="order-cell">
              <dt>成團價</dt>
              <dd>{{ formatMoney(order.groupPrice) }}</dd>
            </div>
            <div class="order-cell">
              <dt>我付的金額</dt>
              <dd class="order-money">{{ formatMoney(order.paidAmount) }}</dd>
            </div>
            <div class="order-cell">
              <dt>訂單狀態</dt>
              <dd>{{ orderStatusInfo(order.orderStatus).text }}</dd>
            </div>
            <div class="order-cell order-cell--wide">
              <dt>取貨地點</dt>
              <dd>📍 {{ order.shippingAddress || '—' }}</dd>
            </div>
          </dl>

          <!-- 團購主聯絡方式：配達確切時間請自行聯繫團購主，不是找小農 -->
          <div class="host-contact">
            <h4>團購主聯絡方式</h4>
            <p>{{ order.hostUserName || '—' }}</p>
            <p>📞 {{ order.hostUserPhone || '—' }}</p>
            <p>✉️ {{ order.hostUserEmail || '—' }}</p>
            <p class="host-contact__note">請自行聯繫團購主確認配達時間</p>
          </div>

          <!-- 確認收貨：只有團購主看得到按鈕，確認過一次就不能再按 -->
          <div v-if="isHost(order)" class="order-footer">
            <button
              v-if="order.orderStatus !== 'COMPLETED'"
              type="button"
              class="confirm-btn"
              :disabled="isConfirming(order.orderId)"
              @click="confirmReceipt(order)"
            >
              確認收貨
            </button>
            <span v-else class="confirmed-text">✓ 已確認收貨</span>
          </div>
        </div>
      </article>

      <Pagination v-model:page="page" :total-pages="totalPages" />
    </template>
  </div>
</template>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ===== 訂單卡片 ===== */
.order-card {
  display: flex;
  padding: 0;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}
.order-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}
.order-img-wrap {
  flex: 0 0 120px;
  background: var(--line);
}
.order-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.order-body {
  flex: 1;
  min-width: 0;
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 20px;
  background: var(--leaf-soft);
  border-bottom: 1px solid var(--line);
}
.order-id-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}
.order-id {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}
.order-date {
  font-size: 13px;
  color: var(--muted);
}
.order-badges {
  display: flex;
  gap: 6px;
}

/* 狀態徽章 */
.badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}
.badge--green { background: #e3f4e8; color: #2f6e46; }
.badge--blue  { background: #e5eefa; color: #2c5d9e; }
.badge--amber { background: #fdf1dc; color: #9a6b15; }
.badge--gray  { background: #eeeeea; color: #75806f; }

/* 欄位小格子 */
.order-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px 16px;
  margin: 0;
  padding: 16px 20px;
}
.order-cell dt {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 2px;
}
.order-cell dd {
  margin: 0;
  font-size: 14px;
  color: var(--ink-soft);
}
.order-cell--wide {
  grid-column: 1 / -1;
}
.order-money {
  font-weight: 700;
  font-size: 15px;
  color: var(--leaf-dark);
}

/* ===== 團購主聯絡方式 ===== */
.host-contact {
  margin: 0 20px 16px;
  padding: 14px 16px;
  background: var(--leaf-soft);
  border-radius: 12px;
}
.host-contact h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--leaf-dark);
}
.host-contact p {
  margin: 4px 0;
  font-size: 13px;
  color: var(--ink-soft);
}
.host-contact__note {
  margin-top: 8px;
  font-weight: 600;
  color: #c2410c;
}

/* ===== 底部：確認收貨 ===== */
.order-footer {
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 18px;
}
.confirm-btn {
  padding: 8px 20px;
  border-radius: 999px;
  border: none;
  background: var(--leaf);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease;
}
.confirm-btn:hover:not(:disabled) {
  background: var(--leaf-dark);
}
.confirm-btn:disabled {
  opacity: 0.6;
  cursor: default;
}
.confirmed-text {
  font-size: 13px;
  color: var(--leaf-dark);
  font-weight: 600;
}

@media (max-width: 560px) {
  .order-card {
    flex-direction: column;
  }
  .order-img-wrap {
    flex-basis: auto;
    height: 140px;
  }
  .order-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ===== 載入中 / 失敗 / 空狀態 ===== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 20px;
  background: #fff;
  border: 1px dashed var(--line);
  border-radius: 14px;
  text-align: center;
}
.state-icon { font-size: 38px; }
.state-box p {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
}
.state-btn {
  margin-top: 6px;
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid var(--leaf);
  background: #fff;
  color: var(--leaf);
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}
.state-btn:hover {
  background: var(--leaf);
  color: #fff;
}

/* 骨架屏 */
.skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px;
}
.sk-line {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg, #f0ede6 25%, #faf8f3 50%, #f0ede6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.sk-w40 { width: 40%; }
.sk-w55 { width: 55%; }
.sk-w70 { width: 70%; }
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
</style>
