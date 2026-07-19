<script setup>
import { onMounted, ref } from 'vue'
import noImage from '@/assets/no-image.svg'
import { listFarms, normalizeFarm } from '@/api/farm'

const farms = ref([])
const loading = ref(true)
const error = ref('')

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
}

onMounted(loadFarms)
</script>

<template>
  <main class="farm-page">
    <!-- 頁面開頭的介紹區（hero） -->
    <section class="hero">
      <h1>🌱 嚴選合作農場</h1>
      <p>每一座農場，都是我們親自走訪、把關品質的好夥伴。</p>
    </section>

    <p v-if="loading" class="state">農場資料載入中…</p>

    <section v-else-if="error" class="state state--error">
      <p>載入失敗：{{ error }}</p>
      <button type="button" @click="loadFarms">重新載入</button>
    </section>

    <p v-else-if="farms.length === 0" class="state">目前還沒有可顯示的合作農場。</p>

    <section v-else class="farm-grid">
      <article v-for="farm in farms" :key="farm.farmerId" class="farm-card">
        <img class="farm-card__img" :src="`/farmily-web/farms/${farm.farmerId}.jpg`" :alt="farm.farmName" @error="$event.target.src = noImage" />

        <div class="farm-card__body">
          <h2 class="farm-card__name">{{ farm.farmName }}</h2>
          <p class="farm-card__loc">📍 {{ farm.location }}</p>
          <p class="farm-card__desc">{{ farm.desc }}</p>
          <RouterLink
            class="farm-card__btn"
            :to="{ name: 'farm-detail', params: { farmerId: farm.farmerId } }"
          >
            查看農場
          </RouterLink>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.farm-page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;          /* 內容置中 */
}

/* ---------- 開頭介紹 ---------- */
.hero {
  text-align: center;
  margin-bottom: 32px;
}
.hero h1 {
  font-size: 28px;
  color: var(--ink);
  margin: 0 0 8px;
}
.hero p {
  color: var(--muted);
  margin: 0;
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

/* ---------- 卡片格線 ---------- */
.farm-grid {
  display: grid;
  /* 每張卡至少 240px，空間夠就自動排成多欄、不夠就換行 */
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

/* ---------- 單張卡片 ---------- */
.farm-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;        /* 讓圖片的圓角跟卡片一致 */
  display: flex;
  flex-direction: column;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.farm-card:hover {
  transform: translateY(-4px);                 /* 滑過時微微浮起 */
  box-shadow: var(--shadow-hover);
}
.farm-card__img {
  width: 100%;
  height: 170px;
  object-fit: cover;       /* 圖片裁切填滿、不變形 */
  display: block;
}
.farm-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;                 /* 撐滿，讓每張卡按鈕對齊底部 */
}
.farm-card__name {
  font-size: 18px;
  color: var(--ink);
  margin: 0;
}
.farm-card__loc {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
}
.farm-card__desc {
  font-size: 14px;
  color: var(--ink-soft);
  line-height: 1.6;
  margin: 0;
  flex: 1;
}
.farm-card__btn {
  display: inline-flex;
  justify-content: center;
  margin-top: 4px;
  padding: 9px 0;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.18s ease;
}
.farm-card__btn:hover {
  background: var(--leaf-dark);
}
</style>
