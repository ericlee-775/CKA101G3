<script setup>
import { ref, computed, onMounted } from 'vue'
import farmerApi from '@/api/farmer'
import cityDistrictApi from '@/api/cityDistrict'

// 身分驗證（免登入用 email + password）
const email = ref('')
const password = ref('')

const status = ref(null)     // FarmerReviewResponse
const globalMsg = ref('')
const globalErr = ref('')
const loading = ref(false)

// 縣市 / 行政區（重新送審用）
const districtList = ref([])
const selectedCity = ref('')
const cities = computed(() => [...new Set(districtList.value.map((d) => d.cityName))])
const districtsInCity = computed(() =>
  districtList.value.filter((d) => d.cityName === selectedCity.value)
)

// 重新送審欄位
const apply = ref({ farmName: '', farmAddress: '', districtId: '' })
const applyMsg = ref('')
const applyErr = ref('')
const savingApply = ref(false)

onMounted(async () => {
  try {
    districtList.value = await cityDistrictApi.listAll()
  } catch {
    districtList.value = []
  }
})

function requireCreds() {
  if (!email.value || !password.value) {
    globalErr.value = '請先輸入信箱與密碼以驗證身分'
    return false
  }
  return true
}

async function checkStatus() {
  globalMsg.value = ''
  globalErr.value = ''
  if (!requireCreds()) return
  loading.value = true
  try {
    const res = await farmerApi.application.status({ email: email.value, password: password.value })
    status.value = res
    // 預先帶入重新送審表單
    apply.value.farmName = res.submittedFarmName || res.farmName || ''
    apply.value.farmAddress = res.submittedFarmAddress || ''
    selectedCity.value = res.submittedCityName || ''
  } catch (e) {
    status.value = null
    globalErr.value = e.message || '查詢失敗'
  } finally {
    loading.value = false
  }
}

async function resendActivation() {
  globalMsg.value = ''
  globalErr.value = ''
  if (!requireCreds()) return
  loading.value = true
  try {
    const msg = await farmerApi.application.resendActivation({ email: email.value, password: password.value })
    globalMsg.value = typeof msg === 'string' ? msg : '已重新寄出啟用信，請至信箱確認'
  } catch (e) {
    globalErr.value = e.message || '重寄失敗'
  } finally {
    loading.value = false
  }
}

function onCityChange() {
  apply.value.districtId = ''
}

async function resubmit() {
  applyMsg.value = ''
  applyErr.value = ''
  if (!apply.value.farmName || !apply.value.farmAddress) {
    applyErr.value = '請填寫農場名稱與地址'
    return
  }
  savingApply.value = true
  try {
    const res = await farmerApi.application.resubmit({
      email: email.value,
      password: password.value,
      farmName: apply.value.farmName,
      farmAddress: apply.value.farmAddress,
      districtId: apply.value.districtId ? Number(apply.value.districtId) : null,
    })
    status.value = res
    applyMsg.value = '已重新送審，請等待管理員審核'
  } catch (e) {
    applyErr.value = e.message || '送審失敗'
  } finally {
    savingApply.value = false
  }
}
</script>

<template>
  <main class="app-page">
    <div class="app-wrap">
      <h1>🌾 小農申請進度</h1>
      <p class="sub">尚未通過初審或尚未完成 Email 驗證而無法登入時，可在此查詢進度、重寄啟用信或重新送審。</p>

      <!-- 身分驗證 -->
      <section class="card">
        <h2>驗證身分</h2>
        <div class="form">
          <label><span>電子信箱</span><input v-model="email" type="email" placeholder="farmer@example.com" /></label>
          <label><span>密碼</span><input v-model="password" type="password" /></label>
          <p v-if="globalErr" class="msg-err">{{ globalErr }}</p>
          <p v-if="globalMsg" class="msg-ok">{{ globalMsg }}</p>
          <div class="btn-row">
            <button class="btn" @click="checkStatus" :disabled="loading">查詢審核狀態</button>
            <button class="btn-ghost" @click="resendActivation" :disabled="loading">重寄啟用信</button>
          </div>
        </div>
      </section>

      <!-- 審核狀態 -->
      <section v-if="status" class="card">
        <h2>目前狀態</h2>
        <div class="info-grid">
          <div><span class="label">農場名稱</span><span>{{ status.submittedFarmName || status.farmName || '—' }}</span></div>
          <div><span class="label">審核狀態</span><span class="badge">{{ status.reviewStatus }}</span></div>
          <div><span class="label">審核輪次</span><span>{{ status.reviewRound ?? '—' }}</span></div>
          <div><span class="label">送審時間</span><span>{{ status.submittedAt || '—' }}</span></div>
        </div>
        <div v-if="status.rejectReason" class="reject-box">
          <strong>退件理由：</strong>{{ status.rejectReason }}
        </div>
      </section>

      <!-- 被退件才顯示重新送審 -->
      <section v-if="status && status.reviewStatus === 'REJECTED'" class="card">
        <h2>重新送審</h2>
        <form class="form" @submit.prevent="resubmit">
          <label><span>農場名稱</span><input v-model="apply.farmName" type="text" /></label>
          <div class="grid2">
            <label>
              <span>縣市</span>
              <select v-model="selectedCity" @change="onCityChange">
                <option value="">請選擇縣市</option>
                <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
              </select>
            </label>
            <label>
              <span>行政區</span>
              <select v-model="apply.districtId" :disabled="!selectedCity">
                <option value="">請選擇行政區</option>
                <option v-for="d in districtsInCity" :key="d.districtId" :value="d.districtId">
                  {{ d.distName }}
                </option>
              </select>
            </label>
          </div>
          <label><span>農場地址</span><input v-model="apply.farmAddress" type="text" /></label>
          <p v-if="applyErr" class="msg-err">{{ applyErr }}</p>
          <p v-if="applyMsg" class="msg-ok">{{ applyMsg }}</p>
          <button class="btn" type="submit" :disabled="savingApply">
            {{ savingApply ? '送出中…' : '重新送審' }}
          </button>
        </form>
      </section>

      <p class="foot">
        <router-link to="/farmer/login">← 回小農登入</router-link>
      </p>
    </div>
  </main>
</template>

<style scoped>
.app-page {
  padding: 40px 18px;
  min-height: 60vh;
}
.app-wrap {
  max-width: 620px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.app-wrap h1 {
  margin: 0;
  font-size: 26px;
  color: var(--ink);
}
.sub {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}
.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
}
.card h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--ink);
}
.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: var(--ink-soft);
}
.form input,
.form select {
  padding: 10px 13px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
  font-family: inherit;
}
.form input:focus,
.form select:focus {
  border-color: var(--leaf);
}
.grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.btn-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: var(--leaf);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}
.btn:hover {
  background: var(--leaf-dark);
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-ghost {
  padding: 10px 20px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  color: var(--ink-soft);
  cursor: pointer;
}
.btn-ghost:hover {
  border-color: var(--leaf);
  color: var(--leaf);
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  font-size: 14px;
  color: var(--ink-soft);
}
.info-grid .label {
  display: block;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 2px;
}
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 600;
}
.reject-box {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fdECEC;
  color: #c0392b;
  font-size: 14px;
}
.msg-err {
  margin: 0;
  color: #c0392b;
  font-size: 13px;
}
.msg-ok {
  margin: 0;
  color: var(--leaf);
  font-size: 13px;
}
.foot {
  text-align: center;
  font-size: 14px;
}
.foot a {
  color: var(--leaf);
  font-weight: 600;
  text-decoration: none;
}
.foot a:hover {
  text-decoration: underline;
}
</style>
