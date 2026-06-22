/*
  cart.html — 購物車（Vue 改寫版）
  - GET    /api/cart           取購物車
  - PUT    /api/cart/items/:id 改數量
  - DELETE /api/cart/items/:id 移除
  - DELETE /api/cart           清空
*/

const { createApp } = Vue;

const CART_FALLBACK_IMG = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=200";

createApp({
  data() {
    return {
      gate: "loading", // loading / guest / non-member / loading-cart / ready / error:訊息
      cart: null,
      qtyTimers: {},
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  computed: {
    items() {
      return (this.cart && this.cart.items) || [];
    }
  },
  mounted() {
    const user = NongAuth.getConsumerUser();
    if (!user) { this.gate = "guest"; return; }
    if (user.type !== "MEMBER") { this.gate = "non-member"; return; }
    this.refresh();
  },
  methods: {
    async refresh() {
      this.gate = "loading-cart";
      try {
        this.cart = await NongAuth.request("/api/cart");
        this.gate = "ready";
        await NongHeader.refreshCartBadge();
      } catch (e) {
        this.gate = "error:購物車載入失敗：" + e.message;
      }
    },
    async removeItem(item) {
      item._busy = true;
      try {
        await NongAuth.request(`/api/cart/items/${item.productId}`, { method: "DELETE" });
        this.showToast("已移除");
        await this.refresh();
      } catch (e) {
        this.showToast(e.message || "移除失敗", true);
        item._busy = false;
      }
    },
    onQtyChange(item) {
      const qty = Math.max(1, Number(item.quantity) || 1);
      item.quantity = qty;
      clearTimeout(this.qtyTimers[item.productId]);
      this.qtyTimers[item.productId] = setTimeout(async () => {
        try {
          await NongAuth.request(`/api/cart/items/${item.productId}`, {
            method: "PUT",
            body: JSON.stringify({ quantity: qty })
          });
          await this.refresh();
        } catch (e) {
          this.showToast(e.message || "更新失敗", true);
        }
      }, 200);
    },
    async clearCart() {
      if (!confirm("確定要清空購物車嗎？")) return;
      try {
        await NongAuth.request("/api/cart", { method: "DELETE" });
        this.showToast("已清空");
        await this.refresh();
      } catch (e) {
        this.showToast(e.message || "清空失敗", true);
      }
    },
    itemImage(it) {
      return (it.imageUrl && it.imageUrl.trim()) || CART_FALLBACK_IMG;
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
