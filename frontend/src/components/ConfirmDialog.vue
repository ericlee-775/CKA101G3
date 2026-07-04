<script setup>
import { watch, onUnmounted } from 'vue'
import { useConfirmDialog } from '@/composables/useConfirm'

// 讀全域彈窗狀態 + 回應方法
const { state, respond } = useConfirmDialog()

// 鍵盤：Esc 取消、Enter 確定
function onKeydown(e) {
  if (!state.open) return
  if (e.key === 'Escape') respond(false)
  else if (e.key === 'Enter') respond(true)
}

// 彈窗開著時才掛鍵盤監聽，關閉就移除
watch(
  () => state.open,
  (open) => {
    if (open) window.addEventListener('keydown', onKeydown)
    else window.removeEventListener('keydown', onKeydown)
  }
)
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <!-- Teleport 到 body：避免被 header 的 sticky / z-index 蓋住 -->
  <Teleport to="body">
    <Transition name="cd-fade">
      <div v-if="state.open" class="cd-overlay" @click.self="respond(false)">
        <div class="cd-card" role="dialog" aria-modal="true">
          <h3 class="cd-title">{{ state.title }}</h3>
          <p v-if="state.message" class="cd-message">{{ state.message }}</p>
          <div class="cd-actions">
            <button type="button" class="cd-btn cd-cancel" @click="respond(false)">
              {{ state.cancelText }}
            </button>
            <button
              type="button"
              class="cd-btn"
              :class="state.danger ? 'cd-danger' : 'cd-confirm'"
              @click="respond(true)"
            >
              {{ state.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.cd-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(30, 30, 25, 0.45);
}
.cd-card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 48px rgba(30, 25, 15, 0.28);
  padding: 24px;
  text-align: center;
}
.cd-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--ink, #2c3a2e);
}
.cd-message {
  margin: 0 0 20px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--muted, #7d8a78);
}
.cd-actions {
  display: flex;
  gap: 10px;
}
.cd-btn {
  flex: 1;
  padding: 11px 16px;
  border-radius: 10px;
  font-size: 15px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.cd-cancel {
  background: #fff;
  border-color: var(--line, #e3d8c4);
  color: var(--ink-soft, #4a564c);
}
.cd-cancel:hover {
  border-color: var(--muted, #7d8a78);
}
.cd-confirm {
  background: var(--leaf, #3b8a57);
  color: #fff;
}
.cd-confirm:hover {
  background: var(--leaf-dark, #2f6e46);
}
.cd-danger {
  background: #c0392b;
  color: #fff;
}
.cd-danger:hover {
  background: #a5321f;
}

/* 淡入 + 卡片微縮放的過場 */
.cd-fade-enter-active,
.cd-fade-leave-active {
  transition: opacity 0.18s ease;
}
.cd-fade-enter-from,
.cd-fade-leave-to {
  opacity: 0;
}
.cd-fade-enter-active .cd-card,
.cd-fade-leave-active .cd-card {
  transition: transform 0.18s ease;
}
.cd-fade-enter-from .cd-card,
.cd-fade-leave-to .cd-card {
  transform: scale(0.94);
}
</style>
