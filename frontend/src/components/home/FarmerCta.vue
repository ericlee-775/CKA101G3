<!--
  首頁「加入小農夥伴」招募廣告。
  會員視角的內容都是淺色紙感,這一區刻意反色成深墨綠面板:
  一個對消費者說話、一個對農夫說話,色彩幫使用者分眾。
  右側是手繪月夜梯田 SVG(亮著燈的農舍:田邊有人還醒著)。
-->
<template>
  <section class="farmer-cta-section">
    <!-- data-cursor-invert:深色底上把全站自訂游標反轉成紙色(App.vue 會偵測) -->
    <div class="farmer-cta" data-cursor-invert>
      <!-- 品牌落款印章(線框版,和 header 實心印章區隔) -->
      <div class="farmer-cta__stamp" aria-hidden="true">你儂<br />我農</div>

      <div class="farmer-cta__content">
        <p class="farmer-cta__eyebrow">加入小農夥伴</p>

        <h2 class="farmer-cta__title">
          你把地種好，<br />
          賣的事交給我們。
        </h2>

        <p class="farmer-cta__sub">
          Farmily 陪你把田裡的東西，好好送到願意懂它的人手上——
          不必學行銷、不必顧客服，專心做你最擅長的事。
        </p>

        <ul class="farmer-cta__points">
          <li><strong>合理分潤</strong>透明結算，不玩上架費</li>
          <li><strong>上架輔導</strong>拍照、文案，有人陪你弄</li>
          <li><strong>產地故事</strong>你的名字，印在商品頁上</li>
        </ul>

        <div class="farmer-cta__actions">
          <router-link class="btn-seal" to="/farmer/register">申請成為夥伴</router-link>
          <router-link class="link-quiet" to="/farmily">看看合作中的小農 →</router-link>
        </div>
      </div>

      <!-- 手繪田景:月夜下的梯田與一盞燈 -->
      <div class="farmer-cta__art" aria-hidden="true">
        <svg viewBox="0 0 460 400" fill="none" preserveAspectRatio="xMaxYMax meet">
          <!-- 月亮 -->
          <circle cx="350" cy="80" r="26" stroke="#EDE8D8" stroke-width="2" opacity="0.85" />
          <path d="M338 74 Q350 66 362 74" stroke="#EDE8D8" stroke-width="1.4" opacity="0.4" />

          <!-- 遠山 -->
          <path d="M40 210 Q140 150 250 195 Q340 230 460 190"
                stroke="#4A6352" stroke-width="2" opacity="0.7" />

          <!-- 梯田等高線:一層層手繪弧線 -->
          <path d="M0 260 Q120 240 240 258 Q360 275 460 252"
                stroke="#6B8570" stroke-width="2" />
          <path d="M0 300 Q140 278 280 298 Q390 313 460 295"
                stroke="#6B8570" stroke-width="2" opacity="0.8" />
          <path d="M0 345 Q160 320 320 342 Q410 354 460 340"
                stroke="#6B8570" stroke-width="2" opacity="0.6" />

          <!-- 稻叢:幾撮短線 -->
          <g stroke="#8FA892" stroke-width="1.8" stroke-linecap="round" opacity="0.9">
            <path d="M120 256 L116 244 M120 256 L121 243 M120 256 L126 245" />
            <path d="M210 262 L206 250 M210 262 L211 249 M210 262 L216 251" />
            <path d="M320 268 L316 256 M320 268 L321 255 M320 268 L326 257" />
            <path d="M170 300 L166 289 M170 300 L171 288 M170 300 L176 290" />
            <path d="M290 306 L286 295 M290 306 L291 294 M290 306 L296 296" />
          </g>

          <!-- 小農舍與亮著的窗:田邊有人還醒著 -->
          <g stroke="#EDE8D8" stroke-width="2" stroke-linejoin="round">
            <path d="M368 232 L392 214 L416 232" />
            <path d="M372 232 L372 258 L412 258 L412 232" />
            <rect x="386" y="240" width="12" height="11" fill="#D9A441" stroke="#D9A441" />
          </g>
          <!-- 炊煙 -->
          <path d="M404 214 Q400 204 406 196 Q411 189 407 180"
                stroke="#EDE8D8" stroke-width="1.4" opacity="0.5" stroke-linecap="round" />
        </svg>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 外層:對齊首頁其他 section 的容器寬與左右留白 */
.farmer-cta-section {
  max-width: 1100px;
  margin: 64px auto 0;
  padding: 0 clamp(18px, 4vw, 56px);
}

/* ============ 招募面板(深墨綠,和淺色頁面反差分眾) ============ */
.farmer-cta {
  background: #27402f;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
}

