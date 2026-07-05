// 縣市 / 行政區下拉資料（公開，不需登入）
import http from './http'

export const cityDistrictApi = {
  // 回傳 [{ districtId, cityName, distName, zipcode }, ...]
  listAll: () => http.get('/api/city-districts'),
}

// 同時提供 default 匯出：讓 `import cityDistrictApi from '@/api/cityDistrict'` 也能用
export default cityDistrictApi

