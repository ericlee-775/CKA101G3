<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import authApi from '@/api/auth'
import PasswordInput from '@/components/PasswordInput.vue'

const route = useRoute()
const router = useRouter()

const token = ref('')
const newPassword = ref('')
const confirm = ref('')

const error = ref('')
const done = ref(false)
const loading = ref(false)
const noToken = ref(false)

onMounted(() => {
  token.value = route.query.token || ''
  if (!token.value) noToken.value = true
})

async function handleSubmit() {
  error.value = ''
  if (!newPassword.value) {
    error.value = '請輸入新密碼'
    return
  }
  // 後端 @Size(8~60) + @Pattern：長度 8~60 且需同時含英文與數字
  if (newPassword.value.length < 8 || newPassword.value.length > 60) {
    error.value = '密碼長度需 8~60 字'
    return
  }
  if (!/[A-Za-z]/.test(newPassword.value) || !/\d/.test(newPassword.value)) {
    error.value = '密碼需同時包含英文字母與數字'
    return
  }
  if (newPassword.value !== confirm.value) {
    error.value = '兩次輸入的密碼不一致'
    return
  }
  loading.value = true
  try {
    await authApi.resetPassword(token.value, newPassword.value)
    done.value = true
    setTimeout(() => router.push('/login'), 1500)
  } catch (e) {
    error.value = e.message || '重設失敗，連結可能已過期，請重新申請'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card">
      <h1>重設密碼</h1>

      <div v-if="noToken" class="auth-done">
        <p class="auth-error" style="text-align:center">
          連結無效或缺少 token，請重新從「忘記密碼」申請。
        </p>
        <p class="auth-foot">
          <router-link to="/forgot-password">前往忘記密碼</router-link>
        </p>
      </div>

      <template v-else>
        <p class="auth-sub">請設定你的新密碼</p>

        <form v-if="!done" class="auth-form" @submit.prevent="handleSubmit">
          <label>
            <span>新密碼</span>
            <PasswordInput v-model="newPassword" placeholder="至少 8 個字元" autocomplete="new-password" />
          </label>
          <label>
            <span>確認新密碼</span>
            <PasswordInput v-model="confirm" placeholder="再輸入一次新密碼" autocomplete="new-password" />
          </label>

          <p v-if="error" class="auth-error">{{ error }}</p>

          <button class="auth-btn" type="submit" :disabled="loading">
            {{ loading ? '處理中…' : '重設密碼' }}
          </button>
        </form>

        <div v-else class="auth-done">
          <p class="auth-ok">密碼重設成功！正在帶你前往登入…</p>
        </div>
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
  font-size: 14px;
  text-align: center;
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
