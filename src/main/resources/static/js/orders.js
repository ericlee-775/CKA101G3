/*
  orders.html — 我的訂單列表（Vue 改寫版）
  - GET /api/orders
*/

const { createApp } = Vue;

const ORDER_STATUS_LABEL = {
  PENDING_PAYMENT: { text: "待付款", cls: "warning" },
  PAID:            { text: "已付款・等待出貨", cls: "" },
  SHIPPED:         { text: "已出貨", cls: "" },
  COMPLETED:       { text: "已完成", cls: "success" },
  CANCELLED:       { text: "已取消", cls: "muted" }
};

createApp({
  data() {
    return {
      orders: [],
      loadState: "loading" // loading / empty / ready / error:訊息
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      const user = NongAuth.getConsumerUser();
      if (!user) { location.href = "login.html?reason=auth"; return; }
      try {
        const page = await NongAuth.request("/api/orders?size=30");
        this.orders = page.content || [];
        this.loadState = this.orders.length ? "ready" : "empty";
      } catch (e) {
        this.loadState = "error:訂單載入失敗：" + e.message;
      }
    },
    statusOf(o) {
      return ORDER_STATUS_LABEL[o.status] || { text: o.status, cls: "muted" };
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
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
