<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import memberApi from '@/api/member'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()

// 表單欄位
const name = ref('')
const email = ref('')
const password = ref('')
const confirm = ref('')

const error = ref('')
const done = ref(false)
const loading = ref(false)

// ===== 田埂插秧:一個欄位對應一叢秧苗,填好就插好一叢 =====
const nameOk = computed(() => name.value.trim().length > 0)
const emailOk = computed(() => email.value.includes('@') && email.value.length > 3)
// 對齊後端：8~60 字且需同時含英文與數字
const pwdOk = computed(() =>
  password.value.length >= 8 &&
  password.value.length <= 60 &&
  /[A-Za-z]/.test(password.value) &&
  /\d/.test(password.value)
)
const confirmOk = computed(() => confirm.value.length > 0 && confirm.value === password.value)

// 四叢秧苗:label 顯示在小木牌上,ok = 該欄位完成(插好秧)
const crops = computed(() => [
  { label: '姓名', ok: nameOk.value },
  { label: '信箱', ok: emailOk.value },
  { label: '密碼', ok: pwdOk.value },
  { label: '確認', ok: confirmOk.value },
])

const grownCount = computed(() => crops.value.filter((c) => c.ok).length)
const hint = computed(() => {
  if (done.value) return '結穗了!歡迎加入 Farmily'
  if (grownCount.value === 4) return '四叢都插好了,按「註冊」等它結穗'
  return `已插好 ${grownCount.value} / 4 叢秧苗`
})

