<script setup>
// 「部落格」頁面 — 串接後端 GET /api/blogs(查詢 + 篩選 + 排序 + 分頁)
import { ref, computed, onMounted } from 'vue'
import { listPublicBlogs, listBlogTypes } from '@/api/blog'

const blogs = ref([])
const loadState = ref('loading')   // loading | ready | empty | error
const errorMsg = ref('')

const page = ref(1)                 // 目前第幾頁(從 1 起算)
const total = ref(0)                // 後端回傳的總筆數
const pageSize = 8                  // 每頁幾筆(改這個數字就能調整)

// ===== 篩選 / 排序狀態 =====
const types = ref([])               // 分類清單(下拉用)
const selectedType = ref('')        // 目前選的分類 blogTypeId('' = 全部)
const search = ref('')              // 關鍵字
// 排序用單一下拉，對應後端的 orderBy + sort 兩個參數
const sortKey = ref('time_desc')
const sortMap = {
  time_desc: { orderBy: 'blog_time', sort: 'desc' },
  time_asc: { orderBy: 'blog_time', sort: 'asc' },
  like_desc: { orderBy: 'blog_like_count', sort: 'desc' },
  like_asc: { orderBy: 'blog_like_count', sort: 'asc' },
}

// blogTypeId → 分類名稱 對照(給卡片標籤用)
const typeMap = computed(() =>
  Object.fromEntries(types.value.map((t) => [t.blogTypeId, t.blogTypeName]))
)
function typeName(id) {
  return typeMap.value[id] || '文章'
}

// 由 total 算出總頁數
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

async function load() {
  loadState.value = 'loading'
  try {
    const offset = (page.value - 1) * pageSize          // 第 N 頁 → 跳過前面幾筆
    const { orderBy, sort } = sortMap[sortKey.value]
    const data = await listPublicBlogs(offset, pageSize, {
      blogTypeId: selectedType.value || undefined,      // 空字串就不帶 → 全部分類
      search: search.value.trim() || undefined,
      orderBy,
      sort,
    })
    blogs.value = data.results || []                    // 這一頁的文章
    total.value = data.total || 0                       // 全部共幾筆
    loadState.value = blogs.value.length ? 'ready' : 'empty'
  } catch (e) {
    errorMsg.value = e.message
    loadState.value = 'error'
  }
}

// 篩選/排序變動 → 回第 1 頁再查(避免停在不存在的頁碼)
function applyFilters() {
  page.value = 1
  load()
}

// 清除所有條件
function resetFilters() {
  selectedType.value = ''
  search.value = ''
  sortKey.value = 'time_desc'
  applyFilters()
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

onMounted(async () => {
  try {
    types.value = await listBlogTypes()   // 取分類清單給下拉；失敗就留空(不影響列表)
  } catch {
    types.value = []
  }
  load()
})
</script>

<template>
  <main class="page">
    <!-- Hero：與「全部商品」同款規格(emoji 同行、置中) -->
    <section class="hero">
      <h1>✍️ 部落格</h1>
      <p>小農產地日記與會員分享，看看餐桌背後的故事。</p>
    </section>

    <!-- 篩選 / 搜尋 / 排序 -->
    <div class="toolbar">
      <select v-model="selectedType" class="ctrl" @change="applyFilters">
        <option value="">全部分類</option>
        <option v-for="t in types" :key="t.blogTypeId" :value="t.blogTypeId">
          {{ t.blogTypeName }}
        </option>
      </select>

      <input
        v-model="search"
        class="ctrl search"
        type="search"
        placeholder="搜尋標題或內容…"
        @keyup.enter="applyFilters"
      />

      <select v-model="sortKey" class="ctrl" @change="applyFilters">
        <option value="time_desc">最新發布</option>
        <option value="time_asc">最舊發布</option>
        <option value="like_desc">最多讚</option>
        <option value="like_asc">最少讚</option>
      </select>

      <button class="ctrl btn" @click="applyFilters">搜尋</button>
      <button class="ctrl btn ghost" @click="resetFilters">清除</button>
    </div>

    <p v-if="loadState === 'loading'" class="state">載入文章中…</p>
    <p v-else-if="loadState === 'empty'" class="state">目前沒有符合條件的文章。</p>
    <section v-else-if="loadState === 'error'" class="state state--error">
      <p>載入失敗：{{ errorMsg }}</p>
      <button type="button" @click="load">重新載入</button>
    </section>

    <template v-else>
      <ul class="list">
        <li v-for="b in blogs" :key="b.blogId">
          <router-link class="blog-card" :to="`/blogs/${b.blogId}`">
            <img class="blog-card__image" :src="`/api/blogs/${b.blogId}/image`"
                 @error="$event.target.style.display = 'none'" alt="" />
            <div class="blog-card__body">
              <span class="type-badge" :class="'cat-' + b.blogTypeId">{{ typeName(b.blogTypeId) }}</span>
              <h3>{{ b.blogTitle }}</h3>
              <!-- 作者：小農文=🏡農場名、會員文=👤會員名；後端沒帶名字時整行不顯示 -->
              <p v-if="b.authorName" class="author">
                {{ b.authorType === 'FARMER' ? '🏡' : '👤' }} {{ b.authorName }}
              </p>
              <p class="content">{{ snippet(b.blogContent) }}</p>
              <div class="meta">
                <span class="meta__like">♡ {{ b.blogLikeCount || 0 }}</span>
                <time>{{ fmt(b.blogTime) }}</time>
              </div>
            </div>
          </router-link>
        </li>
      </ul>

      <!-- 換頁 -->
      <nav v-if="totalPages > 1" class="pager">
        <button :disabled="page === 1" @click="goPage(page - 1)">上一頁</button>
        <button v-for="n in totalPages" :key="n"
                :class="{ active: n === page }" @click="goPage(n)">{{ n }}</button>
        <button :disabled="page === totalPages" @click="goPage(page + 1)">下一頁</button>
      </nav>
    </template>
  </main>
</template>

<style scoped>
.page {
  width: min(1120px, calc(100% - 36px));
  margin: 0 auto;
  padding: 32px 0 72px;   /* 上方間距對齊其他導覽頁(32px) */
}

/* ===== Hero（與「全部商品」同款規格：emoji 同行、置中）===== */
.hero {
  text-align: center;
  margin-bottom: 32px;
}
.hero h1 {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 28px;
}
.hero p {
  margin: 0;
  color: var(--muted);
}

/* ===== 工具列 ===== */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 28px;
}
.ctrl {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  font-size: 14px;
  color: var(--ink);
}
.ctrl.search {
  flex: 1;
  min-width: 200px;
}
.btn {
  cursor: pointer;
  background: var(--leaf);
  color: #fff;
  border-color: var(--leaf);
  transition: background 0.18s ease;
}
.btn:hover {
  background: var(--leaf-dark);
}
.btn.ghost {
  background: #fff;
  color: var(--leaf-dark);
}
.btn.ghost:hover {
  background: var(--leaf-soft);
}

