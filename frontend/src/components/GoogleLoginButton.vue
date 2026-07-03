<script setup>
import { onMounted, ref } from 'vue'

// ⚠️ 必須與後端 GoogleTokenVerifier 的 GOOGLE_CLIENT_ID 一致，
// 否則後端驗證 aud 會失敗（token 不是給本應用）。
const CLIENT_ID = '528090060943-mooi8topfu9g29umm7f67579vvsk553e.apps.googleusercontent.com'
const GSI_SRC = 'https://accounts.google.com/gsi/client'

// 拿到 Google id_token 就往外 emit，由父層決定打哪個後端端點
const emit = defineEmits(['credential', 'error'])
const btnEl = ref(null)

// 載入 Google Identity Services SDK（只載一次）
function loadScript() {
  return new Promise((resolve, reject) => {
    if (window.google?.accounts?.id) return resolve()
    const existing = document.querySelector(`script[src="${GSI_SRC}"]`)
    if (existing) {
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', () => reject(new Error('無法載入 Google 登入元件')))
      return
    }
    const s = document.createElement('script')
    s.src = GSI_SRC
    s.async = true
    s.defer = true
    s.onload = () => resolve()
    s.onerror = () => reject(new Error('無法載入 Google 登入元件'))
    document.head.appendChild(s)
  })
}

onMounted(async () => {
  try {
    await loadScript()
    window.google.accounts.id.initialize({
      client_id: CLIENT_ID,
      callback: (response) => {
        if (response?.credential) {
          emit('credential', response.credential) // 這就是後端要的 idToken
        } else {
          emit('error', new Error('未取得 Google 憑證'))
        }
      },
    })
    window.google.accounts.id.renderButton(btnEl.value, {
      theme: 'outline',
      size: 'large',
      width: 316,
      text: 'signin_with',
      shape: 'rectangular',
      locale: 'zh_TW',
    })
  } catch (e) {
    emit('error', e)
  }
})
</script>

<template>
  <!-- Google 會把官方按鈕渲染進這個容器 -->
  <div ref="btnEl" class="gsi-btn"></div>
</template>

<style scoped>
.gsi-btn {
  display: flex;
  justify-content: center;
}
</style>
