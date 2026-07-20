<script setup>
// 小農後台：商品管理（待接後端 API）,商品管理（清單／改價／上下架）
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { toast } from '@/composables/useToast'
import { categoryIcon } from '@/constants/categoryIcons'

const products = ref([])
// 新增商品彈窗開關：按「＋ 新增商品」設 true 跳出來，關閉/取消設回 false
const showAddModal = ref(false)
const loading = ref(true)
const error = ref('')
const categoryErr = ref('')
const categories = ref([])
const selectedMainCat = ref('')
const newProduct = ref({ productName: '', subCatClassId: '', retailPrice: '', groupPrice: '', unitPricingMeasure: '', isGroupBuy: false, description: '' })
const selectedImage = ref(null)
// 商品內圖（多張，詳情頁顯示）；送出時封面會固定排在第一張
const selectedImages = ref([])
// 圖片預覽網址（URL.createObjectURL 產生的本機暫存網址；換圖/送出後要 revoke 釋放記憶體）
const coverPreview = ref('')
const imagePreviews = ref([])
// 對到 template 的兩個 file input：<input type="file"> 不受 v-model 控制，
// 送出成功後要靠這兩個 ref 手動清掉畫面上殘留的檔名
const coverInputEl = ref(null)
const imagesInputEl = ref(null)
const addMsg = ref('')
const addErr = ref('')
const adding = ref(false)
const mainCategories = computed(() => [...new Map(categories.value.map((s) => [s.productMainCatId, s.productMainCatName]))])
const subCategoriesInMain = computed(() => categories.value.filter((s) => s.productMainCatId === selectedMainCat.value))

// ===== 純前端分頁：一頁 5 筆，不用重打 API，直接從 products 切一段出來 =====
const currentPage = ref(1)
const pageSize = 5
// 目前這一頁該顯示的商品：算出「起點」，往後切 5 筆
const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return products.value.slice(start, start + pageSize)
})
// 總共幾頁：商品總數除以 5、無條件進位；至少是 1 頁，避免 0/0 的怪狀況
const totalPages = computed(() => Math.ceil(products.value.length / pageSize) || 1)
// 頁碼按鈕要顯示的數字清單：[1, 2, 3, ...totalPages]
const pageNumbers = computed(() => {
  const pages = []
  for (let i = 1; i <= totalPages.value; i++) pages.push(i)
  return pages
})


async function loadProducts() {

  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/farmer/products/list')
    if (!res.ok) {

      throw new Error(`伺服器回應${res.status}`)
    }
    products.value = await res.json()

  } catch (e) {
    error.value = e.message || '無法載入商品,請稍後再試'
  } finally {
    loading.value = false
  }

}

async function savePrice(product) {

  const id = product.productId
  try {
    const res = await fetch(`/api/farmer/products/${id}`, {

      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ retailPrice: Number(product.retailPrice) }),

    })
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    toast('已儲存')
  } catch (e) {
    error.value = e.message || '無法存入商品資訊,請稍後再試'
  }
}

async function changeStatus(product, status) {

  const id = product.productId;
  try {
    const res = await fetch(`/api/farmer/products/${id}`, {

      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ status: status }),
    })
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    toast('已儲存')
    loadProducts()
  } catch (e) {
    error.value = e.message || '無法存入商品資訊,請稍後再試'
  }

}

async function loadCategories() {


  error.value = ''
  try {
    const res = await fetch('/api/products/categories')
    if (!res.ok) {

      throw new Error(`伺服器回應${res.status}`)
    }
    categories.value = await res.json()

  } catch (e) {
    categoryErr.value = e.message || '無法載入商品,請稍後再試'
  }

}

function onFileChange(event) {

  const file = event.target.files[0]
  selectedImage.value = file
  // 產生本機預覽網址前，先釋放上一張的，避免記憶體越吃越多（同 blog 的 filePreview 做法）
  if (coverPreview.value) URL.revokeObjectURL(coverPreview.value)
  coverPreview.value = file ? URL.createObjectURL(file) : ''

}

