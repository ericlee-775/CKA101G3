/*
  group-buy-detail.html — 團購詳情 + 加入 / 退出（Vue 改寫版）
  - GET  /api/group-buys/:id
  - POST /api/group-buys/:id/join (帶收件 + 付款資訊)
  - POST /api/group-buys/:id/withdraw
*/

const { createApp } = Vue;

const GBD_FALLBACK_IMG = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=1400&q=80";

createApp({
  data() {
    return {
      gb: null,
      loadState: "loading", // loading / ready / error:訊息
      modalOpen: false,
      form: {
        quantity: 1,
        paymentMethod: "CREDIT_CARD_SIM",
        recipientName: "",
        recipientPhone: "",
        shippingZipcode: "",
        shippingCity: "",
        shippingDist: "",
        shippingDetail: "",
        note: ""
      },
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  computed: {
    coverImage() {
      return (this.gb && this.gb.productImageUrl && this.gb.productImageUrl.trim()) || GBD_FALLBACK_IMG;
    },
    canJoin() {
      const s = this.gb && this.gb.status;
      return s === "OPEN" || s === "ONGOING" || s === "ACTIVE";
    }
  },
  mounted() {
    const id = new URLSearchParams(location.search).get("id");
    if (!id) { this.loadState = "error:缺少團購 id"; return; }
    this.reload(id);
  },
  methods: {
    async reload(id) {
      try {
        this.gb = await NongAuth.request(`/api/group-buys/${id}`);
        this.loadState = "ready";
      } catch (e) {
        this.loadState = "error:團購載入失敗：" + e.message;
      }
    },
    openJoinModal() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      this.form = {
        quantity: 1,
        paymentMethod: "CREDIT_CARD_SIM",
        recipientName: user.name || "",
        recipientPhone: "",
        shippingZipcode: "",
        shippingCity: "",
        shippingDist: "",
        shippingDetail: "",
        note: ""
      };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitJoin() {
      const payload = {
        quantity: Number(this.form.quantity) || 1,
        paymentMethod: this.form.paymentMethod,
        recipientName: this.form.recipientName.trim(),
        recipientPhone: this.form.recipientPhone.trim(),
        shippingZipcode: this.form.shippingZipcode.trim(),
        shippingCity: this.form.shippingCity.trim(),
        shippingDist: this.form.shippingDist.trim(),
        shippingDetail: this.form.shippingDetail.trim(),
        note: this.form.note.trim() || null
      };
      try {
        await NongAuth.request(`/api/group-buys/${this.gb.id}/join`, { method: "POST", body: JSON.stringify(payload) });
        this.closeModal();
        this.showToast("已加入團購");
        await this.reload(this.gb.id);
      } catch (err) {
        this.showToast(err.message || "加入失敗", true);
      }
    },
    async withdraw() {
      if (!confirm("確定要退出這個團購嗎？")) return;
      try {
        await NongAuth.request(`/api/group-buys/${this.gb.id}/withdraw`, { method: "POST" });
        this.showToast("已退出");
        await this.reload(this.gb.id);
      } catch (e) {
        this.showToast(e.message || "退出失敗", true);
      }
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    fmt(iso) {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      const p = (n) => String(n).padStart(2, "0");
      return `${p(d.getMonth() + 1)}/${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
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
