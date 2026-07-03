<script setup>
import { ref, computed } from 'vue'

// 遊戲用的圖片素材（SVG），import 進來 Vite 會自動處理路徑
import fieldBg from '@/assets/game/field-bg.svg'
import farmerImg from '@/assets/game/farmer.svg'
import iconSunny from '@/assets/game/weather-sunny.svg'
import iconCloudy from '@/assets/game/weather-cloudy.svg'
import iconRainy from '@/assets/game/weather-rainy.svg'
import iconStorm from '@/assets/game/weather-storm.svg'

/* ==============================
   遊戲設定（純資料，不會變動）
   ============================== */

// 三種作物：成本、需要的成長點數、收成賣價
// grow 越大越難養，但賣價也越高 → 玩家要自己權衡
const CROPS = {
  carrot: { name: '蘿蔔', emoji: '🥕', cost: 10, grow: 6,  sell: 30 },
  tomato: { name: '番茄', emoji: '🍅', cost: 25, grow: 12, sell: 80 },
  corn:   { name: '玉米', emoji: '🌽', cost: 50, grow: 20, sell: 180 },
}

// 四種天氣：每天結束時隨機抽一種，影響隔天的成長速度
// weight 是抽中的機率權重（總和 100），boost 是每天增加的成長點數
const WEATHERS = [
  { key: 'sunny',  name: '晴天',   icon: iconSunny,  weight: 40, boost: 2, desc: '陽光普照，作物 +2 成長' },
  { key: 'cloudy', name: '多雲',   icon: iconCloudy, weight: 25, boost: 1, desc: '雲層遮日，作物 +1 成長' },
  { key: 'rainy',  name: '雨天',   icon: iconRainy,  weight: 25, boost: 3, desc: '雨水滋潤，作物 +3 成長' },
  { key: 'storm',  name: '暴風雨', icon: iconStorm,  weight: 10, boost: 0, desc: '狂風暴雨！每株作物有 30% 被摧毀' },
]

// 三種難度：dailyCost 每天固定扣的維護費、stormWeight 暴風雨機率權重、
// stormDestroy 暴風雨摧毀單株的機率、pestChance 每天發生蟲害的機率、
// rotDays 成熟後幾天沒收成就腐爛（99 = 幾乎不會爛）
const DIFFICULTIES = {
  easy:   { name: '輕鬆', desc: '無維護費、無蟲害，慢慢玩沒壓力', dailyCost: 0,  stormWeight: 10, stormDestroy: 0.3,  pestChance: 0,    rotDays: 99 },
  normal: { name: '普通', desc: '每天維護費 $5、偶有蟲害，成熟 3 天內要收成', dailyCost: 5,  stormWeight: 15, stormDestroy: 0.35, pestChance: 0.2,  rotDays: 3 },
  hard:   { name: '困難', desc: '維護費 $12、蟲害頻繁、暴風雨更兇，成熟 2 天內要收成', dailyCost: 12, stormWeight: 22, stormDestroy: 0.45, pestChance: 0.35, rotDays: 2 },
}

const GRID_SIZE = 16     // 4x4 農地
const START_MONEY = 100  // 初始資金

/* ==============================
   遊戲狀態（ref = 會變動、畫面要跟著更新的資料）
   ============================== */

const money = ref(START_MONEY)
const day = ref(1)
const weather = ref(WEATHERS[0])          // 今天的天氣，開局固定晴天
const selectedCrop = ref('carrot')        // 目前選中的種子
const difficulty = ref('normal')          // 目前難度，預設普通
const log = ref(['🌞 第 1 天：歡迎來到 Farmily 小農場！點農地格子開始種植吧。'])

// 農地：每格是一個物件
// { crop: 作物 key 或 null, progress: 累積成長點數, ripeDays: 成熟後過了幾天（算腐爛用） }
const plots = ref(
  Array.from({ length: GRID_SIZE }, () => ({ crop: null, progress: 0, ripeDays: 0 }))
)

/* ==============================
   computed：由現有狀態「算出來」的值
   ============================== */

// 目前難度的設定值（difficulty 變了這裡會自動跟著變）
const diff = computed(() => DIFFICULTIES[difficulty.value])

