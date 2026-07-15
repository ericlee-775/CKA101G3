// 團購收藏 API（對應後端 GroupBuyWishlistController：/api/member/groupBuyLists，需登入一般會員）
import http from './http'

const BASE = '/api/member/groupBuyLists'

export const groupBuyFavoriteApi = {
  // 我收藏的團購清單（只會有 status=open 的）→ WishListDTO[]
  list: () => http.get(BASE),

  // 收藏一筆團購（該團購須為 open 狀態，重複收藏後端會擋）
  add: (groupBuyId) => http.post(`${BASE}/${groupBuyId}`),

  // 取消收藏
  remove: (groupBuyId) => http.del(`${BASE}/${groupBuyId}`),
}

export default groupBuyFavoriteApi
