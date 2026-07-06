<script setup>
import { ref } from 'vue'

// 農場清單（示範資料）。
// 之後可以改成從 Spring Boot 抓回來：fetch('/api/farms').then(...)
// 現在先寫死，方便你先看到畫面、理解 v-for 怎麼用。
const farms = ref([
  {
    id: 1,
    name: '陽光有機農場',
    location: '台中・新社',
    desc: '無農藥栽培的當季時蔬，從產地直送到你家餐桌。',
    image: 'https://picsum.photos/seed/farm1/600/400',
  },
  {
    id: 2,
    name: '山城果園',
    location: '南投・信義',
    desc: '海拔 800 公尺的高山果園，主打甜度爆表的當令水果。',
    image: 'https://picsum.photos/seed/farm2/600/400',
  },
  {
    id: 3,
    name: '溪畔米鄉',
    location: '花蓮・玉里',
    desc: '純淨水源灌溉的稻田，孕育出粒粒分明的香Q好米。',
    image: 'https://picsum.photos/seed/farm3/600/400',
  },
  {
    id: 4,
    name: '牧野牛奶坊',
    location: '嘉義・中埔',
    desc: '自家牧場的乳牛，每日新鮮現擠、低溫殺菌的純鮮乳。',
    image: 'https://picsum.photos/seed/farm4/600/400',
  },
])
</script>

<template>
  <main class="farm-page">
    <!-- 頁面開頭的介紹區（hero） -->
    <section class="hero">
      <h1>🌱 嚴選合作農場</h1>
      <p>每一座農場，都是我們親自走訪、把關品質的好夥伴。</p>
    </section>

    <!--
      農場卡片清單。
      v-for：把 farms 陣列裡的每一筆，各渲染成一張 <article> 卡片。
      :key="farm.id"：給每筆一個唯一識別，Vue 用它來高效更新清單（一定要加）。
    -->
    <section class="farm-grid">
      <article v-for="farm in farms" :key="farm.id" class="farm-card">
        <img class="farm-card__img" :src="farm.image" :alt="farm.name" />

        <div class="farm-card__body">
          <!-- 用 {{ }} 把資料插進畫面 -->
          <h2 class="farm-card__name">{{ farm.name }}</h2>
          <p class="farm-card__loc">📍 {{ farm.location }}</p>
          <p class="farm-card__desc">{{ farm.desc }}</p>
          <button class="farm-card__btn" type="button">查看農場</button>
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
  margin-top: 4px;
  padding: 9px 0;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.farm-card__btn:hover {
  background: var(--leaf-dark);
}
</style>
