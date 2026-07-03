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
import FarmGameView from '@/views/FarmGameView.vue'
// 會員 / 小農 帳號相關頁
import ForgotPasswordView from '@/views/ForgotPasswordView.vue'
import ResetPasswordView from '@/views/ResetPasswordView.vue'
import VerifyEmailView from '@/views/VerifyEmailView.vue'
import ResendVerificationView from '@/views/ResendVerificationView.vue'
import MemberProfileView from '@/views/MemberProfileView.vue'
import FarmerProfileView from '@/views/FarmerProfileView.vue'
import FarmerApplicationView from '@/views/FarmerApplicationView.vue'

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
    { path: '/farm-game',   name: 'farm-game',  component: FarmGameView },
    { path: '/login',       name: 'login',      component: LoginView },
    { path: '/register',    name: 'register',   component: RegisterView },
    // 小農系統:與一般會員登入(/login、/register)分開的獨立入口
    { path: '/farmer/login',    name: 'farmer-login',    component: FarmerLoginView },
    { path: '/farmer/register', name: 'farmer-register', component: FarmerRegisterView },

    // 帳號共用：忘記/重設密碼、Email 驗證
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordView },
    { path: '/reset-password',  name: 'reset-password',  component: ResetPasswordView },
    { path: '/verify-email',    name: 'verify-email',    component: VerifyEmailView },
    { path: '/resend-verification', name: 'resend-verification', component: ResendVerificationView },

    // 會員 / 小農 個人中心
    { path: '/member/me',        name: 'member-me',        component: MemberProfileView },
    { path: '/farmer/me',        name: 'farmer-me',        component: FarmerProfileView },
    { path: '/farmer/application', name: 'farmer-application', component: FarmerApplicationView }
  ],
})

export default router
