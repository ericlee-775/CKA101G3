// 小農 API（對應後端 FarmerController：/api/farmer
//                 與 FarmerApplicationController：/api/farmer/application）
import http from './http'

const BASE = '/api/farmer'

export const farmerApi = {
  // 註冊申請；reg = { email, password(≥8), farmName, farmAddress, farmerPhoneNum, farmDesc,
  //                   districtId?, locLat?, locLong?, certFileLand?, certFileProduct?, certFileIdentity? }
  register: (reg) => http.post(`${BASE}/register`, reg),

  // 登入；log = { email, password, rememberMe }
  login: (log) => http.post(`${BASE}/login`, log),

  // 查自己資料
  getMe: () => http.get(`${BASE}/me`),

  // 修改「非審核」欄位（電話、農場描述），立即生效；{ farmerPhoneNum?, farmDesc? }
  updateContact: (req) => http.put(`${BASE}/me`, req),

  // 修改「審核相關」欄位，會重新送審；{ farmName?, districtId?, farmAddress?, locLat?, locLong?, cert...? }
  resubmit: (req) => http.put(`${BASE}/me/application`, req),

  // 修改密碼；{ oldPassword, newPassword(≥8) }
  changePassword: (pw) => http.put(`${BASE}/me/password`, pw),

  // ===== 免登入的申請入口（尚未通過初審 / 未完成驗證，還不能登入時使用）=====
  application: {
    // 查最新審核狀態 + 退件理由；{ email, password }
    status: (log) => http.post(`${BASE}/application/status`, log),

    // 免登入重新送審；{ email, password, farmName?, farmAddress?, districtId?, ... }
    resubmit: (req) => http.post(`${BASE}/application/resubmit`, req),

    // 重寄啟用信；{ email, password }
    resendActivation: (log) => http.post(`${BASE}/application/resend-activation`, log),
  },
}

export default farmerApi
