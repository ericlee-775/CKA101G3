// 會員通知 API (對應後端 MemberNotificationController: /api/member/notifications)
import http from './http'

const BASE = '/api/member/notifications'

export const notificationApi = {

    // 查看我的通知列表
    list: (targetType = '', page = 0) => {
        const params = new URLSearchParams()  // 用於收集參數、組建網址
        if (targetType) {
            params.set('targetType', targetType)
        }
        params.set('page', page)
        return http.get(`${BASE}?${params.toString()}`) // 例:/api/member/notifications?targetType=ORDER&page=0
    },

    // markOneAsRead, updateStatus(@PathVariable Integer notifId)
    markOneAsRead: (notifId) => http.patch(`${BASE}/${notifId}/read`),

    // markAllAsRead, updatAllStatus(@AuthenticationPrincipal MemberUserDetails user)
    markAllAsRead: () => http.patch(`${BASE}/read-all`),

    // 小鈴鐺列表, getNotifPreview(@AuthenticationPrincipal MemberUserDetails user)
    getNotifPreview: () => http.get(`${BASE}/preview`),

    // 小鈴鐺未讀數, getNotifUnreadCount(@AuthenticationPrincipal MemberUserDetails user)
    getNotifUnreadCount: () => http.get(`${BASE}/unread-count`)

}

export default notificationApi