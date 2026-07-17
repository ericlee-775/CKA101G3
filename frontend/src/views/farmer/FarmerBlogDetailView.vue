<script setup>
// 小農後台：產地日記 詳情頁（整頁閱讀版型）
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import blogApi, { publicBlogComments } from '@/api/blog'
import { confirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()
const blogId = computed(() => Number(route.params.id))

const blog = ref(null)
const photos = ref([])           // 相簿 [{ blogPhotoId }]
const comments = ref([])         // 讀者留言（唯讀）
const commentsFailed = ref(false)// 文章隱藏時公開留言端點會 404
const loadState = ref('loading') // loading | ready | error
const errorMsg = ref('')
const coverVer = ref(Date.now())
const coverUrl = computed(() => `/api/blogs/${blogId.value}/image?v=${coverVer.value}`)
// 相簿放大：存目前第幾張(index)，-1=關閉，才能左右切換
const lightboxIndex = ref(-1)
const lightboxSrc = computed(() => {
  const p = photos.value[lightboxIndex.value]
  return p ? blogApi.photoImgUrl(p.blogPhotoId) : ''
})
function openLightbox(idx) { lightboxIndex.value = idx }
function closeLightbox() { lightboxIndex.value = -1 }
function nextPhoto() {
  if (!photos.value.length) return
  lightboxIndex.value = (lightboxIndex.value + 1) % photos.value.length
}
function prevPhoto() {
  if (!photos.value.length) return
  lightboxIndex.value = (lightboxIndex.value - 1 + photos.value.length) % photos.value.length
}
function onLightboxKey(e) {
  if (lightboxIndex.value < 0) return
  if (e.key === 'ArrowRight') nextPhoto()
  else if (e.key === 'ArrowLeft') prevPhoto()
  else if (e.key === 'Escape') closeLightbox()
}

async function load() {
  loadState.value = 'loading'
  try {
    blog.value = await blogApi.getMine(blogId.value)
    photos.value = (await blogApi.listPhotos(blogId.value)) || []
    loadState.value = 'ready'
  } catch (e) {
    errorMsg.value = e.message
    loadState.value = 'error'
    return
  }
  // 留言獨立抓：失敗(文章隱藏時公開留言端點會 404)不影響整頁載入
  try {
    comments.value = (await publicBlogComments(blogId.value)) || []
    commentsFailed.value = false
  } catch {
    comments.value = []
    commentsFailed.value = true
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

onMounted(() => {
  load()
  window.addEventListener('keydown', onLightboxKey)
})
onUnmounted(() => window.removeEventListener('keydown', onLightboxKey))
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
        <img v-for="(p, idx) in photos" :key="p.blogPhotoId"
             :src="blogApi.photoImgUrl(p.blogPhotoId)"
             @click="openLightbox(idx)"
             @error="$event.target.style.display = 'none'" alt="" />
      </div>

      <!-- 留言（唯讀：小農檢視讀者留言） -->
      <section class="comments">
        <h3 class="c-title">留言（{{ comments.length }}）</h3>
        <p v-if="commentsFailed" class="c-empty">目前無法載入留言（文章隱藏時無法讀取）。</p>
        <ul v-else-if="comments.length" class="c-list">
          <li v-for="c in comments" :key="c.commentId">
            <div class="c-head">{{ c.authorName || '會員' }} · {{ fmt(c.commentTime) }}</div>
            <div class="c-body">{{ c.commentPost }}</div>
          </li>
        </ul>
        <p v-else class="c-empty">目前還沒有留言。</p>
      </section>
    </article>

    <div v-if="lightboxIndex >= 0" class="lightbox" @click="closeLightbox">
      <button v-if="photos.length > 1" class="lb-nav lb-prev" @click.stop="prevPhoto" aria-label="上一張">‹</button>
      <img :src="lightboxSrc" alt="" @click.stop="nextPhoto" />
      <button v-if="photos.length > 1" class="lb-nav lb-next" @click.stop="nextPhoto" aria-label="下一張">›</button>
      <button class="lightbox-x" @click.stop="closeLightbox">✕</button>
      <div v-if="photos.length > 1" class="lb-count">{{ lightboxIndex + 1 }} / {{ photos.length }}</div>
    </div>
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
  border: 1px solid var(--line); cursor: zoom-in; transition: transform .15s ease;
}
.gallery img:hover { transform: scale(1.02); }

.lightbox {
  position: fixed; inset: 0; z-index: 100;
  background: #000d; display: grid; place-items: center; padding: 24px; cursor: zoom-out;
}
.lightbox img { max-width: 92vw; max-height: 92vh; object-fit: contain; border-radius: 8px; cursor: pointer; }
.lightbox-x {
  position: fixed; top: 18px; right: 22px;
  width: 40px; height: 40px; border: none; border-radius: 50%;
  background: #fff2; color: #fff; font-size: 20px; cursor: pointer;
}
.lightbox-x:hover { background: #fff4; }
.lb-nav {
  position: fixed; top: 50%; transform: translateY(-50%);
  width: 52px; height: 52px; border: none; border-radius: 50%;
  background: #fff2; color: #fff; font-size: 30px; line-height: 1; cursor: pointer;
  display: grid; place-items: center;
}
.lb-nav:hover { background: #fff4; }
.lb-prev { left: 22px; }
.lb-next { right: 22px; }
.lb-count {
  position: fixed; bottom: 22px; left: 50%; transform: translateX(-50%);
  color: #fff; background: #0006; padding: 4px 12px; border-radius: 999px; font-size: 13px;
}

.btn-ghost {
  padding: 8px 16px; border: 1px solid var(--line); border-radius: 9px;
  background: #fff; font-size: 14px; cursor: pointer;
}
.btn-ghost:hover { border-color: var(--leaf); color: var(--leaf-dark); }
.btn-danger:hover { border-color: #c0392b; color: #c0392b; }

/* 留言（唯讀） */
.comments { margin: 28px 32px 0; }
.c-title { font-size: 16px; color: var(--ink); margin: 0 0 12px; }
.c-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px; }
.c-list li { border: 1px solid var(--line); border-radius: 10px; padding: 12px 14px; }
.c-head { font-size: 13px; color: var(--muted); margin-bottom: 6px; }
.c-body { color: var(--ink); font-size: 15px; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
.c-empty { color: var(--muted); font-size: 14px; }
</style>
