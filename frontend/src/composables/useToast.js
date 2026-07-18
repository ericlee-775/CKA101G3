import { reactive } from 'vue'

/*
  全域「輕量通知 toast」狀態（單一來源），取代原生 alert()。
  用法（任何元件）：
    import { toast } from '@/composables/useToast'
    toast('已儲存')                  // 成功（綠），1.2 秒自動消失
    toast('操作失敗', 'error')       // 失敗（紅），停 3.5 秒讓人看清楚
    toast('訊息', 'error', 5000)     // 要自訂秒數就傳第三個參數
  搭配掛在 App.vue 的 <ToastMessage />（讀這份 state 來顯示畫面）——跟 useConfirm 同一套路。
*/
const state = reactive({
  open: false,
  message: '',
  type: 'success', // 'success'（綠）| 'error'（紅）
})

// 自動關閉的計時器：純內部簿記，不放進 reactive（不參與畫面渲染，也不暴露給元件）
let timer = null

// 顯示一則通知；不傳 duration 時依類型給預設值——錯誤訊息要停久一點，使用者才來得及看到
export function toast(message, type = 'success', duration) {
  const ms = duration ?? (type === 'error' ? 3500 : 1200)
  if (timer) clearTimeout(timer)
  state.message = message
  state.type = type
  state.open = true
  timer = setTimeout(() => {
    state.open = false
  }, ms)
}

// 給 ToastMessage 元件用：讀取顯示狀態
export function useToastState() {
  return { state }
}
