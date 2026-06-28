# CKA101G3 小農電商 — Vue 前端筆記

> 給組員的 Vue 前端開發 / 串接指南。
> 後端是 Spring Boot（WAR 部署到 Tomcat），前端是 **Vite + Vue**，build 後的產物放在後端的 `static/farmily-web/`。

## 0. 一分鐘搞懂整體架構
```
瀏覽器
  ├── 前台畫面：/farmily-web/**      ← Vue SPA（這份筆記的主角）
  ├── 資料 API：/api/**             ← Spring Boot 回 JSON
  └── 後台畫面：/admin/**            ← Thymeleaf（管理員後台）
```

---

## 1. 環境介紹

> ⚠️ TODO：補上你們的實際資訊

- **Node.js**
- **Vue 原始碼專案位置**：`CKA101G3/frontend`
- 安裝依賴：
  ```bash
  npm install
  ```
- 啟動開發伺服器（熱更新，改 code 即時看到 localhost:5173）：
  ```bash
  npm run dev
  ```
- 打包（產生正式檔案）：
  ```bash
  npm run build
  ```

---

## 2. 專案結構

> ⚠️ TODO：依你們實際的 src 結構調整

```
src/
├── main.js            # 進入點：建立 Vue app、掛載到 #app、註冊 router
├── App.vue            # 根組件
├── router/            # Vue Router 路由設定（哪個網址對應哪個頁面）
├── views/             # 頁面層級的組件（商品列表頁、詳情頁…）大家主要在這開發
├── components/        # 可重用的小組件（商品卡片、Header…）
├── api/               # 跟後端串接的 fetch 請求（封裝呼叫 API 的函式）
└── assets/            # 圖片、css
```

---

## ⭐ 先搞懂：一個 `.vue` 檔長怎樣（單檔組件 SFC）

Vue 的每一個頁面 / 組件 = 一個 **`.vue` 檔**，裡面由**三個區塊**組成，這叫 **單檔組件（Single File Component, SFC）**：

| 區塊 | 寫什麼 | 對應你熟的東西 |
|---|---|---|
| `<script>` | **JavaScript 邏輯** + 用 **fetch 串後端**、準備資料 | 像 Controller / JS 行為 |
| `<template>` | **HTML** + 動態綁定（`{{ }}`、`:`、`v-for`…） | 像 Thymeleaf 的 HTML 樣板 |
| `<style>` | **CSS**（加 `scoped` = 只作用在這個組件） | 樣式 |

一個空的 `.vue` 樣板長這樣：
```vue
<script setup>
  // 寫 JavaScript、用 fetch 串後端的地方
</script>

<template>
  <!-- 寫動態 HTML 的地方 -->
</template>

<style scoped>
  /* 寫 CSS 的地方 */
</style>
```
**一個完整的 `.vue` 範例（商品列表，三段都有）：**

> 💡 重要：看得懂下面這段程式碼，就代表你會 Vue 了，可以開始做專題！

```vue
<script setup>
// ① <script>：寫 JavaScript，並用 fetch 串後端拿資料
import { ref, onMounted } from 'vue'

const products = ref([])      // 響應式資料：一變動，畫面自動跟著更新

async function loadProducts() {
  const res = await fetch('/api/products')
  if (!res.ok) throw new Error('載入失敗：' + res.status)
  products.value = await res.json()   // 塞進 ref → template 自動重繪
}

onMounted(loadProducts)         // 這個組件一出現在畫面（使用者一進到這頁）就去抓資料
</script>

<template>
  <!-- ② <template>：寫 HTML + 動態綁定 -->
  <div class="product-list">
    <div v-for="p in products" :key="p.productId" class="card">
      <h3>{{ p.productName }}</h3>
      <p>{{ p.retailPrice }} 元</p>
      <img :src="`/api/products/${p.productId}/image`" />
    </div>
  </div>
</template>

<style scoped>
/* ③ <style>：寫 CSS。scoped = 樣式只作用在這個組件，不會污染別頁 */
.card { border: 1px solid #ddd; padding: 12px; }
</style>
```

> 🔄 **資料流向**：`<script>` 用 fetch 拿到資料 → 存進 `ref` → `<template>` 自動顯示 → `<style>` 負責美化。
> 📌 `<script setup>` 是 Vue 3 現在的標準寫法（Composition API）；三個區塊順序不強制，但習慣 `script → template → style`。

---

