<script setup>
import { ref, onMounted } from 'vue';

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
    alert('已取消收藏')
    loadWishList()
  } catch (e) {
    error.value = e.message || '無法取消收藏,請稍後再試'
  }

}



onMounted(loadWishList)

</script>

<template>
  <section class="card">
    <p v-if="loading" class="state">載入中…</p>

    <div v-else-if="error" class="state state-error">
      <p class="msg-err">{{ error }}</p>
      <button type="button" class="btn-ghost" @click="loadWishList">重新載入</button>
    </div>

    <div v-else-if="products.length === 0" class="state">
      <p>還沒有收藏商品，快去挑選喜歡的商品吧！</p>
      <router-link class="btn" to="/products">去逛逛商品 →</router-link>
    </div>

    <div v-else class="favorite-list">
      <article v-for="product in products" :key="product.productId" class="favorite-row">
        <div class="favorite-row__info">
          <h3>{{ product.productName }}</h3>
          <p class="favorite-row__unit">{{ product.unitPricingMeasure }}</p>
        </div>
        <div class="favorite-row__actions">
          <span class="favorite-row__price">NT$ {{ product.retailPrice }}</span>
          <button type="button" class="btn-ghost" @click="removeWishList(product.productId)">移除收藏</button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
/* 卡片外框：跟全站其他頁面同一套（白底、陰影、頂部一條主題綠） */
.card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: var(--shadow);
  padding: 24px;
  border-top: 3px solid var(--leaf);
}

/* 載入中／錯誤／空清單：置中提示，內容用 flex 直排、留間距 */
.state {
  text-align: center;
  color: var(--muted);
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.state p {
  margin: 0;
}
.msg-err {
  margin: 0;
  color: #c0392b;
  font-size: 14px;
}

/* 收藏清單：每筆之間留間距 */
.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 單筆收藏：左邊商品資訊、右邊價格+移除按鈕，兩端對齊；
   flex-wrap 讓窄螢幕時能自動換行，不會擠爆 */
.favorite-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  flex-wrap: wrap;
}
.favorite-row__info h3 {
  margin: 0 0 2px;
  font-size: 16px;
  color: var(--ink);
}
.favorite-row__unit {
  margin: 0;
  font-size: 13px;
  color: var(--muted);
}
.favorite-row__actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
.favorite-row__price {
  font-size: 15px;
  font-weight: 600;
  color: var(--leaf-dark);
}

/* 主要動作（去逛逛商品）：實心主題綠 */
.btn {
  padding: 9px 18px;
  border: none;
  border-radius: 10px;
  background: var(--leaf);
  color: #fff;
  font-size: 14px;
  text-decoration: none;
  display: inline-block;
  cursor: pointer;
}
.btn:hover {
  background: var(--leaf-dark);
}

/* 次要動作（重新載入／移除收藏）：白底外框 */
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

/* 窄螢幕：單筆收藏改成上下堆疊，操作區撐滿寬度 */
@media (max-width: 480px) {
  .favorite-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .favorite-row__actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
