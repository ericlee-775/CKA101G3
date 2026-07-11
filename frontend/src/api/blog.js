// 部落格 API。小農(/api/farmer/blogs) 與 會員(/api/member/blogs) 共用同一套邏輯，
// 只差 base 路徑與「分類規則」：小農固定產地日記(1)、會員自己選分類。
import http, { extractError } from './http'

// 共用：送 FormData、統一錯誤處理
async function sendForm(url, method, fd) {
  const res = await fetch(url, { method, body: fd, credentials: 'include' })
  if (!res.ok) {
    const err = new Error(await extractError(res))   // 清成人話，不把原始 JSON 丟給使用者
    err.status = res.status
    throw err
  }
  if (res.status === 204) return null
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

// 工廠：給一個 base 和「固定分類 id(可為 null)」，產出一組 CRUD + 照片 API
function createBlogApi(base, fixedBlogTypeId) {
  async function sendMultipart(url, method, data) {
    const fd = new FormData()
    fd.append('blogTitle', data.blogTitle)
    fd.append('blogContent', data.blogContent)
    // 小農固定用 fixedBlogTypeId；會員用表單選的 data.blogTypeId
    fd.append('blogTypeId', fixedBlogTypeId ?? data.blogTypeId)
    if (data.blogImg) fd.append('file', data.blogImg)               // 封面（單張）
    if (data.photos && data.photos.length) {                        // 相簿（多張）
      data.photos.forEach((f) => fd.append('photos', f))
    }
    return sendForm(url, method, fd)
  }

  return {
    listMine: (offset = 0, limit = 10) => http.get(`${base}?offset=${offset}&limit=${limit}`),
    getMine: (blogId) => http.get(`${base}/${blogId}`),
    createMine: (data) => sendMultipart(base, 'POST', data),
    updateMine: (blogId, data) => sendMultipart(`${base}/${blogId}`, 'PUT', data),
    deleteMine: (blogId) => http.del(`${base}/${blogId}`),

    // ===== 照片集 =====
    listPhotos: (blogId) => http.get(`/api/blogs/${blogId}/photos`), // 公開讀
    photoImgUrl: (photoId) => `/api/photos/${photoId}/image`,        // 公開圖檔
    addPhotos: (blogId, files) => {
      const fd = new FormData()
      files.forEach((f) => fd.append('photos', f))
      return sendForm(`${base}/${blogId}/photos`, 'POST', fd)
    },
    deletePhoto: (photoId) => http.del(`${base}/photos/${photoId}`),
  }
}

// 部落格分類清單（公開）：給會員發文選分類用
export const listBlogTypes = () => http.get('/api/blogs/types')

// ===== 公開（商城前台，任何人可讀）=====
// params 可選：{ blogTypeId, search, orderBy, sort }；有值才帶，維持與舊呼叫相容
export const listPublicBlogs = (offset = 0, limit = 5, params = {}) => {
  const q = new URLSearchParams({ offset, limit })
  if (params.blogTypeId) q.set('blogTypeId', params.blogTypeId)
  if (params.search) q.set('search', params.search)
  if (params.orderBy) q.set('orderBy', params.orderBy)
  if (params.sort) q.set('sort', params.sort)
  return http.get(`/api/blogs?${q.toString()}`)
}
export const getPublicBlog = (blogId) => http.get(`/api/blogs/${blogId}`)
export const publicBlogPhotos = (blogId) => http.get(`/api/blogs/${blogId}/photos`)
export const publicBlogComments = (blogId) => http.get(`/api/blogs/${blogId}/comments`)

// 需登入會員（userId 由後端 session 取，前端不用傳）
export const addBlogComment = (blogId, commentPost) =>
  http.post(`/api/blogs/${blogId}/comments`, { commentPost })
// 刪自己的留言（後端驗證只能刪本人的）
export const deleteBlogComment = (commentId) =>
  http.del(`/api/blogs/comments/${commentId}`)
export const likeBlog = (blogId) =>
  http.post(`/api/blogs/${blogId}/like`)
// 進頁面查目前會員按過沒（未登入回 liked:false）
export const blogLikeStatus = (blogId) =>
  http.get(`/api/blogs/${blogId}/like-status`)

// 檢舉（reportReason 必填；檢舉人由後端 session 取）
export const reportBlog = (blogId, reportReason) =>
  http.post(`/api/blogs/${blogId}/reports`, { reportReason })
export const reportComment = (commentId, reportReason) =>
  http.post(`/api/comments/${commentId}/reports`, { reportReason })

export const farmerBlogApi = createBlogApi('/api/farmer/blogs', 1)   // 小農：固定產地日記
export const memberBlogApi = createBlogApi('/api/member/blogs', null) // 會員：分類自己選

// 向後相容：既有小農頁面 import 的 default 仍是小農版
export default farmerBlogApi