## ⭐ `<template>` 模板語法基礎（新手必看）

> Vue 的畫面寫在 `<template>` 裡，長得像 HTML，但多了一些「會動」的語法。
### ① `{{ }}` 顯示資料（文字插值）

把資料「印」到畫面上，用兩個大括號：

```html
<p>商品名稱：{{ product.productName }}</p>   <!-- 顯示：商品名稱：高麗菜 -->
<p>價格：{{ product.retailPrice }} 元</p>     <!-- 顯示：價格：50 元 -->
```

> 記法：`{{ }}` = 「把裡面的值秀出來」。

**🔄 跟 Thymeleaf 比：**

| | Vue | Thymeleaf |
|---|------|-----------|
| 寫法 | `<p>{{ product.productName }}</p>` | `<p th:text="${product.productName}">商品名</p>` |
| 值放哪 | 直接放在標籤**內容**裡 | 放在 `th:text` **屬性**裡 |
| 標籤裡的字 | 沒有預設文字 | 可留「商品名」當預設字（執行時會被取代） |

差異重點：
1. **位置不同**：Vue 的 `{{ }}` 寫在**標籤中間**；Thymeleaf 的 `th:text` 是**標籤的屬性**。
2. **資料哪來**：Vue 的 `product` 是前端用 fetch 拿到、存在 JavaScript 變數裡的 JSON；Thymeleaf 的 `${product}` 是後端 Controller 用 `Model` 塞進來的。
3. **何時填值**：Vue 在瀏覽器執行時填；Thymeleaf 在伺服器渲染時就填好了。

### ② `:` 綁定屬性（v-bind）

HTML 標籤的屬性（src、href、class…）想塞「變數」時，前面加冒號 `:`：

```html
<!-- ❌ 寫死的，永遠是同一張圖 -->
<img src="/api/products/1/image" />

<!-- ✅ 用變數，每個商品不同圖 -->
<img :src="`/api/products/${product.productId}/image`" />
```

### ③ `v-if` 條件顯示（要不要出現）⭐

依條件決定「這段畫面要不要顯示」：

```html
<!-- 有庫存才顯示「可購買」，沒庫存顯示「已售完」 -->
<p v-if="product.retailPrice > 0">可購買</p>
<p v-else>已售完</p>
```

實用例子：

```html
<!-- 資料還沒載入完，先顯示「載入中」 -->
<p v-if="loading">載入中...</p>

<!-- 沒有商品時顯示提示 -->
<p v-if="products.length === 0">目前沒有商品</p>
```

> `v-if` 為 `true` 才顯示，`false` 就「整段從畫面消失」（不是隱藏，是根本不存在）。
> 可搭配 `v-else-if` / `v-else`。

**🔄 跟 Thymeleaf 比（同一個例子：有庫存顯示可購買，否則已售完）：**

```html
<!-- Vue -->
<p v-if="product.retailPrice > 0">可購買</p>
<p v-else>已售完</p>
```
```html
<!-- Thymeleaf -->
<p th:if="${product.retailPrice > 0}">可購買</p>
<p th:unless="${product.retailPrice > 0}">已售完</p>
```

差異重點：
1. **else 不同**：Vue 有 `v-else`（不用重寫條件）；Thymeleaf 沒 else，要用 `th:unless` 把條件**再寫一次**。
2. **動態切換**：像「載入中 → 載入完」這種即時變化，Vue 在瀏覽器會自動更新；Thymeleaf 送出後就定型，要變得重新跟伺服器要整頁。

### ④ `v-for` 迴圈渲染（列出一堆）⭐⭐

把「一個陣列」變成「一排畫面」——商品列表必用：

```html
<!-- products 是商品陣列，跑一圈，每個商品產生一個 <div> -->
<div v-for="product in products" :key="product.productId">
  <h3>{{ product.productName }}</h3>
  <p>{{ product.retailPrice }} 元 / {{ product.unitPricingMeasure }}</p>
  <img :src="`/api/products/${product.productId}/image`" />
</div>
```

畫面結果（假設有 3 個商品）：
```
高麗菜  50 元 / 顆   [圖]
香蕉    30 元 / 串   [圖]
芒果    80 元 / 顆   [圖]
```

> ⚠️ **`:key` 一定要加**！給每個項目一個獨一無二的值（用 `productId` 最好）。
> Vue 靠 key 分辨「誰是誰」，沒加 key 更新畫面時容易出錯、效能也差。

