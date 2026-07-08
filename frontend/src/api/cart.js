// 購物車 API（對應後端 ProductShoppingCartController：/api/shoppingcart）
//
// 這些端點都需要登入：http.js 會帶上 session cookie，未登入後端會回 401。
// userId 一律由後端從登入身分(session)取得，前端不需要、也不該自己帶。
import http from './http'

const BASE = '/api/shoppingcart'

export const cartApi = {
  // 取得目前登入者的購物車
  // 回傳：[{ productId, productName, retailPrice, unitPricingMeasure, quantity }]
  getCart: () => http.get(BASE),

  // 加入商品（同商品已存在會累加）；quantity 走 query string
  add: (productId, quantity = 1) =>
    http.post(`${BASE}/${productId}?quantity=${quantity}`),

  // 設定某商品的數量（絕對值，非累加）
  update: (productId, quantity) =>
    http.put(`${BASE}/${productId}?quantity=${quantity}`),

  // 移除某商品
  remove: (productId) => http.del(`${BASE}/${productId}`),
}

export default cartApi
