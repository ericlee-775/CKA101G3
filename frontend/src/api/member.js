// 一般會員 API（對應後端 UserController：/api/member）
import http from './http'

const BASE = '/api/member'

export const memberApi = {
  // 註冊；reg = { email, password(≥8), userName, userNickname?, districtId?, userAddress?, userPhoneNum?, birthday? }
  register: (reg) => http.post(`${BASE}/register`, reg),

  // 登入；log = { email, password, rememberMe }
  login: (log) => http.post(`${BASE}/login`, log),

  // Google 登入；{ idToken }
  googleLogin: (idToken) => http.post(`${BASE}/oauth/google`, { idToken }),

  // 查自己資料
  getMe: () => http.get(`${BASE}/me`),

  // 修改自己資料；update = { userName?, userNickname?, userPhoneNum?, districtId?, userAddress?, birthday? }
  updateMe: (update) => http.put(`${BASE}/me`, update),

  // 修改密碼；{ oldPassword, newPassword(≥8) }
  changePassword: (pw) => http.put(`${BASE}/me/password`, pw),

  // 註銷帳號
  deleteMe: () => http.del(`${BASE}/me`),
}

export default memberApi
