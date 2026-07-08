<script setup>
// 會員中心「我的團購」：已成團的訂單清單（獨立元件，內容都寫在這裡）
// 資料來源：GET /api/member/groupBuy/mySuccessOrders
// 回傳欄位（GroupBuyOrderDTO）：orderId / groupBuyId / totalQuantity / groupPrice / totalAmount /
//   shippingAddress / shippedStatus / shippedAt / trackingNum / createdAt / orderStatus / paidStatus …
import { ref, onMounted } from 'vue'
import memberGroupBuyApi from '@/api/memberGroupBuy'

// 通知父層（MemberGroupBuysView）目前有幾筆，顯示在 tab 的數字小徽章
const emit = defineEmits(['count'])

const orders = ref([])
const loading = ref(true)
const error = ref('')

// 後端三組 enum 都用英文代碼序列化，這裡對照中文與顏色（tone 對應 .badge--xxx）
const SHIPPED_MAP = {
  pending:   { label: '待出貨', tone: 'amber' },
  shipped:   { label: '已出貨', tone: 'blue' },
  delivered: { label: '已送達', tone: 'green' },
}
const ORDER_MAP = {
  pending:   { label: '等待收貨', tone: 'amber' },
  confirmed: { label: '確認收貨', tone: 'green' },
}
const badgeOf = (map, code) => map[code] || { label: code ?? '—', tone: 'gray' }

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

async function load() {
  loading.value = true
  error.value = ''
  try {
    orders.value = (await memberGroupBuyApi.mySuccessOrders()) || []
    emit('count', orders.value.length)
  } catch (e) {
    error.value = e.message || '載入失敗，請稍後再試'
  } finally {
    loading.value = false
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
    <article v-for="order in orders" :key="order.orderId" class="order-card">
      <header class="order-head">
        <div class="order-id-group">
          <span class="order-id">訂單編號 #{{ order.orderId }}</span>
          <time class="order-date">{{ formatDate(order.createdAt) }}</time>
        </div>
        <div class="order-badges">
          <span class="badge" :class="`badge--${badgeOf(SHIPPED_MAP, order.shippedStatus).tone}`">
            {{ badgeOf(SHIPPED_MAP, order.shippedStatus).label }}
          </span>
          <span class="badge" :class="`badge--${badgeOf(ORDER_MAP, order.orderStatus).tone}`">
            {{ badgeOf(ORDER_MAP, order.orderStatus).label }}
          </span>
        </div>
      </header>

      <dl class="order-grid">
        <div class="order-cell">
          <dt>數量</dt>
          <dd>{{ order.totalQuantity ?? '—' }} 件</dd>
        </div>
        <div class="order-cell">
          <dt>成團價</dt>
          <dd>{{ formatMoney(order.groupPrice) }}</dd>
        </div>
        <div class="order-cell">
          <dt>總金額</dt>
          <dd class="order-money">{{ formatMoney(order.totalAmount) }}</dd>
        </div>
        <div v-if="order.trackingNum" class="order-cell">
          <dt>物流編號</dt>
          <dd>{{ order.trackingNum }}</dd>
        </div>
        <div class="order-cell order-cell--wide">
          <dt>取貨地點</dt>
          <dd>📍 {{ order.shippingAddress || '—' }}</dd>
        </div>
      </dl>
    </article>
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

@media (max-width: 560px) {
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
