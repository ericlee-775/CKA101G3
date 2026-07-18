<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import memberOrdersApi from '@/api/memberOrders'
import cityDistrictApi from '@/api/cityDistrict'
import cartStore from '@/stores/cart'
import noImage from '@/assets/no-image.svg'

const router = useRouter()

const loading   = ref(true)
const loadError = ref('')
const submitting = ref(false)

const info = ref(null)   // 後端 checkout-info 提供的資料 (userName, phone, district, detailAddress, list farmerGroup, totalAmount, myCoupons, recommendedCouponId)
const districts = ref([])  // 縣市區下拉選單的全部選項


const form = reactive({     // 使用者可編輯的表單欄位 (提交後端 ProductOrderRequestDTO)
  recipientName: '',
  recipientPhone: '',
  districtId: null,
  detailAddress: '',
  coupon: '',
  cardNo: '',
  cardExpMonth: '',
  cardExpYear: '',
  cardCvc: '',
})

// 接收各欄位錯誤訊息
const errors = reactive({
  recipientName: '',
  recipientPhone: '',
  districtId: '',
  detailAddress: '',
  cardNo: '',
  cardExp: '',
  cardCvc: '',
})

// 後端回傳的錯誤訊息
const formError = ref('')

onMounted(loadCheckout)

async function loadCheckout(){
  loading.value = true
  loadError.value = ''
  try {
    const [checkoutInfo, districtList] = await Promise.all([
      memberOrdersApi.checkoutInfo(),
      cityDistrictApi.listAll(),
    ])

    info.value = checkoutInfo
    districts.value = districtList || []

    // 後端傳來的資料設為表單預設值
    form.recipientName = checkoutInfo.userName ?? ''
    form.recipientPhone = checkoutInfo.phone ?? ''
    form.districtId    = checkoutInfo.district?.districtId ?? null
    form.detailAddress = checkoutInfo.detailAddress ?? ''
    form.coupon        = checkoutInfo.recommendedCouponId ?? ''
  } catch (e) {
    loadError.value = e.message || '載入資料失敗'
  } finally {
    loading.value = false
  }
}

// 找出目前選中的那張券物件 (用來顯示折抵多少)
const selectedCoupon = computed(() =>
  info.value?.myCoupons?.find(c => c.couponId === form.coupon) || null
)
// 折抵金額 (沒選券就是 0)
const discount = computed(() => selectedCoupon.value?.amount ?? 0)
// 實付 = 總額 − 折抵 (不會小於 0)
const finalPayment = computed(() =>
  Math.max(0, (info.value?.totalAmount ?? 0) - discount.value)
)


// 輸入格式驗證
const rules = {
  recipientName: (v) => {
    if (!v.trim()) return '請填寫收件人姓名'
    if (v.length > 50) return '姓名過長 (上限 50 字)'
    return ''
  },
  recipientPhone: (v) => {
    if (!v.trim()) return '請填寫收件人電話'
    if (!/^09\d{8}$/.test(v)) return '手機格式錯誤 (09 開頭共 10 碼)'
    return ''
  },
  districtId: (v) => (!v ? '請選擇縣市區域' : ''),
  detailAddress: (v) => {
    if (!v.trim()) return '請填寫收件地址'
    if (v.length > 80) return '地址過長 (上限 80 字)'
    return ''
  },
}

// 驗證信用卡格式
function cardErrors(){
  const { cardNo, cardExpMonth, cardExpYear, cardCvc } = form
  const out = { cardNo: '', cardExp: '', cardCvc: '' }

  if (!cardNo.trim()) { out.cardNo = '請填寫卡號' }
  else if (!/^\d{16}$/.test(cardNo.replace(/\s/g, ''))) { out.cardNo = '卡號需為 16 位數字' }

  if (!cardExpMonth || !cardExpYear) { out.cardExp = '請選擇有效期' }
  else if (isCardExpired(cardExpMonth, cardExpYear)) { out.cardExp = '卡片已過期' }

  if (!cardCvc.trim()) { out.cardCvc = '請填寫檢查碼' }
  else if (!/^\d{3}$/.test(cardCvc)) { out.cardCvc = '檢查碼需為 3 位數字' }

  return out
}

function isCardExpired(cardExpMonth, cardExpYear){
  const now = new Date()
  const curYear = now.getFullYear()
  const curMonth = now.getMonth() + 1
  const y = Number(cardExpYear), m = Number(cardExpMonth)

  return y < curYear || (y === curYear && m < curMonth)
}

