/*
  products.html — 商品列表頁（Vue 改寫版）
  - 分類 pill：GET /api/public/categories
  - 商品：GET /api/public/products?categoryId=&keyword=
  - 加入購物車：POST /api/cart/items (需登入消費者)
*/

const { createApp } = Vue;

const FALLBACK_IMG = [
  "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1561136594-7f68413baa99?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1566385101042-1a0aa0c1268c?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1587049352851-8d4e89133924?auto=format&fit=crop&w=900&q=80",
  "https://images.unsplash.com/photo-1607083206968-13611e3d76db?auto=format&fit=crop&w=900&q=80"
];

createApp({
  data() {
    return {
      categories: [{ id: null, name: "全部" }],
      activeCategoryId: null,
      keyword: "",
      products: [],
      listState: "loading", // loading / ready / empty / error:訊息
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  mounted() {
    this.loadFiltersFromQuery();
    this.loadCategories();
    this.loadProducts();
  },
  methods: {
    loadFiltersFromQuery() {
      const q = new URLSearchParams(location.search);
      if (q.has("categoryId")) this.activeCategoryId = Number(q.get("categoryId")) || null;
      if (q.has("keyword")) this.keyword = q.get("keyword");
    },
    async loadCategories() {
      try {
        const cats = await NongAuth.request("/api/public/categories");
        this.categories = [{ id: null, name: "全部" }, ...cats];
      } catch (e) {
        console.warn("/api/public/categories 失敗：", e.message);
      }
    },
    async loadProducts() {
      this.listState = "loading";
      const params = new URLSearchParams({ size: "24" });
      if (this.activeCategoryId != null) params.set("categoryId", String(this.activeCategoryId));
      if (this.keyword) params.set("keyword", this.keyword);
      try {
        const page = await NongAuth.request("/api/public/products?" + params.toString());
        this.products = page.content || [];
        this.listState = this.products.length ? "ready" : "empty";
      } catch (e) {
        this.products = [];
        this.listState = "error:商品載入失敗：" + e.message;
      }
    },
    async selectCategory(id) {
      this.activeCategoryId = id;
      this.syncQuery();
      await this.loadProducts();
    },
    async submitSearch() {
      this.keyword = this.keyword.trim();
      this.syncQuery();
      await this.loadProducts();
    },
    syncQuery() {
      const params = new URLSearchParams();
      if (this.activeCategoryId != null) params.set("categoryId", String(this.activeCategoryId));
      if (this.keyword) params.set("keyword", this.keyword);
      const next = params.toString();
      history.replaceState(null, "", next ? `?${next}` : location.pathname);
    },
    async addToCart(product) {
      const user = NongAuth.getConsumerUser();
      if (!user) {
        this.showToast("請先登入再加入購物車", true);
        setTimeout(() => (location.href = "login.html"), 800);
        return;
      }
      if (user.type !== "MEMBER") {
        this.showToast("小農 / 管理員身份無法購買", true);
        return;
      }
      product._adding = true;
      try {
        await NongAuth.request("/api/cart/items", {
          method: "POST",
          body: JSON.stringify({ productId: product.id, quantity: 1 })
        });
        this.showToast("已加入購物車");
        await NongHeader.refreshCartBadge();
      } catch (e) {
        this.showToast(e.message || "加入失敗", true);
      } finally {
        product._adding = false;
      }
    },
    showToast(msg, isError) {
      this.toast = { msg, error: !!isError, visible: true };
      clearTimeout(this.toastTimer);
      this.toastTimer = setTimeout(() => { this.toast.visible = false; }, 1800);
    },
    productImage(p, i) {
      return (p.imageUrl && p.imageUrl.trim()) || FALLBACK_IMG[i % FALLBACK_IMG.length];
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    same(a, b) {
      return (a == null && b == null) || a === b;
    },
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
