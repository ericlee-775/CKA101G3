<script setup>
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import authStore from '@/stores/auth'

// ===== 直接內嵌的 API 呼叫（http 會自動帶 session cookie）=====
const api = {
  create: (trip) => http.post('/api/farmer/farm-trips', trip),
  createSession: (farmTripId, session) =>
    http.post(`/api/farmer/farm-trips/${farmTripId}/sessions`, session),
  myTrips: (farmerId) => http.get(`/api/farmer/farm-trips?farmerId=${farmerId}`),
  sessions: (farmTripId) => http.get(`/api/farm-trips/${farmTripId}/sessions`),
}

// 目前登入的小農 id（由 session 決定，從 authStore 取，不用手填）
const farmerId = ref(null)

// ---- 建立活動表單 ----
const tripForm = ref({
  farmTripType: 'FARM_EXPERIENCE',
  farmTripTitle: '',
  farmTripIntro: '',
  location: '',
  referPrice: null,
})
const creating = ref(false)
const tripMsg = ref('')
const tripErr = ref('')

// ---- 我的活動（給「開場次」挑選目標用）----
const myTrips = ref([])
const selectedTripId = ref(null)
const sessions = ref([])

// ---- 開場次表單 ----
const sessionForm = ref({
  farmTripStart: '',
  farmTripEnd: '',
  tripBookStart: '',
  tripBookEnd: '',
})
const openingSession = ref(false)
const sessionMsg = ref('')
const sessionErr = ref('')

const STATUS_LABEL = { PENDING: '審核中', ACTIVE: '上架中', REJECTED: '已退回', CLOSED: '已關閉' }
function statusLabel(s) { return STATUS_LABEL[s] || s || '' }

