/*
  checkout.html — 結帳（Vue 改寫版）
  - GET /api/cart 取摘要
  - POST /api/orders/checkout 送出
  - 依 OrderService 行為，會依小農自動拆單為多張 Order
*/

const { createApp } = Vue;

createApp({
  data() {
    return {
      gate: "loading", // loading / non-member / empty / ready / error:訊息
      cart: null,
      form: {
        recipientName: "",
        recipientPhone: "",
        shippingAddress: "",
        note: "",
        paymentMethod: "CREDIT_CARD_SIM"
      },
      submitting: false,
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      const user = NongAuth.getConsumerUser();
      if (!user) { location.href = "login.html?reason=auth"; return; }
      if (user.type !== "MEMBER") { this.gate = "non-member"; return; }
      this.form.recipientName = user.name || "";
      try {
        this.cart = await NongAuth.request("/api/cart");
      } catch (e) {
        this.gate = "error:購物車載入失敗：" + e.message;
        return;
      }
      if (!this.cart.items || !this.cart.items.length) { this.gate = "empty"; return; }
      this.gate = "ready";
    },
    async submit() {
      const payload = {
        recipientName: this.form.recipientName.trim(),
        recipientPhone: this.form.recipientPhone.trim(),
        shippingAddress: this.form.shippingAddress.trim(),
        note: this.form.note.trim() || null,
        paymentMethod: this.form.paymentMethod
      };
      this.submitting = true;
      try {
        const orders = await NongAuth.request("/api/orders/checkout", {
          method: "POST",
          body: JSON.stringify(payload)
        });
        this.showToast(`已建立 ${orders.length} 張訂單`);
        await NongHeader.refreshCartBadge();
        setTimeout(() => (location.href = "orders.html"), 700);
      } catch (err) {
        this.showToast(err.message || "結帳失敗", true);
        this.submitting = false;
      }
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
