<script setup>
// 版權年份自動帶當年，不用每年手動改
const year = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <!-- 上半部：四個欄位（品牌 / 探索 / 關於 / 聯絡） -->
    <div class="footer-top">
      <!-- 品牌欄 -->
      <div class="footer-brand">
        <img
          class="footer-logo"
          src="https://storage.googleapis.com/cka101-15/form/farmLogo.png?v=20260613-transparent"
          alt="Farmily logo"
        />
        <strong>Farmily</strong>
        <p>新鮮直送・產地到餐桌。<br />與在地小農攜手，把最好的食材送到你家。</p>
      </div>

      <!-- 探索欄：用 router-link，點了會換頁 -->
      <nav class="footer-col">
        <h4>探索</h4>
        <router-link to="/farmily">合作農場</router-link>
        <router-link to="/products">全部商品</router-link>
        <router-link to="/group-buys">團購</router-link>
        <router-link to="/farm-map">產地地圖</router-link>
      </nav>

      <!-- 關於欄 -->
      <nav class="footer-col">
        <h4>關於</h4>
        <router-link to="/about">關於我們</router-link>
        <router-link to="/news">最新消息</router-link>
        <router-link to="/blogs">部落格</router-link>
        <router-link to="/farm-trips">體驗活動</router-link>
      </nav>

      <!-- 小農專區（與一般會員系統分開的獨立入口） -->
      <nav class="footer-col">
        <h4>小農專區</h4>
        <router-link to="/farmer/login">小農登入</router-link>
        <router-link to="/farmer/register">申請小農帳號</router-link>
      </nav>

      <!-- 聯絡欄 -->
      <div class="footer-col">
        <h4>聯絡我們</h4>
        <p>📧 service@farmily.com</p>
        <p>📞 (04) 1234-5678</p>
        <p>📍 台中市西區農夫路 101 號</p>

        <!-- 社群：先放外連，之後換成你的實際連結 -->
        <div class="footer-social">
          <a href="#" aria-label="Facebook">Ⓕ</a>
          <a href="#" aria-label="Instagram">📷</a>
          <a href="#" aria-label="LINE">💬</a>
        </div>
      </div>
    </div>

    <!-- 下半部：版權列 -->
    <div class="footer-bottom">
      <span>© {{ year }} Farmily. 版權所有。</span>
      <span class="footer-policy">
        <a href="#">隱私權政策</a>
        <a href="#">服務條款</a>
      </span>
    </div>
  </footer>
</template>

<style scoped>
/* ========== 整體頁尾 ========== */
.site-footer {
  margin-top: 48px;
  background: var(--ink);            /* 深色底，跟頁面內容區隔 */
  color: #d8e0d6;
  padding: 48px clamp(18px, 4vw, 56px) 0;
}

/* ========== 上半部欄位 ========== */
.footer-top {
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  /* 品牌欄較寬，中間連結欄平均，聯絡欄稍寬 */
  grid-template-columns: 1.6fr 1fr 1fr 1fr 1.4fr;
  gap: 32px;
}

/* 品牌欄 */
.footer-brand .footer-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
}
.footer-brand strong {
  display: block;
  margin: 8px 0 12px;
  font-size: 20px;
  color: #fff;
}
.footer-brand p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #aab8a6;
}

/* 連結欄 */
.footer-col h4 {
  margin: 0 0 14px;
  font-size: 15px;
  color: #fff;
}
.footer-col a,
.footer-col p {
  display: block;
  margin: 0 0 10px;
  font-size: 14px;
  color: #aab8a6;
  text-decoration: none;
  transition: color 0.18s ease;
}
.footer-col a:hover {
  color: var(--leaf-soft);           /* 滑過變亮綠 */
}

/* 社群 icon */
.footer-social {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}
.footer-social a {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border: 1px solid #46523f;
  border-radius: 50%;
  font-size: 16px;
  margin: 0;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.footer-social a:hover {
  background: var(--leaf);
  border-color: var(--leaf);
}

/* ========== 版權列 ========== */
.footer-bottom {
  max-width: 1100px;
  margin: 40px auto 0;
  padding: 20px 0;
  border-top: 1px solid #3a463440;  /* 半透明分隔線 */
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #8a978690;
}
.footer-policy {
  display: flex;
  gap: 18px;
}
.footer-policy a {
  color: #aab8a6;
  text-decoration: none;
  transition: color 0.18s ease;
}
.footer-policy a:hover {
  color: var(--leaf-soft);
}

/* ========== 響應式 ========== */
@media (max-width: 820px) {
  .footer-top {
    grid-template-columns: 1fr 1fr;  /* 兩欄 */
    gap: 28px;
  }
  .footer-brand {
    grid-column: 1 / -1;             /* 品牌欄獨佔一整列 */
  }
}
@media (max-width: 480px) {
  .footer-top {
    grid-template-columns: 1fr;      /* 單欄 */
  }
  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
