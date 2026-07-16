<script setup>
// 小農後台：團購管理
// 分兩大區塊（上方切換）：
//   1) 團購審核：GET /api/farmer/groupBuy/list，依 requestStatus 分「待審核／開團中／已拒絕」三個頁籤
//      - 待審核可「通過／拒絕」（POST /api/farmer/groupBuy/farmerResponse/{id}）
//      - 每筆都有「詳細資料」按鈕 → GET /api/farmer/groupBuy/list/{groupBuyId}
//      - 開團中的多一個「進度」按鈕 → GET /api/farmer/groupBuy/progress/{groupBuyId}
//   2) 團購訂單：GET /api/farmer/groupBuy/orderList，依 shippedStatus 分「待出貨／已送達」
//      - 每筆都有配送狀態下拉選單，選完按「更新」送 POST /api/farmer/groupBuy/orderShipped/{orderId}
//        （後端目前不論送什麼值，實際都會直接改成 DELIVERED，這裡還是照選單的值送出，
//        等後端之後改成真的照 body 判斷時前端不用再改）
//      - 每筆都有「詳細資料」按鈕 → GET /api/farmer/groupBuy/order/{orderId}（含團購主聯絡方式）
//
// 注意事項：
//   - /list/{groupBuyId} 原本直接回傳 JPA entity（GroupBuyVO）造成循環參照序列化爆掉，已修好，
//     現在回傳的是跟 GroupBuyFarmerDTO 一樣的扁平欄位（沒有巢狀的 product/hostUser）。
//   - /orderList、/order/{orderId} 原本 gb_order 資料表少欄位（先是 shipping_address，後來換成
//     tracking_num）造成 SQL 400，都已修好。
//   - ShippedStatus / OrderStatus / PaidStatus 這三個 enum 後端是用「全大寫」序列化
//     （跟 GroupBuyStatus / RequestStatus 的全小寫不同），對照表要用大寫 key，
//     混用會讓畫面直接顯示英文代碼原文（例如曾經在畫面上看到兩個 "PENDING"）。
//   - 訂單表格的「建立時間」目前一定是空的：GroupBuyService.showOrderList()（給 /orderList 用）
//     組 DTO 時沒有呼叫 dto.setCreatedAt(...)，這個欄位後端根本沒送。點「詳細資料」看到的建立時間
//     是對的，因為那支走的是 showOneOrder()（GET /order/{orderId}），那支有設定這個欄位。
//     這是後端 DTO 少映射一個欄位，前端沒有其他資料來源可以補，需要後端補上這行才會有值。
import { ref, computed, onMounted } from 'vue'
import farmerGroupBuyApi from '@/api/farmerGroupBuy'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
import { shippedStatusInfo, orderStatusInfo, paidStatusInfo } from '@/utils/orderStatus'
import { confirm } from '@/composables/useConfirm'
import { usePagination } from '@/composables/usePagination'
import Pagination from '@/components/Pagination.vue'

// ========== 上方主分頁：審核 / 訂單 ==========
const MAIN_TABS = [
  { key: 'review', label: '團購審核' },
  { key: 'orders', label: '團購訂單' },
]
const mainTab = ref('review')

function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}
function formatDate(datetime) {
  if (!datetime) return '—'
  return new Date(datetime).toLocaleString('zh-TW', { dateStyle: 'short', timeStyle: 'short' })
}

// ========== 團購審核 ==========
const reviewList = ref([])
const reviewLoading = ref(true)
const reviewError = ref('')

