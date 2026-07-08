<script setup>
// 小農後台：團購管理
// 串接 FarmerGroupBuyController（/api/farmer/groupBuy，需以小農身分登入，session cookie 認證）：
//   GET  /api/farmer/groupBuy/list                    → GroupBuyFarmerDTO[]（自己商品底下的團購申請/清單）
//   POST /api/farmer/groupBuy/farmerResponse/{id}      → 審核（通過／拒絕），body: { requestStatus, rejectReason? }
//
// 注意：FarmerGroupBuyController 還有一支 GET /order/{groupBuyId} 可查團購訂單明細，
// 但後端 GroupBuyService.showOrder() 內部其實是拿這個 id 去查「訂單(order)」的主鍵，
// 而不是團購(group buy)的主鍵，兩者目前對不上，GroupBuyFarmerDTO 也沒有回傳 orderId，
// 沒有可靠的 id 能打這支 API，因此這裡先不接，避免做出一個看似能用、實際上會查錯筆資料的功能。
import { ref, onMounted } from 'vue'
import farmerGroupBuyApi from '@/api/farmerGroupBuy'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
import { confirm } from '@/composables/useConfirm'

const list = ref([])
const loading = ref(true)
const error = ref('')

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    list.value = await farmerGroupBuyApi.list()
  } catch (e) {
    error.value = e.message || '無法載入團購清單，請稍後再試。'
  } finally {
    loading.value = false
  }
}

function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}
function formatDate(datetime) {
  if (!datetime) return '—'
  return new Date(datetime).toLocaleString('zh-TW', { dateStyle: 'short', timeStyle: 'short' })
}

// 審核狀態（RequestStatus enum）→ 顯示文字與樣式
const REQUEST_STATUS_MAP = {
  pending: { text: '待審核', className: 'status--pending' },
  approved: { text: '通過', className: 'status--success' },
  rejected: { text: '拒絕', className: 'status--failed' },
}
function requestStatusInfo(status) {
  return REQUEST_STATUS_MAP[status] || { text: status || '—', className: '' }
}

// 每筆正在送出審核中的 groupBuyId，送出中就 disable 按鈕避免重複點擊。
const busyIds = ref(new Set())
function isBusy(id) {
  return busyIds.value.has(id)
}
function setBusy(id, busy) {
  const next = new Set(busyIds.value)
  if (busy) next.add(id)
  else next.delete(id)
  busyIds.value = next
}

// 目前展開「拒絕原因」輸入框的那一筆 groupBuyId；同時間只會有一筆展開。
const rejectingId = ref(null)
const rejectReasonText = ref('')

function openRejectBox(gb) {
  rejectingId.value = gb.groupBuyId
  rejectReasonText.value = ''
}
function cancelReject() {
  rejectingId.value = null
  rejectReasonText.value = ''
}

async function approve(gb) {
  const ok = await confirm({
    title: '通過團購申請',
    message: `確定要通過「${gb.productName}」的團購申請嗎？通過後即會開團，無法再改。`,
    confirmText: '通過',
  })
  if (!ok) return

  setBusy(gb.groupBuyId, true)
  try {
    await farmerGroupBuyApi.review(gb.groupBuyId, { requestStatus: 'approved' })
    await loadList()
  } catch (e) {
    error.value = e.message || '審核失敗，請稍後再試。'
  } finally {
    setBusy(gb.groupBuyId, false)
  }
}

async function submitReject(gb) {
  const reason = rejectReasonText.value.trim()
  if (!reason) {
    error.value = '請填寫拒絕原因'
    return
  }
  const ok = await confirm({
    title: '拒絕團購申請',
    message: `確定要拒絕「${gb.productName}」的團購申請嗎？此動作無法復原。`,
    confirmText: '拒絕',
    danger: true,
  })
  if (!ok) return

  setBusy(gb.groupBuyId, true)
  try {
    await farmerGroupBuyApi.review(gb.groupBuyId, { requestStatus: 'rejected', rejectReason: reason })
    cancelReject()
    await loadList()
  } catch (e) {
    error.value = e.message || '審核失敗，請稍後再試。'
  } finally {
    setBusy(gb.groupBuyId, false)
  }
}

