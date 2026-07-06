// 共用的 fetch 封裝：統一帶 session cookie、JSON 轉換、錯誤處理
//
// 為什麼用絕對路徑 /api：
//  - 開發模式(npm run dev)：Vite proxy 會把 /api 轉發到 http://localhost:8080
//  - 部署模式(npm run build)：前端由 Spring Boot 同源提供，/api 直接打同一台後端
// 兩種情況都是同源，session cookie(JSESSIONID)會自動帶上。

// 從後端回應中盡量取出「可讀的錯誤訊息」
async function extractError(response) {
  const text = await response.text().catch(() => '')
  if (!text) return `發生錯誤 (${response.status})`
  // 後端有些端點回 JSON 物件、有些回純文字字串
  try {
    const data = JSON.parse(text)
    // 常見錯誤欄位：message / error；驗證錯誤可能是欄位對訊息的物件
    if (data && typeof data === 'object') {
      return data.message || data.error || text
    }
    return String(data)
  } catch {
    return text
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
    const message = await extractError(response)
    const err = new Error(message)
    err.status = response.status
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
  del: (url, body, opts) => request(url, { ...opts, method: 'DELETE', body }),
}

export default http