// 單欄驗證 (@blur)
function validateField(key){
  if (key === 'card') {
    Object.assign(errors, cardErrors())
    return
  }
  if (rules[key]) { errors[key] = rules[key](form[key])}
}

// 打字時清掉該欄紅字 (@input, @change)
function clearError(key){
  if (key === 'card') {
    errors.cardNo = ''
    errors.cardExp = ''
    errors.cardCvc = ''
    return
  }
  errors[key] = ''
}

// 全部欄位驗證 (送出前)
function validateForm(){
  Object.keys(rules).forEach(k => { errors[k] = rules[k](form[k]) })
  Object.assign(errors, cardErrors())
  return Object.values(errors).every(m => !m) // 每一則 error 都是空字串才算通過驗證
}

// 確認結帳按鈕 disabled 用
const isFormValid = computed(() => {
  const basicOk = Object.keys(rules).every(k => !rules[k](form[k]))
  const card = cardErrors()
  return basicOk && !card.cardNo && !card.cardExp && !card.cardCvc
})


// 確認結帳後的小浮窗 (按下結帳後先跳出的浮窗)
const showConfirm = ref(false)

function openConfirm(){
  if (!validateForm()) return
  formError.value = ''
  showConfirm.value = true
}

const selectedDistrictText = computed(() => {
  const d = districts.value.find(d => d.districtId === form.districtId)
  return d ? `${d.zipcode} ${d.cityName}${d.distName}` : ''
})


async function submitCheckout(){
  
  showConfirm.value = false   // 關浮窗

  const request = {
    recipientName: form.recipientName.trim(),
    recipientPhone: form.recipientPhone.trim(),
    districtId: form.districtId,
    detailAddress: form.detailAddress.trim(),
    coupon: form.coupon || null,     // 空字串轉 null（沒用券）
  }

  submitting.value = true
  try {
    await memberOrdersApi.checkout(request)
    // 後端下單時已 clearCart，前端要重讀才不會停在結帳前的舊資料
    await cartStore.reload()
    router.replace('/member/orders')
  } catch (e) {
    formError.value = e.message || '結帳失敗，請稍後再試'
  } finally {
    submitting.value = false
  }
}



// 金額轉換
const fmm = (n) => (n == null ? '-' : `NT$ ${Number(n).toLocaleString('zh-TW')}`)

// 信用卡有效期下拉選單
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))
const yearOptions = computed(() => {
  const thisYear = new Date().getFullYear()
  return Array.from({ length: 10 }, (_, i) => String(thisYear + i))
})

function onImgError(e){
  if (e.target.dataset.fallback) return
  e.target.dataset.fallback = '1'
  e.target.src = noImage
}

</script>