const REVIEW_TABS = [
  { key: 'pending', label: '待審核' },
  { key: 'approved', label: '開團中' },
  { key: 'rejected', label: '已拒絕' },
]
const reviewSubTab = ref('pending')
// 「開團中」頁籤名副其實：通過審核後如果已經成團/未成團/被取消（status 不再是 open），
// 就不該再留在這裡——已成團的會改到「團購訂單」區塊看（orderList 有資料之後才看得到）。
const filteredReviewList = computed(() =>
  reviewList.value.filter((gb) =>
    reviewSubTab.value === 'approved'
      ? gb.requestStatus === 'approved' && gb.status === 'open'
      : gb.requestStatus === reviewSubTab.value
  )
)
const { page: reviewPage, totalPages: reviewTotalPages, pageItems: pagedReviewList } =
  usePagination(filteredReviewList, 10)

async function loadReviewList() {
  reviewLoading.value = true
  reviewError.value = ''
  try {
    reviewList.value = await farmerGroupBuyApi.list()
  } catch (e) {
    reviewError.value = e.message || '無法載入團購清單，請稍後再試。'
  } finally {
    reviewLoading.value = false
  }
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
    await loadReviewList()
  } catch (e) {
    reviewError.value = e.message || '審核失敗，請稍後再試。'
  } finally {
    setBusy(gb.groupBuyId, false)
  }
}

async function submitReject(gb) {
  const reason = rejectReasonText.value.trim()
  if (!reason) {
    reviewError.value = '請填寫拒絕原因'
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
    await loadReviewList()
  } catch (e) {
    reviewError.value = e.message || '審核失敗，請稍後再試。'
  } finally {
    setBusy(gb.groupBuyId, false)
  }
}

// ========== 團購訂單 ==========
const orderList = ref([])
const orderLoading = ref(true)
const orderError = ref('')

// ShippedStatus enum 是全大寫（PENDING / DELIVERED），頁籤 key 要對到大寫代碼。
const ORDER_TABS = [
  { key: 'PENDING', label: '待出貨' },
  { key: 'DELIVERED', label: '已送達' },
]
const orderSubTab = ref('PENDING')
const filteredOrderList = computed(() =>
  orderList.value.filter((o) => o.shippedStatus === orderSubTab.value)
)
const { page: orderPage, totalPages: orderTotalPages, pageItems: pagedOrderList } =
  usePagination(filteredOrderList, 10)

// 訂單本身沒有商品名稱，用審核清單（同樣是這位小農的資料）依 groupBuyId 對照出商品名稱顯示。
const productNameByGroupBuyId = computed(() =>
  Object.fromEntries(reviewList.value.map((gb) => [gb.groupBuyId, gb.productName]))
)

async function loadOrderList() {
  orderLoading.value = true
  orderError.value = ''
  try {
    orderList.value = await farmerGroupBuyApi.orderList()
  } catch (e) {
    orderError.value = e.message || '無法載入訂單清單，請稍後再試。'
  } finally {
    orderLoading.value = false
  }
}

// 團購總收益：所有已撥款訂單的總金額加總，跟訂單清單分開獨立打一支 API。
const totalRevenue = ref(null)
const totalRevenueLoading = ref(true)
async function loadTotalRevenue() {
  totalRevenueLoading.value = true
  try {
    totalRevenue.value = await farmerGroupBuyApi.totalRevenue()
  } catch {
    totalRevenue.value = null
  } finally {
    totalRevenueLoading.value = false
  }
}

const shippingBusyIds = ref(new Set())
function isShippingBusy(id) {
  return shippingBusyIds.value.has(id)
}

// 下拉選單目前選的配送狀態，用 orderId 當 key；沒選過就先帶入該筆目前的狀態。
const shippedSelection = ref({})
function selectedShipped(order) {
  return shippedSelection.value[order.orderId] ?? order.shippedStatus
}
function setSelectedShipped(order, value) {
  shippedSelection.value = { ...shippedSelection.value, [order.orderId]: value }
}

