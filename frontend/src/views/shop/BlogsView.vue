<script setup>
// 「部落格」頁面 — 串接後端 GET /api/blogs(查詢 + 分頁)
import { ref, computed, onMounted } from 'vue'
import { listPublicBlogs } from '@/api/blog'

const blogs = ref([])
const loadState = ref('loading')   // loading | ready | empty | error
const errorMsg = ref('')

const page = ref(1)                 // 目前第幾頁(從 1 起算)
const total = ref(0)                // 後端回傳的總筆數
const pageSize = 5                  // 每頁幾筆(改這個數字就能調整)

// 由 total 算出總頁數
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

async function load() {
  loadState.value = 'loading'
  try {
    const offset = (page.value - 1) * pageSize          // 第 N 頁 → 跳過前面幾筆
    const data = await listPublicBlogs(offset, pageSize)
    blogs.value = data.results || []                    // 這一頁的文章
    total.value = data.total || 0                       // 全部共幾筆
    loadState.value = blogs.value.length ? 'ready' : 'empty'
  } catch (e) {
    errorMsg.value = e.message
    loadState.value = 'error'
  }
}

function goPage(n) {
  if (n < 1 || n > totalPages.value || n === page.value) return
  page.value = n
  load()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function fmt(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const p = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`
}

// 內容現在是 HTML，列表預覽把標籤去掉、只留純文字
function snippet(html) {
  return (html || '').replace(/<[^>]*>/g, '').trim()
}

onMounted(load)
</script>

<template>
  <main class="page">
    <h1>✍️ 部落格</h1>

    <p v-if="loadState === 'loading'">載入中…</p>
    <p v-else-if="loadState === 'empty'">目前沒有文章。</p>
    <p v-else-if="loadState === 'error'" class="err">載入失敗：{{ errorMsg }}</p>

    <template v-else>
      <ul class="list">
        <li v-for="b in blogs" :key="b.blogId">
          <router-link class="card" :to="`/blogs/${b.blogId}`">
            <img class="cover" :src="`/api/blogs/${b.blogId}/image`"
                 @error="$event.target.style.display = 'none'" alt="" />
            <h3>{{ b.blogTitle }}</h3>
            <p class="content">{{ snippet(b.blogContent) }}</p>
            <div class="meta">♡ {{ b.blogLikeCount || 0 }} ｜ {{ fmt(b.blogTime) }}</div>
          </router-link>
        </li>
      </ul>

      <!-- 換頁 -->
      <div v-if="totalPages > 1" class="pager">
        <button :disabled="page === 1" @click="goPage(page - 1)">‹ 上一頁</button>
        <button v-for="n in totalPages" :key="n"
                :class="{ active: n === page }" @click="goPage(n)">{{ n }}</button>
        <button :disabled="page === totalPages" @click="goPage(page + 1)">下一頁 ›</button>
      </div>
    </template>
  </main>
</template>

<style scoped>
.page { padding: 32px clamp(18px, 4vw, 56px); }
.list {
  list-style: none; padding: 0; display: grid; gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}
.card {
  display: block; border: 1px solid #e3e3e3; border-radius: 12px; padding: 16px;
  text-decoration: none; color: inherit; cursor: pointer;
  transition: box-shadow .18s ease, transform .18s ease;
}
.card:hover { box-shadow: 0 6px 18px #0000001f; transform: translateY(-2px); }
.cover { width: 100%; height: 150px; object-fit: cover; border-radius: 8px; margin-bottom: 12px; }
.card h3 { margin: 0 0 8px; }
.content { color: #555; font-size: 14px; margin: 0 0 12px; max-height: 4.5em; overflow: hidden; }
.meta { color: #888; font-size: 12px; }
.err { color: #c0392b; }
.pager { display: flex; gap: 8px; justify-content: center; margin-top: 28px; }
.pager button {
  min-width: 38px; padding: 6px 12px; border: 1px solid #ccc; background: #fff;
  border-radius: 8px; cursor: pointer;
}
.pager button.active { background: #16a34a; color: #fff; border-color: #16a34a; }
.pager button:disabled { opacity: .4; cursor: not-allowed; }
</style>
