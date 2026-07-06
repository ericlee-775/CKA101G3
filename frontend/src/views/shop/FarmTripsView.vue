<script setup>
import { ref, onMounted } from 'vue'

// 目前登入者（還沒接登入，先手動指定，報名／評論／查訂單都用它）
const userId = ref(1)

// 畫面模式：'list' 活動列表 / 'detail' 單一活動詳情
const view = ref('list')

// ---- 活動列表 ----
const trips = ref([])
const listLoading = ref(true)
const listError = ref('')

// ---- 活動詳情 ----
const detail = ref(null)
const sessions = ref([])
const comments = ref([])
const detailLoading = ref(false)
const detailError = ref('')

// ---- 報名表單 ----
const bookingSessionId = ref(null)   // 正在報名哪個場次；null = 沒有展開表單
const bookForm = ref({ numPeople: 1, userName: '', userPhoneNum: '', note: '' })
const bookMsg = ref('')

// ---- 評論表單 ----
const commentForm = ref({ star: 5, content: '' })
const commentMsg = ref('')

// ---- 我的報名 ----
const myOrders = ref([])
const myOrdersMsg = ref('')

// 活動類型 enum 轉中文
const TYPE_LABEL = { FARM_EXPERIENCE: '農場體驗營', FIELD_VISIT: '產地參訪' }
function typeLabel(t) { return TYPE_LABEL[t] || t || '' }

// 場次狀態 enum 轉中文
const SESSION_LABEL = { ACTIVE: '報名中', CANCELLED: '已取消', COMPLETED: '已截止' }
function sessionLabel(s) { return SESSION_LABEL[s] || s || '' }

function formatPrice(p) {
  if (p == null) return '—'
  return `NT$ ${Number(p).toLocaleString('zh-TW')}`
}

