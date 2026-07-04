<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import memberApi from '@/api/member'
import authStore from '@/stores/auth'
import GoogleLoginButton from '@/components/GoogleLoginButton.vue'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()

// 表單欄位（雙向綁定 v-model 用）
const email = ref('')
const password = ref('')
const rememberMe = ref(false)

// 錯誤訊息 & 成功提示 & 送出中狀態
const error = ref('')
const done = ref(false)
const loading = ref(false)

// 送出登入：呼叫後端 POST /api/member/login
async function handleLogin() {
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

  loading.value = true
  try {
    const user = await memberApi.login({
      email: email.value,
      password: password.value,
      rememberMe: rememberMe.value,
    })
    authStore.setUser(user, 'MEMBER')   // 記住登入者身分
    done.value = true
    setTimeout(() => router.push('/'), 800)
  } catch (e) {
    error.value = e.message || '登入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}

// Google 登入：拿到 id_token 後 POST /api/member/oauth/google
async function handleGoogle(credential) {
  error.value = ''
  loading.value = true
  try {
    const user = await memberApi.googleLogin(credential)
    authStore.setUser(user, 'MEMBER')
    done.value = true
    setTimeout(() => router.push('/'), 800)
  } catch (e) {
    error.value = e.message || 'Google 登入失敗'
  } finally {
    loading.value = false
  }
}

function handleGoogleError(e) {
  error.value = e.message || 'Google 登入元件載入失敗'
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
          <PasswordInput v-model="password" placeholder="請輸入密碼" autocomplete="current-password" />
        </label>

        <!-- 記住我 + 忘記密碼 -->
        <div class="auth-row">
          <label class="auth-check">
            <input v-model="rememberMe" type="checkbox" />
            <span>記住我</span>
          </label>
          <router-link class="auth-link" to="/forgot-password">忘記密碼？</router-link>
        </div>

        <!-- 只有有錯誤時才顯示（v-if） -->
        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">登入成功！正在帶你回首頁…</p>

        <button class="auth-btn" type="submit" :disabled="loading">
          {{ loading ? '登入中…' : '登入' }}
        </button>
      </form>

      <!-- 分隔線 -->
      <div class="auth-divider"><span>或</span></div>

      <!-- Google 登入（拿到 id_token 後打 /api/member/oauth/google）-->
      <GoogleLoginButton @credential="handleGoogle" @error="handleGoogleError" />

      <p class="auth-foot">
        還沒有帳號？
        <router-link to="/register">前往註冊</router-link>
      </p>
      <p class="auth-foot">
        沒收到驗證信？
        <router-link to="/resend-verification">重寄驗證信</router-link>
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

/* 記住我 / 忘記密碼 那一列 */
.auth-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  margin-top: -4px;
}
.auth-check {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--ink-soft);
  cursor: pointer;
}
.auth-link {
  color: var(--leaf);
  text-decoration: none;
}
.auth-link:hover {
  text-decoration: underline;
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
.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 「或」分隔線 */
.auth-divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 20px 0 16px;
  color: var(--muted);
  font-size: 13px;
}
.auth-divider::before,
.auth-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--line);
}
.auth-divider span {
  padding: 0 12px;
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
