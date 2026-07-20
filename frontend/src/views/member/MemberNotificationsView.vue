<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router';
import notificationApi from '@/api/memberNotification';
import { confirm } from '@/composables/useConfirm';
import notificationStore from '@/stores/memberNotification';

const notifs = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const targetType = ref('')
const marking = ref(false)
const router = useRouter()
const activeIndex = computed(() => TYPE_OPTIONS.findIndex(o => o.value === targetType.value))


// 分類標籤選項
// targetType: account, order, groupbuy, trip, blog,...
const TYPE_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'account', label: '帳號' },
  { value: 'order', label: '訂單' },
  { value: 'groupbuy', label: '團購' },
  { value: 'trip', label: '體驗活動' },
  { value: 'blog', label: '部落格' },
  { value: 'system', label: '系統公告' }
]

// 通知列表代碼
const TYPE_LABEL = {
  account: '帳號',
  order: '訂單',
  groupbuy: '團購',
  trip: '體驗活動', 
  blog: '部落格',
  system: '系統公告'
}

// 跳轉頁面路徑 (會員端)
const TARGET_ROUTE = {
  account: '/member/me',
  order: '/member/orders',
  groupbuy: '/member/group-buys',
  trip: '/member/farm-trips', 
  blog: '/member/blogs',
}


// 已讀標籤
// const STATUS_LABEL = {
//   unread: '未讀',
//   read: '已讀'
// }

onMounted(loadNotif)

// 載入通知 (取得 notifs 陣列)
async function loadNotif() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await notificationApi.list(targetType.value, page.value)
    notifs.value = res.content || [] // 把這頁的通知列存進 notifs 陣列
    totalPages.value = res.totalPages

  } catch (e) {
    loadError.value = e.message || '載入通知失敗'

  } finally {
    loading.value = false
  }
}

// 切換分類
function changeType(t) {
  targetType.value = t
  page.value = 0
  loadNotif()
}

// 換頁
async function changePage(p) {
  if (p < 0 || p >= totalPages.value) {
    return
  }

  window.scrollTo({ top: 0 })
  page.value = p
  await loadNotif()

}

// 全部標為已讀
async function markAll() {
  const ok = await confirm({
    title: '全部已讀',
    message: '確定全部標為已讀嗎?',
    confirmText: '全部已讀'
  })
  if (!ok) { return }

  marking.value = true
  try {
    await notificationApi.markAllAsRead()
    notificationStore.markAllRead()
    await loadNotif()
  } catch (e) {
    alert(e.message || '操作失敗')
  } finally {
    marking.value = false
  }
}

// 單筆標為已讀
async function markOne(n) {
  if (n.status === 'unread') {
    try {
      await notificationApi.markOneAsRead(n.notificationId)
      notificationStore.markRead(n.notificationId)
      n.status = 'read'
    } catch (e) {
      alert(e.message || '操作失敗')
      return
    }
  }

  // blog 通知導到「那篇文章」；其他類型導到對應區塊清單
  if (n.targetType === 'blog' && n.targetId != null) {
    router.push({ name: 'member-blogs-detail', params: { id: n.targetId } })
    return
  }

  const path = TARGET_ROUTE[n.targetType]
  if (path) {
    router.push(path)
  }
}

// 格式化顯示時間
// dt = 後端 LocalDateTime，序列化成 JSON 後的 ISO 字串 (ex. "2026-07-07T10:30:00")
function formateDateTime(dt) {
  if (!dt) { return '' }  // 沒值 (null/undefined/空字串) 就回空字串，避免 "Invalid Date"
  return new Date(dt).toLocaleString('zh-TW', { hour12: false })
}


</script>

<template>
  <main class="notif-page">
    <div class="notif-wrap">

      <header class="page-head">
        <h1>🔔 我的通知</h1>
      </header>

      <div class="notif-toolbar">
        <div class="seg" role="tablist" :style="{ '--seg-count': TYPE_OPTIONS.length }">
          <span class="seg-pill" :style="{ transform: `translateX(${activeIndex * 100}%)` }"></span>

          <button v-for="opt in TYPE_OPTIONS" :key="opt.value" type="button"
          class="seg-btn" :class="{ 'is-active': targetType === opt.value }"
          role="tab" :aria-selected="targetType === opt.value"
          @click="changeType(opt.value)">
            {{ opt.label }}
          </button>
        </div>
          
        <button class="btn-ghost mark-all" :disabled="marking" @click="markAll">
          {{ marking ? '處理中...' : '全部已讀' }} <!-- 處理中 marking == true，換字並禁用 -->
        </button>
      </div>

      <!-- 載入狀態 -->
      <Transition name="tab-fade" mode="out-in">
        <div :key="targetType + '-' + page">
          <p v-if="loading" class="hint">載入中...</p>
          <p v-else-if="loadError" class="msg-err">{{ loadError }}</p>
          <p v-else-if="notifs.length === 0" class="hint">暫無通知</p>
    
          <!-- 非以上三種狀態，載入通知列表 -->
          <ul v-else class="notif-list">
            <li 
              v-for="n in notifs" :key="n.notificationId"
              class="notif-item" :class="{ unread: n.status === 'unread' }"
              @click="markOne(n)"
            >
              <span class="notif-tag" :class="'tag-' + (n.targetType || 'other')">
                {{ TYPE_LABEL[n.targetType] || '其他' }}
              </span>
              <div class="notif-main">
                <p class="notif-contnet">{{ n.content }}</p>
                <span class="notif-time">{{ formateDateTime(n.createdAt) }}</span>
              </div>
              <span v-if="n.status === 'unread'" class="dot" title="unread"></span>
            </li>
          </ul>
    
          <!-- 分頁 (超過一頁才顯示) -->
          <div v-if="totalPages > 1" class="pager">
            <button class="btn-ghost" :disabled="page === 0" @click="changePage(page - 1)">上一頁</button>
            <span class="pager-info">第 {{ page + 1}} / {{ totalPages }} 頁</span>
            <button class="btn-ghost" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">下一頁</button>
          </div>
        </div>
      </Transition>
    </div>
  </main>
