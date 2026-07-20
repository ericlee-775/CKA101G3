<script setup>
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import authStore from '@/stores/auth'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'

const api = {
  create: (fd) => http.post('/api/farmer/farm-trips', fd),
  updateTrip: (tripId, fd) => http.put(`/api/farmer/farm-trips/${tripId}`, fd),
  removeTrip: (tripId, farmerId) => http.del(`/api/farmer/farm-trips/${tripId}?farmerId=${farmerId}`),
  myTrips: (farmerId) => http.get(`/api/farmer/farm-trips?farmerId=${farmerId}`),
  myOrders: (farmerId) => http.get(`/api/farmer/farm-trips/orders?farmerId=${farmerId}`),
  sessions: (tripId) => http.get(`/api/farm-trips/${tripId}/sessions`),
  createSession: (tripId, body) => http.post(`/api/farmer/farm-trips/${tripId}/sessions`, body),
  updateSession: (sessionId, body) => http.put(`/api/farmer/farm-trips/sessions/${sessionId}`, body),
  cancelSession: (sessionId) => http.put(`/api/farmer/farm-trips/sessions/${sessionId}/cancel`),
  reopenSession: (sessionId) => http.put(`/api/farmer/farm-trips/sessions/${sessionId}/reopen`),
  notifySession: (sessionId, body) => http.post(`/api/farmer/farm-trips/sessions/${sessionId}/notify`, body),
}

const farmerId = ref(null)
const loading = ref(true)
const loadErr = ref('')
const trips = ref([])
const allOrders = ref([])

// 展開的活動 + 各活動已載入的場次
const expandedId = ref(null)
const sessionsMap = ref({})   // tripId -> sessions[]

// ---- 建立活動 ----
const tripForm = ref({ farmTripType: 'FARM_EXPERIENCE', farmTripTitle: '', farmTripIntro: '', location: '', referPrice: null })
const picFile = ref(null)
function onPicChange(e) { picFile.value = e.target.files[0] || null }
const creating = ref(false)
const tripMsg = ref('')
const tripErr = ref('')

// ---- 修改活動 ----
const editingTripId = ref(null)
const editTripForm = ref({ farmTripType: 'FARM_EXPERIENCE', farmTripTitle: '', farmTripIntro: '', location: '', referPrice: null })
const editTripPic = ref(null)
const savingTrip = ref(false)
const editTripMsg = ref('')
const editTripErr = ref('')

// ---- 新增場次 ----
const newSession = ref({ farmTripStart: null, farmTripEnd: null, tripBookStart: null, tripBookEnd: null })
const sessionMsg = ref('')

// ---- 編輯場次 ----
const editingId = ref(null)
const editForm = ref({ farmTripStart: '', farmTripEnd: '', tripBookStart: '', tripBookEnd: '' })

// ---- 通知報名者 ----
const notifyingId = ref(null)   // 目前正在撰寫通知的場次 id
const notifyForm = ref({ subject: '', message: '' })
const notifySending = ref(false)
const notifyMsg = ref('')
const notifyErr = ref('')

const TRIP_STATUS = { PENDING: '審核中', ACTIVE: '上架中', REJECTED: '已退回', CLOSED: '已關閉' }
const SESSION_STATUS = { ACTIVE: '報名中', CANCELLED: '已取消', COMPLETED: '已截止' }
const ORDER_STATUS = { CONFIRMED: '已確認', CANCELLED: '已取消', COMPLETED: '已完成' }
const TYPE_LABEL = { FARM_EXPERIENCE: '農場體驗營', FIELD_VISIT: '產地參訪' }

function tripStatus(s) { return TRIP_STATUS[s] || s || '' }
function sessionStatus(s) { return SESSION_STATUS[s] || s || '' }
function orderStatus(s) { return ORDER_STATUS[s] || s || '' }
function typeLabel(t) { return TYPE_LABEL[t] || t || '' }

