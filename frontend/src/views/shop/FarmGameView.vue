<script setup>
/*
  農產切切樂（Fruit Ninja 風格）
  ------------------------------------------------
  跟原本的種田遊戲不一樣：種田遊戲畫面是「HTML 元素 + Vue 綁定」，
  這種每秒要重畫 60 次的動作遊戲，改用 <canvas> 自己畫圖，
  Vue 只負責「分數、生命、開始/結束畫面」這些 UI 狀態。

  核心概念：
  1. 遊戲迴圈：requestAnimationFrame 每一幀呼叫一次 → 更新物理 → 重畫全部
  2. 物理：農產品被往上拋（初速度向上），每一幀被重力往下拉
  3. 切割判定：滑鼠這一幀到上一幀的「線段」，有沒有掃過農產品的圓形範圍
*/
import { ref, onMounted, onBeforeUnmount } from 'vue'

/* ==============================
   遊戲設定（純資料）
   ============================== */

// 可以切的農產品：emoji、半徑（碰撞範圍）、果汁顏色、得分
const PRODUCES = [
  { emoji: '🍉', name: '西瓜', radius: 42, juice: '#e0505a', score: 5 },
  { emoji: '🍅', name: '番茄', radius: 32, juice: '#e8543f', score: 10 },
  { emoji: '🌽', name: '玉米', radius: 34, juice: '#f2c14e', score: 10 },
  { emoji: '🥕', name: '蘿蔔', radius: 30, juice: '#ef8a3c', score: 15 },
  { emoji: '🍆', name: '茄子', radius: 32, juice: '#8e5aa8', score: 15 },
  { emoji: '🎃', name: '南瓜', radius: 44, juice: '#e78a2e', score: 20 },
]

// 炸彈：切到直接結束遊戲（radius 給小一點，比較不容易誤切）
const BOMB = { emoji: '💣', radius: 30 }

const GRAVITY = 1600        // 重力加速度（px/秒²），越大掉越快
const START_LIVES = 3       // 沒切到、掉出畫面就扣一條命
const BOMB_CHANCE = 0.12    // 每顆生成物是炸彈的機率
const COMBO_WINDOW = 0.4    // 幾秒內連切 2 顆以上算連擊
const BEST_KEY = 'farmily-slice-best'  // localStorage 的最高分 key

/* ==============================
   Vue 狀態（畫面上的 UI 用）
   ============================== */

const score = ref(0)
const lives = ref(START_LIVES)
const best = ref(Number(localStorage.getItem(BEST_KEY)) || 0)
// 遊戲階段：ready = 開始畫面、playing = 遊戲中、over = 結束畫面
const phase = ref('ready')
const overReason = ref('')  // 結束原因（切到炸彈 / 命用完）

/* ==============================
   Canvas 遊戲內部狀態
   ------------------------------
   這些「不用」ref！因為每秒變動 60 次，
   用普通變數就好，ref 的更新追蹤反而是多餘的負擔。
   ============================== */

const canvasRef = ref(null)  // 綁 <canvas> 這個 DOM 元素本身，才需要 ref
let ctx = null               // canvas 的 2D 畫筆
let W = 0, H = 0             // 畫布邏輯寬高（CSS px）
let rafId = 0                // requestAnimationFrame 的編號，離開頁面時要取消
let lastTime = 0             // 上一幀的時間，用來算 dt（兩幀間隔幾秒）

let fruits = []     // 場上飛的完整農產品 { x, y, vx, vy, rot, vr, type, isBomb }
let pieces = []     // 被切成的兩半 { ...同上, side: 'left'|'right', life }
let particles = []  // 果汁噴濺的小圓點
let floats = []     // 漂浮的加分文字（+10、3 連擊！）
let trail = []      // 刀光軌跡的點 { x, y, t }

let elapsed = 0        // 這局玩了幾秒（用來越玩越快）
let spawnTimer = 0     // 距離下一波生成還剩幾秒
let comboCount = 0     // 連擊窗口內切到幾顆
let comboTimer = 0     // 連擊窗口倒數
let pointerDown = false
let lastPointer = null // 上一次滑鼠位置，跟這一次連成「切割線段」

/* ==============================
   遊戲流程控制
   ============================== */

