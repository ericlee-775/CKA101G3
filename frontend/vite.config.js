import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import path from 'node:path' // 1. 引入 Node.js 的路徑模組

const __dirname = fileURLToPath(new URL('.', import.meta.url))
// https://vite.dev/config/
export default defineConfig({
  // 網頁由 Spring Boot 從 /farmily-web/ 子路徑提供，base 必須一致，
  // 否則打包後 JS/CSS 會 404（白畫面）。router 用 import.meta.env.BASE_URL 會自動跟著走。
  base: '/farmily-web/',
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
   // 2. 這是「部署模式」:npm run build 時
   // ,Vue 打包後的檔案直接丟進 Spring Boot 的 static/ 資料夾,
   // 讓 Spring Boot 一個服務同時提供前端網頁 + 後端 API。
  build: {
    // 輸出到 Spring Boot 的 static/farmily-web。
    // 用相對路徑:frontend/ 的上一層就是專案根(CKA101G3),
    // 這樣每個隊友 clone 下來路徑都一致,不必再各自改絕對路徑。
    outDir: '../src/main/resources/static/farmily-web',
    emptyOutDir: true, // 打包前自動清空舊檔案
    assetsDir: 'static' // 讓 js/css 檔案統一放在 static 資料夾內
  },
  //這是「開發模式」:npm run dev 時(前端跑在 5173),
  // 只要程式碼裡呼叫 /api/xxx,Vite 會幫你轉發到 http://localhost:8080,避免瀏覽器跨域(CORS)問題。
   server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的 Spring Boot 運行網址與 Port
        changeOrigin: true,             // 允許跨域
      }
    }
  }



})

