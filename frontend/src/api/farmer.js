// 小農 API（對應後端 FarmerController：/api/farmer
//                 與 FarmerApplicationController：/api/farmer/application）
import http from './http'

const BASE = '/api/farmer'

export const farmerApi = {
  // 註冊申請；因含證明文件圖片，請傳 FormData（multipart/form-data），欄位：
  //   email, password(≥8), farmName, farmAddress, farmerPhoneNum, farmDesc,
  //   certFileLand, certFileProduct, certFileIdentity(File), districtId?, locLat?, locLong?
  register: (reg) => http.post(`${BASE}/register`, reg),

  // 登入；log = { email, password, rememberMe }
  login: (log) => http.post(`${BASE}/login`, log),

  // 查自己資料
  getMe: () => http.get(`${BASE}/me`),

  // 查自己所有審核輪次紀錄（含各輪文件有無；不含承辦管理員資訊）
  getMyReviews: () => http.get(`${BASE}/me/reviews`),

  // 某輪某類文件的圖片網址（給 <img :src> 用；同源會自動帶 session cookie）
  //   type = 'land' | 'product' | 'identity'
  certUrl: (reviewId, type) => `${BASE}/me/reviews/${reviewId}/cert/${type}`,

  // 修改「非審核」欄位（電話、農場描述），立即生效；{ farmerPhoneNum?, farmDesc? }
  updateContact: (req) => http.put(`${BASE}/me`, req),

  // 修改「審核相關」欄位，會重新送審；含證明文件圖片，請傳 FormData（multipart/form-data）：
  //   farmName?, districtId?, farmAddress?, locLat?, locLong?,
  //   certFileLand?, certFileProduct?, certFileIdentity?(File；不帶則後端沿用上一輪)
  // 用 POST（非 PUT）：PUT+multipart 在 Tomcat 不會被解析，會 403
  resubmit: (req) => http.post(`${BASE}/me/application`, req),

  // 修改密碼；{ oldPassword, newPassword(≥8) }
  changePassword: (pw) => http.put(`${BASE}/me/password`, pw),

  // ===== 免登入的申請入口（尚未通過初審 / 未完成驗證，還不能登入時使用）=====
  application: {
    // 查最新審核狀態 + 退件理由；{ email, password }
    status: (log) => http.post(`${BASE}/application/status`, log),

    // 免登入重新送審；含證明文件圖片，請傳 FormData（multipart/form-data），欄位：
    //   email, password, farmName?, farmAddress?, districtId?,
    //   certFileLand, certFileProduct, certFileIdentity(File)
    resubmit: (req) => http.post(`${BASE}/application/resubmit`, req),

    // 重寄啟用信；{ email, password }
    resendActivation: (log) => http.post(`${BASE}/application/resend-activation`, log),
  },
}

export default farmerApi
