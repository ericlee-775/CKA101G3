<script setup>
// 會員中心：撰寫 / 編輯文章。有 :id = 編輯，無 = 新增
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { memberBlogApi as blogApi, listBlogTypes } from '@/api/blog'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { confirm } from '@/composables/useConfirm'

const FARM_DIARY_TYPE = 1 // 產地日記：會員不可選

const route = useRoute()
const router = useRouter()

const blogId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => blogId.value !== null)

const form = reactive({ blogTitle: '', blogContent: '', blogTypeId: null, blogImg: null })
const types = ref([])         // 可選分類（已排除產地日記）
const filePreview = ref('')
const currentCover = ref('')
const loading = ref(false)
const saving = ref(false)
const formError = ref('')

/* ===== 相簿 ===== */
const newPhotos = ref([])
const newPhotoPreviews = ref([])
const existingPhotos = ref([])
const photoBusy = ref(false)

async function loadTypes() {
  const all = (await listBlogTypes()) || []
  types.value = all.filter((t) => t.blogTypeId !== FARM_DIARY_TYPE) // 會員不能發產地日記
  // 新增模式：預設選第一個可用分類
  if (!isEdit.value && types.value.length) form.blogTypeId = types.value[0].blogTypeId
}

async function loadForEdit() {
  const b = await blogApi.getMine(blogId.value)
  form.blogTitle = b.blogTitle
  form.blogContent = b.blogContent
  form.blogTypeId = b.blogTypeId
  currentCover.value = `/api/blogs/${b.blogId}/image?v=${Date.now()}`
  await loadPhotos()
}

async function loadPhotos() {
  existingPhotos.value = (await blogApi.listPhotos(blogId.value)) || []
}

function onFileChange(e) {
  const file = e.target.files?.[0] || null
  form.blogImg = file
  filePreview.value = file ? URL.createObjectURL(file) : ''
}

function onAlbumChange(e) {
  const files = Array.from(e.target.files || [])
  e.target.value = ''
  if (!files.length) return
  if (isEdit.value) {
    uploadMore(files)
  } else {
    newPhotos.value.push(...files)
    files.forEach((f) => newPhotoPreviews.value.push(URL.createObjectURL(f)))
  }
}

async function uploadMore(files) {
  photoBusy.value = true
  try {
    await blogApi.addPhotos(blogId.value, files)
    await loadPhotos()
  } catch (e) {
    alert(e.message || '照片上傳失敗')
  } finally {
    photoBusy.value = false
  }
}

function removeNewPhoto(i) {
  newPhotos.value.splice(i, 1)
  newPhotoPreviews.value.splice(i, 1)
}

async function removeExisting(photoId) {
  const ok = await confirm({ title: '刪除照片', message: '確定刪除這張照片？', confirmText: '刪除', danger: true })
  if (!ok) return
  photoBusy.value = true
  try {
    await blogApi.deletePhoto(photoId)
    await loadPhotos()
  } catch (e) {
    alert(e.message || '刪除失敗')
  } finally {
    photoBusy.value = false
  }
}

async function save() {
  formError.value = ''
  const contentText = form.blogContent.replace(/<[^>]*>/g, '').trim()
  if (!form.blogTitle.trim() || !contentText) {
    formError.value = '標題與內容為必填'
    return
  }
  if (!form.blogTypeId) {
    formError.value = '請選擇文章分類'
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await blogApi.updateMine(blogId.value, form)
      router.push(`/member/blogs/${blogId.value}`)
    } else {
      const created = await blogApi.createMine({ ...form, photos: newPhotos.value })
      router.push(`/member/blogs/${created.blogId}`)
    }
  } catch (e) {
    formError.value = e.message || '儲存失敗'
  } finally {
    saving.value = false
  }
}

function cancel() {
  router.push('/member/blogs')
}

