<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import farmerApi from '@/api/farmer'
import cityDistrictApi from '@/api/cityDistrict'
import PasswordInput from '@/components/PasswordInput.vue'
import { validateCertFile } from '@/utils/certFile'

const router = useRouter()

// 表單欄位（對齊後端 FarmerRegisterRequest）
const farmName = ref('')          // 農場名稱
const email = ref('')
const phone = ref('')             // farmerPhoneNum
const farmAddress = ref('')       // 農場地址（後端必填）
const farmDesc = ref('')          // 農場介紹（後端必填）
const districtId = ref('')        // 行政區 id
const password = ref('')
const confirm = ref('')

// 證明文件（三張皆必填）：農地、產品、身分
const certLand = ref(null)        // certFileLand
const certProduct = ref(null)     // certFileProduct
const certIdentity = ref(null)    // certFileIdentity

const certRefs = { land: certLand, product: certProduct, identity: certIdentity }

// 使用者在該欄位選檔：驗證通過才存入對應 ref，並清掉舊錯誤
function onCertChange(key, event) {
  error.value = ''
  const target = certRefs[key]
  const file = event.target.files && event.target.files[0]
  if (!file) {
    target.value = null
    return
  }
  const msg = validateCertFile(file)
  if (msg) {
    error.value = msg
    target.value = null
    event.target.value = ''   // 驗證失敗清空，允許重選同一檔
    return
  }
  target.value = file
}

// 縣市 / 行政區下拉資料（來自 /api/city-districts）
const districtList = ref([])      // 全部行政區的扁平清單
const selectedCity = ref('')      // 先選縣市，再選行政區

const error = ref('')
const done = ref(false)
const loading = ref(false)

// 載入縣市/行政區
onMounted(async () => {
  try {
    districtList.value = await cityDistrictApi.listAll()
  } catch {
    // 下拉載入失敗不擋註冊，只是無法選區
    districtList.value = []
  }
})

// 不重複的縣市清單
const cities = computed(() => {
  const set = new Set(districtList.value.map((d) => d.cityName))
  return [...set]
})

// 依選到的縣市過濾出行政區
const districtsInCity = computed(() =>
  districtList.value.filter((d) => d.cityName === selectedCity.value)
)

// 換縣市時清掉已選的行政區
function onCityChange() {
  districtId.value = ''
}

