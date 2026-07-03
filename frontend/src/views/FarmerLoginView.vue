<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import farmerApi from '@/api/farmer'
import authStore from '@/stores/auth'

const router = useRouter()

// 表單欄位
const email = ref('')
const password = ref('')
const rememberMe = ref(false)

const error = ref('')
const done = ref(false)
const loading = ref(false)

// 小農登入：呼叫後端 POST /api/farmer/login（與一般會員系統分開）
async function handleLogin() {
  error.value = ''

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
    const farmer = await farmerApi.login({
      email: email.value,
      password: password.value,
      rememberMe: rememberMe.value,
    })
    authStore.setUser(farmer, 'FARMER')
    done.value = true
    // 登入成功導向小農個人中心
    setTimeout(() => router.push('/farmer/me'), 800)
  } catch (e) {
    error.value = e.message || '登入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card auth-card--farmer">
      <span class="auth-badge">🌾 小農專區</span>
      <h1>小農登入</h1>
      <p class="auth-sub">登入你的小農帳號，管理你的商品</p>

      <form class="auth-form" @submit.prevent="handleLogin">
        <label>
          <span>電子信箱</span>
          <input v-model="email" type="email" placeholder="farmer@example.com" />
        </label>

        <label>
          <span>密碼</span>
          <input v-model="password" type="password" placeholder="請輸入密碼" />
        </label>

        <!-- 記住我 + 忘記密碼（帶 type=FARMER）-->
        <div class="auth-row">
          <label class="auth-check">
            <input v-model="rememberMe" type="checkbox" />
            <span>記住我</span>
          </label>
          <router-link class="auth-link" to="/forgot-password?type=FARMER">忘記密碼？</router-link>
        </div>

        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">登入成功！正在帶你前往…</p>

        <button class="auth-btn" type="submit" :disabled="loading">
          {{ loading ? '登入中…' : '登入' }}
        </button>
      </form>

      <p class="auth-foot">
        還不是小農夥伴？
        <router-link to="/farmer/register">申請小農帳號</router-link>
      </p>
      <!-- 尚未通過初審 / 未完成 Email 驗證而無法登入者，走免登入的申請進度入口 -->
      <p class="auth-foot">
        無法登入？
        <router-link to="/farmer/application">查詢申請進度 / 重寄啟用信</router-link>
      </p>
      <p class="auth-note">這是小農帳號入口，與一般會員帳號分開。</p>
    </div>
  </main>
</template>

<style scoped>
.auth-page {
  display: grid;
  place-items: center;
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
/* 小農版用綠色上邊框，跟會員登入區隔 */
.auth-card--farmer {
  border-top: 4px solid var(--leaf);
}
.auth-badge {
  display: inline-block;
  margin-bottom: 10px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 12px;
  font-weight: 600;
}
.auth-card h1 {
  margin: 0;
  font-size: 24px;
  color: var(--ink);
}
.auth-sub {
  margin: 6px 0 24px;
  color: var(--muted);
  font-size: 14px;
}

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
  border-color: var(--leaf);
}

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
.auth-note {
  margin: 10px 0 0;
  text-align: center;
  font-size: 12px;
  color: var(--muted);
}
</style>
