<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import memberApi from '@/api/member'
import cityDistrictApi from '@/api/cityDistrict'
import authStore from '@/stores/auth'
import PasswordInput from '@/components/PasswordInput.vue'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()

const profile = ref(null)
const loadError = ref('')

// 編輯表單欄位
const form = ref({
  userName: '',
  userNickname: '',
  userPhoneNum: '',
  userAddress: '',
  birthday: '',
  districtId: '',
})

// 縣市 / 行政區
const districtList = ref([])
const selectedCity = ref('')

const cities = computed(() => [...new Set(districtList.value.map((d) => d.cityName))])
const districtsInCity = computed(() =>
  districtList.value.filter((d) => d.cityName === selectedCity.value)
)

// 各區塊的提示訊息
const profileMsg = ref('')
const profileErr = ref('')
const savingProfile = ref(false)

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
    const me = await memberApi.getMe()
    profile.value = me
    authStore.setUser(me, 'MEMBER')
    // 帶入表單
    form.value.userName = me.userName || ''
    form.value.userNickname = me.userNickname || ''
    form.value.userPhoneNum = me.userPhoneNum || ''
    form.value.userAddress = me.userAddress || ''
    form.value.birthday = me.birthday || ''
    form.value.districtId = me.districtId || ''
    selectedCity.value = me.cityName || ''
  } catch (e) {
    if (e.status === 401) {
      // 未登入 → 導去登入頁
      router.push('/login')
      return
    }
    loadError.value = e.message || '載入資料失敗'
  }
}

function onCityChange() {
  form.value.districtId = ''
}

async function saveProfile() {
  profileMsg.value = ''
  profileErr.value = ''
  savingProfile.value = true
  try {
    const updated = await memberApi.updateMe({
      userName: form.value.userName,
      userNickname: form.value.userNickname,
      userPhoneNum: form.value.userPhoneNum,
      userAddress: form.value.userAddress,
      birthday: form.value.birthday || null,
      districtId: form.value.districtId ? Number(form.value.districtId) : null,
    })
    profile.value = updated
    authStore.setUser(updated, 'MEMBER')
    profileMsg.value = '資料已更新'
  } catch (e) {
    profileErr.value = e.message || '更新失敗'
  } finally {
    savingProfile.value = false
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
    await memberApi.changePassword({
      oldPassword: pw.value.oldPassword,
      newPassword: pw.value.newPassword,
    })
    pwMsg.value = '密碼修改成功！'
    pw.value = { oldPassword: '', newPassword: '', confirm: '' }
  } catch (e) {
    pwErr.value = e.message || '密碼修改失敗'
  } finally {
    savingPw.value = false
  }
}

async function deleteAccount() {
  const ok = await confirm({
    title: '註銷帳號',
    message: '確定要註銷帳號嗎？此動作無法復原。',
    confirmText: '註銷',
    danger: true,
  })
  if (!ok) return
  try {
    await memberApi.deleteMe()
    authStore.clear()
    alert('帳號已註銷')
    router.push('/')
  } catch (e) {
    alert(e.message || '註銷失敗')
  }
}
</script>

<template>
  <main class="profile-page">
    <div class="profile-wrap">
      <!-- 標題：這頁現在是會員中心底下的「個人資料」分頁；登出鈕在左側選單 -->
      <header class="profile-head">
        <h1>個人資料</h1>
      </header>

      <p v-if="loadError" class="msg-err">{{ loadError }}</p>

      <template v-if="profile">
        <!-- 帳號摘要 -->
        <section class="card">
          <h2>帳號資訊</h2>
          <div class="info-grid">
            <div><span class="label">Email</span><span>{{ profile.email }}</span></div>
            <div><span class="label">狀態</span><span>{{ profile.userStatus }}</span></div>
            <div><span class="label">Email 驗證</span><span>{{ profile.emailVerified ? '已驗證' : '未驗證' }}</span></div>
            <div><span class="label">消費級距</span><span>{{ profile.spendingTier || '—' }}</span></div>
            <div><span class="label">登入方式</span><span>{{ profile.authProvider || 'LOCAL' }}</span></div>
          </div>
        </section>

        <!-- 編輯資料 -->
        <section class="card">
          <h2>個人資料</h2>
          <form class="form" @submit.prevent="saveProfile">
            <label><span>姓名</span><input v-model="form.userName" type="text" /></label>
            <label><span>暱稱</span><input v-model="form.userNickname" type="text" /></label>
            <label><span>電話</span><input v-model="form.userPhoneNum" type="tel" /></label>
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
                <select v-model="form.districtId" :disabled="!selectedCity">
                  <option value="">請選擇行政區</option>
                  <option v-for="d in districtsInCity" :key="d.districtId" :value="d.districtId">
                    {{ d.distName }}
                  </option>
                </select>
              </label>
            </div>
            <label><span>地址</span><input v-model="form.userAddress" type="text" /></label>
            <label><span>生日</span><input v-model="form.birthday" type="date" /></label>

            <p v-if="profileErr" class="msg-err">{{ profileErr }}</p>
            <p v-if="profileMsg" class="msg-ok">{{ profileMsg }}</p>
            <button class="btn" type="submit" :disabled="savingProfile">
              {{ savingProfile ? '儲存中…' : '儲存變更' }}
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

        <!-- 危險操作 -->
        <section class="card card-danger">
          <h2>註銷帳號</h2>
          <p class="danger-note">註銷後帳號將無法使用，此動作無法復原。</p>
          <button class="btn-danger" @click="deleteAccount">註銷我的帳號</button>
        </section>
      </template>
    </div>
  </main>
</template>

<style scoped>
/* 外距和置中交給 MemberLayout 的內容區管，這裡只限制表單寬度方便閱讀 */
.profile-wrap {
  max-width: 640px;
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
}
.card h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--ink);
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
.card-danger {
  border-color: #f0c9c4;
}
.danger-note {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--muted);
}
.btn-danger {
  padding: 10px 20px;
  border: 1px solid #c0392b;
  border-radius: 10px;
  background: #fff;
  color: #c0392b;
  font-size: 15px;
  cursor: pointer;
}
.btn-danger:hover {
  background: #c0392b;
  color: #fff;
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
