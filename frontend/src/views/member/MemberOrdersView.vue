<script setup>
import { ref, reactive, onMounted } from 'vue'
import memberOrdersApi from '@/api/memberOrders'
import { confirm } from '@/composables/useConfirm'
import noImage from '@/assets/no-image.svg'

const orders = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)

const openOrderId = ref(null) // 目前展開的是哪一張訂單 (null=全部收合)
const groups = reactive({}) // 快取已載過的訂單明細 (物件，已按小農分好組)
const itemsLoading = ref(false)
const itemsError = ref('')

const receivingKey = ref(null)  // 正在處理確認收貨的 orderId-farmerId

// 狀態代碼轉換
const SHIPPED_STATUS = {
  pending: { label: '待出貨', tone: 'amber' },
  shipping: { label: '配送中', tone: 'blue' },
  delivered: { label: '已送達', tone: 'green' },
}

// 預防沒有對應的狀態標籤 (預設灰色)
const badgeOf = (map, code) => map[code] || {label: code ?? '-', tone: 'gray'}

// 時間格式轉換
function fmdt(dt){
  if (!dt) return '-'
  const d = new Date(dt)
  if (Number.isNaN(d.getTime())) return String(dt)
  return d.toLocaleString("zh-TW", {hour12: false})
}

// 金額轉換
const fmm = (n) => (n == null ? '-' : `NT$ ${Number(n).toLocaleString('zh-TW')}`)

// 圖片載入失敗時用預設圖
function onImagError(e){
  if (e.target.dataset.fallback) { return }
  e.target.dataset.fallback = "1"
  e.target.src = noImage
}

