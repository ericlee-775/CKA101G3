<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import noImage from '@/assets/no-image.svg'
import { listFarms, normalizeFarm } from '@/api/farm'

const farms = ref([])
const loading = ref(true)
const error = ref('')

const selectedRegion = ref('全部')
const selectedFarm = ref(null)     // 目前彈出小卡的農場（null = 不顯示）

const regions = computed(() => {
  const values = farms.value.map(f => f.region).filter(Boolean)
  return ['全部', ...new Set(values)]
})

// 有座標才能標在地圖上
const filteredFarms = computed(() => {
  const list = selectedRegion.value === '全部'
    ? farms.value
    : farms.value.filter(f => f.region === selectedRegion.value)
  return list
})
const mappableFarms = computed(() =>
  filteredFarms.value.filter(f => f.locLat != null && f.locLong != null)
)

// ---- Leaflet ----
const mapEl = ref(null)
let map = null
let markerLayer = null
const markersById = {}

// CSS pin（避開 Leaflet 預設圖示打包路徑問題）
function pinIcon(active) {
  return L.divIcon({
    className: `farm-pin${active ? ' is-active' : ''}`,
    html: '<span class="farm-pin__dot"></span>',
    iconSize: [26, 26],
    iconAnchor: [13, 26],
  })
}

function initMap() {
  if (map || !mapEl.value) return
  map = L.map(mapEl.value, { scrollWheelZoom: true }).setView([23.75, 121.0], 7)
  // OSM 標準圖磚：用當地語言，台灣顯示中文地名（CARTO 那組是英文/拉丁化）
  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap',
    maxZoom: 19,
  }).addTo(map)
  markerLayer = L.layerGroup().addTo(map)
  renderMarkers()

  // Leaflet 若在容器尺寸還沒定時初始化，會不載圖磚 → 下一幀/稍後強制重算並重新定位
  const settle = () => {
    if (!map) return
    map.invalidateSize()
    fitToMarkers()
  }
  requestAnimationFrame(settle)
  setTimeout(settle, 300)
}

function renderMarkers() {
  if (!markerLayer) return
  markerLayer.clearLayers()
  for (const key of Object.keys(markersById)) delete markersById[key]

  for (const farm of mappableFarms.value) {
    const lat = Number(farm.locLat)
    const lng = Number(farm.locLong)
    const marker = L.marker([lat, lng], {
      icon: pinIcon(selectedFarm.value?.farmerId === farm.farmerId),
      title: farm.farmName,
    })
    marker.on('click', () => selectFarm(farm))
    marker.addTo(markerLayer)
    markersById[farm.farmerId] = marker
  }
}

// 把所有釘點框進畫面（尺寸正確後才呼叫才準）
function fitToMarkers() {
  const points = mappableFarms.value
    .filter(f => f.locLat != null && f.locLong != null)
    .map(f => [Number(f.locLat), Number(f.locLong)])
  if (points.length && map) {
    map.fitBounds(points, { padding: [50, 50], maxZoom: 12 })
  }
}

// 更新釘點外觀（highlight 目前選中的）
function refreshPins() {
  for (const [id, marker] of Object.entries(markersById)) {
    marker.setIcon(pinIcon(Number(id) === selectedFarm.value?.farmerId))
  }
}

function selectFarm(farm) {
  selectedFarm.value = farm
  refreshPins()
  const marker = markersById[farm.farmerId]
  if (marker && map) {
    map.flyTo(marker.getLatLng(), Math.max(map.getZoom(), 11), { duration: 0.6 })
  }
}

function closeCard() {
  selectedFarm.value = null
  refreshPins()
}

async function loadFarms() {
  loading.value = true
  error.value = ''
  try {
    const data = await listFarms()
    farms.value = data.map(normalizeFarm)
  } catch (e) {
    error.value = e.message || '無法載入農場資料，請稍後再試。'
  } finally {
    loading.value = false
  }
  // 等 loading=false、地圖容器(v-else)真的渲染出來後再初始化
  if (farms.value.length) {
    await nextTick()
    initMap()
  }
}

