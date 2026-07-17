<script setup>
// 會員中心：我的體驗活動報名
// 後端端點（CustomerController，/api/farm-trips）：
//   GET  /orders/mine?userId={id}   → 我的報名清單
//   PUT  /orders/{orderId}/cancel   → 取消報名（只有 CONFIRMED 可取消）
//   POST /{farmTripId}/comments     → 評論（只有報名且活動已結束才可）
import { ref, onMounted } from 'vue'
import authStore from '@/stores/auth'

const userId = ref(authStore.state.user?.userId)

const myOrders = ref([])
const myOrdersMsg = ref('')

// 評論狀態
const reviewingId = ref(null)
const reviewForm = ref({ star: 5, content: '' })
const reviewMsg = ref('')

// 報名狀態中文對照
function statusLabel(s) {
  return { CONFIRMED: '預約成功', CANCELLED: '已取消', COMPLETED: '已完成' }[s] || s
}

// 活動日期時間：2026/04/10 09:00
function fmtDateTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (isNaN(d.getTime())) return '—'
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

async function loadMyOrders() {
  myOrdersMsg.value = ''
  if (!userId.value) {
    myOrdersMsg.value = '請先登入會員。'
    return
  }
  try {
    const res = await fetch(`/api/farm-trips/orders/mine?userId=${userId.value}`, {
      credentials: 'include',
    })
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    myOrders.value = await res.json()
    if (myOrders.value.length === 0) myOrdersMsg.value = '目前沒有報名紀錄。'
  } catch (e) {
    myOrdersMsg.value = '查詢失敗：' + (e.message || '請稍後再試')
  }
}

async function cancelOrder(orderId) {
  if (!confirm('確定取消報名嗎？')) return
  try {
    const res = await fetch(`/api/farm-trips/orders/${orderId}/cancel`, {
      method: 'PUT',
      credentials: 'include',
    })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    loadMyOrders()
  } catch (e) {
    myOrdersMsg.value = '取消失敗：' + (e.message || '請稍後再試')
  }
}

// 可評論條件：已確認的報名 + 活動結束時間已過
function canReview(o) {
  return o.orderStatus === 'CONFIRMED'
    && o.farmTripEnd
    && new Date(o.farmTripEnd).getTime() < Date.now()
}

function startReview(o) {
  reviewingId.value = o.farmTripOrderId
  reviewForm.value = { star: 5, content: '' }
  reviewMsg.value = ''
}
function cancelReview() {
  reviewingId.value = null
}

const detailId = ref(null)   // 正在看哪一筆的詳情
function toggleDetail(o) {
  detailId.value = detailId.value === o.farmTripOrderId ? null : o.farmTripOrderId
}