/* ---- 左:文字區 ---- */
.farmer-cta__content {
  padding: 64px 24px 64px 64px;
  position: relative;
  z-index: 1;
}

.farmer-cta__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 20px;
  font-size: 0.85rem;
  letter-spacing: 0.3em;
  color: #ede8d8;
  opacity: 0.75;
}
.farmer-cta__eyebrow::before {
  content: '';
  width: 28px;
  height: 1px;
  background: #ede8d8;
  opacity: 0.6;
}

.farmer-cta__title {
  margin: 0 0 16px;
  font-family: 'Noto Serif TC', serif;
  font-size: clamp(1.5rem, 2.8vw, 2rem);
  font-weight: 600;
  line-height: 1.7;
  letter-spacing: 0.1em;
  color: #f5f1e6;
}

.farmer-cta__sub {
  margin: 0 0 32px;
  max-width: 30em;
  font-size: 0.95rem;
  line-height: 2;
  letter-spacing: 0.06em;
  color: #ede8d8;
  opacity: 0.85;
}

/* ---- 三個承諾:小字橫排,每點前一顆手繪穀粒 ---- */
.farmer-cta__points {
  display: flex;
  gap: 28px;
  margin: 0 0 40px;
  padding: 0;
  list-style: none;
}
.farmer-cta__points li {
  position: relative;
  padding-left: 28px;
  font-size: 0.88rem;
  letter-spacing: 0.08em;
  line-height: 1.8;
  color: #ede8d8;
}
/* 穀粒 bullet:內嵌 SVG,稻穗金描邊 */
.farmer-cta__points li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.45em;
  width: 14px;
  height: 14px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 14 14'%3E%3Cpath d='M7 1.5 Q10.5 4 10 7.5 Q9.5 11.5 7 12.5 Q4.5 11.5 4 7.5 Q3.5 4 7 1.5 Z' fill='none' stroke='%23D9A441' stroke-width='1.4' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
}
.farmer-cta__points strong {
  display: block;
  font-weight: 600;
  font-size: 0.95rem;
  color: #f5f1e6;
}

/* ---- 按鈕 ---- */
.farmer-cta__actions {
  display: flex;
  align-items: center;
  gap: 24px;
}
/* 主行動:印泥紅實心鈕(和品牌印章同色) */
.btn-seal {
  display: inline-block;
  background: #a94438;
  color: #f5f1e6;
  font-size: 1rem;
  letter-spacing: 0.2em;
  text-decoration: none;
  padding: 13px 36px 13px calc(36px + 0.2em); /* 補回 letter-spacing 造成的視覺偏移 */
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.25s ease, transform 0.25s ease;
}
.btn-seal:hover {
  background: #8f3a2f;
  transform: translateY(-2px);
}
.btn-seal:focus-visible {
  outline: 2px solid #f5f1e6;
  outline-offset: 3px;
}
/* 次行動:低調底線連結 */
.link-quiet {
  font-size: 0.9rem;
  letter-spacing: 0.1em;
  color: #ede8d8;
  opacity: 0.8;
  text-decoration: none;
  border-bottom: 1px solid #4a6352;
  padding-bottom: 2px;
  transition: opacity 0.25s ease, border-color 0.25s ease;
}
.link-quiet:hover {
  opacity: 1;
  border-color: #ede8d8;
}

/* ---- 右:手繪田景插畫 ---- */
.farmer-cta__art {
  position: relative;
  min-height: 320px;
}
.farmer-cta__art svg {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
}

/* ---- 右上角的小印章:線框落款(和 header 實心印章區隔) ---- */
.farmer-cta__stamp {
  position: absolute;
  top: 28px;
  right: 28px;
  width: 52px;
  height: 52px;
  border: 2px solid #a94438;
  border-radius: 4px;
  color: #a94438;
  font-family: 'Noto Serif TC', serif;
  font-size: 0.95rem;
  line-height: 1.25;
  letter-spacing: 0.05em;
  display: grid;
  place-items: center;
  text-align: center;
  transform: rotate(4deg);
  opacity: 0.9;
  z-index: 2;
  user-select: none;
}

/* ============ 響應式 ============ */
@media (max-width: 860px) {
  .farmer-cta {
    grid-template-columns: 1fr;      /* 直向:文字在上、插畫在下 */
  }
  .farmer-cta__content {
    padding: 48px 28px 8px;
  }
  .farmer-cta__points {
    flex-direction: column;
    gap: 14px;
  }
  .farmer-cta__art {
    min-height: 220px;
  }
  .farmer-cta__stamp {
    top: 20px;
    right: 20px;
    width: 46px;
    height: 46px;
    font-size: 0.85rem;
  }
}
</style>