// 內圖多選：FileList 不是真的陣列，先轉成陣列存起來（同 blog 的 onAlbumChange 做法）
function onFilesChange(event) {
  imagePreviews.value.forEach((url) => URL.revokeObjectURL(url))
  selectedImages.value = Array.from(event.target.files || [])
  imagePreviews.value = selectedImages.value.map((f) => URL.createObjectURL(f))
}

async function addProduct() {
  addMsg.value = ''
  addErr.value = ''

  // 送出前把「所有」擋得下來的問題一次收集起來、合併成一句顯示，
  // 不再一項一項擋（後端仍是最終防線，這裡擋不到的後端會擋）。
  // 注意：訊息文字要跟後端 ProductInsertDTO 的驗證訊息一字不差——改任一邊記得同步另一邊
  const errors = []
  if (!newProduct.value.subCatClassId) errors.push('請選擇商品分類')
  if (!newProduct.value.productName.trim()) errors.push('商品名稱不可為空')
  // 價格要是 0 以上的「整數」：Number.isInteger 連 10.5 這種小數也擋（後端欄位是 Integer，放過去只會炸出看不懂的英文錯誤）
  if (newProduct.value.retailPrice === '') {
    errors.push('請輸入零售價')
  } else if (!Number.isInteger(Number(newProduct.value.retailPrice)) || Number(newProduct.value.retailPrice) < 0) {
    errors.push('零售價不可為負數，請輸入0以上的整數')
  }
  if (newProduct.value.groupPrice !== ''
    && (!Number.isInteger(Number(newProduct.value.groupPrice)) || Number(newProduct.value.groupPrice) < 0)) {
    errors.push('團購價不可為負數，請輸入0以上的整數')
  }
  if (newProduct.value.isGroupBuy && newProduct.value.groupPrice === '') {
    errors.push('開放團購時必須填寫團購價')
  }
  // 團購價不得高於或等於零售價：後端 service 也會擋（雙保險）
  if (newProduct.value.groupPrice !== '' && newProduct.value.retailPrice !== ''
    && Number(newProduct.value.groupPrice) >= Number(newProduct.value.retailPrice)) {
    errors.push('團購價不得高於或等於零售價')
  }
  if (!newProduct.value.unitPricingMeasure.trim()) errors.push('計價單位不可為空，例如：包/300g')

  // 圖片大小：數字要跟 application.properties 的 multipart 上限一致（單檔 5MB、整筆請求 10MB），
  // 前端先擋，不然使用者只會看到看不懂的「伺服器回應413」
  const MB = 1024 * 1024
  const cover = selectedImage.value || selectedImages.value[0] || null
  const gallery = selectedImages.value.filter((f) => f !== cover)
  const allFiles = cover ? [cover, ...gallery] : []
  if (allFiles.some((f) => f.size > 5 * MB)) {
    errors.push('圖片太大，單張最多 5MB')
  }
  // 封面實際會送兩份（列表縮圖一份＋詳情第一張一份），總量要照實算
  const totalSize = (cover ? cover.size * 2 : 0) + gallery.reduce((sum, f) => sum + f.size, 0)
  if (totalSize > 10 * MB) {
    errors.push('圖片總量超過 10MB，請減少張數或改用較小的圖')
  }

  if (errors.length > 0) {
    // 用換行符接起來，配合 .msg-err 的 white-space: pre-line，每個錯誤各佔一行（由上到下＝欄位順序）
    addErr.value = errors.join('\n')
    return
  }

  try {

    const fd = new FormData()
    fd.append('productName', newProduct.value.productName)
    fd.append('subCatClassId', newProduct.value.subCatClassId)
    fd.append('retailPrice', newProduct.value.retailPrice)
    fd.append('groupPrice', newProduct.value.groupPrice)
    fd.append('unitPricingMeasure', newProduct.value.unitPricingMeasure)
    fd.append('isGroupBuy', newProduct.value.isGroupBuy)
    fd.append('description', newProduct.value.description)
    // 封面＋內圖（cover/gallery 在上面大小檢查時已算好）：
    // 封面存 PRODUCT_DETAIL 當列表縮圖，內圖固定把封面排第一張，維持「詳情第一張＝封面」
    if (cover) {
      fd.append('productImage', cover)
      fd.append('productImages', cover)
      for (const f of gallery) { fd.append('productImages', f) }
    }

    adding.value = true
    const res = await fetch(`/api/farmer/products`, {
      method: 'POST',
      credentials: 'include',
      body: fd
    })
    if (!res.ok) {
      // 讀後端回傳的文字內容（例如「驗證失敗」），沒有的話才退回顯示狀態碼。
      // 後端把多個欄位錯誤用「、」串成一句，這裡換成換行符，讓它們也一行一句顯示
      const msg = await res.text()
      throw new Error(msg.replaceAll('、', '\n') || `伺服器回應${res.status}`)
    }

    addMsg.value = '已新增'
    loadProducts()
    newProduct.value = { productName: '', subCatClassId: '', retailPrice: '', groupPrice: '', unitPricingMeasure: '', isGroupBuy: false, description: '' }
    selectedMainCat.value = '' // 大分類回到「請選擇大分類」
    selectedImage.value = null
    selectedImages.value = []
    // 預覽也要一起清掉並釋放
    if (coverPreview.value) URL.revokeObjectURL(coverPreview.value)
    imagePreviews.value.forEach((url) => URL.revokeObjectURL(url))
    coverPreview.value = ''
    imagePreviews.value = []
    // file input 不受 v-model 控制，畫面上的檔名要用 ref 手動清
    if (coverInputEl.value) coverInputEl.value.value = ''
    if (imagesInputEl.value) imagesInputEl.value.value = ''
  } catch (e) {
    addErr.value = e.message || '無法新增商品,請稍後再試'
  }
  finally {
    adding.value = false

  }

}

