<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import farmerApi from '@/api/farmer'
import cityDistrictApi from '@/api/cityDistrict'
import authStore from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'
import { confirm } from '@/composables/useConfirm'
import { validateCertFile } from '@/utils/certFile'

const router = useRouter()

const profile = ref(null)
const loadError = ref('')

// 我的審核紀錄與已上傳文件（唯讀）
const reviews = ref([])
const reviewsError = ref('')
const certUrl = (reviewId, type) => farmerApi.certUrl(reviewId, type)

// 時間格式化（yyyy-MM-dd HH:mm）；無值回 '—'
function formatDateTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 縣市 / 行政區
const districtList = ref([])
const selectedCity = ref('')
const cities = computed(() => [...new Set(districtList.value.map((d) => d.cityName))])
const districtsInCity = computed(() =>
  districtList.value.filter((d) => d.cityName === selectedCity.value)
)

// 立即生效欄位（電話、農場介紹）
const contact = ref({ farmerPhoneNum: '', farmDesc: '' })
const contactMsg = ref('')
const contactErr = ref('')
const savingContact = ref(false)

// 重新送審欄位（會觸發重審）
const apply = ref({ farmName: '', farmAddress: '', districtId: '' })
const applyMsg = ref('')
const applyErr = ref('')
const savingApply = ref(false)

// 重新送審的證明文件（選填：沒選就沿用上一輪的圖片）
const certLand = ref(null)
const certProduct = ref(null)
const certIdentity = ref(null)
const certRefs = { land: certLand, product: certProduct, identity: certIdentity }

// 使用者選檔：驗證通過才存入對應 ref
function onCertChange(key, event) {
  applyErr.value = ''
  const target = certRefs[key]
  const file = event.target.files && event.target.files[0]
  if (!file) {
    target.value = null
    return
  }
  const msg = validateCertFile(file)
  if (msg) {
    applyErr.value = msg
    target.value = null
    event.target.value = ''   // 驗證失敗清空，允許重選同一檔
    return
  }
  target.value = file
}

// 改密碼
const pw = ref({ oldPassword: '', newPassword: '', confirm: '' })
const pwMsg = ref('')
const pwErr = ref('')
const savingPw = ref(false)

onMounted(async () => {
  try {
    districtList.value = await cityDistrictApi.listAll()
  } catch {
    districtList.value = []
  }
  await loadProfile()
  await loadReviews()
})

// 載入自己的審核紀錄（失敗只提示，不影響其他區塊）
async function loadReviews() {
  reviewsError.value = ''
  try {
    reviews.value = await farmerApi.getMyReviews()
  } catch (e) {
    reviews.value = []
    reviewsError.value = e.message || '審核紀錄載入失敗'
  }
}

async function loadProfile() {
  try {
    const me = await farmerApi.getMe()
    profile.value = me
    authStore.setUser(me, 'FARMER')
    contact.value.farmerPhoneNum = me.farmerPhoneNum || ''
    contact.value.farmDesc = me.farmDesc || ''
    apply.value.farmName = me.farmName || ''
    apply.value.farmAddress = me.farmAddress || ''
    apply.value.districtId = me.districtId || ''
    selectedCity.value = me.cityName || ''
  } catch (e) {
    if (e.status === 401) {
      router.push('/farmer/login')
      return
    }
    loadError.value = e.message || '載入資料失敗'
  }
}

function onCityChange() {
  apply.value.districtId = ''
}

async function saveContact() {
  contactMsg.value = ''
  contactErr.value = ''
  // 對齊後端：手機格式（有填才驗）、農場介紹長度
  const phone = contact.value.farmerPhoneNum.replace(/[\s-]/g, '')
  if (phone && !/^09\d{8}$/.test(phone)) {
    contactErr.value = '手機號碼格式錯誤（需為 09 開頭共 10 碼）'
    return
  }
  if (contact.value.farmDesc.length > 2000) {
    contactErr.value = '農場介紹最多 2000 字'
    return
  }
  savingContact.value = true
  try {
    const updated = await farmerApi.updateContact({
      farmerPhoneNum: phone,
      farmDesc: contact.value.farmDesc,
    })
    profile.value = updated
    contactMsg.value = '聯絡資訊已更新'
  } catch (e) {
    contactErr.value = e.message || '更新失敗'
  } finally {
    savingContact.value = false
  }
}

