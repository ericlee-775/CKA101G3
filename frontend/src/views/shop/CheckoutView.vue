<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import memberOrdersApi from '@/api/memberOrders'
import cityDistrictApi from '@/api/cityDistrict'
import cartStore from '@/stores/cart'
import { confirm } from '@/composables/useConfirm'
import noImage from '@/assets/no-image.svg'

const router = useRouter()

const loading   = ref(true)
const loadError = ref('')
const submitting = ref(false)

const info = ref(null)   // 後端 checkout-info 提供的唯讀資料 (userName, phone, district, detailAddress, list farmerGroup, totalAmount, usableCoupons, recommendedCouponId)
const districts = ref([])  // 縣市區下拉選單的全部選項

const form = reactive({     // 使用者可編輯的表單欄位 (提交後端 ProductOrderRequestDTO)
  districtId: null,
  detailAddress: '',
  coupon: '',
  cardNo: '',
  cardExp: '',
  cardCvc: '',
})

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
  info.value?.usableCoupons?.find(c => c.couponId === form.coupon) || null
)
// 折抵金額 (沒選券就是 0)
const discount = computed(() => selectedCoupon.value?.amount ?? 0)
// 實付 = 總額 − 折抵 (不會小於 0)
const finalPayment = computed(() =>
  Math.max(0, (info.value?.totalAmount ?? 0) - discount.value)
)


async function submitCheckout(){
  formError.value = ''

  if (!form.districtId)               { formError.value = '請選擇縣市區域'; return }
  if (!form.detailAddress.trim())     { formError.value = '請填寫收件地址'; return }
  if (form.detailAddress.length > 80) { formError.value = '地址過長（上限 80 字）'; return }

  const ok = await confirm({
    title: '確認結帳',
    message: `實付金額 ${fmm(finalPayment.value)}，確定要送出訂單嗎？`,
    confirmText: '確認結帳',
  })
  if (!ok) return

  const payload = {
    districtId: form.districtId,
    detailAddress: form.detailAddress.trim(),
    coupon: form.coupon || null,     // 空字串轉 null（沒用券）
  }

  submitting.value = true
  try {
    await memberOrdersApi.checkout(payload)
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
            <span class="field-label">姓名</span>
            <input class="input" :value="info.userName" disabled />
          </label>
          <label class="field">
            <span class="field-label">電話</span>
            <input class="input" :value="info.phone" disabled />
          </label>
        </div>

        <div class="field-row">
          <label class="field">
            <span class="field-label">縣市 / 區域</span>
            <select class="input" v-model="form.districtId">
              <option :value="null" disabled>請選擇</option>
              <option v-for="d in districts" :key="d.districtId" :value="d.districtId">
                {{ d.cityName }} {{ d.distName }}
              </option>
            </select>
          </label>
          <label class="field field--grow">
            <span class="field-label">詳細地址</span>
            <input class="input" v-model="form.detailAddress" maxlength="80" placeholder="路 / 街、門牌、樓層" />
          </label>
        </div>
      </article>

      <article class="panel">
        <h2 class="panel-title">
          付款資訊
          <span class="panel-note">💳 信用卡（版面佔位）</span>
        </h2>
        <div class="field-row">
          <label class="field field--grow">
            <span class="field-label">卡號</span>
            <input class="input" v-model="form.cardNo" placeholder="**** **** **** ****" inputmode="numeric" />
          </label>
          <label class="field field--sm">
            <span class="field-label">有效期</span>
            <input class="input" v-model="form.cardExp" placeholder="MM / YY" />
          </label>
          <label class="field field--sm">
            <span class="field-label">檢查碼</span>
            <input class="input" v-model="form.cardCvc" placeholder="CVC" inputmode="numeric" />
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
          <option v-for="c in info.usableCoupons" :key="c.couponId" :value="c.couponId">
            {{ c.couponInfo }}（滿 {{ c.minSpending }}，折 {{ fmm(c.amount) }}）
          </option>
        </select>
        <p v-if="form.coupon && form.coupon === info.recommendedCouponId" class="hint">
          ✅ 已為您套用最划算的優惠券
        </p>

        <dl class="sum-grid">
          <div class="sum-row"><dt>訂單總額</dt><dd>{{ fmm(info.totalAmount) }}</dd></div>
          <div class="sum-row"><dt>優惠折抵</dt><dd class="is-discount">− {{ fmm(discount) }}</dd></div>
          <div class="sum-row sum-row--total"><dt>應付金額</dt><dd>{{ fmm(finalPayment) }}</dd></div>
        </dl>
      </article>

      <p v-if="formError" class="msg-err">{{ formError }}</p>

      <footer class="checkout-foot">
        <button class="btn-primary btn-lg" :disabled="submitting" @click="submitCheckout">
          {{ submitting ? '處理中…' : '確認結帳' }}
        </button>
      </footer>
    </template>
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
.checkout-foot { display: flex; justify-content: flex-end; }
.btn-lg { padding: 12px 32px; font-size: 16px; border-radius: 12px; }
.btn-primary { border: 1px solid var(--leaf); background: var(--leaf); color: #fff; cursor: pointer; }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-ghost { padding: 7px 16px; border: 1px solid var(--line); border-radius: 10px; background: #fff; color: var(--ink-soft); cursor: pointer; font-size: 14px; text-decoration: none; }

.state-box { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 56px 24px; background: #fff; border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); color: var(--muted); text-align: center; }
.state-icon { font-size: 40px; }
.hint { color: var(--leaf-dark); font-size: 13px; margin: 0; }
.msg-err { color: #c0392b; font-size: 14px; text-align: center; }
</style>