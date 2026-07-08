/*
  小農證明文件（農地 / 產品 / 身分）上傳共用工具。
  上傳走 multipart/form-data（同商品做法），前端直接把 File 放進 FormData，
  後端 MultipartFile 接收後轉 byte[] 存進 FarmerReview。
  這裡只做「送出前的即時檢查」，真正的大小上限由後端 application.properties 的 multipart 設定把關。
*/

// 允許的圖片格式與大小上限（與後端 max-file-size=5MB 對齊，前端先擋以給即時提示）
export const ACCEPTED_CERT_TYPES = ['image/jpeg', 'image/png']
export const MAX_CERT_SIZE = 5 * 1024 * 1024 // 5MB

// 驗證單一檔案，通過回傳 null，否則回傳錯誤訊息字串
export function validateCertFile(file) {
  if (!file) return '請選擇檔案'
  if (!ACCEPTED_CERT_TYPES.includes(file.type)) {
    return '僅接受 JPG 或 PNG 圖片'
  }
  if (file.size > MAX_CERT_SIZE) {
    return '圖片大小不可超過 5MB'
  }
  return null
}