// Esc 鍵關閉彈窗，照抄 ConfirmDialog.vue 的做法：
// 彈窗開著時才掛鍵盤監聽，關閉時移除，避免沒開彈窗時 Esc 也被攔截
function onModalKeydown(e) {
  if (e.key === 'Escape') showAddModal.value = false
}
watch(showAddModal, (open) => {
  if (open) {
    // 每次打開先清掉上一輪的成功/錯誤訊息，不然會看到過期的「已新增」
    addMsg.value = ''
    addErr.value = ''
    window.addEventListener('keydown', onModalKeydown)
  } else {
    window.removeEventListener('keydown', onModalKeydown)
  }
})

// 換大分類就把子分類清掉：不清的話畫面看起來空白，
// 但 subCatClassId 還存著上一個大類的子類 id，送出會變成「新大類配舊子類」
watch(selectedMainCat, () => {
  newProduct.value.subCatClassId = ''
})
onUnmounted(() => window.removeEventListener('keydown', onModalKeydown))

onMounted(loadProducts)
onMounted(loadCategories)
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>📦 商品管理</h1>
    </header>

    <!-- 商品清單卡片：現在是整個版面唯一的主角，標題旁邊放「新增商品」按鈕 -->
    <section class="card">
      <div class="card-head">
        <h2>目前商品</h2>
        <button type="button" class="btn" @click="showAddModal = true">＋ 新增商品</button>
      </div>

      <p v-if="loading" class="state">載入中..</p>

      <div v-else-if="error" class="state state-error">
        <p class="msg-err">{{ error }}</p>
        <button type="button" class="btn-ghost" @click="loadProducts">重新載入</button>
      </div>

      <p v-else-if="products.length === 0" class="state">目前沒有商品。</p>

      <div v-else class="product-list">
        <article v-for="(product, index) in pagedProducts" :key="product.productId" class="product-row"
          :class="{ 'product-row--inactive': product.status !== 'ACTIVE' }">
          <div class="product-row__head">
            <h3>{{ product.productName }}</h3>
            <!-- 上架/下架用不同顏色的小標籤，一眼就能分辨狀態 -->
            <span class="tag" :class="{ 'tag-warn': product.status !== 'ACTIVE' }">{{ product.status }}</span>
          </div>
          <!-- 「第幾項」不能只看 index：index 每一頁都會重新從 0 算，
               要加上「前面幾頁跳過的筆數」才是這筆商品在整份清單裡真正的排名 -->
          <p class="product-row__unit">
            第{{ (currentPage - 1) * pageSize + index + 1 }}項・{{ product.unitPricingMeasure }}
          </p>

          <div class="product-row__actions">
            <!-- 已下架的商品改價沒有意義，把改價相關控件一併停用，整列只留「上架」一個動作 -->
            <input type="number" v-model="product.retailPrice" class="price-input"
              :disabled="product.status !== 'ACTIVE'">
            <button type="button" class="btn" :disabled="product.status !== 'ACTIVE'"
              @click="savePrice(product)">存價格</button>
            <!-- 只顯示「反向動作」那一顆：上架中→給下架鈕、已下架→給上架鈕。
                 「上架」用實心主要按鈕：它是灰列上唯一可用的動作，要夠顯眼 -->
            <button v-if="product.status === 'ACTIVE'" type="button" class="btn-ghost"
              @click="changeStatus(product, 'INACTIVE')">下架</button>
            <button v-else type="button" class="btn" @click="changeStatus(product, 'ACTIVE')">上架</button>
          </div>
        </article>

        <!-- 分頁控制：只有 1 頁時不用顯示上一頁/下一頁，畫面比較乾淨 -->
        <div v-if="totalPages > 1" class="pagination">
          <button type="button" class="btn-ghost" :disabled="currentPage === 1" @click="currentPage--">
            上一頁
          </button>
          <!-- 頁碼下拉選單：選了哪一頁，v-model 直接把 currentPage 設成那個數字，馬上跳頁 -->
          <select v-model="currentPage" class="page-select">
            <option v-for="page in pageNumbers" :key="page" :value="page">第 {{ page }} 頁</option>
          </select>
          <button type="button" class="btn-ghost" :disabled="currentPage === totalPages" @click="currentPage++">
            下一頁
          </button>
        </div>
      </div>
    </section>

    <!-- 新增商品彈窗：平常不顯示，按上面「＋ 新增商品」才跳出來。
         結構照抄 ConfirmDialog.vue：Teleport 到 body 避免被版面卡住、
         Transition 做淡入淡出、遮罩層 @click.self 點背景關閉。-->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
          <div class="modal-card" role="dialog" aria-modal="true">
            <div class="modal-head">
              <h2>新增商品</h2>
              <button type="button" class="modal-close" @click="showAddModal = false" aria-label="關閉">✕</button>
            </div>

            <form class="form" @submit.prevent="addProduct">
              <div class="grid2">
                <label>
                  <span>大分類</span>
                  <select v-model="selectedMainCat">
                    <option value="">請選擇大分類</option>
                    <!-- 大分類前掛 icon（emoji），對照表在 constants/categoryIcons.js -->
                    <option v-for="[id, name] in mainCategories" :key="id" :value="id">{{ categoryIcon(name) }} {{ name
                      }}</option>
                  </select>
                </label>
                <label>
                  <span>子分類</span>
                  <select v-model="newProduct.subCatClassId" :disabled="!selectedMainCat">
                    <option value="">請選擇子類別</option>
                    <option v-for="s in subCategoriesInMain" :key="s.subCatClassId" :value="s.subCatClassId">
                      {{ s.subCatClassName }}
                    </option>
                  </select>
                </label>
              </div>

              <label>
                <span>商品名稱</span>
                <input type="text" v-model="newProduct.productName">
              </label>

              <div class="grid2">
                <label>
                  <span>零售價</span>
                  <input type="number" v-model="newProduct.retailPrice">
                </label>
                <label>
                  <span>團購價（選填）</span>
                  <input type="number" v-model="newProduct.groupPrice">
                </label>
              </div>

              <label>
                <span>計價單位</span>
                <input type="text" v-model="newProduct.unitPricingMeasure" placeholder="例如：包/300g">
              </label>

              <label>
                <span>商品描述（選填）</span>
                <input type="text" v-model="newProduct.description">
              </label>

              <label class="checkbox-row">
                <input type="checkbox" v-model="newProduct.isGroupBuy">
                <span>開放團購</span>
              </label>

              <label>
                <span>封面圖（選填，商品列表顯示）</span>
                <input ref="coverInputEl" type="file" accept="image/*" @change="onFileChange">
              </label>
              <img v-if="coverPreview" class="cover-preview" :src="coverPreview" alt="封面預覽">

              <label>
                <span>商品內圖（可多張，選填，詳情頁顯示；封面會自動排第一張）</span>
                <input ref="imagesInputEl" type="file" accept="image/*" multiple @change="onFilesChange">
              </label>
              <div v-if="imagePreviews.length" class="thumb-list">
                <img v-for="url in imagePreviews" :key="url" :src="url" alt="內圖預覽">
              </div>

              <!-- 用固定高度的容器包住訊息，不管有沒有訊息、訊息多長，
                   這塊空間都在，下面的送出按鈕才不會被推來推去 -->
              <div class="form-msg">
                <p v-if="categoryErr" class="msg-err">{{ categoryErr }}</p>
                <p v-if="addMsg" class="msg-ok">{{ addMsg }}</p>
                <p v-if="addErr" class="msg-err">{{ addErr }}</p>
              </div>

              <button type="submit" class="btn" :disabled="adding">
                {{ adding ? '新增中…' : '新增商品' }}
              </button>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