// 送出下拉選單選的配送狀態。
// 注意：後端 ship() 目前不論 body 傳什麼，一律直接改成 DELIVERED（沒有真的照選的值判斷），
// 這裡還是照選單的值送出，等後端之後改成真的照 body 判斷時，前端不用再改。
async function updateShippedStatus(order) {
  const next = selectedShipped(order)
  if (next === order.shippedStatus) return

  const ok = await confirm({
    title: '更新配送狀態',
    message: `確定要把訂單 #${order.orderId} 的配送狀態改成「${shippedStatusInfo(next).text}」嗎？`,
    confirmText: '確認更新',
  })
  if (!ok) {
    setSelectedShipped(order, order.shippedStatus)
    return
  }

  const busy = new Set(shippingBusyIds.value)
  busy.add(order.orderId)
  shippingBusyIds.value = busy
  try {
    await farmerGroupBuyApi.shipOrder(order.orderId, next)
    await loadOrderList()
  } catch (e) {
    orderError.value = e.message || '更新出貨狀態失敗，請稍後再試。'
  } finally {
    const done = new Set(shippingBusyIds.value)
    done.delete(order.orderId)
    shippingBusyIds.value = done
  }
}

// ========== 詳細資料 / 進度彈窗（三種內容共用同一個彈窗殼） ==========
const modal = ref({ open: false, type: '', title: '', loading: false, error: '', data: null })

async function openModal(type, title, loader) {
  modal.value = { open: true, type, title, loading: true, error: '', data: null }
  try {
    modal.value.data = await loader()
  } catch (e) {
    modal.value.error = e.message || '無法載入資料，請稍後再試。'
  } finally {
    modal.value.loading = false
  }
}
function closeModal() {
  modal.value.open = false
}

function openReviewDetail(gb) {
  openModal('reviewDetail', `團購申請詳細資料｜${gb.productName}`, () => farmerGroupBuyApi.getOne(gb.groupBuyId))
}
function openProgress(gb) {
  openModal('progress', `團購進度｜${gb.productName}`, () => farmerGroupBuyApi.progress(gb.groupBuyId))
}
function openOrderDetail(order) {
  openModal('orderDetail', `訂單詳細資料｜#${order.orderId}`, () => farmerGroupBuyApi.getOrder(order.orderId))
}

// 流程說明：純靜態內容，不用打 API，直接開窗。
function openGuide() {
  modal.value = { open: true, type: 'guide', title: '團購流程與注意事項', loading: false, error: '', data: true }
}

