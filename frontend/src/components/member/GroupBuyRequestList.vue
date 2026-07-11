<script setup>
// 會員中心「我的團購」：我發起過的團購申請（獨立元件，內容都寫在這裡）
// 資料來源：GET /api/member/groupBuy/myRequests
// 回傳欄位（UnderReviewDTO）：groupBuyId / productName / groupPrice / requestStatus /
//   targetAmount / openDatetime / ddlDatetime / rejectReason / replyDatetime
// 跟「進行中」（GroupBuyJoinedList，資料來源是 /joinedGroupBuyList）互不相干，
// 同一筆團購如果自己也有加入，兩邊會同時各自出現一筆，這是預期行為。
import { ref, onMounted } from 'vue'
import memberGroupBuyApi from '@/api/memberGroupBuy'

const emit = defineEmits(['count'])

const list = ref([])
const loading = ref(true)
const error = ref('')

// RequestStatus 是英文代碼（沒加 @JsonValue），前端自己對照中文與徽章顏色
const STATUS_MAP = {
  pending: { label: '待審核', tone: 'amber' },
  approved: { label: '已通過', tone: 'green' },
  rejected: { label: '已拒絕', tone: 'gray' },
}
const statusOf = (code) => STATUS_MAP[code] || { label: code ?? '—', tone: 'gray' }

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
    list.value = (await memberGroupBuyApi.myRequests()) || []
    emit('count', list.value.length)
  } catch (e) {
    error.value = e.message || '載入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="gb-list">
    <!-- 載入中：骨架卡片 -->
    <template v-if="loading">
      <div v-for="i in 2" :key="i" class="gb-card skeleton">
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

    <!-- 沒有任何團購申請 -->
    <div v-else-if="list.length === 0" class="state-box">
      <span class="state-icon">📋</span>
      <p>暫無任何團購申請</p>
      <router-link class="state-btn" to="/group-buys">去看看可以發起哪些團購 →</router-link>
    </div>

    <!-- 申請卡片 -->
    <article v-for="(req, i) in list" :key="i" class="gb-card">
      <header class="gb-head">
        <h3 class="gb-name">{{ req.productName }}</h3>
        <span class="badge" :class="`badge--${statusOf(req.requestStatus).tone}`">
          {{ statusOf(req.requestStatus).label }}
        </span>
      </header>

      <dl class="gb-grid">
        <div class="gb-cell">
          <dt>團購價</dt>
          <dd>{{ formatMoney(req.groupPrice) }}</dd>
        </div>
        <div class="gb-cell">
          <dt>達標金額</dt>
          <dd class="gb-money">{{ formatMoney(req.targetAmount) }}</dd>
        </div>
        <div class="gb-cell">
          <dt>開團時間</dt>
          <dd>{{ formatDate(req.openDatetime) }}</dd>
        </div>
        <div class="gb-cell">
          <dt>截止時間</dt>
          <dd>{{ formatDate(req.ddlDatetime) }}</dd>
        </div>
        <div v-if="req.requestStatus !== 'pending'" class="gb-cell">
          <dt>審核回覆時間</dt>
          <dd>{{ formatDate(req.replyDatetime) }}</dd>
        </div>
        <div v-if="req.requestStatus === 'rejected'" class="gb-cell gb-cell--wide">
          <dt>拒絕原因</dt>
          <dd class="gb-reject-reason">{{ req.rejectReason || '—' }}</dd>
        </div>
      </dl>
    </article>
  </div>
</template>

<style scoped>
.gb-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ===== 申請卡片 ===== */
.gb-card {
  padding: 18px 20px;
  background: #fff;
  border: 1px solid var(--line);
  border-left: 4px solid var(--leaf);
  border-radius: 14px;
  box-shadow: var(--shadow);
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}
.gb-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.gb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.gb-name {
  margin: 0;
  font-size: 17px;
  color: var(--ink);
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
.badge--amber { background: #fdf1dc; color: #9a6b15; }
.badge--gray  { background: #eeeeea; color: #75806f; }

/* 欄位以小格子排列，窄螢幕自動換行 */
.gb-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px 16px;
  margin: 0;
}
.gb-cell dt {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 2px;
}
.gb-cell dd {
  margin: 0;
  font-size: 14px;
  color: var(--ink-soft);
}
.gb-cell--wide {
  grid-column: 1 / -1;
}
.gb-money {
  font-weight: 700;
  color: var(--leaf-dark);
}
.gb-reject-reason {
  color: #b03434;
}

@media (max-width: 560px) {
  .gb-grid {
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

/* 骨架屏：載入時的灰色佔位卡 */
.skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
