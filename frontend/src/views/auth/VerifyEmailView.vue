<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import authApi from '@/api/auth'

const route = useRoute()

// 'loading' | 'success' | 'error'
const status = ref('loading')
const message = ref('')

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    status.value = 'error'
    message.value = '連結無效或缺少 token'
    return
  }
  try {
    const res = await authApi.verifyEmail(token)
    status.value = 'success'
    message.value = typeof res === 'string' ? res : 'Email 驗證成功！'
  } catch (e) {
    status.value = 'error'
    message.value = e.message || '驗證失敗，連結可能已過期'
  }
})
</script>

<template>
  <main class="auth-page">
    <div class="auth-card" style="text-align:center">
      <h1>Email 驗證</h1>

      <p v-if="status === 'loading'" class="verify-msg">驗證中，請稍候…</p>

      <template v-else-if="status === 'success'">
        <div class="verify-icon verify-ok">✓</div>
        <p class="verify-msg">{{ message }}</p>
        <p class="auth-foot">
          <router-link to="/login">前往會員登入</router-link>
          ｜
          <router-link to="/farmer/login">小農登入</router-link>
        </p>
      </template>

      <template v-else>
        <div class="verify-icon verify-err">✕</div>
        <p class="verify-msg verify-error">{{ message }}</p>
        <p class="auth-foot">
          連結可能已過期，
          <router-link to="/resend-verification">重寄驗證信</router-link>
        </p>
        <p class="auth-foot">
          <router-link to="/login">回會員登入</router-link>
          ｜
          <router-link to="/farmer/login">小農登入</router-link>
        </p>
      </template>
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
  margin: 0 0 16px;
  font-size: 24px;
  color: var(--ink);
}
.verify-icon {
  width: 56px;
  height: 56px;
  margin: 8px auto 16px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 30px;
  color: #fff;
}
.verify-ok {
  background: var(--leaf);
}
.verify-err {
  background: #c0392b;
}
.verify-msg {
  margin: 0;
  color: var(--ink-soft);
  font-size: 15px;
}
.verify-error {
  color: #c0392b;
}
.auth-foot {
  margin: 20px 0 0;
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