function formatDateTime(ts) {
  if (!ts) return '—'
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '—' : d.toLocaleString('zh-TW',
    { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// datetime-local 的字串轉成毫秒數字送後端（java.sql.Timestamp 吃得下數字）
function toMillis(s) { return s ? new Date(s).getTime() : null }

onMounted(async () => {
  await authStore.ensureHydrated()
  farmerId.value = authStore.state.user?.farmerId ?? null
  if (farmerId.value) loadMyTrips()
})

async function loadMyTrips() {
  try {
    myTrips.value = await api.myTrips(farmerId.value)
  } catch {
    myTrips.value = []
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
    const created = await api.create({
      farmerId: farmerId.value,
      farmTripType: tripForm.value.farmTripType,
      farmTripTitle: tripForm.value.farmTripTitle,
      farmTripIntro: tripForm.value.farmTripIntro,
      location: tripForm.value.location,
      referPrice: tripForm.value.referPrice,
    })
    tripMsg.value = `活動「${created.farmTripTitle}」已送出，目前狀態：待審核。審核通過後才會上架。`
    tripForm.value = { farmTripType: 'FARM_EXPERIENCE', farmTripTitle: '', farmTripIntro: '', location: '', referPrice: null }
    await loadMyTrips()
    selectedTripId.value = created.farmTripId   // 建立後直接選取它，方便馬上開場次
    onSelectTrip()
  } catch (e) {
    tripErr.value = e.message || '建立失敗，請稍後再試。'
  } finally {
    creating.value = false
  }
}

// ===== 選活動 → 載入它的場次 =====
async function onSelectTrip() {
  sessionMsg.value = ''
  sessionErr.value = ''
  sessions.value = []
  if (!selectedTripId.value) return
  try {
    sessions.value = await api.sessions(selectedTripId.value)
  } catch {
    sessions.value = []
  }
}

// ===== 開新場次 =====
async function submitSession() {
  sessionMsg.value = ''
  sessionErr.value = ''
  if (!selectedTripId.value) { sessionErr.value = '請先選擇要開場次的活動。'; return }
  if (!sessionForm.value.farmTripStart) { sessionErr.value = '請填寫活動開始時間。'; return }
  openingSession.value = true
  try {
    await api.createSession(selectedTripId.value, {
      farmTripStart: toMillis(sessionForm.value.farmTripStart),
      farmTripEnd: toMillis(sessionForm.value.farmTripEnd),
      tripBookStart: toMillis(sessionForm.value.tripBookStart),
      tripBookEnd: toMillis(sessionForm.value.tripBookEnd),
    })
    sessionMsg.value = '場次已開立，狀態：報名中。'
    sessionForm.value = { farmTripStart: '', farmTripEnd: '', tripBookStart: '', tripBookEnd: '' }
    onSelectTrip()   // 重新載入場次列表
  } catch (e) {
    sessionErr.value = e.message || '開立場次失敗，請稍後再試。'
  } finally {
    openingSession.value = false
  }
}
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>🎪 體驗活動管理</h1>
    </header>

    <!-- 建立活動 -->
    <section class="card">
      <h2>建立體驗活動</h2>
      <p class="hint">送出後狀態為「待審核」，管理員審核通過才會上架給消費者看到。</p>

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
        <label>地點
          <input v-model="tripForm.location" placeholder="例如：苗栗縣大湖鄉" />
        </label>
        <label>參考價（每人）
          <input type="number" v-model.number="tripForm.referPrice" min="0" placeholder="NT$" />
        </label>
      </div>

      <button class="btn" :disabled="creating" @click="submitTrip">
        {{ creating ? '送出中…' : '送出活動（送審）' }}
      </button>
      <p v-if="tripMsg" class="msg">{{ tripMsg }}</p>
      <p v-if="tripErr" class="err">{{ tripErr }}</p>
    </section>

    <!-- 開場次 -->
    <section class="card">
      <h2>為活動開立場次</h2>

      <label class="full">選擇活動
        <select v-model="selectedTripId" @change="onSelectTrip">
          <option :value="null" disabled>請選擇要開場次的活動</option>
          <option v-for="t in myTrips" :key="t.farmTripId" :value="t.farmTripId">
            {{ t.farmTripTitle }}（{{ statusLabel(t.tripStatus) }}）
          </option>
        </select>
      </label>

      <div class="form">
        <label>活動開始
          <input type="datetime-local" v-model="sessionForm.farmTripStart" />
        </label>
        <label>活動結束
          <input type="datetime-local" v-model="sessionForm.farmTripEnd" />
        </label>
        <label>報名開始
          <input type="datetime-local" v-model="sessionForm.tripBookStart" />
        </label>
        <label>報名截止
          <input type="datetime-local" v-model="sessionForm.tripBookEnd" />
        </label>
      </div>

      <button class="btn" :disabled="openingSession" @click="submitSession">
        {{ openingSession ? '開立中…' : '開立場次' }}
      </button>
      <p v-if="sessionMsg" class="msg">{{ sessionMsg }}</p>
      <p v-if="sessionErr" class="err">{{ sessionErr }}</p>

      <div v-if="sessions.length" class="sessions">
        <h3>目前場次</h3>
        <ul>
          <li v-for="s in sessions" :key="s.farmSessionId">
            🗓️ {{ formatDateTime(s.farmTripStart) }} ~ {{ formatDateTime(s.farmTripEnd) }}
            ｜報名 {{ formatDateTime(s.tripBookStart) }} ~ {{ formatDateTime(s.tripBookEnd) }}
            ｜已報名 {{ s.attendance || 0 }} 人
          </li>
        </ul>
      </div>
    </section>
  </main>
</template>

<style scoped>
.farmer-page { padding: 32px 24px; display: flex; flex-direction: column; gap: 20px; }
.page-head h1 { margin: 0; font-size: 24px; color: var(--ink); }
.card {
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: var(--shadow); padding: 24px; border-top: 3px solid var(--leaf);
}
.card h2 { margin: 0 0 6px; font-size: 20px; color: var(--ink); }
.hint { margin: 0 0 16px; color: var(--muted); font-size: 14px; }
.form { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; margin-bottom: 16px; }
.form .full { grid-column: 1 / -1; }
label { display: flex; flex-direction: column; gap: 6px; font-size: 14px; color: var(--ink-soft); }
input, select, textarea {
  padding: 8px 10px; border: 1px solid var(--line); border-radius: 8px;
  font-size: 14px; font-family: inherit;
}
.btn {
  background: var(--leaf); color: #fff; border: none; padding: 10px 20px;
  border-radius: 10px; cursor: pointer; font-size: 14px;
}
.btn:hover { background: var(--leaf-dark); }
.btn:disabled { opacity: .6; cursor: default; }
.msg { color: var(--leaf-dark); margin: 10px 0 0; }
.err { color: #c0392b; margin: 10px 0 0; }
.sessions { margin-top: 18px; border-top: 1px solid var(--line); padding-top: 14px; }
.sessions h3 { margin: 0 0 8px; font-size: 15px; color: var(--ink); }
.sessions ul { list-style: none; padding: 0; margin: 0; }
.sessions li { padding: 6px 0; border-bottom: 1px solid var(--line); font-size: 14px; color: var(--ink-soft); }
</style>