async function loadOrders(){
  loading.value = true
  loadError.value = ''
  openOrderId.value = null  // 換頁時把展開的明細收起來

  try {
    const res = await memberOrdersApi.list(page.value)
    orders.value = res.content || []
    totalPages.value = res.totalPages ?? 0
  } catch(e){
    loadError.value = e.message || '載入訂單失敗'
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)

// 展開 / 收合訂單明細
async function toggleOrderItems(orderId){

  // 如果訂單明細已經開啟，收合
  if (openOrderId.value === orderId){
    openOrderId.value = null
    return
  }

  // 展開這張訂單的明細
  openOrderId.value = orderId
  itemsError.value = ''

  // 若明細已經在快取
  if (groups[orderId]) return

  // 沒快取，api 取得明細
  itemsLoading.value = true
  try {
    const res = await memberOrdersApi.listItems(orderId)
    groups[orderId] = res || []
  } catch (e){
    itemsError.value = e.message || '載入明細失敗'
    openOrderId.value = null // 失敗就收合列表
  } finally {
    itemsLoading.value = false
  }
}

function goPage(p){
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  loadOrders()
}


async function markReceived(orderId, group){
  const ok = await confirm({
    title: '確認收貨',
    message: `確定已經收到 ${group.farmerName || '此小農'} 的全部商品嗎?`,
    confirmText: '確認收貨',
    danger: true,
  })
  if (!ok) return
  if (group.shippedStatus !== 'shipping') { return }

  receivingKey.value = `${orderId}-${group.farmerId}`   // 鎖定收貨按鈕

  try {
    await memberOrdersApi.received(orderId, group.farmerId)
    delete groups[orderId]  // 清除這張訂單的明細快取
    openOrderId.value = null    // 先收合
    await toggleOrderItems(orderId)  // 再展開重 load 新的送達狀態
  } catch(e){
    alert(e.message || '操作失敗')
  } finally {
    receivingId.value = null
  }
}

</script>

<template>
  <section class="order-page">
    <header class="order-page-head">
      <h1>🧾 我的訂單</h1>
      <p class="hint-sub">每筆訂單來自多個農場商品，展開明細後可依農場分別確認收貨</p>
    </header>

    <p v-if="loading" class="state-box">載入中...</p>

    <div v-else-if="loadError" class="state-box">
      <span class="state-icon">😵</span>
      <p>{{ loadError }}</p>
      <button class="btn-ghost" @click="loadOrders">重新載入</button>
    </div>

    <div v-else-if="orders.length === 0" class="state-box">
      <span class="state-icon">🧾</span>
      <p>您還沒有任何訂單</p>
      <router-link class="btn-ghost" to="/products">快來逛逛商品 →</router-link>
    </div>

    <div v-else class="order-list">
      <article v-for="o in orders" :key="o.orderId" class="order-card">
        <header class="order-head">
          <div>
            <span class="order-id">訂單編號 #{{ o.orderId }}</span>
            <time class="order-date" :datetime="o.createdAt">{{ fmdt(o.createdAt) }}</time>
          </div>
        </header>

        <dl class="order-grid">
          <div class="order-cell">
            <dt>商品小計</dt>
            <dd>{{ fmm(o.totalAmount) }}</dd>
          </div>
          <div class="order-cell">
            <dt>折扣</dt>
            <dd :class="{ 'is-discount': o.discountAmount > 0 }">
              {{ o.discountAmount > 0 ? fmm(o.discountAmount) : '-' }}
            </dd>
          </div>
          <div class="order-cell">
            <dt>實付金額</dt>
            <dd class="order-money">{{ fmm(o.finalPayment) }}</dd>
          </div>
        </dl>

        <footer class="order-foot">
          <button class="btn-ghost" @click="toggleOrderItems(o.orderId)">
            {{ openOrderId === o.orderId ? '收合明細 ▲' : '查看明細 ▼' }}
          </button>
        </footer>

        <div v-if="openOrderId === o.orderId" class="item-panel">
          <p v-if="itemsLoading" class="hint">明細載入中...</p>
          <p v-else-if="itemsError" class="msg-err">{{ itemsError }}</p>

          <div v-else v-for="g in groups[o.orderId]" :key="g.farmerId" class="farmer-group">
            <header class="farmer-head">
              <span class="farmer-name">🧑‍🌾 {{ g.farmerName || `小農 #${g.farmerId}` }}</span>
              <span class="badge" :class="`badge--${badgeOf(SHIPPED_STATUS, g.shippedStatus).tone}`">
                {{ badgeOf(SHIPPED_STATUS, g.shippedStatus).label }}
              </span>
            </header>

            <table class="item-table">
                <colgroup>
                  <col style="width: 50%" />
                  <col style="width: 18%" />
                  <col style="width: 12%" />
                  <col style="width: 20%" />
                </colgroup>
              <thead>
                <tr>
                  <th>商品</th>
                  <th>單價</th>
                  <th>數量</th>
                  <th>小計</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in g.items" :key="item.productId">
                  <td class="item-name">
                    <img :src="`/api/products/${item.productId}/image`" @error="onImagError" alt="" class="item-img" />
                    <span>{{ item.productName }}</span>
                  </td>
                  <td>{{ fmm(item.price) }}</td>
                  <td>{{ fmm(item.quantity) }}</td>
                  <td>{{ fmm(item.itemSubtotal) }}</td>
                </tr>
              </tbody>
            </table>

            <footer class="farmer-foot">
              <span class="farmer-subtotal">農場小計 {{ fmm(g.subtotal) }}</span>
              <button v-if="g.shippedStatus === 'shipping'" 
                class="btn-primary" :disabled="receivingKey === `${o.orderId}-${g.farmerId}`"
                @click="markReceived(o.orderId, g)"
              >
                {{ receivingKey === `${o.orderId}-${g.farmerId}` ? '處理中...' : '確認收貨' }}
              </button>
            </footer>
          </div>
        </div>
      </article>
    </div>

    <div v-if="totalPages > 1" class="pager">
      <button class="btn-ghost" :disabled="page === 0" @click="goPage(page - 1)">上一頁</button>
      <span class="pager-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
      <button class="btn-ghost" :disabled="page + 1 >= totalPages" @click="goPage(page + 1)">下一頁</button>
    </div>
  </section>
</template>

<style scoped>

.order-page { display: flex; flex-direction: column; gap: 16px; }
.order-page-head h1 { margin: 0; font-size: 24px; color: var(--ink); }
.hint-sub { margin: 6px 0 0; font-size: 13px; color: var(--muted); }

.order-list { display: flex; flex-direction: column; gap: 14px; }

.order-card {
  background: #fff;
  border: 1px solid var(--line);
  border-left: 4px solid var(--leaf);
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}

.order-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}
.order-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.order-id   { display: block; font-weight: 600; color: var(--ink); }
.order-date { font-size: 12px; color: var(--muted); }
.order-badges { display: flex; gap: 6px; }

