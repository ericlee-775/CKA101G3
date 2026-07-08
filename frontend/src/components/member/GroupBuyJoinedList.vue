<script setup>
// 會員中心「我的團購」：進行中的團購清單（獨立元件，內容都寫在這裡）
// 資料來源：GET /api/member/groupBuy/joinedGroupBuyList
// 回傳欄位（ShowJoinedGroupBuyDTO）：status / ddlDatetime / pickupAddress / productName / buyQty / paidAmount
import { ref, onMounted } from 'vue'
import memberGroupBuyApi from '@/api/memberGroupBuy'

// 通知父層（MemberGroupBuysView）目前有幾筆，顯示在 tab 的數字小徽章
const emit = defineEmits(['count'])

const list = ref([])
const loading = ref(true)
const error = ref('')

// 後端 GroupBuyStatus 是英文代碼（沒加 @JsonValue，Jackson 用 enum name 序列化），
// 前端自己對照中文與徽章顏色；tone 對應下方 CSS 的 .badge--xxx
const STATUS_MAP = {
  open:      { label: '開團中', tone: 'green' },
  pending:   { label: '待開團', tone: 'amber' },
  success:   { label: '已成團', tone: 'blue' },
  failed:    { label: '未成團', tone: 'gray' },
  cancelled: { label: '已取消', tone: 'gray' },
}
const statusOf = (code) => STATUS_MAP[code] || { label: code ?? '—', tone: 'gray' }

// Timestamp 可能是 ISO 字串或毫秒數字，兩種都轉成本地時間顯示
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

// 截止日是否快到了（3 天內）：卡片上會多一個提醒
function isClosingSoon(ddl) {
  if (!ddl) return false
  const diff = new Date(ddl).getTime() - Date.now()
  return diff > 0 && diff < 3 * 24 * 60 * 60 * 1000
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = (await memberGroupBuyApi.joinedGroupBuyList()) || []
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

    <!-- 沒有進行中的團購 -->
    <div v-else-if="list.length === 0" class="state-box">
      <span class="state-icon">👥</span>
      <p>目前沒有進行中的團購</p>
      <router-link class="state-btn" to="/group-buys">去看看大家在揪什麼 →</router-link>
    </div>

    <!-- 團購卡片 -->
    <article v-for="(gb, i) in list" :key="i" class="gb-card">
      <header class="gb-head">
        <h3 class="gb-name">{{ gb.productName }}</h3>
        <div class="gb-badges">
          <span v-if="isClosingSoon(gb.ddlDatetime)" class="badge badge--red">⏰ 即將截止</span>
          <span class="badge" :class="`badge--${statusOf(gb.status).tone}`">
            {{ statusOf(gb.status).label }}
          </span>
        </div>
      </header>

      <dl class="gb-grid">
        <div class="gb-cell">
          <dt>我訂購</dt>
          <dd>{{ gb.buyQty ?? '—' }} 件</dd>
        </div>
        <div class="gb-cell">
          <dt>已付金額</dt>
          <dd class="gb-money">{{ formatMoney(gb.paidAmount) }}</dd>
        </div>
        <div class="gb-cell">
          <dt>截止時間</dt>
          <dd>{{ formatDate(gb.ddlDatetime) }}</dd>
        </div>
        <div class="gb-cell gb-cell--wide">
          <dt>取貨地點</dt>
          <dd>📍 {{ gb.pickupAddress || '—' }}</dd>
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

/* ===== 團購卡片 ===== */
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
.gb-badges {
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
.badge--red   { background: #fdeaea; color: #b03434; }

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
