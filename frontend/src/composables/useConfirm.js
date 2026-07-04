import { reactive } from 'vue'

/*
  全域「確認彈窗」狀態（單一來源）。
  用法（任何元件）：
    import { confirm } from '@/composables/useConfirm'
    if (!(await confirm({ title: '登出', message: '確定要登出嗎？', danger: true }))) return
    ...按下確定才會往下執行

  搭配掛在 App.vue 的 <ConfirmDialog />（讀這份 state 來顯示畫面）。
*/
const state = reactive({
  open: false,
  title: '',
  message: '',
  confirmText: '確定',
  cancelText: '取消',
  danger: false,     // true 時確定鈕用紅色（破壞性操作）
  _resolve: null,    // 保存目前這次 confirm 的 Promise resolve
})

// 開啟彈窗，回傳一個 Promise：使用者按「確定」→ true、「取消」→ false
export function confirm(options = {}) {
  // 若前一個彈窗還開著，先當作取消結束，避免 resolve 遺失
  if (state._resolve) state._resolve(false)

  state.title = options.title || '確認操作'
  state.message = options.message || ''
  state.confirmText = options.confirmText || '確定'
  state.cancelText = options.cancelText || '取消'
  state.danger = options.danger || false
  state.open = true

  return new Promise((resolve) => {
    state._resolve = resolve
  })
}

// 給 ConfirmDialog 元件用：回應使用者的選擇並關閉彈窗
export function useConfirmDialog() {
  function respond(result) {
    if (!state.open) return
    state.open = false
    if (state._resolve) {
      state._resolve(result)
      state._resolve = null
    }
  }
  return { state, respond }
}