<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import farmerApi from '@/api/farmer'
import cityDistrictApi from '@/api/cityDistrict'
import PasswordInput from '@/components/PasswordInput.vue'
import TermsModal from '@/components/TermsModal.vue'
import { validateCertFile } from '@/utils/certFile'

const router = useRouter()

// 使用條款：需勾選同意才能送出申請
const agreed = ref(false)
const showTerms = ref(false)

// 表單欄位（對齊後端 FarmerRegisterRequest）
const farmName = ref('')          // 農場名稱
const email = ref('')
const phone = ref('')             // farmerPhoneNum
const farmAddress = ref('')       // 農場地址（後端必填）
const farmDesc = ref('')          // 農場介紹（後端必填）
const districtId = ref('')        // 行政區 id
const password = ref('')
const confirm = ref('')

// 經緯度（選填）：自動定位填入；拒絕/失敗留空亦可送出
const locLat = ref('')            // locLat
const locLong = ref('')           // locLong
const geoStatus = ref('')         // '' | detecting | ok | denied | unsupported

// 呼叫瀏覽器定位；允許→填入座標，拒絕/失敗→留空（不擋送出）
function detectLocation() {
  if (!('geolocation' in navigator)) {
    geoStatus.value = 'unsupported'
    return
  }
  geoStatus.value = 'detecting'
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      // 對齊後端 @Digits(fraction = 8)：固定 8 位小數
      locLat.value = pos.coords.latitude.toFixed(8)
      locLong.value = pos.coords.longitude.toFixed(8)
      geoStatus.value = 'ok'
    },
    () => {
      geoStatus.value = 'denied'   // 使用者拒絕或定位失敗：座標留空
    },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}

const geoHint = computed(() => {
  switch (geoStatus.value) {
    case 'detecting': return '定位中…請允許瀏覽器存取位置'
    case 'ok': return '已自動填入目前位置座標，可手動微調'
    case 'denied': return '未取得定位權限，經緯度可留空，不影響送出申請'
    case 'unsupported': return '此瀏覽器不支援定位，經緯度可留空'
    default: return ''
  }
})

// 證明文件（三張皆必填）：農地、產品、身分
const certLand = ref(null)        // certFileLand
const certProduct = ref(null)     // certFileProduct
const certIdentity = ref(null)    // certFileIdentity

const certRefs = { land: certLand, product: certProduct, identity: certIdentity }

// 各檔案 input 的 DOM 參照：移除文件時用來清空原生 input，允許重選同一檔
const certLandEl = ref(null)
const certProductEl = ref(null)
const certIdentityEl = ref(null)
const certInputEls = { land: certLandEl, product: certProductEl, identity: certIdentityEl }

// 各證明文件的預覽圖 URL（由 URL.createObjectURL 產生，移除/替換/離開時需 revoke 釋放）
const certPreviews = reactive({ land: '', product: '', identity: '' })

// 設定預覽圖：先釋放舊 URL 再產生新的（file 為 null 則只清空）
function setPreview(key, file) {
  if (certPreviews[key]) URL.revokeObjectURL(certPreviews[key])
  certPreviews[key] = file ? URL.createObjectURL(file) : ''
}

// 使用者在該欄位選檔：驗證通過才存入對應 ref，並清掉舊錯誤
function onCertChange(key, event) {
  error.value = ''
  const target = certRefs[key]
  const file = event.target.files && event.target.files[0]
  if (!file) {
    target.value = null
    setPreview(key, null)
    return
  }
  const msg = validateCertFile(file)
  if (msg) {
    error.value = msg
    target.value = null
    setPreview(key, null)
    event.target.value = ''   // 驗證失敗清空，允許重選同一檔
    return
  }
  target.value = file
  setPreview(key, file)
}

// 移除已選檔案：清掉 ref、預覽圖與原生 input，允許重新上傳同一檔
function onCertRemove(key) {
  error.value = ''
  certRefs[key].value = null
  setPreview(key, null)
  const el = certInputEls[key].value
  if (el) el.value = ''
}

