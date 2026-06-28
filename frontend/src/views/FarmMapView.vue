<script setup>
import { ref, computed } from 'vue'

// 產地清單（示範資料）。query 是給 Google 地圖定位用的關鍵字。
// 之後一樣可以改成從 Spring Boot 的 /api/farms 抓回來。
const farms = ref([
  { id: 1, name: '陽光有機農場', region: '中部', location: '台中・新社', query: '台中市新社區' },
  { id: 2, name: '山城果園',     region: '中部', location: '南投・信義', query: '南投縣信義鄉' },
  { id: 3, name: '溪畔米鄉',     region: '東部', location: '花蓮・玉里', query: '花蓮縣玉里鎮' },
  { id: 4, name: '牧野牛奶坊',   region: '南部', location: '嘉義・中埔', query: '嘉義縣中埔鄉' },
  { id: 5, name: '北海漁村',     region: '北部', location: '宜蘭・頭城', query: '宜蘭縣頭城鎮' },
])

// 所有地區（固定選項）
const regions = ['全部', '北部', '中部', '南部', '東部']

// 目前選到的地區（預設「全部」）。ref = 會變動、且變動時畫面自動更新的狀態。
const selectedRegion = ref('全部')

// 目前選到、要顯示在地圖上的農場（預設第一筆）。
const selectedFarm = ref(farms.value[0])

/*
  computed（計算屬性）：根據 selectedRegion 自動算出「要顯示哪些農場」。
  好處：selectedRegion 一變，這個清單自動重算，不用自己手動更新。
*/
const filteredFarms = computed(() => {
  if (selectedRegion.value === '全部') return farms.value
  return farms.value.filter(farm => farm.region === selectedRegion.value)
})

/*
  地圖網址也用 computed：selectedFarm 一變，地圖 src 自動換 → 地圖重新定位。
  output=embed 讓 Google 地圖可以直接內嵌，不需要 API 金鑰。
*/
const mapSrc = computed(
  () => `https://www.google.com/maps?q=${encodeURIComponent(selectedFarm.value.query)}&output=embed`
)
</script>

<template>
  <main class="map-page">
    <header class="map-hero">
      <h1>🗺️ 產地地圖</h1>
      <p>點選左側農場，看看你的食材來自台灣哪個角落。</p>
    </header>

    <!-- 地區篩選列 -->
    <div class="region-tabs">
      <!--
        v-for 產生每個地區按鈕。
        :class 綁定：目前選到的地區加上 is-active 樣式。
        @click：點一下就把 selectedRegion 換成這個地區 → filteredFarms 自動重算。
      -->
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

    <!-- 左清單 + 右地圖 -->
    <div class="map-layout">
      <!-- 左：農場清單 -->
      <ul class="farm-list">
        <li
          v-for="farm in filteredFarms"
          :key="farm.id"
          class="farm-item"
          :class="{ 'is-active': selectedFarm.id === farm.id }"
          @click="selectedFarm = farm"
        >
          <strong>{{ farm.name }}</strong>
          <span>📍 {{ farm.location }}</span>
        </li>

        <!-- v-if：萬一某地區沒有農場，顯示提示 -->
        <li v-if="filteredFarms.length === 0" class="farm-empty">
          這個地區目前還沒有合作農場
        </li>
      </ul>

      <!-- 右：地圖（:src 綁定 computed，選不同農場就重新定位） -->
      <div class="map-frame">
        <iframe
          :src="mapSrc"
          width="100%"
          height="100%"
          style="border: 0"
          loading="lazy"
          referrerpolicy="no-referrer-when-downgrade"
        ></iframe>
      </div>
    </div>
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
