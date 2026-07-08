<script setup>
// 會員中心：我的文章 列表
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { memberBlogApi as blogApi } from '@/api/blog'
import { confirm } from '@/composables/useConfirm'

const router = useRouter()

const blogs = ref([])
const loadState = ref('loading') // loading | ready | empty | error
const errorMsg = ref('')

const page = ref(1)
const total = ref(0)
const pageSize = 10
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

const imgVer = ref(Date.now())
const coverUrl = (blogId) => `/api/blogs/${blogId}/image?v=${imgVer.value}`

async function load() {
  loadState.value = 'loading'
  try {
    const offset = (page.value - 1) * pageSize
    const data = await blogApi.listMine(offset, pageSize)
    blogs.value = data.results || []
    total.value = data.total || 0
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
}

function fmt(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`
}

function goCreate() {
  router.push('/member/blogs/new')
}
function goView(b) {
  router.push(`/member/blogs/${b.blogId}`)
}
function goEdit(b) {
  router.push(`/member/blogs/${b.blogId}/edit`)
}

async function removeBlog(b) {
  const ok = await confirm({
    title: '刪除文章',
    message: `確定要刪除「${b.blogTitle}」嗎？此動作無法復原。`,
    confirmText: '刪除',
    danger: true,
  })
  if (!ok) return
  try {
    await blogApi.deleteMine(b.blogId)
    if (blogs.value.length === 1 && page.value > 1) page.value -= 1
    await load()
  } catch (e) {
    alert(e.message || '刪除失敗')
  }
}

onMounted(load)
</script>

<template>
  <main class="mb-page">
    <header class="page-head">
      <h1>✍️ 我的文章</h1>
      <button class="btn-primary" @click="goCreate">＋ 寫文章</button>
    </header>

    <section class="card">
      <p v-if="loadState === 'loading'" class="hint">載入中…</p>
      <p v-else-if="loadState === 'error'" class="hint err">載入失敗：{{ errorMsg }}</p>
      <p v-else-if="loadState === 'empty'" class="hint">還沒有文章，點右上「＋ 寫文章」開始吧。</p>

      <template v-else>
        <table class="tbl">
          <thead>
            <tr>
              <th class="col-cover">封面</th>
              <th>標題</th>
              <th class="col-date">發布時間</th>
              <th class="col-like">♡</th>
              <th class="col-act">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in blogs" :key="b.blogId">
              <td class="col-cover">
                <img class="thumb" :src="coverUrl(b.blogId)"
                     @error="$event.target.style.visibility = 'hidden'" alt="" />
              </td>
              <td class="ttl">{{ b.blogTitle }}</td>
              <td class="col-date">{{ fmt(b.blogTime) }}</td>
              <td class="col-like">{{ b.blogLikeCount || 0 }}</td>
              <td class="col-act">
                <button class="btn-sm" @click="goView(b)">查看</button>
                <button class="btn-sm" @click="goEdit(b)">編輯</button>
                <button class="btn-sm btn-danger" @click="removeBlog(b)">刪除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="totalPages > 1" class="pager">
          <button :disabled="page === 1" @click="goPage(page - 1)">‹</button>
          <button v-for="n in totalPages" :key="n"
                  :class="{ active: n === page }" @click="goPage(n)">{{ n }}</button>
          <button :disabled="page === totalPages" @click="goPage(page + 1)">›</button>
        </div>
      </template>
    </section>
  </main>
</template>

<style scoped>
.mb-page { }
.page-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px;
}
.page-head h1 { margin: 0; font-size: 22px; color: var(--ink); }

.card {
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: var(--shadow); padding: 22px; border-top: 3px solid var(--leaf);
}
.hint { margin: 0; color: var(--muted); font-size: 15px; }
.hint.err { color: #c0392b; }

.tbl { width: 100%; border-collapse: collapse; font-size: 14px; }
.tbl th, .tbl td { padding: 10px 12px; text-align: left; border-bottom: 1px solid var(--line); }
.tbl th { color: var(--muted); font-weight: 600; }
.col-cover { width: 72px; }
.col-date { width: 110px; color: var(--muted); }
.col-like { width: 48px; text-align: center; }
.col-act { width: 190px; text-align: right; white-space: nowrap; }
.ttl { font-weight: 600; color: var(--ink); }
.thumb { width: 56px; height: 42px; object-fit: cover; border-radius: 6px; }

.btn-primary {
  padding: 9px 16px; border: none; border-radius: 9px;
  background: var(--leaf); color: #fff; font-size: 14px; cursor: pointer;
}
.btn-primary:hover { background: var(--leaf-dark); }
.btn-sm {
  padding: 5px 10px; margin-left: 6px; border: 1px solid var(--line);
  border-radius: 7px; background: #fff; font-size: 13px; cursor: pointer;
}
.btn-sm:hover { border-color: var(--leaf); color: var(--leaf-dark); }
.btn-danger:hover { border-color: #c0392b; color: #c0392b; }

.pager { display: flex; gap: 6px; justify-content: center; margin-top: 20px; }
.pager button {
  min-width: 34px; padding: 5px 10px; border: 1px solid var(--line);
  background: #fff; border-radius: 7px; cursor: pointer;
}
.pager button.active { background: var(--leaf); color: #fff; border-color: var(--leaf); }
.pager button:disabled { opacity: .4; cursor: not-allowed; }
</style>
