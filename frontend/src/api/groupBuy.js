// 團購 API（對應後端 PublicGroupBuyController：/api/groupBuy，GET 皆 permitAll，不用登入）
import http from './http'

const BASE = '/api/groupBuy'

export const groupBuyApi = {
  // 消費者可看的團購清單 → ProductGroupBuyDTO[]
  // type: 'all'（總覽，預設，pending+open 都給）/ 'available'（可發起，status=pending）/ 'open'（開團中，status=open）
  list: (type = 'all') => http.get(`${BASE}/consumer/list?type=${type}`),

  // 單一團購詳情 → GroupBuyDetailDTO
  getOne: (groupBuyId) => http.get(`${BASE}/${groupBuyId}`),
}

export default groupBuyApi
