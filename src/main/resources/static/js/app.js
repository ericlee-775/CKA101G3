/*
  index.html — 首頁專屬腳本（Vue 改寫版）
  - 幻燈片
  - 主題分類與精選商品
  - 體驗活動、最新消息、部落格
  共用 header 邏輯仍在 header.js；API 串接統一透過 NongAuth.request()。
*/

const { createApp } = Vue;

const HOME_PRODUCT_IMAGES = [
  "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1587049352851-8d4e89133924?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1607083206968-13611e3d76db?auto=format&fit=crop&w=900&q=80"
];

const HOME_TRIP_IMAGES = [
  "https://storage.googleapis.com/cka101-15/farmtest/hero-farm-main.jpg",
  "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1523741543316-beb7fc7023d8?auto=format&fit=crop&w=900&q=80"
];

const HOME_NEWS_IMAGE = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";

const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };

createApp({
  data() {
    return {
      // 幻燈片
      slideCount: 4,
      activeSlide: 0,
      slideTimer: null,

      // 主題分類
      homeCategories: [{ id: null, name: "全部商品" }],
      activeHomeCategoryId: null,

      // 各區塊資料與狀態（loading / ready / empty / error:訊息）
      products: [],
      productsState: "loading",
      trips: [],
      tripsState: "loading",
      news: [],
      newsState: "loading",
      blogs: [],
      blogsState: "loading",

      // 提供給樣板使用的標籤
      tripTypeLabels: TRIP_TYPE_LABELS,
      pricingLabels: PRICING_LABELS
    };
  },
  watch: {
    // 切換分類時重新拉商品
    activeHomeCategoryId() {
      this.loadFeaturedProducts();
    }
  },
  mounted() {
    this.startSlider();
    this.loadHomeCategories();
    this.loadFeaturedProducts();
    this.loadFeaturedTrips();
    this.loadLatestNews();
    this.loadRecentBlogs();
  },
  beforeUnmount() {
    clearInterval(this.slideTimer);
  },
  methods: {
    /* ---------- 幻燈片 ---------- */
    startSlider() {
      clearInterval(this.slideTimer);
      this.slideTimer = setInterval(() => {
        this.activeSlide = (this.activeSlide + 1) % this.slideCount;
      }, 5200);
    },
    goSlide(index) {
      this.activeSlide = index;
      this.startSlider();
    },

    /* ---------- 主題分類 ---------- */
    async loadHomeCategories() {
      const defaults = [{ id: null, name: "全部商品" }];
      try {
        const cats = await NongAuth.request("/api/public/categories");
        this.homeCategories = Array.isArray(cats) && cats.length
          ? [{ id: null, name: "全部商品" }, ...cats.slice(0, 5)]
          : defaults;
      } catch (_e) {
        this.homeCategories = defaults;
      }
    },
    selectCategory(id) {
      this.activeHomeCategoryId = id;
    },
    sameCategory(a, b) {
      return (a == null && b == null) || Number(a) === Number(b);
    },

    /* ---------- 精選商品 ---------- */
    async loadFeaturedProducts() {
      this.productsState = "loading";
      try {
        const params = new URLSearchParams({ size: "3" });
        if (this.activeHomeCategoryId != null) params.set("categoryId", String(this.activeHomeCategoryId));
        const page = await NongAuth.request("/api/public/products?" + params.toString());
        this.products = this.normalizePage(page);
        this.productsState = this.products.length ? "ready" : "empty";
      } catch (e) {
        this.products = [];
        this.productsState = "error:商品載入失敗：" + e.message;
      }
    },
    productImage(p, i) {
      return this.cleanImage(p.imageUrl) || HOME_PRODUCT_IMAGES[i % HOME_PRODUCT_IMAGES.length];
    },

    /* ---------- 體驗活動 ---------- */
    async loadFeaturedTrips() {
      this.tripsState = "loading";
      try {
        const page = await NongAuth.request("/api/farm-trips?size=3");
        this.trips = this.normalizePage(page);
        this.tripsState = this.trips.length ? "ready" : "empty";
      } catch (e) {
        this.trips = [];
        this.tripsState = "error:活動載入失敗：" + e.message;
      }
    },
    tripImage(t, i) {
      return this.cleanImage(t.imageUrl) || HOME_TRIP_IMAGES[i % HOME_TRIP_IMAGES.length];
    },
    tripUnit(t) {
      return this.pricingLabels[t.pricingMode] || "每人";
    },

    /* ---------- 最新消息 ---------- */
    async loadLatestNews() {
      this.newsState = "loading";
      try {
        const page = await NongAuth.request("/api/news?size=3");
        this.news = this.normalizePage(page);
        this.newsState = this.news.length ? "ready" : "empty";
      } catch (e) {
        this.news = [];
        this.newsState = "error:消息載入失敗：" + e.message;
      }
    },
    newsImage(n) {
      return this.cleanImage(n.coverImageUrl) || HOME_NEWS_IMAGE;
    },

    /* ---------- 部落格 ---------- */
    async loadRecentBlogs() {
      this.blogsState = "loading";
      try {
        const page = await NongAuth.request("/api/blogs?size=3");
        this.blogs = this.normalizePage(page);
        this.blogsState = this.blogs.length ? "ready" : "empty";
      } catch (_e) {
        this.blogs = [];
        this.blogsState = "error:文章載入失敗。";
      }
    },

    /* ---------- 共用小工具 ---------- */
    normalizePage(page) {
      if (Array.isArray(page)) return page;
      return page?.content || [];
    },
    cleanImage(url) {
      const value = String(url || "").trim();
      return value || null;
    },
    truncate(text, length) {
      const clean = String(text || "").replace(/\s+/g, " ").trim();
      return clean.length > length ? clean.slice(0, length) + "..." : clean;
    },
    currency(v) {
      return `NT$ ${Number(v || 0).toLocaleString("zh-TW")}`;
    },
    formatDate(iso) {
      if (!iso) return "近期公告";
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return "近期公告";
      const pad = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
    },
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
