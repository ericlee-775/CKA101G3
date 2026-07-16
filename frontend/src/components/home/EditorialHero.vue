<script setup>
/*
  首頁編輯風主視覺(節氣刊物風)。
  由 docs/design/farmily-hero-editorial-v2.html 導入,整合時的調整:
    - 拿掉 standalone 檔的頂部 nav(網站已有 ShopHeader)與自訂游標
      (原本 body{cursor:none} 會霸佔整站游標,不適合真實多頁網站)
    - 色票變數改用 --fh-* 前綴,避免和全站 theme.css 的 --ink/--leaf/--line 撞名
    - 動畫改在 Vue 生命週期掛載/卸載,換頁時清乾淨,不留 rAF / 事件監聽
    - 兩顆按鈕改成 router-link,接到實際路由
*/
import { onMounted, onBeforeUnmount, ref } from 'vue'

const root = ref(null)    // section 根,理念文字進場觀察用
const photo = ref(null)   // 插畫面板 figure,滑鼠微傾斜用

let rafId = null
let onMove = null
let observer = null

onMounted(() => {
  const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches
  const isTouch = matchMedia('(hover: none)').matches

  // 主視覺照片:依滑鼠在視窗的位置,極輕微 3D 傾斜(±2.5 度)。
  // 只在桌機、且使用者沒開「減少動態」時啟用。
  if (!reduceMotion && !isTouch && photo.value) {
    let mx = innerWidth / 2
    let my = innerHeight / 2
    let tiltX = 0
    let tiltY = 0
    onMove = (e) => {
      mx = e.clientX
      my = e.clientY
    }
    addEventListener('mousemove', onMove)
    const tick = () => {
      const tx = (mx / innerWidth - 0.5) * 5
      const ty = (my / innerHeight - 0.5) * -5
      tiltX += (tx - tiltX) * 0.06
      tiltY += (ty - tiltY) * 0.06
      // 傾斜走 transform;照片的上下起伏走 CSS translate 屬性,兩者獨立不互相蓋掉
      if (photo.value) photo.value.style.transform = `rotateY(${tiltX}deg) rotateX(${tiltY}deg)`
      rafId = requestAnimationFrame(tick)
    }
    rafId = requestAnimationFrame(tick)
  }

  // 理念文字:滾到才逐句浮現
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((en) => {
        if (en.isIntersecting) {
          en.target.classList.add('is-in')
          observer.unobserve(en.target)
        }
      })
    },
    { threshold: 0.4 },
  )
  root.value?.querySelectorAll('.reveal').forEach((el) => observer.observe(el))
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
  if (onMove) removeEventListener('mousemove', onMove)
  if (observer) observer.disconnect()
})
</script>