// 換地區 → 重畫釘點；若目前選中的農場不在新清單，關掉小卡
watch(mappableFarms, () => {
  if (!map) return
  renderMarkers()
  fitToMarkers()
  if (selectedFarm.value &&
      !mappableFarms.value.some(f => f.farmerId === selectedFarm.value.farmerId)) {
    selectedFarm.value = null
  }
})

onMounted(loadFarms)
onBeforeUnmount(() => { if (map) { map.remove(); map = null } })
</script>

<template>
  <main class="map-page">
    <!-- Hero：與「全部商品」同款規格(emoji + 標題同行、置中) -->
    <section class="hero">
      <h1>🗺️ 產地地圖</h1>
      <p>點選農場釘點或左側清單，看看你的食材來自台灣哪個角落。</p>
    </section>

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
        >{{ region }}</button>
      </div>

      <div class="map-layout">
        <!-- 左：農場清單 -->
        <ul class="farm-list">
          <li
            v-for="farm in filteredFarms"
            :key="farm.farmerId"
            class="farm-item"
            :class="{ 'is-active': selectedFarm?.farmerId === farm.farmerId }"
            @click="selectFarm(farm)"
          >
            <strong>{{ farm.farmName }}</strong>
            <span>📍 {{ farm.location }}</span>
            <small v-if="farm.farmDesc">{{ farm.farmDesc }}</small>
            <em v-if="farm.locLat == null" class="no-geo">（尚無座標，無法定位）</em>
          </li>
          <li v-if="filteredFarms.length === 0" class="farm-empty">
            這個地區目前還沒有合作農場
          </li>
        </ul>

        <!-- 右：地圖 + 彈出小卡 -->
        <div class="map-wrap">
          <div ref="mapEl" class="map-frame"></div>

          <div v-if="selectedFarm" class="farm-card">
            <button class="farm-card__close" type="button" @click="closeCard" aria-label="關閉">✕</button>
            <div class="farm-card__banner">
              <img
                :src="`/farmily-web/farms/${selectedFarm.farmerId}.jpg`"
                :alt="selectedFarm.farmName"
                @error="$event.target.src = noImage"
              />
            </div>
            <div class="farm-card__body">
              <h3>{{ selectedFarm.farmName }}</h3>
              <p class="farm-card__loc">📍 {{ selectedFarm.location }}</p>
              <p class="farm-card__desc">{{ selectedFarm.farmDesc || '這座農場尚未提供介紹。' }}</p>
              <RouterLink
                class="farm-card__btn"
                :to="{ name: 'farm-detail', params: { farmerId: selectedFarm.farmerId } }"
              >前往農場專頁 →</RouterLink>
            </div>
          </div>
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
/* ===== Hero（與「全部商品」ProductsView 同款：emoji 同行、置中）===== */
.hero { text-align: center; margin-bottom: 32px; }
.hero h1 { font-size: 28px; color: var(--ink); margin: 0 0 8px; }
.hero p { color: var(--muted); margin: 0; }