onMounted(async () => {
  loading.value = true
  try {
    await loadTypes()
    if (isEdit.value) await loadForEdit()
  } catch (e) {
    formError.value = e.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="mb-edit">
    <header class="page-head">
      <h1>✍️ {{ isEdit ? '編輯文章' : '撰寫文章' }}</h1>
      <div class="head-actions">
        <button class="btn-ghost" @click="cancel">取消</button>
        <button class="btn-primary" :disabled="saving" @click="save">
          {{ saving ? '儲存中…' : '儲存' }}
        </button>
      </div>
    </header>

    <section class="card">
      <p v-if="loading" class="hint">載入中…</p>

      <template v-else>
        <label class="field">
          <span>標題</span>
          <input v-model="form.blogTitle" type="text" maxlength="100" placeholder="想分享的主題…" />
        </label>

        <label class="field">
          <span>分類</span>
          <select v-model="form.blogTypeId">
            <option v-for="t in types" :key="t.blogTypeId" :value="t.blogTypeId">
              {{ t.blogTypeName }}
            </option>
          </select>
        </label>

        <div class="field">
          <span>內容</span>
          <RichTextEditor v-model="form.blogContent" />
        </div>

        <div class="field">
          <span>封面圖<small v-if="isEdit">（不選＝沿用原圖）</small></span>
          <input type="file" accept="image/*" @change="onFileChange" />
        </div>

        <img v-if="filePreview" class="preview" :src="filePreview" alt="預覽" />
        <img v-else-if="isEdit && currentCover" class="preview" :src="currentCover"
             @error="$event.target.style.display = 'none'" alt="原封面" />

        <!-- 相簿 -->
        <div class="field">
          <span>
            相簿（多張）
            <small v-if="isEdit">（選檔即上傳）</small>
            <small v-else>（存檔時一起上傳）</small>
          </span>
          <input type="file" accept="image/*" multiple :disabled="photoBusy" @change="onAlbumChange" />
        </div>

        <div class="album" v-if="isEdit ? existingPhotos.length : newPhotoPreviews.length">
          <template v-if="isEdit">
            <div class="album-item" v-for="p in existingPhotos" :key="p.blogPhotoId">
              <img :src="blogApi.photoImgUrl(p.blogPhotoId)" alt="" />
              <button class="album-x" title="刪除" @click="removeExisting(p.blogPhotoId)">✕</button>
            </div>
          </template>
          <template v-else>
            <div class="album-item" v-for="(src, i) in newPhotoPreviews" :key="i">
              <img :src="src" alt="" />
              <button class="album-x" title="移除" @click="removeNewPhoto(i)">✕</button>
            </div>
          </template>
        </div>

        <p v-if="formError" class="err">{{ formError }}</p>
      </template>
    </section>
  </main>
</template>

<style scoped>
.page-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px;
}
.page-head h1 { margin: 0; font-size: 22px; color: var(--ink); }
.head-actions { display: flex; gap: 10px; }

.card {
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: var(--shadow); padding: 22px; border-top: 3px solid var(--leaf);
  display: flex; flex-direction: column; gap: 18px;
}
.hint { margin: 0; color: var(--muted); }
.err { margin: 0; color: #c0392b; font-size: 14px; }

.field { display: flex; flex-direction: column; gap: 6px; font-size: 14px; color: var(--ink-soft); }
.field small { color: var(--muted); font-weight: 400; }
.field input[type=text], .field select {
  padding: 11px 14px; border: 1px solid var(--line); border-radius: 10px;
  font-size: 16px; outline: none; background: #fff;
}
.field input[type=text]:focus, .field select:focus { border-color: var(--leaf); }

.preview { width: 100%; max-height: 320px; object-fit: cover; border-radius: 12px; }

.album {
  display: grid; gap: 10px;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
}
.album-item { position: relative; }
.album-item img {
  width: 100%; height: 100px; object-fit: cover; border-radius: 8px; border: 1px solid var(--line);
}
.album-x {
  position: absolute; top: 4px; right: 4px;
  width: 22px; height: 22px; border: none; border-radius: 50%;
  background: #000a; color: #fff; cursor: pointer; font-size: 12px; line-height: 1;
}
.album-x:hover { background: #c0392b; }

.btn-primary {
  padding: 9px 18px; border: none; border-radius: 9px;
  background: var(--leaf); color: #fff; font-size: 14px; cursor: pointer;
}
.btn-primary:hover { background: var(--leaf-dark); }
.btn-primary:disabled { opacity: .6; cursor: not-allowed; }
.btn-ghost {
  padding: 9px 18px; border: 1px solid var(--line); border-radius: 9px;
  background: #fff; font-size: 14px; cursor: pointer;
}
</style>
