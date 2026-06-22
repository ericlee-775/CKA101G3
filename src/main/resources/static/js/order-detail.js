/*
  order-detail.html — 訂單詳情（消費者視角，Vue 改寫版）
  - GET /api/orders/:id
  - 依狀態提供：付款 / 取消 / 確認收貨 (POST /api/orders/:id/{pay|cancel|confirm})
*/

const { createApp } = Vue;

const OD_STATUS_LABEL = {
  PENDING_PAYMENT: { text: "待付款", cls: "warning" },
  PAID:            { text: "已付款・等待出貨", cls: "" },
  SHIPPED:         { text: "已出貨", cls: "" },
  COMPLETED:       { text: "已完成", cls: "success" },
  CANCELLED:       { text: "已取消", cls: "muted" }
};
const PAY_LABEL = {
  CREDIT_CARD_SIM: "模擬信用卡",
  BANK_TRANSFER_SIM: "模擬 ATM 轉帳",
  CASH_ON_DELIVERY: "貨到付款"
};

createApp({
  data() {
    return {
      order: null,
      loadState: "loading", // loading / ready / error:訊息
      actionBusy: false,
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  computed: {
    status() {
      const o = this.order;
      return o ? (OD_STATUS_LABEL[o.status] || { text: o.status, cls: "muted" }) : { text: "", cls: "" };
    },
    payLabel() {
      const o = this.order;
      return o ? (PAY_LABEL[o.paymentMethod] || o.paymentMethod) : "";
    }
  },
  mounted() {
    const id = new URLSearchParams(location.search).get("id");
    if (!id) { this.loadState = "error:缺少訂單 id"; return; }
    this.reload(id);
  },
  methods: {
    async reload(id) {
      try {
        this.order = await NongAuth.request(`/api/orders/${id}`);
        this.loadState = "ready";
      } catch (e) {
        this.loadState = "error:訂單載入失敗：" + e.message;
      }
    },
    async doAction(action) {
      const map = { pay: "pay", cancel: "cancel", confirm: "confirm" };
      const path = map[action];
      if (!path) return;
      this.actionBusy = true;
      try {
        await NongAuth.request(`/api/orders/${this.order.id}/${path}`, { method: "POST" });
        this.showToast(action === "pay" ? "付款完成" : action === "cancel" ? "已取消" : "已確認收貨");
        await this.reload(this.order.id);
      } catch (e) {
        this.showToast(e.message || "失敗", true);
      } finally {
        this.actionBusy = false;
      }
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    fmt(iso) {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      const pad = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
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