/* 徽章：tone */
.badge { padding: 3px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge--green { background: #e3f4e8; color: #2f6e46; }
.badge--blue  { background: #e5eefa; color: #2c5d9e; }
.badge--amber { background: #fdf1dc; color: #9a6b15; }
.badge--gray  { background: #eeeeea; color: #75806f; }

/* 金額格子 */
.order-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px 16px; margin: 0; }
.order-cell dt { font-size: 12px; color: var(--muted); margin-bottom: 2px; }
.order-cell dd { margin: 0; font-size: 14px; color: var(--ink-soft); }
.order-money   { font-weight: 700; color: var(--leaf-dark); font-size: 16px; }
.is-discount   { color: #b5651d; }

.order-foot { display: flex; gap: 8px; justify-content: flex-end; }

/* 明細展開區 */
.item-panel  { border-top: 1px dashed var(--line); padding-top: 12px; display: flex; flex-direction: column; gap: 16px; }

/* 小農分組樣式 */
.farmer-group { border: 1px solid var(--line); border-radius: 12px; padding: 12px 14px; background: var(--paper); }
.farmer-head  { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 8px; }
.farmer-name  { font-weight: 600; font-size: 14px; color: var(--leaf-dark); }
.farmer-foot  { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-top: 10px; }
.farmer-subtotal { margin-left: auto; font-size: 12px; color: var(--muted); }

.item-table  { width: 100%; border-collapse: collapse; font-size: 14px; table-layout: fixed; }
.item-table th { text-align: left; font-size: 12px; color: var(--muted); font-weight: 500; padding-bottom: 6px; }
.item-table td { padding: 8px 0; border-top: 1px solid var(--line); color: var(--ink-soft); }
.item-name   { display: flex; align-items: center; gap: 10px; }
.item-img    { width: 40px; height: 40px; object-fit: cover; border-radius: 8px; background: var(--leaf-soft); }
.item-table th:not(:first-child),
.item-table td:not(:first-child) {
  text-align: right;
}

/* 按鈕 / 狀態 */
.btn-ghost { padding: 7px 16px; border: 1px solid var(--line); border-radius: 10px; background: #fff;
             color: var(--ink-soft); cursor: pointer; font-size: 14px; text-decoration: none; }
.btn-ghost:hover:not(:disabled) { border-color: var(--leaf); color: var(--leaf); }
.btn-primary { padding: 7px 18px; border: 1px solid var(--leaf); border-radius: 10px;
               background: var(--leaf); color: #fff; cursor: pointer; font-size: 14px; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }

.state-box { display: flex; flex-direction: column; align-items: center; gap: 10px;
             padding: 56px 24px; background: #fff; border: 1px solid var(--line);
             border-radius: 16px; box-shadow: var(--shadow); color: var(--muted); text-align: center; }
.state-icon { font-size: 40px; }
.hint    { color: var(--muted); font-size: 13px; text-align: center; padding: 12px 0; }
.msg-err { color: #c0392b; font-size: 14px; text-align: center; padding: 12px 0; }

.pager { display: flex; align-items: center; justify-content: center; gap: 14px; margin-top: 4px; }
.pager-info { color: var(--ink-soft); font-size: 14px; }

</style>

