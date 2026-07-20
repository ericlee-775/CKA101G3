<script setup>
import { ref, onMounted } from 'vue';
import farmerOrdersApi from '@/api/farmerOrders';
import { confirm } from '@/composables/useConfirm';
import noImage from '@/assets/no-image.svg'


const orders = ref([])
const loading = ref(true)
const loadError = ref('')
const page = ref(0)
const totalPages = ref(0)
const shippingId = ref(null)


const SHIPPED_STATUS = {
  pending: { label: '待出貨', tone: 'amber' },
  shipping: { label: '配送中', tone: 'blue' },
  delivered: { label: '已送達', tone: 'green' },
}

const PAYOUT_STATUS = {
  pending: { label: '款項處理中', tone: 'muted'},
  paid: { label: '款項已撥款', tone: 'leaf' },
}

const badgeOf = (code) => SHIPPED_STATUS[code] || { label: code ?? '-', tone: 'gray' }
const payoutOf = (code) => PAYOUT_STATUS[code] || { label: code ?? '-', tone: 'muted' }

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

onMounted(loadOrders)

async function loadOrders(){
  loading.value = true
  loadError.value = ''
  try {
    const res = await farmerOrdersApi.list(page.value)
    orders.value = res.content || []
    totalPages.value = res.totalPages ?? 0
  } catch (e){
    loadError.value = e.message || '載入訂單失敗'
  } finally {
    loading.value = false
  }
}

async function changePage(p){
  if (p < 0 || p >= totalPages.value) { return }
  
  window.scrollTo({ top: 0})
  page.value = p
  await loadOrders()

}

async function markShipped(order){
  if (order.shippedStatus !== 'pending') { return }

  const ok = await confirm({
    title: '確認出貨',
    message: `確認已完成訂單 #${order.orderId} 出貨?`,
    confirmText: '確認出貨',
    danger: true
  })
  if (!ok) { return }

  shippingId.value = order.orderId
  
  try {
    await farmerOrdersApi.ship(order.orderId)
    await loadOrders()
  } catch (e){
    alert(e.message || '操作失敗')
  } finally {
    shippingId.value = null
  }
}

</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>🧾 訂單管理</h1>
    </header>

    <Transition name="tab-fade" mode="out-in">
      <div :key="page">
        <p v-if="loading" class="state-box">載入中...</p>
        <div v-else-if="loadError" class="state-box">
          <span class="state-icon">😵</span>
          <p>{{ loadError }}</p>
          <button class="btn-ghost" @click="loadOrders">重新載入</button>
        </div>
        <div v-else-if="orders.length === 0" class="state-box">
          <span class="state-icon">🧾</span>
          <p>您還沒有任何訂單</p>
        </div>
        <div v-else class="order-list">
          <article v-for="o in orders" :key="o.orderId" class="order-card">
            <header class="order-head">
              <div>
                <span class="order-id">訂單編號 #{{ o.orderId }}</span>
                <time class="order-date" :datetime="o.createdAt">{{ fmdt(o.createdAt) }}</time>
              </div>
              <div class="order-status">
                <span class="badge" :class="`badge--${badgeOf(o.shippedStatus).tone}`">
                  {{ badgeOf(o.shippedStatus).label }}
                </span>
                <time v-if="o.shippedAt" class="order-date order-shipped-at" :datetime="o.shippedAt">
                  {{ fmdt(o.shippedAt) }}
                </time> 
              </div>
            </header>
    
            <p class="order-address">📍 {{ o.shippingAddress }}</p>
    
            <table class="item-table">
              <colgroup>
                <col style="width: 50%" />
                <col style="width: 20%" />
                <col style="width: 12%" />
                <col style="width: 18%" />
              </colgroup>
              <thead>
                <tr><th>商品</th><th>單價</th><th>數量</th><th>小計</th></tr>
              </thead>
              <tbody>
                <tr v-for="it in o.items" :key="it.productId">
                  <td class="item-name">
                    <img :src="`/api/products/${it.productId}/image`" @error="onImagError" alt="" class="item-img" />
                    <span>{{ it.productName }}</span>
                  </td>
                  <td>{{ fmm(it.price) }}</td>
                  <td>{{ it.quantity }}</td>
                  <td>{{ fmm(it.price * it.quantity) }}</td>
                </tr>
              </tbody>
            </table>
    
            <footer class="order-foot">
              <span class="order-subtotal">訂單小計 {{ fmm(o.subtotal) }}</span>
    
              <div class="order-foot-right">
                <button v-if="o.shippedStatus === 'pending'" class="btn-primary" :disabled="shippingId === o.orderId" @click="markShipped(o)">
                  {{ shippingId === o.orderId ? '處理中...' : '確認出貨' }}
                </button>
                <span v-if="o.shippedStatus !== 'pending'" class="payout" :class="`payout--${payoutOf(o.payoutStatus).tone}`">
                  {{ payoutOf(o.payoutStatus).label }}
                </span>
              </div>
            </footer>
          </article>
        </div>
    
        <div v-if="totalPages > 1" class="pager">
          <button class="btn-ghost" :disabled="page === 0" @click="changePage(page - 1)">上一頁</button>
          <span class="pager-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
          <button class="btn-ghost" :disabled="page + 1 >= totalPages" @click="changePage(page + 1)">下一頁</button>
        </div>

      </div>
    </Transition>
  </main>
