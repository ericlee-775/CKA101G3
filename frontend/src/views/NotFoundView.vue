<script setup>
// 404 頁面：任何對不到路由的網址都會被 router 的 catch-all 導到這裡。
// 整頁自帶背景與「回首頁」按鈕，所以不套 ShopLayout，直接當獨立全螢幕頁面。
</script>

<template>
  <!-- .notfound 釘滿整個視窗（等同原 HTML 的 <body>），overflow:hidden 讓滑鼠滾輪無法捲動 -->
  <main class="notfound">
    <div class="scene">
      <div class="sunset" aria-hidden="true"></div>
      <div class="bird b1" aria-hidden="true"></div>
      <div class="bird b2" aria-hidden="true"></div>

      <div class="hills" aria-hidden="true"></div>

      <div class="firefly f1" aria-hidden="true"></div>
      <div class="firefly f2" aria-hidden="true"></div>
      <div class="firefly f3" aria-hidden="true"></div>
      <div class="firefly f4" aria-hidden="true"></div>

      <!-- 木架吊牌 404 -->
      <div class="signpost" role="img" aria-label="404 找不到頁面">
        <div class="beam" aria-hidden="true"></div>
        <div class="board" aria-hidden="true">
          <div class="num">404</div>
        </div>
      </div>

      <p class="title">連稻草人也找不到這個頁面</p>
      <p class="subtitle">它可能已經收成完畢、悄悄下架了。<br>天色不早,回市集看看今天的新鮮貨吧!</p>

      <router-link class="home-btn" to="/">回到市集首頁</router-link>

      <!-- 稻草人與烏鴉 -->
      <div class="scarecrow" aria-hidden="true">
        <div class="pole"></div>
        <div class="cross"></div>
        <div class="arm left"></div>
        <div class="arm right"></div>
        <div class="shirt"></div>
        <div class="head">
          <span class="s-eye left"></span>
          <span class="s-eye right"></span>
          <span class="s-mouth"></span>
        </div>
        <div class="hat-brim"></div>
        <div class="hat-top"></div>
        <div class="crow">
          <span class="c-tail"></span>
          <span class="c-body"></span>
          <span class="c-head"></span>
          <span class="c-eye"></span>
          <span class="c-beak"></span>
        </div>
      </div>

      <!-- 前景麥田 -->
      <div class="wheat-field" aria-hidden="true"></div>
      <div class="stalk w1" aria-hidden="true"></div>
      <div class="stalk w2" aria-hidden="true"></div>
      <div class="stalk w3" aria-hidden="true"></div>
      <div class="stalk w4" aria-hidden="true"></div>
      <div class="stalk w5" aria-hidden="true"></div>
      <div class="stalk w6" aria-hidden="true"></div>
      <div class="stalk w7" aria-hidden="true"></div>
    </div>
  </main>
</template>

<style scoped>
/* 主題色變數掛在元件容器上（原檔放 :root，改到這裡以免覆蓋全站 theme.css 的 --ink 等變數） */
.notfound {
  --sky-top: #FFE8C2;
  --sky-mid: #FFD096;
  --sky-low: #F7B57C;
  --hill: #6E8F5E;
  --hill-dark: #55744A;
  --wheat: #E3B45C;
  --wheat-dark: #C9973F;
  --wood: #9C6B43;
  --wood-dark: #7C5233;
  --ink: #4A3826;
  --ink-soft: #86735C;
  --straw: #E8C878;
  --shirt: #C0563F;
  --patch: #5E86A8;

  font-family: 'PingFang TC', 'Microsoft JhengHei', 'Noto Sans TC', sans-serif;
  /* 直接釘在視窗四邊：整頁滿版、不受任何外層容器影響，overflow:hidden 讓滾輪動不了 */
  position: fixed;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(to bottom, var(--sky-top) 0%, var(--sky-mid) 45%, var(--sky-low) 70%);
  color: var(--ink);
}

.notfound * { margin: 0; padding: 0; box-sizing: border-box; }

.scene {
  position: relative;
  width: 100%;
  max-width: 960px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 20px 150px;
  text-align: center;
}

