// 團購狀態（後端 GroupBuyStatus enum）→ 顯示文字與樣式 class。
// 列表頁（GroupBuysView）與詳情頁（GroupBuyDetailView）共用同一份對照表，避免兩邊顯示不一致。
export const GROUP_BUY_STATUS_MAP = {
  open: { text: '開團中', className: 'status--open' },
  success: { text: '已成團', className: 'status--success' },
  failed: { text: '未成團', className: 'status--failed' },
  cancelled: { text: '已取消', className: 'status--cancelled' },
  pending: { text: '待開團', className: 'status--pending' },
}

export function groupBuyStatusInfo(status) {
  return GROUP_BUY_STATUS_MAP[status] || { text: status || '—', className: '' }
}