onMounted(loadList)
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>🛒 團購管理</h1>
    </header>

    <section class="card">
      <p v-if="loading" class="state">載入中…</p>

      <div v-else-if="error" class="state state--error">
        <p>😢 {{ error }}</p>
        <button type="button" @click="loadList">重新載入</button>
      </div>

      <p v-else-if="list.length === 0" class="state">目前沒有團購申請或進行中的團購。</p>

      <div v-else class="table-wrap">
        <table class="gb-table">
          <thead>
            <tr>
              <th>商品</th>
              <th>發起人</th>
              <th>團購價</th>
              <th>目標數量</th>
              <th>團購狀態</th>
              <th>審核狀態</th>
              <th>申請時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="gb in list" :key="gb.groupBuyId">
              <tr>
                <td class="col-name">{{ gb.productName }}</td>
                <td>{{ gb.hostUserName || '—' }}</td>
                <td>{{ formatPrice(gb.groupPrice) }}</td>
                <td>{{ gb.targetAmount ?? '—' }}</td>
                <td>
                  <span class="badge" :class="groupBuyStatusInfo(gb.status).className">
                    {{ groupBuyStatusInfo(gb.status).text }}
                  </span>
                </td>
                <td>
                  <span class="badge" :class="requestStatusInfo(gb.requestStatus).className">
                    {{ requestStatusInfo(gb.requestStatus).text }}
                  </span>
                </td>
                <td>{{ formatDate(gb.requestDatetime) }}</td>
                <td class="col-actions">
                  <template v-if="gb.requestStatus === 'pending'">
                    <button
                      type="button"
                      class="btn btn--approve"
                      :disabled="isBusy(gb.groupBuyId)"
                      @click="approve(gb)"
                    >
                      通過
                    </button>
                    <button
                      type="button"
                      class="btn btn--reject"
                      :disabled="isBusy(gb.groupBuyId)"
                      @click="rejectingId === gb.groupBuyId ? cancelReject() : openRejectBox(gb)"
                    >
                      拒絕
                    </button>
                  </template>
                  <span v-else-if="gb.requestStatus === 'rejected' && gb.rejectReason" class="reject-reason">
                    原因：{{ gb.rejectReason }}
                  </span>
                  <span v-else class="col-actions__none">—</span>
                </td>
              </tr>
              <tr v-if="rejectingId === gb.groupBuyId" class="reject-row">
                <td colspan="8">
                  <div class="reject-box">
                    <input
                      v-model="rejectReasonText"
                      type="text"
                      class="reject-box__input"
                      placeholder="請輸入拒絕原因"
                      maxlength="200"
                    />
                    <button
                      type="button"
                      class="btn btn--reject"
                      :disabled="isBusy(gb.groupBuyId)"
                      @click="submitReject(gb)"
                    >
                      確認拒絕
                    </button>
                    <button type="button" class="btn btn--ghost" @click="cancelReject">取消</button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>

<style scoped>
.farmer-page {
  padding: 32px 24px;
}
.page-head h1 {
  margin: 0 0 20px;
  font-size: 24px;
  color: var(--ink);
}
.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
  border-top: 3px solid var(--leaf);
}

.state {
  text-align: center;
  color: var(--muted);
  padding: 32px 0;
  margin: 0;
}
.state--error button {
  margin-top: 12px;
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}

/* ---------- 表格 ---------- */
.table-wrap {
  overflow-x: auto;
}
.gb-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  white-space: nowrap;
}
.gb-table th,
.gb-table td {
  padding: 12px 10px;
  text-align: left;
  border-bottom: 1px solid var(--line);
}
.gb-table th {
  color: var(--muted);
  font-weight: 600;
  font-size: 13px;
}
.col-name {
  color: var(--ink);
  font-weight: 600;
}
.col-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.col-actions__none {
  color: var(--muted);
}

/* ---------- 狀態標籤 ---------- */
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.status--open { background: #ecfdf3; color: #15803d; }
.status--success { background: #eff6ff; color: #1d4ed8; }
.status--failed { background: #f3f4f6; color: #6b7280; }
.status--cancelled { background: #f3f4f6; color: #6b7280; }
.status--pending { background: #fff7ed; color: #c2410c; }

.reject-reason {
  color: var(--muted);
  font-size: 13px;
  white-space: normal;
}

/* ---------- 操作按鈕 ---------- */
.btn {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease;
}
.btn:disabled {
  opacity: 0.6;
  cursor: default;
}
.btn--approve {
  background: var(--leaf);
  color: #fff;
}
.btn--approve:hover:not(:disabled) {
  background: var(--leaf-dark);
}
.btn--reject {
  background: #fff;
  border-color: #c0392b;
  color: #c0392b;
}
.btn--reject:hover:not(:disabled) {
  background: #fdf0ee;
}
.btn--ghost {
  background: transparent;
  border-color: var(--line);
  color: var(--ink);
}
.btn--ghost:hover {
  border-color: var(--muted);
}

/* ---------- 拒絕原因輸入列 ---------- */
.reject-row td {
  padding-top: 0;
  border-bottom: 1px solid var(--line);
}
.reject-box {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 10px 0 4px;
}
.reject-box__input {
  flex: 1;
  min-width: 200px;
  padding: 8px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 13px;
}
.reject-box__input:focus {
  outline: none;
  border-color: var(--leaf);
}
</style>