<template>
  <section class="checkout-page">
    <header class="checkout-head">
      <h1>🧾 訂單確認</h1>
    </header>

    <p v-if="loading" class="state-box">載入中…</p>
    <div v-else-if="loadError" class="state-box">
      <span class="state-icon">😵</span>
      <p>{{ loadError }}</p>
      <button class="btn-ghost" @click="loadCheckout">重新載入</button>
    </div>

    <template v-else>
      <article class="panel">
        <h2 class="panel-title">訂購資訊</h2>
        
        <div class="field-row">
          <label class="field">
            <span class="field-label">收件人姓名</span>
            <input
              class="input" :class="{ 'input--error': errors.recipientName }"
              v-model="form.recipientName"
              @blur="validateField('recipientName')"
              @input="clearError('recipientName')"
              maxlength="50" placeholder="收件人姓名" />
            <span v-if="errors.recipientName" class="field-err">{{ errors.recipientName }}</span>
          </label>

          <label class="field">
            <span class="field-label">收件人電話</span>
            <input
              class="input" :class="{ 'input--error': errors.recipientPhone }"
              v-model="form.recipientPhone"
              @blur="validateField('recipientPhone')"
              @input="clearError('recipientPhone')"
              maxlength="10" inputmode="numeric" placeholder="09 開頭手機號碼" />
            <span v-if="errors.recipientPhone" class="field-err">{{ errors.recipientPhone }}</span>
          </label>
        </div>

        <div class="field-row">
          <label class="field">
            <span class="field-label">縣市 / 區域</span>
            <select
              class="input" :class="{ 'input--error': errors.districtId }"
              v-model="form.districtId"
              @blur="validateField('districtId')"
              @change="clearError('districtId')">
              <option :value="null" disabled>請選擇</option>
              <option v-for="d in districts" :key="d.districtId" :value="d.districtId">
                {{ d.cityName }} {{ d.distName }}
              </option>
            </select>
            <span v-if="errors.districtId" class="field-err">{{ errors.districtId }}</span>
          </label>

          <label class="field field--grow">
            <span class="field-label">詳細地址</span>
            <input
              class="input" :class="{ 'input--error': errors.detailAddress }"
              v-model="form.detailAddress"
              @blur="validateField('detailAddress')"
              @input="clearError('detailAddress')"
              maxlength="80" placeholder="路 / 街、門牌、樓層" />
            <span v-if="errors.detailAddress" class="field-err">{{ errors.detailAddress }}</span>
          </label>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">
          付款資訊
          <span class="panel-note">💳 信用卡 </span>
        </h2>

        <div class="field-row">
          <label class="field field--grow">
            <span class="field-label">卡號</span>
            <input
              class="input" :class="{ 'input--error': errors.cardNo }"
              v-model="form.cardNo"
              @blur="validateField('card')"
              @input="clearError('card')"
              placeholder="**** **** **** ****" inputmode="numeric" />
            <span v-if="errors.cardNo" class="field-err">{{ errors.cardNo }}</span>
          </label>

          <div class="field field--exp">
            <span class="field-label">有效期</span>
            <div class="exp-row">
              <select
                class="input" :class="{ 'input--error': errors.cardExp }"
                v-model="form.cardExpMonth"
                @blur="validateField('card')"
                @change="clearError('card')">
                <option value="" disabled>月</option>
                <option v-for="m in monthOptions" :key="m" :value="m">{{ m }}</option>
              </select>
              <select
                class="input" :class="{ 'input--error': errors.cardExp }"
                v-model="form.cardExpYear"
                @blur="validateField('card')"
                @change="clearError('card')">
                <option value="" disabled>年</option>
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
              </select>
            </div>
            <span v-if="errors.cardExp" class="field-err">{{ errors.cardExp }}</span>
          </div>

          <label class="field field--sm">
            <span class="field-label">檢查碼</span>
            <input
              class="input" :class="{ 'input--error': errors.cardCvc }"
              v-model="form.cardCvc"
              @blur="validateField('card')"
              @input="clearError('card')"
              maxlength="3" placeholder="CVC" inputmode="numeric" />
            <span v-if="errors.cardCvc" class="field-err">{{ errors.cardCvc }}</span>
          </label>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">訂購明細</h2>
        <div v-for="g in info.farmerGroup" :key="g.farmerId" class="farmer-group">
          <header class="farmer-head">
            <span class="farmer-name">🧑‍🌾 {{ g.farmerName || `小農 #${g.farmerId}` }}</span>
          </header>
          <table class="item-table">
            <colgroup>
              <col style="width: 50%" />
              <col style="width: 18%" />
              <col style="width: 12%" />
              <col style="width: 20%" />
            </colgroup>
            <thead>
              <tr>
                <th>商品</th>
                <th>單價</th>
                <th>數量</th>
                <th>小計</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="it in g.items" :key="it.productId">
                <td class="item-name">
                  <img :src="`/api/products/${it.productId}/image`" @error="onImgError" alt="" class="item-img" />
                  <span>{{ it.productName }}</span>
                </td>
                <td>{{ fmm(it.price) }}</td>
                <td>{{ it.quantity }}</td>
                <td>{{ fmm(it.itemSubtotal) }}</td>
              </tr>
            </tbody>
          </table>
          <p class="farmer-subtotal">農場小計 {{ fmm(g.subtotal) }}</p>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">使用優惠券</h2>

        <select class="input" v-model="form.coupon">
          <option value="">不使用優惠券</option>
          <option v-for="c in info.myCoupons" :key="c.couponId" :value="c.couponId" :disabled="info.totalAmount < c.minSpending">
            {{ c.couponInfo }} (滿 {{ c.minSpending }}，折 {{ fmm(c.amount) }})
            <template v-if="info.totalAmount < c.minSpending">
              — 還差 {{ fmm(c.minSpending - info.totalAmount) }}
            </template>
          </option>
        </select>
        <p v-if="form.coupon && form.coupon === info.recommendedCouponId" class="hint">
          🎟️ 已為您套用最划算的優惠券
        </p>

        <dl class="sum-grid">
          <div class="sum-row"><dt>訂單總額</dt><dd>{{ fmm(info.totalAmount) }}</dd></div>
          <div class="sum-row"><dt>優惠折抵</dt><dd class="is-discount">– {{ fmm(discount) }}</dd></div>
          <div class="sum-row sum-row--total"><dt>應付金額</dt><dd>{{ fmm(finalPayment) }}</dd></div>
        </dl>
      </article>

      <p v-if="formError" class="msg-err">{{ formError }}</p>

      <footer class="checkout-foot">
        <span v-if="!isFormValid" class="hint-warn">請完整填寫上方欄位</span>
        <button
          class="btn-primary btn-lg"
          :disabled="submitting || !isFormValid"
          @click="openConfirm">
          {{ submitting ? '處理中…' : '確認結帳' }}
        </button>
      </footer>
    </template>

    <Teleport to="body">
      <Transition name="oc-fade">
        <div v-if="showConfirm" class="oc-overlay" @click.self="showConfirm = false">
          <div class="oc-card" role="dialog" aria-modal="true">
            <h3 class="oc-title">請確認訂單內容</h3>

            <section class="oc-block">
              <h4 class="oc-block-title">收件資訊</h4>
              <p class="oc-line">{{ form.recipientName }}　{{ form.recipientPhone }}</p>
              <p class="oc-line">{{ selectedDistrictText }}{{ form.detailAddress }}</p>
            </section>

            <section class="oc-block">
              <h4 class="oc-block-title">訂購明細</h4>
              <div v-for="g in info.farmerGroup" :key="g.farmerId" class="oc-farmer">
                <p class="oc-farmer-name">🧑‍🌾 {{ g.farmerName || `小農 #${g.farmerId}` }}</p>
                <p v-for="it in g.items" :key="it.productId" class="oc-item">
                  <span class="oc-item-name">{{ it.productName }} × {{ it.quantity }}</span>
                  <span class="oc-item-sub">{{ fmm(it.itemSubtotal) }}</span>
                </p>
              </div>
            </section>

            <section class="oc-block oc-sum">
              <p class="oc-line oc-line--row">
                <span>訂單總額</span><span>{{ fmm(info.totalAmount) }}</span>
              </p>
              <p v-if="discount > 0" class="oc-line oc-line--row is-discount">
                <span>優惠折抵</span><span>− {{ fmm(discount) }}</span>
              </p>
              <p class="oc-line oc-line--row oc-total">
                <span>應付金額</span><span>{{ fmm(finalPayment) }}</span>
              </p>
            </section>

            <div class="oc-actions">
              <button class="oc-btn oc-cancel" @click="showConfirm = false">返回修改</button>
              <button class="oc-btn oc-confirm" @click="submitCheckout">確認送出</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </section>
