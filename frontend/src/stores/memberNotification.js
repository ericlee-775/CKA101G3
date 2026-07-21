// 會員通知小鈴鐺未讀數狀態顯示

import { reactive, computed } from "vue";
import authStore from "./auth";
import notificationApi from "@/api/memberNotification";

const state = reactive({ 
    unreadCount: 0,
    preview: [],
})

// 向後端要最新未讀總數
async function refresh(){
    if (!authStore.isMember){
        state.unreadCount = 0
        state.preview = []
        return
    }
    try {
        const [count, preview] = await Promise.all([
            notificationApi.getNotifUnreadCount(),
            notificationApi.getNotifPreview()
        ])
        state.unreadCount = count
        state.preview = preview
    } catch {
    }
}

function markRead(notifId){
    const item = state.preview.find(n => n.notificationId === notifId)
    if (item && item.status === 'unread'){
        item.status = 'read'
        state.unreadCount = Math.max(0, state.unreadCount - 1)
    }

    if (!item) {
        state.unreadCount = Math.max(0, state.unreadCount - 1)
    }
}

function markAllRead(){
    state.unreadCount = 0
    state.preview.forEach(n => { n.status = 'read' })
}

function clear(){
    state.unreadCount = 0
    state.preview = [] 
}

export const notificationStore = {
    unreadCount: computed(() => state.unreadCount),
    preview: computed(() => state.preview),
    refresh,
    markRead,
    markAllRead,
    clear,
}

export default notificationStore