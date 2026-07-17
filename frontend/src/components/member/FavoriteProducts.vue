<script setup>
import { ref, onMounted } from 'vue';
import noImage from '@/assets/no-image.svg'
import { toast } from '@/composables/useToast'

// 會員中心：我的收藏 → 商品收藏（預設佔位元件）
// 商品收藏的所有程式碼（打 API、清單畫面、取消收藏）都寫在這個元件裡。
// 清單載入完成後記得 emit('count', 筆數)，讓外層 tab 顯示數字。
const emit = defineEmits(['count'])
const products = ref([])
const loading = ref(true)
const error = ref('')


async function loadWishList(){

  loading.value = true
  error.value = ''
  try{
    const res = await fetch('/api/member/wishlists')
    if(!res.ok){
      throw new Error(`伺服器回應${res.status}`)
    }
    products.value = await res.json()
    emit('count', products.value.length)
  }
  catch(e){
    error.value = e.message || '無法載入商品收藏,請稍後再試'
  }
  finally{
    loading.value = false
  }
}


async function removeWishList(productId) {

  
  try {
    const res = await fetch(`/api/member/wishlists/${productId}`, {

      method: 'DELETE',
      credentials: 'include',
    })
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    toast('已取消收藏')
    loadWishList()
  } catch (e) {
    error.value = e.message || '無法取消收藏,請稍後再試'
  }

}



onMounted(loadWishList)

</script>

<template>
  <div class="fp-list">
    <div v-if="loading" class="state-box">
      <p>載入中…</p>
    </div>

    <div v-else-if="error" class="state-box">
      <span class="state-icon">😵</span>
      <p>{{ error }}</p>
      <button type="button" class="state-btn" @click="loadWishList">重新載入</button>
    </div>

    <div v-else-if="products.length === 0" class="state-box">
      <span class="state-icon">🤍</span>
      <p>還沒有收藏任何商品</p>
      <router-link class="state-btn" to="/products">去逛逛商品 →</router-link>
    </div>

    <template v-else>
      <article v-for="product in products" :key="product.productId" class="fp-card">
        <RouterLink
          class="fp-card__link"
          :to="{ name: 'product-detail', params: { productId: product.productId } }"
        >
          <div class="fp-img-wrap">
            <!-- 直接用後端圖片端點當 src；沒圖（404）時 @error 換成預設圖 -->
            <img class="fp-img" :src="`/api/products/${product.productId}/image`"
              :alt="product.productName" loading="lazy"
              @error="$event.target.src = noImage">
          </div>
        </RouterLink>

        <div class="fp-body">
          <div class="fp-head">
            <RouterLink
              class="fp-name-link"
              :to="{ name: 'product-detail', params: { productId: product.productId } }"
            >
              <h3 class="fp-name">{{ product.productName }}</h3>
            </RouterLink>
            <!-- 這裡的愛心一定是實心（清單裡都是收藏過的），點下去取消收藏 -->
            <button type="button" class="fp-heart" aria-label="取消收藏"
              @click="removeWishList(product.productId)">
              ♥
            </button>
          </div>

          <!-- 商品描述：沒填描述（null）時顯示預設字 -->
          <p class="fp-desc">{{ product.description || '（無描述）' }}</p>

          <dl class="fp-grid">
            <div class="fp-cell">
              <dt>零售價</dt>
              <dd>NT$ {{ product.retailPrice }}</dd>
            </div>
            <div class="fp-cell">
              <dt>計價單位</dt>
              <dd>{{ product.unitPricingMeasure || '—' }}</dd>
            </div>
          </dl>
        </div>
      </article>
    </template>
  </div>
</template>

<style scoped>
.fp-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ===== 收藏卡片：左圖右文 ===== */
.fp-card {
  display: flex;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: box-shadow 0.18s ease, transform 0.18s ease;
}
.fp-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}
.fp-card__link {
  flex: 0 0 120px;
  display: block;
}
.fp-img-wrap {
  width: 100%;
  height: 100%;
  background: var(--line);
}
.fp-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.fp-body {
  flex: 1;
  min-width: 0;
  padding: 16px 20px;
}

/* 名稱 + 愛心排同一行、左右對齊 */
.fp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.fp-name-link {
  color: inherit;
  text-decoration: none;
  min-width: 0;
}
.fp-name-link:hover .fp-name {
  color: var(--leaf-dark);
}
.fp-name {
  margin: 0;
  font-size: 17px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.fp-heart {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: #fdeaea;
  color: #e0435b;
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 0.15s ease, background 0.15s ease;
}
.fp-heart:hover {
  transform: scale(1.1);
  background: #fbd7d7;
}

/* 商品描述：小字、次要色，單行截斷（同 .fp-name 的三件組），避免長描述撐爆卡片 */
.fp-desc {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 欄位以小格子排列，窄螢幕自動換行 */
.fp-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px 16px;
  margin: 0;
}
.fp-cell dt {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 2px;
}
.fp-cell dd {
  margin: 0;
  font-size: 14px;
  color: var(--ink-soft);
}

@media (max-width: 560px) {
  .fp-card {
    flex-direction: column;
  }
  .fp-card__link {
    flex-basis: auto;
    height: 140px;
  }
  .fp-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ===== 載入中 / 失敗 / 空狀態 ===== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 20px;
  background: #fff;
  border: 1px dashed var(--line);
  border-radius: 14px;
  text-align: center;
}
.state-icon { font-size: 38px; }
.state-box p {
  margin: 0;
  font-size: 14px;
  color: var(--muted);
}
.state-btn {
  margin-top: 6px;
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid var(--leaf);
  background: #fff;
  color: var(--leaf);
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;
}
.state-btn:hover {
  background: var(--leaf);
  color: #fff;
}
</style>
