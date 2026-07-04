<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import authApi, { ACCOUNT_TYPE } from '@/api/auth'

const route = useRoute()

const email = ref('')
// 身分：預設從網址 ?type=FARMER 帶入，否則會員
const accountType = ref(route.query.type === 'FARMER' ? ACCOUNT_TYPE.FARMER : ACCOUNT_TYPE.MEMBER)

const error = ref('')
const done = ref(false)
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  if (!email.value || !email.value.includes('@')) {
    error.value = '請輸入正確的信箱'
    return
  }
  loading.value = true
  try {
    await authApi.forgotPassword(email.value, accountType.value)
    // 後端不論帳號是否存在都回相同訊息，避免洩漏帳號
    done.value = true
  } catch (e) {
    error.value = e.message || '寄送失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>忘記密碼</h1>
      <p class="auth-sub">輸入註冊信箱，我們會寄送重設密碼連結給你</p>

      <form v-if="!done" class="auth-form" @submit.prevent="handleSubmit">
        <label>
          <span>身分</span>
          <select v-model="accountType">
            <option value="MEMBER">一般會員</option>
            <option value="FARMER">小農</option>
          </select>
        </label>

        <label>
          <span>電子信箱</span>
          <input v-model="email" type="email" placeholder="you@example.com" />
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>

        <button class="auth-btn" type="submit" :disabled="loading">
          {{ loading ? '寄送中…' : '寄送重設連結' }}
        </button>
      </form>

      <div v-else class="auth-done">
        <p class="auth-ok">若此信箱有註冊帳號，系統已寄出重設密碼信，請至信箱確認。</p>
      </div>

      <p class="auth-foot">
        想起來了？
        <router-link to="/login">回會員登入</router-link>
        ｜
        <router-link to="/farmer/login">小農登入</router-link>
      </p>
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
.auth-form input,
.auth-form select {
  padding: 11px 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.18s ease;
  font-family: inherit;
}
.auth-form input:focus,
.auth-form select:focus {
  border-color: var(--leaf);
}
.auth-error {
  margin: 0;
  color: #c0392b;
  font-size: 13px;
}
.auth-ok {
  margin: 0;
  color: var(--leaf);
  font-size: 14px;
  text-align: center;
  line-height: 1.6;
}
.auth-done {
  padding: 12px 0;
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
</style>
