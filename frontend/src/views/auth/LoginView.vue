<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import memberApi from '@/api/member'
import authStore from '@/stores/auth'
import GoogleLoginButton from '@/components/GoogleLoginButton.vue'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const route = useRoute()
// 被導航守衛擋下來時會帶 ?redirect=原本要去的頁;登入成功後接回,沒有就回首頁
const afterLogin = () => router.push(route.query.redirect || '/')

// 表單欄位
const email = ref('')
const password = ref('')
const remember = ref(false)

const error = ref('')
const done = ref(false)
const loading = ref(false)
// 只有登入失敗且原因是「帳號未驗證」時,才顯示重寄驗證信入口
const showResend = ref(false)

// ===== 植物生長階段 =====
// 0 種子 / 1 信箱填好發芽 / 2 密碼填好結綠果 / 3 登入成功收成
const stage = computed(() => {
  if (done.value) return 3
  const emailOk = email.value.includes('@') && email.value.length > 3
  const pwdOk = password.value.length > 0
  if (emailOk && pwdOk) return 2
  if (emailOk || pwdOk) return 1
  return 0
})

const HINTS = {
  0: '填上信箱,種子就會發芽 🌱',
  1: '發芽了!再輸入密碼讓它長大 🌿',
  2: '結出小果子了,按「登入」讓它成熟 🍅',
  3: '收成!歡迎回家 🧺',
}
const hint = computed(() => HINTS[stage.value])

const plantClass = computed(() => ({
  p1: stage.value >= 1,
  p2: stage.value >= 2,
  p3: stage.value >= 3,
}))

// 送出登入:呼叫後端 POST /api/member/login
async function handleLogin() {
  error.value = ''
  showResend.value = false

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
      rememberMe: remember.value,
    })
    authStore.setUser(user, 'MEMBER')   // 記住登入者身分,header 與受保護頁面才認得
    done.value = true
    // 留一點時間讓小烏龜回頭打招呼,再進站
    setTimeout(afterLogin, 2000)
  } catch (e) {
    error.value = e.message || '登入失敗，請確認帳號密碼'
    // 後端回 EMAIL_NOT_VERIFIED（HTTP 403）才亮出重寄入口
    showResend.value = e.code === 'EMAIL_NOT_VERIFIED'
  } finally {
    loading.value = false
  }
}

