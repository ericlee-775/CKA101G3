<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import cartStore from '@/stores/cart'
import authApi from '@/api/auth'
import { confirm } from '@/composables/useConfirm'
import notificationApi from '@/api/memberNotification'
import notificationStore from '@/stores/memberNotification'

const router = useRouter()

// 購物車總件數（給右上角小紅點用）；超過 99 顯示成 99+
const cartCount = computed(() => cartStore.totalCount.value)

// 數量「增加」的瞬間播一次動畫：購物車跳一下、紅點彈出來。
// 連續加入時先關掉再下一幀重開，動畫才會重播（同一個 class 連續掛著不會重跑）。
const cartBump = ref(false)
let bumpTimer = null
watch(cartCount, (newVal, oldVal) => {
  if (newVal <= oldVal) return // 減少/歸零（移除商品）不慶祝
  cartBump.value = false
  requestAnimationFrame(() => {
    cartBump.value = true
  })
  clearTimeout(bumpTimer)
  bumpTimer = setTimeout(() => {
    cartBump.value = false
  }, 700)
})

// 控制手機版選單的開合：點 ☰ 切換 true/false，
// 在 <nav> 上用 :class 綁定，true 時才把選單展開。
const isMenuOpen = ref(false)

// 依身分決定「個人中心」連結
const accountLink = computed(() => (authStore.isFarmer ? '/farmer/me' : '/member/me'))

// 顯示名稱：會員用 userName、小農用 farmName，都沒有就退回 email
const displayName = computed(() => {
  const u = authStore.state.user
  if (!u) return ''
  return u.userName || u.farmName || u.email || '我的帳號'
})

// ===== 帳號下拉選單 =====
// hover 帳號膠囊浮出選單（純 CSS）；點膠囊本身走 accountLink 進會員中心
// 會員中心選單（跟 MemberLayout 的側邊選單一致，但不含「通知」→ 通知走小鈴鐺）
const accountMenu = [
  { label: '個人資料', icon: '👤', to: '/member/me' },
  { label: '我的文章', icon: '✍️', to: '/member/blogs' },
  { label: '我的團購', icon: '👥', to: '/member/group-buys' },
  { label: '我的收藏', icon: '❤️', to: '/member/favorites' },
  { label: '我的訂單', icon: '🧾', to: '/member/orders' },
  { label: '已報名活動', icon: '🌱', to: '/member/farm-trips' },
  { label: '優惠券', icon: '🎟️', to: '/member/coupons' },
]

// ===== 通知小鈴鐺 =====

const notifications = ref([])
const unreadCount = notificationStore.unreadCount

// 小鈴鐺分類 icon (targetType)
const ICON_BY_TYPE = {
  account: '👤', order: '📦', groupbuy: '👥', trip: '🌱', blog: '✍️'
}

// 分類對應中文標題 (title)
const TITLE_BY_TYPE = {
  account : '帳號異動', order: '訂單狀態', groupbuy: '團購動態', trip: '體驗活動', blog: '文章評論'
}

