// 小農團購管理 API（對應後端 FarmerGroupBuyController：/api/farmer/groupBuy，需登入小農身分）
import http from './http'

const BASE = '/api/farmer/groupBuy'

export const farmerGroupBuyApi = {
  // 自己商品底下的團購清單（含團購主的發起申請）→ GroupBuyFarmerDTO[]
  list: () => http.get(`${BASE}/list`),

  // 審核團購申請；res = { requestStatus: 'approved' | 'rejected', rejectReason? }
  // 拒絕時 rejectReason 必填（後端會驗證），通過後不可再改。
  review: (groupBuyId, res) => http.post(`${BASE}/farmerResponse/${groupBuyId}`, res),
}

export default farmerGroupBuyApi
