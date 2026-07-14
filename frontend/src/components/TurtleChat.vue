<script setup>
// 全站浮動「烏龜客服」聊天泡泡 🐢
// 接後端 /api/ai/chat（記憶 + 商品 RAG），同源 fetch 自動帶 session cookie
import { ref, nextTick } from 'vue';

const open = ref(false);
const flipping = ref(false);   // 控制烏龜後空翻動畫
const input = ref('');
const loading = ref(false);
const listEl = ref(null);

// 對話訊息：role = 'user' | 'ai'
const messages = ref([
  { role: 'ai', text: '哈囉～我是小龜客服 🐢\n想找什麼農產品、有什麼購物問題都可以問我喔！' },
]);

function toggle() {
  open.value = !open.value;
  flipping.value = true;   // 每次點都讓烏龜後空翻一下
  if (open.value) scrollBottom();
}

async function scrollBottom() {
  await nextTick();
  if (listEl.value) listEl.value.scrollTop = listEl.value.scrollHeight;
}

async function send() {
  const text = input.value.trim();
  if (!text || loading.value) return;

  messages.value.push({ role: 'user', text });
  input.value = '';
  loading.value = true;
  scrollBottom();

  try {
    // 後端回純文字（text/plain）
    const res = await fetch(`/api/ai/chat?message=${encodeURIComponent(text)}`, {
      credentials: 'include', // 帶 session cookie，後端才知道是哪個會員/訪客
    });
    if (!res.ok) throw new Error('bad response');
    const reply = await res.text();
    messages.value.push({ role: 'ai', text: reply });
  } catch (e) {
    messages.value.push({ role: 'ai', text: '小龜迷路了 🐢💦 請稍後再試一次～' });
  } finally {
    loading.value = false;
    scrollBottom();
  }
}
</script>

<template>
  <div class="turtle-chat">
    <!-- 展開的聊天面板 -->
    <transition name="pop">
      <div v-if="open" class="panel">
        <!-- 標題列 -->
        <div class="panel-head">
          <span class="head-turtle">🐢</span>
          <div class="head-text">
            <strong>小龜客服</strong>
            <small>Farmily 農產小幫手</small>
          </div>
          <button class="close-btn" @click="toggle" aria-label="關閉">✕</button>
        </div>

        <!-- 訊息區 -->
        <div class="msg-list" ref="listEl">
          <div
            v-for="(m, i) in messages"
            :key="i"
            class="msg-row"
            :class="m.role"
          >
            <span v-if="m.role === 'ai'" class="avatar">🐢</span>
            <div class="bubble">{{ m.text }}</div>
          </div>

          <!-- AI 思考中：烏龜努力爬過來 🐢 -->
          <div v-if="loading" class="msg-row ai">
            <span class="avatar">🐢</span>
            <div class="bubble thinking">
              <div class="crawl-track">
                <span class="crawl-turtle">🐢</span>
              </div>
              <span class="thinking-text">小龜努力爬過來中…</span>
            </div>
          </div>
        </div>

        <!-- 輸入區 -->
        <div class="input-bar">
          <input
            v-model="input"
            type="text"
            placeholder="問問小龜…"
            :disabled="loading"
            @keyup.enter="send"
          />
          <button class="send-btn" :disabled="loading || !input.trim()" @click="send">
            送出
          </button>
        </div>
      </div>
    </transition>

    <!-- 浮動按鈕（烏龜）；點一下小龜會後空翻 -->
    <button class="fab" :class="{ 'is-open': open }" @click="toggle" aria-label="開啟客服">
      <span
        class="fab-turtle"
        :class="{ 'do-flip': flipping }"
        @animationend="flipping = false"
      >🐢</span>
    </button>
  </div>
</template>

<style scoped>
/* 主題色：烏龜殼綠 + 米白 */
.turtle-chat {
  --turtle-green: #5f8a4f;
  --turtle-green-dark: #47693c;
  --turtle-cream: #f6f3e7;
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9998;
  font-family: inherit;
}