// 轉換時間 (N 分鐘前)
function timeAgo(dt){
  if (!dt) { return '' }
  const diffMs = Date.now() - new Date(dt).getTime()
  const min = Math.floor(diffMs / 60000)
  if (min < 1) return '剛剛'
  if (min < 60) return `${min} 分鐘前`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} 小時前`
  const day = Math.floor(hr / 24)
  if (day < 7) return `${day} 天前`
  // 超過一週顯示日期
  return new Date(dt).toLocaleDateString('zh-TW')
}

// 取得小鈴鐺 preview list 和未讀數
async function loadBell(){
  if (!authStore.isMember) { return }
  await notificationStore.refresh()
  try {
    notifications.value = await notificationApi.getNotifPreview()
  } catch (e){
  }
}

// 登入後重載鈴鐺
watch(() => authStore.isMember, (isMember) => {
  if (isMember){
    loadBell()
  } else {
    notifications.value = []
    notificationStore.reset()
  }
})

// 載入小鈴鐺，每 {300} 秒自動更新一次未讀數
let BellTimer = null
onMounted(() => {
  loadBell()
  BellTimer = setInterval(loadBell, 300000)
})
onBeforeUnmount(() => {
  if (BellTimer) clearInterval(BellTimer)
})


// 登出：先跳彈窗確認，再打後端清 session、清前端狀態，導回首頁
async function logout() {
  if (!(await confirm({ title: '登出', message: '確定要登出嗎？', confirmText: '登出' }))) return
  try {
    await authApi.logout()
  } catch {
    // 忽略錯誤，前端狀態仍要清掉
  }
  authStore.clear()
  router.push('/')
}
</script>

<template>
  <header class="site-header">
    <!-- 品牌區：logo 回首頁；印章是獨立按鈕（<a> 不能巢狀），點了進「關於我們」 -->
    <div class="brand-group">
      <router-link class="brand" to="/" aria-label="Farmily 首頁">
        <img
          class="brand-logo"
          src="https://storage.googleapis.com/cka101-15/form/farmLogo.png?v=20260613-transparent"
          alt="Farmily logo"
        />
        <span class="brand-text">
          <strong>Farmily</strong>
          <small>新鮮直送・產地到餐桌</small>
        </span>
      </router-link>
      <!-- 你儂我農朱印：hover 微轉下壓像蓋章，點擊看品牌故事 -->
      <router-link class="brand-seal" to="/" aria-label="你儂我農——回首頁" title="回首頁">
        <span>你</span><span>儂</span>
        <span>我</span><span>農</span>
      </router-link>
    </div>

    <!-- 手機版選單按鈕：點一下切換 isMenuOpen -->
    <button
      class="mobile-menu"
      type="button"
      aria-label="開啟選單"
      @click="isMenuOpen = !isMenuOpen"
    >
      ☰
    </button>

    <!--
      導覽列。
      :class="{ open: isMenuOpen }" → 手機版展開時才加上 open class。
      點任何連結後 isMenuOpen = false → 換頁後自動把手機選單收起來。
    -->
    <nav class="main-nav" :class="{ open: isMenuOpen }" @click="isMenuOpen = false">
      <router-link class="nav-link" to="/news">最新消息</router-link>
      <router-link class="nav-link" to="/products">全部商品</router-link>
      <router-link class="nav-link" to="/farmily">合作農場</router-link>
      <router-link class="nav-link" to="/group-buys">團購</router-link>
      <router-link class="nav-link" to="/blogs">部落格</router-link>
      <router-link class="nav-link" to="/farm-trips">體驗活動</router-link>
      <router-link class="nav-link" to="/farm-map">產地地圖</router-link>

      <!-- 右側使用者區：購物車 + (未登入顯示 登入/註冊；已登入顯示 個人中心 + 登出) -->
      <div class="user-zone">
        <!-- 購物車：任何身分都看得到，點了到購物車頁 -->
        <router-link class="cart-btn" :class="{ 'cart-btn--bump': cartBump }" to="/cart" aria-label="購物車">
          <!-- 木推車圖示(CSS 手工畫,跟商品頁動畫同一台車) -->
          <span class="cart-icon" aria-hidden="true">
            <span class="wagon__veggies">🥬🥕</span>
            <span class="wagon__bed"></span>
            <span class="wagon__wheel wagon__wheel--front"></span>
            <span class="wagon__wheel wagon__wheel--back"></span>
          </span>
          <!-- 有東西才顯示小紅點；超過 99 顯示 99+ -->
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
        </router-link>

        <template v-if="authStore.isLoggedIn">
          <!-- 通知小鈴鐺（會員限定）：hover 浮出通知預覽，點擊直接進通知頁 -->
          <div v-if="!authStore.isFarmer" class="bell-wrap">
            <router-link class="bell-btn" to="/member/notifications" aria-label="通知">
              <span class="bell-icon">🔔</span>
              <span v-if="unreadCount > 0" class="bell-badge">{{ unreadCount }}</span>
            </router-link>

            <!-- hover 小視窗 -->
            <div class="notify-popover">
              <div class="notify-head">
                <strong>通知</strong>
                <span v-if="unreadCount > 0" class="notify-unread">{{ unreadCount }} 則未讀</span>
              </div>
              <ul class="notify-list">
                <li v-for="n in notifications" :key="n.id" class="notify-item" :class="{ unread: (n.status === 'unread') }">
                  <span class="notify-icon">{{ ICON_BY_TYPE[n.targetType] }}</span>
                  <div class="notify-body">
                    <p class="notify-title">{{ TITLE_BY_TYPE[n.targetType] }}</p>
                    <p class="notify-text">{{ n.content }}</p>
                    <small class="notify-time">{{ timeAgo(n.createdAt) }}</small>
                  </div>
                </li>
              </ul>
              <router-link class="notify-more" to="/member/notifications">查看全部通知 →</router-link>
            </div>
          </div>

          <!-- 小農：沒有會員中心選單，維持原本單一連結 -->
          <router-link v-if="authStore.isFarmer" class="account-btn" :to="accountLink">
            <span class="account-avatar">👤</span>
            <span class="account-name">{{ displayName }}</span>
          </router-link>

          <!-- 會員：hover 帳號膠囊浮出下拉選單；點膠囊本身直接進會員中心 -->
          <div v-else class="account-wrap">
            <router-link class="account-btn account-toggle" :to="accountLink">
              <span class="account-avatar">👤</span>
              <span class="account-name">{{ displayName }}</span>
              <span class="account-caret">▾</span>
            </router-link>

            <div class="account-menu">
              <router-link
                v-for="item in accountMenu"
                :key="item.to"
                class="account-menu-item"
                :to="item.to"
              >
                <span class="account-menu-icon">{{ item.icon }}</span>
                {{ item.label }}
              </router-link>
            </div>
          </div>

          <button class="logout-btn" type="button" @click="logout">登出</button>
        </template>
        <template v-else>
          <router-link class="login-btn" to="/login">登入</router-link>
          <router-link class="register-btn" to="/register">註冊</router-link>
        </template>
      </div>
    </nav>
  </header>
</template>

<style scoped>
/* ========== 整體 header ========== */
.site-header {
  position: sticky;                  /* 捲動時固定在最上方 */
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: auto 1fr;   /* 左:品牌(自動寬) 右:導覽列(撐滿) */
  align-items: center;
  gap: 24px;
  min-height: 64px;
  padding: 10px clamp(18px, 4vw, 56px);
  /* 和頁面同一張紙、無邊框無陰影 → header 融入背景,不再有分隔線 */
  background: var(--paper);
}

/* ========== 品牌區 ========== */
.brand-group {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;             /* 拿掉 router-link 預設底線 */
}
/* icon 高度對齊「Farmily + tagline」兩行文字的總高（約 19*1.2 + 12*1.2 ≈ 38px），
   視覺重心才穩，不會 icon 比字大一截 */
.brand-logo {
  width: 38px;
  height: 38px;
  object-fit: contain;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.brand-text strong {
  font-size: 19px;
  color: var(--ink);
}
/* 田字格朱印：紅底 + 白格露出紅色十字線，四字分置四角。
   是個連結（關於我們）：hover 微轉下壓像蓋章，提示可以點 */
.brand-seal {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 1.5px;
  width: 42px;
  height: 42px;
  padding: 2px;
  box-sizing: border-box;
  background: #a94438;               /* 磚紅/胭脂紅：比朱紅沉,像老印泥 */
  border-radius: 5px;
  flex: none;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.brand-seal span {
  display: grid;
  place-items: center;
  background: #fff;
  font-family: 'Noto Serif TC', serif;
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  color: #a94438;
}
/* hover：先微微抬起再斜著壓下去，像在紙上蓋章；:active 再壓深一點 */
.brand-seal:hover {
  animation: seal-stamp 0.35s ease forwards;
  box-shadow: 0 2px 8px rgba(169, 68, 56, 0.35);
}
.brand-seal:active {
  transform: rotate(-4deg) scale(0.9);
  animation: none;
}
@keyframes seal-stamp {
  0%   { transform: rotate(0) scale(1); }
  40%  { transform: rotate(-8deg) scale(1.08) translateY(-2px); }
  100% { transform: rotate(-4deg) scale(0.97) translateY(1px); }
}
@media (prefers-reduced-motion: reduce) {
  .brand-seal:hover,
  .brand-seal:active {
    animation: none;
    transform: none;
  }
}
/* tagline 淡化：墨綠 60% 透明度，讓 Farmily 更跳出來 */
.brand-text small {
  font-size: 12px;
  font-weight: 400;
  color: var(--muted);               /* 不支援 color-mix 的舊瀏覽器退回原色 */
  color: color-mix(in srgb, var(--leaf-dark) 60%, transparent);
  letter-spacing: 0.04em;
}

/* ========== 手機版選單按鈕（桌面隱藏） ========== */
.mobile-menu {
  display: none;
  justify-self: end;
  width: 42px;
  height: 42px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  font-size: 20px;
  cursor: pointer;
}

/* ========== 導覽列 ========== */
.main-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;                      /* 在 grid 內允許收縮，項目太多時擠壓而不是撐開 */
}
.nav-link {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;              /* 膠囊造型 */
  color: var(--ink);
  font-size: 15px;
  text-decoration: none;
  white-space: nowrap;               /* 文字不換行 */
  transition: background 0.18s ease, color 0.18s ease;  /* 滑過時平滑變色 */
}
.nav-link:hover {
  background: var(--leaf-soft);
  color: var(--leaf);
}
/* router-link-active：Vue Router 自動加在「目前所在頁面」連結上，用來做高亮 */
.nav-link.router-link-active {
  background: var(--leaf);
  color: #fff;
}

/* ========== 右側使用者區 ========== */
.user-zone {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;                 /* 推到最右邊 */
}
/* 購物車按鈕：圓形圖示鈕，滑過時淡綠底 */
.cart-btn {
  position: relative;                /* 讓小紅點可以絕對定位在右上角 */
  display: inline-grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  text-decoration: none;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.cart-btn:hover {
  background: var(--leaf-soft);
  border-color: var(--leaf);
}
/* ----- 木推車圖示(取代 🛒 emoji;零件:蔬菜/木斗/把手/輪子) ----- */
.cart-icon {
  position: relative;
  width: 26px;
  height: 21px;
}
/* 蔬菜:從車斗探出頭 */
.wagon__veggies {
  position: absolute;
  top: -6px;
  left: 3px;
  font-size: 10px;
  letter-spacing: -3px;
  line-height: 1;
}
/* 木斗:木頭色 + 直條木板紋 */
.wagon__bed {
  position: absolute;
  left: 0;
  bottom: 4px;
  width: 26px;
  height: 10px;
  box-sizing: border-box;
  border: 1.5px solid #6f4a26;
  border-radius: 2px 2px 4px 4px;
  background: repeating-linear-gradient(90deg, #b07c46 0 5px, #9a6a3a 5px 6px);
}
/* 把手:斜斜翹起在車尾 */
.wagon__bed::after {
  content: '';
  position: absolute;
  right: -7px;
  top: -3px;
  width: 9px;
  height: 2px;
  border-radius: 2px;
  background: #8a5a33;
  transform: rotate(-32deg);
}
/* 輪子:深木色圓 + 淺色輻條 */
.wagon__wheel {
  position: absolute;
  bottom: 0;
  width: 8px;
  height: 8px;
  box-sizing: border-box;
  border-radius: 50%;
  background: #4a3421;
  border: 2px solid #2c1f12;
}
.wagon__wheel::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 1px;
  background: #d8b98c;
}
.wagon__wheel--front {
  left: 3px;
}
.wagon__wheel--back {
  right: 3px;
}
/* 滑過購物車鈕:輪子轉起來,暗示「出發去購物車」 */
.cart-btn:hover .wagon__wheel {
  animation: wagon-roll 0.6s linear infinite;
}
@keyframes wagon-roll {
  to {
    transform: rotate(360deg);
  }
}
/* 數量小紅點 */
.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #c0392b;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

/* ===== 加入購物車的提示動畫：圖示跳一下、紅點「啵」地彈出 ===== */
.cart-icon {
  display: inline-block; /* span 預設 inline 吃不到 transform，要改 inline-block */
}
/* 圖示：像購物車被丟了東西進來，跳起來晃兩下停穩 */
.cart-btn--bump .cart-icon {
  animation: cart-hop 0.6s cubic-bezier(0.28, 0.84, 0.42, 1);
}
/* 紅點：從小彈大再回彈（overshoot），像氣泡冒出來 */
.cart-btn--bump .cart-badge {
  animation: badge-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes cart-hop {
  0%   { transform: translateY(0) rotate(0); }
  25%  { transform: translateY(-7px) rotate(-10deg); }
  50%  { transform: translateY(0) rotate(7deg); }
  70%  { transform: translateY(-3px) rotate(-4deg); }
  100% { transform: translateY(0) rotate(0); }
}
@keyframes badge-pop {
  0%   { transform: scale(0.3); }
  60%  { transform: scale(1.35); }
  100% { transform: scale(1); }
}
/* 使用者系統設定「減少動態效果」時不播 */
@media (prefers-reduced-motion: reduce) {
  .cart-btn--bump .cart-icon,
  .cart-btn--bump .cart-badge {
    animation: none;
  }
}

/* 登入：外框膠囊 / 註冊：實心膠囊。兩者都是 router-link（會渲染成 <a>） */
.login-btn,
.register-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 18px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;             /* 拿掉連結底線 */
  white-space: nowrap;
  transition: background 0.18s ease, color 0.18s ease;
}
.login-btn {
  border: 1px solid var(--leaf);
  background: transparent;
  color: var(--leaf);
}
.login-btn:hover {
  background: var(--leaf-soft);
}
.register-btn {
  border: 1px solid var(--leaf);
  background: var(--leaf);
  color: #fff;
}
.register-btn:hover {
  background: var(--leaf-dark);
  border-color: var(--leaf-dark);
}

/* ========== 通知小鈴鐺 ========== */
.bell-wrap {
  position: relative;                /* 通知浮窗要靠它絕對定位 */
}
.bell-btn {
  position: relative;
  display: inline-grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  text-decoration: none;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.bell-wrap:hover .bell-btn {
  background: var(--leaf-soft);
  border-color: var(--leaf);
}
.bell-icon {
  display: inline-block;             /* span 預設 inline 吃不到 transform，要改 inline-block */
  font-size: 19px;
  line-height: 1;
  transform-origin: top center;      /* 以「鈴鐺掛勾」為軸心搖，才像真的鈴鐺 */
}
/* 滑過去鈴鐺左右搖，幅度遞減 → 像被撥了一下慢慢停 */
.bell-wrap:hover .bell-icon {
  animation: bell-ring 0.9s ease-in-out;
}
@keyframes bell-ring {
  0%   { transform: rotate(0); }
  15%  { transform: rotate(20deg); }
  30%  { transform: rotate(-16deg); }
  45%  { transform: rotate(11deg); }
  60%  { transform: rotate(-7deg); }
  75%  { transform: rotate(4deg); }
  100% { transform: rotate(0); }
}
/* 未讀數小紅點：加個心跳 pulse 提醒 */
.bell-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #c0392b;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  animation: badge-pulse 2s ease-in-out infinite;
}
@keyframes badge-pulse {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.18); }
}

/* 通知浮窗：預設隱藏，hover 鈴鐺（或浮窗本身）時淡入下滑出現 */
.notify-popover {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  padding: 12px 0 0;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-6px);
  transition: opacity 0.18s ease, transform 0.18s ease, visibility 0.18s;
  z-index: 40;
}
.bell-wrap:hover .notify-popover {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}
.notify-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 10px;
  border-bottom: 1px solid var(--line);
}
.notify-head strong {
  font-size: 15px;
  color: var(--ink);
}
.notify-unread {
  font-size: 12px;
  color: var(--leaf);
  font-weight: 600;
}
.notify-list {
  margin: 0;
  padding: 0;
  list-style: none;
  max-height: 320px;
  overflow-y: auto;
}
.notify-item {
  display: flex;
  gap: 10px;
  padding: 10px 16px;
  transition: background 0.15s ease;
}
.notify-item:hover {
  background: var(--leaf-soft);
}
.notify-item + .notify-item {
  border-top: 1px solid var(--line);
}
/* 未讀：左側綠色小豎條提示 */
.notify-item.unread {
  box-shadow: inset 3px 0 0 var(--leaf);
}
.notify-icon {
  font-size: 18px;
  line-height: 1.4;
}
.notify-body {
  min-width: 0;                      /* 讓長文字能正常截斷 */
}
.notify-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink);
}
.notify-text {
  margin: 2px 0 0;
  font-size: 12.5px;
  color: var(--muted);
  line-height: 1.45;
  display: -webkit-box;              /* 最多兩行，超過截斷 */
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.notify-time {
  font-size: 11px;
  color: var(--muted);
}
.notify-more {
  display: block;
  padding: 10px 16px;
  border-top: 1px solid var(--line);
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--leaf);
  text-decoration: none;
  transition: background 0.15s ease;
}
.notify-more:hover {
  background: var(--leaf-soft);
}

/* ========== 帳號下拉選單 ========== */
.account-wrap {
  position: relative;
}
.account-toggle {
  cursor: pointer;                   /* 改成 button 後補上手指游標 */
  font-family: inherit;
}
.account-caret {
  font-size: 11px;
  line-height: 1;
  transition: transform 0.2s ease;
}
.account-wrap:hover .account-caret {
  transform: rotate(180deg);         /* hover 展開時箭頭轉上 */
}
.account-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 180px;
  padding: 6px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-6px);
  transition: opacity 0.18s ease, transform 0.18s ease, visibility 0.18s;
  z-index: 40;
}
/* 用透明橋接補上膠囊與選單之間的 8px 空隙,滑過去時 hover 不會斷 */
.account-menu::before {
  content: "";
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}
.account-wrap:hover .account-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}
.account-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 9px;
  font-size: 14px;
  color: var(--ink);
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.15s ease, color 0.15s ease;
}
.account-menu-item:hover {
  background: var(--leaf-soft);
  color: var(--leaf);
}
.account-menu-icon {
  font-size: 15px;
  line-height: 1;
}

/* 已登入：個人中心膠囊 + 登出 */
.account-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 16px 7px 12px;
  border-radius: 999px;
  border: 1px solid var(--leaf);
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  max-width: 180px;
  transition: background 0.18s ease;
}
.account-btn:hover {
  background: var(--leaf);
  color: #fff;
}
.account-avatar {
  font-size: 15px;
  line-height: 1;
}
.account-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.logout-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.logout-btn:hover {
  border-color: #c0392b;
  color: #c0392b;
}

/* ========== 響應式（窄螢幕 ≤ 820px） ========== */
@media (max-width: 820px) {
  .site-header {
    grid-template-columns: 1fr auto; /* 左品牌、右 ☰ */
  }
  .mobile-menu {
    display: inline-grid;
    place-items: center;
  }
  /* 行動版空間有限：不論捲動與否都只留 icon + Farmily */
  .brand-text small,
  .brand-seal {
    display: none;
  }
  .main-nav {
    grid-column: 1 / -1;             /* 換到下一整行 */
    flex-direction: column;          /* 直向排列 */
    align-items: stretch;
    display: none;                   /* 預設收起 */
  }
  .main-nav.open {
    display: flex;                   /* 點 ☰ 後才展開 */
  }
  .user-zone {
    margin-left: 0;
    flex-wrap: wrap;                 /* 鈴鐺 + 帳號 + 登出擠不下時換行 */
  }
  /* 手機版：帳號下拉改成往下推擠、佔滿整行，避免被裁掉 */
  .account-wrap {
    width: 100%;
  }
  .account-menu {
    position: static;
    width: 100%;
    box-shadow: none;
  }
  /* 手機沒有 hover，通知浮窗直接隱藏 → 點鈴鐺就是進通知頁 */
  .notify-popover {
    display: none;
  }
}
</style>
