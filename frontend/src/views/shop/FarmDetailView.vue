<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import noImage from '@/assets/no-image.svg'
import { getFarm, getFarmProducts, getFarmTrips, getFarmBlogs } from '@/api/farm'

const route = useRoute()
const router = useRouter()

const farmerId = computed(() => route.params.farmerId)
const farm = ref(null)
const products = ref([])
const trips = ref([])
const blogs = ref([])
const loading = ref(true)
const error = ref('')
const relatedErrors = ref({
  products: '',
  trips: '',
  blogs: '',
})

const heroImage = computed(() => noImage)

const farmAddress = computed(() => {
  if (!farm.value) return ''
  return [farm.value.cityName, farm.value.distName, farm.value.farmAddress]
    .filter(Boolean)
    .join('')
})

const mapUrl = computed(() => {
  if (!farm.value?.mapQuery) return ''
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(farm.value.mapQuery)}`
})

function formatPrice(price) {
  if (price == null) return '—'
  return `NT$ ${Number(price).toLocaleString('zh-TW')}`
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}

function stripHtml(html) {
  return (html || '').replace(/<[^>]*>/g, '').trim()
}

function tripTypeLabel(type) {
  const labels = {
    FARM_EXPERIENCE: '農場體驗',
    FIELD_VISIT: '產地參訪',
  }
  return labels[type] || type || '體驗活動'
}

function stars(count) {
  const value = Math.max(0, Math.min(5, Number(count) || 0))
  return '★'.repeat(value) + '☆'.repeat(5 - value)
}

function onImageError(event) {
  event.target.src = noImage
}

async function loadFarmDetail() {
  loading.value = true
  error.value = ''
  farm.value = null
  products.value = []
  trips.value = []
  blogs.value = []
  relatedErrors.value = {
    products: '',
    trips: '',
    blogs: '',
  }

  const id = farmerId.value
  try {
    farm.value = await getFarm(id)

    // 三塊關聯內容各自載入：某一塊失敗（例如後端端點還沒做）不影響其他塊
    const [productRes, tripRes, blogRes] = await Promise.allSettled([
      getFarmProducts(id),
      getFarmTrips(id),
      getFarmBlogs(id),
    ])

    products.value = pickList(productRes, 'products', '商品')
    trips.value = pickList(tripRes, 'trips', '體驗活動')
    blogs.value = pickList(blogRes, 'blogs', '部落格')
  } catch (e) {
    error.value = e.status === 404 ? '找不到這個農場' : (e.message || '無法載入農場資料，請稍後再試。')
  } finally {
    loading.value = false
  }
}

// allSettled 結果轉清單：成功回陣列，失敗記下該區塊錯誤並回空陣列
function pickList(result, key, label) {
  if (result.status === 'fulfilled') return result.value || []
  relatedErrors.value[key] = `${label}載入失敗`
  return []
}

function goBack() {
  router.push({ name: 'farmily' })
}

onMounted(loadFarmDetail)
watch(farmerId, loadFarmDetail)
</script>

<template>
  <main class="farm-detail-page">
    <button class="back-link" type="button" @click="goBack">← 返回合作農場</button>

    <p v-if="loading" class="state">農場資料載入中…</p>

    <section v-else-if="error" class="state state--error">
      <p>載入失敗：{{ error }}</p>
      <button type="button" @click="loadFarmDetail">重新載入</button>
    </section>

    <template v-else-if="farm">
      <section class="farm-hero">
        <img class="farm-hero__image" :src="heroImage" :alt="farm.farmName" @error="onImageError" />
        <div class="farm-hero__content">
          <span class="farm-hero__eyebrow">合作農場</span>
          <h1>{{ farm.farmName }}</h1>
          <p>{{ farm.farmDesc || '這座農場尚未提供介紹。' }}</p>

          <dl class="farm-meta">
            <div>
              <dt>地區</dt>
              <dd>{{ [farm.cityName, farm.distName].filter(Boolean).join('・') || '尚未提供' }}</dd>
            </div>
            <div>
              <dt>地址</dt>
              <dd>{{ farmAddress || '尚未提供' }}</dd>
            </div>
            <div v-if="farm.farmerPhoneNum">
              <dt>電話</dt>
              <dd>{{ farm.farmerPhoneNum }}</dd>
            </div>
          </dl>

          <a v-if="mapUrl" class="map-link" :href="mapUrl" target="_blank" rel="noreferrer">
            在地圖查看
          </a>
        </div>
      </section>

      <section class="section-block">
        <div class="section-head">
          <h2>農場商品</h2>
          <RouterLink v-if="products.length" :to="{ name: 'products' }">查看全部商品</RouterLink>
        </div>

        <p v-if="relatedErrors.products" class="section-error">{{ relatedErrors.products }}</p>
        <p v-else-if="products.length === 0" class="empty">這個農場目前沒有上架商品。</p>
        <div v-else class="trip-grid">
          <RouterLink
            v-for="product in products"
            :key="product.productId"
            class="trip-card product-card"
            :to="{ name: 'product-detail', params: { productId: product.productId } }"
          >
            <h3>{{ product.productName }}</h3>
            <p>{{ product.description }}</p>
            <strong>{{ formatPrice(product.retailPrice) }}</strong>
          </RouterLink>
        </div>
      </section>

      <section class="section-block">
        <div class="section-head">
          <h2>體驗活動</h2>
          <RouterLink v-if="trips.length" :to="{ name: 'farm-trips' }">查看全部活動</RouterLink>
        </div>

        <p v-if="relatedErrors.trips" class="section-error">{{ relatedErrors.trips }}</p>
        <p v-else-if="trips.length === 0" class="empty">這個農場目前沒有開放中的體驗活動。</p>
        
        <div v-else class="trip-grid">
          <RouterLink
            v-for="trip in trips"
            :key="trip.farmTripId"
            :to="{ name: 'farm-trip-detail', params: { farmTripId: trip.farmTripId } }"
            class="trip-card trip-card-link"
          >
            <img
              class="trip-card__image"
              :src="`/api/farm-trips/${trip.farmTripId}/image`"
              :alt="trip.farmTripTitle"
              @error="$event.target.style.display = 'none'"
            />
            <div class="trip-card__body">
              <span>{{ tripTypeLabel(trip.farmTripType) }}</span>
              <h3>{{ trip.farmTripTitle }}</h3>
              <p>📍 {{ trip.location || farmAddress || '尚未提供地點' }}</p>
              <p class="stars">{{ stars(trip.starNumbers) }}</p>
              <strong>參考價 {{ formatPrice(trip.referPrice) }}</strong>
            </div>
          </RouterLink>
        </div>

      </section>

      <section class="section-block">
        <div class="section-head">
          <h2>農場部落格</h2>
          <RouterLink v-if="blogs.length" :to="{ name: 'blogs' }">查看全部文章</RouterLink>
        </div>

        <p v-if="relatedErrors.blogs" class="section-error">{{ relatedErrors.blogs }}</p>
        <p v-else-if="blogs.length === 0" class="empty">這個農場目前沒有公開文章。</p>
        <div v-else class="blog-list">
          <RouterLink
            v-for="blog in blogs"
            :key="blog.blogId"
            class="blog-card"
            :to="{ name: 'blog-detail', params: { blogId: blog.blogId } }"
          >
            <img
              class="blog-card__image"
              :src="`/api/blogs/${blog.blogId}/image`"
              :alt="blog.blogTitle"
              @error="$event.target.style.display = 'none'"
            />
            <div>
              <time>{{ formatDate(blog.blogTime) }}</time>
              <h3>{{ blog.blogTitle }}</h3>
              <p>{{ stripHtml(blog.blogContent) }}</p>
              <span>♡ {{ blog.blogLikeCount || 0 }}</span>
            </div>
          </RouterLink>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>

.trip-card-link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  display: block;
}
.trip-card-link:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  transition: all 0.15s ease;
}

.farm-detail-page {
  width: min(1120px, calc(100% - 36px));
  margin: 0 auto;
  padding: 34px 0 72px;
}

.back-link {
  margin-bottom: 18px;
  border: none;
  background: none;
  color: var(--leaf-dark);
  cursor: pointer;
  font-weight: 700;
}

.state {
  padding: 48px 0;
  text-align: center;
  color: var(--muted);
}

.state--error {
  color: #b42318;
}

.state--error button {
  margin-top: 12px;
  padding: 9px 18px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink);
  cursor: pointer;
}

.farm-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.82fr);
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: #fff;
  box-shadow: var(--shadow);
}

.farm-hero__image {
  width: 100%;
  height: 100%;
  min-height: 360px;
  object-fit: cover;
}

.farm-hero__content {
  padding: 34px;
}

.farm-hero__eyebrow {
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 700;
}

.farm-hero h1 {
  margin: 8px 0 12px;
  color: var(--ink);
  font-size: 34px;
}

.farm-hero p {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.8;
}

.farm-meta {
  display: grid;
  gap: 12px;
  margin: 22px 0 0;
}

.farm-meta div {
  min-width: 0;
}

.farm-meta dt {
  color: var(--muted);
  font-size: 13px;
}

.farm-meta dd {
  margin: 4px 0 0;
  color: var(--ink);
  word-break: break-word;
}

.map-link {
  display: inline-flex;
  margin-top: 24px;
  padding: 10px 22px;
  border-radius: 999px;
  background: var(--leaf);
  color: #fff;
  font-weight: 700;
  text-decoration: none;
}

.map-link:hover {
  background: var(--leaf-dark);
}

.section-block {
  margin-top: 34px;
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-head h2 {
  margin: 0;
  color: var(--ink);
  font-size: 24px;
}

.section-head a {
  color: var(--leaf-dark);
  font-weight: 700;
  text-decoration: none;
}

.empty {
  padding: 24px 0;
  color: var(--muted);
}

.section-error {
  padding: 16px 0;
  color: #b42318;
}

.trip-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 18px;
}

.trip-card,
.blog-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  box-shadow: var(--shadow);
}

.trip-card h3,
.blog-card h3 {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 18px;
}

.trip-card p,
.blog-card p {
  margin: 0 0 10px;
  color: var(--muted);
  line-height: 1.6;
}

.trip-card strong {
  color: var(--leaf-dark);
}

.trip-card {
  padding: 0;
  overflow: hidden;
}

.trip-card__image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background: var(--leaf-soft);
  display: block;
}

.trip-card__body {
  padding: 18px;
}

.product-card {
  display: block;
  color: inherit;
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.product-card strong {
  display: block;
  margin-top: 8px;
}

.trip-card span {
  display: inline-flex;
  margin-bottom: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 700;
}

.stars {
  color: #d69e00;
  letter-spacing: 1px;
}

.blog-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.blog-card {
  display: block;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.blog-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.blog-card__image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background: var(--leaf-soft);
  display: block;
}

.blog-card div {
  padding: 16px 18px;
}

.blog-card time,
.blog-card span {
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 700;
}

.blog-card p {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

@media (max-width: 800px) {
  .farm-detail-page {
    width: min(100% - 28px, 1120px);
  }

  .farm-hero {
    grid-template-columns: 1fr;
  }

  .farm-hero__image {
    min-height: 240px;
  }

  .farm-hero__content {
    padding: 24px;
  }
}
</style>
