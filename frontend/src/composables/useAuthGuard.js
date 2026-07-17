import { confirm } from './useConfirm'
import authStore from '@/stores/auth'

/*
  會員限定操作的共用守門（收藏、按讚…都能用），取代各頁自己複製的「請先登入」彈窗。
  用法（任何元件，需傳入該頁的 router）：
    import { requireMemberLogin } from '@/composables/useAuthGuard'
    if (!(await requireMemberLogin(router, '登入會員後即可收藏喜歡的商品。'))) return
    ...已確認是登入會員，往下執行
  回傳 true＝是登入會員可繼續；false＝已被擋下（彈窗問過了，按「前往登入」會自動導頁）。
*/
export async function requireMemberLogin(router, message = '登入會員後即可使用此功能。') {
  if (authStore.isLoggedIn && authStore.isMember) return true

  const goLogin = await confirm({
    title: '請先登入會員',
    message,
    confirmText: '前往登入',
    cancelText: '稍後再說',
  })
  if (goLogin) router.push('/login')
  return false
}