function formatPrice(p) { return p == null ? '—' : `NT$ ${Number(p).toLocaleString('zh-TW')}` }

function formatDT(ts) {
  if (!ts) return '—'
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '—' : d.toLocaleString('zh-TW',
    { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
function toMillis(s) { return s ? new Date(s).getTime() : null }
// timestamp -> datetime-local 欄位需要的 "YYYY-MM-DDTHH:mm"
function toLocalInput(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  if (isNaN(d.getTime())) return ''
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`
}

// 某活動的圖片網址（沒圖時 <img> 會觸發 onerror 隱藏）
function tripImg(tripId) { return `/api/farm-trips/${tripId}/image` }
function hideImg(e) { e.target.style.display = 'none' }

// 某場次的報名名單（從已載入的訂單過濾）
function ordersOfSession(sessionId) {
  return allOrders.value.filter(o => o.farmSessionId === sessionId)
}

onMounted(async () => {
  await authStore.ensureHydrated()
  farmerId.value = authStore.state.user?.farmerId ?? null
  await loadAll()
})

async function loadAll() {
  loading.value = true
  loadErr.value = ''
  if (!farmerId.value) {
    loadErr.value = '無法取得小農身分，請重新以小農帳號登入。'
    loading.value = false
    return
  }
  try {
    const [t, o] = await Promise.all([api.myTrips(farmerId.value), api.myOrders(farmerId.value)])
    trips.value = t || []
    allOrders.value = o || []
  } catch (e) {
    loadErr.value = e.message || '載入失敗，請稍後再試。'
  } finally {
    loading.value = false
  }
}

async function toggleExpand(tripId) {
  if (expandedId.value === tripId) { expandedId.value = null; return }
  expandedId.value = tripId
  editingId.value = null
  sessionMsg.value = ''
  newSession.value = { farmTripStart: null, farmTripEnd: null, tripBookStart: null, tripBookEnd: null }
  if (!sessionsMap.value[tripId]) await loadSessions(tripId)
}

async function loadSessions(tripId) {
  try {
    sessionsMap.value = { ...sessionsMap.value, [tripId]: await api.sessions(tripId) }
  } catch {
    sessionsMap.value = { ...sessionsMap.value, [tripId]: [] }
  }
}

// ===== 建立活動 =====
async function submitTrip() {
  tripMsg.value = ''
  tripErr.value = ''
  if (!tripForm.value.farmTripTitle) { tripErr.value = '請填寫活動標題。'; return }
  if (!farmerId.value) { tripErr.value = '請先以小農身分登入。'; return }
  creating.value = true
  try {
    const fd = new FormData()
    fd.append('farmerId', farmerId.value)
    fd.append('farmTripType', tripForm.value.farmTripType)
    fd.append('farmTripTitle', tripForm.value.farmTripTitle)
    fd.append('farmTripIntro', tripForm.value.farmTripIntro ?? '')
    fd.append('location', tripForm.value.location ?? '')
    if (tripForm.value.referPrice != null) fd.append('referPrice', tripForm.value.referPrice)
    if (picFile.value) fd.append('pic', picFile.value)
    const created = await api.create(fd)
    tripMsg.value = `活動「${created.farmTripTitle}」已送出，狀態：待審核。`
    tripForm.value = { farmTripType: 'FARM_EXPERIENCE', farmTripTitle: '', farmTripIntro: '', location: '', referPrice: null }
    picFile.value = null
    await loadAll()
  } catch (e) {
    tripErr.value = e.message || '建立失敗，請稍後再試。'
  } finally {
    creating.value = false
  }
}

// ===== 修改活動 =====
function startEditTrip(t) {
  editingTripId.value = t.farmTripId
  editTripPic.value = null
  editTripMsg.value = ''
  editTripErr.value = ''
  editTripForm.value = {
    farmTripType: t.farmTripType || 'FARM_EXPERIENCE',
    farmTripTitle: t.farmTripTitle || '',
    farmTripIntro: t.farmTripIntro || '',
    location: t.location || '',
    referPrice: t.referPrice ?? null,
  }
}
function cancelEditTrip() { editingTripId.value = null }
function onEditPicChange(e) { editTripPic.value = e.target.files[0] || null }

async function submitEditTrip(tripId) {
  editTripMsg.value = ''
  editTripErr.value = ''
  if (!editTripForm.value.farmTripTitle) { editTripErr.value = '請填寫活動標題。'; return }
  if (!farmerId.value) { editTripErr.value = '請先以小農身分登入。'; return }
  savingTrip.value = true
  try {
    const fd = new FormData()
    fd.append('farmerId', farmerId.value)
    fd.append('farmTripType', editTripForm.value.farmTripType)
    fd.append('farmTripTitle', editTripForm.value.farmTripTitle)
    fd.append('farmTripIntro', editTripForm.value.farmTripIntro ?? '')
    fd.append('location', editTripForm.value.location ?? '')
    if (editTripForm.value.referPrice != null) fd.append('referPrice', editTripForm.value.referPrice)
    if (editTripPic.value) fd.append('pic', editTripPic.value)
    await api.updateTrip(tripId, fd)
    editingTripId.value = null
    await loadAll()
  } catch (e) {
    editTripErr.value = e.message || '修改失敗，請稍後再試。'
  } finally {
    savingTrip.value = false
  }
}

// ===== 刪除活動 =====
async function removeTrip(t) {
  if (!confirm(`確定要刪除活動「${t.farmTripTitle}」嗎？此動作無法復原。`)) return
  try {
    await api.removeTrip(t.farmTripId, farmerId.value)
    if (expandedId.value === t.farmTripId) expandedId.value = null
    if (editingTripId.value === t.farmTripId) editingTripId.value = null
    await loadAll()
  } catch (e) {
    alert('刪除失敗：' + (e.message || '請稍後再試'))
  }
}

// ===== 新增場次 =====
async function addSession(tripId) {
  sessionMsg.value = ''
  if (!newSession.value.farmTripStart) { sessionMsg.value = '請填寫活動開始時間。'; return }
  try {
    await api.createSession(tripId, {
      farmTripStart: toMillis(newSession.value.farmTripStart),
      farmTripEnd: toMillis(newSession.value.farmTripEnd),
      tripBookStart: toMillis(newSession.value.tripBookStart),
      tripBookEnd: toMillis(newSession.value.tripBookEnd),
    })
    newSession.value = { farmTripStart: null, farmTripEnd: null, tripBookStart: null, tripBookEnd: null }
    sessionMsg.value = '場次已新增。'
    await loadSessions(tripId)
  } catch (e) {
    sessionMsg.value = '新增失敗：' + (e.message || '請稍後再試')
  }
}

// ===== 編輯場次時間 =====
function startEdit(s) {
  editingId.value = s.farmSessionId
  editForm.value = {
    farmTripStart: s.farmTripStart ? new Date(s.farmTripStart) : null,
    farmTripEnd: s.farmTripEnd ? new Date(s.farmTripEnd) : null,
    tripBookStart: s.tripBookStart ? new Date(s.tripBookStart) : null,
    tripBookEnd: s.tripBookEnd ? new Date(s.tripBookEnd) : null,
  }
}
async function saveEdit(tripId, sessionId) {
  try {
    await api.updateSession(sessionId, {
      farmTripStart: toMillis(editForm.value.farmTripStart),
      farmTripEnd: toMillis(editForm.value.farmTripEnd),
      tripBookStart: toMillis(editForm.value.tripBookStart),
      tripBookEnd: toMillis(editForm.value.tripBookEnd),
    })
    editingId.value = null
    await loadSessions(tripId)
  } catch (e) {
    sessionMsg.value = '修改失敗：' + (e.message || '請稍後再試')
  }
}

// ===== 取消場次 =====
async function cancelSess(tripId, sessionId) {
  if (!confirm('確定要取消這個場次嗎？')) return
  try {
    await api.cancelSession(sessionId)
    await loadSessions(tripId)
  } catch (e) {
    sessionMsg.value = '取消失敗：' + (e.message || '請稍後再試')
  }
}

async function reopenSess(tripId, sessionId) {
  if (!confirm('確定要重啟這個場次嗎？（開放重新報名，之前已取消的報名不會恢復）')) return
  try {
    await api.reopenSession(sessionId)
    await loadSessions(tripId)
  } catch (e) {
    sessionMsg.value = '重啟失敗：' + (e.message || '請稍後再試')
  }
}

// ===== 通知報名者 =====
function openNotify(sessionId) {
  notifyingId.value = notifyingId.value === sessionId ? null : sessionId
  notifyForm.value = { subject: '', message: '' }
  notifyMsg.value = ''
  notifyErr.value = ''
}
function cancelNotify() { notifyingId.value = null }

async function sendNotify(sessionId) {
  notifyMsg.value = ''
  notifyErr.value = ''
  if (!notifyForm.value.message.trim()) { notifyErr.value = '請填寫通知內容。'; return }
  notifySending.value = true
  try {
    const count = await api.notifySession(sessionId, {
      subject: notifyForm.value.subject.trim(),
      message: notifyForm.value.message.trim(),
    })
    if (count > 0) {
      notifyMsg.value = `已寄出通知給 ${count} 位報名者。`
      notifyForm.value = { subject: '', message: '' }
    } else {
      notifyErr.value = '沒有可通知的報名者（此場次沒有「已確認」的報名，或查無 email）。'
    }
  } catch (e) {
    notifyErr.value = '寄送失敗：' + (e.message || '請稍後再試')
  } finally {
    notifySending.value = false
  }
}
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>🎪 體驗活動管理</h1>
      <span class="uid" v-if="farmerId">目前小農 ID：{{ farmerId }}</span>
    </header>

    <!-- 建立活動 -->
    <section class="card">
      <h2>建立體驗活動</h2>
      <p class="hint">送出後狀態為「待審核」，管理員審核通過才會上架。</p>
      <div class="form">
        <label>活動類型
          <select v-model="tripForm.farmTripType">
            <option value="FARM_EXPERIENCE">農場體驗營</option>
            <option value="FIELD_VISIT">產地參訪</option>
          </select>
        </label>
        <label>活動標題
          <input v-model="tripForm.farmTripTitle" placeholder="例如：草莓園採果一日體驗" />
        </label>
        <label class="full">活動介紹
          <textarea v-model="tripForm.farmTripIntro" rows="3" placeholder="活動內容、流程、注意事項…" />
        </label>
        <label>地點<input v-model="tripForm.location" placeholder="例如：苗栗縣大湖鄉" /></label>
        <label>參考價（每人）<input type="number" v-model.number="tripForm.referPrice" min="0" placeholder="NT$" /></label>
        <label class="full">活動圖片<input type="file" accept="image/*" @change="onPicChange" /></label>
      </div>
      <button class="btn" :disabled="creating" @click="submitTrip">{{ creating ? '送出中…' : '送出活動（送審）' }}</button>
      <p v-if="tripMsg" class="msg">{{ tripMsg }}</p>
      <p v-if="tripErr" class="err">{{ tripErr }}</p>
    </section>

    <!-- 我的活動 -->
    <section class="card">
      <h2>我的活動</h2>
      <p v-if="loading">載入中…</p>
      <p v-else-if="loadErr" class="err">{{ loadErr }}</p>
      <p v-else-if="trips.length === 0" class="hint">目前這個帳號沒有活動（farmerId = {{ farmerId }}）。若你確定建過活動，請確認活動的 farmer_id 是否等於此 ID。</p>

      <div v-for="t in trips" :key="t.farmTripId" class="trip">
        <div class="trip-head" @click="toggleExpand(t.farmTripId)">
          <img class="thumb" :src="tripImg(t.farmTripId)" alt="" @error="hideImg" />
          <div class="trip-main">
            <div class="trip-title">
              <strong>{{ t.farmTripTitle }}</strong>
              <span class="badge" :class="'st-' + t.tripStatus">{{ tripStatus(t.tripStatus) }}</span>
            </div>
            <div class="muted">{{ typeLabel(t.farmTripType) }}｜📍 {{ t.location }}｜{{ formatPrice(t.referPrice) }}</div>
          </div>
          <div class="trip-ops" @click.stop>
            <button class="btn-sm" @click="startEditTrip(t)">修改活動資訊</button>
            <button class="btn-sm danger" @click="removeTrip(t)">刪除</button>
          </div>

          <button
            type="button"
            class="session-toggle"
            @click.stop="toggleExpand(t.farmTripId)"
          >
            {{ expandedId === t.farmTripId ? '收合場次 ▲' : '管理場次 ▼' }}
          </button>

        </div>

        <!-- 修改活動 -->
        <div v-if="editingTripId === t.farmTripId" class="trip-edit">
          <p class="hint">修改後活動會改回「待審核」，須管理員重新審核通過才會再上架。</p>
          <div class="form">
            <label>活動類型
              <select v-model="editTripForm.farmTripType">
                <option value="FARM_EXPERIENCE">農場體驗營</option>
                <option value="FIELD_VISIT">產地參訪</option>
              </select>
            </label>
            <label>活動標題<input v-model="editTripForm.farmTripTitle" /></label>
            <label class="full">活動介紹<textarea v-model="editTripForm.farmTripIntro" rows="3" /></label>
            <label>地點<input v-model="editTripForm.location" /></label>
            <label>參考價（每人）<input type="number" v-model.number="editTripForm.referPrice" min="0" placeholder="NT$" /></label>
            <label class="full">更換圖片（不選則保留原圖）<input type="file" accept="image/*" @change="onEditPicChange" /></label>
          </div>
          <div class="edit-btns">
            <button class="btn" :disabled="savingTrip" @click="submitEditTrip(t.farmTripId)">{{ savingTrip ? '儲存中…' : '儲存修改（重新送審）' }}</button>
            <button class="btn-sm ghost" @click="cancelEditTrip">取消</button>
          </div>
          <p v-if="editTripMsg" class="msg">{{ editTripMsg }}</p>
          <p v-if="editTripErr" class="err">{{ editTripErr }}</p>
        </div>

        <!-- 展開：場次 + 報名名單 -->
        <div v-if="expandedId === t.farmTripId" class="trip-body">

          <h3>場次</h3>
          <p v-if="(sessionsMap[t.farmTripId] || []).length === 0" class="hint">尚無場次。</p>

          <div v-for="s in (sessionsMap[t.farmTripId] || [])" :key="s.farmSessionId" class="session">
            <!-- 顯示模式 -->
            <template v-if="editingId !== s.farmSessionId">
              <div class="session-info">
                <div>🗓️ 活動：{{ formatDT(s.farmTripStart) }} ~ {{ formatDT(s.farmTripEnd) }}</div>
                <div class="muted">報名：{{ formatDT(s.tripBookStart) }} ~ {{ formatDT(s.tripBookEnd) }}</div>
                <div class="muted">已報名 {{ s.attendance || 0 }} 人｜{{ sessionStatus(s.sessionStatus) }}</div>
              </div>
              <div class="session-actions" v-if="s.sessionStatus !== 'CANCELLED'">
                <button class="btn-sm" @click="startEdit(s)">修改活動時間</button>
                <button class="btn-sm danger" @click="cancelSess(t.farmTripId, s.farmSessionId)">取消場次</button>
              </div>

              <div class="session-actions" v-else>
                <button class="btn-sm" @click="reopenSess(t.farmTripId, s.farmSessionId)">重啟場次</button>
              </div>

            </template>

            <!-- 編輯模式 -->
            <form v-else class="edit-form" @submit.prevent="saveEdit(t.farmTripId, s.farmSessionId)">
              
            <label>活動開始
              <VueDatePicker v-model="editForm.farmTripStart" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>活動結束
              <VueDatePicker v-model="editForm.farmTripEnd" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>報名開始
              <VueDatePicker v-model="editForm.tripBookStart" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>報名截止
              <VueDatePicker v-model="editForm.tripBookEnd" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>

              <div class="edit-btns">
                <button class="btn-sm" type="submit">儲存</button>
                <button class="btn-sm ghost" type="button" @click="editingId = null">取消</button>
              </div>
            </form>

            <!-- 該場次報名名單 -->
            <div class="roster" v-if="ordersOfSession(s.farmSessionId).length">
              <div class="roster-head">
                報名名單（{{ ordersOfSession(s.farmSessionId).length }} 筆）
                <button class="btn-sm ghost" @click="openNotify(s.farmSessionId)">通知報名者</button>
              </div>
              <ul>
                <li v-for="o in ordersOfSession(s.farmSessionId)" :key="o.farmTripOrderId">
                  {{ o.userName }}｜{{ o.userPhoneNum }}｜{{ o.numPeople }} 人｜{{ orderStatus(o.orderStatus) }}
                  <span class="muted" v-if="o.note">｜備註：{{ o.note }}</span>
                  <span class="muted">｜{{ o.farmTripOrderBookingNo }}</span>
                </li>
              </ul>

              <!-- 撰寫通知信 -->
              <div v-if="notifyingId === s.farmSessionId" class="notify-box">
                <p class="hint">此通知會 email 給本場次「已確認」的報名者（已完成、已取消者不寄），內容可提醒衣著、天氣、裝備、集合地點等。</p>
                <label>主旨（可留白，預設為「Farmily - 體驗活動提醒」）
                  <input v-model="notifyForm.subject" placeholder="例如：草莓園採果體驗 - 行前提醒" />
                </label>
                <label>通知內容
                  <textarea v-model="notifyForm.message" rows="4"
                    placeholder="例如：本週日天氣涼，請穿著長袖與布鞋；集合地點在大湖遊客中心停車場，08:50 前抵達。" />
                </label>
                <div class="notify-btns">
                  <button class="btn" :disabled="notifySending" @click="sendNotify(s.farmSessionId)">{{ notifySending ? '寄送中…' : '寄送通知' }}</button>
                  <button class="btn-sm ghost" @click="cancelNotify">取消</button>
                </div>
                <p v-if="notifyMsg" class="msg">{{ notifyMsg }}</p>
                <p v-if="notifyErr" class="err">{{ notifyErr }}</p>
              </div>
            </div>
          </div>

          <!-- 新增場次 -->
          <h3 class="mt">新增場次</h3>
          <div class="form">
            
            <label>活動開始
              <VueDatePicker v-model="newSession.farmTripStart" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>活動結束
              <VueDatePicker v-model="newSession.farmTripEnd" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>報名開始
              <VueDatePicker v-model="newSession.tripBookStart" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>
            <label>報名截止
              <VueDatePicker v-model="newSession.tripBookEnd" :teleport="true"
                format="yyyy/MM/dd HH:mm" locale="zh-TW" placeholder="選擇日期時間" />
            </label>

          </div>
          <button class="btn" @click="addSession(t.farmTripId)">新增場次</button>
          <p v-if="sessionMsg" class="msg">{{ sessionMsg }}</p>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.farmer-page { padding: 32px 24px; display: flex; flex-direction: column; gap: 20px; }
.page-head { display: flex; align-items: baseline; justify-content: space-between; }
.page-head h1 { margin: 0; font-size: 24px; color: var(--ink); }
.uid { color: var(--muted); font-size: 14px; }
.card { background: #fff; border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); padding: 24px; border-top: 3px solid var(--leaf); }
.card h2 { margin: 0 0 6px; font-size: 20px; color: var(--ink); }
.hint { margin: 8px 0; color: var(--muted); font-size: 14px; }
.form { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; margin: 10px 0 12px; }
.form .full { grid-column: 1 / -1; }
label { display: flex; flex-direction: column; gap: 6px; font-size: 14px; color: var(--ink-soft); }
input, select, textarea { padding: 8px 10px; border: 1px solid var(--line); border-radius: 8px; font-size: 14px; font-family: inherit; }
.btn { background: var(--leaf); color: #fff; border: none; padding: 10px 20px; border-radius: 10px; cursor: pointer; font-size: 14px; }
.btn:hover { background: var(--leaf-dark); }
.btn:disabled { opacity: .6; cursor: default; }
.btn-sm { border: 1px solid var(--leaf); background: #fff; color: var(--leaf-dark); padding: 5px 12px; border-radius: 8px; cursor: pointer; font-size: 13px; }
.btn-sm.danger { border-color: #c0392b; color: #c0392b; }
.btn-sm.ghost { border-color: var(--line); color: var(--ink-soft); }
.msg { color: var(--leaf-dark); margin: 8px 0 0; }
.err { color: #c0392b; margin: 8px 0 0; }

.trip { border: 1px solid var(--line); border-radius: 12px; margin-top: 14px; overflow: hidden; }
.trip-head { display: flex; align-items: center; gap: 14px; padding: 12px 16px; cursor: pointer; }
.trip-head:hover { background: var(--leaf-soft); }
.thumb { width: 84px; height: 60px; object-fit: cover; border-radius: 8px; flex: none; }
.trip-main { flex: 1; }
.trip-ops { display: flex; gap: 8px; flex: none; }
.trip-edit { padding: 12px 16px 18px; border-top: 1px dashed var(--line); background: var(--leaf-soft); }
.trip-edit .edit-btns { display: flex; align-items: center; gap: 10px; margin-top: 4px; }
.trip-title { display: flex; align-items: center; gap: 10px; }
.muted { color: var(--muted); font-size: 13px; }
.chev { color: var(--muted); }

.session-toggle {
  flex-shrink: 0;
  padding: 8px 16px;
  border: 1px solid var(--leaf);
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.session-toggle:hover { background: var(--leaf-dark); }

.badge { font-size: 12px; padding: 2px 10px; border-radius: 999px; background: var(--leaf-soft); color: var(--leaf-dark); }
.badge.st-PENDING { background: #fff3cd; color: #8a6d00; }
.badge.st-REJECTED { background: #f8d7da; color: #a12622; }
.badge.st-CLOSED { background: #e2e3e5; color: #555; }

.trip-body { padding: 8px 16px 18px; border-top: 1px solid var(--line); }
.trip-body h3 { font-size: 15px; color: var(--ink); margin: 12px 0 6px; }
.trip-body h3.mt { margin-top: 20px; }
.session { border: 1px solid var(--line); border-radius: 10px; padding: 12px; margin-bottom: 10px; }
.session-info div { margin: 2px 0; font-size: 14px; }
.session-actions { display: flex; gap: 8px; margin-top: 8px; }
.edit-form { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; background: var(--leaf-soft); padding: 12px; border-radius: 8px; }
.edit-btns { grid-column: 1 / -1; display: flex; gap: 8px; }
.roster { margin-top: 10px; border-top: 1px dashed var(--line); padding-top: 8px; }
.roster-head { display: flex; align-items: center; gap: 12px; font-size: 14px; color: var(--ink); margin-bottom: 4px; }
.notify-box { margin-top: 10px; padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: var(--leaf-soft); display: flex; flex-direction: column; gap: 10px; }
.notify-btns { display: flex; align-items: center; gap: 10px; }
.roster ul { list-style: none; padding: 0; margin: 0; }
.roster li { padding: 4px 0; font-size: 14px; color: var(--ink-soft); border-bottom: 1px solid var(--line); }
</style>