</template>


<style scoped>

.checkout-page *, .checkout-page *::before, .checkout-page *::after {
  box-sizing: border-box;
}
.checkout-page {
  padding: 32px clamp(18px, 4vw, 56px);
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.checkout-head h1 { margin: 0; font-size: 24px; color: var(--ink); }

/* 每個區塊一張卡 */
.panel {
  background: #fff; border: 1px solid var(--line); border-left: 4px solid var(--leaf);
  border-radius: 16px; box-shadow: var(--shadow);
  display: flex; flex-direction: column; gap: 16px; padding: 20px 22px;
}
.panel-title { margin: 0; font-size: 16px; color: var(--ink); display: flex; align-items: center; gap: 10px; }
.panel-note { font-size: 12px; font-weight: 400; color: var(--muted); }

/* 表單欄位 */
.field-row { display: flex; gap: 16px; flex-wrap: wrap; }
.field { display: flex; flex-direction: column; gap: 6px; flex: 1 1 180px; }
.field--grow { flex: 2 1 260px; }
.field--sm { flex: 0 1 120px; }
.field--exp { flex: 0 1 200px; }
.exp-row { display: flex; gap: 8px; }
.field-label { font-size: 12px; color: var(--muted); }
.input {
  padding: 9px 12px; border: 1px solid var(--line); border-radius: 10px;
  font-size: 14px; color: var(--ink); background: #fff; width: 100%;
}
.input:focus { outline: none; border-color: var(--leaf); }
.input:disabled { background: var(--paper); color: var(--ink-soft); }

/* 農場分組 */
.farmer-group { border: 1px solid var(--line); border-radius: 12px; padding: 12px 14px; background: var(--paper); }
.farmer-head { margin-bottom: 8px; }
.farmer-name { font-weight: 600; font-size: 14px; color: var(--leaf-dark); }
.farmer-subtotal { margin: 8px 0 0; text-align: right; font-size: 12px; color: var(--muted); }

.item-table { width: 100%; border-collapse: collapse; font-size: 14px; table-layout: fixed; }
.item-table th { text-align: left; font-size: 12px; color: var(--muted); font-weight: 500; padding-bottom: 6px; }
.item-table td { padding: 8px 0; border-top: 1px solid var(--line); color: var(--ink-soft); }
.item-name { display: flex; align-items: center; gap: 10px; }
.item-img { width: 40px; height: 40px; object-fit: cover; border-radius: 8px; background: var(--leaf-soft); }
.item-table th:not(:first-child),
.item-table td:not(:first-child) {
  text-align: right;
}

/* 金額結算 */
.sum-grid { margin: 4px 0 0; display: flex; flex-direction: column; gap: 8px; }
.sum-row { display: flex; justify-content: space-between; font-size: 14px; color: var(--ink-soft); }
.is-discount { color: #b5651d; }
.sum-row--total { border-top: 1px dashed var(--line); padding-top: 15px; font-weight: 700; color: var(--leaf-dark); align-items: baseline; }
.sum-row--total dt { font-size: 18px; }
.sum-row--total dd { font-size: 26px; }

/* 按鈕 */
/* 加上 align-items 讓提示文字和按鈕垂直置中對齊 */
.checkout-foot { display: flex; justify-content: flex-end; align-items: center; gap: 12px; }
.btn-lg { padding: 12px 32px; font-size: 16px; border-radius: 12px; }
.btn-primary { border: 1px solid var(--leaf); background: var(--leaf); color: #fff; cursor: pointer; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-ghost { padding: 7px 16px; border: 1px solid var(--line); border-radius: 10px; background: #fff; color: var(--ink-soft); cursor: pointer; font-size: 14px; text-decoration: none; }

.state-box { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 56px 24px; background: #fff; border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); color: var(--muted); text-align: center; }
.state-icon { font-size: 40px; }
.hint { color: var(--leaf-dark); font-size: 13px; margin: 0; }
.msg-err { color: #c0392b; font-size: 14px; text-align: center; }

.field-err { font-size: 12px; color: #c0392b; }   /* 欄位錯誤紅字 */
.input--error { border-color: #c0392b; }          /* 錯誤欄位紅框 */
.hint-warn { font-size: 13px; color: var(--muted); }

/* ===== 結帳確認浮窗 ===== */
.oc-overlay {
  position: fixed; inset: 0; z-index: 1000;
  display: grid; place-items: center; padding: 18px;
  background: rgba(30, 30, 25, 0.45);
}
.oc-card {
  width: 100%; max-width: 460px;
  max-height: 80vh; overflow-y: auto;   /* 明細多時可捲動，不會超出畫面 */
  background: #fff; border-radius: 16px;
  box-shadow: 0 18px 48px rgba(30, 25, 15, 0.28);
  padding: 24px; text-align: left;
}
.oc-title { margin: 0 0 16px; font-size: 18px; color: var(--ink); text-align: center; }

.oc-block { margin-bottom: 16px; }
.oc-block-title { margin: 0 0 8px; font-size: 13px; color: var(--muted); font-weight: 600; }
.oc-line { margin: 0 0 4px; font-size: 14px; color: var(--ink-soft); }
.oc-line--row { display: flex; justify-content: space-between; }

.oc-farmer { margin-bottom: 10px; }
.oc-farmer-name { margin: 0 0 4px; font-size: 13px; font-weight: 600; color: var(--leaf-dark); }
.oc-item { display: flex; justify-content: space-between; gap: 12px; margin: 0 0 3px; font-size: 14px; color: var(--ink-soft); }
.oc-item-name { flex: 1; }
.oc-item-sub { white-space: nowrap; }

.oc-sum { border-top: 1px dashed var(--line); padding-top: 12px; margin-bottom: 20px; }
.oc-total { font-size: 18px; font-weight: 700; color: var(--leaf-dark); margin-top: 6px; }

.oc-actions { display: flex; gap: 10px; }
.oc-btn { flex: 1; padding: 11px 16px; border-radius: 10px; font-size: 15px; cursor: pointer; border: 1px solid transparent; }
.oc-cancel { background: #fff; border-color: var(--line); color: var(--ink-soft); }
.oc-cancel:hover { border-color: var(--muted); }
.oc-confirm { background: var(--leaf); color: #fff; }
.oc-confirm:hover { background: var(--leaf-dark); }

/* 淡入 + 卡片微縮放 */
.oc-fade-enter-active, .oc-fade-leave-active { transition: opacity 0.18s ease; }
.oc-fade-enter-from, .oc-fade-leave-to { opacity: 0; }
.oc-fade-enter-active .oc-card, .oc-fade-leave-active .oc-card { transition: transform 0.18s ease; }
.oc-fade-enter-from .oc-card, .oc-fade-leave-to .oc-card { transform: scale(0.94); }

</style>