/* ===== 狀態訊息 ===== */
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
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  cursor: pointer;
}

/* ===== 卡片格線（比照首頁 product-grid）===== */
.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}
.blog-card {
  display: flex;
  flex-direction: column;
  height: 100%;               /* 撐滿格線列高，內容多寡不影響卡片高度 */
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  box-shadow: var(--shadow);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.blog-card:hover {
  transform: translateY(-4px);
  border-color: var(--leaf);
  box-shadow: 0 14px 30px rgba(55, 45, 28, 0.18);   /* 加深，滑過明顯浮起 */
}
.blog-card__image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
  background: var(--leaf-soft);
  transition: transform 0.45s ease;   /* 滑過封面微放大 */
}
.blog-card:hover .blog-card__image {
  transform: scale(1.05);
}
.blog-card__body {
  display: flex;
  flex-direction: column;
  flex: 1;                    /* 佔滿卡片剩餘高度，好讓 meta 靠底對齊 */
  padding: 16px 18px;
}
.type-badge {
  display: inline-block;
  margin-bottom: 8px;
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--leaf-soft);
  color: var(--leaf-dark);
  font-size: 12px;
  font-weight: 600;
}
/* 分類配色（產地日記綠 / 蔬果知識藍 / 體驗回顧黃 / 食譜橘）與首頁一致 */
.type-badge.cat-1 { background: #e5f0dd; color: #3f6a23; }
.type-badge.cat-2 { background: #e3eefb; color: #2f5fa5; }
.type-badge.cat-3 { background: #faf3c9; color: #8a7a12; }
.type-badge.cat-4 { background: #fdeede; color: #b5651d; }
.blog-card__body h3 {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 17px;
  transition: color 0.18s ease;   /* hover 變綠用 */
  /* 標題最多 2 行，超長不撐歪卡片 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
/* hover 時標題變品牌綠，暗示可點擊 */
.blog-card:hover .blog-card__body h3 { color: var(--leaf-dark); }
.author {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 10px;
  color: var(--muted);
  font-size: 13px;
}
.content {
  color: var(--ink-soft);
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 14px;
  /* 多行截斷：滿 3 行就以「…」收尾，不會切在字中間 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;           /* 不管內文長短，都把讚數/日期推到卡片底部對齊 */
  color: var(--muted);
  font-size: 12px;
}
.meta__like {
  color: var(--leaf-dark);
  font-weight: 700;
}

/* ===== 分頁（與 NewsView 同款 pill）===== */
.pager {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 32px;
  flex-wrap: wrap;
}
.pager button {
  min-width: 42px;
  padding: 8px 14px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  border-radius: 999px;
  cursor: pointer;
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
  .hero h1 {
    font-size: 28px;
  }
}
</style>