<template>
  <div class="ehero" ref="root">
    <main class="hero">
      <!-- 直書節氣:載入時金線先畫出來,字再浮現 -->
      <aside class="hero__term">二十四節氣・小暑</aside>

      <div class="hero__text">
        <p class="hero__eyebrow">TAIWAN FARM TO TABLE</p>

        <h1 class="hero__title">
          <span class="line"><span>新鮮，</span></span>
          <span class="line"><span>從產地開始。</span></span>
        </h1>

        <p class="hero__desc">
          Farmily 與台灣各地小農直接合作。跟著節氣的腳步，
          我們只在風味最好的時刻採收，隔日直送你的餐桌。
        </p>

        <div class="hero__actions">
          <router-link class="btn-solid" to="/products"><span>購買商品</span></router-link>
          <router-link class="link-arrow" to="/farmily">認識我們的農場 <i>→</i></router-link>
        </div>
      </div>

      <!-- 右側主視覺:滑鼠移動時輕微 3D 傾斜 + 緩緩上下起伏 -->
      <div class="hero__visual">
        <!-- frame 只有照片那麼寬,讓印章、圖說貼齊照片邊緣(照片已縮小置中) -->
        <div class="hero__frame">
          <!-- 會慢慢轉的圓形印章:壓在照片左上角後面,只從角落露出一角,只會轉動 -->
          <svg class="hero__seal" viewBox="0 0 104 104" aria-hidden="true">
            <defs>
              <path id="ehSealPath" d="M 52,52 m -40,0 a 40,40 0 1,1 80,0 a 40,40 0 1,1 -80,0" />
            </defs>
            <text><textPath href="#ehSealPath">產地直送・當季採收・FARMILY・</textPath></text>
          </svg>

          <!-- 手繪稻田插畫(取代照片):日出、飛鳥、隨風搖的稻穗,沿用滑鼠微傾斜 -->
          <figure
            class="hero__photo hero__scene"
            ref="photo"
            role="img"
            aria-label="手繪稻田插畫：日出、飛鳥與隨風輕搖的稻穗，題有詩句「把酒話桑麻・相逢在田家」"
          >
            <svg class="scene-svg" viewBox="0 0 520 693" preserveAspectRatio="xMidYMax slice" aria-hidden="true">
              <!-- 遠山:兩層淡墨 -->
              <path d="M0 430 Q 90 380 180 420 T 380 415 T 520 430 L 520 693 L 0 693 Z" fill="#DCD6C0" />
              <path d="M0 470 Q 120 425 240 462 T 520 468 L 520 693 L 0 693 Z" fill="#D2CBB2" />

              <!-- 太陽:單線圓 + 淡金暈,載入時從地平線升起 -->
              <g class="sun-disc">
                <circle cx="170" cy="300" r="86" fill="none" stroke="#A98C4B" stroke-width="1.5" opacity="0.5" />
                <circle cx="170" cy="300" r="58" fill="#E9DFC2" />
                <circle cx="170" cy="300" r="58" fill="none" stroke="#A98C4B" stroke-width="1.5" />
              </g>

              <!-- 飛鳥:兩筆水墨,緩緩滑過天空 -->
              <g class="bird" stroke="#55624F" stroke-width="2" fill="none" stroke-linecap="round">
                <path d="M40 200 q 8 -9 16 0 q 8 -9 16 0" />
              </g>
              <g class="bird b2" stroke="#55624F" stroke-width="1.6" fill="none" stroke-linecap="round" opacity="0.7">
                <path d="M10 250 q 6 -7 12 0 q 6 -7 12 0" />
              </g>

              <!-- 田埂線條:一層層水田 -->
              <g stroke="#2C3B2A" stroke-width="1.2" fill="none" opacity="0.55">
                <path d="M0 520 Q 260 500 520 522" />
                <path d="M0 560 Q 260 538 520 562" />
                <path d="M0 604 Q 260 580 520 606" />
              </g>

              <!-- 前景稻穗:墨綠+秋金,輕輕搖曳 -->
              <g fill="none" stroke-linecap="round">
                <g class="stalk">
                  <path d="M60 693 C 62 620 58 570 72 520" stroke="#2C3B2A" stroke-width="3" />
                  <path d="M72 520 q -14 -8 -16 -26 M72 520 q 12 -10 10 -28 M72 526 q -18 0 -26 -14"
                        stroke="#A98C4B" stroke-width="2.4" />
                </g>
                <g class="stalk">
                  <path d="M130 693 C 128 610 136 560 124 505" stroke="#2C3B2A" stroke-width="3" />
                  <path d="M124 505 q -13 -9 -13 -27 M124 505 q 13 -8 13 -26 M124 512 q 17 2 26 -11"
                        stroke="#A98C4B" stroke-width="2.4" />
                </g>
                <g class="stalk">
                  <path d="M205 693 C 208 600 198 555 214 495" stroke="#2C3B2A" stroke-width="3.2" />
                  <path d="M214 495 q -15 -7 -17 -26 M214 495 q 12 -11 9 -29 M214 502 q -19 1 -28 -12"
                        stroke="#A98C4B" stroke-width="2.6" />
                </g>
                <g class="stalk">
                  <path d="M300 693 C 296 615 306 565 292 515" stroke="#2C3B2A" stroke-width="3" />
                  <path d="M292 515 q -13 -9 -14 -27 M292 515 q 14 -8 14 -26 M292 522 q 18 1 27 -12"
                        stroke="#A98C4B" stroke-width="2.4" />
                </g>
                <g class="stalk">
                  <path d="M382 693 C 386 605 374 560 392 500" stroke="#2C3B2A" stroke-width="3.2" />
                  <path d="M392 500 q -15 -8 -16 -27 M392 500 q 13 -10 11 -29 M392 508 q -19 0 -27 -13"
                        stroke="#A98C4B" stroke-width="2.6" />
                </g>
                <g class="stalk">
                  <path d="M455 693 C 452 625 462 575 448 530" stroke="#2C3B2A" stroke-width="2.8" />
                  <path d="M448 530 q -12 -8 -13 -25 M448 530 q 12 -8 12 -25"
                        stroke="#A98C4B" stroke-width="2.2" />
                </g>
              </g>
            </svg>

            <!-- 直排詩句 -->
            <div class="verse" aria-hidden="true">
              <span>把酒話桑麻</span>
              <span>相逢在田家</span>
              <span class="minor">跟著節氣採收・照顧土地與人</span>
            </div>
          </figure>
          <!-- 圖說必須在 figure 之外(figure 有 overflow:hidden 會裁掉它),
               因此用 p 而非 figcaption(figcaption 依規範只能是 figure 的子元素) -->
          <p class="hero__caption">手繪夏日田家・稻浪與飛鳥 — 07 / 2026</p>
        </div>
      </div>
    </main>

    <!-- 酒類警語:主視覺題有「把酒話桑麻」的飲酒意象,依法標示 -->
    <p class="hero__warning">未滿十八歲禁止飲酒・酒後不開車,開車不喝酒</p>

    <!-- 下方:品牌理念(滾動進場) -->
    <section class="manifesto">
      <p>
        <span class="reveal">我們相信，食材最好的樣子，</span><br />
        <span class="reveal" style="transition-delay: 0.2s">不在貨架上，而在它離開土地的那一刻。</span>
      </p>
    </section>
  </div>
