<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const newsId = computed(() => route.params.newsId)
const news = ref(null)
const loading = ref(true)
const error = ref('')
const imageVisible = ref(true)

async function loadNewsDetail() {
  loading.value = true
  error.value = ''
  imageVisible.value = true

  try {
    const res = await fetch(`/api/news/${newsId.value}`, {
      credentials: 'include',
    })

    if (!res.ok) {
      throw new Error(`伺服器回應 ${res.status}`)
    }

    news.value = await res.json()
  } catch (e) {
    error.value = e.message || '無法載入最新消息內容，請稍後再試。'
  } finally {
    loading.value = false
  }
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

onMounted(loadNewsDetail)
watch(newsId, loadNewsDetail)
</script>

<template>
  <main class="page">
    <RouterLink class="back-link" :to="{ name: 'news' }">返回最新消息</RouterLink>

    <p v-if="loading" class="state">載入最新消息內容中…</p>

    <section v-else-if="error" class="state state--error">
      <p>載入失敗：{{ error }}</p>
      <button type="button" @click="loadNewsDetail">重新載入</button>
    </section>

    <article v-else-if="news" class="detail">
      <header class="detail__header">
        <time class="detail__date">{{ formatDate(news.publishTime || news.createdAt) }}</time>
        <h1>{{ news.title }}</h1>
      </header>

      <img
        v-if="imageVisible"
        class="detail__image"
        :src="`/api/news/${news.newsId}/image`"
        :alt="news.title"
        @error="imageVisible = false"
      >

      <p class="detail__content">{{ news.content }}</p>
    </article>
  </main>
</template>

<style scoped>
.page {
  width: min(920px, calc(100% - 36px));
  margin: 0 auto;
  padding: 48px 0 72px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  margin-bottom: 22px;
  color: var(--leaf-dark);
  font-weight: 700;
  text-decoration: none;
}

.back-link::before {
  content: "‹";
  margin-right: 8px;
  font-size: 24px;
  line-height: 1;
}

.state {
  padding: 44px 0;
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

.detail {
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--shadow);
}

.detail__header {
  padding: 30px 32px 22px;
}

.detail__date {
  display: block;
  margin-bottom: 12px;
  color: var(--leaf-dark);
  font-size: 14px;
  font-weight: 700;
}

.detail h1 {
  margin: 0;
  color: var(--ink);
  font-size: 34px;
  line-height: 1.25;
}

.detail__image {
  display: block;
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  background: var(--leaf-soft);
}

.detail__content {
  margin: 0;
  padding: 28px 32px 34px;
  color: var(--ink-soft);
  font-size: 18px;
  line-height: 1.9;
  white-space: pre-line;
}

@media (max-width: 720px) {
  .page {
    width: min(100% - 28px, 920px);
    padding-top: 34px;
  }

  .detail__header,
  .detail__content {
    padding-left: 20px;
    padding-right: 20px;
  }

  .detail h1 {
    font-size: 28px;
  }
}
</style>
