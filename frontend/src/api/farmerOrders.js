import http from "./http";

// 小農後台訂單管理
const BASE = '/api/farmer/orders'

export const farmerOrdersApi = {

    list: (page = 0) => http.get(`${BASE}?page=${page}`),

    ship: (orderId) => http.patch(`${BASE}/${orderId}/shipped`),

}

export default farmerOrdersApi