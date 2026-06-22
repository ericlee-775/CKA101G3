/*
  farm-trips.html — 體驗活動列表（Vue 改寫版）
  - GET /api/farm-trips?size=&tripType=
  - 分類用 tripType (FARM_EXPERIENCE / FIELD_VISIT)
*/

const { createApp } = Vue;

const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };

createApp({
  data() {
    return {
      activeTripType: new URLSearchParams(location.search).get("tripType") || null,
      trips: [],
      listState: "loading", // loading / ready / empty / error:訊息
      pills: [
        { type: null, name: "全部" },
        { type: "FARM_EXPERIENCE", name: "農場體驗" },
        { type: "FIELD_VISIT", name: "產地參訪" }
      ],
      tripTypeLabels: TRIP_TYPE_LABELS
    };
  },
  mounted() {
    this.loadTrips();
  },
  methods: {
    async loadTrips() {
      this.listState = "loading";
      const params = new URLSearchParams({ size: "24" });
      if (this.activeTripType) params.set("tripType", this.activeTripType);
      try {
        const page = await NongAuth.request("/api/farm-trips?" + params.toString());
        this.trips = page.content || [];
        this.listState = this.trips.length ? "ready" : "empty";
      } catch (e) {
        this.listState = "error:體驗活動載入失敗：" + e.message;
      }
    },
    selectType(type) {
      this.activeTripType = type;
      this.syncTripQuery();
      this.loadTrips();
    },
    syncTripQuery() {
      const params = new URLSearchParams();
      if (this.activeTripType) params.set("tripType", this.activeTripType);
      const next = params.toString();
      history.replaceState(null, "", next ? `?${next}` : location.pathname);
    },
    tripImage(t) {
      return (t.imageUrl && t.imageUrl.trim()) || "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";
    },
    tripUnit(t) {
      return PRICING_LABELS[t.pricingMode] || "每人";
    },
    rating(t) {
      return (t.averageRating ?? 0).toFixed(1);
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