function startGame() {
  score.value = 0
  lives.value = START_LIVES
  phase.value = 'playing'
  fruits = []; pieces = []; particles = []; floats = []; trail = []
  elapsed = 0
  spawnTimer = 0.8   // 開場 0.8 秒後丟第一波
  comboCount = 0
  lastTime = performance.now()
}

function endGame(reason) {
  phase.value = 'over'
  overReason.value = reason
  // 破紀錄就存進 localStorage，下次進來還在
  if (score.value > best.value) {
    best.value = score.value
    localStorage.setItem(BEST_KEY, String(best.value))
  }
}

/* ==============================
   生成農產品
   ============================== */

// 從畫面底下往上拋一顆。位置、角度、初速度都帶點隨機才自然
function spawnOne() {
  const isBomb = Math.random() < BOMB_CHANCE
  const type = isBomb ? BOMB : PRODUCES[Math.floor(Math.random() * PRODUCES.length)]
  const x = W * (0.15 + Math.random() * 0.7)          // 從畫面中間 70% 範圍出發
  // 往上的初速度：用物理公式 v = √(2gh) 回推，讓它大約飛到畫面 60%~90% 高
  const peak = H * (0.6 + Math.random() * 0.3)
  const vy = -Math.sqrt(2 * GRAVITY * peak)
  // 水平速度：往畫面中央偏，比較不會直接飛出左右邊界
  const vx = (W / 2 - x) * (0.2 + Math.random() * 0.5)

  fruits.push({
    x, y: H + type.radius,   // 從畫布底部下面一點點出現
    vx, vy,
    rot: Math.random() * Math.PI * 2,           // 初始旋轉角
    vr: (Math.random() - 0.5) * 4,              // 旋轉速度（弧度/秒）
    type, isBomb,
  })
}

// 一次丟一波（1~3 顆），而且越玩越快、越玩越多
function spawnWave() {
  const difficulty = Math.min(1, elapsed / 60)  // 0 → 1，60 秒後達到最難
  const count = 1 + Math.floor(Math.random() * (2 + difficulty * 2))
  for (let i = 0; i < count; i++) {
    // 同一波的每顆錯開一點時間丟，不要疊在一起
    setTimeout(spawnOne, i * 150)
  }
  spawnTimer = 1.6 - difficulty * 0.8  // 生成間隔從 1.6 秒縮到 0.8 秒
}

/* ==============================
   切割判定與效果
   ============================== */

// 數學小工具：點 p 到「線段 a→b」的最短距離
// 用來判斷刀刃線段有沒有掃到農產品（距離 < 半徑就是切到）
function segmentDist(ax, ay, bx, by, px, py) {
  const dx = bx - ax, dy = by - ay
  const lenSq = dx * dx + dy * dy
  // t = 投影比例，夾在 0~1 之間代表最近點落在線段上
  const t = lenSq === 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / lenSq))
  const cx = ax + t * dx, cy = ay + t * dy
  return Math.hypot(px - cx, py - cy)
}

// 滑鼠從 (ax,ay) 滑到 (bx,by)，檢查有沒有切到東西
function checkSlice(ax, ay, bx, by) {
  const angle = Math.atan2(by - ay, bx - ax)  // 這一刀的方向（切面角度用）

  for (let i = fruits.length - 1; i >= 0; i--) {
    const f = fruits[i]
    if (segmentDist(ax, ay, bx, by, f.x, f.y) > f.type.radius) continue

    fruits.splice(i, 1)  // 從場上移除這顆

    if (f.isBomb) {
      // 切到炸彈：全畫面閃白 + 遊戲結束
      spawnParticles(f.x, f.y, '#333', 24)
      endGame('💥 切到炸彈了！')
      return
    }

    // --- 切到農產品 ---
    score.value += f.type.score
    floats.push({ x: f.x, y: f.y - 30, text: `+${f.type.score}`, t: 0 })
    spawnPieces(f, angle)                          // 裂成兩半
    spawnParticles(f.x, f.y, f.type.juice, 12)     // 噴果汁

    // 連擊：短時間內切到第 2 顆以上，額外加分
    comboCount++
    comboTimer = COMBO_WINDOW
    if (comboCount >= 2) {
      const bonus = comboCount * 5
      score.value += bonus
      floats.push({ x: f.x, y: f.y - 60, text: `${comboCount} 連擊 +${bonus}！`, t: 0, big: true })
    }
  }
}

