<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表單欄位（雙向綁定 v-model 用）
const email = ref('')
const password = ref('')

// 錯誤訊息 & 成功提示
const error = ref('')
const done = ref(false)

// 送出登入（目前是 UI 階段，先做前端驗證，還沒串後端）
function handleLogin() {
  error.value = ''

  // 基本驗證
  if (!email.value || !password.value) {
    error.value = '請輸入信箱與密碼'
    return
  }
  if (!email.value.includes('@')) {
    error.value = '信箱格式不正確'
    return
  }

  // TODO：之後這裡改成 fetch('/api/login', ...) 呼叫 Spring Boot
  done.value = true
  // 模擬登入成功後導回首頁
  setTimeout(() => router.push('/'), 1000)
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>會員登入</h1>
      <p class="auth-sub">歡迎回到 Farmily 🌱</p>

      <!-- @submit.prevent：攔截表單預設送出（避免整頁重整），改呼叫 handleLogin -->
      <form class="auth-form" @submit.prevent="handleLogin">
        <label>
          <span>電子信箱</span>
          <!-- v-model：把輸入框的值雙向綁到 email -->
          <input v-model="email" type="email" placeholder="you@example.com" />
        </label>

        <label>
          <span>密碼</span>
          <input v-model="password" type="password" placeholder="請輸入密碼" />
        </label>

        <!-- 只有有錯誤時才顯示（v-if） -->
        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">登入成功！正在帶你回首頁…</p>

        <button class="auth-btn" type="submit">登入</button>
      </form>

      <p class="auth-foot">
        還沒有帳號？
        <router-link to="/register">前往註冊</router-link>
      </p>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  display: grid;
  place-items: center;              /* 卡片置中 */
  padding: 48px 18px;
  min-height: 60vh;
}
.auth-card {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 32px;
}
.auth-card h1 {
  margin: 0;
  font-size: 24px;
  color: var(--ink);
  text-align: center;
}
.auth-sub {
  margin: 6px 0 24px;
  text-align: center;
  color: var(--muted);
  font-size: 14px;
}

/* 表單 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.auth-form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: var(--ink-soft);
}
.auth-form input {
  padding: 11px 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.18s ease;
}
.auth-form input:focus {
  border-color: var(--leaf);        /* 點選時邊框變綠 */
}

/* 提示訊息 */
.auth-error {
  margin: 0;
  color: #c0392b;
  font-size: 13px;
}
.auth-ok {
  margin: 0;
  color: var(--leaf);
  font-size: 13px;
}

/* 送出按鈕 */
.auth-btn {
  margin-top: 4px;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: var(--leaf);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.auth-btn:hover {
  background: var(--leaf-dark);
}

/* 底部切換連結 */
.auth-foot {
  margin: 20px 0 0;
  text-align: center;
  font-size: 14px;
  color: var(--muted);
}
.auth-foot a {
  color: var(--leaf);
  font-weight: 600;
  text-decoration: none;
}
.auth-foot a:hover {
  text-decoration: underline;
}
</style>