// Google 登入:拿到 id_token 後 POST /api/member/oauth/google
async function handleGoogle(credential) {
  error.value = ''
  loading.value = true
  try {
    const user = await memberApi.googleLogin(credential)
    authStore.setUser(user, 'MEMBER')
    done.value = true
    setTimeout(afterLogin, 2000)
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
  <main class="auth-page" :class="{ harvest: done }">
    <!-- ===== 清晨農田場景 ===== -->
    <div class="scene" aria-hidden="true">
      <div class="sun"></div>
      <div class="cloud c1"></div>
      <div class="cloud c2"></div>
      <div class="cloud c3"></div>
      <div class="hill far"></div>
      <div class="hill mid"></div>
      <div class="hill near"></div>

      <!-- 田埂上散步的小烏龜:登入成功會停下來回頭 -->
      <div class="turtle">
        <div class="t-bubble">歡迎回來!</div>
        <div class="t-shell"></div>
        <div class="t-head"></div>
        <div class="t-leg front"></div>
        <div class="t-leg back"></div>
        <div class="t-tail"></div>
      </div>

      <span class="mote"></span><span class="mote"></span><span class="mote"></span>
      <span class="mote"></span><span class="mote"></span><span class="mote"></span>
    </div>

    <div class="layout">
      <!-- 左:品牌與會長大的植物 -->
      <section class="grove">
        <div class="brand">
          <div class="logo"><span class="sprout">🌱</span> Farmily <em>小農市集</em></div>
          <p>天還沒亮，菜就已經在路上。直接向在地小農買菜 —— 新鮮、當季、可溯源。</p>
        </div>

        <div class="plot">
          <div class="plant" :class="plantClass">
            <span class="confetti x1">🍃</span>
            <span class="confetti x2">🌿</span>
            <span class="confetti x3">🍃</span>
            <div class="fruit"></div>
            <div class="leaf left a s1"></div>
            <div class="leaf right b s1"></div>
            <div class="leaf left c s2"></div>
            <div class="leaf right d s2"></div>
            <div class="stem"></div>
            <div class="seed"></div>
            <div class="mound"></div>
          </div>
      
        </div>
      </section>

      <!-- 右:登入表單 -->
      <section class="card">
        <h1>會員登入</h1>
        <p class="sub">歡迎回到 Farmily，繼續你的採買</p>

        <form class="auth-form" @submit.prevent="handleLogin">
          <label class="fg">
            <span class="lbl">電子信箱</span>
            <input v-model="email" type="email" placeholder="you@example.com" autocomplete="email" />
          </label>

          <label class="fg">
            <span class="lbl">密碼</span>
            <PasswordInput v-model="password" placeholder="請輸入密碼" autocomplete="current-password" />
          </label>

          <div class="row">
            <label class="check">
              <input v-model="remember" type="checkbox" /> <span>記住我</span>
            </label>
            <router-link class="link" to="/forgot-password">忘記密碼?</router-link>
          </div>

          <p v-if="error" class="auth-error">{{ error }}</p>
          <p v-if="showResend" class="auth-resend">
            帳號尚未完成 Email 驗證，
            <router-link :to="{ name: 'resend-verification', query: { email } }">重寄驗證信</router-link>
          </p>
          <p v-if="done" class="auth-ok">登入成功!正在帶你進入市集…</p>

          <button class="btn" :class="{ done }" type="submit" :disabled="loading || done">
            {{ done ? '歡迎回來 🧺' : loading ? '登入中…' : '登入' }}
          </button>
        </form>

        <!-- 分隔線 -->
        <div class="divider"><span>或</span></div>

        <!-- Google 登入(拿到 id_token 後打 /api/member/oauth/google)-->
        <GoogleLoginButton @credential="handleGoogle" @error="handleGoogleError" />

        <p class="foot">
          還沒有帳號?
          <router-link to="/register">前往註冊</router-link>
        </p>
<!--        <p class="foot">-->
<!--          沒收到驗證信?-->
<!--          <router-link to="/resend-verification">重寄驗證信</router-link>-->
<!--        </p>-->
      </section>
    </div>
  </main>
</template>

<style scoped>
/* ============ 主題色 ============ */
.auth-page {
  --dawn-hi: #fff3d6;
  --dawn-mid: #ffe0ae;
  --dawn-sky: #cde6e3;
  --hill-far: #b5d49a;
  --hill-mid: #84b46f;
  --hill-near: #4f8a4d;
  --soil: #7a5439;
  --tomato: #d95a45;
  --tomato-dark: #c04b38;
  --leaf-g: #3f7a44;
  --leaf-soft: #e7f1e2;
  --ink: #33402e;
  --ink-soft: #5b6653;
  --muted: #8f9784;
  --line: #e3ded0;

  position: relative;
  /* 這頁掛在 ShopLayout 底下(上方有 sticky header,約 84px),
     扣掉 header 才不會超出一個螢幕、也不會在上方留過多空白 */
  min-height: calc(100dvh - 84px);
  overflow: hidden;
  font-family: 'Noto Sans TC', system-ui, sans-serif;
  color: var(--ink);
  background: linear-gradient(
    180deg,
    var(--dawn-sky) 0%,
    var(--dawn-mid) 46%,
    var(--dawn-hi) 62%,
    var(--hill-far) 62.1%
  );
}

/* ============ 清晨場景 ============ */
.scene {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.sun {
  position: absolute;
  left: 11%;
  top: 16%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, #fffbe9 0%, #ffe9ad 55%, rgba(255, 233, 173, 0) 72%);
  filter: drop-shadow(0 0 46px rgba(255, 224, 150, 0.9));
  animation: sunrise 2.4s cubic-bezier(0.25, 0.7, 0.3, 1) both;
  transition: filter 1.2s ease, transform 1.2s ease;
}
@keyframes sunrise {
  from { transform: translateY(150px) scale(0.85); opacity: 0; }
  to   { transform: translateY(0) scale(1); opacity: 1; }
}
.auth-page.harvest .sun {
  filter: drop-shadow(0 0 80px rgba(255, 224, 150, 1));
  transform: scale(1.12);
}

.cloud {
  position: absolute;
  height: 22px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  animation: drift linear infinite;
}
.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: inherit;
  border-radius: 50%;
}
.cloud::before { width: 34px; height: 34px; top: -16px; left: 18%; }
.cloud::after  { width: 24px; height: 24px; top: -10px; left: 52%; }
.cloud.c1 { width: 110px; top: 14%; left: -140px; animation-duration: 60s; }
.cloud.c2 { width: 80px;  top: 24%; left: -100px; animation-duration: 48s; animation-delay: -22s; opacity: 0.75; }
.cloud.c3 { width: 140px; top: 8%;  left: -170px; animation-duration: 75s; animation-delay: -40s; opacity: 0.6; }
@keyframes drift { to { transform: translateX(calc(100vw + 220px)); } }

/* 三層丘陵 */
.hill { position: absolute; border-radius: 50% 50% 0 0; }
.hill.far {
  left: -24%; right: 30%; bottom: 16vh; height: 34vh;
  background: linear-gradient(180deg, var(--hill-far), #a3c489);
}
.hill.mid {
  left: 34%; right: -28%; bottom: 8vh; height: 36vh;
  background: linear-gradient(180deg, var(--hill-mid), #6ba05b);
}
.hill.near {
  left: -12%; right: -12%; bottom: -14vh; height: 44vh;
  background: linear-gradient(180deg, #5c974f, var(--hill-near));
  overflow: hidden;
}
.hill.near::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    100deg,
    transparent 0 44px,
    rgba(255, 255, 255, 0.07) 44px 47px
  );
}

/* ============ 小烏龜 ============ */
.turtle {
  position: absolute;
  bottom: 12vh;
  left: -80px;
  width: 52px;
  height: 34px;
  animation: stroll 70s linear -18s infinite;
}
@keyframes stroll {
  to { transform: translateX(calc(100vw + 160px)); }
}

/* 登入成功:停下腳步,站到畫面中前方 */
.auth-page.harvest .turtle {
  animation: settle-in 0.6s ease both;
  left: 56%;
  transform: none;
}
@keyframes settle-in {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

.t-shell {
  position: absolute;
  bottom: 8px;
  left: 2px;
  width: 40px;
  height: 22px;
  border-radius: 100% 100% 12% 12% / 100% 100% 20% 20%;
  background:
    radial-gradient(circle at 30% 42%, rgba(255, 255, 255, 0.14) 4px, transparent 5px),
    radial-gradient(circle at 58% 34%, rgba(255, 255, 255, 0.14) 4px, transparent 5px),
    linear-gradient(180deg, #5d7a43, #46603a);
  box-shadow: inset 0 -4px 0 rgba(0, 0, 0, 0.14);
  animation: bob 1.4s ease-in-out infinite;
}
@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-1.5px); }
}

.t-head {
  position: absolute;
  bottom: 12px;
  right: 0;
  width: 13px;
  height: 11px;
  border-radius: 55% 60% 60% 45%;
  background: #8aa863;
  animation: peek 3.5s ease-in-out infinite;
  transition: transform 0.55s cubic-bezier(0.3, 1.3, 0.4, 1);
}
.t-head::before {
  content: '';
  position: absolute;
  top: 3px;
  right: 3px;
  width: 2px;
  height: 2px;
  border-radius: 50%;
  background: #2e3a26;
}
@keyframes peek {
  0%, 100% { transform: translateX(0); }
  50%      { transform: translateX(1.5px); }
}

/* ★ 回頭:頭移到殼的後方並鏡像,眼睛朝向後面(看你) */
.auth-page.harvest .t-head {
  animation: none;
  transform: translate(-34px, -3px) scaleX(-1);
}

.t-leg {
  position: absolute;
  bottom: 2px;
  width: 8px;
  height: 9px;
  border-radius: 4px;
  background: #7d9a5b;
  transform-origin: top center;
}
.t-leg.front { right: 9px; animation: step 1.4s ease-in-out infinite; }
.t-leg.back  { left: 7px;  animation: step 1.4s ease-in-out infinite 0.7s; }
@keyframes step {
  0%, 100% { transform: rotate(10deg); }
  50%      { transform: rotate(-10deg); }
}
.auth-page.harvest .t-leg { animation: none; transform: rotate(0); }

.t-tail {
  position: absolute;
  bottom: 9px;
  left: -2px;
  width: 7px;
  height: 5px;
  border-radius: 50% 0 0 50%;
  background: #7d9a5b;
}

/* 回頭時冒出的對話泡泡 */
.t-bubble {
  position: absolute;
  bottom: 40px;
  left: -14px;
  padding: 5px 11px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 13px;
  color: #33402e;
  white-space: nowrap;
  background: rgba(255, 253, 246, 0.95);
  border: 1px solid rgba(63, 122, 68, 0.25);
  border-radius: 12px 12px 12px 3px;
  opacity: 0;
  transform: scale(0.6);
  transform-origin: bottom left;
}
.auth-page.harvest .t-bubble {
  animation: bubble-pop 0.5s cubic-bezier(0.2, 1.4, 0.4, 1) 0.7s forwards;
}
@keyframes bubble-pop {
  to { opacity: 1; transform: scale(1); }
}

/* 晨光微粒 */
.mote {
  position: absolute;
  bottom: 24vh;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff2c0;
  opacity: 0;
  animation: rise 9s ease-in-out infinite;
}
.mote:nth-of-type(1) { left: 18%; animation-delay: 0s; }
.mote:nth-of-type(2) { left: 32%; animation-delay: 2.5s; width: 4px; height: 4px; }
.mote:nth-of-type(3) { left: 55%; animation-delay: 1.2s; }
.mote:nth-of-type(4) { left: 70%; animation-delay: 4s; width: 5px; height: 5px; }
.mote:nth-of-type(5) { left: 84%; animation-delay: 5.6s; width: 4px; height: 4px; }
.mote:nth-of-type(6) { left: 44%; animation-delay: 7s; }
@keyframes rise {
  0%   { transform: translateY(0); opacity: 0; }
  15%  { opacity: 0.85; }
  100% { transform: translateY(-42vh); opacity: 0; }
}

/* ============ 版面 ============ */
.layout {
  position: relative;
  z-index: 2;
  max-width: 1020px;
  margin: 0 auto;
  min-height: calc(100dvh - 84px);
  padding: 24px 32px 28px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 48px;
  align-items: center;
}

/* ---------- 左:品牌 + 植物 ---------- */
.grove {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 440px;
}
.brand .logo {
  display: inline-flex;
  align-items: baseline;
  gap: 10px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 34px;
  font-weight: 700;
  color: #2f4a2c;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
}
.brand .logo .sprout { font-size: 26px; }
.brand .logo em {
  font-style: normal;
  font-size: 15px;
  font-weight: 400;
  color: #48633f;
  letter-spacing: 2px;
}
.brand p {
  margin-top: 12px;
  max-width: 300px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 17px;
  line-height: 1.9;
  color: #3c5236;
}

.plot { margin-top: auto; text-align: center; width: min(240px, 100%); }

.plant { position: relative; width: 190px; height: 210px; margin: 0 auto; transform: scale(0.86); transform-origin: bottom center; }

.mound {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 130px;
  height: 34px;
  border-radius: 50%;
  background: radial-gradient(ellipse at 50% 30%, #8a6142, var(--soil));
  box-shadow: inset 0 -6px 10px rgba(0, 0, 0, 0.18);
}
.seed {
  position: absolute;
  bottom: 22px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  height: 12px;
  border-radius: 50% 50% 50% 0;
  background: #e8c874;
  rotate: 45deg;
  animation: seedpulse 1.6s ease-in-out infinite;
  transition: opacity 0.4s;
}
@keyframes seedpulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(232, 200, 116, 0.55); }
  50%      { box-shadow: 0 0 0 10px rgba(232, 200, 116, 0); }
}
.plant.p1 .seed { opacity: 0; }

.stem {
  position: absolute;
  bottom: 24px;
  left: 50%;
  width: 6px;
  height: 168px;
  border-radius: 6px;
  background: linear-gradient(180deg, #aed88e, #4e8a4d);
  transform: translateX(-50%) scaleY(0);
  transform-origin: bottom center;
  transition: transform 1.1s cubic-bezier(0.22, 0.9, 0.3, 1);
}
.plant.p1 .stem { transform: translateX(-50%) scaleY(0.42); }
.plant.p2 .stem { transform: translateX(-50%) scaleY(1); }

.leaf {
  position: absolute;
  width: 48px;
  height: 27px;
  background: linear-gradient(135deg, #94c973, #578f4b);
  transform: scale(0) rotate(var(--rot));
  transition: transform 0.55s cubic-bezier(0.2, 1.35, 0.4, 1) var(--d, 0.2s);
}
.leaf.left  { right: 50%; margin-right: 1px; border-radius: 100% 0 100% 0; transform-origin: 100% 100%; }
.leaf.right { left: 50%;  margin-left: 1px;  border-radius: 0 100% 0 100%; transform-origin: 0% 100%; }
.leaf.a { bottom: 78px;  --rot: 16deg;  --d: 0.25s; }
.leaf.b { bottom: 96px;  --rot: -16deg; --d: 0.45s; }
.leaf.c { bottom: 138px; --rot: 20deg;  --d: 0.25s; width: 42px; height: 24px; }
.leaf.d { bottom: 158px; --rot: -20deg; --d: 0.45s; width: 42px; height: 24px; }
.plant.p1 .leaf.s1 { transform: scale(1) rotate(var(--rot)); }
.plant.p2 .leaf.s2 { transform: scale(1) rotate(var(--rot)); }

.fruit {
  position: absolute;
  bottom: 182px;
  left: 50%;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: radial-gradient(circle at 32% 30%, #c3e29a, #7fb765);
  transform: translateX(-50%) scale(0);
  transition: transform 0.6s cubic-bezier(0.2, 1.4, 0.4, 1), background 0.9s ease, box-shadow 0.9s ease;
}
.fruit::before {
  content: '';
  position: absolute;
  top: -7px;
  left: 14px;
  width: 12px;
  height: 9px;
  background: #4e8a4d;
  border-radius: 50% 50% 40% 40%;
  transform: rotate(-14deg);
}
.plant.p2 .fruit { transform: translateX(-50%) scale(0.55); }
.plant.p3 .fruit {
  transform: translateX(-50%) scale(1);
  background: radial-gradient(circle at 32% 30%, #ff9a76, var(--tomato));
  box-shadow: 0 8px 22px rgba(192, 75, 56, 0.42);
}

.confetti { position: absolute; bottom: 200px; left: 50%; font-size: 15px; opacity: 0; }
.plant.p3 .confetti { animation: toss 1s ease-out forwards; }
.confetti.x1 { --tx: -46px; --ty: -54px; animation-delay: 0.1s !important; }
.confetti.x2 { --tx: 6px;   --ty: -70px; animation-delay: 0.2s !important; }
.confetti.x3 { --tx: 48px;  --ty: -48px; animation-delay: 0.3s !important; }
@keyframes toss {
  0%   { opacity: 0; transform: translate(0, 0) scale(0.4) rotate(0); }
  25%  { opacity: 1; }
  100% { opacity: 0; transform: translate(var(--tx), var(--ty)) scale(1) rotate(140deg); }
}

.hint {
  margin: 14px 0 0;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 15px;
  color: #3c5236;
  background: rgba(255, 253, 246, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 999px;
  padding: 7px 16px;
  display: inline-block;
  backdrop-filter: blur(4px);
}

/* ---------- 右:登入卡片 ---------- */
.card {
  background: #fffdf8;
  border: 1px solid var(--line);
  border-radius: 22px;
  padding: 26px 32px 28px;
  box-shadow: 0 24px 60px rgba(58, 80, 42, 0.18);
  animation: cardin 0.8s cubic-bezier(0.2, 0.8, 0.25, 1) 0.3s both;
}
@keyframes cardin {
  from { opacity: 0; transform: translateY(26px); }
  to   { opacity: 1; transform: translateY(0); }
}

.card h1 {
  margin: 0;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 27px;
  font-weight: 700;
}
.sub { margin: 6px 0 18px; color: var(--muted); font-size: 14px; }

.auth-form { display: flex; flex-direction: column; gap: 14px; }

.fg { display: flex; flex-direction: column; gap: 6px; }
.fg .lbl { font-size: 13px; color: var(--ink-soft); font-weight: 500; }

/* :deep() 讓樣式也套到 PasswordInput 內部的 input */
.fg input,
.fg :deep(input) {
  width: 100%;
  box-sizing: border-box;         /* 和密碼框一致,右邊才對得齊 */
  padding: 10px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  font-size: 15px;
  font-family: inherit;
  background: #fdfcf7;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
/* 密碼框右側保留眼睛圖示的空間 */
.fg :deep(.pw-input) { padding-right: 44px; }
.fg input:focus,
.fg :deep(input:focus) {
  border-color: var(--leaf-g);
  box-shadow: 0 0 0 4px var(--leaf-soft);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  margin-top: -2px;
}
.check { display: flex; align-items: center; gap: 7px; color: var(--ink-soft); cursor: pointer; }
.check input { accent-color: var(--leaf-g); width: 15px; height: 15px; }
.link { color: var(--leaf-g); text-decoration: none; }
.link:hover { text-decoration: underline; }

.auth-error { margin: 0; color: #c0392b; font-size: 13px; }
.auth-ok    { margin: 0; color: var(--leaf-g); font-size: 13px; }

.auth-resend { margin: 0; color: var(--ink-soft); font-size: 13px; }
.auth-resend a { color: var(--leaf-g); font-weight: 600; text-decoration: none; }
.auth-resend a:hover { text-decoration: underline; }

.btn {
  margin-top: 6px;
  padding: 13px;
  border: none;
  border-radius: 12px;
  background: var(--tomato);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: background 0.2s, transform 0.1s;
}
.btn:hover { background: var(--tomato-dark); }
.btn:active { transform: translateY(1px); }
.btn:focus-visible { outline: 3px solid var(--leaf-g); outline-offset: 2px; }
.btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.5s, height 0.5s;
}
.btn:active::after { width: 340px; height: 340px; transition: 0s; }
.btn:disabled { opacity: 0.75; cursor: not-allowed; }
.btn.done { background: var(--leaf-g); opacity: 1; }

/* 「或」分隔線 */
.divider {
  display: flex;
  align-items: center;
  text-align: center;
  margin: 18px 0 14px;
  color: var(--muted);
  font-size: 13px;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--line);
}
.divider span { padding: 0 12px; }

.foot {
  margin: 14px 0 0;
  text-align: center;
  font-size: 14px;
  color: var(--muted);
}
.foot a { color: var(--leaf-g); font-weight: 600; text-decoration: none; }
.foot a:hover { text-decoration: underline; }

/* ---------- RWD ---------- */
@media (max-width: 840px) {
  .layout {
    grid-template-columns: 1fr;
    max-width: 440px;
    gap: 12px;
    padding: 32px 22px 48px;
  }
  .grove { min-height: auto; gap: 20px; }
  .brand { text-align: center; }
  .brand p { margin: 10px auto 0; }
  .plot { margin: 0 auto; }
  .plant { transform: scale(0.72); transform-origin: bottom center; height: 200px; }
  .card { padding: 28px 24px; }
  .turtle { bottom: 8vh; }
  .auth-page.harvest .turtle { left: 60%; }
}

@media (prefers-reduced-motion: reduce) {
  .sun, .cloud, .mote, .card, .seed,
  .turtle, .t-shell, .t-head, .t-leg { animation: none !important; }
  .turtle { left: 30%; }
  .stem, .leaf, .fruit, .btn::after, .t-head { transition: none !important; }
  .plant.p3 .confetti { animation: none !important; opacity: 0; }
  .auth-page.harvest .t-bubble { animation: none !important; opacity: 1; transform: scale(1); }
}
</style>
