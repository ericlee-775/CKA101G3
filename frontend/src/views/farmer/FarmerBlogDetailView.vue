<script setup>
// 小農後台：產地日記 詳情頁（整頁閱讀版型）
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import blogApi from '@/api/blog'
import { confirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()
const blogId = computed(() => Number(route.params.id))

const blog = ref(null)
const photos = ref([])           // 相簿 [{ blogPhotoId }]
const loadState = ref('loading') // loading | ready | error
const errorMsg = ref('')
const coverVer = ref(Date.now())
const coverUrl = computed(() => `/api/blogs/${blogId.value}/image?v=${coverVer.value}`)

async function load() {
  loadState.value = 'loading'
  try {
    blog.value = await blogApi.getMine(blogId.value)
    photos.value = (await blogApi.listPhotos(blogId.value)) || []
    loadState.value = 'ready'
  } catch (e) {
    errorMsg.value = e.message
    loadState.value = 'error'
  }
}

function fmt(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`
}

function goEdit() {
  router.push(`/farmer/blog/${blogId.value}/edit`)
}
function goBack() {
  router.push('/farmer/blog')
}
async function remove() {
  const ok = await confirm({
    title: '刪除產地日記',
    message: `確定要刪除「${blog.value.blogTitle}」嗎？此動作無法復原。`,
    confirmText: '刪除',
    danger: true,
  })
  if (!ok) return
  try {
    await blogApi.deleteMine(blogId.value)
    router.push('/farmer/blog')
  } catch (e) {
    alert(e.message || '刪除失敗')
  }
}

onMounted(load)
</script>

<template>
  <main class="detail-page">
    <div class="topbar">
      <button class="btn-ghost" @click="goBack">‹ 返回列表</button>
      <div class="topbar-actions" v-if="loadState === 'ready'">
        <button class="btn-ghost" @click="goEdit">編輯</button>
        <button class="btn-ghost btn-danger" @click="remove">刪除</button>
      </div>
    </div>

    <p v-if="loadState === 'loading'" class="hint">載入中…</p>
    <p v-else-if="loadState === 'error'" class="hint err">載入失敗：{{ errorMsg }}</p>

    <article v-else class="article">
      <img class="cover" :src="coverUrl"
           @error="$event.target.style.display = 'none'" alt="" />
      <span class="badge">產地日記</span>
      <h1 class="title">{{ blog.blogTitle }}</h1>
      <div class="meta">{{ fmt(blog.blogTime) }} ｜ ♡ {{ blog.blogLikeCount || 0 }}</div>
      <div class="content" v-html="blog.blogContent"></div>

      <!-- 相簿 -->
      <div v-if="photos.length" class="gallery">
        <img v-for="p in photos" :key="p.blogPhotoId"
             :src="blogApi.photoImgUrl(p.blogPhotoId)"
             @error="$event.target.style.display = 'none'" alt="" />
      </div>
    </article>
  </main>
</template>

<style scoped>
.detail-page { padding: 24px; }
.topbar {
  max-width: 760px; margin: 0 auto 16px;
  display: flex; align-items: center; justify-content: space-between;
}
.topbar-actions { display: flex; gap: 8px; }
.hint { text-align: center; color: var(--muted); }
.hint.err { color: #c0392b; }

.article {
  max-width: 760px; margin: 0 auto;
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: var(--shadow); padding: 0 0 32px; overflow: hidden;
}
.cover {
  width: 100%; max-height: 380px; object-fit: cover; display: block;
}
.badge {
  display: inline-block; margin: 24px 0 0 32px;
  padding: 4px 14px; border-radius: 999px;
  background: var(--leaf-soft, #e5f0dd); color: var(--leaf-dark, #3f6a23);
  font-size: 13px; font-weight: 600;
}
.title {
  margin: 12px 32px 6px; font-size: 28px; color: var(--ink); line-height: 1.3;
}
.meta { margin: 0 32px 20px; color: var(--muted); font-size: 13px; }
.content {
  margin: 0 32px; color: var(--ink-soft); font-size: 16px; line-height: 1.85;
}
/* 富文本內容排版 */
.content :deep(h3) { color: #16a34a; font-size: 20px; margin: 24px 0 8px; }
.content :deep(p) { margin: 0 0 14px; }
.content :deep(ul), .content :deep(ol) { margin: 0 0 14px; padding-left: 24px; }
.content :deep(a) { color: #2563eb; text-decoration: underline; }
.content :deep(img) { max-width: 100%; border-radius: 10px; }

/* 相簿 */
.gallery {
  margin: 24px 32px 0;
  display: grid; gap: 10px;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
}
.gallery img {
  width: 100%; height: 130px; object-fit: cover; border-radius: 10px;
  border: 1px solid var(--line);
}

.btn-ghost {
  padding: 8px 16px; border: 1px solid var(--line); border-radius: 9px;
  background: #fff; font-size: 14px; cursor: pointer;
}
.btn-ghost:hover { border-color: var(--leaf); color: var(--leaf-dark); }
.btn-danger:hover { border-color: #c0392b; color: #c0392b; }
</style>