### ⑤ `@` 綁定事件（v-on）

使用者點擊、輸入時要做事，用 `@`：

```html
<button @click="addToCart(product)">加入購物車</button>
<button @click="goDetail(product.productId)">看詳情</button>
```

> `@click` 是 `v-on:click` 的簡寫。`@` 後面接事件名（click、input、submit…）。

### ⑥ `v-model` 表單雙向綁定（搜尋框、輸入框）

輸入框的值 ↔ 變數「自動同步」：

```html
<input v-model="keyword" placeholder="搜尋商品" />
<p>你輸入了：{{ keyword }}</p>   <!-- 邊打字邊即時顯示 -->
```



### 📋 語法速查表

| 語法 | 作用 | 例子 |
|------|------|------|
| `{{ }}` | 顯示資料 | `{{ product.productName }}` |
| `:屬性` | 綁定變數到屬性 | `:src="imgUrl"` |
| `v-if` / `v-else` | 條件顯示 | `v-if="loading"` |
| `v-for` | 迴圈列出 | `v-for="p in products" :key="p.productId"` |
| `@事件` | 綁定事件 | `@click="buy()"` |
| `v-model` | 表單雙向綁定 | `v-model="keyword"` |

> 💡 記憶口訣：
> - 想**秀東西** → `{{ }}`
> - 想讓**屬性帶變數** → 加 `:`
> - 想**有條件出現** → `v-if`
> - 想**列一排** → `v-for`（記得 `:key`）
> - 想**綁動作** → `@`

### 🔄 跟 Thymeleaf 對照（有 Thymeleaf 底子的看這個）

語法概念其實很像，差別在「**誰來組畫面**」：
- **Thymeleaf**：在**伺服器**把資料填進 HTML，組好才送瀏覽器。
- **Vue**：瀏覽器拿到 JSON 後，在**前端**自己組畫面。

| 作用 | Vue | Thymeleaf | 說明 |
|------|-----|-----------|------|
| 顯示資料 | `{{ product.productName }}` | `th:text="${product.productName}"` 或 `[[${...}]]` | 都是把值印出來 |
| 綁定屬性 | `:src="imgUrl"` | `th:src="@{...}"` | 把變數塞進屬性 |
| 條件顯示 | `v-if="loading"` | `th:if="${loading}"` | 概念幾乎一樣 |
| 反向條件 | `v-else` | `th:unless="${...}"` | Vue 有 else，Thymeleaf 用 unless |
| 迴圈列出 | `v-for="p in products"` | `th:each="p : ${products}"` | **最像的一組** |
| 綁事件 | `@click="buy()"` | （**沒有**） | Thymeleaf 在伺服器渲染，沒有前端事件 |
| 表單雙向綁定 | `v-model="keyword"` | （**沒有**） | 同上，Thymeleaf 靠表單 submit |

### 同一個商品列表，兩種寫法對照

**Vue：**
```html
<div v-for="product in products" :key="product.productId">
  <h3>{{ product.productName }}</h3>
  <p>{{ product.retailPrice }} 元</p>
  <img :src="`/api/products/${product.productId}/image`" />
</div>
```

**Thymeleaf：**
```html
<div th:each="product : ${products}">
  <h3 th:text="${product.productName}">商品名</h3>
  <p th:text="${product.retailPrice} + ' 元'">價格</p>
  <img th:src="@{/api/products/{id}/image(id=${product.productId})}" />
</div>
```

> 🔑 **三個關鍵差異**：
> 1. **資料哪來**：Vue 靠 fetch 打 API 拿 JSON；Thymeleaf 由後端 Controller 用 `Model` 塞進來。
> 2. **何時組畫面**：Vue 在瀏覽器、Thymeleaf 在伺服器。
> 3. **互動**：Vue 有 `@click`、`v-model` 做前端互動不刷頁；Thymeleaf 沒有，要靠表單送出 / 換頁。
>
> → 所以 Vue 適合「**互動多、常切頁**」的前台；Thymeleaf 適合「**CRUD、求快**」的後台。

---

## 3. 怎麼跑起來 & 怎麼部署到後端

### 開發時（熱更新）
```bash
npm run dev        # 開發伺服器（通常 http://localhost:5173）
```

