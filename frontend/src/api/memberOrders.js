import http from "./http";

// 會員商品訂單
const BASE = "/api/member/orders"

export const memberOrdersApi = {

    list: (page = 0) => http.get(`${BASE}?page=${page}`),
    
    listItems: (orderId) => http.get(`${BASE}/${orderId}/items`),

    received: (orderId) => http.patch(`${BASE}/${orderId}/farmers/${farmerId}/received`),
    
    checkoutInfo: () => http.get(`${BASE}/checkout-info`),
    
    checkout: (req) => http.post(`${BASE}/checkout`, req),


}

export default memberOrdersApi