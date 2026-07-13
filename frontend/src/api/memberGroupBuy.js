// 會員團購 API（對應後端 MemberGroupBuyController：/api/member/groupBuy，需以一般會員身分登入）
import http from './http'

const BASE = '/api/member/groupBuy'

export const memberGroupBuyApi = {
  // 我已參加、尚未成團的團購清單
  joinedGroupBuyList: () => http.get(`${BASE}/joinedGroupBuyList`),

  // 我已成團的訂單
  mySuccessOrders: () => http.get(`${BASE}/mySuccessOrders`),

  // 我發起過的團購申請（審核中/已通過/已拒絕）→ UnderReviewDTO[]
  myRequests: () => http.get(`${BASE}/myRequests`),

  // 加入團購；join = { buyQty, productName, target, groupPrice, ddlDatetime, pickupAddress }
  joinGroupBuy: (groupBuyId, join) => http.post(`${BASE}/joinGroupBuy/${groupBuyId}`, join),

  // 發起團購；hostCreate = { targetAmount, openDatetime(YYYY-MM-DD), ddlDatetime(YYYY-MM-DD), pickupAddress }
  hostCreate: (productId, hostCreate) => http.post(`${BASE}/hostCreate/${productId}`, hostCreate),

  // 團購主確認收貨（只有該筆團購的發起人可以呼叫，後端會擋非本人；確認過一次就不能再確認）
  confirmReceipt: (orderId) => http.patch(`${BASE}/orders/${orderId}/confirm-receipt`),
}

export default memberGroupBuyApi