async function resubmit() {
  applyMsg.value = ''
  applyErr.value = ''
  if (!apply.value.farmName || !apply.value.farmAddress) {
    applyErr.value = '請填寫農場名稱與地址'
    return
  }
  if (apply.value.farmName.length > 50) { applyErr.value = '農場名稱最多 50 字'; return }
  if (apply.value.farmAddress.length > 100) { applyErr.value = '農場地址最多 100 字'; return }
  const ok = await confirm({
    title: '重新送審',
    message: '修改這些欄位會重新送審，期間狀態會變為待審核。確定要送出嗎？',
    confirmText: '送出送審',
  })
  if (!ok) return
  savingApply.value = true
  try {
    // 走 multipart/form-data：欄位名稱需對齊後端 FarmerResubmitRequest
    // 證明文件選填，沒選就不 append，後端會沿用上一輪的圖片
    const fd = new FormData()
    fd.append('farmName', apply.value.farmName)
    fd.append('farmAddress', apply.value.farmAddress)
    if (apply.value.districtId) fd.append('districtId', Number(apply.value.districtId))
    if (certLand.value) fd.append('certFileLand', certLand.value)
    if (certProduct.value) fd.append('certFileProduct', certProduct.value)
    if (certIdentity.value) fd.append('certFileIdentity', certIdentity.value)
    const updated = await farmerApi.resubmit(fd)
    profile.value = updated
    // 送出後清掉已選檔案，避免誤以為還會再送
    certLand.value = null
    certProduct.value = null
    certIdentity.value = null
    applyMsg.value = '已重新送審，請等待管理員審核'
    await loadReviews()   // 送審後多一輪，刷新審核紀錄
  } catch (e) {
    applyErr.value = e.message || '送審失敗'
  } finally {
    savingApply.value = false
  }
}

async function changePassword() {
  pwMsg.value = ''
  pwErr.value = ''
  if (pw.value.newPassword.length < 8 || pw.value.newPassword.length > 60) {
    pwErr.value = '新密碼長度需 8~60 字'
    return
  }
  if (!/[A-Za-z]/.test(pw.value.newPassword) || !/\d/.test(pw.value.newPassword)) {
    pwErr.value = '新密碼需同時包含英文字母與數字'
    return
  }
  if (pw.value.newPassword !== pw.value.confirm) {
    pwErr.value = '兩次輸入的新密碼不一致'
    return
  }
  savingPw.value = true
  try {
    await farmerApi.changePassword({
      oldPassword: pw.value.oldPassword,
      newPassword: pw.value.newPassword,
    })
    pwMsg.value = '密碼修改成功！其他裝置已登出'
    pw.value = { oldPassword: '', newPassword: '', confirm: '' }
  } catch (e) {
    pwErr.value = e.message || '密碼修改失敗'
  } finally {
    savingPw.value = false
  }
}
</script>