/* 整頁外框：留白＋限制最大寬度並置中，跟 FarmerProfileView 同一種「單欄置中」手感。
   用 flex column + gap 排列底下兩張卡片（商品清單卡、新增商品卡），不用自己一個個加 margin。*/
.farmer-page {
  padding: 32px 24px;
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-head h1 {
  margin: 0;
  font-size: 26px;
  color: var(--ink);
}

/* 卡片：全站共用的樣式（白底、淡邊框、陰影、頂部一條主題綠），
   兩張卡片（商品清單／新增商品）都套這個 class，看起來才會像同一個系統。*/
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
}

/* 卡片標題那一行：文字靠左、「新增商品」按鈕靠右，兩端對齊 */
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-head h2 {
  margin: 0;
}

/* 載入中／空清單的置中提示文字（顏色用次要文字色，不搶主要內容的視覺） */
.state {
  text-align: center;
  color: var(--muted);
  padding: 24px 0;
}

.state-error {
  padding: 0;
}

/* 表單/操作的成功、失敗訊息：綠色=成功、紅色=失敗，全站統一用這兩個 class */
.msg-ok {
  margin: 0;
  color: var(--leaf);
  font-size: 13px;
}

.msg-err {
  margin: 0;
  color: #c0392b;
  font-size: 13px;
  white-space: pre-line;
  /* 讓訊息裡的 \n 真的換行：多個錯誤各佔一行 */
}