</template>

<style scoped>
/* 只作用在這個元件內的色票,用 --fh-* 前綴避開全站 theme.css 的同名變數 */
.ehero {
  --fh-paper: #f1ecdf; /* 米白:紙張底色 */
  --fh-ink: #1c2819; /* 墨綠近黑:標題 */
  --fh-body: #403e34; /* 內文灰 */
  --fh-moss: #294334; /* 墨綠:輔助 */
  --fh-gold: #ac8329; /* 稻穗金:互動點綴 */
  --fh-wheat: #937d5b; /* 淺棕:圖說等次要文字 */
  --fh-line: #d0c9b7; /* 分隔線 */

  position: relative;
  background: var(--fh-paper);
  color: var(--fh-body);
  font-family: 'Noto Sans TC', 'PingFang TC', 'Microsoft JhengHei', sans-serif;
  overflow: hidden; /* 印章露角、照片傾斜時不撐出水平捲軸 */
}

/* 紙張顆粒質感:鋪一層極淡的雜訊 */
.ehero::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 3;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='120'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2'/%3E%3CfeColorMatrix values='0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 0.04 0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

/* ============================================================
   HERO:左文右圖的編輯排版
   ============================================================ */
.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: clamp(16px, 2.5vw, 40px);
  align-items: center;
  padding: clamp(24px, 4vh, 44px) clamp(24px, 5vw, 72px) clamp(28px, 4vh, 48px);
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* --- 直書節氣標籤:貼在最左側 --- */
.hero__term {
  position: absolute;
  left: clamp(8px, 2vw, 24px);
  top: 50%;
  translate: 0 -50%;
  writing-mode: vertical-rl;
  letter-spacing: 0.5em;
  font-size: 0.82rem;
  color: var(--fh-moss);
  display: flex;
  align-items: center;
  gap: 14px;
  animation: eh-fade 1.2s ease 0.9s both;
}
.hero__term::before {
  content: '';
  width: 1px;
  height: 72px;
  background: var(--fh-gold);
  transform-origin: top;
  animation: eh-drawLine 1s cubic-bezier(0.65, 0, 0.35, 1) 1.1s both;
}
@keyframes eh-drawLine {
  from {
    scale: 1 0;
  }
  to {
    scale: 1 1;
  }
}

.hero__text {
  padding-left: clamp(24px, 4vw, 64px);
  text-align: left;
}

.hero__eyebrow {
  font-size: 0.85rem;
  letter-spacing: 0.32em;
  color: var(--fh-gold);
  margin-bottom: 22px;
  animation: eh-fade 1s ease 0.2s both;
}

