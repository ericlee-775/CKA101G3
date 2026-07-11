// 會員通知小鈴鐺未讀數狀態顯示

import { reactive, computed } from "vue";
import authStore from "./auth";
import notificationApi from "@/api/memberNotification";

const state = reactive({ unreadCount: 0 })

// 向後端要最新未讀總數
async function refresh(){
    if (!authStore.isMember){
        state.unreadCount = 0
        return
    }
    try {
        state.unreadCount = await notificationApi.getNotifUnreadCount()
    } catch {
    }
}

function decrement(n = 1){
    state.unreadCount = Math.max(0, state.unreadCount - n)
}

function reset(){
    state.unreadCount = 0
}

export const notificationStore = {
    unreadCount: computed(() => state.unreadCount),
    refresh,
    decrement,
    reset,
}

export default notificationStore