.state { padding: 44px 0; text-align: center; color: var(--muted); }
.state--error { color: #b42318; }
.state--error button {
  margin-top: 12px; padding: 9px 18px; border: 1px solid var(--line);
  border-radius: 999px; background: #fff; color: var(--ink); cursor: pointer;
}

/* 地區篩選 */
.region-tabs { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; margin-bottom: 20px; }
.region-tab {
  padding: 8px 18px; border: 1px solid var(--line); border-radius: 999px;
  background: #fff; color: var(--ink-soft); font-size: 14px; cursor: pointer;
  transition: background .18s, color .18s, border-color .18s;
}
.region-tab:hover { border-color: var(--leaf); color: var(--leaf); }
.region-tab.is-active { background: var(--leaf); border-color: var(--leaf); color: #fff; }

/* 版面：左清單 + 右地圖 */
.map-layout { display: grid; grid-template-columns: 280px 1fr; gap: 20px; }

.farm-list {
  list-style: none; margin: 0; padding: 0;
  display: flex; flex-direction: column; gap: 10px;
  max-height: 560px; overflow-y: auto;
}
.farm-item {
  display: flex; flex-direction: column; gap: 4px; padding: 14px 16px;
  border: 1px solid var(--line); border-radius: 12px; background: #fff; cursor: pointer;
  transition: border-color .18s, box-shadow .18s;
}
.farm-item:hover { border-color: var(--leaf); }
.farm-item.is-active { border-color: var(--leaf); box-shadow: 0 0 0 2px var(--leaf-soft); }
.farm-item strong { color: var(--ink); font-size: 16px; }
.farm-item span { color: var(--muted); font-size: 13px; }
.farm-item small { color: var(--ink-soft); font-size: 13px; line-height: 1.5; }
.no-geo { color: var(--muted); font-size: 12px; font-style: normal; }
.farm-empty { padding: 16px; color: var(--muted); text-align: center; }

/* 地圖 */
/* z-index:0 建立一個低層級 stacking context，把 Leaflet 內部(圖層/控制項 z-index 高達 1000)
   全部關在這層裡，捲動時才不會壓過 sticky header(z-index:30) 把上排導覽選項蓋掉 */
.map-wrap { position: relative; z-index: 0; }
.map-frame {
  height: 560px; border-radius: 14px; overflow: hidden; border: 1px solid var(--line);
  background: var(--leaf-soft);
}

/* 彈出小卡（浮在地圖上，比照參考圖） */
.farm-card {
  position: absolute; top: 18px; left: 18px; z-index: 500;
  width: min(320px, calc(100% - 36px));
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: 0 12px 30px rgba(30,25,15,.22); overflow: hidden;
}
.farm-card__close {
  position: absolute; top: 10px; right: 10px; z-index: 2;
  width: 30px; height: 30px; border: none; border-radius: 50%;
  background: #0006; color: #fff; cursor: pointer; font-size: 14px;
}
.farm-card__close:hover { background: #0009; }
.farm-card__banner { height: 130px; background: var(--leaf-soft); }
.farm-card__banner img { width: 100%; height: 100%; object-fit: cover; display: block; }
.farm-card__body { padding: 16px 18px 18px; }
.farm-card__body h3 { margin: 0 0 6px; color: var(--ink); font-size: 19px; }
.farm-card__loc { margin: 0 0 10px; color: var(--muted); font-size: 13px; }
.farm-card__desc {
  margin: 0 0 16px; color: var(--ink-soft); font-size: 14px; line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}
.farm-card__btn {
  display: block; text-align: center; padding: 11px; border-radius: 10px;
  background: var(--leaf); color: #fff; font-weight: 700; text-decoration: none;
  transition: background .18s;
}
.farm-card__btn:hover { background: var(--leaf-dark); }

@media (max-width: 760px) {
  .map-layout { grid-template-columns: 1fr; }
  .farm-list { max-height: none; }
  .map-frame { height: 420px; }
}
</style>

<!-- 非 scoped：Leaflet 動態產生的釘點 DOM 不在 scoped 範圍內 -->
<style>
.farm-pin { display: grid; place-items: center; }
.farm-pin__dot {
  width: 18px; height: 18px; border-radius: 50% 50% 50% 0;
  background: #3b8a57; border: 2px solid #fff; transform: rotate(-45deg);
  box-shadow: 0 2px 5px rgba(0,0,0,.35); transition: transform .15s, background .15s;
}
.farm-pin.is-active .farm-pin__dot {
  background: #d1461f; width: 24px; height: 24px;
}
.leaflet-container { font: inherit; }
</style>
