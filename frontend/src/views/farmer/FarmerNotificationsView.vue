<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { notificationApi } from '@/api/farmerNotification';
import { confirm } from '@/composables/useConfirm';
import notificationStore from '@/stores/farmerNotification';

const notifs = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const targetType = ref('')
const marking = ref(false)
const router = useRouter()


// 分類開選的選項
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
  account: '/farmer/me',
  order: '/farmer/orders',
  groupbuy: '/farmer/group-buys',
  trip: '/farmer/farm-trips', 
  blog: '/farmer/blog',
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
  // blog 通知導到「那篇文章」(小農自己的文章頁，隱藏也看得到)；其他類型導到對應區塊清單
  if (n.targetType === 'blog' && n.targetId != null) {
    router.push({ name: 'farmer-blog-detail', params: { id: n.targetId } })
    return
  }
  const path = TARGET_ROUTE[n.targetType]
  if(path) {
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
  <main class="farmer-page">

    <header class="page-head">
      <h1>🔔 通知</h1>
    </header>

    <section class="card">
      <nav class="sub-tabs">
        <button v-for="opt in TYPE_OPTIONS" :key="opt.value" type="button" class="sub-tab-btn" :class="{ 'sub-tab-btn--active': targetType === opt.value }"
        @click="changeType(opt.value)">{{ opt.label }}</button>
        <!-- :class="{active: 條件}" 用來高亮目前的分類 -->
        <button class="btn-ghost mark-all" :disabled="marking" @click="markAll">
          {{ marking ? '處理中...' : '全部已讀' }}
        </button>
      </nav>

      <Transition name="tab-fade" mode="out-in">
        <div :key="targetType + '-' + page">
          <!-- 載入狀態 -->
          <p v-if="loading" class="state">載入中...</p>
          <p v-else-if="loadError" class="state state-error">{{ loadError }}</p>
          <p v-else-if="notifs.length === 0" class="state">暫無通知</p>
    
          <!-- 非以上三種狀態，載入通知列表 -->
          <ul v-else class="notif-list">
            <li v-for="n in notifs" :key="n.notificationId" class="notif-row" :class="{ unread: n.status === 'unread' }"
              @click="markOne(n)">
              <span class="notif-tag" :class="'tag-' + (n.targetType || 'other')">
                {{ TYPE_LABEL[n.targetType] || '其他' }}
              </span>
              <div class="notif-main">
                <p class="notif-contnet">{{ n.content }}</p>
                <time class="notif-time" :datetime="n.createdAt">{{ formateDateTime(n.createdAt) }}</time>
              </div>
              <span v-if="n.status === 'unread'" class="dot" title="unread"></span>
            </li>
          </ul>
    
          <!-- 分頁 (超過一頁才顯示) -->
          <div v-if="totalPages > 1" class="pager">
            <button class="btn-ghost" :disabled="page === 0" @click="changePage(page - 1)">上一頁</button>
            <span class="pager-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
            <button class="btn-ghost" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">下一頁</button>
          </div>
        </div>
      </Transition>
    </section>
  </main>
</template>

<style scoped>

/* 頁面外框 & 標題 */
.farmer-page { padding: 32px 24px; }
.page-head { margin-bottom: 20px; }
.page-head h1 { margin: 0; font-size: 24px; color: var(--ink); }

/* 大白框 + 上緣綠條:全站共用面板 */
.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
  border-top: 3px solid var(--leaf);
}

/* 分類篩選藥丸 */
.sub-tabs {
  display: flex;
  align-items: center;      /* 藥丸和按鈕垂直對齊 */
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 18px;
}
.sub-tab-btn {
  padding: 6px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-soft);
  font-size: 13px;
  cursor: pointer;
  transition: background .18s ease, border-color .18s ease, color .18s ease;
}
.sub-tab-btn:hover { border-color: var(--leaf); }

/* active: 淺綠底 + 綠框 + 深綠字 */
.sub-tab-btn--active {
  background: var(--leaf-soft);
  border-color: var(--leaf);
  color: var(--leaf-dark);
  font-weight: 600;
}

/* 列表淡入轉場 */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.tab-fade-enter-from { opacity: 0; transform: translateY(8px); }   /* 進場: 從下淡入 (translateY 淡入幅度) */
.tab-fade-leave-to   { opacity: 0; transform: translateY(-8px); }  /* 離場: 往上淡出 */


/* 全部已讀: 推到最右 */
.mark-all { margin-left: auto; }

/* 狀態文字: 面板內簡單置中(對齊 .state) */
.state { text-align: center; color: var(--muted); padding: 24px 0; }
.state-error { color: #c0392b; }

/* 清單 */
.notif-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 14px; }

/* 每列: 淡邊框、無陰影、無浮起 (對齊 .product-row) */
.notif-row {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: border-color .15s ease;
}
.notif-row:hover { border-color: var(--leaf); }
.notif-row.unread { background: var(--leaf-soft); border-color: var(--leaf); }

/* 左側分類標籤 (固定寬、置中) */
.notif-tag {
  flex-shrink: 0; width: 72px; box-sizing: border-box; text-align: center;
  padding: 3px 10px; border-radius: 999px;
  background: var(--leaf-soft); color: var(--leaf-dark);
  font-size: 12px; font-weight: 600; white-space: nowrap;
}

/* 分類標籤配色 (沿用會員頁), 沒對到的就用預設綠色 */
.tag-account  { background: #ffe4e4; color: #d60000; }  /* 會員:紅 */
.tag-order    { background: #fdeede; color: #b5651d; }  /* 訂單:橘 */
.tag-groupbuy { background: #e3eefb; color: #2f5fa5; }  /* 團購:藍 */
.tag-trip     { background: #f7f59f; color: #8f8d05; }  /* 體驗活動:黃 */
.tag-blog     { background: #f1e0ff; color: #790ac4; }  /* 部落格:紫*/
.tag-system   { background: #eff3ea; color: #49553c; }  /* 系統公告:墨綠*/

/* 中間內容 (min-width:0 讓長文字正常收合) */
.notif-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.notif-content { margin: 0; color: var(--ink); font-size: 15px; }
.notif-time { font-size: 12px; color: var(--muted); }

/* 未讀圓點 */
.dot { flex-shrink: 0; width: 10px; height: 10px; border-radius: 50%; background: var(--leaf); }

/* 分頁 */
.pager { display: flex; align-items: center; justify-content: center; gap: 14px; margin-top: 16px; }
.pager-info { color: var(--ink-soft); font-size: 14px; }

/* 按鈕 */
.btn-ghost {
  padding: 7px 16px; border: 1px solid var(--line); border-radius: 10px;
  background: #fff; color: var(--ink-soft); cursor: pointer; font-size: 14px;
}
.btn-ghost:hover:not(:disabled) { border-color: var(--leaf); color: var(--leaf); }
.btn-ghost:disabled { opacity: .5; cursor: not-allowed; }

</style>