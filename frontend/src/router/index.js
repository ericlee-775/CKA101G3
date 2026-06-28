import { createRouter, createWebHistory } from 'vue-router'

// 每一個頁面，就是一個 .vue 元件
import HomeView from '@/views/HomeView.vue'
import NewsView from '@/views/NewsView.vue'
import FarmilyView from '@/views/Farmily.vue'
import ProductsView from '@/views/ProductsView.vue'
import GroupBuysView from '@/views/GroupBuysView.vue'
import BlogsView from '@/views/BlogsView.vue'
import FarmTripsView from '@/views/FarmTripsView.vue'
import FarmMapView from '@/views/FarmMapView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import FarmerLoginView from '@/views/FarmerLoginView.vue'
import FarmerRegisterView from '@/views/FarmerRegisterView.vue'

const router = createRouter({
  // import.meta.env.BASE_URL 會自動讀 vite.config.js 的 base，
  // 將來你部署到 /farmily-web/ 子路徑也不用改這裡。
  history: createWebHistory(import.meta.env.BASE_URL),

  // 這就是「網址 ↔ 顯示哪個元件」的對應表，一行一個頁面
  routes: [
    { path: '/',            name: 'home',       component: HomeView },
    { path: '/news',        name: 'news',       component: NewsView },
    { path: '/farmily',     name: 'farmily',    component: FarmilyView},
    { path: '/products',    name: 'products',   component: ProductsView },
    { path: '/group-buys',  name: 'group-buys', component: GroupBuysView },
    { path: '/blogs',       name: 'blogs',      component: BlogsView },
    { path: '/farm-trips',  name: 'farm-trips', component: FarmTripsView },
    { path: '/farm-map',    name: 'farm-map',   component: FarmMapView },
    { path: '/login',       name: 'login',      component: LoginView },
    { path: '/register',    name: 'register',   component: RegisterView },
    // 小農系統:與一般會員登入(/login、/register)分開的獨立入口
    { path: '/farmer/login',    name: 'farmer-login',    component: FarmerLoginView },
    { path: '/farmer/register', name: 'farmer-register', component: FarmerRegisterView }
  ],
})

export default router