/* ---------- 夕陽 ---------- */
.sunset {
  position: absolute;
  top: 12%; left: 50%;
  width: 190px; height: 190px;
  background: radial-gradient(circle, #FFF2D0 0%, #FFDC9A 55%, transparent 72%);
  border-radius: 50%;
  transform: translateX(-50%);
  animation: sunset-breathe 6s ease-in-out infinite;
  pointer-events: none;
}
@keyframes sunset-breathe {
  0%, 100% { opacity: .85; scale: 1; }
  50%      { opacity: 1;   scale: 1.08; }
}

/* ---------- 遠方飛鳥 ---------- */
.bird {
  position: absolute;
  width: 26px; height: 10px;
  animation: bird-fly linear infinite;
}
.bird::before, .bird::after {
  content: '';
  position: absolute;
  top: 0;
  width: 14px; height: 10px;
  border: 2.5px solid var(--ink);
  border-bottom: none;
  border-left: none;
  border-radius: 0 100% 0 0;
  opacity: .55;
}
.bird::before { left: 0;  transform: scaleX(-1); }
.bird::after  { right: 0; }
.bird.b1 { top: 16%; left: -60px; animation-duration: 30s; }
.bird.b2 { top: 11%; left: -120px; animation-duration: 38s; animation-delay: -12s; scale: .7; }
@keyframes bird-fly {
  from { transform: translateX(0) translateY(0); }
  50%  { transform: translateX(50vw) translateY(-24px); }
  to   { transform: translateX(calc(100vw + 200px)) translateY(0); }
}

/* ---------- 螢火蟲 ---------- */
.firefly {
  position: absolute;
  width: 7px; height: 7px;
  background: #FFF3B0;
  border-radius: 50%;
  box-shadow: 0 0 12px 4px rgba(255, 236, 150, .8);
  animation: glow ease-in-out infinite, hover-float ease-in-out infinite;
}
.firefly.f1 { bottom: 30%; left: 14%; animation-duration: 3s, 7s; }
.firefly.f2 { bottom: 42%; left: 82%; animation-duration: 4s, 9s; animation-delay: -1s, -3s; }
.firefly.f3 { bottom: 22%; left: 70%; animation-duration: 3.5s, 8s; animation-delay: -2s, -5s; scale: .8; }
.firefly.f4 { bottom: 50%; left: 8%;  animation-duration: 5s, 10s; animation-delay: -.5s, -2s; scale: .7; }
@keyframes glow {
  0%, 100% { opacity: .15; }
  50%      { opacity: 1; }
}
@keyframes hover-float {
  0%, 100% { transform: translate(0, 0); }
  33%      { transform: translate(18px, -22px); }
  66%      { transform: translate(-14px, -8px); }
}

/* ---------- 木架 + 吊牌 404 ---------- */
.signpost {
  position: relative;
  width: min(560px, 88vw);
  margin-bottom: 6px;
}
.beam {
  position: relative;
  height: 20px;
  background: linear-gradient(to bottom, var(--wood), var(--wood-dark));
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(74, 56, 38, .25);
}
.beam::before, .beam::after {
  content: '';
  position: absolute;
  top: 4px;
  width: 8px; height: 12px;
  background: rgba(0,0,0,.18);
  border-radius: 50%;
}
.beam::before { left: 12%; }
.beam::after  { right: 12%; }

.board {
  position: relative;
  margin: 0 auto;
  width: min(420px, 74vw);
  padding: 14px 0 20px;
  background:
    repeating-linear-gradient(to right, transparent 0 118px, rgba(0,0,0,.12) 118px 122px),
    linear-gradient(to bottom, #B27C4F, var(--wood) 40%, var(--wood-dark));
  border-radius: 14px;
  box-shadow: inset 0 0 0 5px rgba(74,56,38,.35), 0 14px 26px rgba(74, 56, 38, .28);
  transform-origin: top center;
  animation: swing 4.5s ease-in-out infinite;
}
/* 吊繩 */
.board::before, .board::after {
  content: '';
  position: absolute;
  top: -34px;
  width: 5px; height: 36px;
  background: #C9A268;
  border-radius: 3px;
  rotate: 0deg;
}
.board::before { left: 15%; rotate: 6deg; }
.board::after  { right: 15%; rotate: -6deg; }
@keyframes swing {
  0%, 100% { transform: rotate(-2.4deg); }
  50%      { transform: rotate(2.4deg); }
}

.board .num {
  font-size: clamp(88px, 18vw, 150px);
  font-weight: 900;
  line-height: 1;
  color: #FFEFD2;
  text-shadow:
    0 3px 0 rgba(74,56,38,.5),
    0 -1px 0 rgba(255,255,255,.25);
  letter-spacing: 6px;
}

/* ---------- 稻草人 ---------- */
.scarecrow {
  position: absolute;
  bottom: 96px;
  right: 6%;
  width: 150px;
  height: 260px;
  transform-origin: bottom center;
  animation: sway 5s ease-in-out infinite;
  z-index: 1;
}
@keyframes sway {
  0%, 100% { rotate: -1.6deg; }
  50%      { rotate: 1.8deg; }
}
/* 主桿與橫桿 */
.scarecrow .pole {
  position: absolute;
  bottom: 0; left: 50%;
  width: 12px; height: 210px;
  background: linear-gradient(to right, var(--wood), var(--wood-dark));
  transform: translateX(-50%);
  border-radius: 6px;
}
.scarecrow .cross {
  position: absolute;
  top: 96px; left: 50%;
  width: 148px; height: 11px;
  background: linear-gradient(to bottom, var(--wood), var(--wood-dark));
  transform: translateX(-50%);
  border-radius: 6px;
}
/* 衣服 */
.scarecrow .shirt {
  position: absolute;
  top: 88px; left: 50%;
  width: 84px; height: 78px;
  background: var(--shirt);
  transform: translateX(-50%);
  border-radius: 16px 16px 10px 10px;
  box-shadow: inset -10px -8px 0 rgba(0,0,0,.12);
}
.scarecrow .shirt::before {
  content: '';
  position: absolute;
  bottom: 12px; left: 12px;
  width: 20px; height: 16px;
  background: var(--patch);
  border-radius: 4px;
  rotate: -8deg;
}
/* 袖子 */
.scarecrow .arm {
  position: absolute;
  top: 92px;
  width: 44px; height: 20px;
  background: var(--shirt);
  border-radius: 10px;
}
.scarecrow .arm.left  { left: -8px;  rotate: 6deg; }
.scarecrow .arm.right { right: -8px; rotate: -6deg; }
/* 袖口稻草 */
.scarecrow .arm::after {
  content: '';
  position: absolute;
  top: 3px;
  width: 16px; height: 14px;
  background:
    linear-gradient(to bottom, transparent 30%, var(--straw) 30%);
  clip-path: polygon(0 0, 20% 100%, 35% 10%, 55% 100%, 70% 5%, 88% 100%, 100% 0);
}
.scarecrow .arm.left::after  { left: -14px; }
.scarecrow .arm.right::after { right: -14px; scale: -1 1; }
/* 頭:麻布袋 */
.scarecrow .head {
  position: absolute;
  top: 34px; left: 50%;
  width: 62px; height: 58px;
  background: #EDD9A8;
  transform: translateX(-50%);
  border-radius: 46% 46% 42% 42%;
  box-shadow: inset -8px -6px 0 rgba(0,0,0,.08);
}
.scarecrow .head .s-eye {
  position: absolute;
  top: 24px;
  width: 8px; height: 8px;
  background: var(--ink);
  border-radius: 50%;
}
.scarecrow .head .s-eye.left  { left: 15px; }
.scarecrow .head .s-eye.right { right: 15px; }
.scarecrow .head .s-mouth {
  position: absolute;
  bottom: 12px; left: 50%;
  width: 22px; height: 3px;
  background: var(--ink);
  transform: translateX(-50%);
  border-radius: 3px;
}
/* 帽子 */
.scarecrow .hat-top {
  position: absolute;
  top: 8px; left: 50%;
  width: 46px; height: 32px;
  background: linear-gradient(to bottom, var(--straw), #D3B25E);
  transform: translateX(-50%);
  border-radius: 40% 40% 8% 8%;
}
.scarecrow .hat-brim {
  position: absolute;
  top: 34px; left: 50%;
  width: 86px; height: 12px;
  background: #D3B25E;
  transform: translateX(-50%) rotate(-2deg);
  border-radius: 50%;
}

/* ---------- 烏鴉 ---------- */
.crow {
  position: absolute;
  top: 78px; left: -22px;
  width: 40px; height: 30px;
  animation: crow-hop 3.4s ease-in-out infinite;
  transform-origin: bottom center;
}
@keyframes crow-hop {
  0%, 55%, 100% { transform: translateY(0) rotate(0); }
  62%           { transform: translateY(-9px) rotate(-8deg); }
  70%           { transform: translateY(0) rotate(0); }
  82%           { transform: rotate(10deg); }  /* 歪頭 */
  92%           { transform: rotate(0); }
}
.crow .c-body {
  position: absolute;
  bottom: 4px; left: 0;
  width: 32px; height: 22px;
  background: #3A3F4A;
  border-radius: 60% 55% 45% 45%;
}
.crow .c-head {
  position: absolute;
  bottom: 18px; right: -2px;
  width: 17px; height: 15px;
  background: #3A3F4A;
  border-radius: 50%;
}
.crow .c-beak {
  position: absolute;
  bottom: 22px; right: -11px;
  width: 0; height: 0;
  border-left: 11px solid #E8A03C;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}
.crow .c-eye {
  position: absolute;
  bottom: 26px; right: 3px;
  width: 4px; height: 4px;
  background: #FFF;
  border-radius: 50%;
}
.crow .c-tail {
  position: absolute;
  bottom: 12px; left: -10px;
  width: 16px; height: 8px;
  background: #3A3F4A;
  border-radius: 4px;
  rotate: -18deg;
}

/* ---------- 文案 ---------- */
.title {
  margin-top: 34px;
  font-size: clamp(22px, 4vw, 32px);
  font-weight: 800;
}
.subtitle {
  margin-top: 12px;
  font-size: clamp(14px, 2.4vw, 18px);
  color: var(--ink-soft);
  line-height: 1.7;
}
.home-btn {
  display: inline-block;
  margin-top: 26px;
  padding: 14px 46px;
  background: var(--ink);
  color: #FFEFD2;
  font-size: 17px;
  font-weight: 700;
  text-decoration: none;
  border-radius: 999px;
  box-shadow: 0 6px 0 #33261A;
  transition: transform .15s ease, box-shadow .15s ease;
}
.home-btn:hover  { transform: translateY(-3px); box-shadow: 0 9px 0 #33261A; }
.home-btn:active { transform: translateY(2px);  box-shadow: 0 3px 0 #33261A; }
.home-btn:focus-visible { outline: 3px solid var(--shirt); outline-offset: 3px; }

/* ---------- 遠山 ---------- */
.hills {
  position: absolute;
  bottom: 88px; left: 0;
  width: 100%; height: 120px;
  pointer-events: none;
}
.hills::before, .hills::after {
  content: '';
  position: absolute;
  bottom: 0;
  border-radius: 50% 50% 0 0;
  background: var(--hill);
  opacity: .55;
}
.hills::before { left: -12%; width: 66%; height: 100%; }
.hills::after  { right: -14%; width: 72%; height: 78%; background: var(--hill-dark); }

/* ---------- 前景麥田 ---------- */
.wheat-field {
  position: absolute;
  bottom: 0; left: 0;
  width: 100%; height: 96px;
  background: linear-gradient(to bottom, var(--wheat), var(--wheat-dark));
  border-radius: 60% 60% 0 0 / 26px 26px 0 0;
  z-index: 2;
}
.stalk {
  position: absolute;
  bottom: 78px;
  width: 5px; height: 52px;
  background: var(--wheat-dark);
  border-radius: 4px;
  transform-origin: bottom center;
  animation: stalk-sway ease-in-out infinite;
  z-index: 2;
}
/* 麥穗 */
.stalk::before {
  content: '';
  position: absolute;
  top: -20px; left: 50%;
  width: 14px; height: 30px;
  background: var(--wheat);
  transform: translateX(-50%);
  border-radius: 50% 50% 40% 40%;
  box-shadow:
    inset 0 4px 0 rgba(255,255,255,.28),
    inset 0 -4px 0 rgba(0,0,0,.1);
}
.stalk.w1 { left: 6%;  animation-duration: 3.2s; }
.stalk.w2 { left: 12%; animation-duration: 3.8s; animation-delay: -.9s; scale: .85; }
.stalk.w3 { left: 22%; animation-duration: 3.4s; animation-delay: -1.7s; }
.stalk.w4 { left: 44%; animation-duration: 4s;   animation-delay: -.4s; scale: .8; }
.stalk.w5 { left: 58%; animation-duration: 3.3s; animation-delay: -2.1s; }
.stalk.w6 { left: 78%; animation-duration: 3.7s; animation-delay: -1.2s; scale: .9; }
.stalk.w7 { left: 92%; animation-duration: 3.1s; animation-delay: -2.6s; }
@keyframes stalk-sway {
  0%, 100% { rotate: -6deg; }
  50%      { rotate: 7deg; }
}

/* ---------- 減少動態偏好 ---------- */
@media (prefers-reduced-motion: reduce) {
  .notfound *, .notfound *::before, .notfound *::after { animation: none !important; transition: none !important; }
}

@media (max-width: 560px) {
  .scarecrow { scale: .62; right: -4%; bottom: 60px; }
  .hills { bottom: 70px; }
  .wheat-field { height: 76px; }
  .stalk { bottom: 60px; scale: .8; }
  .scene { padding-bottom: 120px; }
}
</style>
