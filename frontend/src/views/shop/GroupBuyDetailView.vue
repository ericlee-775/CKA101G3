<script setup>
// 團購詳情頁（不分層：資料抓取、狀態、畫面全寫在這一個元件裡，用封裝過的 groupBuyApi）。
// 這個元件對應兩條路由（見 router/index.js），依進來的網址決定要打哪支 API：
//
//   1) /group-buys/:groupBuyId（name: group-buy-detail）—— 這筆團購已經存在
//      GET /api/groupBuy/{groupBuyId} → GroupBuyDetailDTO（productId/商品名/原價/團購價/達標金額/
//      開團時間/截止時間/取貨地點/狀態）。DTO 現在有帶 productId，圖片直接用它打
//      GET /api/products/{productId}/image，不用再靠列表頁用 query 傳了。
//
//   2) /group-buys/product/:productId（name: group-buy-host）—— 「可發起」的商品，還沒有任何團購紀錄
//      後端 /api/groupBuy/consumer/list?type=available 改成查「支援團購的商品」而不是既有團購，
//      回傳的每一筆 groupBuyId 都是 null，沒有 id 可以查 GroupBuyDetailDTO，
//      所以這裡改打 GET /api/products/{productId}（商品本身的資料）組出一個「假的」團購物件顯示。
//
// 依團購狀態顯示對應的操作（兩者都需要以一般會員身分登入，MemberGroupBuyController 用 session 認證）：
//   沒有 groupBuyId（可發起商品）或 status === 'pending' → 「我要發起團購」，打 POST /api/member/groupBuy/hostCreate/{productId}
//     達標金額至少 1、開團時間不能早於今天、截止時間至少要比開團時間晚 5 天（前端擋，後端也有各自的驗證）。
//   status === 'open'（開團中）→「我要參加團購」，打 POST /api/member/groupBuy/joinGroupBuy/{groupBuyId}
//     這支不需要額外的 productId，body 其他欄位直接沿用已載入的 groupBuy 資料。
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import groupBuyApi from '@/api/groupBuy'
import memberGroupBuyApi from '@/api/memberGroupBuy'
import { listFarms } from '@/api/farm'
import { groupBuyStatusInfo } from '@/utils/groupBuyStatus'
import authStore from '@/stores/auth'
import noImage from '@/assets/no-image.svg'

const route = useRoute()
const router = useRouter()
const isMember = computed(() => authStore.isMember)
// 走「可發起商品」那條路由進來的：沒有既有團購紀錄，只有商品資料。
const isHostOnly = computed(() => route.name === 'group-buy-host')

// 團購詳情（GroupBuyDetailDTO）。還沒抓到前是 null。
const groupBuy = ref(null)
const loading = ref(true)
const error = ref('')

// 農場：DTO 只給 farmName，沒有 farmerId，但農場詳情頁需要 farmerId。
// 做法是抓 GET /api/farms 全部農場清單，用 farmName 對照出 farmerId。
// 對照不到（例如同名農場、或清單抓失敗）就不給連結，農場名稱純顯示文字。
const farmerId = ref(null)
async function resolveFarmerId(farmName) {
  farmerId.value = null
  if (!farmName) return
  try {
    const farms = await listFarms()
    const matched = farms.find((f) => f.farmName === farmName)
    if (matched) farmerId.value = matched.farmerId
  } catch {
    // 抓不到農場清單就不給連結，不影響團購頁其他內容。
  }
}

const FALLBACK_IMAGE = noImage
const imageUrl = ref('')

async function loadImage(productId) {
  if (!productId) return
  try {
    const res = await fetch(`/api/products/${productId}/image`)
    if (!res.ok) return
    const blob = await res.blob()
    imageUrl.value = URL.createObjectURL(blob)
  } catch {
    // 抓圖失敗就維持預設圖。
  }
}

function revokeImageUrl() {
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value)
}

// 把價格顯示成「NT$ 220」。
function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

// 把時間戳顯示成「2026/03/12」。
function formatDate(datetime) {
  if (!datetime) return '—'
  return new Date(datetime).toLocaleDateString('zh-TW')
}