onMounted(() => {
  loadReviewList()
  loadOrderList()
  loadTotalRevenue()
})
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <div class="page-head__title">
        <h1>🛒 團購管理</h1>
        <button type="button" class="guide-btn" @click="openGuide">📖 流程說明</button>
      </div>
      <nav class="main-tabs">
        <button
          v-for="tab in MAIN_TABS"
          :key="tab.key"
          type="button"
          class="main-tab-btn"
          :class="{ 'main-tab-btn--active': mainTab === tab.key }"
          @click="mainTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>
    </header>

    <!-- ========== 團購審核 ========== -->
    <section v-if="mainTab === 'review'" class="card">
      <nav class="sub-tabs">
        <button
          v-for="tab in REVIEW_TABS"
          :key="tab.key"
          type="button"
          class="sub-tab-btn"
          :class="{ 'sub-tab-btn--active': reviewSubTab === tab.key }"
          @click="reviewSubTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>

      <p v-if="reviewLoading" class="state">載入中…</p>
      <div v-else-if="reviewError" class="state state--error">
        <p>😢 {{ reviewError }}</p>
        <button type="button" @click="loadReviewList">重新載入</button>
      </div>
      <p v-else-if="filteredReviewList.length === 0" class="state">這個分類目前沒有資料。</p>

      <div v-else class="table-wrap">
        <table class="gb-table">
          <thead>
            <tr>
              <th>商品</th>
              <th>發起人</th>
              <th>團購價</th>
              <th>達標金額</th>
              <th>團購狀態</th>
              <th>申請時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="gb in pagedReviewList" :key="gb.groupBuyId">
              <tr>
                <td class="col-name">{{ gb.productName }}</td>
                <td>{{ gb.hostUserName || '—' }}</td>
                <td>{{ formatPrice(gb.groupPrice) }}</td>
                <td>{{ formatPrice(gb.targetAmount) }}</td>
                <td>
                  <span class="badge" :class="groupBuyStatusInfo(gb.status).className">
                    {{ groupBuyStatusInfo(gb.status).text }}
                  </span>
                </td>
                <td>{{ formatDate(gb.requestDatetime) }}</td>
                <td class="col-actions">
                  <button type="button" class="btn btn--ghost" @click="openReviewDetail(gb)">詳細資料</button>
                  <button
                    v-if="gb.status === 'open'"
                    type="button"
                    class="btn btn--ghost"
                    @click="openProgress(gb)"
                  >
                    進度
                  </button>
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
                </td>
              </tr>
              <tr v-if="rejectingId === gb.groupBuyId" class="reject-row">
                <td colspan="7">
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
        <Pagination v-model:page="reviewPage" :total-pages="reviewTotalPages" />
      </div>
    </section>

    <!-- ========== 團購訂單 ========== -->
    <section v-else class="card">
      <div class="revenue-box">
        <span class="revenue-label">團購總收益</span>
        <span class="revenue-value">
          {{ totalRevenueLoading ? '載入中…' : formatPrice(totalRevenue) }}
        </span>
      </div>

      <nav class="sub-tabs">
        <button
          v-for="tab in ORDER_TABS"
          :key="tab.key"
          type="button"
          class="sub-tab-btn"
          :class="{ 'sub-tab-btn--active': orderSubTab === tab.key }"
          @click="orderSubTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>

      <p v-if="orderLoading" class="state">載入中…</p>
      <div v-else-if="orderError" class="state state--error">
        <p>😢 {{ orderError }}</p>
        <button type="button" @click="loadOrderList">重新載入</button>
      </div>
      <p v-else-if="filteredOrderList.length === 0" class="state">這個分類目前沒有訂單。</p>

      <div v-else class="table-wrap">
        <p class="ship-hint">📦 配達完成記得更改配送狀態為已送達</p>
        <table class="gb-table">
          <thead>
            <tr>
              <th>訂單編號</th>
              <th>商品</th>
              <th>數量</th>
              <th>總金額</th>
              <th>建立時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in pagedOrderList" :key="o.orderId">
              <td class="col-name">#{{ o.orderId }}</td>
              <td>{{ productNameByGroupBuyId[o.groupBuyId] || `團購 #${o.groupBuyId}` }}</td>
              <td>{{ o.totalQuantity ?? '—' }}</td>
              <td>{{ formatPrice(o.totalAmount) }}</td>
              <td>{{ formatDate(o.createdAt) }}</td>
              <td class="col-actions">
                <select
                  class="ship-select"
                  :value="selectedShipped(o)"
                  :disabled="isShippingBusy(o.orderId)"
                  @change="setSelectedShipped(o, $event.target.value)"
                >
                  <option value="PENDING">待出貨</option>
                  <option value="DELIVERED">已送達</option>
                </select>
                <button
                  type="button"
                  class="btn btn--approve"
                  :disabled="isShippingBusy(o.orderId) || selectedShipped(o) === o.shippedStatus"
                  @click="updateShippedStatus(o)"
                >
                  更新
                </button>
                <button type="button" class="btn btn--ghost" @click="openOrderDetail(o)">詳細資料</button>
              </td>
            </tr>
          </tbody>
        </table>
        <Pagination v-model:page="orderPage" :total-pages="orderTotalPages" />
      </div>
    </section>

    <!-- ========== 詳細資料 / 進度 / 流程說明彈窗 ========== -->
    <Teleport to="body">
      <div v-if="modal.open" class="modal-overlay" @click.self="closeModal">
        <div class="modal-card" :class="{ 'modal-card--wide': modal.type === 'guide' }">
          <header class="modal-head">
            <h3>{{ modal.title }}</h3>
            <button type="button" class="modal-close" @click="closeModal">✕</button>
          </header>
          <div class="modal-body">
            <p v-if="modal.loading" class="state">載入中…</p>
            <p v-else-if="modal.error" class="state state--error">😢 {{ modal.error }}</p>

            <!-- 團購流程說明（靜態內容） -->
            <div v-else-if="modal.type === 'guide'" class="guide-content">
              <p>為確保團購訂單能順利完成，請小農在審核團購申請前，先詳閱以下流程與注意事項：</p>
              <ol>
                <li>
                  <strong>確認配送地址</strong>
                  <p>團購成功後，商品將由小農自行配送至團購主所設定的收貨地址。因此，在審核團購申請時，請先確認該地址是否位於可配送範圍內。</p>
                </li>
                <li>
                  <strong>審核通過並開放會員參加</strong>
                  <p>小農按下「審核通過」後，此筆團購將正式開放。所有會員皆可在團購截止日期前加入團購並完成付款。</p>
                </li>
                <li>
                  <strong>截止後由系統自動結算</strong>
                  <p>團購進行期間不會提前結算，系統將於截止日期到期後，依照所有參加會員的付款總金額進行結算。</p>
                  <p>若付款總金額達到團購主原先設定的目標金額，此筆團購即為成團成功，系統將自動建立團購訂單；若未達目標金額，則視為成團失敗。</p>
                </li>
                <li>
                  <strong>聯繫團購主並安排配送</strong>
                  <p>成團成功後，請小農主動聯繫團購主，確認實際配送日期及時間。團購主的姓名、電子郵件等聯絡資訊，可於訂單詳細資料中查看。</p>
                </li>
                <li>
                  <strong>完成配送及點收</strong>
                  <p>配送商品時，請與團購主當面確認商品內容及數量。確認點收完畢後，請小農於系統中將訂單狀態更新為「已配達」，並提醒團購主至會員中心按下「確認收貨」。</p>
                </li>
                <li>
                  <strong>款項撥付</strong>
                  <p>團購主完成確認收貨後，系統將於三日內自動撥付款項給小農。請小農留意款項狀態，並確認款項是否正確入帳。</p>
                </li>
              </ol>
              <p class="guide-footer">請務必於審核前確認配送範圍，並於成團後主動與團購主聯繫，以確保訂單配送及款項撥付流程順利完成。</p>
            </div>

            <!-- 團購申請詳細資料（/list/{groupBuyId}，跟 GroupBuyFarmerDTO 同形狀：扁平欄位，不是巢狀 entity） -->
            <dl v-else-if="modal.type === 'reviewDetail' && modal.data" class="modal-meta">
              <div class="modal-meta-row"><dt>團購編號</dt><dd>{{ modal.data.groupBuyId }}</dd></div>
              <div class="modal-meta-row"><dt>商品名稱</dt><dd>{{ modal.data.productName ?? '—' }}</dd></div>
              <div class="modal-meta-row"><dt>團購價</dt><dd>{{ formatPrice(modal.data.groupPrice) }}</dd></div>
              <div class="modal-meta-row"><dt>達標金額</dt><dd>{{ formatPrice(modal.data.targetAmount) }}</dd></div>
              <div class="modal-meta-row"><dt>發起人</dt><dd>{{ modal.data.hostUserName ?? '—' }}</dd></div>
              <div class="modal-meta-row"><dt>團購狀態</dt><dd>{{ groupBuyStatusInfo(modal.data.status).text }}</dd></div>
              <div class="modal-meta-row"><dt>審核狀態</dt><dd>{{ requestStatusInfo(modal.data.requestStatus).text }}</dd></div>
              <div class="modal-meta-row"><dt>開團時間</dt><dd>{{ formatDate(modal.data.openDatetime) }}</dd></div>
              <div class="modal-meta-row"><dt>截止時間</dt><dd>{{ formatDate(modal.data.ddlDatetime) }}</dd></div>
              <div class="modal-meta-row"><dt>取貨地點</dt><dd>{{ modal.data.pickupAddress || '—' }}</dd></div>
              <div class="modal-meta-row"><dt>申請時間</dt><dd>{{ formatDate(modal.data.requestDatetime) }}</dd></div>
              <div class="modal-meta-row"><dt>審核回覆時間</dt><dd>{{ formatDate(modal.data.replyDatetime) }}</dd></div>
              <div v-if="modal.data.rejectReason" class="modal-meta-row">
                <dt>拒絕原因</dt><dd>{{ modal.data.rejectReason }}</dd>
              </div>
            </dl>

            <!-- 團購進度（GroupBuyParticipationDTO） -->
            <dl v-else-if="modal.type === 'progress' && modal.data" class="modal-meta">
              <div class="modal-meta-row"><dt>團購編號</dt><dd>{{ modal.data.groupBuyId }}</dd></div>
              <div class="modal-meta-row"><dt>發起人</dt><dd>{{ modal.data.hostName ?? '—' }}</dd></div>
              <div class="modal-meta-row"><dt>取貨地點</dt><dd>{{ modal.data.pickupAddress || '—' }}</dd></div>
              <div class="modal-meta-row"><dt>目前總數量</dt><dd>{{ modal.data.totalQuantity ?? 0 }}</dd></div>
              <div class="modal-meta-row"><dt>目前總金額</dt><dd>{{ formatPrice(modal.data.totalAmount) }}</dd></div>
            </dl>

            <!-- 訂單詳細資料（GroupBuyOrderDTO） -->
            <template v-else-if="modal.type === 'orderDetail' && modal.data">
              <dl class="modal-meta">
                <div class="modal-meta-row"><dt>訂單編號</dt><dd>#{{ modal.data.orderId }}</dd></div>
                <div class="modal-meta-row"><dt>數量</dt><dd>{{ modal.data.totalQuantity ?? '—' }}</dd></div>
                <div class="modal-meta-row"><dt>團購價</dt><dd>{{ formatPrice(modal.data.groupPrice) }}</dd></div>
                <div class="modal-meta-row"><dt>總金額</dt><dd>{{ formatPrice(modal.data.totalAmount) }}</dd></div>
                <div class="modal-meta-row"><dt>取貨地點</dt><dd>{{ modal.data.shippingAddress || '—' }}</dd></div>
                <div class="modal-meta-row"><dt>出貨狀態</dt><dd>{{ shippedStatusInfo(modal.data.shippedStatus).text }}</dd></div>
                <div class="modal-meta-row"><dt>出貨時間</dt><dd>{{ formatDate(modal.data.shippedAt) }}</dd></div>
                <div class="modal-meta-row"><dt>訂單狀態</dt><dd>{{ orderStatusInfo(modal.data.orderStatus).text }}</dd></div>
                <div class="modal-meta-row"><dt>撥款狀態</dt><dd>{{ paidStatusInfo(modal.data.paidStatus).text }}</dd></div>
                <div class="modal-meta-row"><dt>建立時間</dt><dd>{{ formatDate(modal.data.createdAt) }}</dd></div>
                <div class="modal-meta-row"><dt>收貨時間</dt><dd>{{ formatDate(modal.data.receivedAt) }}</dd></div>
                <div class="modal-meta-row"><dt>完成時間</dt><dd>{{ formatDate(modal.data.completedAt) }}</dd></div>
              </dl>
              <div class="host-contact">
                <h4>團購主聯絡方式</h4>
                <p>{{ modal.data.hostUserName || '—' }}</p>
                <p>📞 {{ modal.data.hostUserPhone || '—' }}</p>
                <p>✉️ {{ modal.data.hostUserEmail || '—' }}</p>
                <p class="host-contact__note">配達確切時間請致電團購主</p>
              </div>
            </template>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<style scoped>