// 有幾格已成熟可收成（顯示在資訊列，提醒玩家）
const readyCount = computed(
  () => plots.value.filter(p => p.crop && p.progress >= CROPS[p.crop].grow).length
)

// 破產判定：沒錢買最便宜的種子、田裡也沒任何作物 → 遊戲卡死，提示重來
const isBankrupt = computed(
  () => money.value < CROPS.carrot.cost && plots.value.every(p => !p.crop)
)

/* ==============================
   遊戲邏輯
   ============================== */

// 寫一行紀錄到遊戲日誌（新訊息放最上面，最多留 8 筆）
function addLog(msg) {
  log.value.unshift(`第 ${day.value} 天：${msg}`)
  if (log.value.length > 8) log.value.pop()
}

// 依權重隨機抽天氣：先抽 0~100 的亂數，再逐項扣掉權重，扣到負的那項就是結果
// 難度越高暴風雨權重越大，多出來的權重從晴天扣，總和維持 100
function rollWeather() {
  const extra = diff.value.stormWeight - 10   // 比基礎(10)多出來的暴風雨權重
  let roll = Math.random() * 100
  for (const w of WEATHERS) {
    let weight = w.weight
    if (w.key === 'storm') weight += extra
    if (w.key === 'sunny') weight -= extra
    roll -= weight
    if (roll < 0) return w
  }
  return WEATHERS[0]
}

// 點擊農地格子：空地 → 種植；成熟 → 收成；成長中 → 什麼都不做
function clickPlot(plot) {
  if (isBankrupt.value) return   // 遊戲結束後鎖住農地
  if (!plot.crop) {
    // --- 種植 ---
    const crop = CROPS[selectedCrop.value]
    if (money.value < crop.cost) {
      addLog(`💸 錢不夠！${crop.name}種子要 $${crop.cost}。`)
      return
    }
    money.value -= crop.cost
    plot.crop = selectedCrop.value
    plot.progress = 0
    plot.ripeDays = 0
    addLog(`🌱 花 $${crop.cost} 種下${crop.name}。`)
  } else if (plot.progress >= CROPS[plot.crop].grow) {
    // --- 收成 ---
    const crop = CROPS[plot.crop]
    money.value += crop.sell
    addLog(`${crop.emoji} 收成${crop.name}，賣得 $${crop.sell}！`)
    plot.crop = null
    plot.progress = 0
    plot.ripeDays = 0
  }
  // 成長中點了沒反應，讓玩家等天數
}

// 把一格清回空地（被摧毀 / 腐爛時用）
function clearPlot(plot) {
  plot.crop = null
  plot.progress = 0
  plot.ripeDays = 0
}

// 「下一天」：抽新天氣 → 扣維護費 → 依天氣讓作物成長
// 額外風險：暴風雨摧毀、成熟太久腐爛、隨機蟲害
function nextDay() {
  if (isBankrupt.value) return   // 遊戲結束後不能再過天
  day.value += 1
  weather.value = rollWeather()
  addLog(`${weather.value.name}｜${weather.value.desc}`)

  // 每日維護費（輕鬆模式是 0）：錢最低扣到 0，不會變負數
  if (diff.value.dailyCost > 0) {
    money.value = Math.max(0, money.value - diff.value.dailyCost)
    addLog(`🧾 支付農場維護費 $${diff.value.dailyCost}。`)
  }

  let destroyed = 0
  let rotted = 0
  for (const plot of plots.value) {
    if (!plot.crop) continue
    if (weather.value.key === 'storm' && Math.random() < diff.value.stormDestroy) {
      // 暴風雨：這株作物被摧毀，整格清空
      clearPlot(plot)
      destroyed++
    } else if (plot.progress >= CROPS[plot.crop].grow) {
      // 已成熟：不再成長，開始累積過熟天數，放太久就腐爛
      plot.ripeDays += 1
      if (plot.ripeDays >= diff.value.rotDays) {
        clearPlot(plot)
        rotted++
      }
    } else {
      plot.progress += weather.value.boost
    }
  }
  if (destroyed > 0) addLog(`💥 暴風雨摧毀了 ${destroyed} 株作物…`)
  if (rotted > 0) addLog(`🥀 ${rotted} 株作物放太久腐爛了，血本無歸…`)

  // 蟲害：每天有機率隨機挑一株成長中的作物，成長倒退 4 點
  if (Math.random() < diff.value.pestChance) {
    const growing = plots.value.filter(p => p.crop && p.progress < CROPS[p.crop].grow)
    if (growing.length > 0) {
      const victim = growing[Math.floor(Math.random() * growing.length)]
      victim.progress = Math.max(0, victim.progress - 4)
      addLog(`🐛 蟲害來襲！${CROPS[victim.crop].name}的成長倒退 4 點…`)
    }
  }

  // 一天結束後檢查有沒有輸：買不起最便宜的種子、田裡又空無一物 → 遊戲結束
  if (isBankrupt.value) {
    addLog('💀 破產了！沒錢買種子、田裡也沒作物，農場經營失敗…')
  }
}

