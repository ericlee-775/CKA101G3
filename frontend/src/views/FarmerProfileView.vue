<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import farmerApi from '@/api/farmer'
import authApi from '@/api/auth'
import cityDistrictApi from '@/api/cityDistrict'
import authStore from '@/stores/auth'

const router = useRouter()

const profile = ref(null)
const loadError = ref('')

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
})

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
  savingContact.value = true
  try {
    const updated = await farmerApi.updateContact({
      farmerPhoneNum: contact.value.farmerPhoneNum,
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
  if (!confirm('修改這些欄位會重新送審，期間狀態會變為待審核。確定要送出嗎？')) return
  savingApply.value = true
  try {
    const updated = await farmerApi.resubmit({
      farmName: apply.value.farmName,
      farmAddress: apply.value.farmAddress,
      districtId: apply.value.districtId ? Number(apply.value.districtId) : null,
    })
    profile.value = updated
    applyMsg.value = '已重新送審，請等待管理員審核'
  } catch (e) {
    applyErr.value = e.message || '送審失敗'
  } finally {
    savingApply.value = false
  }
}

async function changePassword() {
  pwMsg.value = ''
  pwErr.value = ''
  if (pw.value.newPassword.length < 8) {
    pwErr.value = '新密碼至少需 8 個字元'
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

async function logout() {
  try {
    await authApi.logout()
  } catch {
    // ignore
  }
  authStore.clear()
  router.push('/farmer/login')
}
</script>

<template>
  <main class="profile-page">
    <div class="profile-wrap">
      <header class="profile-head">
        <h1>🌾 小農中心</h1>
        <button class="btn-ghost" @click="logout">登出</button>
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
            <p class="danger-note">送出後將重新送審，期間審核狀態會變為待審核。（證明文件如需更換請洽管理流程）</p>
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
              <span>目前密碼</span><input v-model="pw.oldPassword" type="password" />
            </label>
            <label><span>新密碼</span><input v-model="pw.newPassword" type="password" placeholder="至少 8 個字元" /></label>
            <label><span>確認新密碼</span><input v-model="pw.confirm" type="password" /></label>
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
.btn-ghost {
  padding: 8px 16px;
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
</style>
