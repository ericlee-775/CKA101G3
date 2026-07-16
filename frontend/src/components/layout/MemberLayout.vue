<script setup>
// 會員中心版型：左側「選單卡」 + 右側內容區。
// 和小農後台（FarmerLayout）不同的是：會員中心是「逛商城的延伸」，
// 所以它巢狀掛在 ShopLayout 底下，保留頂部導覽列和購物車，
// 側邊欄也用淺色卡片風格，而不是後台的深色外殼。
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import authApi from '@/api/auth'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()

// 顯示名稱：優先 userName，退回 email
const displayName = computed(() => {
  const u = authStore.state.user
  if (!u) return '會員'
  return u.userName || u.userNickname || u.email || '會員'
})

// 側邊選單：之後訂單 / 優惠券 / 通知做好了，把對應路由的元件換掉即可，這裡不用動
const menu = [
  { label: '個人資料', icon: '👤', to: '/member/me' },
  { label: '我的文章', icon: '✍️', to: '/member/blogs' },
  { label: '我的團購', icon: '👥', to: '/member/group-buys' },
  { label: '我的收藏', icon: '❤️', to: '/member/favorites' },
  { label: '我的訂單', icon: '🧾', to: '/member/orders' },
  { label: '已報名活動', icon: '🌱', to: '/member/farm-trips' },
  { label: '優惠券', icon: '🎟️', to: '/member/coupons' },
  { label: '通知', icon: '🔔', to: '/member/notifications' },
]

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
  <div class="member-shell">
    <!-- 左側選單卡 -->
    <aside class="member-sidebar">
      <!-- 目前登入的會員 -->
      <div class="side-user">
        <span class="side-avatar">👤</span>
        <span class="side-user-name">{{ displayName }}</span>
      </div>

      <!-- 功能選單：router-link-active 會自動高亮目前所在頁 -->
      <nav class="side-nav">
        <router-link v-for="item in menu" :key="item.to" class="side-link" :to="item.to">
          <span class="side-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <button class="side-logout" type="button" @click="logout">登出</button>
    </aside>

    <!-- 右側內容區：router 會把對應的會員子頁畫在這裡 -->
    <section class="member-content">
      <router-view />
    </section>
  </div>
</template>

<style scoped>
.member-shell {
  display: grid;
  grid-template-columns: 220px 1fr;  /* 左:選單固定寬  右:內容撐滿 */
  gap: 24px;
  align-items: start;                /* 讓側邊欄不被拉高，sticky 才有效 */
  max-width: 1080px;
  margin: 0 auto;
  padding: 32px clamp(18px, 4vw, 56px) 56px;
}

/* ========== 側邊選單卡 ========== */
.member-sidebar {
  position: sticky;
  top: 90px;                         /* 停在 sticky header 底下 */
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px 14px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
}

.side-user {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 14px;
  font-weight: 600;
}
.side-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--leaf);
  font-size: 15px;
  flex-shrink: 0;
}
.side-user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

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
  color: var(--ink-soft);
  font-size: 15px;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.18s ease, color 0.18s ease;
}
.side-link:hover {
  background: var(--leaf-soft);
  color: var(--leaf);
}
.side-link.router-link-active {
  background: var(--leaf);
  color: #fff;
}
.side-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.side-logout {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease;
}
.side-logout:hover {
  border-color: #c0392b;
  color: #c0392b;
}

/* ========== 響應式（窄螢幕 ≤ 820px）：選單改成上方橫向 tab 列 ========== */
@media (max-width: 820px) {
  .member-shell {
    grid-template-columns: 1fr;      /* 上下堆疊 */
    gap: 16px;
    padding-top: 18px;
  }
  .member-sidebar {
    position: static;                /* 手機不需要 sticky */
    flex-direction: row;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  .side-nav {
    flex-direction: row;
    flex: 1 1 100%;
    overflow-x: auto;                /* 項目太多時可橫向捲動 */
  }
  .side-logout {
    margin-left: auto;
  }
}
</style>