// 產生左右兩個半塊：沿著切割方向的「垂直方向」彈開
function spawnPieces(f, angle) {
  const push = 160  // 兩半彈開的速度
  for (const side of ['left', 'right']) {
    const dir = side === 'left' ? -1 : 1
    pieces.push({
      x: f.x, y: f.y,
      // 原本的速度 + 垂直於刀向的推力，兩半往反方向飛
      vx: f.vx * 0.5 + Math.cos(angle + Math.PI / 2) * push * dir,
      vy: f.vy * 0.5 + Math.sin(angle + Math.PI / 2) * push * dir,
      rot: f.rot,
      vr: dir * (2 + Math.random() * 3),
      sliceAngle: angle,   // 記住切面角度，畫的時候用來裁切
      side,
      type: f.type,
      life: 1,             // 1 → 0 慢慢變透明
    })
  }
}

// 果汁粒子：往四面八方噴的小圓點
function spawnParticles(x, y, color, count) {
  for (let i = 0; i < count; i++) {
    const a = Math.random() * Math.PI * 2
    const speed = 120 + Math.random() * 260
    particles.push({
      x, y,
      vx: Math.cos(a) * speed,
      vy: Math.sin(a) * speed - 100,  // 稍微往上噴比較好看
      r: 3 + Math.random() * 5,
      color,
      life: 1,
    })
  }
}

/* ==============================
   每一幀的更新（物理）
   ============================== */

function update(dt) {
  elapsed += dt

  // --- 生成 ---
  spawnTimer -= dt
  if (spawnTimer <= 0) spawnWave()

  // --- 連擊窗口倒數 ---
  if (comboTimer > 0) {
    comboTimer -= dt
    if (comboTimer <= 0) comboCount = 0  // 時間到，連擊歸零
  }

  // --- 完整農產品：重力 + 移動，掉出底部沒切到就扣命 ---
  for (let i = fruits.length - 1; i >= 0; i--) {
    const f = fruits[i]
    f.vy += GRAVITY * dt
    f.x += f.vx * dt
    f.y += f.vy * dt
    f.rot += f.vr * dt

    // vy > 0 代表正在往下掉，而且已經掉出畫面底部
    if (f.vy > 0 && f.y > H + f.type.radius * 2) {
      fruits.splice(i, 1)
      if (!f.isBomb) {          // 炸彈沒接到是好事，不扣命
        lives.value--
        if (lives.value <= 0) {
          endGame('💔 太多農產品掉到地上了…')
          return
        }
      }
    }
  }

  // --- 半塊：一樣受重力，同時慢慢變透明後移除 ---
  for (let i = pieces.length - 1; i >= 0; i--) {
    const p = pieces[i]
    p.vy += GRAVITY * dt
    p.x += p.vx * dt
    p.y += p.vy * dt
    p.rot += p.vr * dt
    p.life -= dt * 0.9
    if (p.life <= 0 || p.y > H + 100) pieces.splice(i, 1)
  }

  // --- 果汁粒子 ---
  for (let i = particles.length - 1; i >= 0; i--) {
    const p = particles[i]
    p.vy += GRAVITY * 0.6 * dt
    p.x += p.vx * dt
    p.y += p.vy * dt
    p.life -= dt * 1.8
    if (p.life <= 0) particles.splice(i, 1)
  }

  // --- 加分文字：往上飄、變淡 ---
  for (let i = floats.length - 1; i >= 0; i--) {
    const f = floats[i]
    f.y -= 50 * dt
    f.t += dt
    if (f.t > 1) floats.splice(i, 1)
  }

  // --- 刀光：只保留最近 0.15 秒的軌跡點 ---
  const now = performance.now()
  trail = trail.filter(p => now - p.t < 150)
}

/* ==============================
   每一幀的繪製
   ============================== */

// 用 emoji 當圖：把畫筆移到中心、旋轉，再把字畫在正中央
function drawEmoji(item) {
  ctx.save()
  ctx.translate(item.x, item.y)
  ctx.rotate(item.rot)
  ctx.font = `${item.type.radius * 2}px serif`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(item.type.emoji, 0, 0)
  ctx.restore()
}