// 重新開始：把所有狀態設回初始值（難度維持目前選的）
function restart() {
  money.value = START_MONEY
  day.value = 1
  weather.value = WEATHERS[0]
  plots.value = Array.from({ length: GRID_SIZE }, () => ({ crop: null, progress: 0, ripeDays: 0 }))
  log.value = [`🌞 第 1 天：新的農場（${diff.value.name}模式），重新出發！`]
}

// 切換難度：因為規則整個變了，直接重開一局才公平
function setDifficulty(key) {
  if (difficulty.value === key) return
  difficulty.value = key
  restart()
}

/* ==============================
   顯示用的小工具函式
   ============================== */

// 決定某一格要顯示的表情符號：空地 / 種子 / 幼苗 / 成熟作物
function plotEmoji(plot) {
  if (!plot.crop) return ''
  const crop = CROPS[plot.crop]
  const ratio = plot.progress / crop.grow
  if (ratio >= 1) return crop.emoji     // 成熟
  if (ratio >= 0.5) return '🌿'         // 長到一半
  return '🌱'                            // 剛種下
}

// 某一格的成長百分比（給進度條用），最多 100
function plotPercent(plot) {
  if (!plot.crop) return 0
  return Math.min(100, Math.round((plot.progress / CROPS[plot.crop].grow) * 100))
}

// 是否已成熟（成熟的格子要發光提示可收成）
function isReady(plot) {
  return plot.crop && plot.progress >= CROPS[plot.crop].grow
}

// 是否快腐爛了（成熟後只剩最後一天可收 → 標籤變紅色警告）
function isRotting(plot) {
  return isReady(plot) && diff.value.rotDays < 99 && plot.ripeDays >= diff.value.rotDays - 1
}
</script>