// 「可發起商品」模式：打商品 API，組一個跟 GroupBuyDetailDTO 同形狀的物件給畫面共用。
async function loadHostOnlyProduct(productId) {
  const res = await fetch(`/api/products/${productId}`)
  if (!res.ok) {
    throw new Error(res.status === 404 ? '找不到這個商品' : `伺服器回應 ${res.status}`)
  }
  const product = await res.json()
  groupBuy.value = {
    groupBuyId: null,
    productName: product.productName,
    groupPrice: product.groupPrice,
    retailPrice: product.retailPrice,
    description: product.description,
    targetAmount: null,
    openDatetime: null,
    ddlDatetime: null,
    pickupAddress: null,
    status: null,
    // 商品 API（ProductDetailDTO）本身就有帶 farmName，可發起頁面也能顯示農場名稱。
    farmName: product.farmName,
  }
}

async function loadGroupBuy() {
  loading.value = true
  error.value = ''
  groupBuy.value = null
  farmerId.value = null
  revokeImageUrl()
  imageUrl.value = ''
  // 重新進頁面時，把之前展開的表單/訊息都收掉，避免顯示上一筆團購的殘留狀態。
  showHostForm.value = false
  showJoinForm.value = false
  hostMsg.value = ''
  joinMsg.value = ''

  try {
    if (isHostOnly.value) {
      const productId = route.params.productId
      await loadHostOnlyProduct(productId)
      await loadImage(productId)
      resolveFarmerId(groupBuy.value.farmName)
    } else {
      groupBuy.value = await groupBuyApi.getOne(route.params.groupBuyId)
      await loadImage(groupBuy.value.productId)
      resolveFarmerId(groupBuy.value.farmName)
    }
  } catch (e) {
    error.value = e.status === 404 || /查無|找不到/.test(e.message || '')
      ? (isHostOnly.value ? '找不到這個商品' : '找不到這個團購')
      : e.message || '無法載入資料，請稍後再試。'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'group-buys' })
}

// ========== 發起團購（狀態 pending 時顯示）==========
const showHostForm = ref(false)
const hostForm = ref({ targetAmount: null, openDatetime: '', ddlDatetime: '', pickupAddress: '' })
const hostSubmitting = ref(false)
const hostMsg = ref('')