<template>
  <main class="profile-page">
    <div class="profile-wrap">
      <header class="profile-head">
        <h1>🌾 小農中心</h1>
      </header>

      <p v-if="loadError" class="msg-err">{{ loadError }}</p>

      <template v-if="profile">
        <!-- 帳號資訊 -->
        <section class="card">
          <h2>帳號資訊</h2>
          <div class="info-grid">
            <div><span class="label">Email</span><span>{{ profile.email }}</span></div>
            <div><span class="label">小農狀態</span><span>{{ profile.farmerStatus }}</span></div>
            <div><span class="label">審核狀態</span><span>{{ profile.reviewStatus || '—' }}</span></div>
            <div><span class="label">審核輪次</span><span>{{ profile.reviewRound ?? '—' }}</span></div>
          </div>
        </section>

        <!-- 審核紀錄與已上傳文件（唯讀）：含歷次重新送審 -->
        <section class="card">
          <h2>審核紀錄與文件</h2>
          <p v-if="reviewsError" class="msg-err">{{ reviewsError }}</p>
          <p v-else-if="!reviews.length" class="empty-note">尚無審核紀錄</p>
          <ul v-else class="review-list">
            <li v-for="r in reviews" :key="r.reviewId" class="review-item">
              <div class="review-head">
                <span class="round">第 {{ r.reviewRound }} 輪</span>
                <span class="badge" :class="'badge--' + (r.reviewStatus || '').toLowerCase()">{{ r.reviewStatus }}</span>
                <span class="review-date">{{ formatDateTime(r.submittedAt) }}</span>
              </div>
              <p v-if="r.rejectReason" class="reject-reason">退件理由：{{ r.rejectReason }}</p>
              <div class="cert-thumbs">
                <a v-if="r.hasCertLand" class="cert-thumb" :href="certUrl(r.reviewId, 'land')" target="_blank" rel="noopener">
                  <img :src="certUrl(r.reviewId, 'land')" alt="農地證明" /><span>農地證明</span>
                </a>
                <a v-if="r.hasCertProduct" class="cert-thumb" :href="certUrl(r.reviewId, 'product')" target="_blank" rel="noopener">
                  <img :src="certUrl(r.reviewId, 'product')" alt="產品證明" /><span>產品證明</span>
                </a>
                <a v-if="r.hasCertIdentity" class="cert-thumb" :href="certUrl(r.reviewId, 'identity')" target="_blank" rel="noopener">
                  <img :src="certUrl(r.reviewId, 'identity')" alt="身分證明" /><span>身分證明</span>
                </a>
                <span v-if="!r.hasCertLand && !r.hasCertProduct && !r.hasCertIdentity" class="empty-note">此輪無文件</span>
              </div>
            </li>
          </ul>
        </section>

        <!-- 立即生效：聯絡資訊 -->
        <section class="card">
          <h2>聯絡資訊<span class="tag">立即生效</span></h2>
          <form class="form" @submit.prevent="saveContact">
            <label><span>聯絡電話</span><input v-model="contact.farmerPhoneNum" type="tel" /></label>
            <label><span>農場介紹</span><textarea v-model="contact.farmDesc" rows="3"></textarea></label>
            <p v-if="contactErr" class="msg-err">{{ contactErr }}</p>
            <p v-if="contactMsg" class="msg-ok">{{ contactMsg }}</p>
            <button class="btn" type="submit" :disabled="savingContact">
              {{ savingContact ? '儲存中…' : '儲存' }}
            </button>
          </form>
        </section>

        <!-- 需重審：農場名稱 / 地址 -->
        <section class="card">
          <h2>農場資料<span class="tag tag-warn">需重新送審</span></h2>
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

            <!-- 證明文件（選填：不選則沿用上一輪的圖片，JPG / PNG，單張 ≤ 5MB）-->
            <div class="cert-group">
              <p class="cert-title">更換證明文件（選填，不選則沿用上一輪；JPG / PNG，單張 ≤ 5MB）</p>
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

            <p class="danger-note">送出後將重新送審，期間審核狀態會變為待審核。</p>
            <p v-if="applyErr" class="msg-err">{{ applyErr }}</p>
            <p v-if="applyMsg" class="msg-ok">{{ applyMsg }}</p>
            <button class="btn" type="submit" :disabled="savingApply">
              {{ savingApply ? '送出中…' : '重新送審' }}
            </button>
          </form>
        </section>

        <!-- 改密碼 -->
        <section class="card">
          <h2>修改密碼</h2>
          <form class="form" @submit.prevent="changePassword">
            <label v-if="profile.hasPassword">
              <span>目前密碼</span><PasswordInput v-model="pw.oldPassword" autocomplete="current-password" />
            </label>
            <label><span>新密碼</span><PasswordInput v-model="pw.newPassword" placeholder="至少 8 個字元" autocomplete="new-password" /></label>
            <label><span>確認新密碼</span><PasswordInput v-model="pw.confirm" autocomplete="new-password" /></label>
            <p v-if="pwErr" class="msg-err">{{ pwErr }}</p>
            <p v-if="pwMsg" class="msg-ok">{{ pwMsg }}</p>
            <button class="btn" type="submit" :disabled="savingPw">
              {{ savingPw ? '處理中…' : '更新密碼' }}
            </button>
          </form>
        </section>
      </template>
    </div>
  </main>
</template>

<style scoped>
.profile-page {
  padding: 40px 18px;
  min-height: 60vh;
}
.profile-wrap {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.profile-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.profile-head h1 {
  margin: 0;
  font-size: 26px;
  color: var(--ink);
}
.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
  border-top: 3px solid var(--leaf);
}
.card h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 10px;
}
.tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
}
.tag-warn {
  background: #fdecc8;
  color: #9a6a00;
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
.form select,
.form textarea {
  padding: 10px 13px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 15px;
  outline: none;
  font-family: inherit;
}
.form input:focus,
.form select:focus,
.form textarea:focus {
  border-color: var(--leaf);
}
.form textarea {
  resize: vertical;
}
.grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.btn {
  align-self: flex-start;
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
.danger-note {
  margin: 0;
  font-size: 12px;
  color: var(--muted);
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
/* ===== 審核紀錄與文件（唯讀） ===== */
.empty-note {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
}
.review-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.review-head .round {
  font-weight: 600;
  color: var(--ink);
}
.review-head .review-date {
  color: var(--muted);
  font-size: 12px;
  margin-left: auto;
}
.reject-reason {
  margin: 8px 0 0;
  font-size: 13px;
  color: #c0392b;
}
.cert-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.cert-thumb {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: var(--ink-soft);
  font-size: 12px;
}
.cert-thumb img {
  width: 88px;
  height: 88px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--line);
}
/* 審核狀態配色：待審(黃) / 審核中(藍) / 核准(綠) / 退件(紅) */
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
}
.badge--pending {
  background: #fef3c7;
  color: #92400e;
}
.badge--reviewing {
  background: #dbeafe;
  color: #1e40af;
}
.badge--approved {
  background: #dcfce7;
  color: #166534;
}
.badge--rejected {
  background: #fee2e2;
  color: #991b1b;
}
</style>
