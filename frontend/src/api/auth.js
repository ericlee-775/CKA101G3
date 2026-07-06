// 三身分共用的認證 API（對應後端 AuthController）
// accountType 只接受 'MEMBER' 或 'FARMER'
import http from './http'

export const authApi = {
  // 登出（清掉 session）
  logout: () => http.post('/api/logout'),

  // 點驗證信連結後完成 Email 驗證；token 來自信中連結
  verifyEmail: (token) =>
    http.get(`/api/auth/verify-email?token=${encodeURIComponent(token)}`),

  // 重寄 Email 驗證信；(email, 'MEMBER' | 'FARMER')
  resendVerification: (email, accountType) =>
    http.post('/api/auth/resend-verification', { email, accountType }),

  // 忘記密碼第一步：寄重設密碼信
  forgotPassword: (email, accountType) =>
    http.post('/api/auth/forgot-password', { email, accountType }),

  // 忘記密碼第二步：帶 token + 新密碼(≥8) 重設
  resetPassword: (token, newPassword) =>
    http.post('/api/auth/reset-password', { token, newPassword }),
}

export const ACCOUNT_TYPE = { MEMBER: 'MEMBER', FARMER: 'FARMER' }

export default authApi