### 開發時 proxy 設定（vite.config）
組長統一設定好，這裡了解概念即可。
> 開發時前端在 5173、後端在 8080，靠 proxy 把 `/api` 轉給後端，避免跨域問題。
```js
export default defineConfig({
  base: '/farmily-web/',
  server: {
    proxy: {
      '/api': 'http://localhost:8080'   // 開發時把 /api 轉給後端
    }
  }
})
```

### 部署到後端
最後專題完成打包進spring boot方法
這個專案的部署方式是「**build 後把產物複製到後端的 static 資料夾**」：

1. `npm run build` → 產生 `dist/`（或設定好的輸出資料夾）
2. 把產物複製到後端：
   ```
   後端路徑：src/main/resources/static/farmily-web/
   ├── index.html          # 空殼頁（<div id="app"></div>）
   ├── favicon.ico
   └── static/             # 打包後的 JS / CSS（檔名帶 hash）
       ├── index-xxxx.js
       └── index-xxxx.css
   ```
3. 後端重新打包成 WAR → 丟 Tomcat
4. 瀏覽器開 `http://localhost:8080/farmily-web/`

> 📌 **base 路徑是 `/farmily-web/`**（Vite 設定 `base: '/farmily-web/'`，assets 輸出到 `static/`）。
> 改 base 路徑的話，後端的 `WebConfig` 也要一起改。

---

## 4. 跟後端串接 API（最重要 ⭐）

### 4.1 API 一覽（目前已有）

| 功能 | 方法 | 網址 | 回傳 |
|------|------|------|------|
| 查所有商品 | GET | `/api/products` | JSON 陣列（精簡 DTO：id、名稱、價格、單位） |
| 查單一商品詳情 | GET | `/api/products/{id}` | 詳情 DTO（含分類名稱，不含圖片）；查無回 404 |
| 新增商品 | POST | `/api/products` | 201（multipart 表單，含圖片） |
| 修改商品價格 | PATCH | `/api/products/{id}/price` | 200（JSON：retailPrice / groupPrice）；查無回 404 |
| 讀商品圖片 | GET | `/api/products/{id}/image` | 圖片本身（二進位）；無圖回 404 |


### 4.2 fetch 基礎（串接前必看 ⭐）

我們用瀏覽器**內建**的 `fetch` 打 API（不用裝套件）。它回傳 Promise，所以搭配 `await`。

#### ① 最重要：fetch 接資料是「兩段式」，要兩次 await

```js
const res  = await fetch('/api/products')   // 第①段：拿到「回應物件」（還沒拿到資料！）
const data = await res.json()               // 第②段：把 body 解析成 JSON（也是 Promise，要再 await）
```
- 第①段完成 = 伺服器回應了、header 到了，但 `res` **不是資料**，是 `Response` 物件。
- 第②段 `res.json()` 才真正把 body 讀出來、轉成 JS 物件。
- ⚠️ body **只能讀一次**：`res.json()` 讀過後再 `res.text()` 會報錯。

#### ② `res` 身上常用的東西

| 屬性 / 方法 | 作用 |
|---|---|
| `res.ok` | 布林：狀態碼 200~299 為 `true`，其他（404/500）為 `false` |
| `res.status` | 數字狀態碼（200、404、500…） |
| `res.json()` | 把 body 當 **JSON** 解析（最常用） |
| `res.text()` | 把 body 當**純文字** |
| `res.blob()` | 把 body 當**二進位**（圖片、檔案） |

#### ③ 最大的雷：fetch 不把 404/500 當錯誤 ⭐⭐

fetch **只有「網路層失敗」（斷網 / CORS）才會進 catch**；後端回 404、500 它仍視為「成功收到回應」。
所以每次都要**自己檢查 `res.ok`**（下面 4.3~4.6 範例都會這樣做）：

```js
const res = await fetch('/api/products/999')   // 後端回 404
if (!res.ok) throw new Error(`請求失敗：${res.status}`)   // 自己把 4xx/5xx 變成錯誤
const data = await res.json()
```
> （axios 會自動把 4xx/5xx 丟進 catch；fetch 要你手動。）

#### ④ 送資料：第二參數 options

GET 不用第二參數；POST / PATCH / PUT / DELETE 要：

```js
await fetch('/api/products/5/price', {
  method: 'PATCH',                                  // 不寫預設 GET
  headers: { 'Content-Type': 'application/json' },  // 送 JSON 一定要設
  body: JSON.stringify({ retailPrice: 80 })         // body 只能是「字串」→ 物件要 stringify
})
```
- 傳**檔案**時改用 `FormData` 當 body，且 **千萬不要自己設 `Content-Type`**（瀏覽器要自動帶 boundary）。



