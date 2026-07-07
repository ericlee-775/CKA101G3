// 會員團購 API（對應後端 MemberGroupBuyController：/api/member/groupBuy，需以一般會員身分登入）
import http from './http'

const BASE = '/api/member/groupBuy'

export const memberGroupBuyApi = {
  // 我已參加、尚未成團的團購清單
  joinedGroupBuyList: () => http.get(`${BASE}/joinedGroupBuyList`),

  // 我已成團的訂單
  mySuccessOrders: () => http.get(`${BASE}/mySuccessOrders`),

  // 加入團購；join = { buyQty, productName, target, groupPrice, ddlDatetime, pickupAddress }
  joinGroupBuy: (groupBuyId, join) => http.post(`${BASE}/joinGroupBuy/${groupBuyId}`, join),

  // 發起團購；hostCreate = { targetAmount, openDatetime(YYYY-MM-DD), ddlDatetime(YYYY-MM-DD), pickupAddress }
  hostCreate: (productId, hostCreate) => http.post(`${BASE}/hostCreate/${productId}`, hostCreate),
}

export default memberGroupBuyApi
