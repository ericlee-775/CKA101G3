<script>
// 關掉屬性自動繼承：外部傳進來的 placeholder / autocomplete / id 等，
// 由我們自己 v-bind 到真正的 <input>，而不是套在最外層的 <div> 上。
export default { inheritAttrs: false }
</script>

<script setup>
import { ref } from 'vue'

// v-model 綁定密碼字串
defineProps({
  modelValue: { type: String, default: '' },
})
defineEmits(['update:modelValue'])

// 是否明文顯示密碼
const visible = ref(false)
</script>

<template>
  <div class="pw-field">
    <!-- 依 visible 切換 type：text=明文、password=遮罩 -->
    <input
      class="pw-input"
      :type="visible ? 'text' : 'password'"
      :value="modelValue"
      v-bind="$attrs"
      @input="$emit('update:modelValue', $event.target.value)"
    />

    <!-- 眼睛圖示：type=button 避免在表單內誤觸送出 -->
    <button
      type="button"
      class="pw-toggle"
      :aria-label="visible ? '隱藏密碼' : '顯示密碼'"
      :title="visible ? '隱藏密碼' : '顯示密碼'"
      @click="visible = !visible"
    >
      <!-- 目前為明文 → 顯示「眼睛畫斜線」代表可切回隱藏 -->
      <svg v-if="visible" viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
        <line x1="1" y1="1" x2="23" y2="23"/>
      </svg>
      <!-- 目前為遮罩 → 顯示「眼睛」代表點了會看到密碼 -->
      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor"
           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
        <circle cx="12" cy="12" r="3"/>
      </svg>
    </button>
  </div>
</template>

<style scoped>
.pw-field {
  position: relative;
  display: flex;
  align-items: center;
}
/* 對齊全站表單 input 的外觀（padding / border / 圓角 / focus 綠框），
   右邊多留空間放眼睛圖示 */
.pw-input {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 44px 11px 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.18s ease;
}
.pw-input:focus {
  border-color: var(--leaf);
}
/* 關掉 Edge/IE 對 type=password 自動加的原生眼睛/清除鈕，避免和自訂按鈕重複 */
.pw-input::-ms-reveal,
.pw-input::-ms-clear {
  display: none;
}
/* 眼睛切換鈕：絕對定位貼在輸入框右側 */
.pw-toggle {
  position: absolute;
  right: 6px;
  top: 50%;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  transition: color 0.18s ease, background 0.18s ease;
}
.pw-toggle:hover {
  color: var(--leaf-dark);
  background: var(--leaf-soft);
}
.pw-toggle svg {
  width: 20px;
  height: 20px;
  display: block;
}
</style>