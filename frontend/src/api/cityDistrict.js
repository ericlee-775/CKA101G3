// 縣市 / 行政區下拉資料（公開，不需登入）
import http from './http'

export const cityDistrictApi = {
  // 回傳 [{ districtId, cityName, distName, zipcode }, ...]
  listAll: () => http.get('/api/city-districts'),
}

export default cityDistrictApi
