<script setup>
// 會員中心：文章詳情頁
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { memberBlogApi as blogApi, listBlogTypes } from '@/api/blog'
import { confirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()
const blogId = computed(() => Number(route.params.id))

const blog = ref(null)
const photos = ref([])
const typeName = ref('')
const loadState = ref('loading') // loading | ready | error
const errorMsg = ref('')
const coverVer = ref(Date.now())
const coverUrl = computed(() => `/api/blogs/${blogId.value}/image?v=${coverVer.value}`)
const lightboxSrc = ref('')       // 點相簿放大用；空=關閉
function openLightbox(photoId) { lightboxSrc.value = blogApi.photoImgUrl(photoId) }
function closeLightbox() { lightboxSrc.value = '' }

async function load() {
  loadState.value = 'loading'
  try {
    blog.value = await blogApi.getMine(blogId.value)
    photos.value = (await blogApi.listPhotos(blogId.value)) || []
    // 把 blogTypeId 對成分類名稱當標籤
    const types = (await listBlogTypes()) || []
    typeName.value = types.find((t) => t.blogTypeId === blog.value.blogTypeId)?.blogTypeName || '文章'
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
  router.push(`/member/blogs/${blogId.value}/edit`)
}
function goBack() {
  router.push('/member/blogs')
}
async function remove() {
  const ok = await confirm({
    title: '刪除文章',
    message: `確定要刪除「${blog.value.blogTitle}」嗎？此動作無法復原。`,
    confirmText: '刪除',
    danger: true,
  })
  if (!ok) return
  try {
    await blogApi.deleteMine(blogId.value)
    router.push('/member/blogs')
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
      <span class="badge">{{ typeName }}</span>
      <h1 class="title">{{ blog.blogTitle }}</h1>
      <div class="meta">{{ fmt(blog.blogTime) }} ｜ ♡ {{ blog.blogLikeCount || 0 }}</div>
      <div class="content" v-html="blog.blogContent"></div>

      <div v-if="photos.length" class="gallery">
        <img v-for="p in photos" :key="p.blogPhotoId"
             :src="blogApi.photoImgUrl(p.blogPhotoId)"
             @click="openLightbox(p.blogPhotoId)"
             @error="$event.target.style.display = 'none'" alt="" />
      </div>
    </article>

    <div v-if="lightboxSrc" class="lightbox" @click="closeLightbox">
      <img :src="lightboxSrc" alt="" />
      <button class="lightbox-x" @click.stop="closeLightbox">✕</button>
    </div>
  </main>
</template>

<style scoped>
.detail-page { }
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
.cover { width: 100%; max-height: 380px; object-fit: cover; display: block; }
.badge {
  display: inline-block; margin: 24px 0 0 32px;
  padding: 4px 14px; border-radius: 999px;
  background: var(--leaf-soft, #e5f0dd); color: var(--leaf-dark, #3f6a23);
  font-size: 13px; font-weight: 600;
}
.title { margin: 12px 32px 6px; font-size: 28px; color: var(--ink); line-height: 1.3; }
.meta { margin: 0 32px 20px; color: var(--muted); font-size: 13px; }
.content { margin: 0 32px; color: var(--ink-soft); font-size: 16px; line-height: 1.85; }
.content :deep(h3) { color: #16a34a; font-size: 20px; margin: 24px 0 8px; }
.content :deep(p) { margin: 0 0 14px; }
.content :deep(ul), .content :deep(ol) { margin: 0 0 14px; padding-left: 24px; }
.content :deep(a) { color: #2563eb; text-decoration: underline; }
.content :deep(img) { max-width: 100%; border-radius: 10px; }

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
.lightbox img { max-width: 92vw; max-height: 92vh; object-fit: contain; border-radius: 8px; }
.lightbox-x {
  position: fixed; top: 18px; right: 22px;
  width: 40px; height: 40px; border: none; border-radius: 50%;
  background: #fff2; color: #fff; font-size: 20px; cursor: pointer;
}
.lightbox-x:hover { background: #fff4; }

.btn-ghost {
  padding: 8px 16px; border: 1px solid var(--line); border-radius: 9px;
  background: #fff; font-size: 14px; cursor: pointer;
}
.btn-ghost:hover { border-color: var(--leaf); color: var(--leaf-dark); }
.btn-danger:hover { border-color: #c0392b; color: #c0392b; }
</style>
