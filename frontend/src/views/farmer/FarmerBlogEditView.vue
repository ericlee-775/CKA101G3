<script setup>
// 小農後台：產地日記 新增 / 編輯（整頁編輯器）。有 :id = 編輯，無 = 新增
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import blogApi from '@/api/blog'
import RichTextEditor from '@/components/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()

const blogId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => blogId.value !== null)

const form = reactive({ blogTitle: '', blogContent: '', blogImg: null })
const filePreview = ref('')   // 新選圖的本機預覽
const currentCover = ref('')  // 編輯時的原封面
const loading = ref(false)
const saving = ref(false)
const formError = ref('')

async function loadForEdit() {
  loading.value = true
  try {
    const b = await blogApi.getMine(blogId.value)
    form.blogTitle = b.blogTitle
    form.blogContent = b.blogContent
    currentCover.value = `/api/blogs/${b.blogId}/image?v=${Date.now()}`
  } catch (e) {
    formError.value = e.message
  } finally {
    loading.value = false
  }
}

function onFileChange(e) {
  const file = e.target.files?.[0] || null
  form.blogImg = file
  filePreview.value = file ? URL.createObjectURL(file) : ''
}

async function save() {
  formError.value = ''
  const contentText = form.blogContent.replace(/<[^>]*>/g, '').trim()
  if (!form.blogTitle.trim() || !contentText) {
    formError.value = '標題與內容為必填'
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await blogApi.updateMine(blogId.value, form)
      router.push(`/farmer/blog/${blogId.value}`)
    } else {
      const created = await blogApi.createMine(form)
      router.push(`/farmer/blog/${created.blogId}`)
    }
  } catch (e) {
    formError.value = e.message || '儲存失敗'
  } finally {
    saving.value = false
  }
}

function cancel() {
  router.push('/farmer/blog')
}

onMounted(() => {
  if (isEdit.value) loadForEdit()
})
</script>

<template>
  <main class="farmer-page">
    <header class="page-head">
      <h1>🌱 {{ isEdit ? '編輯產地日記' : '新增產地日記' }}</h1>
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
          <input v-model="form.blogTitle" type="text" maxlength="100" placeholder="今天的農場…" />
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

        <p v-if="formError" class="err">{{ formError }}</p>
      </template>
    </section>
  </main>
</template>

<style scoped>
.farmer-page { padding: 32px 24px; max-width: 900px; }
.page-head {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
}
.page-head h1 { margin: 0; font-size: 24px; color: var(--ink); }
.head-actions { display: flex; gap: 10px; }

.card {
  background: #fff; border: 1px solid var(--line); border-radius: 16px;
  box-shadow: var(--shadow); padding: 24px; border-top: 3px solid var(--leaf);
  display: flex; flex-direction: column; gap: 18px;
}
.hint { margin: 0; color: var(--muted); }
.err { margin: 0; color: #c0392b; font-size: 14px; }

.field { display: flex; flex-direction: column; gap: 6px; font-size: 14px; color: var(--ink-soft); }
.field small { color: var(--muted); font-weight: 400; }
.field input[type=text] {
  padding: 11px 14px; border: 1px solid var(--line); border-radius: 10px;
  font-size: 16px; outline: none;
}
.field input[type=text]:focus { border-color: var(--leaf); }

.preview { width: 100%; max-height: 320px; object-fit: cover; border-radius: 12px; }

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
