// 產地地圖 API：農場清單/詳情，以及某農場的關聯內容。
// 集中在此，view 不直接 fetch（跟 @/api/blog.js 一致，重複邏輯不散落在各頁）。
import http from './http'

// 補上 view 需要的衍生欄位：地區(篩選用)、顯示地點、地圖查詢字串、介紹
export function normalizeFarm(farm) {
  if (!farm) return farm
  const region = farm.cityName || '未分類'
  const location = [farm.cityName, farm.distName].filter(Boolean).join('・')
    || farm.farmAddress || '尚未提供地點'
  const query = farm.mapQuery
    || [farm.cityName, farm.distName, farm.farmAddress, farm.farmName].filter(Boolean).join(' ')
  const desc = farm.farmDesc || '這座農場尚未提供介紹。'
  return { ...farm, region, location, query, desc }
}

// 農場清單 / 單一農場（後端已有）
export const listFarms = () => http.get('/api/farms')
export const getFarm = (farmerId) => http.get(`/api/farms/${farmerId}`)

// 某農場的關聯內容
export const getFarmBlogs = (farmerId) => http.get(`/api/farms/${farmerId}/blogs`)        // 後端已有
export const getFarmTrips = (farmerId) => http.get(`/api/farms/${farmerId}/farm-trips`)    // 後端待加（體驗活動模組負責人）

// 某農場的商品：直接用商品複合查詢的 farmerId 篩選（不用等 /api/farms/{id}/products）。
// 搜尋端點回的是分頁 Page 物件，取出 content 陣列給農場專頁使用。
export const getFarmProducts = (farmerId) =>
  http.get(`/api/products/search?farmerId=${farmerId}&size=100`)
    .then((data) => (Array.isArray(data) ? data : data?.content ?? []))
