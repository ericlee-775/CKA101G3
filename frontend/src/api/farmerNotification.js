// 小農通知 API (對應後端 FarmerNotificationController: /api/farmer/notifications)
import http from "./http";

const BASE = '/api/farmer/notifications'

export const notificationApi ={

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
    
        // markAllAsRead, updatAllStatus(@AuthenticationPrincipal FarmerUserDetails user)
        markAllAsRead: () => http.patch(`${BASE}/read-all`),
    
        // 小鈴鐺列表, getNotifPreview(@AuthenticationPrincipal FarmerUserDetails user)
        getNotifRewview: () => http.get(`${BASE}/preview`),
    
        // 小鈴鐺未讀數, getNotifUnreadCount(@AuthenticationPrincipal FarmerUserDetails user)
        getNotifUnreadCount: () => http.get(`${BASE}/unread-count`)
    
}