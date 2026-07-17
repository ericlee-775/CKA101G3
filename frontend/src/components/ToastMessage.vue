<script setup>
import { useToastState } from '@/composables/useToast'

// 全域輕量通知（取代 alert）：掛在 App.vue，任何地方呼叫 toast() 都由這裡顯示。
// 樣式：桌面通知風——白卡片從右上角滑入，左緣色條標示成功（綠）/失敗（紅），
// 自動消失、pointer-events:none 完全不擋使用者操作。
const { state } = useToastState()
</script>

<template>
  <!-- Teleport 到 body：避免被 header 的 sticky / z-index 蓋住（同 ConfirmDialog 做法） -->
  <Teleport to="body">
    <Transition name="toast-slide">
      <div v-if="state.open" class="toast" :class="`toast--${state.type}`" role="status">
        <span class="toast__ico">{{ state.type === 'error' ? '⚠️' : '✅' }}</span>
        <p class="toast__msg">{{ state.message }}</p>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 2000; /* 蓋過 ConfirmDialog(1000) 和各頁彈窗 */
  pointer-events: none; /* 通知永遠不攔截滑鼠，下面的頁面照常可操作 */
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 200px;
  max-width: 320px;
  padding: 14px 18px;
  background: #fff;
  border: 1px solid var(--line, #e3d8c4);
  border-left: 4px solid var(--leaf, #3b8a57); /* 左緣色條：預設成功綠 */
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(30, 25, 15, 0.22);
}
.toast--error {
  border-left-color: #c0392b;
}
.toast__ico {
  font-size: 18px;
  flex-shrink: 0;
}
.toast__msg {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: var(--ink, #2c3a2e);
}
.toast--error .toast__msg {
  color: #c0392b;
}

/* 從右側滑入 + 淡入的過場 */
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(24px);
}
</style>
