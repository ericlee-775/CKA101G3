<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ---- 活動列表 ----
const trips = ref([])
const listLoading = ref(true)
const listError = ref('')
function hideImg(e) { e.target.style.display = 'none' }
// 先試靜態示範圖，失敗改抓資料庫圖，再失敗才隱藏
function tripImg(e, id) {
  const el = e.target
  if (!el.dataset.fallback) {
    el.dataset.fallback = '1'
    el.src = `/api/farm-trips/${id}/image`
  } else {
    el.style.display = 'none'
  }
}

function goFarm(farmerId) {
  if (farmerId == null) return
  router.push({ name: 'farm-detail', params: { farmerId } })
}

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

// 報名狀態
const STATUS_LABEL = { OPEN: '開放報名中', CLOSED: '報名已截止', CANCELLED: '報名已取消' }
function statusLabel(s) { return STATUS_LABEL[s] || '尚無場次' }

const activeStatus = ref(null)   // null = 全部狀態
const STATUS_TABS = [
  { value: null,        label: '全部狀態',   icon: '🗂️' },
  { value: 'OPEN',      label: '開放報名中', icon: '🟢' },
  { value: 'CLOSED',    label: '報名已截止', icon: '⛔' },
  { value: 'CANCELLED', label: '報名已取消', icon: '🚫' },
]

const filteredTrips = computed(() =>
  trips.value.filter(t =>
    (activeType.value === null || t.farmTripType === activeType.value) &&
    (activeStatus.value === null || t.registrationStatus === activeStatus.value)
  )
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
          捲起袖子、走進產地，當一天小農！本頁彙整各地小農親手舉辦的農遊體驗，
          標示價格皆為<strong>參考價</strong>，實際費用以活動當天為準。
        </p>
        <p>
          看到喜歡的場次就直接線上<strong>預約</strong>，<strong>無需事先付款</strong>；
          活動當天到場後，再把費用交給小農就好。
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

      <div class="type-tabs">
        <button
          v-for="tab in STATUS_TABS"
          :key="tab.label"
          class="type-tab"
          :class="{ active: activeStatus === tab.value }"
          @click="activeStatus = tab.value"
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
          <img class="thumb" :src="`/farmily-web/trips/${t.farmTripId}.jpg`" alt="" @error="tripImg($event, t.farmTripId)" />          <span class="badge">{{ typeLabel(t.farmTripType) }}</span>
          
          <span
            v-if="t.registrationStatus"
            class="status-badge"
            :class="'rs-' + t.registrationStatus"
          >{{ statusLabel(t.registrationStatus) }}</span>
          
          <h3>{{ t.farmTripTitle }}</h3>
          <p class="farm-name" v-if="t.farmName">
            🏡 <span class="farm-link" @click.stop.prevent="goFarm(t.farmerId)">{{ t.farmName }}</span>
          </p>          
          <p class="muted">📍 {{ t.location }}</p>
          <p class="star">{{ stars(t.starNumbers) }}</p>
          <p class="price">參考價 {{ formatPrice(t.referPrice) }}</p>
        </router-link>
      </div>
    </section>
  </main>
</template>

<style scoped>

.status-badge {
  position: absolute;
  top: 26px;
  left: 26px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.2);
}
.status-badge.rs-OPEN { background: var(--leaf); }
.status-badge.rs-CLOSED { background: #8a8f98; }
.status-badge.rs-CANCELLED { background: #c0392b; }

.farm-name { color: var(--leaf-dark); font-weight: 600; margin: 2px 0; }
.farm-link { cursor: pointer; text-decoration: underline; }
.farm-link:hover { color: var(--leaf); }

.page-note {
  position: relative;
  background: linear-gradient(135deg, #fbfaf4 0%, var(--leaf-soft) 100%);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 38px 28px 34px 32px;
  margin-top: 24px;
  line-height: 1.95;
  color: var(--ink-soft);
  box-shadow: var(--shadow);
  overflow: hidden;
}
/* 左側細緻漸層裝飾線，取代原本較厚重的粗邊 */
.page-note::before {
  content: "";
  position: absolute;
  top: 0; left: 0; bottom: 0;
  width: 0px;
  background: linear-gradient(to bottom, var(--leaf), var(--leaf-dark));
}

/* 左上角的大引號，低透明度當作典雅的浮水印 */
/* 左上角的葉子小圖示 */
.page-note::after {
  content: "🌿";
  position: absolute;
  top: 14px;
  left: 18px;
  font-size: 24px;
  line-height: 1;
  opacity: 0.9;
  pointer-events: none;
}

/* 右下角的葉子小圖示（掛在最後一段文字上） */
.page-note p:last-child::after {
  content: "🌿";
  position: absolute;
  bottom: 14px;
  right: 18px;
  font-size: 24px;
  line-height: 1;
  opacity: 0.9;
  pointer-events: none;
  transform: scaleX(-1);   /* 水平翻轉，和左上角的葉子左右對稱 */
}

.page-note p {
  margin: 6px 0;
  font-size: 15px;
  letter-spacing: 0.02em;
}
.page-note strong {
  color: var(--leaf-dark);
  font-weight: 600;
}

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
.topbar { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 12px; }
h1 {color: var(--ink); }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; margin-top: 20px; }
.card {
  position: relative;              
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
</style>
