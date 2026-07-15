<script setup>
import { computed, onMounted, ref } from 'vue'

const newsList = ref([])
const loading = ref(true)
const error = ref('')
const page = ref(1)
const total = ref(0)
const pageSize = 5

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

async function loadNews() {
  loading.value = true
  error.value = ''

  try {
    const offset = (page.value - 1) * pageSize
    const res = await fetch(`/api/news?limit=${pageSize}&offset=${offset}`, {
      credentials: 'include',
    })

    if (!res.ok) {
      throw new Error(`伺服器回應 ${res.status}`)
    }

    const data = await res.json()
    newsList.value = data.results || []
    total.value = data.total || 0
  } catch (e) {
    error.value = e.message || '無法載入最新消息，請稍後再試。'
  } finally {
    loading.value = false
  }
}

function goPage(nextPage) {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadNews()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function formatDate(value) {
  if (!value) return '尚未設定時間'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function hideBrokenImage(event) {
  event.target.style.display = 'none'
}

onMounted(loadNews)
</script>

<template>
  <main class="page">
    <section class="hero">
      <span class="hero__icon" aria-hidden="true">📰</span>
      <div>
        <h1>最新消息</h1>
        <p>掌握 Farmily 平台公告、產季資訊與活動更新。</p>
      </div>
    </section>

    <p v-if="loading" class="state">載入最新消息中…</p>

    <section v-else-if="error" class="state state--error">
      <p>載入失敗：{{ error }}</p>
      <button type="button" @click="loadNews">重新載入</button>
    </section>

    <p v-else-if="newsList.length === 0" class="state">目前沒有最新消息。</p>

    <template v-else>
      <section class="news-list" aria-label="最新消息列表">
        <RouterLink
          v-for="item in newsList"
          :key="item.newsId"
          :to="{ name: 'news-detail', params: { newsId: item.newsId } }"
          class="news-card"
        >
          <img
            class="news-card__image"
            :src="`/api/news/${item.newsId}/image`"
            :alt="item.title"
            loading="lazy"
            @error="hideBrokenImage"
          >
          <div class="news-card__body">
            <time class="news-card__date">{{ formatDate(item.publishTime || item.createdAt) }}</time>
            <h2>{{ item.title }}</h2>
            <p>{{ item.content }}</p>
            <span class="news-card__more">查看詳細內容</span>
          </div>
        </RouterLink>
      </section>

      <nav v-if="totalPages > 1" class="pager" aria-label="最新消息分頁">
        <button type="button" :disabled="page === 1" @click="goPage(page - 1)">
          上一頁
        </button>
        <button
          v-for="n in totalPages"
          :key="n"
          type="button"
          :class="{ active: n === page }"
          @click="goPage(n)"
        >
          {{ n }}
        </button>
        <button type="button" :disabled="page === totalPages" @click="goPage(page + 1)">
          下一頁
        </button>
      </nav>
    </template>
  </main>
</template>

<style scoped>
.page {
  width: min(1120px, calc(100% - 36px));
  margin: 0 auto;
  padding: 56px 0 72px;
}

.hero {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.hero__icon {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--leaf-soft);
  font-size: 28px;
}

.hero h1 {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 32px;
}

.hero p {
  margin: 0;
  color: var(--muted);
}

.state {
  padding: 44px 0;
  text-align: center;
  color: var(--muted);
}

.state--error {
  color: #b42318;
}

.state--error button,
.pager button {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  cursor: pointer;
}

.state--error button {
  margin-top: 12px;
  padding: 9px 18px;
  border-radius: 999px;
}

.news-list {
  display: grid;
  gap: 18px;
}

.news-card {
  display: grid;
  grid-template-columns: 360px 1fr;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--shadow);
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.news-card:hover {
  border-color: var(--leaf);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.news-card__image {
  width: 100%;
  height: 100%;
  min-height: 168px;
  object-fit: cover;
  background: var(--leaf-soft);
}

.news-card__body {
  padding: 20px 22px;
}

.news-card__date {
  display: block;
  margin-bottom: 8px;
  color: var(--leaf-dark);
  font-size: 13px;
  font-weight: 700;
}

.news-card h2 {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 22px;
}

.news-card p {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.7;
}

.news-card__more {
  display: inline-block;
  margin-top: 16px;
  color: var(--leaf-dark);
  font-size: 14px;
  font-weight: 700;
}

.pager {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.pager button {
  min-width: 42px;
  padding: 8px 14px;
  border-radius: 999px;
}

.pager button.active {
  border-color: var(--leaf);
  background: var(--leaf);
  color: #fff;
}

.pager button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .page {
    width: min(100% - 28px, 1120px);
    padding-top: 36px;
  }

  .hero {
    align-items: flex-start;
  }

  .hero h1 {
    font-size: 28px;
  }

  .news-card {
    grid-template-columns: 1fr;
  }

  .news-card__image {
    height: 180px;
  }
}
</style>
