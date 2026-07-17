<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import notificationApi from '@/api/memberNotification';
import { confirm } from '@/composables/useConfirm';
import notificationStore from '@/stores/memberNotification';

const router = useRouter()

const notifs = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const targetType = ref('')
const marking = ref(false)


// 分類開選的選項
// targetType: account, order, groupbuy, trip, blog,...
const TYPE_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'account', label: '帳號' },
  { value: 'order', label: '訂單' },
  { value: 'groupbuy', label: '團購' },
  { value: 'trip', label: '體驗活動' },
  { value: 'blog', label: '專欄文章' }
]

// 通知列表代碼
const TYPE_LABEL = {
  account: '帳號',
  order: '訂單',
  groupbuy: '團購',
  trip: '體驗活動', 
  blog: '專欄文章'
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
function changePage(p) {
  if (p < 0 || p > totalPages.value) {
    return
  }
  page.value = p
  loadNotif()
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
      await loadNotif()
    } catch (e) {
      alert(e.message || '操作失敗')
    }
  }
}

// 依 targetType 決定這則通知要導向哪一頁；目前只有 blog 有對應頁面，其餘回 null
function resolveTarget(n) {
  if (n.targetType === 'blog' && n.targetId != null) {
    return { name: 'member-blogs-detail', params: { id: n.targetId } }
  }
  return null
}

// 點整列：能導向的先標已讀再跳頁；不能導向的維持原本「只標已讀」行為
function openNotif(n) {
  const to = resolveTarget(n)
  if (to) {
    // 要跳頁，不必等列表重載；標已讀採 fire-and-forget，並樂觀更新本列狀態
    if (n.status === 'unread') {
      n.status = 'read'
      notificationApi.markOneAsRead(n.notificationId)
        .then(() => notificationStore.markRead(n.notificationId))
        .catch(() => {})
    }
    router.push(to)
  } else {
    markOne(n)
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
      
      <nav class="filter">
        <button v-for="opt in TYPE_OPTIONS" :key="opt.value" 
        class="chip" :class="{ active: targetType === opt.value }"
        @click="changeType(opt.value)">{{ opt.label }}</button>
        <!-- :class="{active: 條件}" 用來高亮目前的分類 -->
        
        <button class="btn-ghost mark-all" :disabled="marking" @click="markAll">
          {{ marking ? '處理中...' : '全部已讀' }} <!-- 處理中 marking == true，換字並禁用 -->
        </button>
      </nav>

      <!-- 載入狀態 -->
      <p v-if="loading" class="hint">載入中...</p>
      <p v-else-if="loadError" class="msg-err">{{ loadError }}</p>
      <p v-else-if="notifs.length === 0" class="hint">暫無通知</p>

      <!-- 非以上三種狀態，載入通知列表 -->
      <ul v-else class="notif-list">
        <li
          v-for="n in notifs" :key="n.notificationId"
          class="notif-item"
          :class="{ unread: n.status === 'unread', 'notif-item--link': resolveTarget(n) }"
          @click="openNotif(n)"
        >
          <span class="notif-tag" :class="'tag-' + (n.targetType || 'other')">
            {{ TYPE_LABEL[n.targetType] || '其他' }}
          </span>
          <div class="notif-main">
            <p class="notif-content">
              {{ n.content }}
              <span v-if="resolveTarget(n)" class="notif-go">查看文章 →</span>
            </p>
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

/* 分類 chip */
.filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 6px 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink-soft);
  cursor: pointer;
  font-size: 14px;
}

/* 選中高亮 */
.chip.active {
  background: var(--leaf);
  color: #fff;
  border-color: var(--leaf);
}

.mark-all {
  margin-left: auto;
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

/* 未讀:綠邊 + 淺綠底 */
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
  align-self: flex-start;      /* 貼齊頂端, 和第一行文字對齊 */
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

/* 可導向的通知：內文後面的「查看文章 →」連結提示 */
.notif-go {
  margin-left: 8px;
  white-space: nowrap;
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 600;
}
.notif-item--link:hover .notif-go {
  text-decoration: underline;
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
.tag-blog     { background: #f1e0ff; color: #790ac4; }  /* 專欄文章:紫*/
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
