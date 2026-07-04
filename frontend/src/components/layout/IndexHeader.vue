<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import authApi from '@/api/auth'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()

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
      <router-link class="nav-link" to="/farmily">農場</router-link>
      <router-link class="nav-link" to="/products">全部商品</router-link>
      <router-link class="nav-link" to="/group-buys">團購</router-link>
      <router-link class="nav-link" to="/blogs">部落格</router-link>
      <router-link class="nav-link" to="/farm-trips">體驗活動</router-link>
      <router-link class="nav-link" to="/farm-map">產地地圖</router-link>
      <router-link class="nav-link" to="/farm-game">小農遊戲</router-link>

      <!-- 右側使用者區：未登入顯示 登入/註冊；已登入顯示 個人中心 + 登出 -->
      <div class="user-zone">
        <template v-if="authStore.isLoggedIn">
          <router-link class="account-btn" :to="accountLink">
            <span class="account-avatar">👤</span>
            <span class="account-name">{{ displayName }}</span>
          </router-link>
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
  padding: 12px clamp(18px, 4vw, 56px);
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
  overflow-x: auto;                  /* 項目太多時可橫向捲動 */
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
  }
}
</style>

<style>

</style>
