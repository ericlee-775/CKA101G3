// 部落格 API。小農(/api/farmer/blogs) 與 會員(/api/member/blogs) 共用同一套邏輯，
// 只差 base 路徑與「分類規則」：小農固定產地日記(1)、會員自己選分類。
import http from './http'

// 共用：送 FormData、統一錯誤處理
async function sendForm(url, method, fd) {
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

export const farmerBlogApi = createBlogApi('/api/farmer/blogs', 1)   // 小農：固定產地日記
export const memberBlogApi = createBlogApi('/api/member/blogs', null) // 會員：分類自己選

// 向後相容：既有小農頁面 import 的 default 仍是小農版
export default farmerBlogApi