// 離開頁面時釋放所有預覽圖 URL，避免記憶體洩漏
onBeforeUnmount(() => {
  Object.keys(certPreviews).forEach((key) => {
    if (certPreviews[key]) URL.revokeObjectURL(certPreviews[key])
  })
})

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
  // 進頁即自動偵測位置（會跳出瀏覽器授權詢問）；拒絕不影響其他欄位
  detectLocation()
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
  // 後端 @Size(max = 50 / 200 / 2000)
  if (farmName.value.length > 50) {
    error.value = '農場名稱最多 50 字'
    return
  }
  if (farmAddress.value.length > 100) {
    error.value = '農場地址最多 100 字'
    return
  }
  if (farmDesc.value.length > 2000) {
    error.value = '農場介紹最多 2000 字'
    return
  }
  // 後端 @NotNull：所在區域必選
  if (!districtId.value) {
    error.value = '請選擇所在縣市與行政區'
    return
  }
  // 經緯度為選填：有填才驗範圍（對齊後端 -90~90 / -180~180），留空直接略過不擋送出
  if (locLat.value !== '' &&
      (isNaN(Number(locLat.value)) || Number(locLat.value) < -90 || Number(locLat.value) > 90)) {
    error.value = '緯度需介於 -90 ~ 90，或留空'
    return
  }
  if (locLong.value !== '' &&
      (isNaN(Number(locLong.value)) || Number(locLong.value) < -180 || Number(locLong.value) > 180)) {
    error.value = '經度需介於 -180 ~ 180，或留空'
    return
  }
  // 後端 @Pattern ^09\d{8}$：手機需 09 開頭共 10 碼（先去掉空白/連字號再驗）
  const phoneNorm = phone.value.replace(/[\s-]/g, '')
  if (!/^09\d{8}$/.test(phoneNorm)) {
    error.value = '手機號碼格式錯誤（需為 09 開頭共 10 碼）'
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
  if (password.value !== confirm.value) {
    error.value = '兩次輸入的密碼不一致'
    return
  }
  // 三張證明文件皆必填（格式/大小已在 onCertChange 用 validateCertFile 擋過）
  if (!certLand.value || !certProduct.value || !certIdentity.value) {
    error.value = '請上傳農地、產品與身分三項證明文件'
    return
  }
  // 需先閱讀並同意小農合作條款
  if (!agreed.value) {
    error.value = '請先閱讀並勾選同意小農合作條款與隱私權政策'
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
    fd.append('farmerPhoneNum', phoneNorm)
    fd.append('farmDesc', farmDesc.value)
    // districtId 為必填（後端 @NotNull），前面已驗證有值
    fd.append('districtId', Number(districtId.value))
    // 經緯度選填：有值才送，留空後端沿用 null
    if (locLat.value !== '') fd.append('locLat', locLat.value)
    if (locLong.value !== '') fd.append('locLong', locLong.value)
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

        <!-- 縣市 / 行政區（必選，後端 districtId @NotNull）-->
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

        <!-- 經緯度（選填）：自動定位填入，拒絕存取可留空 -->
        <div class="geo-group">
          <div class="geo-head">
            <span>座標（經緯度，選填）</span>
            <button type="button" class="geo-btn" @click="detectLocation"
                    :disabled="geoStatus === 'detecting'">
              {{ geoStatus === 'detecting' ? '定位中…' : '自動定位' }}
            </button>
          </div>
          <div class="auth-grid">
            <label>
              <span>緯度 (Lat)</span>
              <input v-model="locLat" type="text" inputmode="decimal" placeholder="自動偵測或留空" />
            </label>
            <label>
              <span>經度 (Long)</span>
              <input v-model="locLong" type="text" inputmode="decimal" placeholder="自動偵測或留空" />
            </label>
          </div>
          <small v-if="geoHint" class="geo-hint"
                 :class="{ 'geo-err': geoStatus === 'denied' || geoStatus === 'unsupported' }">
            {{ geoHint }}
          </small>
        </div>

        <label>
          <span>農場介紹</span>
          <textarea v-model="farmDesc" rows="3" placeholder="簡單介紹你的農場與產品"></textarea>
        </label>

        <!-- 證明文件（三張皆必填，JPG / PNG，單張 ≤ 2MB）-->
        <div class="cert-group">
          <p class="cert-title">證明文件（皆須上傳，JPG / PNG，單張 ≤ 5MB）</p>
          <label class="cert-item">
            <span>農地證明</span>
            <input ref="certLandEl" type="file" accept="image/jpeg,image/png" @change="onCertChange('land', $event)" />
            <small v-if="certLand" class="cert-name">
              已選：{{ certLand.name }}
              <button type="button" class="cert-remove" @click.prevent="onCertRemove('land')">移除</button>
            </small>
            <img v-if="certPreviews.land" :src="certPreviews.land" class="cert-preview" alt="農地證明預覽" />
          </label>
          <label class="cert-item">
            <span>產品證明</span>
            <input ref="certProductEl" type="file" accept="image/jpeg,image/png" @change="onCertChange('product', $event)" />
            <small v-if="certProduct" class="cert-name">
              已選：{{ certProduct.name }}
              <button type="button" class="cert-remove" @click.prevent="onCertRemove('product')">移除</button>
            </small>
            <img v-if="certPreviews.product" :src="certPreviews.product" class="cert-preview" alt="產品證明預覽" />
          </label>
          <label class="cert-item">
            <span>身分證明</span>
            <input ref="certIdentityEl" type="file" accept="image/jpeg,image/png" @change="onCertChange('identity', $event)" />
            <small v-if="certIdentity" class="cert-name">
              已選：{{ certIdentity.name }}
              <button type="button" class="cert-remove" @click.prevent="onCertRemove('identity')">移除</button>
            </small>
            <img v-if="certPreviews.identity" :src="certPreviews.identity" class="cert-preview" alt="身分證明預覽" />
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

        <!-- 使用條款：勾選同意 + 點連結開啟彈窗閱讀 -->
        <label class="agree">
          <input v-model="agreed" type="checkbox" />
          <span>
            我已閱讀並同意
            <button type="button" class="agree-link" @click="showTerms = true">
              《小農合作條款與隱私權政策》
            </button>
          </span>
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>
        <p v-if="done" class="auth-ok">申請成功！正在帶你前往登入…</p>

        <button class="auth-btn" type="submit" :disabled="loading || !agreed">
          {{ loading ? '送出中…' : '送出申請' }}
        </button>
      </form>

      <!-- 條款彈窗：按「我已閱讀並同意」會自動勾選上方同意框 -->
      <TermsModal v-model="showTerms" type="farmer" @agree="agreed = true" />

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
  width: 100%;
  box-sizing: border-box;   /* 含 padding 一起算寬，避免撐破容器 */
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
/* grid 子項預設 min-width:auto 會被輸入框最小寬撐開而溢出，改成可縮小 */
.auth-grid > label {
  min-width: 0;
}

/* 經緯度（選填）區塊 */
.geo-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border: 1px dashed var(--line);
  border-radius: 10px;
}
.geo-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
}
.geo-btn {
  padding: 6px 12px;
  border: 1px solid var(--leaf);
  border-radius: 8px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.geo-btn:hover {
  background: #fff;
}
.geo-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.geo-hint {
  color: var(--muted);
  font-size: 12px;
}
.geo-hint.geo-err {
  color: #b06a00;
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--leaf-dark);
  font-size: 12px;
}
.cert-remove {
  padding: 2px 8px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: #c0392b;
  font-size: 12px;
  cursor: pointer;
}
.cert-remove:hover {
  border-color: #c0392b;
}
.cert-preview {
  margin-top: 6px;
  max-width: 160px;
  max-height: 120px;
  border: 1px solid var(--line);
  border-radius: 8px;
  object-fit: cover;
}

/* 同意使用條款 */
.agree {
  flex-direction: row !important;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-soft);
  line-height: 1.6;
  cursor: pointer;
}
.agree input {
  width: auto;
  margin-top: 3px;
  accent-color: var(--leaf);
  flex: none;
}
.agree-link {
  padding: 0;
  border: none;
  background: none;
  color: var(--leaf);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}
.agree-link:hover {
  color: var(--leaf-dark);
}

.auth-error {
  margin: 0;
  color: #c0392b;
  font-size: 13px;
  white-space: pre-line;   /* 後端多欄位錯誤以 \n 換行顯示 */
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