// 送出小農註冊申請：POST /api/farmer/register
async function handleRegister() {
  error.value = ''

  if (!farmName.value || !email.value || !phone.value ||
      !farmAddress.value || !farmDesc.value || !password.value) {
    error.value = '請完整填寫農場名稱、信箱、電話、地址、介紹與密碼'
    return
  }
  if (!email.value.includes('@')) {
    error.value = '信箱格式不正確'
    return
  }
  // 後端 @Size(min = 8)
  if (password.value.length < 8) {
    error.value = '密碼至少需 8 個字元'
    return
  }
  if (password.value !== confirm.value) {
    error.value = '兩次輸入的密碼不一致'
    return
  }
  // 三張證明文件皆必填
  if (!certLand.value || !certProduct.value || !certIdentity.value) {
    error.value = '請上傳農地、產品與身分三項證明文件'
    return
  }

  loading.value = true
  try {
    // 走 multipart/form-data：欄位名稱需對齊後端 FarmerRegisterRequest
    const fd = new FormData()
    fd.append('email', email.value)
    fd.append('password', password.value)
    fd.append('farmName', farmName.value)
    fd.append('farmAddress', farmAddress.value)
    fd.append('farmerPhoneNum', phone.value)
    fd.append('farmDesc', farmDesc.value)
    // districtId 為選填，有選才送
    if (districtId.value) fd.append('districtId', Number(districtId.value))
    fd.append('certFileLand', certLand.value)
    fd.append('certFileProduct', certProduct.value)
    fd.append('certFileIdentity', certIdentity.value)
    await farmerApi.register(fd)
    done.value = true
    // 申請成功導到小農登入頁
    setTimeout(() => router.push('/farmer/login'), 1200)
  } catch (e) {
    error.value = e.message || '申請失敗，請稍後再試'
  } finally {
    loading.value = false
  }
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
          <span>電子信箱</span>
          <input v-model="email" type="email" placeholder="farmer@example.com" />
        </label>

        <label>
          <span>聯絡電話</span>
          <input v-model="phone" type="tel" placeholder="0912-345-678" />
        </label>

        <!-- 縣市 / 行政區（選填，選了地址才完整）-->
        <div class="auth-grid">
          <label>
            <span>縣市</span>
            <select v-model="selectedCity" @change="onCityChange">
              <option value="">請選擇縣市</option>
              <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label>
            <span>行政區</span>
            <select v-model="districtId" :disabled="!selectedCity">
              <option value="">請選擇行政區</option>
              <option v-for="d in districtsInCity" :key="d.districtId" :value="d.districtId">
                {{ d.distName }}
              </option>
            </select>
          </label>
        </div>

        <label>
          <span>農場地址</span>
          <input v-model="farmAddress" type="text" placeholder="門牌詳細地址" />
        </label>

        <label>
          <span>農場介紹</span>
          <textarea v-model="farmDesc" rows="3" placeholder="簡單介紹你的農場與產品"></textarea>
        </label>

        <!-- 證明文件（三張皆必填，JPG / PNG，單張 ≤ 2MB）-->
        <div class="cert-group">
          <p class="cert-title">證明文件（皆須上傳，JPG / PNG，單張 ≤ 5MB）</p>
          <label class="cert-item">
            <span>農地證明</span>
            <input type="file" accept="image/jpeg,image/png" @change="onCertChange('land', $event)" />
            <small v-if="certLand" class="cert-name">已選：{{ certLand.name }}</small>
          </label>
          <label class="cert-item">
            <span>產品證明</span>
            <input type="file" accept="image/jpeg,image/png" @change="onCertChange('product', $event)" />
            <small v-if="certProduct" class="cert-name">已選：{{ certProduct.name }}</small>
          </label>
          <label class="cert-item">
            <span>身分證明</span>
            <input type="file" accept="image/jpeg,image/png" @change="onCertChange('identity', $event)" />
            <small v-if="certIdentity" class="cert-name">已選：{{ certIdentity.name }}</small>
          </label>
        </div>

        <label>
          <span>密碼</span>
          <PasswordInput v-model="password" placeholder="至少 8 個字元" autocomplete="new-password" />
        </label>

        <label>
          <span>確認密碼</span>
          <PasswordInput v-model="confirm" placeholder="再輸入一次密碼" autocomplete="new-password" />
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">申請成功！正在帶你前往登入…</p>

        <button class="auth-btn" type="submit" :disabled="loading">
          {{ loading ? '送出中…' : '送出申請' }}
        </button>
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
  max-width: 420px;
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
.auth-form input,
.auth-form select,
.auth-form textarea {
  padding: 11px 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.18s ease;
  font-family: inherit;
}
.auth-form input:focus,
.auth-form select:focus,
.auth-form textarea:focus {
  border-color: var(--leaf);
}
.auth-form textarea {
  resize: vertical;
}

/* 縣市 + 行政區 兩欄並排 */
.auth-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

/* 證明文件上傳區 */
.cert-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
  border: 1px dashed var(--line);
  border-radius: 10px;
}
.cert-title {
  margin: 0;
  font-size: 13px;
  color: var(--ink-soft);
  font-weight: 600;
}
.cert-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  color: var(--ink-soft);
}
.cert-item input[type='file'] {
  padding: 8px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  background: #fff;
}
.cert-name {
  color: var(--leaf-dark);
  font-size: 12px;
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
.auth-note {
  margin: 10px 0 0;
  text-align: center;
  font-size: 12px;
  color: var(--muted);
}
</style>
