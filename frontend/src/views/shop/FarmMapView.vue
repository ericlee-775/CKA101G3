<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { listFarms, getFarm, normalizeFarm } from '@/api/farm'

const farms = ref([])
const loading = ref(true)
const error = ref('')
const detailError = ref('')

const selectedRegion = ref('全部')
const selectedFarm = ref(null)
const selectingId = ref(null)

const regions = computed(() => {
  const values = farms.value.map(farm => farm.region).filter(Boolean)
  return ['全部', ...new Set(values)]
})

const filteredFarms = computed(() => {
  if (selectedRegion.value === '全部') return farms.value
  return farms.value.filter(farm => farm.region === selectedRegion.value)
})

const mapSrc = computed(() => {
  if (!selectedFarm.value?.query) return ''
  return `https://www.google.com/maps?q=${encodeURIComponent(selectedFarm.value.query)}&output=embed`
})

async function loadFarms() {
  loading.value = true
  error.value = ''
  detailError.value = ''

  try {
    const data = await listFarms()
    farms.value = data.map(normalizeFarm)
  } catch (e) {
    error.value = e.message || '無法載入農場資料，請稍後再試。'
  } finally {
    loading.value = false
  }
}

async function selectFarm(farm) {
  if (!farm) return

  selectedFarm.value = farm
  selectingId.value = farm.farmerId
  detailError.value = ''

  try {
    selectedFarm.value = normalizeFarm(await getFarm(farm.farmerId))
  } catch (e) {
    detailError.value = e.message || '無法載入農場詳細資料。'
  } finally {
    selectingId.value = null
  }
}

watch(filteredFarms, (list) => {
  if (list.length === 0) {
    selectedFarm.value = null
    return
  }

  const selectedStillVisible = list.some(farm => farm.farmerId === selectedFarm.value?.farmerId)
  if (!selectedStillVisible) {
    selectFarm(list[0])
  }
})

// 收集每個清單項目的 DOM，讓「選中」時能把它捲進可視範圍
const itemEls = {}
function setItemRef(id, el) {
  if (el) itemEls[id] = el
  else delete itemEls[id]     // 元素卸載時清掉，避免留下失效參照
}

// 選中的農場改變時（不論點擊或篩選後自動選第一筆），把它捲到清單可視處
watch(() => selectedFarm.value?.farmerId, async (id) => {
  if (!id) return
  await nextTick()            // 等 DOM 更新完(is-active 樣式等)再捲
  itemEls[id]?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
})

onMounted(loadFarms)
</script>

<template>
  <main class="map-page">
    <header class="map-hero">
      <h1>🗺️ 產地地圖</h1>
      <p>點選左側農場，看看你的食材來自台灣哪個角落。</p>
      <p v-if="selectedFarm" class="current-farm">目前顯示：<strong>{{ selectedFarm.farmName }}</strong></p>
    </header>

    <p v-if="loading" class="state">農場資料載入中…</p>

    <section v-else-if="error" class="state state--error">
      <p>載入失敗：{{ error }}</p>
      <button type="button" @click="loadFarms">重新載入</button>
    </section>

    <p v-else-if="farms.length === 0" class="state">目前還沒有可顯示的合作農場。</p>

    <template v-else>
      <div class="region-tabs">
        <button
          v-for="region in regions"
          :key="region"
          class="region-tab"
          :class="{ 'is-active': selectedRegion === region }"
          type="button"
          @click="selectedRegion = region"
        >
          {{ region }}
        </button>
      </div>

      <p v-if="detailError" class="detail-error">農場詳細資料載入失敗：{{ detailError }}</p>

      <div class="map-layout">
        <ul class="farm-list">
          <li
            v-for="farm in filteredFarms"
            :key="farm.farmerId"
            :ref="el => setItemRef(farm.farmerId, el)"
            class="farm-item"
            :class="{ 'is-active': selectedFarm?.farmerId === farm.farmerId }"
            @click="selectFarm(farm)"
          >
            <strong>{{ farm.farmName }}</strong>
            <span>📍 {{ farm.location }}</span>
            <small v-if="farm.farmDesc">{{ farm.farmDesc }}</small>
            <em v-if="selectingId === farm.farmerId">載入中…</em>
          </li>

          <li v-if="filteredFarms.length === 0" class="farm-empty">
            這個地區目前還沒有合作農場
          </li>
        </ul>

        <div class="map-frame">
          <iframe
            v-if="mapSrc"
            :src="mapSrc"
            :title="`${selectedFarm?.farmName || '農場'}位置地圖`"
            width="100%"
            height="100%"
            style="border: 0"
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
          ></iframe>
          <p v-else class="map-empty">請先選擇農場</p>
        </div>
      </div>
    </template>
  </main>
</template>

<style scoped>
.map-page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
}

/* ---------- 開頭 ---------- */
.map-hero {
  text-align: center;
  margin-bottom: 24px;
}
.map-hero h1 {
  font-size: 28px;
  color: var(--ink);
  margin: 0 0 8px;
}
.map-hero p {
  color: var(--muted);
  margin: 0;
}
.current-farm {
  margin-top: 10px !important;
  color: var(--ink-soft);
  font-size: 14px;
}
.current-farm strong {
  color: var(--ink);
}

.state {
  padding: 44px 0;
  text-align: center;
  color: var(--muted);
}

.state--error {
  color: #b42318;
}

.state--error button {
  margin-top: 12px;
  padding: 9px 18px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink);
  cursor: pointer;
}

.detail-error {
  margin: -6px 0 16px;
  color: #b42318;
  text-align: center;
}

/* ---------- 地區篩選列 ---------- */
.region-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-bottom: 20px;
}
.region-tab {
  padding: 8px 18px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink-soft);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, border-color 0.18s ease;
}
.region-tab:hover {
  border-color: var(--leaf);
  color: var(--leaf);
}
.region-tab.is-active {
  background: var(--leaf);
  border-color: var(--leaf);
  color: #fff;
}

/* ---------- 清單 + 地圖排版 ---------- */
.map-layout {
  display: grid;
  grid-template-columns: 280px 1fr;   /* 左清單固定寬、右地圖撐滿 */
  gap: 20px;
}

/* ---------- 左：農場清單 ---------- */
.farm-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.farm-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.farm-item:hover {
  border-color: var(--leaf);
}
.farm-item.is-active {
  border-color: var(--leaf);
  box-shadow: 0 0 0 2px var(--leaf-soft);   /* 選中時外圈淺綠光暈 */
}
.farm-item strong {
  color: var(--ink);
  font-size: 16px;
}
.farm-item span {
  color: var(--muted);
  font-size: 13px;
}
.farm-item small {
  color: var(--ink-soft);
  font-size: 13px;
  line-height: 1.5;
}
.farm-item em {
  color: var(--leaf-dark);
  font-size: 12px;
  font-style: normal;
}
.farm-empty {
  padding: 16px;
  color: var(--muted);
  text-align: center;
}

/* ---------- 右：地圖 ---------- */
.map-frame {
  min-height: 420px;
  border-radius: 14px;
  overflow: hidden;                 /* 讓 iframe 跟著圓角 */
  border: 1px solid var(--line);
}

.map-empty {
  display: grid;
  place-items: center;
  height: 100%;
  margin: 0;
  color: var(--muted);
}

/* ---------- 響應式：窄螢幕改成上下排 ---------- */
@media (max-width: 760px) {
  .map-layout {
    grid-template-columns: 1fr;     /* 單欄 */
  }
  .map-frame {
    min-height: 320px;
  }
}
</style>
