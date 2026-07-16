<script setup>
// 會員中心「我的收藏」→ 團購收藏（獨立元件，內容都寫在這裡）
// 資料來源：GET /api/member/groupBuyLists（GroupBuyWishlistController，只會回 status=open 的團購）
// 回傳欄位（WishListDTO）：hostUser / groupBuyId / retailPrice / status / ddlDatetime / pickupAddress /
//   productName / groupPrice / productId / targetAmount / currentAmount / difference / favorite(固定 true)
//
// 這裡的愛心一定是實心（清單裡的每一筆本來就都是收藏過的），點擊會呼叫
// DELETE /api/member/groupBuyLists/{groupBuyId} 取消收藏，跟公開團購頁（GroupBuysView.vue）用的是同一組 API。
import { ref, onMounted } from 'vue'
import groupBuyFavoriteApi from '@/api/groupBuyFavorite'
import { usePagination } from '@/composables/usePagination'
import Pagination from '@/components/Pagination.vue'
import noImage from '@/assets/no-image.svg'

const emit = defineEmits(['count'])

const list = ref([])
const loading = ref(true)
const error = ref('')

const { page, totalPages, pageItems: pagedList } = usePagination(list, 10)

const formatMoney = (n) => (n == null ? '—' : `NT$ ${Number(n).toLocaleString('zh-TW')}`)
function formatDate(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

// productId 這支 DTO 直接就有，不用像訂單/開團申請清單那樣繞一手去查。
const FALLBACK_IMAGE = noImage
const imageUrls = ref({})
async function loadImages() {
  for (const gb of list.value) {
    fetchImage(gb.productId)
  }
}
async function fetchImage(productId) {
  if (productId == null || imageUrls.value[productId]) return
  try {
    const res = await fetch(`/api/products/${productId}/image`)
    if (!res.ok) return
    const blob = await res.blob()
    imageUrls.value[productId] = URL.createObjectURL(blob)
  } catch {
    // 沒圖就維持預設圖，不影響其他卡片。
  }
}
function groupBuyImage(gb) {
  return imageUrls.value[gb.productId] || FALLBACK_IMAGE
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = (await groupBuyFavoriteApi.list()) || []
    emit('count', list.value.length)
    loadImages()
  } catch (e) {
    error.value = e.message || '載入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// 取消收藏：從畫面上移除該筆卡片，不用整個重新載入。
const removingIds = ref(new Set())
async function unfavorite(gb) {
  if (removingIds.value.has(gb.groupBuyId)) return
  const busy = new Set(removingIds.value)
  busy.add(gb.groupBuyId)
  removingIds.value = busy
  try {
    await groupBuyFavoriteApi.remove(gb.groupBuyId)
    list.value = list.value.filter((item) => item.groupBuyId !== gb.groupBuyId)
    emit('count', list.value.length)
  } catch (e) {
    error.value = e.message || '取消收藏失敗，請稍後再試'
  } finally {
    const done = new Set(removingIds.value)
    done.delete(gb.groupBuyId)
    removingIds.value = done
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

    <!-- 沒有收藏任何團購 -->
    <div v-else-if="list.length === 0" class="state-box">
      <span class="state-icon">🤍</span>
      <p>還沒有收藏任何團購</p>
      <router-link class="state-btn" to="/group-buys">去看看有什麼團購 →</router-link>
    </div>

    <!-- 收藏卡片 -->
    <template v-else>
      <article v-for="gb in pagedList" :key="gb.groupBuyId" class="gb-card">
        <!-- 點卡片任何地方（除了愛心）都導到消費者看的團購詳情頁：/group-buys/:groupBuyId
             （前端路由，實際資料來自 PublicGroupBuyController 的 GET /api/groupBuy/{groupBuyId}） -->
        <RouterLink
          class="gb-card__link"
          :to="{ name: 'group-buy-detail', params: { groupBuyId: gb.groupBuyId } }"
        >
          <div class="gb-img-wrap">
            <img class="gb-img" :src="groupBuyImage(gb)" :alt="gb.productName" loading="lazy" />
          </div>

          <div class="gb-body">
            <div class="gb-head">
              <h3 class="gb-name">{{ gb.productName }}</h3>
              <!-- 這裡的愛心一定是實心，點下去取消收藏；用 stop+prevent 避免連帶觸發卡片導頁 -->
              <button
                type="button"
                class="gb-heart"
                :disabled="removingIds.has(gb.groupBuyId)"
                aria-label="取消收藏"
                @click.stop.prevent="unfavorite(gb)"
              >
                ♥
              </button>
            </div>

            <dl class="gb-grid">
              <div class="gb-cell">
                <dt>發起人</dt>
                <dd>{{ gb.hostUser || '—' }}</dd>
              </div>
              <div class="gb-cell">
                <dt>團購價</dt>
                <dd>{{ formatMoney(gb.groupPrice) }}<span class="gb-retail"> 原價 {{ formatMoney(gb.retailPrice) }}</span></dd>
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

            <div class="gb-progress">
              <span>目標金額 {{ formatMoney(gb.targetAmount) }}</span>
              <span class="gb-progress__diff">
                {{ gb.difference > 0 ? `再 ${formatMoney(gb.difference)} 即成團` : '已達標，等待截止結算' }}
              </span>
            </div>
          </div>
        </RouterLink>
      </article>

      <Pagination v-model:page="page" :total-pages="totalPages" />
    </template>
  </div>
</template>

<style scoped>
.gb-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ===== 收藏卡片 ===== */
.gb-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}
.gb-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}
.gb-card__link {
  display: flex;
  color: inherit;
  text-decoration: none;
}
.gb-img-wrap {
  flex: 0 0 120px;
  aspect-ratio: 1 / 1;
  align-self: flex-start;
  background: var(--line);
}
.gb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.gb-body {
  flex: 1;
  min-width: 0;
  padding: 16px 20px;
}

.gb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.gb-card__link:hover .gb-name {
  color: var(--leaf-dark);
}
.gb-name {
  margin: 0;
  min-width: 0;
  font-size: 17px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.gb-heart {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: #fdeaea;
  color: #e0435b;
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 0.15s ease, background 0.15s ease;
}
.gb-heart:hover:not(:disabled) {
  transform: scale(1.1);
  background: #fbd7d7;
}
.gb-heart:disabled {
  opacity: 0.6;
  cursor: default;
}

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
.gb-retail {
  font-size: 12px;
  color: var(--muted);
  text-decoration: line-through;
  margin-left: 4px;
}

/* 達標進度提示 */
.gb-progress {
  margin-top: 12px;
  padding: 10px 14px;
  background: var(--leaf-soft);
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-soft);
}
.gb-progress__diff {
  font-weight: 700;
  color: var(--leaf-dark);
}

@media (max-width: 560px) {
  .gb-card__link {
    flex-direction: column;
  }
  .gb-img-wrap {
    flex-basis: auto;
    width: 100%;
    align-self: stretch;
  }
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

/* 骨架屏 */
.skeleton {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px;
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