/* ---------- 浮動按鈕 ---------- */
.fab {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  border: none;
  background: var(--turtle-green);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 0.2s ease, background 0.2s ease;
  animation: bob 2.4s ease-in-out infinite;
}
.fab:hover { background: var(--turtle-green-dark); transform: scale(1.06); }
.fab.is-open { animation: none; }
/* inline-block：transform（旋轉）對 inline 元素無效，要改成 inline-block 才會翻 */
.fab-turtle { font-size: 32px; line-height: 1; display: inline-block; }

/* 點擊時觸發：跳起來 + 往後翻一整圈 */
.fab-turtle.do-flip { animation: backflip 0.6s ease; }
@keyframes backflip {
  0%   { transform: translateY(0)     rotate(0deg); }
  50%  { transform: translateY(-18px) rotate(-180deg); }
  100% { transform: translateY(0)     rotate(-360deg); }
}

/* 按鈕待機時上下輕輕浮動 */
@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* ---------- 聊天面板 ---------- */
.panel {
  position: absolute;
  right: 0;
  bottom: 74px;
  width: 330px;
  max-width: calc(100vw - 32px);
  height: 460px;
  max-height: calc(100vh - 120px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--turtle-green);
  color: #fff;
}
.head-turtle { font-size: 26px; }
.head-text { display: flex; flex-direction: column; line-height: 1.2; flex: 1; }
.head-text small { opacity: 0.85; font-size: 11px; }
.close-btn {
  background: transparent; border: none; color: #fff;
  font-size: 16px; cursor: pointer; padding: 4px 6px; border-radius: 6px;
}
.close-btn:hover { background: rgba(255, 255, 255, 0.2); }

/* ---------- 訊息區 ---------- */
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: var(--turtle-cream);
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.msg-row { display: flex; align-items: flex-end; gap: 6px; }
.msg-row.user { flex-direction: row-reverse; }
.avatar { font-size: 22px; flex-shrink: 0; }

.bubble {
  max-width: 78%;
  padding: 9px 12px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;   /* 保留換行 */
  word-break: break-word;
}
.msg-row.ai .bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.msg-row.user .bubble {
  background: var(--turtle-green);
  color: #fff;
  border-bottom-right-radius: 4px;
}

/* ---------- 烏龜爬行 loading ---------- */
.bubble.thinking { display: flex; flex-direction: column; gap: 4px; min-width: 150px; }
.crawl-track {
  position: relative;
  height: 22px;
  border-bottom: 2px dashed #c9c2a6;
  overflow: hidden;
}
.crawl-turtle {
  position: absolute;
  bottom: 0;
  left: 0;
  font-size: 20px;
  animation: crawl 2.2s linear infinite;
}
.thinking-text { font-size: 12px; color: #8a835f; }

/* 烏龜從左爬到右 + 爬行時身體上下小擺動
   scaleX(-1)：把 🐢（預設頭朝左）水平翻轉成朝右，才是往前爬不是倒退嚕 */
@keyframes crawl {
  0%   { left: -24px;  transform: scaleX(-1) translateY(0)   rotate(0deg); }
  25%  {               transform: scaleX(-1) translateY(-2px) rotate(-3deg); }
  50%  {               transform: scaleX(-1) translateY(0)   rotate(0deg); }
  75%  {               transform: scaleX(-1) translateY(-2px) rotate(3deg); }
  100% { left: 100%;   transform: scaleX(-1) translateY(0)   rotate(0deg); }
}

/* ---------- 輸入區 ---------- */
.input-bar {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid #eee;
  background: #fff;
}
.input-bar input {
  flex: 1;
  border: 1px solid #d8d3bf;
  border-radius: 20px;
  padding: 8px 14px;
  font-size: 14px;
  outline: none;
}
.input-bar input:focus { border-color: var(--turtle-green); }
.send-btn {
  border: none;
  background: var(--turtle-green);
  color: #fff;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  cursor: pointer;
}
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ---------- 面板開關動畫 ---------- */
.pop-enter-active, .pop-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.pop-enter-from, .pop-leave-to { opacity: 0; transform: translateY(12px) scale(0.96); }
</style>
