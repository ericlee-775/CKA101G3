// 共用的 fetch 封裝：統一帶 session cookie、JSON 轉換、錯誤處理
//
// 為什麼用絕對路徑 /api：
//  - 開發模式(npm run dev)：Vite proxy 會把 /api 轉發到 http://localhost:8080
//  - 部署模式(npm run build)：前端由 Spring Boot 同源提供，/api 直接打同一台後端
// 兩種情況都是同源，session cookie(JSESSIONID)會自動帶上。

// 把後端訊息清成人話：去掉 Spring ResponseStatusException 的
// "415 UNSUPPORTED_MEDIA_TYPE " 這類狀態碼前綴，以及外層多餘引號
function cleanMessage(msg) {
  return String(msg)
    .replace(/^\d{3}\s+[A-Z_]+\s*/, '')   // 去掉「狀態碼 狀態名」前綴
    .replace(/^"([\s\S]*)"$/, '$1')       // 去掉外層引號
    .trim()
}

// 從後端回應中盡量取出「可讀的錯誤訊息」
export async function extractError(response) {
  const text = await response.text().catch(() => '')
  if (!text) return `發生錯誤 (${response.status})`
  let msg = text
  // 後端有些端點回 JSON 物件、有些回純文字字串
  try {
    const data = JSON.parse(text)
    if (data && typeof data === 'object') {
      if (data.message || data.error) {
        msg = data.message || data.error          // 常見錯誤欄位
      } else {
        // Bean Validation 欄位錯誤 map：{ 欄位: 訊息, ... }，用「；」串起來（單行也可讀，不依賴 CSS）
        const messages = Object.values(data).filter((v) => typeof v === 'string')
        msg = messages.length ? messages.join('；') : text
      }
    } else {
      msg = String(data)
    }
  } catch {
    // 純文字，直接用 text
  }
  return cleanMessage(msg) || `發生錯誤 (${response.status})`
}

// 從後端錯誤回應取出「程式判斷用」的錯誤碼（對應 ApiError.code），取不到回 null
async function extractErrorCode(response) {
  const text = await response.text().catch(() => '')
  if (!text) return null
  try {
    const data = JSON.parse(text)
    return data && typeof data === 'object' ? (data.code || null) : null
  } catch {
    return null
  }
}

// 核心請求方法
async function request(url, { method = 'GET', body, headers = {} } = {}) {
  const options = {
    method,
    credentials: 'include',        // 帶上 / 接收 session cookie
    headers: { ...headers },
  }

  if (body !== undefined && body !== null) {
    if (body instanceof FormData) {
      // multipart/form-data：不要手動設 Content-Type，讓瀏覽器自動補上 boundary
      options.body = body
    } else {
      options.headers['Content-Type'] = 'application/json'
      options.body = JSON.stringify(body)
    }
  }

  const response = await fetch(url, options)

  if (!response.ok) {
    // response body 只能讀一次，clone 一份給 code 解析用
    const message = await extractError(response.clone())
    const err = new Error(message)
    err.status = response.status
    err.code = await extractErrorCode(response)   // 後端 ApiError.code，例如 EMAIL_NOT_VERIFIED
    throw err
  }

  // 204 或空 body：回 null
  if (response.status === 204) return null

  const text = await response.text()
  if (!text) return null

  // 後端回傳有時是 JSON DTO、有時是純文字訊息，兩種都支援
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

export const http = {
  get: (url, opts) => request(url, { ...opts, method: 'GET' }),
  post: (url, body, opts) => request(url, { ...opts, method: 'POST', body }),
  put: (url, body, opts) => request(url, { ...opts, method: 'PUT', body }),
  patch: (url, body, opts) => request(url, {...opts, method: 'PATCH', body}),
  del: (url, body, opts) => request(url, { ...opts, method: 'DELETE', body }),
}

export default http
