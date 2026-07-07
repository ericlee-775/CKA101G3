<script setup>
// 團購詳情頁（不分層：資料抓取、狀態、畫面全寫在這一個元件裡，用封裝過的 groupBuyApi）。
// 路由：/group-buys/:groupBuyId（見 router/index.js）。點列表頁卡片會帶著 groupBuyId 進來。
//
// 這頁打的後端 API：
//   GET /api/groupBuy/{groupBuyId} → GroupBuyDetailDTO（商品名/團購價/目標數量/開團時間/截止時間/取貨地點/狀態）
//   permitAll，不用登入。
//
// 圖片：GroupBuyDetailDTO 沒有 productId 欄位，所以圖片沒辦法直接從這支 API 查。
// 做法是列表頁點卡片時，把 productId 用路由 query 帶過來（見 GroupBuysView.vue），
// 這裡讀 route.query.productId 去打 GET /api/products/{productId}/image；
// 如果是直接輸入網址進到這頁（沒有 query），就直接顯示暫無圖片佔位圖，不會出錯。
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import groupBuyApi from '@/api/groupBuy'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
import noImage from '@/assets/no-image.svg'

const route = useRoute()
const router = useRouter()

// 團購詳情（GroupBuyDetailDTO）。還沒抓到前是 null。
const groupBuy = ref(null)
const loading = ref(true)
const error = ref('')

const FALLBACK_IMAGE = noImage
const imageUrl = ref('')

async function loadImage(productId) {
  if (!productId) return
  try {
    const res = await fetch(`/api/products/${productId}/image`)
    if (!res.ok) return
    const blob = await res.blob()
    imageUrl.value = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗就維持預設圖。
  }
}

function revokeImageUrl() {
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value)
}

// 把價格顯示成「NT$ 220」。
function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// 把時間戳顯示成「2026/03/12」。
function formatDate(datetime) {
  if (!datetime) return '—'
  return new Date(datetime).toLocaleDateString('zh-TW')
}

async function loadGroupBuy() {
  const id = route.params.groupBuyId
  loading.value = true
  error.value = ''
  groupBuy.value = null
  revokeImageUrl()
  imageUrl.value = ''

  try {
    groupBuy.value = await groupBuyApi.getOne(id)
    // productId 是從列表頁用 query 帶過來的，只用來查圖片，不是這支 API 的欄位。
    await loadImage(route.query.productId)
  } catch (e) {
    error.value = e.status === 404 || /查無/.test(e.message || '')
      ? '找不到這個團購'
      : e.message || '無法載入團購資料，請稍後再試。'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'group-buys' })
}

onMounted(loadGroupBuy)
onUnmounted(revokeImageUrl)
// 從一個詳情頁換到另一個（例如之後加了「相關團購」連結）時，元件不會重建，靠 watch 重抓。
watch(() => route.params.groupBuyId, loadGroupBuy)
</script>

<template>
  <main class="page">
    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <div class="state__actions">
        <button type="button" @click="loadGroupBuy">重新載入</button>
        <button type="button" class="ghost" @click="goBack">回團購列表</button>
      </div>
    </div>

    <!-- 團購詳情 -->
    <article v-else-if="groupBuy" class="detail">
      <section class="gallery">
        <img
          class="gallery__main"
          :src="imageUrl || FALLBACK_IMAGE"
          :alt="groupBuy.productName"
          @error="($event) => ($event.target.src = FALLBACK_IMAGE)"
        />
      </section>

      <section class="info">
        <span class="badge" :class="groupBuyStatusInfo(groupBuy.status).className">
          {{ groupBuyStatusInfo(groupBuy.status).text }}
        </span>
        <h1 class="info__name">{{ groupBuy.productName }}</h1>

        <p class="info__price">
          團購價 {{ formatPrice(groupBuy.groupPrice) }}
        </p>

        <dl class="info__meta">
          <div class="info__meta-row">
            <dt>目標數量</dt>
            <dd>{{ groupBuy.targetAmount ?? '—' }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>開團時間</dt>
            <dd>{{ formatDate(groupBuy.openDatetime) }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>截止時間</dt>
            <dd>{{ formatDate(groupBuy.ddlDatetime) }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>取貨地點</dt>
            <dd>{{ groupBuy.pickupAddress || '—' }}</dd>
          </div>
        </dl>

        <button type="button" class="back-link" @click="goBack">← 回團購列表</button>
      </section>
    </article>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
}

/* ---------- 載入 / 錯誤狀態 ---------- */
.state {
  text-align: center;
  color: var(--muted);
  padding: 60px 0;
}
.state__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 12px;
}
.state button {
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}
.state button.ghost {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--ink);
}

/* ---------- 詳情主體：左圖右字 ---------- */
.detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 40px;
  align-items: start;
}
@media (max-width: 720px) {
  .detail {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.gallery__main {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 16px;
  background: var(--line);
  display: block;
}

/* ---------- 文字資訊 ---------- */
.info__name {
  font-size: 26px;
  color: var(--ink);
  margin: 12px 0 16px;
}
.info__price {
  font-size: 24px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0 0 20px;
}

.badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}
.status--open { background: #ecfdf3; color: #15803d; }
.status--success { background: #eff6ff; color: #1d4ed8; }
.status--failed { background: #f3f4f6; color: #6b7280; }
.status--cancelled { background: #f3f4f6; color: #6b7280; }
.status--pending { background: #fff7ed; color: #c2410c; }

.info__meta {
  border-top: 1px solid var(--line);
  padding-top: 16px;
  margin: 0;
}
.info__meta-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed var(--line);
  font-size: 14px;
}
.info__meta-row dt {
  color: var(--muted);
}
.info__meta-row dd {
  margin: 0;
  color: var(--ink);
  font-weight: 500;
  text-align: right;
}

.back-link {
  margin-top: 28px;
  background: none;
  border: none;
  color: var(--leaf-dark);
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}
.back-link:hover {
  text-decoration: underline;
}
</style>
