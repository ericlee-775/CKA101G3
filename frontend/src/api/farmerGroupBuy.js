// 小農團購管理 API（對應後端 FarmerGroupBuyController：/api/farmer/groupBuy，需登入小農身分）
import http from './http'

const BASE = '/api/farmer/groupBuy'

export const farmerGroupBuyApi = {
  // 自己商品底下的團購清單（含團購主的發起申請）→ GroupBuyFarmerDTO[]
  list: () => http.get(`${BASE}/list`),

  // 單筆團購申請的詳細內容（審核清單「詳細資料」按鈕用）→ GroupBuyFarmerDTO
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

  // 出貨狀態更新；shippedStatus: 'PENDING' | 'DELIVERED'
  // 注意：後端目前不論送什麼值，實際都會直接改成 DELIVERED（ship() 沒有讀 body），
  // 這裡仍照下拉選單選的值送出，等後端之後照 body 真的判斷狀態時前端不用再改。
  shipOrder: (orderId, shippedStatus) => http.post(`${BASE}/orderShipped/${orderId}`, { shippedStatus }),

  // 團購總收益（已撥款訂單的總金額加總）→ 純數字，不是包在物件裡
  totalRevenue: () => http.get(`${BASE}/totalRevenue`),
}

export default farmerGroupBuyApi