// 後端的 Timestamp 可能回傳數字(毫秒)或字串，new Date 都吃得下
function formatDateTime(ts) {
  if (!ts) return '—'
  const d = new Date(ts)
  if (isNaN(d.getTime())) return '—'
  return d.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
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

// ========== 進入詳情 ==========
async function openDetail(farmTripId) {
  view.value = 'detail'
  detailLoading.value = true
  detailError.value = ''
  detail.value = null
  sessions.value = []
  comments.value = []
  bookingSessionId.value = null
  bookMsg.value = ''
  commentMsg.value = ''
  try {
    const [dRes, sRes, cRes] = await Promise.all([
      fetch(`/api/farm-trips/${farmTripId}`),
      fetch(`/api/farm-trips/${farmTripId}/sessions`),
      fetch(`/api/farm-trips/${farmTripId}/comments`),
    ])
    if (!dRes.ok) throw new Error(`伺服器回應 ${dRes.status}`)
    detail.value = await dRes.json()
    sessions.value = sRes.ok ? await sRes.json() : []
    comments.value = cRes.ok ? await cRes.json() : []
  } catch (e) {
    detailError.value = e.message || '無法載入活動詳情。'
  } finally {
    detailLoading.value = false
  }
}

function backToList() {
  view.value = 'list'
  loadTrips()   // 回列表時順便刷新（星數/評論數可能變了）
}

// ========== 報名 ==========
function startBooking(sessionId) {
  bookingSessionId.value = sessionId
  bookForm.value = { numPeople: 1, userName: '', userPhoneNum: '', note: '' }
  bookMsg.value = ''
}

async function submitBooking(sessionId) {
  bookMsg.value = ''
  if (!bookForm.value.userName || !bookForm.value.userPhoneNum) {
    bookMsg.value = '請填寫聯絡人姓名與電話。'
    return
  }
  try {
    const res = await fetch(`/api/farm-trips/sessions/${sessionId}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: userId.value,
        numPeople: bookForm.value.numPeople,
        userName: bookForm.value.userName,
        userPhoneNum: bookForm.value.userPhoneNum,
        note: bookForm.value.note,
      }),
    })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    const order = await res.json()
    bookMsg.value = `報名成功！訂單編號：${order.farmTripOrderBookingNo}`
    bookingSessionId.value = null
    openDetail(detail.value.farmTripId)   // 重載，讓報名人數更新
  } catch (e) {
    bookMsg.value = '報名失敗：' + (e.message || '請稍後再試')
  }
}

// ========== 評論 ==========
async function submitComment() {
  commentMsg.value = ''
  if (!commentForm.value.content) {
    commentMsg.value = '請寫下留言內容。'
    return
  }
  try {
    const res = await fetch(`/api/farm-trips/${detail.value.farmTripId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: userId.value,
        star: commentForm.value.star,
        content: commentForm.value.content,
      }),
    })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    commentForm.value = { star: 5, content: '' }
    commentMsg.value = '感謝你的評論！'
    openDetail(detail.value.farmTripId)   // 重載評論與平均星數
  } catch (e) {
    commentMsg.value = '送出失敗：' + (e.message || '請稍後再試')
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
      <label class="uid">
        使用者 ID：
        <input type="number" v-model.number="userId" min="1" />
      </label>
    </div>

    <!-- ============ 活動列表 ============ -->
    <section v-if="view === 'list'">
      <p v-if="listLoading">載入中…</p>
      <p v-else-if="listError" class="error">{{ listError }}</p>
      <p v-else-if="trips.length === 0">目前沒有上架中的活動。</p>

      <div v-else class="grid">
        <article
          v-for="t in trips"
          :key="t.farmTripId"
          class="card"
          @click="openDetail(t.farmTripId)"
        >
          <span class="badge">{{ typeLabel(t.farmTripType) }}</span>
          <h3>{{ t.farmTripTitle }}</h3>
          <p class="muted">📍 {{ t.location }}</p>
          <p class="star">{{ stars(t.starNumbers) }}</p>
          <p class="price">參考價 {{ formatPrice(t.referPrice) }}</p>
        </article>
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

    <!-- ============ 活動詳情 ============ -->
    <section v-else-if="view === 'detail'">
      <button class="btn-outline" @click="backToList">← 回列表</button>

      <p v-if="detailLoading">載入中…</p>
      <p v-else-if="detailError" class="error">{{ detailError }}</p>

      <template v-else-if="detail">
        <span class="badge">{{ typeLabel(detail.farmTripType) }}</span>
        <h2>{{ detail.farmTripTitle }}</h2>
        <p class="muted">📍 {{ detail.location }}</p>
        <p class="star">{{ stars(detail.starNumbers) }}（{{ detail.commentNumbers || 0 }} 則評論）</p>
        <p class="price">參考價 {{ formatPrice(detail.referPrice) }}</p>
        <p class="intro">{{ detail.farmTripIntro }}</p>

        <!-- 場次 -->
        <h3>可報名場次</h3>
        <p v-if="sessions.length === 0" class="muted">目前尚無場次。</p>
        <div v-for="s in sessions" :key="s.farmSessionId" class="session">
          <div class="session-info">
            <p>🗓️ 活動時間：{{ formatDateTime(s.farmTripStart) }} ~ {{ formatDateTime(s.farmTripEnd) }}</p>
            <p class="muted">報名期間：{{ formatDateTime(s.tripBookStart) }} ~ {{ formatDateTime(s.tripBookEnd) }}</p>
            <p class="muted">目前報名人數：{{ s.attendance || 0 }}｜狀態：{{ sessionLabel(s.sessionStatus) }}</p>
          </div>
          <button
            v-if="s.sessionStatus === 'ACTIVE'"
            class="btn"
            @click="startBooking(s.farmSessionId)"
          >我要報名</button>

          <!-- 報名表單 -->
          <form
            v-if="bookingSessionId === s.farmSessionId"
            class="book-form"
            @submit.prevent="submitBooking(s.farmSessionId)"
          >
            <label>人數 <input type="number" v-model.number="bookForm.numPeople" min="1" /></label>
            <label>姓名 <input v-model="bookForm.userName" /></label>
            <label>電話 <input v-model="bookForm.userPhoneNum" /></label>
            <label>備註 <input v-model="bookForm.note" /></label>
            <button class="btn" type="submit">送出報名</button>
          </form>
        </div>
        <p v-if="bookMsg" class="msg">{{ bookMsg }}</p>

        <!-- 評論 -->
        <h3>評論</h3>
        <form class="comment-form" @submit.prevent="submitComment">
          <label>
            評分
            <select v-model.number="commentForm.star">
              <option v-for="n in 5" :key="n" :value="n">{{ n }} 星</option>
            </select>
          </label>
          <label class="grow">
            留言 <input v-model="commentForm.content" placeholder="分享你的體驗…" />
          </label>
          <button class="btn" type="submit">送出</button>
        </form>
        <p v-if="commentMsg" class="msg">{{ commentMsg }}</p>

        <ul class="comment-list">
          <li v-for="c in comments" :key="c.commentId">
            <span class="star">{{ stars(c.star) }}</span>
            <span>{{ c.content }}</span>
            <span class="muted small">{{ formatDateTime(c.createdAt) }}</span>
          </li>
        </ul>
      </template>
    </section>
  </main>
</template>

<style scoped>
.page { padding: 32px clamp(18px, 4vw, 56px); color: var(--ink); }
.topbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
h1 { color: var(--ink); }
.uid input { width: 70px; padding: 4px 8px; border: 1px solid var(--line); border-radius: 8px; }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; margin-top: 20px; }
.card {
  background: #fff; border: 1px solid var(--line); border-radius: 14px; padding: 18px;
  box-shadow: var(--shadow); cursor: pointer; transition: transform .15s, box-shadow .15s;
}
.card:hover { transform: translateY(-3px); box-shadow: var(--shadow-hover); }
.card h3 { margin: 8px 0 4px; color: var(--ink); }