/* 訊息容器：min-height 抓「合併好幾則錯誤」時大概會佔的高度，
   平常沒有訊息時這塊空間依然保留，按鈕位置才不會一直跳動 */
.form-msg {
  min-height: 40px;
}

/* ===== 商品清單 ===== */

/* 每筆商品之間的垂直間距 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 單筆商品的外框：用淡邊框把它跟卡片背景區隔出來，不需要再加一層陰影（太重複） */
.product-row {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px 16px;
}

/* 已下架的商品：灰底＋標題文字變淡。
   刻意不用 opacity 蓋整列——那樣唯一要讓人按的「上架」鈕也會跟著變淡 */
.product-row--inactive {
  background: #f6f6f3;
}

/* .product-row__head h3 也設了 color，優先權跟這裡打平；
   多疊一層 .product-row__head 提高優先權，才不會被後面那條蓋掉 */
.product-row--inactive .product-row__head h3,
.product-row--inactive .product-row__unit {
  color: var(--muted);
}

/* 停用狀態的改價輸入框：灰底＋禁止游標，跟可用狀態做出區隔 */
.price-input:disabled {
  background: #efefec;
  color: var(--muted);
  cursor: not-allowed;
}

/* 商品名稱 + 狀態標籤 排在同一行、左右對齊 */
.product-row__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.product-row__head h3 {
  margin: 0;
  font-size: 16px;
  color: var(--ink);
}

.product-row__unit {
  margin: 4px 0 12px;
  font-size: 13px;
  color: var(--muted);
}

/* 價格輸入框＋三顆按鈕排一列，螢幕太窄時自動換行（flex-wrap） */
.product-row__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* 狀態小標籤：綠色代表上架、橘色代表下架，
   跟 FarmerProfileView 審核狀態的 .tag/.tag-warn 用同一套配色邏輯 */
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

