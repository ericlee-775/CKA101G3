// 團購 API（對應後端 PublicGroupBuyController：/api/groupBuy，GET 皆 permitAll，不用登入）
import http from './http'

const BASE = '/api/groupBuy'

export const groupBuyApi = {
  // 消費者可看的全部團購清單 → ProductGroupBuyDTO[]
  list: () => http.get(`${BASE}/all`),

  // 單一團購詳情 → GroupBuyDetailDTO
  getOne: (groupBuyId) => http.get(`${BASE}/${groupBuyId}`),
}

export default groupBuyApi
