<script setup>
// 零依賴富文本編輯器（contenteditable + 工具列）
// v-model 綁 HTML 字串。刻意不做內嵌圖片，避免 base64 撐爆 TEXT(64KB)。
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

const editor = ref(null)

// 初始把外部值塞進來
onMounted(() => {
  if (editor.value) editor.value.innerHTML = props.modelValue || ''
})

// 外部值變了（例如切換到另一篇編輯）才覆寫，避免打字時游標亂跳
watch(
  () => props.modelValue,
  (val) => {
    if (editor.value && val !== editor.value.innerHTML) {
      editor.value.innerHTML = val || ''
    }
  }
)

// 打字時把最新 HTML 吐回去
function onInput() {
  emit('update:modelValue', editor.value.innerHTML)
}

// 執行格式化指令（execCommand 雖已標記過時，但各家瀏覽器仍支援，學校專案夠用）
function cmd(command, value = null) {
  document.execCommand(command, false, value)
  editor.value.focus()
  onInput()
}

function setColor(color) {
  cmd('foreColor', color)
}

function addLink() {
  const url = prompt('輸入連結網址（含 https://）')
  if (url) cmd('createLink', url)
}
</script>

<template>
  <div class="rte">
    <!-- 工具列。用 mousedown.prevent 才不會按按鈕時失去選取範圍 -->
    <div class="rte-toolbar" @mousedown.prevent>
      <button type="button" title="標題" @click="cmd('formatBlock', 'H3')"><b>標題</b></button>
      <button type="button" title="內文" @click="cmd('formatBlock', 'P')">內文</button>
      <span class="sep"></span>
      <button type="button" title="粗體" @click="cmd('bold')"><b>B</b></button>
      <button type="button" title="斜體" @click="cmd('italic')"><i>I</i></button>
      <button type="button" title="底線" @click="cmd('underline')"><u>U</u></button>
      <span class="sep"></span>
      <button type="button" class="dot green" title="綠色" @click="setColor('#16a34a')"></button>
      <button type="button" class="dot red" title="紅色" @click="setColor('#c0392b')"></button>
      <button type="button" class="dot ink" title="預設色" @click="setColor('#333333')"></button>
      <span class="sep"></span>
      <button type="button" title="項目清單" @click="cmd('insertUnorderedList')">• 清單</button>
      <button type="button" title="編號清單" @click="cmd('insertOrderedList')">1. 清單</button>
      <button type="button" title="插入連結" @click="addLink()">🔗</button>
      <span class="sep"></span>
      <button type="button" title="清除格式" @click="cmd('removeFormat')">清除</button>
    </div>

    <div
      ref="editor"
      class="rte-body"
      contenteditable="true"
      @input="onInput"
    ></div>
  </div>
</template>

<style scoped>
.rte {
  border: 1px solid var(--line, #ddd);
  border-radius: 9px;
  overflow: hidden;
}
.rte-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  background: #f7f8f5;
  border-bottom: 1px solid var(--line, #eee);
}
.rte-toolbar button {
  min-width: 30px;
  height: 28px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: #444;
}
.rte-toolbar button:hover {
  background: #e7ebe2;
}
.rte-toolbar .sep {
  width: 1px;
  height: 18px;
  background: var(--line, #ddd);
  margin: 0 4px;
}
.rte-toolbar .dot {
  width: 18px;
  height: 18px;
  min-width: 18px;
  padding: 0;
  border-radius: 50%;
  border: 1px solid #0002;
}
.rte-toolbar .dot.green { background: #16a34a; }
.rte-toolbar .dot.red { background: #c0392b; }
.rte-toolbar .dot.ink { background: #333; }

.rte-body {
  min-height: 300px;
  max-height: 55vh;
  overflow-y: auto;
  padding: 12px 14px;
  outline: none;
  font-size: 15px;
  line-height: 1.7;
  color: var(--ink-soft, #333);
}
/* 內容排版：h3 自動變綠色小標，貼近範例觀感 */
.rte-body :deep(h3) {
  color: #16a34a;
  font-size: 17px;
  margin: 14px 0 6px;
}
.rte-body :deep(p) { margin: 0 0 10px; }
.rte-body :deep(ul),
.rte-body :deep(ol) { margin: 0 0 10px; padding-left: 22px; }
.rte-body :deep(a) { color: #2563eb; text-decoration: underline; }
</style>
