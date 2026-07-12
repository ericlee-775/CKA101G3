<script setup>
// 會員中心：優惠券
// 後端只有兩支 API（都在 /api/member/coupons，需登入，userId 由 session 取得）：
//   GET  /api/member/coupons          → 查我的券 [{ couponId, userId, status }]
//   POST /api/member/coupons/{code}   → 領券（200 成功 / 409 已領過 / 4xx 查無此券）
// 沒有「列出可領券」的 API，所以前台用「輸入優惠碼領取」的流程。
import { ref, onMounted } from 'vue'

// ---------- 我的券 ----------
const myCoupons = ref([])
const loading = ref(true)
const error = ref('')

async function loadMyCoupons() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/member/coupons')
    if (!res.ok) throw new Error(`伺服器回應 ${res.status}`)
    myCoupons.value = await res.json()
  } catch (e) {
    error.value = e.message || '無法載入優惠券，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// ---------- 領券 ----------
const claimCode = ref('')
const claiming = ref(false)
const msg = ref('')
const msgType = ref('') // 'ok' | 'err'

async function claim() {
  const code = claimCode.value.trim()
  if (!code || claiming.value) return
  claiming.value = true
  msg.value = ''
  msgType.value = ''
  try {
    const res = await fetch(`/api/member/coupons/${encodeURIComponent(code)}`, {
      method: 'POST',
    })
    if (res.ok) {
      msgType.value = 'ok'
      msg.value = `成功領取「${code}」！`
      claimCode.value = ''
      await loadMyCoupons() // 重新載入我的券
    } else if (res.status === 409) {
      msgType.value = 'err'
      msg.value = '這張優惠券你已經領過了'
    } else {
      // 查無此券等：後端可能回一段訊息文字
      const text = await res.text().catch(() => '')
      msgType.value = 'err'
      msg.value = text || '領取失敗，請確認優惠碼是否正確'
    }
  } catch {
    msgType.value = 'err'
    msg.value = '領取失敗，請稍後再試。'
  } finally {
    claiming.value = false
  }
}

// ---------- 狀態顯示 ----------
const STATUS_LABEL = { UNUSED: '可使用', USED: '已使用', EXPIRED: '已過期' }
function statusLabel(s) {
  return STATUS_LABEL[s] || s
}

onMounted(loadMyCoupons)
</script>

<template>
  <section class="coupon-page">
    <header class="coupon-head">
      <h1>🎟️ 我的優惠券</h1>
      <p>輸入優惠碼即可領取，領到的券會顯示在下方。</p>
    </header>

    <!-- 領取區 -->
    <div class="claim-box">
      <input
        v-model="claimCode"
        class="claim-input"
        type="text"
        placeholder="輸入優惠碼，例如 WELCOME100"
        @keyup.enter="claim"
      />
      <button class="claim-btn" :disabled="claiming || !claimCode.trim()" @click="claim">
        {{ claiming ? '領取中…' : '領取' }}
      </button>
    </div>
    <p v-if="msg" class="claim-msg" :class="msgType">{{ msg }}</p>

    <!-- 我的券清單 -->
    <p v-if="loading" class="state">載入中…</p>

    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <button type="button" @click="loadMyCoupons">重新載入</button>
    </div>

    <p v-else-if="myCoupons.length === 0" class="state">
      你還沒有任何優惠券，用上方的優惠碼領一張吧！
    </p>

    <ul v-else class="coupon-list">
      <li
        v-for="c in myCoupons"
        :key="c.couponId"
        class="coupon-card"
        :class="{ 'is-inactive': c.status !== 'UNUSED' }"
      >
        <span class="coupon-ticket">🎟️</span>
        <div class="coupon-body">
          <p class="coupon-amount">折 ${{ c.amount }}</p>
          <p class="coupon-info">{{ c.couponInfo }}</p>
          <p class="coupon-cond">滿 ${{ c.minSpending }} 可用</p>
          <p v-if="c.issueEndDate" class="coupon-expiry">到期：{{ c.issueEndDate.slice(0, 10) }}</p>
          <div class="coupon-foot">
            <span class="coupon-code">{{ c.couponId }}</span>
            <span class="coupon-status" :class="c.status === 'UNUSED' ? 'ok' : 'off'">
              {{ statusLabel(c.status) }}
            </span>
          </div>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.coupon-page {
  max-width: 720px;
  margin: 0 auto;
}
.coupon-head {
  text-align: center;
  margin-bottom: 24px;
}
.coupon-head h1 {
  margin: 0 0 6px;
  font-size: 24px;
  color: var(--ink);
}
.coupon-head p {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
}

/* ---------- 領取區 ---------- */
.claim-box {
  display: flex;
  gap: 10px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 16px;
  box-shadow: var(--shadow);
}
.claim-input {
  flex: 1;
  height: 44px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 14px;
  color: var(--ink);
  outline: none;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}
.claim-input:focus {
  border-color: var(--leaf);
  box-shadow: 0 0 0 3px var(--leaf-soft);
}
.claim-btn {
  height: 44px;
  padding: 0 26px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity 0.18s ease;
}
.claim-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.claim-msg {
  text-align: center;
  margin: 14px 0 0;
  font-size: 14px;
}
.claim-msg.ok {
  color: var(--leaf-dark);
}
.claim-msg.err {
  color: #c0392b;
}

/* ---------- 狀態 ---------- */
.state {
  text-align: center;
  color: var(--muted);
  padding: 40px 0;
}
.state--error button {
  margin-top: 12px;
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}

/* ---------- 券清單 ---------- */
.coupon-list {
  list-style: none;
  padding: 0;
  margin: 28px 0 0;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.coupon-card {
  flex: 1 1 260px;      /* 約 260px 寬、可換行；不用 grid 的 1fr 硬撐 */
  max-width: 340px;     /* 少量券時也不會被拉太寬 */
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 18px;
  box-shadow: var(--shadow);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.coupon-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}
.coupon-card.is-inactive {
  opacity: 0.55;
}
.coupon-ticket {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;        /* emoji 不被壓縮 */
}
.coupon-body {
  flex: 1;
  min-width: 0;          /* 讓長文字能在 flex 內正常換行，不撐爆卡片 */
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.coupon-info {
  overflow-wrap: anywhere;   /* 券說明太長時自動斷行 */
}
.coupon-amount {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--leaf-dark);
}
.coupon-info {
  margin: 0;
  font-size: 14px;
  color: var(--ink);
}
.coupon-cond,
.coupon-expiry {
  margin: 0;
  font-size: 12px;
  color: var(--muted);
}
.coupon-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
}
.coupon-code {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: var(--muted);
}
.coupon-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
  white-space: nowrap;
}
.coupon-status.ok {
  background: var(--leaf-soft);
  color: var(--leaf-dark);
}
.coupon-status.off {
  background: var(--line);
  color: var(--muted);
}
</style>