/* 分頁控制列：上一頁／目前頁數／下一頁，置中排列 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 4px;
}

/* 頁碼下拉選單：跟其他小型輸入框同一種尺寸感，選了哪頁 v-model 直接跳過去 */
.page-select {
  padding: 6px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink-soft);
  font-size: 13px;
  outline: none;
}

.page-select:focus {
  border-color: var(--leaf);
}

/* 商品清單裡改價格用的輸入框，故意比表單其他欄位窄，因為只填一個數字 */
.price-input {
  width: 90px;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 14px;
}

.price-input:focus {
  outline: none;
  border-color: var(--leaf);
}

/* ===== 新增商品表單 ===== */

/* 表單整體：一路往下排，欄位之間留固定間距 */
.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 每個欄位是「標籤文字 + 輸入框」上下排列 */
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

/* 兩欄並排（大分類/子分類、零售價/團購價），畫面太窄時改回單欄，見下面 @media */
.grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

/* checkbox 那一行不用直排：勾選框和文字放同一行比較好讀。
   用「.form .checkbox-row」而不是單純「.checkbox-row」，
   是為了讓這行的優先權蓋過上面 .form label 設的 flex-direction: column。*/
.form .checkbox-row {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.checkbox-row input {
  width: auto;
}

/* ===== 圖片預覽 ===== */

/* 封面預覽：單張稍大，一眼確認選對圖 */
.cover-preview {
  max-width: 160px;
  border-radius: 10px;
  border: 1px solid var(--line);
}

/* 內圖預覽：一排小縮圖，太多時自動換行 */
.thumb-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.thumb-list img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--line);
}

/* 主要動作按鈕（存價格／新增商品）：實心主題綠，最顯眼 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: var(--leaf);
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

/* :not(:disabled) 排除停用按鈕：disabled 元素仍會觸發 :hover，
   不排除的話「存價格」下架時滑過去照樣變深色，讓人以為還能點 */
.btn:not(:disabled):hover {
  background: var(--leaf-dark);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 次要動作按鈕（重新載入／上架／下架）：白底外框，比 .btn 低調 */
.btn-ghost {
  padding: 8px 16px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  color: var(--ink-soft);
  font-size: 14px;
  cursor: pointer;
}

.btn-ghost:hover {
  border-color: var(--leaf);
  color: var(--leaf);
}

/* 手機寬度：兩欄並排的 grid2 改成單欄，避免欄位擠在一起 */
@media (max-width: 560px) {
  .grid2 {
    grid-template-columns: 1fr;
  }
}

/* ===== 新增商品彈窗 =====
   視覺語言照抄 ConfirmDialog.vue（半透明遮罩＋置中白卡片），
   因為是 Teleport 到 body、且是 scoped 樣式，這裡要重新定義一份，
   不能直接沿用 ConfirmDialog 內部的 .cd-xxx（scoped 樣式不會跨元件共用）。*/

/* 遮罩層：鋪滿整個畫面，蓋在最上層 */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(30, 30, 25, 0.45);
}

/* 彈窗本體：比 ConfirmDialog 寬，因為裡面要放整個表單；
   加 max-height + overflow-y，螢幕太矮時表單可以在卡片內捲動，不會被切掉 */
.modal-card {
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 48px rgba(30, 25, 15, 0.28);
  padding: 24px;
}

/* 彈窗標題列：標題靠左、關閉按鈕（✕）靠右 */
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.modal-head h2 {
  margin: 0;
  font-size: 18px;
  color: var(--ink);
}

.modal-close {
  border: none;
  background: transparent;
  font-size: 18px;
  line-height: 1;
  color: var(--muted);
  cursor: pointer;
  padding: 4px;
}

.modal-close:hover {
  color: var(--ink);
}

/* 淡入淡出＋卡片微縮放的過場動畫（Vue 的 Transition 會自動加/拿掉這些 class） */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.18s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-card,
.modal-fade-leave-active .modal-card {
  transition: transform 0.18s ease;
}

.modal-fade-enter-from .modal-card,
.modal-fade-leave-to .modal-card {
  transform: scale(0.94);
}
</style>
