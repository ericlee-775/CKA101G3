<script setup>
// 會員中心：我的團購
//
// 這一頁只負責「外框」：標題、切換膠囊、轉場動畫（比照 MemberFavoritesView.vue 的作法）。
// 內容一律寫在獨立元件裡：
//   - 進行中（尚未成團）→ components/member/GroupBuyJoinedList.vue（GET /api/member/groupBuy/joinedGroupBuyList）
//   - 訂單（已成團）    → components/member/GroupBuyOrderList.vue（GET /api/member/groupBuy/mySuccessOrders）
import { ref, computed } from 'vue'
import GroupBuyJoinedList from '@/components/member/GroupBuyJoinedList.vue'
import GroupBuyOrderList from '@/components/member/GroupBuyOrderList.vue'

const tabs = [
  { key: 'joined', label: '進行中', icon: '👥' },
  { key: 'orders', label: '訂單', icon: '🧾' },
]

const activeKey = ref('joined')
const activeIndex = computed(() => tabs.findIndex((t) => t.key === activeKey.value))

// 各分頁的筆數（子元件載入完會 emit('count') 回來），顯示成 tab 上的小數字
const counts = ref({ joined: null, orders: null })
function setCount(key, n) {
  counts.value[key] = n
}
</script>

<template>
  <section class="fav-page">
    <header class="page-head">
      <h1>👥 我的團購</h1>

      <!-- 切換膠囊：底下有一顆會滑動的綠色藥丸，位置跟著 activeIndex 移動 -->
      <div class="seg" role="tablist">
        <span
          class="seg-pill"
          :style="{ transform: `translateX(${activeIndex * 100}%)` }"
          aria-hidden="true"
        ></span>
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="seg-btn"
          :class="{ 'is-active': tab.key === activeKey }"
          type="button"
          role="tab"
          :aria-selected="tab.key === activeKey"
          @click="activeKey = tab.key"
        >
          <span class="seg-icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
          <span v-if="counts[tab.key] != null" class="seg-count">{{ counts[tab.key] }}</span>
        </button>
      </div>
    </header>

    <!-- KeepAlive：切換時保留元件狀態，不會每次都重打 API；
         Transition：淡入 + 輕微上移的轉場。
         count 的 key 直接寫死，避免 API 較慢時使用者已切走、筆數記到錯的分頁 -->
    <Transition name="tab-fade" mode="out-in">
      <KeepAlive>
        <GroupBuyJoinedList
          v-if="activeKey === 'joined'"
          @count="(n) => setCount('joined', n)"
        />
        <GroupBuyOrderList v-else @count="(n) => setCount('orders', n)" />
      </KeepAlive>
    </Transition>
  </section>
</template>

<style scoped>
.fav-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}
.page-head h1 {
  margin: 0;
  font-size: 22px;
  color: var(--ink);
}

/* ===== 切換膠囊（segmented control）===== */
.seg {
  position: relative;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  padding: 4px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 999px;
  box-shadow: var(--shadow);
}

/* 會滑動的綠色藥丸：寬度 = 一格，transform 由 activeIndex 控制 */
.seg-pill {
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 4px;
  width: calc(50% - 4px);
  border-radius: 999px;
  background: linear-gradient(135deg, var(--leaf), var(--leaf-dark));
  box-shadow: 0 3px 10px rgba(59, 138, 87, 0.35);
  transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

.seg-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 9px 20px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--ink-soft);
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.22s ease;
}
.seg-btn:hover {
  color: var(--leaf-dark);
}
.seg-btn.is-active {
  color: #fff;
}
.seg-icon {
  font-size: 15px;
}

/* 筆數小徽章：未選取時是淺綠底，選取時反白 */
.seg-count {
  min-width: 20px;
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 12px;
  text-align: center;
  transition: background 0.22s ease, color 0.22s ease;
}
.seg-btn.is-active .seg-count {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* ===== 分頁切換的轉場：淡入 + 輕微上移 ===== */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.tab-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.tab-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 窄螢幕：標題和膠囊上下堆疊，膠囊撐滿寬度 */
@media (max-width: 560px) {
  .page-head {
    flex-direction: column;
    align-items: stretch;
  }
  .seg-btn {
    padding: 9px 10px;
    font-size: 13px;
  }
}
</style>
