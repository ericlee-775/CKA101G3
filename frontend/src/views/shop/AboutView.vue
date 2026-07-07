<script setup>
// 「關於我們」頁面：介紹 Farmily 的品牌故事與核心價值。
// 純靜態內容，不用打 API；資料先寫在這裡，之後要改文案只動這個陣列即可。
const values = [
  { icon: '🌱', title: '產地直送', desc: '跳過中間商，蔬果從田裡採收後直接出貨，新鮮度看得見。' },
  { icon: '🤝', title: '支持小農', desc: '合理收購價格回饋生產者，讓在地小農能安心耕作、永續經營。' },
  { icon: '🔍', title: '來源透明', desc: '每項商品都標示產地與農場，吃進嘴裡的東西，來歷清清楚楚。' },
  { icon: '♻️', title: '友善環境', desc: '鼓勵友善耕作與減量包裝，把對土地的傷害降到最低。' },
]
</script>

<template>
  <main class="page">
    <!-- 頁首橫幅：品牌一句話 -->
    <section class="about-hero">
      <h1>🌾 關於我們</h1>
      <p class="about-slogan">新鮮直送・產地到餐桌</p>
    </section>

    <!-- 品牌故事 -->
    <section class="about-story">
      <h2>我們的故事</h2>
      <p>
        Farmily 誕生於一個簡單的想法：讓吃的人認識種的人。
        我們走訪台灣各地的小農，把他們細心照顧的蔬果、鮮乳與加工品，
        用最短的距離送到你的餐桌。
      </p>
      <p>
        在 Farmily，你買到的不只是食材，更是一份對土地與生產者的支持。
        每一筆訂單，都讓在地農業多一分繼續走下去的力量。
      </p>
    </section>

    <!-- 核心價值：v-for 依上面的 values 陣列渲染卡片 -->
    <section class="about-values">
      <h2>我們相信</h2>
      <div class="value-grid">
        <article v-for="v in values" :key="v.title" class="value-card">
          <span class="value-icon">{{ v.icon }}</span>
          <h3>{{ v.title }}</h3>
          <p>{{ v.desc }}</p>
        </article>
      </div>
    </section>

    <!-- 行動呼籲：導去逛商品 / 認識農場 -->
    <section class="about-cta">
      <h2>一起加入 Farmily</h2>
      <p>從一顆番茄、一把青菜開始，支持在地小農。</p>
      <div class="cta-actions">
        <router-link class="btn-primary" to="/products">逛逛商品</router-link>
        <router-link class="btn-ghost" to="/farmily">認識合作農場</router-link>
      </div>
    </section>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 960px;
  margin: 0 auto;
}

/* ===== 頁首橫幅 ===== */
.about-hero {
  text-align: center;
  padding: 24px 0 8px;
}
.about-hero h1 {
  margin: 0 0 8px;
  color: var(--ink);
}
.about-slogan {
  margin: 0;
  font-size: 18px;
  color: var(--leaf-dark);
}

/* ===== 共用：區塊標題 ===== */
section h2 {
  margin: 40px 0 16px;
  font-size: 22px;
  color: var(--ink);
}

/* ===== 品牌故事 ===== */
.about-story p {
  margin: 0 0 14px;
  font-size: 15px;
  line-height: 2;
  color: var(--ink-soft);
}

/* ===== 核心價值卡片 ===== */
.value-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 18px;
}
.value-card {
  background: #fff;
  border: 1px solid var(--line);
  border-top: 3px solid var(--leaf);
  border-radius: 12px;
  padding: 20px 18px;
}
.value-icon {
  font-size: 28px;
}
.value-card h3 {
  margin: 10px 0 8px;
  font-size: 16px;
  color: var(--ink);
}
.value-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--ink-soft);
}

/* ===== 行動呼籲 ===== */
.about-cta {
  text-align: center;
  background: var(--leaf-soft);
  border-radius: 16px;
  padding: 32px 20px;
  margin: 44px 0 16px;
}
.about-cta h2 {
  margin: 0 0 8px;
}
.about-cta p {
  margin: 0 0 20px;
  color: var(--ink-soft);
}
.cta-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}
.btn-primary,
.btn-ghost {
  display: inline-block;
  padding: 10px 22px;
  border-radius: 999px;
  font-size: 15px;
  text-decoration: none;
  transition: background 0.18s ease, color 0.18s ease;
}
.btn-primary {
  background: var(--leaf);
  color: #fff;
}
.btn-primary:hover {
  background: var(--leaf-dark);
}
.btn-ghost {
  border: 1px solid var(--leaf);
  color: var(--leaf-dark);
}
.btn-ghost:hover {
  background: var(--leaf);
  color: #fff;
}
</style>
