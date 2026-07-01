import { createApp } from 'vue'
import './assets/theme.css'     // 引入全站主題顏色（定義 --leaf 等變數），要在元件之前載入
import App from './App.vue'
import router from './router'   // 引入剛剛建立的路由表（會自動找 router/index.js）

const app = createApp(App)
app.use(router)                 // 啟用 router，整個 App 才有路由功能
app.mount('#app')
