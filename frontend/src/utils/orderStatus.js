// 團購訂單相關的三個 enum → 顯示文字與樣式。
// 後端這三個 enum 的序列化是「全大寫」（跟 GroupBuyStatus / RequestStatus 的全小寫不同，
// 混用會導致對照表查不到值、退回顯示原始代碼，例如畫面上直接看到英文的 "PENDING"）。
export const SHIPPED_STATUS_MAP = {
  PENDING: { text: '待出貨', className: 'status--pending' },
  DELIVERED: { text: '已送達', className: 'status--success' },
}
export function shippedStatusInfo(status) {
  return SHIPPED_STATUS_MAP[status] || { text: status || '—', className: '' }
}

export const ORDER_STATUS_MAP = {
  PENDING: { text: '等待中', className: 'status--pending' },
  COMPLETED: { text: '確認收貨', className: 'status--success' },
}
export function orderStatusInfo(status) {
  return ORDER_STATUS_MAP[status] || { text: status || '—', className: '' }
}

export const PAID_STATUS_MAP = {
  UNPAID: { text: '待撥款', className: 'status--pending' },
  PAID: { text: '已撥款', className: 'status--success' },
}
export function paidStatusInfo(status) {
  return PAID_STATUS_MAP[status] || { text: status || '—', className: '' }
}