#### 📋 fetch 速查

| 重點 | 一句話 |
|---|---|
| 兩段式 | `fetch()` 拿 res、`res.json()` 拿資料，**兩次 await** |
| res.ok | **一定要檢查**，fetch 不把 404/500 當錯誤 |
| 送 JSON | `body: JSON.stringify(...)` + `Content-Type: application/json` |
| 傳檔案 | 用 `FormData`，**別設 Content-Type** |
| body | 只能讀一次（`json()` / `text()` / `blob()` 擇一） |

### 4.3 呼叫範例：查商品列表（GET）

```js
// 在組件 onMounted 時呼叫
const res = await fetch('/api/products')
if (!res.ok) throw new Error('載入商品失敗：' + res.status)   // ① 自己檢查狀態
const products = await res.json()                            // ② 自己解析 JSON
console.log(products)   // [{ productId, productName, retailPrice, unitPricingMeasure }, ...]
```

### 4.4 呼叫範例：查單一商品詳情（GET）

```js
const res = await fetch(`/api/products/${id}`)
if (res.status === 404) { /* 顯示「查無此商品」 */ return }
if (!res.ok) throw new Error('載入詳情失敗：' + res.status)
const product = await res.json()   // { productId, productName, retailPrice, groupPrice, ..., subCatClassName }
```

### 4.5 新增商品（POST，含圖片 → 用 FormData）

後端吃 `multipart/form-data`，所以用 `FormData`：

```js
const form = new FormData()
form.append('productName', '高麗菜')
form.append('retailPrice', 50)
form.append('productImage', fileInput.files[0])   // 圖片檔

const res = await fetch('/api/products', {
  method: 'POST',
  body: form
  // ⚠️ 千萬「不要」自己設 Content-Type！用 FormData 時瀏覽器會自動帶上含 boundary 的正確值
})
if (!res.ok) throw new Error('新增失敗：' + res.status)
```

### 4.6 修改商品價格（PATCH，送 JSON）

改價不需要圖片，改用 JSON：

```js
const res = await fetch(`/api/products/${id}/price`, {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },   // 送 JSON 要設這個
  body: JSON.stringify({ retailPrice: 80, groupPrice: 65 })   // 只帶要改的也行：{ groupPrice: 65 }
})
if (res.status === 400) { /* 價格不可為負數等驗證錯誤 */ }
if (res.status === 404) { /* 查無此商品 */ }
if (!res.ok) throw new Error('改價失敗：' + res.status)
```

> 📌 後端對價格有 `@Min(0)` 驗證，送負數會回 **400**；查無商品回 **404**。

---

## 5. Vue Router（前端路由）

> 🔧 路由由組長統一設定好，這裡了解概念即可。
> ⚠️ TODO：補上你們實際的路由表

- 切換頁面（商品列表 ↔ 詳情）**不會重新整理整頁**，由 Vue Router 換組件。
- **為什麼 F5 重新整理不會 404？**
  因為後端 `WebConfig` 設定了：`/farmily-web/` 底下找不到實體檔案的網址，一律 forward 回 `index.html`，交給 Vue Router 決定顯示哪頁。
  → 所以新增前端路由時，不用改後端，這條規則已經涵蓋。

範例路由表：
```js
const routes = [
  { path: '/', component: () => import('@/views/ProductList.vue') },
  { path: '/product/:id', component: () => import('@/views/ProductDetail.vue') },
  // TODO: 補你們的頁面
]
```
> `() => import(...)` 是**懶載入**：點到該頁才下載那一包 JS，首頁載入更快。

---

## 6. 名詞速查

- **SPA**：單頁應用，整站就一個 HTML，切頁靠 JS 換內容、不重新整理。
- **Endpoint**：後端開出的一個「網址入口」，例如 `GET /api/products`。
- **DTO**：給前端用的精簡資料物件（商品列表只回 4 個欄位，不含圖片）。
- **掛載 (mount)**：把 Vue 組件裝到頁面上、變成看得到的畫面。
- **懶載入**：點到某頁才下載那一頁的 JS，加快首次載入。

---

> 📝 維護：這份筆記跟著專案走（`docs/vue-guide.md`），有更新請一起 commit。
> 標記 `TODO` 的地方請依實際情況補上。
