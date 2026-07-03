<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import memberApi from '@/api/member'

const router = useRouter()

// 表單欄位
const name = ref('')
const email = ref('')
const password = ref('')
const confirm = ref('')

const error = ref('')
const done = ref(false)
const loading = ref(false)

// 送出註冊：呼叫後端 POST /api/member/register
async function handleRegister() {
  error.value = ''

  if (!name.value || !email.value || !password.value) {
    error.value = '請完整填寫姓名、信箱與密碼'
    return
  }
  if (!email.value.includes('@')) {
    error.value = '信箱格式不正確'
    return
  }
  // 後端 @Size(min = 8)：密碼至少 8 碼
  if (password.value.length < 8) {
    error.value = '密碼至少需 8 個字元'
    return
  }
  // 確認兩次密碼一致
  if (password.value !== confirm.value) {
    error.value = '兩次輸入的密碼不一致'
    return
  }

  loading.value = true
  try {
    // 前端欄位 name 對應後端 userName
    await memberApi.register({
      email: email.value,
      password: password.value,
      userName: name.value,
    })
    done.value = true
    // 註冊成功後導到登入頁
    setTimeout(() => router.push('/login'), 1200)
  } catch (e) {
    error.value = e.message || '註冊失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>會員註冊</h1>
      <p class="auth-sub">加入 Farmily，享受產地直送 🌱</p>

      <form class="auth-form" @submit.prevent="handleRegister">
        <label>
          <span>姓名</span>
          <input v-model="name" type="text" placeholder="你的名字" />
        </label>

        <label>
          <span>電子信箱</span>
          <input v-model="email" type="email" placeholder="you@example.com" />
        </label>

        <label>
          <span>密碼</span>
          <input v-model="password" type="password" placeholder="至少 8 個字元" />
        </label>

        <label>
          <span>確認密碼</span>
          <input v-model="confirm" type="password" placeholder="再輸入一次密碼" />
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">註冊成功！正在帶你前往登入…</p>

        <button class="auth-btn" type="submit" :disabled="loading">
          {{ loading ? '註冊中…' : '註冊' }}
        </button>
      </form>

      <p class="auth-foot">
        已經有帳號了？
        <router-link to="/login">前往登入</router-link>
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
</style>
