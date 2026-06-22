/*
  group-buys.html — 團購列表頁（Vue 改寫版）
  - GET /api/group-buys?page=&size=
*/

const { createApp } = Vue;

const GB_FALLBACK_IMG = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";

createApp({
  data() {
    return {
      groupBuys: [],
      listState: "loading" // loading / ready / empty / error:訊息
    };
  },
  mounted() {
    this.loadGroupBuys();
  },
  methods: {
    async loadGroupBuys() {
      this.listState = "loading";
      try {
        const page = await NongAuth.request("/api/group-buys?size=24");
        this.groupBuys = page.content || [];
        this.listState = this.groupBuys.length ? "ready" : "empty";
      } catch (e) {
        this.listState = "error:團購載入失敗：" + e.message;
      }
    },
    groupImage(g) {
      return g.productImageUrl && g.productImageUrl.trim() ? g.productImageUrl : GB_FALLBACK_IMG;
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    fmt(iso) {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${m}/${day}`;
    },
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
