// 輕量登入狀態（不引入 Pinia，用 Vue 的 reactive 即可）
//
// 真相來源是「後端的 session cookie」：
//  - 所有 API 都帶 cookie（http.js 的 credentials:'include'），能不能拿到資料由後端決定。
//  - 這個 store 只是「畫面上的鏡子」，讓導覽列/頁面知道現在是誰。
//  - localStorage 只存一個「身分提示(role)」，用來在重新整理後知道該問哪個 /me；
//    真正是否仍登入、最新資料，一律由 hydrate() 向後端重新確認，401 就清掉。
import { reactive, readonly } from 'vue'
import memberApi from '@/api/member'
import farmerApi from '@/api/farmer'

const STORAGE_KEY = 'farmily_auth_role'

// 從 localStorage 還原「身分提示」（不存使用者資料，資料一律跟後端要）
function loadRoleHint() {
  try {
    return localStorage.getItem(STORAGE_KEY) || null
  } catch {
    return null
  }
}

const state = reactive({ user: null, role: loadRoleHint() })

function persistRole() {
  try {
    if (state.role) localStorage.setItem(STORAGE_KEY, state.role)
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // ignore
  }
}

function setUser(user, role) {
  state.user = user
  state.role = role
  persistRole()
}

function clear() {
  state.user = null
  state.role = null
  persistRole()
}

// 以後端 session 為準：用記住的身分向後端要最新資料
//  - 成功 → 更新使用者
//  - 401 → session 已失效，清掉前端狀態
//  - 其他錯誤（網路等）→ 保留現況，不誤清
async function hydrate() {
  if (!state.role) return
  try {
    const me = state.role === 'FARMER' ? await farmerApi.getMe() : await memberApi.getMe()
    setUser(me, state.role)
  } catch (e) {
    if (e && e.status === 401) clear()
  }
}

export const authStore = {
  // 唯讀狀態，避免外部直接改
  state: readonly(state),

  setUser,   // role: 'MEMBER' | 'FARMER'
  clear,
  hydrate,

  get isLoggedIn() {
    return !!state.user
  },
  get isMember() {
    return state.role === 'MEMBER'
  },
  get isFarmer() {
    return state.role === 'FARMER'
  },
}

export default authStore
