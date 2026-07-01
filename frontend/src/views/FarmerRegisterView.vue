<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表單欄位
const farmName = ref('')   // 農場名稱
const owner = ref('')      // 負責人姓名
const email = ref('')
const phone = ref('')
const password = ref('')
const confirm = ref('')

const error = ref('')
const done = ref(false)

// 小農註冊（UI 階段，先做前端驗證）
// 注意：小農系統與一般會員系統「分開」，
// 之後要打的是小農專屬端點，例如 /api/farmer/register，不是 /api/register。
function handleRegister() {
  error.value = ''

  if (!farmName.value || !owner.value || !email.value || !password.value) {
    error.value = '請完整填寫農場名稱、負責人、信箱與密碼'
    return
  }
  if (!email.value.includes('@')) {
    error.value = '信箱格式不正確'
    return
  }
  if (password.value.length < 6) {
    error.value = '密碼至少需 6 個字元'
    return
  }
  if (password.value !== confirm.value) {
    error.value = '兩次輸入的密碼不一致'
    return
  }

  // TODO：之後改成 fetch('/api/farmer/register', ...) 呼叫 Spring Boot
  done.value = true
  // 模擬註冊成功後導到小農登入頁
  setTimeout(() => router.push('/farmer/login'), 1000)
}
</script>

<template>
  <main class="auth-page">
    <div class="auth-card auth-card--farmer">
      <span class="auth-badge">🌾 小農專區</span>
      <h1>申請小農帳號</h1>
      <p class="auth-sub">成為 Farmily 的小農夥伴，開始販售你的好物</p>

      <form class="auth-form" @submit.prevent="handleRegister">
        <label>
          <span>農場名稱</span>
          <input v-model="farmName" type="text" placeholder="例如：陽光有機農場" />
        </label>

        <label>
          <span>負責人姓名</span>
          <input v-model="owner" type="text" placeholder="你的名字" />
        </label>

        <label>
          <span>電子信箱</span>
          <input v-model="email" type="email" placeholder="farmer@example.com" />
        </label>

        <label>
          <span>聯絡電話</span>
          <input v-model="phone" type="tel" placeholder="0912-345-678" />
        </label>

        <label>
          <span>密碼</span>
          <input v-model="password" type="password" placeholder="至少 6 個字元" />
        </label>

        <label>
          <span>確認密碼</span>
          <input v-model="confirm" type="password" placeholder="再輸入一次密碼" />
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">申請成功！正在帶你前往登入…</p>

        <button class="auth-btn" type="submit">送出申請</button>
      </form>

      <p class="auth-foot">
        已經是小農夥伴了？
        <router-link to="/farmer/login">前往登入</router-link>
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