// 日期輸入用的 YYYY-MM-DD 字串（純本地日期組字串，避開 toISOString 的時區位移問題）。
function toDateInputValue(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
// 開團時間不能選今天以前。
const todayStr = ref(toDateInputValue(new Date()))
// 截止時間至少要比「目前選的開團時間」晚 5 天；還沒選開團時間就以今天起算。
const minDdlStr = computed(() => {
  const base = new Date(hostForm.value.openDatetime || todayStr.value)
  base.setDate(base.getDate() + 5)
  return toDateInputValue(base)
})
// 達標金額的最低門檻：後端規定不得低於「團購價 × 2」。前端先擋一次並顯示提示，
// 後端 hostRequest() 也會再驗一次（低於時回「達標金額不得低於{n}元」），兩邊都擋。
const minTargetAmount = computed(() => {
  const gp = Number(groupBuy.value?.groupPrice)
  return gp > 0 ? gp * 2 : null
})

function toggleHostForm() {
  showHostForm.value = !showHostForm.value
  hostMsg.value = ''
}

async function submitHostCreate() {
  hostMsg.value = ''
  // 可發起商品模式下 productId 是路由參數；既有團購模式下 productId 已經在 GroupBuyDetailDTO 裡。
  const productId = route.params.productId ?? groupBuy.value?.productId
  if (!productId) {
    hostMsg.value = '這個團購沒有帶入商品資訊，請從「可發起」列表頁點進來才能發起。'
    return
  }
  const { targetAmount, openDatetime, ddlDatetime, pickupAddress } = hostForm.value
  if (!targetAmount || !openDatetime || !ddlDatetime || !pickupAddress) {
    hostMsg.value = '請完整填寫達標金額、開團/截止時間與取貨地址。'
    return
  }
  if (openDatetime < todayStr.value) {
    hostMsg.value = '開團時間不能早於今天。'
    return
  }
  if (ddlDatetime < minDdlStr.value) {
    hostMsg.value = '截止時間至少要在開團時間 5 天後。'
    return
  }
  if (minTargetAmount.value != null && targetAmount < minTargetAmount.value) {
    hostMsg.value = `達標金額不得低於 ${formatPrice(minTargetAmount.value)}。`
    return
  }

  hostSubmitting.value = true
  try {
    await memberGroupBuyApi.hostCreate(productId, { targetAmount, openDatetime, ddlDatetime, pickupAddress })
    hostMsg.value = '申請已送出，等待小農審核。'
    showHostForm.value = false
  } catch (e) {
    hostMsg.value = e.message || '申請失敗，請稍後再試。'
  } finally {
    hostSubmitting.value = false
  }
}

// ========== 加入團購與結帳（狀態 open 時顯示）==========
const showJoinForm = ref(false)
const joinBuyQty = ref(1)
const joinSubmitting = ref(false)
const joinMsg = ref('')

// 結帳資訊：純前端假資料，只做格式驗證，不會送給後端 API（joinGroupBuy 沒有這幾個欄位）。
const joinPayment = ref({ cardNumber: '', expiry: '', cvv: '' })

function toggleJoinForm() {
  showJoinForm.value = !showJoinForm.value
  joinMsg.value = ''
}

// 驗證結帳欄位格式；回傳錯誤訊息字串，沒有錯誤就回傳空字串。
function validatePayment() {
  if (!/^\d{16}$/.test(joinPayment.value.cardNumber)) {
    return '信用卡卡號請輸入 16 碼數字。'
  }
  if (!/^\d{2}\/\d{2}$/.test(joinPayment.value.expiry)) {
    return '有效期限請輸入正確格式（MM/YY）。'
  }
  if (!/^\d{3}$/.test(joinPayment.value.cvv)) {
    return '安全驗證碼請輸入 3 碼數字。'
  }
  return ''
}

async function submitJoinGroupBuy() {
  joinMsg.value = ''
  if (!joinBuyQty.value || joinBuyQty.value <= 0) {
    joinMsg.value = '請輸入正確的購買數量。'
    return
  }
  const paymentError = validatePayment()
  if (paymentError) {
    joinMsg.value = paymentError
    return
  }

  joinSubmitting.value = true
  try {
    await memberGroupBuyApi.joinGroupBuy(groupBuy.value.groupBuyId, {
      buyQty: joinBuyQty.value,
      productName: groupBuy.value.productName,
      target: groupBuy.value.targetAmount,
      groupPrice: groupBuy.value.groupPrice,
      ddlDatetime: groupBuy.value.ddlDatetime,
      pickupAddress: groupBuy.value.pickupAddress,
    })
    joinMsg.value = '參加成功！'
    showJoinForm.value = false
    joinPayment.value = { cardNumber: '', expiry: '', cvv: '' }
  } catch (e) {
    joinMsg.value = e.message || '參加失敗，請稍後再試。'
  } finally {
    joinSubmitting.value = false
  }
}

onMounted(loadGroupBuy)
onUnmounted(revokeImageUrl)
// 從一個詳情頁換到另一個（例如之後加了「相關團購」連結）時，元件不會重建，靠 watch 重抓。
// 兩種路由的識別參數不同（groupBuyId / productId），兩個都要 watch。
watch(() => [route.params.groupBuyId, route.params.productId], loadGroupBuy)
</script>

<template>
  <main class="page">
    <!-- 載入中 -->
    <p v-if="loading" class="state">載入中…</p>

    <!-- 載入失敗 -->
    <div v-else-if="error" class="state state--error">
      <p>😢 {{ error }}</p>
      <div class="state__actions">
        <button type="button" @click="loadGroupBuy">重新載入</button>
        <button type="button" class="ghost" @click="goBack">回團購列表</button>
      </div>
    </div>

    <!-- 團購詳情 -->
    <article v-else-if="groupBuy" class="detail">
      <section class="gallery">
        <img
          class="gallery__main"
          :src="imageUrl || FALLBACK_IMAGE"
          :alt="groupBuy.productName"
          @error="($event) => ($event.target.src = FALLBACK_IMAGE)"
        />
      </section>

      <section class="info">
        <!-- 可發起商品沒有 status（null），固定顯示「可發起」；其餘照狀態對照表 -->
        <span v-if="groupBuy.status" class="badge" :class="groupBuyStatusInfo(groupBuy.status).className">
          {{ groupBuyStatusInfo(groupBuy.status).text }}
        </span>
        <span v-else class="badge status--pending">可發起</span>

        <h1 class="info__name">{{ groupBuy.productName }}</h1>

        <!-- 農場名稱：對照得出 farmerId 就做成連結導到農場頁，對照不到就純顯示文字 -->
        <p v-if="groupBuy.farmName" class="info__farm">
          🌾
          <RouterLink
            v-if="farmerId != null"
            class="info__farm-link"
            :to="{ name: 'farm-detail', params: { farmerId } }"
          >{{ groupBuy.farmName }}</RouterLink>
          <span v-else>{{ groupBuy.farmName }}</span>
        </p>

        <p class="info__price">
          團購價 {{ formatPrice(groupBuy.groupPrice) }}
          <span v-if="groupBuy.retailPrice != null" class="info__retail">原價 {{ formatPrice(groupBuy.retailPrice) }}</span>
        </p>

        <!-- 可發起商品還沒有目標金額/時間/取貨地點這些資料，改顯示商品描述 -->
        <template v-if="groupBuy.groupBuyId == null">
          <p class="info__desc">
            {{ groupBuy.description || '這項商品支援團購，目前還沒有人發起，成為第一個發起的人吧！' }}
          </p>
          <p class="info__notice">達標以後小農將會致電通知團購主配達時間，請留意來電</p>
        </template>
        <dl v-else class="info__meta">
          <div class="info__meta-row">
            <dt>達標金額</dt>
            <dd>{{ formatPrice(groupBuy.targetAmount) }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>開團時間</dt>
            <dd>{{ formatDate(groupBuy.openDatetime) }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>截止時間</dt>
            <dd>{{ formatDate(groupBuy.ddlDatetime) }}</dd>
          </div>
          <div class="info__meta-row">
            <dt>取貨地點</dt>
            <dd>{{ groupBuy.pickupAddress || '—' }}</dd>
          </div>
        </dl>

        <!-- 未登入會員：提示登入，不顯示發起/參加表單 -->
        <p v-if="!isMember" class="login-hint">
          請先<RouterLink to="/login">登入會員</RouterLink>才能發起或參加團購。
        </p>

        <!-- 可發起：沒有 groupBuyId，或狀態為 pending，都顯示「我要發起團購」 -->
        <div v-else-if="groupBuy.groupBuyId == null || groupBuy.status === 'pending'" class="action-box">
          <button type="button" class="btn" @click="toggleHostForm">
            {{ showHostForm ? '取消' : '我要發起團購' }}
          </button>
          <form v-if="showHostForm" class="action-form" @submit.prevent="submitHostCreate">
            <label>
              達標金額<span v-if="minTargetAmount != null" class="action-form__hint"></span>
              <input type="number" :min="minTargetAmount || 1" v-model.number="hostForm.targetAmount" />
            </label>
            <label>開團時間 <input type="date" :min="todayStr" v-model="hostForm.openDatetime" /></label>
            <label>截止時間 <input type="date" :min="minDdlStr" v-model="hostForm.ddlDatetime" /></label>
            <label>取貨地址 <input type="text" v-model="hostForm.pickupAddress" /></label>
            <button type="submit" class="btn" :disabled="hostSubmitting">送出申請</button>
          </form>
          <p v-if="hostMsg" class="action-msg">{{ hostMsg }}</p>
        </div>

        <!-- 開團中：狀態為 open 才顯示「加入團購與結帳」 -->
        <div v-else-if="groupBuy.status === 'open'" class="action-box">
          <button type="button" class="btn" @click="toggleJoinForm">
            {{ showJoinForm ? '取消' : '加入團購與結帳' }}
          </button>
          <form v-if="showJoinForm" class="action-form" @submit.prevent="submitJoinGroupBuy">
            <label>購買數量 <input type="number" min="1" v-model.number="joinBuyQty" /></label>
            <p class="action-form__subhead">結帳資訊</p>
            <label>
              信用卡卡號
              <input
                type="text"
                inputmode="numeric"
                maxlength="16"
                placeholder="16 碼數字"
                v-model="joinPayment.cardNumber"
              />
            </label>
            <label>
              有效期限（MM/YY）
              <input
                type="text"
                inputmode="numeric"
                maxlength="5"
                placeholder="MM/YY"
                v-model="joinPayment.expiry"
              />
            </label>
            <label>
              安全驗證碼
              <input
                type="text"
                inputmode="numeric"
                maxlength="3"
                placeholder="3 碼數字"
                v-model="joinPayment.cvv"
              />
            </label>
            <button type="submit" class="btn" :disabled="joinSubmitting">確認送出</button>
          </form>
          <p v-if="joinMsg" class="action-msg">{{ joinMsg }}</p>
        </div>

        <button type="button" class="back-link" @click="goBack">← 回團購列表</button>
      </section>
    </article>
  </main>
</template>

<style scoped>
.page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
}

/* ---------- 載入 / 錯誤狀態 ---------- */
.state {
  text-align: center;
  color: var(--muted);
  padding: 60px 0;
}
.state__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 12px;
}
.state button {
  padding: 8px 18px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  cursor: pointer;
}
.state button.ghost {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--ink);
}

