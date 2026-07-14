<script setup>
  import { onMounted, onBeforeUnmount, ref } from 'vue';
  import ConfirmDialog from './components/ConfirmDialog.vue';
  import TurtleChat from './components/TurtleChat.vue';
  import authStore from '@/stores/auth';

  // ===== 全站自訂游標:小圓點 + 慢半拍的外圈,滑到可點元素時外圈放大 =====
  const cursorDot = ref(null);
  const cursorRing = ref(null);
  let rafId = null;
  let onMove = null;
  let onLeave = null;
  let onEnter = null;
  // 什麼算「可互動」:外圈碰到就放大
  const INTERACTIVE = 'a, button, input, textarea, select, label, summary, [role="button"], [contenteditable="true"], [data-hover]';

  function initCursor() {
    // 只在桌機(有滑鼠、精細指標)且使用者沒開「減少動態」時啟用;
    // 觸控/手機/減少動態 → 不加 class,維持原生游標(也是失敗時的安全退路)
    const ok =
      matchMedia('(hover: hover) and (pointer: fine)').matches &&
      matchMedia('(prefers-reduced-motion: no-preference)').matches;
    const dot = cursorDot.value;
    const ring = cursorRing.value;
    if (!ok || !dot || !ring) return;

    document.documentElement.classList.add('custom-cursor');

    let mx = innerWidth / 2;
    let my = innerHeight / 2;
    let rx = mx;
    let ry = my;

    onMove = (e) => {
      mx = e.clientX;
      my = e.clientY;
      // 每次移動判斷游標下方是不是可互動元素,是就把外圈放大
      ring.classList.toggle('is-active', !!e.target.closest(INTERACTIVE));
    };
    // 滑出視窗時淡出,滑回來再淡入,避免游標卡在邊緣
    onLeave = () => {
      dot.style.opacity = '0';
      ring.style.opacity = '0';
    };
    onEnter = () => {
      dot.style.opacity = '1';
      ring.style.opacity = '1';
    };
    addEventListener('mousemove', onMove);
    document.addEventListener('mouseleave', onLeave);
    document.addEventListener('mouseenter', onEnter);

    const tick = () => {
      // 小圓點:直接貼著滑鼠
      dot.style.transform = `translate(${mx}px, ${my}px) translate(-50%, -50%)`;
      // 外圈:每幀追 18%,慢半拍
      rx += (mx - rx) * 0.18;
      ry += (my - ry) * 0.18;
      ring.style.transform = `translate(${rx}px, ${ry}px) translate(-50%, -50%)`;
      rafId = requestAnimationFrame(tick);
    };
    rafId = requestAnimationFrame(tick);
  }

  // App 一啟動就以「後端 session」為準重新確認登入狀態：
  // 有記住身分就向後端 /me 驗證，session 失效(401)會自動清掉。
  onMounted(() => {
    authStore.ensureHydrated();
    initCursor();
  });

  onBeforeUnmount(() => {
    if (rafId) cancelAnimationFrame(rafId);
    if (onMove) removeEventListener('mousemove', onMove);
    if (onLeave) document.removeEventListener('mouseleave', onLeave);
    if (onEnter) document.removeEventListener('mouseenter', onEnter);
    document.documentElement.classList.remove('custom-cursor');
  });
</script>

<template>
    <!--
      App 只當「最外層外殼」：header / footer / 側邊欄都交給各自的 Layout
      元件（ShopLayout、FarmerLayout）處理，這裡只放全頁面共用的東西。
      router 依網址決定要套哪一個 Layout，畫在這個 <router-view> 裡。
    -->
    <router-view></router-view>

    <!-- 全域確認彈窗：任何地方呼叫 confirm() 都由這個元件顯示 -->
    <ConfirmDialog></ConfirmDialog>

    <!-- 全站浮動烏龜客服 🐢 -->
    <TurtleChat></TurtleChat>

    <!-- 全站自訂游標(僅桌機啟用,元素永遠 pointer-events:none 不擋點擊) -->
    <div class="cursor-dot" ref="cursorDot" aria-hidden="true"></div>
    <div class="cursor-ring" ref="cursorRing" aria-hidden="true"></div>
</template>

<style scoped>
  
</style>


<style>
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
}
/* 頁面底色預設不上色(白);首頁的紙感背景改由 HomeView 元件自己鋪 */

/* ===== 全站自訂游標 =====
   預設隱藏;只有 JS 判斷是桌機且未開減少動態時,才在 <html> 加上 .custom-cursor 啟用。
   這樣觸控裝置、或 JS 未執行時,都會保有正常的原生游標(安全退路)。 */
.cursor-dot,
.cursor-ring {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  border-radius: 50%;
  pointer-events: none;               /* 永遠不擋滑鼠點擊 */
  z-index: 99999;                     /* 蓋在所有內容(含彈窗)之上 */
  transition: opacity 0.25s ease;
}
.cursor-dot {
  width: 6px;
  height: 6px;
  background: #2c3a2e;                /* 墨綠點,對應品牌主色 */
}
.cursor-ring {
  width: 34px;
  height: 34px;
  border: 1px solid rgba(44, 58, 46, 0.45);
  transition: width 0.3s, height 0.3s, background 0.3s, border-color 0.3s, opacity 0.25s ease;
}
/* 滑到可互動元素時:外圈放大並染一層淡稻穗金 */
.cursor-ring.is-active {
  width: 56px;
  height: 56px;
  background: rgba(172, 131, 41, 0.12);
  border-color: #ac8329;
}

/* 啟用時:顯示自訂游標,並強制隱藏原生游標(含各元件自訂的 cursor:pointer 等) */
html.custom-cursor .cursor-dot,
html.custom-cursor .cursor-ring {
  display: block;
}
html.custom-cursor,
html.custom-cursor * {
  cursor: none !important;
}
</style>
