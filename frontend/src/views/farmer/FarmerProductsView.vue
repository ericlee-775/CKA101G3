<script setup>
// 小農後台：商品管理（待接後端 API）,商品管理（清單／改價／上下架）
import { ref, onMounted} from 'vue'

const products = ref([])
const loading = ref(true)
const error = ref('')

async function loadProducts() {

  loading.value = true
  error.value = ''
  try {
    const res = await fetch('/api/products/my')
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
    const res = await fetch(`/api/products/${id}`, {

      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ retailPrice: Number(product.retailPrice) }),

    })
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    alert('已儲存')
  } catch (e) {
    error.value = e.message || '無法存入商品資訊,請稍後再試'
  }
}

async function changeStatus(product, status) {

  const id = product.productId;
  try {
    const res = await fetch(`/api/products/${id}`, {

      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ status: status }),
    })
    if (!res.ok) {
      throw new Error(`伺服器回應${res.status}`)
    }
    alert('已儲存')
    loadProducts()
  } catch (e) {
    error.value = e.message || '無法存入商品資訊,請稍後再試'
  }

}

onMounted(loadProducts)
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>📦 商品管理</h1>
    </header>


    <p v-if="loading" class="state">載入中..</p>

    <div v-else-if="error" class="state state-error">
      <p>{{ error }}</p>
      <button type="button" @click="loadProducts">重新載入</button>
    </div>

    <p v-else-if="products.length === 0" class="state">目前沒有商品。</p>

    <section v-else>
      <article v-for="(product, index) in products" :key="product.productId">
        <h2>{{ product.productName }}</h2>
        <p>{{ "第" + (index + 1) + "項" }}</p>
        <p>{{ product.unitPricingMeasure }}</p>
        <p>{{ product.status }}</p>
        <input type="number" v-model="product.retailPrice">
        <button type="button" @click="savePrice(product)">
          存價格
        </button>
         <button type="button" @click="changeStatus(product,'ACTIVE')">
          上架
        </button>
         <button type="button" @click="changeStatus(product,'INACTIVE')">
          下架
        </button>
      </article>
    </section>

  
  </main>
</template>

<style scoped>
.farmer-page {
  padding: 32px 24px;
}

.page-head h1 {
  margin: 0 0 20px;
  font-size: 24px;
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

.placeholder {
  margin: 0;
  color: var(--muted);
  font-size: 15px;
}
</style>