// 半塊：跟上面一樣，但先用 clip() 把畫布「裁掉一半」再畫 emoji，
// 只露出切面其中一側 → 看起來就像被切開了
function drawPiece(p) {
  const r = p.type.radius * 2
  ctx.save()
  ctx.globalAlpha = Math.max(0, p.life)
  ctx.translate(p.x, p.y)
  ctx.rotate(p.sliceAngle)      // 先轉到切割方向
  ctx.beginPath()
  if (p.side === 'left') ctx.rect(-r, -r, r * 2, r)   // 上半
  else ctx.rect(-r, 0, r * 2, r)                       // 下半
  ctx.clip()
  ctx.rotate(p.rot - p.sliceAngle)  // 再轉回 emoji 自己的旋轉角
  ctx.font = `${r}px serif`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(p.type.emoji, 0, 0)
  ctx.restore()
}

function draw() {
  // 每一幀都整張擦掉重畫（canvas 的標準做法）
  ctx.clearRect(0, 0, W, H)

  // 果汁粒子（畫在最下層）
  for (const p of particles) {
    ctx.globalAlpha = Math.max(0, p.life)
    ctx.fillStyle = p.color
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    ctx.fill()
  }
  ctx.globalAlpha = 1

  for (const p of pieces) drawPiece(p)
  for (const f of fruits) drawEmoji(f)

  // 加分文字
  for (const f of floats) {
    ctx.globalAlpha = Math.max(0, 1 - f.t)
    ctx.font = f.big ? 'bold 28px sans-serif' : 'bold 20px sans-serif'
    ctx.fillStyle = f.big ? '#ffd75e' : '#ffffff'
    ctx.strokeStyle = 'rgba(0,0,0,0.5)'
    ctx.lineWidth = 3
    ctx.textAlign = 'center'
    ctx.strokeText(f.text, f.x, f.y)
    ctx.fillText(f.text, f.x, f.y)
  }
  ctx.globalAlpha = 1

  // 刀光：把軌跡點連成線，越舊的越透明、越細
  if (trail.length >= 2) {
    const now = performance.now()
    for (let i = 1; i < trail.length; i++) {
      const age = (now - trail[i].t) / 150     // 0 = 剛畫，1 = 快消失
      ctx.strokeStyle = `rgba(255, 255, 255, ${1 - age})`
      ctx.lineWidth = 6 * (1 - age) + 1
      ctx.lineCap = 'round'
      ctx.beginPath()
      ctx.moveTo(trail[i - 1].x, trail[i - 1].y)
      ctx.lineTo(trail[i].x, trail[i].y)
      ctx.stroke()
    }
  }
}

// 遊戲主迴圈：瀏覽器每一幀（約 1/60 秒）呼叫一次
function loop(now) {
  // dt = 距離上一幀過了幾秒。切到別的分頁再回來 dt 會爆大，
  // 所以最多當作 1/30 秒，避免農產品「瞬移」穿出畫面
  const dt = Math.min((now - lastTime) / 1000, 1 / 30)
  lastTime = now

  if (phase.value === 'playing') update(dt)
  draw()

  rafId = requestAnimationFrame(loop)  // 預約下一幀，形成無限迴圈
}

/* ==============================
   滑鼠 / 觸控輸入
   ------------------------------
   用 pointer 事件一次搞定滑鼠和手機觸控
   ============================== */

// 把「視窗座標」換算成「畫布座標」
function toCanvasXY(e) {
  const rect = canvasRef.value.getBoundingClientRect()
  return { x: e.clientX - rect.left, y: e.clientY - rect.top }
}

function onPointerDown(e) {
  pointerDown = true
  lastPointer = toCanvasXY(e)
}

function onPointerMove(e) {
  if (!pointerDown || phase.value !== 'playing') return
  const p = toCanvasXY(e)
  trail.push({ x: p.x, y: p.y, t: performance.now() })
  if (lastPointer) {
    // 上一點 → 這一點連成刀刃線段，拿去做切割判定
    checkSlice(lastPointer.x, lastPointer.y, p.x, p.y)
  }
  lastPointer = p
}

function onPointerUp() {
  pointerDown = false
  lastPointer = null
}

/* ==============================
   生命週期：進頁面啟動、離開時清乾淨
   ============================== */

// 讓 canvas 的「實際像素」跟上螢幕大小和解析度（Retina 螢幕才不會糊）
function resizeCanvas() {
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const dpr = window.devicePixelRatio || 1
  W = rect.width
  H = rect.height
  canvas.width = W * dpr
  canvas.height = H * dpr
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)  // 之後都用 CSS px 座標來畫
}

onMounted(() => {
  ctx = canvasRef.value.getContext('2d')
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
  lastTime = performance.now()
  rafId = requestAnimationFrame(loop)
})

