<script setup>
// 會員中心：我的體驗活動報名
// （這段原本放在公開的農遊體驗頁 FarmTripsView.vue，改搬到會員中心）
// 後端端點（CustomerController，/api/farm-trips）：
//   GET /orders/mine?userId={id}   → 我的報名清單
//   PUT /orders/{orderId}/cancel   → 取消報名（只有 CONFIRMED 可取消）
import { ref, onMounted } from 'vue'
import authStore from '@/stores/auth'

// 目前登入的會員 id（後端 /orders/mine 目前用 query 帶）
const userId = ref(authStore.state.user?.userId)

const myOrders = ref([])
const myOrdersMsg = ref('')

async function loadMyOrders() {
  myOrdersMsg.value = ''
  if (!userId.value) {
    myOrdersMsg.value = '請先登入會員。'
    return
  }
  try {
    const res = await fetch(`/api/farm-trips/orders/mine?userId=${userId.value}`, {
      credentials: 'include',
    })
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    myOrders.value = await res.json()
    if (myOrders.value.length === 0) myOrdersMsg.value = '目前沒有報名紀錄。'
  } catch (e) {
    myOrdersMsg.value = '查詢失敗：' + (e.message || '請稍後再試')
  }
}

async function cancelOrder(orderId) {
  try {
    const res = await fetch(`/api/farm-trips/orders/${orderId}/cancel`, {
      method: 'PUT',
      credentials: 'include',
    })
    if (!res.ok) {
      const msg = await res.text()
      throw new Error(msg || `伺服器回應 ${res.status}`)
    }
    loadMyOrders()
  } catch (e) {
    myOrdersMsg.value = '取消失敗：' + (e.message || '請稍後再試')
  }
}

// 進頁面就先查一次
onMounted(loadMyOrders)
</script>

<template>
  <section class="my-orders">
    <h1>已報名活動</h1>
    <button class="btn" @click="loadMyOrders">查詢我的報名</button>
    <p v-if="myOrdersMsg" class="muted">{{ myOrdersMsg }}</p>
    <ul v-if="myOrders.length" class="order-list">
      <li v-for="o in myOrders" :key="o.farmTripOrderId">
        <span>訂單 {{ o.farmTripOrderBookingNo }}｜{{ o.numPeople }} 人｜{{ o.orderStatus }}</span>
        <button
          v-if="o.orderStatus === 'CONFIRMED'"
          class="btn-outline"
          @click="cancelOrder(o.farmTripOrderId)"
        >取消</button>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.my-orders h1 { margin: 0 0 16px; font-size: 24px; color: var(--ink); }
.muted { color: var(--muted); }

.btn {
  background: var(--leaf); color: #fff; border: none; padding: 8px 16px;
  border-radius: 10px; cursor: pointer; font-size: 14px;
}
.btn:hover { background: var(--leaf-dark); }
.btn-outline {
  background: #fff; color: var(--leaf-dark); border: 1px solid var(--leaf);
  padding: 6px 14px; border-radius: 10px; cursor: pointer; font-size: 14px; margin: 8px 0;
}

.order-list { list-style: none; padding: 0; margin-top: 16px; }
.order-list li {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; border-bottom: 1px solid var(--line); gap: 12px;
}
</style>
