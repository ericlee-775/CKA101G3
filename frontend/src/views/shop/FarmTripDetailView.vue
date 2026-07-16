<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import authStore from '@/stores/auth'

const route = useRoute()
// 網址上的活動 id：/farm-trips/:farmTripId
const farmTripId = ref(route.params.farmTripId)

// 登入的會員 id（跟 BlogDetailView 一致，從 authStore 拿）
const userId = computed(() => authStore.state.user?.userId ?? null)

// ---- 活動詳情 ----
const detail = ref(null)
const sessions = ref([])
const comments = ref([])
const detailLoading = ref(true)
const detailError = ref('')
function hideImg(e) { e.target.style.display = 'none' }

// ---- 報名表單 ----
const bookingSessionId = ref(null)   // 正在報名哪個場次；null = 沒有展開表單
const bookForm = ref({ numPeople: 1, userName: '', userPhoneNum: '', note: '' })
const bookMsg = ref('')

// ---- 評論表單 ----
const commentForm = ref({ star: 5, content: '' })
const commentMsg = ref('')

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

// ========== 載入詳情 ==========
async function loadDetail() {
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
      fetch(`/api/farm-trips/${farmTripId.value}`),
      fetch(`/api/farm-trips/${farmTripId.value}/sessions`),
      fetch(`/api/farm-trips/${farmTripId.value}/comments`),
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

// ========== 報名 ==========
function startBooking(sessionId) {
  bookingSessionId.value = sessionId
  bookForm.value = { numPeople: 1, userName: '', userPhoneNum: '', note: '' }
  bookMsg.value = ''
}

async function submitBooking(sessionId) {
  bookMsg.value = ''
  // 未登入擋下：沒有 userId 就別送，避免報名綁到 null 或錯的帳號
  if (!userId.value) {
    bookMsg.value = '請先登入會員再報名。'
    return
  }
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
    loadDetail()   // 重載，讓報名人數更新
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
    const res = await fetch(`/api/farm-trips/${farmTripId.value}/comments`, {
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
    loadDetail()   // 重載評論與平均星數
  } catch (e) {
    commentMsg.value = '送出失敗：' + (e.message || '請稍後再試')
  }
}

onMounted(loadDetail)
</script>

<template>
  <main class="page">
    <router-link class="btn-outline" :to="{ name: 'farm-trips' }">← 回列表</router-link>

    <p v-if="detailLoading">載入中…</p>
    <p v-else-if="detailError" class="error">{{ detailError }}</p>

    <template v-else-if="detail">
      <span class="badge">{{ typeLabel(detail.farmTripType) }}</span>
      <h2>{{ detail.farmTripTitle }}</h2>
      <img class="detail-img" :src="`/api/farm-trips/${detail.farmTripId}/image`" alt="" @error="hideImg" />
      <p class="farm-name" v-if="detail.farmName">🏡 {{ detail.farmName }}</p>
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
  </main>
</template>

<style scoped>

.farm-name { color: var(--leaf-dark); font-weight: 600; margin: 2px 0; }

.page { padding: 32px clamp(18px, 4vw, 56px); color: var(--ink); }
h2 { color: var(--ink); }

.detail-img {
  width: 100%; max-width: 520px; max-height: 320px; object-fit: cover;
  border-radius: 12px; margin: 12px 0; display: block;
}

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
  display: inline-block; text-decoration: none;
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
</style>
