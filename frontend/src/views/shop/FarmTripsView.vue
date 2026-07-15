<script setup>
import { ref, onMounted, computed } from 'vue'

// 目前登入者（還沒接登入，先手動指定，查我的報名用它）
const userId = ref(1)

// ---- 活動列表 ----
const trips = ref([])
const listLoading = ref(true)
const listError = ref('')
function hideImg(e) { e.target.style.display = 'none' }

// ---- 我的報名 ----
const myOrders = ref([])
const myOrdersMsg = ref('')

// 活動類型 enum 轉中文
const TYPE_LABEL = { FARM_EXPERIENCE: '農場體驗營', FIELD_VISIT: '產地參訪' }
function typeLabel(t) { return TYPE_LABEL[t] || t || '' }

// ---- 子類別篩選 ----
const activeType = ref(null)   // null = 全部
const TYPE_TABS = [
  { value: null,              label: '全部',     icon: '🌿' },
  { value: 'FARM_EXPERIENCE', label: '農場體驗營', icon: '🏕️' },
  { value: 'FIELD_VISIT',     label: '產地參訪',   icon: '🚜' },
]
const filteredTrips = computed(() =>
  activeType.value === null
    ? trips.value
    : trips.value.filter(t => t.farmTripType === activeType.value)
)

function formatPrice(p) {
  if (p == null) return '—'
  return `NT$ ${Number(p).toLocaleString('zh-TW')}`
}

// 把星數轉成 ★☆ 字串
function stars(n) {
  const s = Math.max(0, Math.min(5, Number(n) || 0))
  return '★'.repeat(s) + '☆'.repeat(5 - s)
}

// ========== 活動列表 ==========
async function loadTrips() {
  listLoading.value = true
  listError.value = ''
  try {
    const res = await fetch('/api/farm-trips')
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    trips.value = await res.json()
  } catch (e) {
    listError.value = e.message || '無法載入活動，請稍後再試。'
  } finally {
    listLoading.value = false
  }
}

// ========== 我的報名 ==========
async function loadMyOrders() {
  myOrdersMsg.value = ''
  try {
    const res = await fetch(`/api/farm-trips/orders/mine?userId=${userId.value}`)
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    myOrders.value = await res.json()
    if (myOrders.value.length === 0) myOrdersMsg.value = '目前沒有報名紀錄。'
  } catch (e) {
    myOrdersMsg.value = '查詢失敗：' + (e.message || '請稍後再試')
  }
}

async function cancelOrder(orderId) {
  try {
    const res = await fetch(`/api/farm-trips/orders/${orderId}/cancel`, { method: 'PUT' })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    loadMyOrders()
  } catch (e) {
    myOrdersMsg.value = '取消失敗：' + (e.message || '請稍後再試')
  }
}

onMounted(loadTrips)
</script>

