import { createRouter, createWebHistory } from 'vue-router'
import authStore from '@/stores/auth'

// 版型（Layout）：決定一群頁面共用的外框
import ShopLayout from '@/components/layout/ShopLayout.vue'      // 商城前台：頂部導覽 + 頁尾
import FarmerLayout from '@/components/layout/FarmerLayout.vue'  // 小農管理：側邊欄 + 內容區
import MemberLayout from '@/components/layout/MemberLayout.vue'  // 會員中心：左側選單 + 內容區（巢狀在 ShopLayout 內）

// 每一個頁面，就是一個 .vue 元件
// 商城前台頁面
import HomeView from '@/views/shop/HomeView.vue'
import NewsView from '@/views/shop/NewsView.vue'
import FarmilyView from '@/views/shop/Farmily.vue'
import ProductsView from '@/views/shop/ProductsView.vue'
import ProductView from '@/views/shop/ProductView.vue'
import CartView from '@/views/shop/CartView.vue'
import GroupBuysView from '@/views/shop/GroupBuysView.vue'
import GroupBuyDetailView from '@/views/shop/GroupBuyDetailView.vue'
import BlogsView from '@/views/shop/BlogsView.vue'
import FarmTripsView from '@/views/shop/FarmTripsView.vue'
import FarmMapView from '@/views/shop/FarmMapView.vue'
import FarmGameView from '@/views/shop/FarmGameView.vue'
import AboutView from '@/views/shop/AboutView.vue'
// 帳號相關：登入 / 註冊 / 密碼 / Email 驗證 / 小農申請
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import FarmerLoginView from '@/views/auth/FarmerLoginView.vue'
import FarmerRegisterView from '@/views/auth/FarmerRegisterView.vue'
import FarmerApplicationView from '@/views/auth/FarmerApplicationView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
import ResetPasswordView from '@/views/auth/ResetPasswordView.vue'
import VerifyEmailView from '@/views/auth/VerifyEmailView.vue'
import ResendVerificationView from '@/views/auth/ResendVerificationView.vue'
// 一般會員中心：個人資料 / 訂單 / 優惠券 / 通知
import MemberProfileView from '@/views/member/MemberProfileView.vue'
import MemberOrdersView from '@/views/member/MemberOrdersView.vue'
import MemberCouponsView from '@/views/member/MemberCouponsView.vue'
import MemberNotificationsView from '@/views/member/MemberNotificationsView.vue'
// 小農管理後台：商家資料 / 商品 / 團購 / 訂單 / 體驗活動 / 通知（優惠券發放歸管理員後台）
import FarmerProfileView from '@/views/farmer/FarmerProfileView.vue'
import FarmerProductsView from '@/views/farmer/FarmerProductsView.vue'
import FarmerGroupBuysView from '@/views/farmer/FarmerGroupBuysView.vue'
import FarmerOrdersView from '@/views/farmer/FarmerOrdersView.vue'
import FarmerFarmTripsView from '@/views/farmer/FarmerFarmTripsView.vue'
import FarmerNotificationsView from '@/views/farmer/FarmerNotificationsView.vue'
// 找不到頁面（404）：對不到上面任何路由時顯示
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  // import.meta.env.BASE_URL 會自動讀 vite.config.js 的 base，
  // 將來你部署到 /farmily-web/ 子路徑也不用改這裡。
  history: createWebHistory(import.meta.env.BASE_URL),

  // 「網址 ↔ 顯示哪個元件」的對應表。
  // 用巢狀路由把頁面掛在對應的 Layout 底下：
  //   - 商城前台頁面 → 套 ShopLayout（頂部導覽 + 頁尾）
  //   - 小農管理頁面 → 套 FarmerLayout（側邊欄）
  // 子路由的 path 不以 / 開頭，會接在父層 path 後面組成完整網址。
  routes: [
    // ===== 商城前台：ShopLayout（頂部橫向導覽 + 頁尾）=====
    {
      path: '/',
      component: ShopLayout,
      children: [
        { path: '',            name: 'home',       component: HomeView },
        { path: 'news',        name: 'news',       component: NewsView },
        { path: 'farmily',     name: 'farmily',    component: FarmilyView },
        { path: 'products',    name: 'products',   component: ProductsView },
        // 商品詳情頁：/products/:productId（圖片 + 描述等，資料走 /api/products/{id} 與 /photo）
        { path: 'products/:productId', name: 'product-detail', component: ProductView },
        { path: 'cart',        name: 'cart',       component: CartView },
        { path: 'group-buys',  name: 'group-buys', component: GroupBuysView },
        // 團購詳情頁：/group-buys/:groupBuyId（資料走 /api/groupBuy/{id}）
        { path: 'group-buys/:groupBuyId', name: 'group-buy-detail', component: GroupBuyDetailView },
        { path: 'blogs',       name: 'blogs',      component: BlogsView },
        { path: 'farm-trips',  name: 'farm-trips', component: FarmTripsView },
        { path: 'farm-map',    name: 'farm-map',   component: FarmMapView },
        { path: 'farm-game',   name: 'farm-game',  component: FarmGameView },
        { path: 'about',       name: 'about',      component: AboutView },

        // 一般會員登入 / 註冊
        { path: 'login',       name: 'login',      component: LoginView },
        { path: 'register',    name: 'register',   component: RegisterView },

        // 小農系統的「公開入口」：登入 / 註冊 / 申請小農（未登入也能開，不套側邊欄）
        { path: 'farmer/login',       name: 'farmer-login',       component: FarmerLoginView },
        { path: 'farmer/register',    name: 'farmer-register',    component: FarmerRegisterView },
        { path: 'farmer/application', name: 'farmer-application', component: FarmerApplicationView },

        // 帳號共用：忘記/重設密碼、Email 驗證
        { path: 'forgot-password',     name: 'forgot-password',     component: ForgotPasswordView },
        { path: 'reset-password',      name: 'reset-password',      component: ResetPasswordView },
        { path: 'verify-email',        name: 'verify-email',        component: VerifyEmailView },
        { path: 'resend-verification', name: 'resend-verification', component: ResendVerificationView },

        // 會員中心：再套一層 MemberLayout（左側選單），整組需登入會員。
        // meta 放在父層，Vue Router 會把父子路由的 meta 合併，子路由自動繼承 requiresAuth。
        {
          path: 'member',
          component: MemberLayout,
          meta: { requiresAuth: 'MEMBER' },
          // 直接開 /member 時導到個人資料
          redirect: { name: 'member-me' },
          children: [
            { path: 'me', name: 'member-me', component: MemberProfileView },
            // 以下三頁各自獨立成元件，功能開發中先放佔位內容
            { path: 'orders',        name: 'member-orders',        component: MemberOrdersView },
            { path: 'coupons',       name: 'member-coupons',       component: MemberCouponsView },
            { path: 'notifications', name: 'member-notifications', component: MemberNotificationsView },
          ],
        },
      ],
    },

    // ===== 小農管理後台：FarmerLayout（側邊欄）=====
    {
      path: '/farmer',
      component: FarmerLayout,
      // 整個小農後台都需登入小農身分（meta 會被子路由繼承）
      meta: { requiresAuth: 'FARMER' },
      // 直接開 /farmer 時導到商家資料
      redirect: { name: 'farmer-me' },
      children: [
        { path: 'me',            name: 'farmer-me',            component: FarmerProfileView },
        { path: 'products',      name: 'farmer-products',      component: FarmerProductsView },
        { path: 'group-buys',    name: 'farmer-group-buys',    component: FarmerGroupBuysView },
        { path: 'orders',        name: 'farmer-orders',        component: FarmerOrdersView },
        { path: 'farm-trips',    name: 'farmer-farm-trips',    component: FarmerFarmTripsView },
        { path: 'notifications', name: 'farmer-notifications', component: FarmerNotificationsView },
      ],
    },

    // ===== 404：放最後，:pathMatch(.*)* 會攔截所有對不到上面規則的網址 =====
    // 例如 /abc、/products/xyz/oops 這種打錯的網址，都會導到 404 頁。
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
  ],
})

// ===== 全站導航守衛 =====
// 伺服器端只看得到 index.html 這個殼，擋不住前端路由；真正「哪些頁面要登入」在這裡把關。
// 受保護頁面（meta.requiresAuth）未登入或身分不符時，導去對應登入頁，
// 並用 query.redirect 記住原本要去的地方，登入後接回。
router.beforeEach(async (to) => {
  const need = to.meta.requiresAuth
  if (!need) return true

  // F5 或直接貼網址進來時，App 的 hydrate 可能還沒跑完，先確保已向後端確認過 session
  await authStore.ensureHydrated()

  const ok = need === 'FARMER' ? authStore.isFarmer : authStore.isMember
  if (authStore.isLoggedIn && ok) return true

  const loginName = need === 'FARMER' ? 'farmer-login' : 'login'
  return { name: loginName, query: { redirect: to.fullPath } }
})

export default router
