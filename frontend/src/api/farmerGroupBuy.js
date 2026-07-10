// 小農團購管理 API（對應後端 FarmerGroupBuyController：/api/farmer/groupBuy，需登入小農身分）
import http from './http'

const BASE = '/api/farmer/groupBuy'

export const farmerGroupBuyApi = {
  // 自己商品底下的團購清單（含團購主的發起申請）→ GroupBuyFarmerDTO[]
  list: () => http.get(`${BASE}/list`),

  // 單筆團購申請的詳細內容（審核清單「詳細資料」按鈕用）→ GroupBuyVO
  getOne: (groupBuyId) => http.get(`${BASE}/list/${groupBuyId}`),

  // 審核團購申請；res = { requestStatus: 'approved' | 'rejected', rejectReason? }
  // 拒絕時 rejectReason 必填（後端會驗證），通過後不可再改。
  review: (groupBuyId, res) => http.post(`${BASE}/farmerResponse/${groupBuyId}`, res),

  // 開團中團購的進度摘要（團購主/取貨地址/目前總數量/總金額）→ GroupBuyParticipationDTO
  progress: (groupBuyId) => http.get(`${BASE}/progress/${groupBuyId}`),

  // 自己商品底下的團購訂單總表 → GroupBuyOrderDTO[]
  orderList: () => http.get(`${BASE}/orderList`),

  // 單筆訂單詳細資料（含團購主聯絡方式）→ GroupBuyOrderDTO
  getOrder: (orderId) => http.get(`${BASE}/order/${orderId}`),

  // 出貨狀態更新為已送達
  shipOrder: (orderId) => http.post(`${BASE}/orderShipped/${orderId}`, {}),
}

export default farmerGroupBuyApi
