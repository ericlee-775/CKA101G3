<script setup>
// 公開部落格單篇詳情頁 — GET /api/blogs/{id} + 照片集，任何人可讀
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import authStore from '@/stores/auth'
import {
  getPublicBlog, publicBlogPhotos, listBlogTypes,
  publicBlogComments, addBlogComment, deleteBlogComment, likeBlog, blogLikeStatus,
  reportBlog, reportComment,
} from '@/api/blog'
import { confirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()
const blogId = computed(() => Number(route.params.blogId))

const blog = ref(null)
const photos = ref([])
const typeName = ref('')
const loadState = ref('loading') // loading | ready | error
const errorMsg = ref('')
// 相簿放大：存「目前第幾張」的 index(-1=關閉)，才能左右切換
const lightboxIndex = ref(-1)
const lightboxSrc = computed(() => {
  const p = photos.value[lightboxIndex.value]
  return p ? `/api/photos/${p.blogPhotoId}/image` : ''
})

// 留言 / 按讚
const comments = ref([])
const newComment = ref('')
const commenting = ref(false)
const liking = ref(false)
const liked = ref(false)   // 目前會員按過沒（決定畫實心♥還空心♡）
// 只有登入的「會員」能留言/按讚（後端用 userId）
const canInteract = computed(() => authStore.isLoggedIn && authStore.isMember)
// 目前登入會員的 userId，用來判斷「這則留言是不是我的」
const myUserId = computed(() => authStore.state.user?.userId ?? null)

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
// 鍵盤：← → 切換、Esc 關閉（只在燈箱開著時作用）
function onLightboxKey(e) {
  if (lightboxIndex.value < 0) return
  if (e.key === 'ArrowRight') nextPhoto()
  else if (e.key === 'ArrowLeft') prevPhoto()
  else if (e.key === 'Escape') closeLightbox()
}

async function loadComments() {
  try {
    comments.value = (await publicBlogComments(blogId.value)) || []
  } catch {
    comments.value = []
  }
}

async function toggleLike() {
  if (!canInteract.value) { alert('請先登入會員才能按讚'); return }
  if (liking.value) return          // 防連點造成請求打架
  // 樂觀更新：先翻實心/空心 + 數字即時 ±1，感覺零延遲
  const prevLiked = liked.value
  const prevCount = blog.value.blogLikeCount || 0
  liked.value = !prevLiked
  blog.value.blogLikeCount = prevCount + (prevLiked ? -1 : 1)
  liking.value = true
  try {
    const updated = await likeBlog(blogId.value)   // userId 由後端 session 取
    liked.value = !!updated.liked                  // 以後端為準校正
    blog.value.blogLikeCount = updated.blogLikeCount
  } catch (e) {
    liked.value = prevLiked                         // 失敗回滾
    blog.value.blogLikeCount = prevCount
    alert('操作失敗：' + e.message)
  } finally {
    liking.value = false
  }
}

async function submitComment() {
  const text = newComment.value.trim()
  if (!text) return
  commenting.value = true
  try {
    await addBlogComment(blogId.value, text)
    newComment.value = ''
    await loadComments()
  } catch (e) {
    alert('留言失敗：' + e.message)
  } finally {
    commenting.value = false
  }
}

// 刪除自己的留言（後端會再驗一次只能刪本人的）
async function removeComment(commentId) {
  const ok = await confirm({
    title: '刪除留言',
    message: '確定要刪除這則留言嗎？此動作無法復原。',
    confirmText: '刪除',
    danger: true,
  })
  if (!ok) return
  try {
    await deleteBlogComment(commentId)
    await loadComments()
  } catch (e) {
    alert('刪除失敗：' + e.message)
  }
}

async function load() {
  loadState.value = 'loading'
  try {
    blog.value = await getPublicBlog(blogId.value)
    photos.value = (await publicBlogPhotos(blogId.value)) || []

    // 分類名稱當標籤
    const types = await listBlogTypes()
    typeName.value = types.find((t) => t.blogTypeId === blog.value.blogTypeId)?.blogTypeName || '文章'

    await loadComments()

    // 登入會員才查「我按過沒」，同步實心/空心與數字
    if (canInteract.value) {
      try {
        const s = await blogLikeStatus(blogId.value)
        liked.value = !!s.liked
        blog.value.blogLikeCount = s.blogLikeCount
      } catch { /* 查不到就維持預設空心，不影響閱讀 */ }
    }
    loadState.value = 'ready'
  } catch (e) {
    errorMsg.value = e.message
    loadState.value = 'error'
  }
}

/* ===== 檢舉 ===== */
const reportTarget = ref(null)   // null=關閉；{type:'blog'} 或 {type:'comment', commentId}
const reportReason = ref('')
const reporting = ref(false)

function openReportBlog() {
  if (!canInteract.value) { alert('請先登入會員才能檢舉'); return }
  reportReason.value = ''
  reportTarget.value = { type: 'blog' }
}
function openReportComment(commentId) {
  if (!canInteract.value) { alert('請先登入會員才能檢舉'); return }
  reportReason.value = ''
  reportTarget.value = { type: 'comment', commentId }
}
function closeReport() { reportTarget.value = null }

async function submitReport() {
  const reason = reportReason.value.trim()
  if (!reason) { alert('請填寫檢舉原因'); return }
  reporting.value = true
  try {
    if (reportTarget.value.type === 'blog') {
      await reportBlog(blogId.value, reason)
    } else {
      await reportComment(reportTarget.value.commentId, reason)
    }
    reportTarget.value = null
    alert('已送出檢舉，感謝回報')
  } catch (e) {
    alert('檢舉失敗：' + e.message)
  } finally {
    reporting.value = false
  }
}

function fmt(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`
}

function goBack() {
  router.push('/blogs')
}

onMounted(() => {
  // 以後端 session 為準重新確認身分，避免 localStorage 舊 role 讓非會員誤以為能互動
  authStore.hydrate()
  load()
  window.addEventListener('keydown', onLightboxKey)
})
onUnmounted(() => window.removeEventListener('keydown', onLightboxKey))
</script>

<template>
  <main class="detail-page">
    <div class="topbar">
      <button class="btn-ghost" @click="goBack">‹ 回部落格</button>
    </div>

    <p v-if="loadState === 'loading'" class="hint">載入中…</p>
    <p v-else-if="loadState === 'error'" class="hint err">載入失敗：{{ errorMsg }}</p>

    <article v-else class="article">
      <img class="cover" :src="`/api/blogs/${blogId}/image`"
           @error="$event.target.style.display = 'none'" alt="" />
      <span class="badge">{{ typeName }}</span>
      <h1 class="title">{{ blog.blogTitle }}</h1>
      <div class="meta">
        {{ fmt(blog.blogTime) }} ｜
        <button class="like-btn" :class="{ liked }" :disabled="liking || !canInteract"
                @click="toggleLike"
                :title="canInteract ? (liked ? '取消讚' : '按讚') : '登入會員後可按讚'">
          {{ liked ? '♥' : '♡' }} {{ blog.blogLikeCount || 0 }}
        </button>
        <button v-if="canInteract" class="report-link" @click="openReportBlog" title="檢舉文章">⚑ 檢舉</button>
      </div>
      <div class="content" v-html="blog.blogContent"></div>

      <div v-if="photos.length" class="gallery">
        <img v-for="(p, idx) in photos" :key="p.blogPhotoId"
             :src="`/api/photos/${p.blogPhotoId}/image`"
             @click="openLightbox(idx)"
             @error="$event.target.style.display = 'none'" alt="" />
      </div>

      <!-- 留言 -->
      <section class="comments">
        <h3 class="c-title">留言（{{ comments.length }}）</h3>

        <div v-if="canInteract" class="comment-form">
          <textarea v-model="newComment" rows="2" placeholder="留下你的想法…"></textarea>
          <button class="btn-primary" :disabled="commenting" @click="submitComment">
            {{ commenting ? '送出中…' : '送出' }}
          </button>
        </div>
        <p v-else class="c-hint">登入會員後即可留言、按讚</p>

        <ul v-if="comments.length" class="c-list">
          <li v-for="c in comments" :key="c.commentId">
            <div class="c-head">
              會員 · {{ fmt(c.commentTime) }}
              <button v-if="myUserId && c.userId === myUserId" class="report-link"
                      @click="removeComment(c.commentId)" title="刪除留言">🗑 刪除</button>
              <button v-else-if="canInteract" class="report-link"
                      @click="openReportComment(c.commentId)" title="檢舉留言">⚑ 檢舉</button>
            </div>
            <div class="c-body">{{ c.commentPost }}</div>
          </li>
        </ul>
        <p v-else class="c-empty">還沒有留言，來當第一個吧。</p>
      </section>
    </article>

    <!-- 放大檢視：點背景關閉、點圖片或 ‹ › 切換、鍵盤 ← → Esc -->
    <div v-if="lightboxIndex >= 0" class="lightbox" @click="closeLightbox">
      <button v-if="photos.length > 1" class="lb-nav lb-prev" @click.stop="prevPhoto" aria-label="上一張">‹</button>
      <img :src="lightboxSrc" alt="" @click.stop="nextPhoto" />
      <button v-if="photos.length > 1" class="lb-nav lb-next" @click.stop="nextPhoto" aria-label="下一張">›</button>
      <button class="lightbox-x" @click.stop="closeLightbox">✕</button>
      <div v-if="photos.length > 1" class="lb-count">{{ lightboxIndex + 1 }} / {{ photos.length }}</div>
    </div>

    <!-- 檢舉 modal -->
    <div v-if="reportTarget" class="report-mask" @click.self="closeReport">
      <div class="report-box">
        <h3>{{ reportTarget.type === 'blog' ? '檢舉文章' : '檢舉留言' }}</h3>
        <textarea v-model="reportReason" rows="3" placeholder="請說明檢舉原因…"></textarea>
        <div class="report-actions">
          <button class="btn-ghost" @click="closeReport">取消</button>
          <button class="btn-primary" :disabled="reporting" @click="submitReport">
            {{ reporting ? '送出中…' : '送出檢舉' }}
          </button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.detail-page { padding: 32px clamp(18px, 4vw, 56px); }
.topbar { max-width: 760px; margin: 0 auto 16px; }
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
  border: 1px solid var(--line); cursor: zoom-in;
  transition: transform .15s ease;
}
.gallery img:hover { transform: scale(1.02); }

/* 放大檢視 */
.lightbox {
  position: fixed; inset: 0; z-index: 100;
  background: #000d; display: grid; place-items: center; padding: 24px;
  cursor: zoom-out;
}
.lightbox img {
  max-width: 92vw; max-height: 92vh; object-fit: contain; border-radius: 8px;
  cursor: pointer;   /* 點圖=下一張 */
}
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

/* 按讚 */
.like-btn {
  border: 1px solid var(--line); background: #fff; border-radius: 999px;
  padding: 3px 12px; font-size: 13px; cursor: pointer; color: var(--ink-soft);
  transition: color .15s, background .15s, border-color .15s;
}
.like-btn:hover { border-color: #e0679a; color: #e0679a; }
.like-btn.liked { border-color: #e0679a; background: #fdeef4; color: #d94b86; font-weight: 600; }
.like-btn:disabled { cursor: not-allowed; }

/* 留言 */
.comments { margin: 28px 32px 0; }
.c-title { font-size: 16px; color: var(--ink); margin: 0 0 12px; }
.comment-form { display: flex; gap: 10px; margin-bottom: 16px; }
.comment-form textarea {
  flex: 1; padding: 10px 12px; border: 1px solid var(--line); border-radius: 10px;
  font-size: 14px; outline: none; font-family: inherit; resize: vertical;
}
.comment-form textarea:focus { border-color: var(--leaf); }
.btn-primary {
  align-self: flex-start; padding: 9px 16px; border: none; border-radius: 9px;
  background: var(--leaf); color: #fff; font-size: 14px; cursor: pointer;
}
.btn-primary:hover { background: var(--leaf-dark); }
.btn-primary:disabled { opacity: .6; cursor: not-allowed; }
.c-hint, .c-empty { color: var(--muted); font-size: 14px; margin: 0 0 12px; }
.c-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
.c-list li { border-top: 1px solid var(--line); padding-top: 12px; }
.c-head { color: var(--muted); font-size: 12px; margin-bottom: 4px; }
.c-body { color: var(--ink-soft); font-size: 15px; line-height: 1.6; white-space: pre-wrap; }

/* 檢舉：小連結按鈕 */
.report-link {
  border: none; background: none; color: var(--muted); cursor: pointer;
  font-size: 12px; padding: 0 0 0 8px;
}
.report-link:hover { color: #c0392b; }

/* 檢舉 modal */
.report-mask {
  position: fixed; inset: 0; z-index: 90; background: #0007;
  display: grid; place-items: center; padding: 20px;
}
.report-box {
  width: 100%; max-width: 420px; background: #fff; border-radius: 14px; padding: 20px;
}
.report-box h3 { margin: 0 0 12px; font-size: 17px; color: var(--ink); }
.report-box textarea {
  width: 100%; box-sizing: border-box; padding: 10px 12px;
  border: 1px solid var(--line); border-radius: 10px;
  font-size: 14px; outline: none; font-family: inherit; resize: vertical;
}
.report-box textarea:focus { border-color: var(--leaf); }
.report-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 14px; }
</style>