/* --- 主標:明體,兩行,逐行從遮罩裡浮出 --- */
.hero__title {
  font-family: 'Noto Serif TC', 'PMingLiU', serif;
  font-size: clamp(2.3rem, 4.8vw, 4rem);
  font-weight: 700;
  color: var(--fh-ink);
  line-height: 1.32;
  letter-spacing: 0.05em;
  margin: 0;
}
.hero__title .line {
  display: block;
  overflow: hidden;
}
.hero__title .line span {
  display: inline-block;
  transform: translateY(110%);
  animation: eh-lineUp 1.1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
.hero__title .line:nth-child(2) span {
  animation-delay: 0.15s;
}
@keyframes eh-lineUp {
  to {
    transform: translateY(0);
  }
}

.hero__desc {
  margin: 26px 0 0;
  max-width: 42ch;
  line-height: 2;
  font-size: 0.98rem;
  animation: eh-fade 1s ease 0.7s both;
}

/* --- CTA --- */
.hero__actions {
  margin-top: 44px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 30px;
  animation: eh-fade 1s ease 1.1s both;
}
.btn-solid {
  padding: 15px 42px;
  background: var(--fh-ink);
  color: var(--fh-paper);
  border: none;
  font-size: 0.95rem;
  letter-spacing: 0.22em;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  font-family: inherit;
}
.btn-solid::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--fh-moss);
  transform: translateY(101%);
  transition: transform 0.45s cubic-bezier(0.65, 0, 0.35, 1);
}
.btn-solid:hover::before {
  transform: translateY(0);
}
.btn-solid span {
  position: relative;
}
.btn-solid:focus-visible {
  outline: 2px solid var(--fh-gold);
  outline-offset: 3px;
}

.link-arrow {
  color: var(--fh-ink);
  text-decoration: none;
  font-size: 0.95rem;
  letter-spacing: 0.18em;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.link-arrow i {
  font-style: normal;
  transition: translate 0.3s;
}
.link-arrow:hover i {
  translate: 6px 0;
}

/* --- 右側主視覺:照片 + 滑鼠微傾斜 --- */
.hero__visual {
  position: relative;
  perspective: 1200px;
}
/* frame 收縮到跟照片一樣寬,並在右欄置中;印章、圖說都相對它定位 */
.hero__frame {
  position: relative;
  width: fit-content;
  max-width: 100%;
  margin-inline: auto;
}
.hero__photo {
  position: relative;
  z-index: 1; /* 壓在圓章上面,圓章只從左上角露出一角 */
  aspect-ratio: 3 / 4;
  /* 用視窗高度收斂,寬度自動跟著算 → 一進頁面就能看到整張完整海報 */
  height: min(82vh, 760px);
  width: auto;
  max-width: 100%;
  overflow: hidden;
  /* 載入時由下往上揭開,揭開後接一個很輕的上下起伏(呼吸感) */
  clip-path: inset(100% 0 0 0);
  animation:
    eh-unveil 1.3s cubic-bezier(0.65, 0, 0.35, 1) 0.4s forwards,
    eh-floaty 7s ease-in-out 1.9s infinite;
  transform-style: preserve-3d;
  will-change: transform, translate;
}
@keyframes eh-unveil {
  to {
    clip-path: inset(0 0 0 0);
  }
}
/* 微微起伏:上下 8px,慢速來回,像照片浮在紙上輕輕呼吸 */
@keyframes eh-floaty {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 -8px;
  }
}
/* --- 手繪稻田插畫面板(取代原本的照片,沿用揭幕/起伏/傾斜) --- */
.hero__scene {
  background: #e6e1cf; /* 深一階的紙色,做插畫底 */
  border: 1px solid rgba(44, 59, 42, 0.25);
}
/* 紙張細框:內縮一圈,像裱起來的畫 */
.hero__scene::after {
  content: '';
  position: absolute;
  inset: 12px;
  border: 1px solid rgba(44, 59, 42, 0.18);
  pointer-events: none;
  z-index: 2;
}
.scene-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

/* 直排詩句 */
.verse {
  position: absolute;
  top: 40px;
  right: 40px;
  display: flex;
  flex-direction: row-reverse;
  gap: 20px;
  z-index: 1;
}
.verse span {
  writing-mode: vertical-rl;
  font-family: 'Noto Serif TC', 'PMingLiU', serif;
  font-size: 24px;
  letter-spacing: 0.45em;
  color: var(--fh-ink);
}
.verse span.minor {
  font-size: 12px;
  color: var(--fh-moss);
  letter-spacing: 0.4em;
}

