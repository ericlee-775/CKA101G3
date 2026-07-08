<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import cartStore from '@/stores/cart'
import authApi from '@/api/auth'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()

// 購物車總件數（給右上角小紅點用）；超過 99 顯示成 99+
const cartCount = computed(() => cartStore.totalCount.value)

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
// 點帳號膠囊展開/收合；點選單外任何地方會自動收起（見下方 document 監聽）
const isAccountMenuOpen = ref(false)
const accountWrap = ref(null) // 綁在外層 div，用來判斷點擊是否發生在選單外

// 會員中心選單（跟 MemberLayout 的側邊選單一致，但不含「通知」→ 通知走小鈴鐺）
const accountMenu = [
  { label: '個人資料', icon: '👤', to: '/member/me' },
  { label: '我的文章', icon: '✍️', to: '/member/blogs' },
  { label: '我的商品', icon: '📦', to: '/member/products' },
  { label: '我的團購', icon: '👥', to: '/member/group-buys' },
  { label: '我的收藏', icon: '❤️', to: '/member/favorites' },
  { label: '我的訂單', icon: '🧾', to: '/member/orders' },
  { label: '優惠券', icon: '🎟️', to: '/member/coupons' },
]

// 點到選單外就收起下拉
function onDocClick(e) {
  if (isAccountMenuOpen.value && accountWrap.value && !accountWrap.value.contains(e.target)) {
    isAccountMenuOpen.value = false
  }
}
onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))

// ===== 通知小鈴鐺 =====
// 假資料先寫死五筆，之後接後端 API 再換掉
const notifications = ref([
  { id: 1, icon: '📦', title: '訂單已出貨', text: '您的訂單 #20260705001 已出貨，預計 2 天內送達。', time: '5 分鐘前', unread: true },
  { id: 2, icon: '👥', title: '團購即將成團', text: '「有機小番茄 5 斤箱」再 2 人即可成團！', time: '1 小時前', unread: true },
  { id: 3, icon: '🎟️', title: '優惠券即將到期', text: '您有 1 張 9 折券將於 7/10 到期，記得使用。', time: '3 小時前', unread: true },
  { id: 4, icon: '💬', title: '文章有新留言', text: '有人在您的文章「夏日果醬手作」留言了。', time: '昨天', unread: false },
  { id: 5, icon: '🌱', title: '收藏的商品降價', text: '您收藏的「紅心芭樂」降價 15%，快去看看！', time: '2 天前', unread: false },
])
// 未讀數（給鈴鐺右上角小紅點）
const unreadCount = computed(() => notifications.value.filter((n) => n.unread).length)

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
    <!-- 品牌區：點 logo 回首頁。只保留「一張 logo + 名稱 + 標語」，乾淨不重複 -->
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
      <router-link class="nav-link" to="/group-buys">團購</router-link>
      <router-link class="nav-link" to="/blogs">部落格</router-link>
      <router-link class="nav-link" to="/farm-trips">體驗活動</router-link>
      <router-link class="nav-link" to="/farm-map">產地地圖</router-link>

      <!-- 右側使用者區：購物車 + (未登入顯示 登入/註冊；已登入顯示 個人中心 + 登出) -->
      <div class="user-zone">
        <!-- 購物車：任何身分都看得到，點了到購物車頁 -->
        <router-link class="cart-btn" to="/cart" aria-label="購物車">
          <span class="cart-icon">🛒</span>
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

            <!-- hover 小視窗：先用前端寫死的五筆假資料 -->
            <div class="notify-popover">
              <div class="notify-head">
                <strong>通知</strong>
                <span v-if="unreadCount > 0" class="notify-unread">{{ unreadCount }} 則未讀</span>
              </div>
              <ul class="notify-list">
                <li v-for="n in notifications" :key="n.id" class="notify-item" :class="{ unread: n.unread }">
                  <span class="notify-icon">{{ n.icon }}</span>
                  <div class="notify-body">
                    <p class="notify-title">{{ n.title }}</p>
                    <p class="notify-text">{{ n.text }}</p>
                    <small class="notify-time">{{ n.time }}</small>
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

          <!-- 會員：點帳號膠囊展開下拉選單（@click.stop 避免冒泡到 nav 把手機選單收掉） -->
          <div v-else class="account-wrap" ref="accountWrap">
            <button
              class="account-btn account-toggle"
              type="button"
              :aria-expanded="isAccountMenuOpen"
              @click.stop="isAccountMenuOpen = !isAccountMenuOpen"
            >
              <span class="account-avatar">👤</span>
              <span class="account-name">{{ displayName }}</span>
              <span class="account-caret" :class="{ open: isAccountMenuOpen }">▾</span>
            </button>

            <transition name="menu-pop">
              <div v-if="isAccountMenuOpen" class="account-menu">
                <router-link
                  v-for="item in accountMenu"
                  :key="item.to"
                  class="account-menu-item"
                  :to="item.to"
                  @click="isAccountMenuOpen = false"
                >
                  <span class="account-menu-icon">{{ item.icon }}</span>
                  {{ item.label }}
                </router-link>
              </div>
            </transition>
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
  background: var(--cream);
  backdrop-filter: blur(14px);       /* 毛玻璃效果 */
  border-bottom: 1px solid var(--line);
  box-shadow: var(--shadow);
}

/* ========== 品牌區 ========== */
.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;             /* 拿掉 router-link 預設底線 */
}
.brand-logo {
  width: 44px;
  height: 44px;
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
.brand-text small {
  font-size: 12px;
  color: var(--muted);
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
.cart-icon {
  font-size: 19px;
  line-height: 1;
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
.account-caret.open {
  transform: rotate(180deg);         /* 展開時箭頭轉上 */
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
  z-index: 40;
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
/* 下拉展開/收合的小動畫（配合 <transition name="menu-pop">） */
.menu-pop-enter-active,
.menu-pop-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}
.menu-pop-enter-from,
.menu-pop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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