/* ---------- 詳情主體：左圖右字 ---------- */
.detail {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 40px;
  align-items: start;
}
@media (max-width: 720px) {
  .detail {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.gallery__main {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 16px;
  background: var(--line);
  display: block;
}

/* ---------- 文字資訊 ---------- */
.info__name {
  font-size: 26px;
  color: var(--ink);
  margin: 12px 0 16px;
}
.info__farm {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--ink-soft);
}
.info__farm-link {
  color: var(--leaf-dark);
  font-weight: 600;
  text-decoration: none;
}
.info__farm-link:hover {
  text-decoration: underline;
}
.info__price {
  font-size: 24px;
  font-weight: 700;
  color: var(--leaf-dark);
  margin: 0 0 20px;
}
.info__retail {
  margin-left: 10px;
  font-size: 14px;
  font-weight: 400;
  color: var(--muted);
  text-decoration: line-through;
}
.info__notice {
  margin: 10px 0 0;
  padding: 10px 14px;
  background: #fff7ed;
  color: #c2410c;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
}

.badge {
  display: inline-block;
  padding: 4px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}
.status--open { background: #ecfdf3; color: #15803d; }
.status--success { background: #eff6ff; color: #1d4ed8; }
.status--failed { background: #f3f4f6; color: #6b7280; }
.status--cancelled { background: #f3f4f6; color: #6b7280; }
.status--pending { background: #fff7ed; color: #c2410c; }

.info__desc {
  color: var(--ink-soft);
  line-height: 1.7;
  border-top: 1px solid var(--line);
  padding-top: 16px;
  margin: 0;
}

.info__meta {
  border-top: 1px solid var(--line);
  padding-top: 16px;
  margin: 0;
}
.info__meta-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed var(--line);
  font-size: 14px;
}
.info__meta-row dt {
  color: var(--muted);
}
.info__meta-row dd {
  margin: 0;
  color: var(--ink);
  font-weight: 500;
  text-align: right;
}

.back-link {
  margin-top: 28px;
  background: none;
  border: none;
  color: var(--leaf-dark);
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}
.back-link:hover {
  text-decoration: underline;
}

/* ---------- 登入提示 / 發起・參加團購 ---------- */
.login-hint {
  margin: 20px 0 0;
  padding: 12px 16px;
  background: var(--leaf-soft);
  border-radius: 10px;
  color: var(--ink-soft);
  font-size: 14px;
}
.login-hint a {
  color: var(--leaf-dark);
  font-weight: 600;
}
.action-box {
  margin-top: 20px;
}
.btn {
  padding: 9px 22px;
  border: none;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.btn:hover:not(:disabled) {
  background: var(--leaf-dark);
}
.btn:disabled {
  opacity: 0.6;
  cursor: default;
}
.action-form {
  margin-top: 14px;
  padding: 16px;
  background: var(--leaf-soft);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 320px;
}
.action-form label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: var(--ink-soft);
}
.action-form__subhead {
  margin: 4px 0 -2px;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
  font-size: 12px;
  color: var(--muted);
}
.action-form__hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--muted);
}
.action-form input {
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 14px;
}
.action-form input:focus {
  outline: none;
  border-color: var(--leaf);
}
.action-form .btn {
  align-self: flex-start;
}
.action-msg {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--leaf-dark);
}
</style>