// 送出註冊:呼叫後端 POST /api/member/register
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
  // 後端 @Size(8~60) + @Pattern：長度 8~60 且需同時含英文與數字
  if (password.value.length < 8 || password.value.length > 60) {
    error.value = '密碼長度需 8~60 字'
    return
  }
  if (!/[A-Za-z]/.test(password.value) || !/\d/.test(password.value)) {
    error.value = '密碼需同時包含英文字母與數字'
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
    // 註冊成功後導到登入頁(留一點時間看結穗與飛鳥動畫)
    setTimeout(() => router.push('/login'), 1800)
  } catch (e) {
    error.value = e.message || '註冊失敗,請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page" :class="{ harvest: done }">
    <!-- ===== 大地展開:清晨田園場景(純裝飾) ===== -->
    <div class="scene" aria-hidden="true">
      <!-- 逆光暈染 + 低垂的太陽 -->
      <div class="glow"></div>
      <div class="sun"></div>

      <!-- 雲朵:微幅飄動 -->
      <div class="cloud c1"></div>
      <div class="cloud c2"></div>
      <div class="cloud c3"></div>

      <!-- 遠山:進場時從左右兩側緩緩滑入 -->
      <div class="hill far-l"></div>
      <div class="hill far-r"></div>
      <div class="hill mid-l"></div>
      <div class="hill mid-r"></div>

      <!-- 農舍:煙囪升起輕柔炊煙 -->
      <div class="farmhouse">
        <i class="smoke s1"></i>
        <i class="smoke s2"></i>
        <i class="smoke s3"></i>
        <i class="chimney"></i>
        <i class="roof"></i>
        <i class="wall"></i>
        <i class="door"></i>
        <i class="win"></i>
      </div>

      <!-- 三層稻田:進場時從中央向兩側展開 -->
      <div class="paddy p3"></div>
      <div class="paddy p2"></div>
      <div class="paddy p1"></div>

      <!-- 田埂上散步的小烏龜(和登入頁同一隻):註冊成功會停下來回頭 -->
      <div class="turtle">
        <div class="t-bubble">歡迎加入!</div>
        <div class="t-shell"></div>
        <div class="t-head"></div>
        <div class="t-leg front"></div>
        <div class="t-leg back"></div>
        <div class="t-tail"></div>
      </div>

      <!-- 飛鳥:註冊成功時掠過天空 -->
      <div class="bird b1"></div>
      <div class="bird b2"></div>
    </div>

    <div class="layout">
      <!-- 左:品牌 + 田埂秧苗 -->
      <section class="grove">
        <div class="brand">
          <div class="logo"><span class="sprout">🌱</span> Farmily <em>小農市集</em></div>
          <p>清晨的田剛醒。填一格,插一叢秧;四叢插好,就和 Farmily 一起等收成。</p>
        </div>

        <!-- 田埂:四叢秧苗,對應四個欄位 -->
        <div class="nursery">
          <div class="ridge-row">
            <div
              v-for="(c, i) in crops"
              :key="c.label"
              class="crop"
              :class="{ grown: c.ok }"
              :style="{ '--i': i }"
            >
              <div class="stalks">
                <i class="st a"></i>
                <i class="st b"></i>
                <i class="st c"></i>
                <i class="grain"></i>
              </div>
            </div>
          </div>
          <div class="ridge"></div>
          <p class="hint">{{ hint }}</p>
        </div>
      </section>

      <!-- 右:註冊表單(暖色毛玻璃卡片) -->
      <section class="card">
        <h1>會員註冊</h1>
        <p class="sub">加入 Farmily,享受產地直送</p>

        <form class="auth-form" @submit.prevent="handleRegister">
          <label class="fg">
            <span class="lbl">姓名</span>
            <input v-model="name" type="text" placeholder="你的名字" autocomplete="name" />
          </label>

          <label class="fg">
            <span class="lbl">電子信箱</span>
            <input v-model="email" type="email" placeholder="you@example.com" autocomplete="email" />
          </label>

          <label class="fg">
            <span class="lbl">密碼</span>
            <PasswordInput v-model="password" placeholder="至少 8 個字元" autocomplete="new-password" />
          </label>

          <label class="fg">
            <span class="lbl">確認密碼</span>
            <PasswordInput v-model="confirm" placeholder="再輸入一次密碼" autocomplete="new-password" />
          </label>

          <p v-if="error" class="auth-error">{{ error }}</p>
          <p v-if="done" class="auth-ok">註冊成功!正在帶你前往登入…</p>

          <button class="btn" :class="{ done }" type="submit" :disabled="loading || done">
            {{ done ? '歡迎加入 Farmily' : loading ? '註冊中…' : '註冊' }}
          </button>
        </form>

        <p class="foot">
          已經有帳號了?
          <router-link to="/login">前往登入</router-link>
        </p>
      </section>
    </div>
  </main>
</template>

<style scoped>
/* ============ 主題色(自帶,不依賴全域變數) ============ */
.auth-page {
  --sky-hi: #fdf4e3;      /* 清晨天頂的暖白 */
  --sky-lo: #fbe8cd;      /* 靠近地平線的金 */
  --sun-core: #ffd98d;    /* 太陽 */
  --hill-far: #d8e0c0;    /* 遠山(帶霧氣) */
  --hill-mid: #b7cc95;    /* 中景山丘 */
  --paddy-3: #a8c583;     /* 最遠一層稻田 */
  --paddy-2: #8db56b;
  --paddy-1: #74a558;     /* 最前景稻田 */
  --soil: #a97b52;        /* 田埂土色 */
  --soil-dark: #8f6540;
  --rice-g: #7cb35f;      /* 秧苗綠 */
  --rice-dark: #55873f;
  --rice-gold: #e0b054;   /* 結穗的金 */
  --leaf-g: #3f7a44;
  --leaf-soft: #e7f1e2;
  --tomato: #d95a45;
  --tomato-dark: #c04b38;
  --ink: #33402e;
  --ink-soft: #5b6653;
  --muted: #8f9784;
  --line: #e5dfd0;

  position: relative;
  /* 這頁掛在 ShopLayout 底下(上方有 sticky header,約 84px),
     扣掉 header 才不會超出一個螢幕、也不會在上方留過多空白 */
  min-height: calc(100dvh - 84px);
  overflow: hidden;
  font-family: 'Noto Sans TC', system-ui, sans-serif;
  color: var(--ink);
  /* 清晨:天頂暖白,往地平線漸金 */
  background: linear-gradient(180deg, var(--sky-hi) 0%, var(--sky-lo) 62%, #f2ecd6 100%);
}

/* ============ 場景 ============ */
.scene {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

/* 逆光暈染:從地平線灑上來的暖光,緩慢呼吸 */
.glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 70% 55% at 68% 78%, rgba(255, 214, 140, 0.55), transparent 68%);
  animation: breathe 7s ease-in-out infinite alternate;
}
@keyframes breathe {
  from { opacity: 0.7; }
  to   { opacity: 1; }
}
/* 註冊成功:天光更暖 */
.auth-page.harvest .glow {
  opacity: 1;
  filter: brightness(1.18) saturate(1.1);
}

/* 低垂的太陽:半沉在遠山後 */
.sun {
  position: absolute;
  right: 24%;
  bottom: 26%;
  width: 92px;
  height: 92px;
  border-radius: 50%;
  background: radial-gradient(circle, #fff2d0 20%, var(--sun-core) 60%, rgba(255, 217, 141, 0));
  filter: blur(2px);
}

/* 雲朵:三團圓角雲,微幅飄動 */
.cloud {
  position: absolute;
  height: 22px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 2px 10px rgba(220, 190, 140, 0.25);
  animation: drift 26s ease-in-out infinite alternate;
}
.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  bottom: 6px;
  border-radius: 50%;
  background: inherit;
  box-shadow: none;
}
.cloud::before { left: 16%; width: 34px; height: 34px; }
.cloud::after  { right: 18%; width: 24px; height: 24px; }
.cloud.c1 { top: 12%; left: 10%; width: 120px; }
.cloud.c2 { top: 22%; left: 56%; width: 90px; opacity: 0.75; animation-delay: -9s; }
.cloud.c3 { top: 7%;  left: 76%; width: 70px; opacity: 0.6;  animation-delay: -17s; }
@keyframes drift {
  from { transform: translateX(-14px); }
  to   { transform: translateX(20px); }
}

/* ---------- 遠山:大地展開,從兩側緩緩滑入 ---------- */
.hill {
  position: absolute;
  border-radius: 50%;
  animation-duration: 1.8s;
  animation-timing-function: cubic-bezier(0.22, 0.9, 0.3, 1);
  animation-fill-mode: both;
}
.hill.far-l {
  left: -22%;
  bottom: 22%;
  width: 62%;
  height: 30%;
  background: var(--hill-far);
  animation-name: unfold-l;
}
.hill.far-r {
  right: -20%;
  bottom: 24%;
  width: 58%;
  height: 26%;
  background: var(--hill-far);
  animation-name: unfold-r;
  animation-delay: 0.1s;
}
.hill.mid-l {
  left: -14%;
  bottom: 16%;
  width: 52%;
  height: 22%;
  background: var(--hill-mid);
  animation-name: unfold-l;
  animation-delay: 0.2s;
}
.hill.mid-r {
  right: -12%;
  bottom: 15%;
  width: 48%;
  height: 20%;
  background: var(--hill-mid);
  animation-name: unfold-r;
  animation-delay: 0.28s;
}
@keyframes unfold-l {
  from { opacity: 0; transform: translateX(-70px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes unfold-r {
  from { opacity: 0; transform: translateX(70px); }
  to   { opacity: 1; transform: translateX(0); }
}

/* ---------- 農舍:白牆紅瓦 + 炊煙 ---------- */
.farmhouse {
  position: absolute;
  left: 13%;
  bottom: 24%;
  width: 76px;
  height: 78px;
  animation: house-in 1.2s ease-out 1s both;
}
@keyframes house-in {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
.farmhouse .wall {
  position: absolute;
  left: 8px;
  bottom: 0;
  width: 60px;
  height: 34px;
  background: linear-gradient(180deg, #faf3e3, #efe3cb);
  border-radius: 2px;
}
.farmhouse .roof {
  position: absolute;
  left: 0;
  bottom: 30px;
  width: 0;
  height: 0;
  border-left: 38px solid transparent;
  border-right: 38px solid transparent;
  border-bottom: 26px solid #c96f4f;
}
.farmhouse .chimney {
  position: absolute;
  left: 50px;
  bottom: 46px;
  width: 9px;
  height: 16px;
  background: #a9805e;
  border-radius: 2px 2px 0 0;
}
.farmhouse .door {
  position: absolute;
  left: 22px;
  bottom: 0;
  width: 12px;
  height: 18px;
  background: #a9805e;
  border-radius: 5px 5px 0 0;
}
.farmhouse .win {
  position: absolute;
  left: 44px;
  bottom: 12px;
  width: 12px;
  height: 11px;
  background: #ffe9b3;
  border: 2px solid #d8c49a;
  border-radius: 2px;
}
/* 炊煙:三團小圓,微幅向上飄散淡出 */
.farmhouse .smoke {
  position: absolute;
  left: 50px;
  bottom: 62px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.75);
  opacity: 0;
  animation: puff 4.2s ease-out infinite;
}
.farmhouse .smoke.s2 { animation-delay: 1.4s; }
.farmhouse .smoke.s3 { animation-delay: 2.8s; }
@keyframes puff {
  0%   { opacity: 0;   transform: translate(0, 0) scale(0.5); }
  18%  { opacity: 0.8; }
  100% { opacity: 0;   transform: translate(14px, -52px) scale(1.7); }
}

/* ---------- 三層稻田:從中央向兩側展開(clip-path 不變形) ---------- */
.paddy {
  position: absolute;
  left: 0;
  right: 0;
  animation: spread 1.9s cubic-bezier(0.22, 0.9, 0.3, 1) both;
}
.paddy.p3 {
  bottom: 13%;
  height: 8%;
  background:
    repeating-linear-gradient(90deg, transparent 0 72px, rgba(255, 255, 255, 0.14) 72px 75px),
    linear-gradient(180deg, var(--paddy-3), #9cba76);
  animation-delay: 0.25s;
}
.paddy.p2 {
  bottom: 6.5%;
  height: 8.5%;
  background:
    repeating-linear-gradient(90deg, transparent 0 88px, rgba(255, 255, 255, 0.16) 88px 91px),
    linear-gradient(180deg, var(--paddy-2), #81aa60);
  border-top: 3px solid rgba(255, 248, 224, 0.5);   /* 田水反光 */
  animation-delay: 0.4s;
}
.paddy.p1 {
  bottom: 0;
  height: 9%;
  background:
    repeating-linear-gradient(90deg, transparent 0 108px, rgba(255, 255, 255, 0.18) 108px 112px),
    linear-gradient(180deg, var(--paddy-1), #659550);
  border-top: 3px solid rgba(255, 248, 224, 0.55);
  animation-delay: 0.55s;
}
@keyframes spread {
  from { clip-path: inset(0 50% 0 50%); opacity: 0.4; }
  to   { clip-path: inset(0 0 0 0);     opacity: 1; }
}
/* 註冊成功:稻田轉一點金黃 */
.auth-page.harvest .paddy { filter: sepia(0.25) saturate(1.1); transition: filter 0.9s; }

/* ---------- 小烏龜(和登入頁同一隻) ---------- */
.turtle {
  position: absolute;
  /* 走在最前排稻田的田埂線上(p1 高 9%) */
  bottom: 8.6%;
  left: -80px;
  width: 52px;
  height: 34px;
  animation: stroll 70s linear -18s infinite;
}
@keyframes stroll {
  to { transform: translateX(calc(100vw + 160px)); }
}

/* 註冊成功:停下腳步,站到畫面中前方 */
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

/* 回頭:頭移到殼的後方並鏡像,眼睛朝向後面(看你) */
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

/* ---------- 飛鳥:成功時掠過天空 ---------- */
.bird {
  position: absolute;
  top: 18%;
  left: -6%;
  width: 22px;
  height: 8px;
  opacity: 0;
}
.bird::before,
.bird::after {
  content: '';
  position: absolute;
  top: 4px;
  width: 12px;
  height: 3px;
  border-radius: 3px;
  background: #7a6a55;
}
.bird::before { left: 0;  transform: rotate(-24deg); transform-origin: right center; }
.bird::after  { right: 0; transform: rotate(24deg);  transform-origin: left center; }
.bird.b2 { top: 23%; transform: scale(0.7); }
.auth-page.harvest .bird { animation: fly 3.6s ease-out forwards; }
.auth-page.harvest .bird.b2 { animation-delay: 0.35s; }
@keyframes fly {
  0%   { opacity: 0; transform: translate(0, 0); }
  12%  { opacity: 1; }
  50%  { transform: translate(30vw, -26px); }
  100% { opacity: 0; transform: translate(64vw, -12px); }
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

/* ---------- 左:品牌 + 田埂秧苗 ---------- */
.grove {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 420px;
}
.brand .logo {
  display: inline-flex;
  align-items: baseline;
  gap: 10px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 34px;
  font-weight: 700;
  color: #4a3d28;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
}
.brand .logo .sprout { font-size: 26px; }
.brand .logo em {
  font-style: normal;
  font-size: 15px;
  font-weight: 400;
  color: #6b5a3c;
  letter-spacing: 2px;
}
.brand p {
  margin-top: 12px;
  max-width: 320px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 17px;
  line-height: 1.9;
  color: #57492f;
}

/* ---------- 田埂與秧苗 ---------- */
.nursery {
  margin-top: auto;
  width: min(340px, 100%);
}
.ridge-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 0 18px;
}
.ridge {
  height: 11px;
  border-radius: 6px;
  background: linear-gradient(180deg, var(--soil), var(--soil-dark));
  box-shadow: 0 8px 16px rgba(120, 90, 50, 0.22);
}
.hint {
  margin: 10px 2px 0;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 14px;
  color: #6b5a3c;
}

/* 一叢秧 = 三根稻莖 + 木牌;結穗時冒出金黃稻穗 */
.crop {
  position: relative;
  width: 56px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stalks {
  position: relative;
  width: 36px;
  height: 48px;
  transform-origin: bottom center;
}
/* 微風:插好秧的那叢輕輕搖曳 */
.crop.grown .stalks {
  animation: breeze 3.4s ease-in-out infinite alternate;
  animation-delay: calc(var(--i) * -0.8s);
}
@keyframes breeze {
  from { transform: rotate(-2deg); }
  to   { transform: rotate(2.6deg); }
}

.st {
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 3px;
  border-radius: 3px;
  background: linear-gradient(180deg, var(--rice-g), var(--rice-dark));
  transform-origin: bottom center;
  transform: translateX(-50%) scaleY(0);
  transition: transform 0.7s cubic-bezier(0.22, 1.2, 0.3, 1), background 0.8s;
}
/* 頂端一小片葉,讓稻莖不只是直線 */
.st::after {
  content: '';
  position: absolute;
  top: 1px;
  left: -2px;
  width: 11px;
  height: 5px;
  border-radius: 100% 0;
  background: #9ccb7a;
  transform: rotate(-32deg);
  transform-origin: left bottom;
  transition: background 0.8s;
}
.st.a { height: 34px; transform: translateX(-50%) rotate(-13deg) scaleY(0); transition-delay: 0s; }
.st.b { height: 42px; transition-delay: 0.08s; }
.st.c { height: 32px; transform: translateX(-50%) rotate(13deg) scaleY(0); transition-delay: 0.16s; }
.st.c::after { left: auto; right: -2px; border-radius: 0 100%; transform: rotate(32deg); transform-origin: right bottom; }

.crop.grown .st   { transform: translateX(-50%) scaleY(1); }
.crop.grown .st.a { transform: translateX(-50%) rotate(-13deg) scaleY(1); }
.crop.grown .st.c { transform: translateX(-50%) rotate(13deg) scaleY(1); }

/* 結穗:成功時每叢頂端垂下金黃稻穗,依序彈出 */
.grain {
  position: absolute;
  top: -4px;
  left: 55%;
  width: 8px;
  height: 15px;
  border-radius: 50% 50% 60% 60%;
  background: linear-gradient(180deg, #ecc370, var(--rice-gold));
  transform: rotate(26deg) scale(0);
  transform-origin: top center;
  opacity: 0;
}
.auth-page.harvest .crop.grown .grain {
  animation: ripen 0.6s cubic-bezier(0.2, 1.5, 0.4, 1) forwards;
  animation-delay: calc(0.3s + var(--i) * 0.14s);
}
@keyframes ripen {
  to { opacity: 1; transform: rotate(26deg) scale(1); }
}
/* 成功時稻莖也染上一點金 */
.auth-page.harvest .crop.grown .st {
  background: linear-gradient(180deg, #c9b45e, #8a8a3f);
}
.auth-page.harvest .crop.grown .st::after { background: #d9c46e; }

/* 小木牌:插在秧前寫欄位名 */
.tag {
  margin-top: 8px;
  padding: 2px 8px;
  font-family: 'LXGW WenKai TC', serif;
  font-size: 12px;
  color: #6d5233;
  background: linear-gradient(180deg, #e9d3ae, #ddc094);
  border: 1px solid #cbaa78;
  border-radius: 4px;
  transition: color 0.3s, border-color 0.3s;
}
.crop.grown .tag {
  color: var(--leaf-g);
  border-color: var(--rice-g);
}

/* ---------- 右:註冊卡片(暖色毛玻璃) ---------- */
.card {
  background: rgba(255, 252, 245, 0.85);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 22px;
  padding: 26px 32px 28px;
  box-shadow: 0 24px 60px rgba(140, 110, 60, 0.18);
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

.auth-form { display: flex; flex-direction: column; gap: 12px; }

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
  background: #fffdf8;
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

.auth-error { margin: 0; color: #c0392b; font-size: 13px; }
.auth-ok    { margin: 0; color: var(--leaf-g); font-size: 13px; }

.btn {
  margin-top: 4px;
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
    gap: 16px;
    padding: 28px 22px 44px;
  }
  .grove { min-height: auto; gap: 20px; }
  .brand { text-align: center; }
  .brand p { margin: 10px auto 0; }
  .nursery { margin: 0 auto; }
  .hint { text-align: center; }
  .card { padding: 28px 24px; }
  .farmhouse { left: 4%; }
  .sun { right: 10%; }
  .auth-page.harvest .turtle { left: 60%; }
}

@media (prefers-reduced-motion: reduce) {
  .glow, .cloud, .hill, .paddy, .farmhouse, .card,
  .turtle, .t-shell, .t-head, .t-leg,
  .crop.grown .stalks { animation: none !important; }
  .turtle { left: 30%; }
  .t-head { transition: none !important; }
  .auth-page.harvest .t-bubble { animation: none !important; opacity: 1; transform: scale(1); }
  .hill, .paddy, .farmhouse { opacity: 1; }
  .paddy { clip-path: inset(0 0 0 0); }
  .st, .tag, .btn::after { transition: none !important; }
  .farmhouse .smoke { animation: none !important; opacity: 0; }
  .auth-page.harvest .bird { animation: none !important; opacity: 0; }
  .auth-page.harvest .crop.grown .grain {
    animation: none !important;
    opacity: 1;
    transform: rotate(26deg) scale(1);
  }
}
</style>