.farmer-page {
  padding: 32px 24px;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 20px;
}
.page-head h1 {
  margin: 0;
  font-size: 24px;
  color: var(--ink);
}
.page-head__title {
  display: flex;
  align-items: center;
  gap: 12px;
}
.guide-btn {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--leaf);
  background: #fff;
  color: var(--leaf-dark);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}
.guide-btn:hover {
  background: var(--leaf);
  color: #fff;
}

/* ---------- 團購總收益 ---------- */
.revenue-box {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 14px 18px;
  margin-bottom: 18px;
  background: var(--leaf-soft);
  border-radius: 12px;
}
.revenue-label {
  font-size: 14px;
  color: var(--ink-soft);
  font-weight: 600;
}
.revenue-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--leaf-dark);
}

/* ---------- 主分頁（審核／訂單）---------- */
.main-tabs {
  display: flex;
  gap: 8px;
}
.main-tab-btn {
  padding: 8px 20px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-soft);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.main-tab-btn:hover {
  border-color: var(--leaf);
}
.main-tab-btn--active {
  background: var(--leaf);
  border-color: var(--leaf);
  color: #fff;
}

.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
  border-top: 3px solid var(--leaf);
}

/* ---------- 子分頁 ---------- */
.sub-tabs {
  display: flex;
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
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.sub-tab-btn:hover {
  border-color: var(--leaf);
}
.sub-tab-btn--active {
  background: var(--leaf-soft);
  border-color: var(--leaf);
  color: var(--leaf-dark);
  font-weight: 600;
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
.ship-hint {
  margin: 0 0 14px;
  padding: 8px 14px;
  background: #fff7ed;
  color: #c2410c;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
}
.ship-select {
  padding: 5px 8px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  color: var(--ink);
  background: #fff;
}
.ship-select:focus {
  outline: none;
  border-color: var(--leaf);
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

/* ---------- 詳細資料彈窗 ---------- */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(30, 30, 25, 0.45);
}
.modal-card {
  width: 100%;
  max-width: 420px;
  max-height: 80vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 48px rgba(30, 25, 15, 0.28);
}
.modal-card--wide {
  max-width: 560px;
}
.modal-head {
  position: sticky;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  background: #fff;
  border-bottom: 1px solid var(--line);
}
.modal-head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--ink);
}
.modal-close {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
}
.modal-close:hover {
  color: var(--ink);
}
.modal-body {
  padding: 18px 20px;
}
.modal-meta {
  margin: 0;
}
.modal-meta-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px dashed var(--line);
  font-size: 13px;
}
.modal-meta-row dt {
  color: var(--muted);
  flex-shrink: 0;
}
.modal-meta-row dd {
  margin: 0;
  color: var(--ink);
  font-weight: 500;
  text-align: right;
}
.host-contact {
  margin-top: 16px;
  padding: 14px 16px;
  background: var(--leaf-soft);
  border-radius: 12px;
}
.host-contact h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--leaf-dark);
}
.host-contact p {
  margin: 4px 0;
  font-size: 13px;
  color: var(--ink-soft);
}
.host-contact__note {
  margin-top: 8px;
  font-weight: 600;
  color: #c2410c;
}

/* ---------- 流程說明 ---------- */
.guide-content {
  font-size: 14px;
  color: var(--ink-soft);
  line-height: 1.7;
}
.guide-content > p:first-child {
  margin-top: 0;
}
.guide-content ol {
  margin: 12px 0;
  padding-left: 22px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.guide-content li {
  padding-left: 4px;
}
.guide-content li strong {
  display: block;
  color: var(--ink);
  font-size: 15px;
  margin-bottom: 4px;
}
.guide-content li p {
  margin: 2px 0;
}
.guide-footer {
  margin: 16px 0 0;
  padding: 12px 14px;
  background: #fff7ed;
  color: #c2410c;
  border-radius: 10px;
  font-weight: 600;
  font-size: 13px;
}
</style>