<template>
  <main class="game" :style="{ backgroundImage: `url(${fieldBg})` }">
    <div class="game-inner">
      <!-- ========== 標題列 ========== -->
      <header class="game-head">
        <img class="farmer" :src="farmerImg" alt="小農夫" />
        <div>
          <h1>Farmily 小農場</h1>
          <p>點空地種植、成熟後儘快收成，小心暴風雨、蟲害和每天的維護費！</p>
        </div>
      </header>

      <!-- ========== 資訊列：金錢 / 天數 / 天氣 / 可收成 ========== -->
      <section class="stats">
        <div class="stat">
          <span class="stat-label">💰 金錢</span>
          <strong class="stat-value">${{ money }}</strong>
        </div>
        <div class="stat">
          <span class="stat-label">📅 天數</span>
          <strong class="stat-value">第 {{ day }} 天</strong>
        </div>
        <div class="stat weather-stat">
          <img class="weather-icon" :src="weather.icon" :alt="weather.name" />
          <div>
            <strong class="stat-value">{{ weather.name }}</strong>
            <span class="weather-desc">{{ weather.desc }}</span>
          </div>
        </div>
        <div class="stat">
          <span class="stat-label">✅ 可收成</span>
          <strong class="stat-value">{{ readyCount }} 格</strong>
        </div>
      </section>

      <div class="game-body">
        <!-- ========== 左：農地格子 ========== -->
        <section class="field">
          <button
            v-for="(plot, i) in plots"
            :key="i"
            class="plot"
            :class="{ ready: isReady(plot), growing: plot.crop && !isReady(plot) }"
            @click="clickPlot(plot)"
          >
            <span class="plot-emoji">{{ plotEmoji(plot) }}</span>
            <!-- 有作物才顯示進度條 -->
            <span v-if="plot.crop" class="bar">
              <span class="bar-fill" :style="{ width: plotPercent(plot) + '%' }"></span>
            </span>
            <span v-if="isReady(plot)" class="ready-tag" :class="{ rot: isRotting(plot) }">
              {{ isRotting(plot) ? '快爛了！' : '收成！' }}
            </span>
          </button>

          <!-- 遊戲結束畫面：破產時整個蓋在農地上，只能重新開始 -->
          <div v-if="isBankrupt" class="gameover">
            <div class="gameover-card">
              <span class="gameover-emoji">💀</span>
              <h2>破產了！</h2>
              <p>你的農場在「{{ diff.name }}」模式下撐了 {{ day }} 天。</p>
              <button class="btn-restart" @click="restart">🔄 重新開始</button>
            </div>
          </div>
        </section>

        <!-- ========== 右：操作面板 ========== -->
        <aside class="panel">
          <!-- 選難度：切換會直接重開一局 -->
          <h2>🎯 難度</h2>
          <div class="diff-list">
            <button
              v-for="(d, key) in DIFFICULTIES"
              :key="key"
              class="diff"
              :class="{ active: difficulty === key }"
              @click="setDifficulty(key)"
            >
              {{ d.name }}
            </button>
          </div>
          <p class="diff-desc">{{ diff.desc }}</p>

          <!-- 選種子 -->
          <h2>🛒 選擇種子</h2>
          <div class="seed-list">
            <button
              v-for="(crop, key) in CROPS"
              :key="key"
              class="seed"
              :class="{ active: selectedCrop === key, disabled: money < crop.cost }"
              @click="selectedCrop = key"
            >
              <span class="seed-emoji">{{ crop.emoji }}</span>
              <span class="seed-name">{{ crop.name }}</span>
              <span class="seed-info">成本 ${{ crop.cost }}｜賣 ${{ crop.sell }}｜需 {{ crop.grow }} 成長</span>
            </button>
          </div>

          <!-- 下一天（遊戲結束時鎖住，逼玩家按重新開始） -->
          <button class="btn-next" :disabled="isBankrupt" @click="nextDay">🌙 下一天</button>

          <!-- 遊戲日誌 -->
          <h2>📜 農場日誌</h2>
          <ul class="log">
            <li v-for="(msg, i) in log" :key="log.length - i">{{ msg }}</li>
          </ul>
        </aside>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* ========== 整頁背景：農場 SVG 圖鋪底 ========== */
.game {
  min-height: 100vh;
  background-size: cover;
  background-position: center bottom;
  padding: 24px 16px 48px;
}

.game-inner {
  max-width: 1080px;
  margin: 0 auto;
}

/* ========== 標題列 ========== */
.game-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.farmer {
  width: 84px;
  height: auto;
  filter: drop-shadow(0 4px 6px rgba(55, 45, 28, 0.25));
}

.game-head h1 {
  margin: 0;
  color: var(--ink);
  font-size: 1.8rem;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.7);
}

.game-head p {
  margin: 4px 0 0;
  color: var(--ink-soft);
}