/* --- 插畫動態:日出 / 稻穗搖曳 / 飛鳥滑翔 --- */
/* 太陽:載入時從地平線升起(延遲配合揭幕動畫) */
.sun-disc {
  transform-origin: center;
  animation: eh-sunrise 2.4s ease-out 0.6s both;
}
@keyframes eh-sunrise {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
/* 稻穗:以根部為軸輕搖;錯開每株的節奏,才不會像一起做體操 */
.stalk {
  transform-origin: bottom center;
  animation: eh-sway 5s ease-in-out infinite alternate;
}
.stalk:nth-child(2n) {
  animation-delay: -1.2s;
  animation-duration: 6s;
}
.stalk:nth-child(3n) {
  animation-delay: -2.6s;
  animation-duration: 5.5s;
}
@keyframes eh-sway {
  from {
    transform: rotate(-1.6deg);
  }
  to {
    transform: rotate(1.8deg);
  }
}
/* 飛鳥:直線緩緩滑過整片天空,第二隻錯開一半 */
.bird {
  animation: eh-glide 14s linear infinite;
}
.bird.b2 {
  animation-delay: -6s;
}
@keyframes eh-glide {
  from {
    transform: translate(-60px, 12px);
  }
  to {
    transform: translate(560px, -26px);
  }
}

/* 照片右下角的圖說,像雜誌圖註 */
.hero__caption {
  position: absolute;
  right: 0;
  bottom: -34px;
  margin: 0;                 /* 歸零 p 的預設上下 margin,避免位移 */
  font-size: 0.76rem;
  letter-spacing: 0.16em;
  color: var(--fh-wheat);
  animation: eh-fade 1s ease 1.4s both;
}

/* 空心圓章:貼照片左上角,壓在照片後面 → 只從角落露出一角,只會轉動 */
.hero__seal {
  position: absolute;
  top: -34px;
  left: -34px;
  width: 100px;
  height: 100px;
  z-index: 0;
  animation:
    eh-spin 40s linear infinite,
    eh-fade 1s ease 1.2s both;
}
.hero__seal text {
  font-size: 11.5px;
  letter-spacing: 3.5px;
  fill: var(--fh-moss);
}
@keyframes eh-spin {
  to {
    rotate: 360deg;
  }
}

@keyframes eh-fade {
  from {
    opacity: 0;
    translate: 0 14px;
  }
  to {
    opacity: 1;
    translate: 0 0;
  }
}

/* 酒類警語:置中一行,壓在紙張顆粒層(z-index:3)之上仍清楚可讀 */
.hero__warning {
  position: relative;
  z-index: 4;
  max-width: 1400px;
  margin: 0 auto;
  padding: 4px clamp(16px, 3vw, 40px) 24px;
  text-align: right;
  font-size: 0.84rem;
  letter-spacing: 0.14em;
  color: var(--fh-moss);
  animation: eh-fade 1s ease 1.5s both;
}

/* ============================================================
   下方:品牌理念(滾動進場)
   ============================================================ */
.manifesto {
  max-width: 820px;
  margin: 40px auto 96px;
  padding: 0 24px;
  text-align: center;
  position: relative;
  z-index: 1;
}
.manifesto p {
  font-family: 'Noto Serif TC', serif;
  font-size: clamp(1.3rem, 2.6vw, 1.85rem);
  line-height: 2;
  color: var(--fh-moss);
  letter-spacing: 0.06em;
  margin: 0;
}
.manifesto .reveal {
  opacity: 0;
  translate: 0 24px;
  transition:
    opacity 0.9s ease,
    translate 0.9s ease;
}
.manifesto .reveal.is-in {
  opacity: 1;
  translate: 0 0;
}

/* ============================================================
   響應式
   ============================================================ */
@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .hero__term {
    display: none;
  }
  .hero__text {
    padding-left: 0;
  }
  .hero__visual {
    order: -1;
  }
  .hero__seal {
    display: none; /* 手機版收起裝飾圓章,避免擠壓 */
  }
}

/* 無障礙:減少動態 */
@media (prefers-reduced-motion: reduce) {
  .ehero *,
  .ehero *::before,
  .ehero *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
  .hero__title .line span {
    transform: none;
  }
  .hero__photo {
    clip-path: none;
  }
}
</style>
