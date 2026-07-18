<script setup>
import { computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import authApi from '@/api/auth'
import { confirm } from '@/composables/useConfirm'
import notificationStore from '@/stores/farmerNotification'

const router = useRouter()

// 顯示小農名稱：優先農場名，退回 email
const displayName = computed(() => {
  const u = authStore.state.user
  if (!u) return '小農'
  return u.farmName || u.email || '小農'
})

// 側邊欄選單：ready:true＝已完成可點；ready:false＝功能開發中，顯示但不可點
const menu = [
  { label: '商品管理', icon: '📦', to: '/farmer/products', ready: true },
  { label: '團購管理', icon: '🛒', to: '/farmer/group-buys', ready: true },
  { label: '訂單', icon: '🧾', to: '/farmer/orders', ready: true },
  { label: '體驗活動管理', icon: '🎪', to: '/farmer/farm-trips', ready: true },
  { label: '產地日記(部落格)', icon: '🌱', to: '/farmer/blog', ready: true },
  { label: '通知', icon: '🔔', to: '/farmer/notifications', ready: true, badge: 'notification' },
]
const readyMenu = computed(() => menu.filter((m) => m.ready))

// menu item 的 badge 欄位對到這張表的 key，沒有 badge 欄位的項目就不顯示數字
const badgeCounts = computed(() => ({
  notification: notificationStore.unreadCount.value,
}))

// 未讀數由側邊欄自己抓：小農後台沒有像商城 ShopHeader 那樣的鈴鐺在背景更新，
// 不自己抓的話數字會一直是 0，要等使用者點進通知頁才有，badge 就沒意義了。
// （會員中心是巢狀在 ShopLayout 底下、header 會抓，所以 MemberLayout 不用寫這段。）
async function loadUnread() {
  if (!authStore.isFarmer) return
  await notificationStore.refresh()
}

// 硬重整時 auth 狀態可能比元件晚就緒，這時 onMounted 那次會被擋掉，靠這裡補抓；登出則歸零
watch(() => authStore.isFarmer, (isFarmer) => {
  if (isFarmer) loadUnread()
  else notificationStore.reset()
})

// 進來先抓一次，之後每 300 秒更新一次（對齊 ShopHeader 鈴鐺的頻率）
let unreadTimer = null
onMounted(() => {
  loadUnread()
  unreadTimer = setInterval(loadUnread, 300000)
})
onBeforeUnmount(() => {
  if (unreadTimer) clearInterval(unreadTimer)
})

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
  <aside class="farmer-sidebar">
    <!-- 品牌區：點 logo 回商品首頁 -->
    <router-link class="side-brand" to="/farmer/products" aria-label="回商品首頁">
      <img
        class="side-logo"
        src="https://storage.googleapis.com/cka101-15/form/farmLogo.png?v=20260613-transparent"
        alt="Farmily logo"
      />
      <span class="side-brand-text">
        <strong>Farmily</strong>
        <small>小農管理前台</small>
      </span>
    </router-link>

    <!-- 目前登入的小農：點擊進入商家資料 -->
    <router-link class="side-user" to="/farmer/me" aria-label="商家資料">
      <span class="side-avatar">👤</span>
      <span class="side-user-name">{{ displayName }}</span>
    </router-link>

    <!-- 管理選單 -->
    <nav class="side-nav">
      <router-link
        v-for="item in readyMenu"
        :key="item.to"
        class="side-link"
        :to="item.to"
      >
        <span class="side-icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
        <span v-if="item.badge && badgeCounts[item.badge] > 0" class="side-badge">
          {{ badgeCounts[item.badge] > 99 ? '99+' : badgeCounts[item.badge] }}
        </span>
      </router-link>
    </nav>

    <!-- 底部：回商城前台 / 登出 -->
    <div class="side-footer">
      <router-link class="side-frontend" to="/" aria-label="回商城前台">
        <span class="side-icon">🏠</span>
        <span>回商城前台</span>
      </router-link>
      <button class="side-logout" type="button" @click="logout">登出</button>
    </div>
  </aside>
</template>

<style scoped>
.farmer-sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px 16px;
  background: var(--ink, #2f3a2a);
  color: #d8e0d6;
  position: sticky;
  top: 0;
  height: 100vh;
}

/* 品牌區 */
.side-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}
.side-logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}
.side-brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}
.side-brand-text strong {
  font-size: 17px;
  color: #fff;
}
.side-brand-text small {
  font-size: 11px;
  color: #aab8a6;
  letter-spacing: 0.04em;
}

/* 登入者 */
.side-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #ffffff14;
  color: #d8e0d6;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.18s ease;
}
.side-user:hover {
  background: #ffffff1f;
}
.side-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--leaf, #6a9a3f);
  font-size: 15px;
  flex-shrink: 0;
}
.side-user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 選單 */
.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.side-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 10px;
  color: #d8e0d6;
  font-size: 15px;
  text-decoration: none;
  transition: background 0.18s ease, color 0.18s ease;
}
.side-link:hover {
  background: #ffffff1a;
}
/* 目前所在頁面高亮 */
.side-link.router-link-active {
  background: var(--leaf, #6a9a3f);
  color: #fff;
}
.side-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

/* 未讀數：靠 margin-left:auto 推到選單最右側，不用 absolute，
   所以不需要父層 position:relative，也不會蓋到文字 */
.side-badge {
  margin-left: auto;
  flex-shrink: 0;
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
/* 選中時整條是綠底，紅底數字會跟綠色互斥刺眼，改白底綠字 */
.side-link.router-link-active .side-badge {
  background: #fff;
  color: var(--leaf, #6a9a3f);
}

/* 底部 */
.side-footer {
  margin-top: auto;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.side-frontend {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #ffffff14;
  color: #d8e0d6;
  font-size: 14px;
  text-decoration: none;
  transition: background 0.18s ease;
}
.side-frontend:hover {
  background: var(--leaf, #6a9a3f);
  color: #fff;
}
.side-logout {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #46523f;
  background: transparent;
  color: #d8e0d6;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.side-logout:hover {
  border-color: #c0392b;
  color: #e8746a;
}

/* 窄螢幕：側邊欄改成上方橫幅，選單橫向捲動 */
@media (max-width: 820px) {
  .farmer-sidebar {
    position: static;
    height: auto;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }
  .side-nav {
    flex-direction: row;
    flex-wrap: wrap;
    flex: 1 1 100%;
    overflow-x: auto;
  }
  .side-footer {
    margin-top: 0;
    flex-direction: row;
  }
}
</style>