.badge {
  display: inline-block; background: var(--leaf-soft); color: var(--leaf-dark);
  font-size: 13px; padding: 2px 10px; border-radius: 999px;
}
.muted { color: var(--muted); }
.small { font-size: 12px; }
.star { color: #e6a700; letter-spacing: 2px; }
.price { color: var(--leaf-dark); font-weight: 600; }
.intro { line-height: 1.7; color: var(--ink-soft); margin: 12px 0 20px; white-space: pre-wrap; }
.error { color: #c0392b; }
.msg { color: var(--leaf-dark); }

.btn {
  background: var(--leaf); color: #fff; border: none; padding: 8px 16px;
  border-radius: 10px; cursor: pointer; font-size: 14px;
}
.btn:hover { background: var(--leaf-dark); }
.btn-outline {
  background: #fff; color: var(--leaf-dark); border: 1px solid var(--leaf);
  padding: 6px 14px; border-radius: 10px; cursor: pointer; font-size: 14px; margin: 8px 0;
}

.session {
  border: 1px solid var(--line); border-radius: 12px; padding: 14px 16px; margin: 12px 0;
  display: flex; flex-wrap: wrap; align-items: center; gap: 12px; justify-content: space-between;
}
.session-info p { margin: 2px 0; }
.book-form, .comment-form {
  width: 100%; display: flex; flex-wrap: wrap; gap: 10px; align-items: center;
  background: var(--leaf-soft); padding: 12px; border-radius: 10px;
}
.book-form label, .comment-form label { display: flex; align-items: center; gap: 6px; }
.book-form input, .comment-form input, .comment-form select {
  padding: 6px 8px; border: 1px solid var(--line); border-radius: 8px;
}
.comment-form .grow { flex: 1; }
.comment-form .grow input { flex: 1; width: 100%; }

.comment-list { list-style: none; padding: 0; margin-top: 12px; }
.comment-list li {
  display: flex; gap: 12px; align-items: center; flex-wrap: wrap;
  padding: 10px 0; border-bottom: 1px solid var(--line);
}

.my-orders { margin-top: 40px; border-top: 1px solid var(--line); padding-top: 20px; }
.order-list { list-style: none; padding: 0; }
.order-list li {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-bottom: 1px solid var(--line); gap: 12px;
}
</style>