// 離開頁面一定要取消動畫迴圈和事件監聽，不然它會在背景一直跑
onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<template>
  <main class="slice-game">
    <div class="stage">
      <!-- ========== 上方資訊列 ========== -->
      <div class="hud">
        <div class="hud-item">🏆 分數 <strong>{{ score }}</strong></div>
        <div class="hud-item">⭐ 最高 <strong>{{ best }}</strong></div>
        <div class="hud-item lives">
          <!-- 生命用愛心排出來：還活著是 ❤️，扣掉的變 🤍 -->
          <span v-for="n in 3" :key="n">{{ n <= lives ? '❤️' : '🤍' }}</span>
        </div>
      </div>

      <!-- ========== 遊戲畫布 ========== -->
      <!--
        touch-action: none 寫在 CSS 裡：告訴手機瀏覽器
        「這裡的滑動是玩遊戲，不要拿去捲動頁面」
      -->
      <canvas
        ref="canvasRef"
        class="canvas"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointerleave="onPointerUp"
      ></canvas>

      <!-- ========== 開始畫面（蓋在畫布上的半透明層） ========== -->
      <div v-if="phase === 'ready'" class="overlay">
        <h1>🔪 農產切切樂</h1>
        <p>滑動切開飛起來的農產品！</p>
        <ul class="rules">
          <li>🍉 滑過農產品就能切開得分</li>
          <li>⚡ 一口氣連切多顆有連擊加分</li>
          <li>💣 千萬別切到炸彈！</li>
          <li>💔 農產品落地會扣一條命，三條命用完就結束</li>
        </ul>
        <button class="btn-start" @click="startGame">開始遊戲</button>
      </div>

      <!-- ========== 結束畫面 ========== -->
      <div v-else-if="phase === 'over'" class="overlay">
        <h1>遊戲結束</h1>
        <p class="reason">{{ overReason }}</p>
        <p class="final">
          得分 <strong>{{ score }}</strong>
          <span v-if="score >= best && score > 0" class="new-best">🎉 新紀錄！</span>
        </p>
        <button class="btn-start" @click="startGame">再玩一次</button>
      </div>
    </div>
  </main>
</template>

<style scoped>
.slice-game {
  min-height: 100vh;
  padding: 24px 16px 48px;
  /* 深色夜空漸層當背景，刀光和果汁比較顯眼 */
  background: linear-gradient(180deg, #1d3557 0%, #2f5233 100%);
  display: flex;
  justify-content: center;
}

.stage {
  position: relative;  /* 讓 overlay 可以用 absolute 蓋滿整個舞台 */
  width: min(1080px, 100%);
}

/* ========== 資訊列 ========== */
.hud {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  color: #fff;
}

.hud-item {
  background: rgba(0, 0, 0, 0.35);
  border-radius: 12px;
  padding: 8px 14px;
  font-size: 0.95rem;
}

.hud-item strong { font-size: 1.2rem; margin-left: 4px; }
.lives { margin-left: auto; letter-spacing: 2px; }

/* ========== 畫布 ========== */
.canvas {
  display: block;
  width: 100%;
  height: min(70vh, 640px);
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.25);
  cursor: crosshair;
  touch-action: none;  /* 手機上滑動不要捲頁面，交給遊戲處理 */
}

/* ========== 開始 / 結束畫面 ========== */
.overlay {
  position: absolute;
  inset: 44px 0 0;               /* 從 HUD 下方開始蓋，蓋住整個畫布 */
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(3px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #fff;
  text-align: center;
  padding: 24px;
}

.overlay h1 { margin: 0; font-size: 2.2rem; }
.overlay p { margin: 0; opacity: 0.9; }

.rules {
  list-style: none;
  margin: 8px 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.95rem;
  opacity: 0.85;
}

.reason { font-size: 1.1rem; }
.final { font-size: 1.3rem; }
.final strong { color: #ffd75e; font-size: 1.8rem; margin: 0 6px; }
.new-best { color: #ffd75e; font-weight: 700; }

.btn-start {
  margin-top: 8px;
  padding: 14px 40px;
  border: none;
  border-radius: 14px;
  background: #e0505a;
  color: #fff;
  font-size: 1.15rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.12s ease, background 0.12s ease;
}

.btn-start:hover { background: #c93d47; transform: translateY(-2px); }
</style>