</template>

<style scoped>
.notif-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notif-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head h1 {
  margin: 0;
  font-size: 24px;
  color: var(--ink);
}

/* 分類列容器: seg 靠左、全部已讀靠右 */
.notif-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

/* 切換膠囊 */
.seg {
  position: relative;
  display: grid;
  grid-template-columns: repeat(var(--seg-count), 1fr);
  padding: 4px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 999px;
  box-shadow: var(--shadow);
}

/* 會滑動的綠藥丸: 寬度 = 一格, transform 由 activeIndex 控制 */
.seg-pill {
  position: absolute;
  top: 4px; bottom: 4px; left: 4px;
  width: calc((100% - 8px) / var(--seg-count));           /* 內容區寬度 ÷ 6 = 一格 */
  border-radius: 999px;
  background: linear-gradient(135deg, var(--leaf), var(--leaf-dark));
  box-shadow: 0 3px 10px rgba(59, 138, 87, 0.35);
  transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);  /* 滑動 (滑動速度: 0.28s) */
}

.seg-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: var(--ink-soft);
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.22s ease;
}
.seg-btn:hover { color: var(--leaf-dark); }
.seg-btn.is-active { color: #fff; }          /* 選中變白字 (蓋在綠藥丸上) */

/* 列表淡入轉場 */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.tab-fade-enter-from { opacity: 0; transform: translateY(8px); }   /* 進場: 從下淡入 (translateY 淡入幅度) */
.tab-fade-leave-to   { opacity: 0; transform: translateY(-8px); }  /* 離場: 往上淡出 */

/* 全部已讀: 獨立靠右 + 膠囊圓角 */
.btn-ghost.mark-all {
  margin-left: auto;          /* 推到最右 */
  border-radius: 999px;       /* 圓角膠囊, 跟 seg 風格一致 */
}


/* 通知清單 */
.notif-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notif-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff;
  border: 1px solid var(--line);
  border-left: 4px solid var(--leaf);
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: box-shadow .18s ease, transform .18s ease;
}

.notif-item.unread {
  background: var(--leaf-soft);
}

.notif-item:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

/* 未讀: 綠邊 + 淺綠底 */
.notif-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.notif-content {
  margin: 0;
  color: var(--ink);
  font-size: 15px;
}

.notif-tag {
  flex-shrink: 0;              /* 不被壓縮, 維持固定寬 */
  width: 72px;
  box-sizing: border-box;
  text-align: center;
  background: var(--leaf-soft); /* 淺綠底 */
  color: var(--leaf-dark);      /* 深綠字 */
  padding: 3px 10px;
  border-radius: 999px;        /* 膠囊造型 */
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;          /* 「訂單」不換行 */
}

.notif-time {
  color: var(--muted);
  font-size: 12px;
}

/* 未讀圓點 */
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--leaf);
  flex-shrink: 0;
}

.tag-account  { background: #ffe4e4; color: #d60000; }  /* 會員:紅 */
.tag-order    { background: #fdeede; color: #b5651d; }  /* 訂單:橘 */
.tag-groupbuy { background: #e3eefb; color: #2f5fa5; }  /* 團購:藍 */
.tag-trip     { background: #f7f59f; color: #8f8d05; }  /* 體驗活動:黃 */
.tag-blog     { background: #f1e0ff; color: #790ac4; }  /* 部落格:紫*/
.tag-system   { background: #eff3ea; color: #49553c; }  /* 系統公告:墨綠*/
/* ...沒對到的就用預設綠色 */

/* 分頁 */
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 8px;
}

.pager-info {
  color: var(--ink-soft);
  font-size: 14px;
}

/* 共用小元素 */
.btn-ghost {
  padding: 8px 16px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  color: var(--ink-soft);
  cursor: pointer;
  font-size: 14px;
}

.btn-ghost:hover:not(:disabled) {
  border-color: var(--leaf);
  color: var(--leaf);
}

.btn-ghost:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.hint {
  color: var(--muted);
  text-align: center;
  padding: 24px 0;
}

.msg-err {
  color: #c0392b;
  text-align: center;
  padding: 24px 0;
}
</style>
