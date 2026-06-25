/*
  product-detail.html — 商品詳情（Vue 改寫版）
  - GET /api/public/products/:id
  - POST /api/cart/items
  - 收藏：GET/POST/DELETE /api/wishlist/products/:id
  - POST /api/group-buys/requests （若 groupBuyEnabled）
*/

const { createApp } = Vue;

const DETAIL_FALLBACK_IMG =
  "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=1400&q=80";

createApp({
  data() {
    return {
      product: null,
      loadState: "loading", // loading / ready / error:訊息
      qty: 1,
      inWishlist: false,
      wishBusy: false,
      addBusy: false,
      modalOpen: false,
      gb: { target: 5, price: 0, open: "", deadline: "", message: "" },
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  computed: {
    coverImage() {
      const p = this.product;
      return (p && p.imageUrl && p.imageUrl.trim()) || DETAIL_FALLBACK_IMG;
    }
  },
  mounted() {
    this.loadProduct();
  },
  methods: {
    async loadProduct() {
      const id = new URLSearchParams(location.search).get("id");
      if (!id) { this.loadState = "error:缺少商品 id"; return; }
      try {
        this.product = await NongAuth.request(`/api/public/products/${id}`);
        this.loadState = "ready";
        this.syncWishlistState();
      } catch (e) {
        this.loadState = "error:商品載入失敗：" + e.message;
      }
    },

    /* ---------- 收藏 ---------- */
    async syncWishlistState() {
      const user = NongAuth.getConsumerUser();
      if (!user || user.type !== "MEMBER") { this.inWishlist = false; return; }
      try {
        const r = await NongAuth.request(`/api/wishlist/products/${this.product.id}/check`);
        this.inWishlist = !!r.inWishlist;
      } catch (_e) {
        this.inWishlist = false;
      }
    },
    async toggleWishlist() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      if (user.type !== "MEMBER") { this.showToast("非會員無法收藏", true); return; }
      this.wishBusy = true;
      try {
        if (this.inWishlist) {
          await NongAuth.request(`/api/wishlist/products/${this.product.id}`, { method: "DELETE" });
          this.inWishlist = false;
          this.showToast("已移除收藏");
        } else {
          await NongAuth.request(`/api/wishlist/products/${this.product.id}`, { method: "POST" });
          this.inWishlist = true;
          this.showToast("已加入收藏");
        }
      } catch (e) {
        this.showToast(e.message || "操作失敗", true);
      } finally {
        this.wishBusy = false;
      }
    },

    /* ---------- 購物車 ---------- */
    async addToCart() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      if (user.type !== "MEMBER") { this.showToast("非會員身份無法購買", true); return; }
      const qty = Math.max(1, Number(this.qty) || 1);
      this.addBusy = true;
      try {
        await NongAuth.request("/api/cart/items", {
          method: "POST",
          body: JSON.stringify({ productId: this.product.id, quantity: qty })
        });
        this.showToast("已加入購物車");
        await NongHeader.refreshCartBadge();
      } catch (e) {
        this.showToast(e.message || "加入失敗", true);
      } finally {
        this.addBusy = false;
      }
    },

    /* ---------- 發起團購 ---------- */
    openGroupBuyModal() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      const now = new Date(Date.now() + 5 * 60 * 1000);
      const dl = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
      this.gb = {
        target: 5,
        price: Number(this.product.price),
        open: this.toLocalInput(now),
        deadline: this.toLocalInput(dl),
        message: ""
      };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitGroupBuy() {
      const payload = {
        productId: this.product.id,
        targetQuantity: Number(this.gb.target),
        groupPrice: Number(this.gb.price),
        openDate: this.gb.open,
        deadlineDate: this.gb.deadline,
        message: this.gb.message || null
      };
      try {
        await NongAuth.request("/api/group-buys/requests", {
          method: "POST",
          body: JSON.stringify(payload)
        });
        this.closeModal();
        this.showToast("已送出申請，等小農審核");
      } catch (err) {
        this.showToast(err.message || "送出失敗", true);
      }
    },

    /* ---------- 工具 ---------- */
    toLocalInput(d) {
      const pad = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    showToast(msg, isError) {
      this.toast = { msg, error: !!isError, visible: true };
      clearTimeout(this.toastTimer);
      this.toastTimer = setTimeout(() => { this.toast.visible = false; }, 1800);
    },
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