/* ========== 資訊列 ========== */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat {
  background: var(--cream);
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 10px 14px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label { color: var(--muted); font-size: 0.85rem; }
.stat-value { color: var(--ink); font-size: 1.15rem; }

.weather-stat {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.weather-icon { width: 40px; height: 40px; }
.weather-desc { display: block; color: var(--muted); font-size: 0.78rem; }

/* ========== 主體：左農地、右面板 ========== */
.game-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
}

/* 窄螢幕改成上下排列 */
@media (max-width: 860px) {
  .game-body { grid-template-columns: 1fr; }
}

/* ========== 農地格子 ========== */
.field {
  position: relative;   /* 讓遊戲結束畫面可以絕對定位蓋在上面 */
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  background: rgba(139, 96, 54, 0.25);
  border: 2px solid rgba(139, 96, 54, 0.4);
  border-radius: 18px;
  padding: 14px;
  align-content: start;
}

/* ========== 遊戲結束畫面 ========== */
.gameover {
  position: absolute;
  inset: 0;
  background: rgba(40, 30, 18, 0.72);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5;
}

.gameover-card {
  background: var(--cream);
  border-radius: 18px;
  padding: 28px 36px;
  text-align: center;
  box-shadow: var(--shadow-hover);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.gameover-emoji { font-size: 3rem; line-height: 1; }
.gameover-card h2 { margin: 0; color: var(--ink); font-size: 1.5rem; }
.gameover-card p { margin: 0 0 6px; color: var(--ink-soft); }

.plot {
  position: relative;
  aspect-ratio: 1;           /* 永遠正方形 */
  border: 2px dashed #b08a5e;
  border-radius: 12px;
  background: linear-gradient(180deg, #a9744a, #8f5f3b);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.plot:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

/* 成長中的格子：土色深一點表示有東西 */
.plot.growing {
  border-style: solid;
  background: linear-gradient(180deg, #8f6238, #74502e);
}

/* 成熟的格子：發出金色光暈，提示可以點收成 */
.plot.ready {
  border: 2px solid #ffd75e;
  box-shadow: 0 0 14px rgba(255, 215, 94, 0.8);
  animation: glow 1.2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { box-shadow: 0 0 8px rgba(255, 215, 94, 0.5); }
  to   { box-shadow: 0 0 18px rgba(255, 215, 94, 0.95); }
}

.plot-emoji { font-size: 2rem; line-height: 1; }

/* 成長進度條 */
.bar {
  width: 70%;
  height: 7px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #9bd66b, #ffd75e);
  transition: width 0.3s ease;
}

.ready-tag {
  position: absolute;
  top: 6px;
  right: 6px;
  background: #ffd75e;
  color: #6b4a12;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 8px;
}

/* 快腐爛的警告標籤：換成紅色，催玩家趕快收 */
.ready-tag.rot {
  background: #d9534f;
  color: #fff;
}

/* ========== 右側面板 ========== */
.panel {
  background: var(--cream);
  border: 1px solid var(--line);
  border-radius: 18px;
  padding: 16px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-self: start;
}

.panel h2 {
  margin: 4px 0 2px;
  font-size: 1rem;
  color: var(--ink);
}

/* 難度按鈕：三顆並排 */
.diff-list { display: flex; gap: 8px; }

.diff {
  flex: 1;
  padding: 8px 0;
  border: 2px solid var(--line);
  border-radius: 12px;
  background: #fff;
  font-weight: 700;
  color: var(--ink);
  cursor: pointer;
  transition: border-color 0.12s ease, background 0.12s ease;
}

.diff.active {
  border-color: var(--leaf);
  background: var(--leaf-soft);
}

.diff-desc {
  margin: 0;
  font-size: 0.75rem;
  color: var(--muted);
}

/* 種子按鈕 */
.seed-list { display: flex; flex-direction: column; gap: 8px; }

.seed {
  display: grid;
  grid-template-columns: 36px 1fr;
  grid-template-areas: "emoji name" "emoji info";
  align-items: center;
  text-align: left;
  gap: 0 8px;
  padding: 8px 10px;
  border: 2px solid var(--line);
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.12s ease, background 0.12s ease;
}

.seed.active {
  border-color: var(--leaf);
  background: var(--leaf-soft);
}

.seed.disabled { opacity: 0.55; }

.seed-emoji { grid-area: emoji; font-size: 1.6rem; }
.seed-name  { grid-area: name; font-weight: 700; color: var(--ink); }
.seed-info  { grid-area: info; font-size: 0.75rem; color: var(--muted); }

/* 下一天按鈕：整個面板最重要的動作，做大做醒目 */
.btn-next {
  padding: 12px;
  border: none;
  border-radius: 12px;
  background: var(--leaf);
  color: #fff;
  font-size: 1.05rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.12s ease, transform 0.12s ease;
}

.btn-next:hover { background: var(--leaf-dark); transform: translateY(-1px); }

/* 遊戲結束時的下一天按鈕：變灰、不能按 */
.btn-next:disabled {
  background: #b6b0a4;
  cursor: not-allowed;
  transform: none;
}

.btn-restart {
  padding: 10px;
  border: none;
  border-radius: 12px;
  background: #c0563e;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

/* 遊戲日誌 */
.log {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.log li {
  font-size: 0.82rem;
  color: var(--ink-soft);
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 5px 8px;
}

/* 最新一筆日誌加深強調 */
.log li:first-child {
  color: var(--ink);
  font-weight: 600;
  border-left: 3px solid var(--leaf);
}
</style>