async function submitReview(o) {
  reviewMsg.value = ''
  if (!reviewForm.value.content.trim()) {
    reviewMsg.value = '請寫下評論內容。'
    return
  }
  try {
    const res = await fetch(`/api/farm-trips/${o.farmTripId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        userId: userId.value,
        star: reviewForm.value.star,
        content: reviewForm.value.content.trim(),
      }),
    })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    reviewingId.value = null
    reviewMsg.value = '感謝你的評論！'
  } catch (e) {
    reviewMsg.value = '送出失敗：' + (e.message || '請稍後再試')
  }
}

onMounted(loadMyOrders)
</script>

<template>
  <section class="my-orders">
    <h1>已報名活動</h1>
    <p v-if="myOrdersMsg" class="muted">{{ myOrdersMsg }}</p>
    <p v-if="reviewMsg" class="muted">{{ reviewMsg }}</p>

    <div v-if="myOrders.length" class="order-head">
      <span>訂單編號</span>
      <span>活動名稱</span>
      <span>活動開始時間</span>
      <span>人數</span>
      <span>報名狀態</span>
      <span></span>
    </div>

    <ul v-if="myOrders.length" class="order-list">
      <li v-for="o in myOrders" :key="o.farmTripOrderId">
        <div class="order-row">
          <RouterLink
            class="trip-link"
            :to="{ name: 'farm-trip-detail', params: { farmTripId: o.farmTripId } }"
          >{{ o.farmTripOrderBookingNo }}</RouterLink>
          <RouterLink
            class="trip-link"
            :to="{ name: 'farm-trip-detail', params: { farmTripId: o.farmTripId } }"
          >{{ o.farmTripTitle || '體驗活動' }}</RouterLink>
          <span>{{ fmtDateTime(o.farmTripStart) }}</span>
          <span>{{ o.numPeople }} 人</span>
          <span>{{ statusLabel(o.orderStatus) }}</span>
          
          <span class="row-actions">
            <button class="btn-outline" @click="toggleDetail(o)">
              {{ detailId === o.farmTripOrderId ? '收合' : '詳情' }}
            </button>
            <button
              v-if="o.orderStatus === 'CONFIRMED'"
              class="btn-outline"
              @click="cancelOrder(o.farmTripOrderId)"
            >取消</button>
            <button
              v-if="canReview(o)"
              class="btn"
              @click="startReview(o)"
            >評論</button>
          </span>

        </div>

        <!-- 評論表單：報名且活動已結束才會出現「評論」按鈕 -->
        <form
          v-if="reviewingId === o.farmTripOrderId"
          class="review-form"
          @submit.prevent="submitReview(o)"
        >
          <label>評分
            <select v-model.number="reviewForm.star">
              <option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option>
            </select>
          </label>
          <label class="grow">留言
            <input v-model="reviewForm.content" placeholder="分享你的體驗…" />
          </label>
          <button class="btn" type="submit">送出評論</button>
          <button class="btn-outline" type="button" @click="cancelReview">取消</button>
        </form>

        <!-- 報名詳情：點「詳情」展開 -->
        <div v-if="detailId === o.farmTripOrderId" class="order-detail">
          <div><span class="dt">訂單編號</span>{{ o.farmTripOrderBookingNo }}</div>
          <div><span class="dt">活動名稱</span>{{ o.farmTripTitle || '體驗活動' }}</div>
          <div><span class="dt">活動時間</span>{{ fmtDateTime(o.farmTripStart) }} ~ {{ fmtDateTime(o.farmTripEnd) }}</div>
          <div><span class="dt">報名人</span>{{ o.userName }}</div>
          <div><span class="dt">聯絡手機</span>{{ o.userPhoneNum }}</div>
          <div><span class="dt">人數</span>{{ o.numPeople }} 人</div>
          <div><span class="dt">備註</span>{{ o.note || '—' }}</div>
          <div><span class="dt">報名時間</span>{{ fmtDateTime(o.bookedAt) }}</div>
          <div><span class="dt">報名狀態</span>{{ statusLabel(o.orderStatus) }}</div>
        </div>

      </li>
    </ul>
  </section>
</template>

<style scoped>

.order-detail {
  background: var(--leaf-soft);
  border-radius: 10px;
  padding: 14px 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 24px;
  color: var(--ink);
  font-size: 14px;
}
.order-detail .dt {
  display: inline-block;
  width: 76px;
  color: var(--muted);
}

.my-orders h1 { margin: 0 0 16px; font-size: 24px; color: var(--ink); }
.muted { color: var(--muted); }

.btn {
  background: var(--leaf); color: #fff; border: none; padding: 8px 16px;
  border-radius: 10px; cursor: pointer; font-size: 14px;
}
.btn:hover { background: var(--leaf-dark); }
.btn-outline {
  background: #fff; color: var(--leaf-dark); border: 1px solid var(--leaf);
  padding: 6px 14px; border-radius: 10px; cursor: pointer; font-size: 14px;
}

/* 用 minmax(0,fr) 讓欄寬純照比例、不被內容撐開；最後動作欄固定寬，標題與資料才會上下對齊 */
.order-head, .order-row {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1.6fr) minmax(0, 1.6fr) minmax(0, 0.6fr) minmax(0, 0.9fr) 150px;
  align-items: center;
  gap: 12px;
}
.order-head > span, .order-row > * {
  text-align: left;
}
.order-row .row-actions {
  justify-content: flex-end;
}

.order-head {
  margin-top: 16px;
  padding: 8px 0;
  border-bottom: 2px solid var(--line);
  color: var(--muted);
  font-size: 13px;
}

.order-list { list-style: none; padding: 0; margin: 0; }
.order-list li {
  display: flex; flex-direction: column; gap: 10px;
  padding: 12px 0; border-bottom: 1px solid var(--line);
}

.trip-link { color: var(--leaf-dark); font-weight: 600; text-decoration: none; }
.trip-link:hover { text-decoration: underline; }

.row-actions { display: flex; gap: 8px; justify-content: flex-end; }

.review-form {
  display: flex; flex-wrap: wrap; align-items: center; gap: 10px;
  background: var(--leaf-soft); padding: 12px; border-radius: 10px;
}
.review-form .grow { flex: 1; }
.review-form .grow input { width: 100%; }
.review-form input, .review-form select {
  padding: 6px 8px; border: 1px solid var(--line); border-radius: 8px;
}
</style>