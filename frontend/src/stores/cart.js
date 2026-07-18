// 購物車狀態（訪客本地 + 登入後串後端）
//
// UI 一律讀同一份 state.items，元件不用知道背後是本地還是後端：
//  - 未登入（訪客）：購物車存在瀏覽器 localStorage，重新整理還在。
//  - 已登入：以後端為準（/api/shoppingcart），所有增/刪/改都打 API 後再重載。
//
// 登入的瞬間（watch isLoggedIn 由 false → true）會把訪客本地購物車「合併」進後端，
// 清掉本地暫存，之後就完全走後端；登出則回到（已清空的）本地購物車。
import { reactive, computed, readonly, watch } from 'vue'
import authStore from './auth'
import cartApi from '@/api/cart'

const STORAGE_KEY = 'farmily_cart'

function isLoggedIn() {
  return authStore.isLoggedIn
}

// ---------- 本地暫存（訪客用） ----------
function loadLocal() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const arr = raw ? JSON.parse(raw) : []
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

function persistLocal() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.items))
  } catch {
    // 存不進去（例如無痕模式額度滿）就算了，不影響畫面
  }
}

function clearLocalStorage() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // ignore
  }
}

const state = reactive({ items: loadLocal() })

// 原地把 state.items 換成新內容（保持同一個陣列參考，reactive 才會更新）
function setItems(list) {
  state.items.splice(0, state.items.length, ...list)
}

// ---------- 以後端為準（已登入） ----------
async function reloadFromServer() {
  try {
    const rows = await cartApi.getCart()
    setItems(
      (rows || []).map((r) => ({
        productId: r.productId,
        productName: r.productName,
        retailPrice: Number(r.retailPrice) || 0,
        unitPricingMeasure: r.unitPricingMeasure || '',
        quantity: r.quantity,
      })),
    )
  } catch {
    // 讀取失敗（例如 session 失效）就不動畫面，交給 auth 的 hydrate 去處理登出
  }
}

// 加入商品。product 需含 productId / productName / retailPrice / unitPricingMeasure。
async function addItem(product, quantity = 1) {
  if (isLoggedIn()) {
    await cartApi.add(product.productId, quantity)
    await reloadFromServer()
    return
  }
  // 訪客：本地累加
  const id = product.productId
  const existing = state.items.find((it) => it.productId === id)
  if (existing) {
    existing.quantity += quantity
  } else {
    state.items.push({
      productId: id,
      productName: product.productName,
      retailPrice: Number(product.retailPrice) || 0,
      unitPricingMeasure: product.unitPricingMeasure || '',
      quantity,
    })
  }
  persistLocal()
}

// 設定某商品的數量（最少 1）
async function setQuantity(id, quantity) {
  const q = Math.max(1, Math.floor(Number(quantity)) || 1)
  if (isLoggedIn()) {
    // 先樂觀更新畫面，再打 API；失敗就用後端資料校正回來
    const item = state.items.find((it) => it.productId === id)
    if (item) item.quantity = q
    try {
      await cartApi.update(id, q)
    } catch {
      await reloadFromServer()
    }
    return
  }
  const item = state.items.find((it) => it.productId === id)
  if (!item) return
  item.quantity = q
  persistLocal()
}

// 移除某商品
async function removeItem(id) {
  if (isLoggedIn()) {
    await cartApi.remove(id)
    await reloadFromServer()
    return
  }
  const i = state.items.findIndex((it) => it.productId === id)
  if (i !== -1) state.items.splice(i, 1)
  persistLocal()
}

// 清空購物車
async function clear() {
  if (isLoggedIn()) {
    // 一次清空（打 DELETE /api/shoppingcart，後端 deleteByUserId）
    await cartApi.clearAll()
    await reloadFromServer()
    return
  }
  state.items.splice(0, state.items.length)
  persistLocal()
}

// 依登入狀態把購物車切到正確來源。
//  - 已登入：把本地暫存的購物車合併進後端，清掉本地，再以後端為準重載。
//  - 未登入：回到本地購物車。
async function sync() {
  if (isLoggedIn()) {
    const local = loadLocal()
    for (const it of local) {
      try {
        await cartApi.add(it.productId, it.quantity)
      } catch {
        // 單筆合併失敗就跳過，不擋其他
      }
    }
    clearLocalStorage()
    await reloadFromServer()
  } else {
    setItems(loadLocal())
  }
}

// 登入狀態一改變就重新同步（登入合併、登出回本地）。
// 注意：App 啟動時 auth.hydrate() 會把 user 由 null 補成登入者，
// 觸發 false → true，這裡就會自動把本地購物車合併進後端。
watch(() => authStore.isLoggedIn, () => {
  sync()
})

// 商品總件數（給 header 小紅點用）
const totalCount = computed(() =>
  state.items.reduce((sum, it) => sum + it.quantity, 0),
)

// 金額總計
const totalPrice = computed(() =>
  state.items.reduce((sum, it) => sum + it.retailPrice * it.quantity, 0),
)

export const cartStore = {
  // 唯讀狀態，避免外部直接改（要改一律走下面的方法）
  state: readonly(state),

  addItem,
  setQuantity,
  removeItem,
  clear,
  sync,
  // 後端已經改動購物車（例如結帳時 clearCart）時，讓前端重讀一次
  reload: reloadFromServer,

  totalCount,
  totalPrice,
}

export default cartStore