<template>
  <main class="page">
    <div class="topbar">
      <h1>🚜 農遊體驗</h1>
    </div>

    <!-- ============ 活動列表 ============ -->
    <section>
      <!-- 頁面說明 -->
      <div class="page-note">
        <p>
          本頁彙整各地小農自行舉辦的農遊體驗活動，讓你走進產地、親手參與農事。
          頁面上顯示的價格皆為小農提供的<strong>參考價</strong>，實際費用以活動當天為準。
        </p>
        <p>
          你可以直接在本頁線上<strong>預約</strong>想參加的場次，
          預約時<strong>無需事先付款</strong>；待體驗活動當天到現場後，再將費用直接付給小農即可。
        </p>
      </div>

      <!-- 子類別篩選 -->
      <div class="type-tabs">
        <button
          v-for="tab in TYPE_TABS"
          :key="tab.label"
          class="type-tab"
          :class="{ active: activeType === tab.value }"
          @click="activeType = tab.value"
        >
          <span class="type-icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <!-- 子類別篩選 -->
      <p v-if="listLoading">載入中…</p>
      <p v-else-if="listError" class="error">{{ listError }}</p>
      <p v-else-if="trips.length === 0">目前沒有上架中的活動。</p>

      <p v-else-if="filteredTrips.length === 0" class="muted">這個類別目前沒有活動。</p>

      <div v-else class="grid">
        <router-link
          v-for="t in filteredTrips"
          :key="t.farmTripId"
          class="card"
          :to="{ name: 'farm-trip-detail', params: { farmTripId: t.farmTripId } }"
        >
          <img class="thumb" :src="`/api/farm-trips/${t.farmTripId}/image`" alt="" @error="hideImg" />
          <span class="badge">{{ typeLabel(t.farmTripType) }}</span>
          <h3>{{ t.farmTripTitle }}</h3>
          <p class="farm-name" v-if="t.farmName">🏡 {{ t.farmName }}</p>
          <p class="muted">📍 {{ t.location }}</p>
          <p class="star">{{ stars(t.starNumbers) }}</p>
          <p class="price">參考價 {{ formatPrice(t.referPrice) }}</p>
        </router-link>
      </div>

      <!-- 我的報名 -->
      <section class="my-orders">
        <h2>我的報名</h2>
        <button class="btn" @click="loadMyOrders">查詢我的報名</button>
        <p v-if="myOrdersMsg" class="muted">{{ myOrdersMsg }}</p>
        <ul v-if="myOrders.length" class="order-list">
          <li v-for="o in myOrders" :key="o.farmTripOrderId">
            <span>訂單 {{ o.farmTripOrderBookingNo }}｜{{ o.numPeople }} 人｜{{ o.orderStatus }}</span>
            <button
              v-if="o.orderStatus === 'CONFIRMED'"
              class="btn-outline"
              @click="cancelOrder(o.farmTripOrderId)"
            >取消</button>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>

<style scoped>

.farm-name { color: var(--leaf-dark); font-weight: 600; margin: 2px 0; }

.page-note {
  background: var(--leaf-soft); border-left: 4px solid var(--leaf);
  border-radius: 12px; padding: 16px 20px; margin-top: 20px;
  line-height: 1.8; color: var(--ink-soft);
}
.page-note p { margin: 4px 0; }
.page-note strong { color: var(--leaf-dark); }
.type-tabs { display: flex; gap: 12px; margin-top: 20px; flex-wrap: wrap; }
.type-tab {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border: 1px solid var(--line); border-radius: 999px;
  padding: 8px 18px; cursor: pointer; font-size: 15px; color: var(--ink); transition: all .15s;
}
.type-tab:hover { border-color: var(--leaf); }
.type-tab.active { background: var(--leaf); color: #fff; border-color: var(--leaf); }
.type-icon { font-size: 18px; }

.page { padding: 32px clamp(18px, 4vw, 56px); color: var(--ink); }
.topbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
h1 { color: var(--ink); }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; margin-top: 20px; }
.card {
  display: block; text-decoration: none; color: inherit;
  background: #fff; border: 1px solid var(--line); border-radius: 14px; padding: 18px;
  box-shadow: var(--shadow); cursor: pointer; transition: transform .15s, box-shadow .15s;
}
.card:hover { transform: translateY(-3px); box-shadow: var(--shadow-hover); }
.card h3 { margin: 8px 0 4px; color: var(--ink); }
.thumb {
  width: 100%; height: 140px; object-fit: cover;
  border-radius: 10px; margin-bottom: 10px; display: block;
}

.badge {
  display: inline-block; background: var(--leaf-soft); color: var(--leaf-dark);
  font-size: 13px; padding: 2px 10px; border-radius: 999px;
}
.muted { color: var(--muted); }
.star { color: #e6a700; letter-spacing: 2px; }
.price { color: var(--leaf-dark); font-weight: 600; }
.error { color: #c0392b; }

.btn {
  background: var(--leaf); color: #fff; border: none; padding: 8px 16px;
  border-radius: 10px; cursor: pointer; font-size: 14px;
}
.btn:hover { background: var(--leaf-dark); }
.btn-outline {
  background: #fff; color: var(--leaf-dark); border: 1px solid var(--leaf);
  padding: 6px 14px; border-radius: 10px; cursor: pointer; font-size: 14px; margin: 8px 0;
}

.my-orders { margin-top: 40px; border-top: 1px solid var(--line); padding-top: 20px; }
.order-list { list-style: none; padding: 0; }
.order-list li {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-bottom: 1px solid var(--line); gap: 12px;
}
</style>
