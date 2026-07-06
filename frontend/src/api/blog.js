// 小農產地日記 API（對應後端 FarmerBlogController：/api/farmer/blogs）
import http from './http'

const BASE = '/api/farmer/blogs'

// 新增 / 編輯要夾帶圖片檔 → 必須用 multipart(FormData)，
// 不能走 http.js（它會把 body 強制轉成 JSON）。這裡用原生 fetch。
async function sendMultipart(url, method, data) {
  const fd = new FormData()
  fd.append('blogTitle', data.blogTitle)
  fd.append('blogContent', data.blogContent)
  fd.append('blogTypeId', 1) // 產地日記固定=1（後端也會再強制一次）
  // 有選新圖才夾帶（欄位名用 file，後端用 @RequestParam 單獨接）；沒選就不帶 → 後端沿用舊圖
  if (data.blogImg) fd.append('file', data.blogImg)

  const res = await fetch(url, { method, body: fd, credentials: 'include' })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    const err = new Error(text || `發生錯誤 (${res.status})`)
    err.status = res.status
    throw err
  }
  if (res.status === 204) return null
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export const blogApi = {
  // 我的產地日記列表（分頁）
  listMine: (offset = 0, limit = 10) =>
    http.get(`${BASE}?offset=${offset}&limit=${limit}`),

  // 查看單篇
  getMine: (blogId) => http.get(`${BASE}/${blogId}`),

  // 新增（multipart）；data = { blogTitle, blogContent, blogImg?(File) }
  createMine: (data) => sendMultipart(BASE, 'POST', data),

  // 編輯（multipart）
  updateMine: (blogId, data) => sendMultipart(`${BASE}/${blogId}`, 'PUT', data),

  // 刪除
  deleteMine: (blogId) => http.del(`${BASE}/${blogId}`),
}

export default blogApi