</template>

<style scoped>
.farmer-page { padding: 32px 24px; max-width: 1000px; margin: 0 auto; }
.page-head h1 { margin: 0 0 20px; font-size: 24px; color: var(--ink); }

.order-list { display: flex; flex-direction: column; gap: 14px; }

/* 訂單卡片 */
.order-card {
  background: #fff;
  border: 1px solid var(--line);
  border-left: 4px solid var(--leaf);
  border-radius: 16px;
  padding: 16px 18px;
  box-shadow: var(--shadow);
  display: flex; flex-direction: column; gap: 12px;
  transition: box-shadow .18s ease, transform .18s ease;
}
.order-card:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }

.order-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.order-id { display: block; font-weight: 600; color: var(--ink); }
.order-date { font-size: 12px; color: var(--muted); }
.order-address { margin: 0; font-size: 14px; color: var(--ink-soft); }

.order-status { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.order-shipped-at { font-size: 12px; color: var(--muted); }

/* 徽章 */
.badge { padding: 3px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge--green { background: #e3f4e8; color: #2f6e46; }
.badge--blue  { background: #e5eefa; color: #2c5d9e; }
.badge--amber { background: #fdf1dc; color: #9a6b15; }
.badge--gray  { background: #eeeeea; color: #75806f; }

/* 明細表格（固定欄寬，數字靠右） */
.item-table { width: 100%; border-collapse: collapse; font-size: 14px; table-layout: fixed; }
.item-table th { text-align: left; font-size: 12px; color: var(--muted); font-weight: 500; padding-bottom: 6px; }
.item-table td { padding: 8px 0; border-top: 1px solid var(--line); color: var(--ink-soft); }
.item-table th:not(:first-child), .item-table td:not(:first-child) { text-align: right; }
.item-name { display: flex; align-items: center; gap: 10px; }
.item-img { width: 40px; height: 40px; object-fit: cover; border-radius: 8px; background: var(--leaf-soft); }

.order-foot { display: flex; align-items: center; justify-content: space-between; gap: 12px; border-top: 1px dashed var(--line); padding-top: 12px; }
.order-subtotal { font-size: 15px; font-weight: 600; color: var(--leaf-dark); }

/* footer 右側：撥款狀態 + 出貨鈕，靠右並排 */
.order-foot-right { display: flex; align-items: center; gap: 12px; }

/* 列表淡入轉場 */
.tab-fade-enter-active,
.tab-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.tab-fade-enter-from { opacity: 0; transform: translateY(8px); }   /* 進場: 從下淡入 (translateY 淡入幅度) */
.tab-fade-leave-to   { opacity: 0; transform: translateY(-8px); }  /* 離場: 往上淡出 */


/* 撥款狀態文字 */
.payout { font-size: 13px; font-weight: 500; }
.payout--muted { color: var(--muted); }
.payout--leaf  { color: var(--leaf-dark); }

/* 按鈕 */
.btn-primary { padding: 8px 20px; border: 1px solid var(--leaf); border-radius: 10px; background: var(--leaf); color: #fff; cursor: pointer; font-size: 14px; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-ghost { padding: 7px 16px; border: 1px solid var(--line); border-radius: 10px; background: #fff; color: var(--ink-soft); cursor: pointer; font-size: 14px; }
.btn-ghost:hover:not(:disabled) { border-color: var(--leaf); color: var(--leaf); }
.btn-ghost:disabled { opacity: .5; cursor: not-allowed; }

.state-box { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 56px 24px; background: #fff; border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); color: var(--muted); text-align: center; }
.state-icon { font-size: 40px; }

.pager { display: flex; align-items: center; justify-content: center; gap: 14px; margin-top: 4px; }
.pager-info { color: var(--ink-soft); font-size: 14px; }
</style>
