/*
  farm-trip-detail.html — 體驗活動詳情 + 場次選擇 + 預約（Vue 改寫版）
  - GET  /api/farm-trips/:id
  - POST /api/farm-trips/sessions/:sessionId/orders
*/

const { createApp } = Vue;

const FTD_TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const FTD_PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每公斤" };
const WD_LABELS = ["週日", "週一", "週二", "週三", "週四", "週五", "週六"];
const MO_LABELS = ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"];

createApp({
  data() {
    return {
      trip: null,
      loadState: "loading", // loading / ready / error:訊息
      now: Date.now(),
      modalOpen: false,
      activeSession: null,
      form: { numPeople: 1, contactName: "", contactPhone: "", note: "" },
      tripTypeLabels: FTD_TRIP_TYPE_LABELS,
      pricingLabels: FTD_PRICING_LABELS,
      moLabels: MO_LABELS,
      wdLabels: WD_LABELS,
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  computed: {
    coverImage() {
      return (this.trip && this.trip.imageUrl && this.trip.imageUrl.trim()) ||
        "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1400&q=80";
    },
    unit() {
      return this.trip ? (FTD_PRICING_LABELS[this.trip.pricingMode] || "每人") : "";
    },
    isPerWeight() {
      return this.trip && this.trip.pricingMode === "PER_WEIGHT";
    },
    sessionGroups() {
      if (!this.trip) return [];
      const visible = (this.trip.sessions || []).filter((s) => s.status !== "CANCELLED");
      const groups = new Map();
      visible.forEach((s) => {
        const d = new Date(s.tripStart);
        const key = `${d.getFullYear()}-${d.getMonth()}`;
        if (!groups.has(key)) groups.set(key, { y: d.getFullYear(), m: d.getMonth(), items: [] });
        groups.get(key).items.push(s);
      });
      return Array.from(groups.values());
    }
  },
  mounted() {
    this.loadTrip();
  },
  methods: {
    async loadTrip() {
      const id = new URLSearchParams(location.search).get("id");
      if (!id) { this.loadState = "error:缺少活動 id"; return; }
      try {
        this.trip = await NongAuth.request(`/api/farm-trips/${id}`);
        this.loadState = "ready";
      } catch (e) {
        this.loadState = "error:活動載入失敗：" + e.message;
      }
    },
    // 計算單一場次的可預約狀態、徽章、按鈕文字等
    sessionMeta(s) {
      const start = new Date(s.tripStart);
      const bookStart = new Date(s.bookStart).getTime();
      const bookEnd = new Date(s.bookEnd).getTime();
      const tooEarly = bookStart > this.now;
      const tooLate = bookEnd < this.now;
      const noSlot = !this.isPerWeight && (s.remaining ?? 0) <= 0;
      const completed = s.status === "COMPLETED";
      const bookable = s.status === "ACTIVE" && !tooEarly && !tooLate && !noSlot;

      let badge = null;
      if (completed) badge = { cls: "badge-cancelled", text: "已結束" };
      else if (tooLate) badge = { cls: "badge-cancelled", text: "報名結束" };
      else if (noSlot) badge = { cls: "badge-low", text: "已額滿" };
      else if (!this.isPerWeight && (s.remaining ?? 99) <= 3) badge = { cls: "badge-low", text: `剩 ${s.remaining} 名` };
      else if (bookEnd - this.now < 3 * 86400000 && !tooLate) badge = { cls: "badge-soon", text: "即將截止" };

      let btnLabel = "預約此場";
      if (completed) btnLabel = "已結束";
      else if (s.status === "CANCELLED") btnLabel = "已取消";
      else if (tooEarly) btnLabel = `${this.fmtDate(s.bookStart)} 起開放`;
      else if (tooLate) btnLabel = "報名已截止";
      else if (noSlot) btnLabel = "已額滿";

      return { start, bookable, badge, btnLabel };
    },
    capLine(s) {
      if (this.isPerWeight) return "無人數上限";
      return `已預約 ${s.attendance ?? 0} / ${(s.attendance ?? 0) + (s.remaining ?? 0)} 人`;
    },
    openModal(s) {
      const meta = this.sessionMeta(s);
      if (!meta.bookable) return;
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      if (user.type !== "MEMBER") { this.showToast("僅會員可預約", true); return; }
      this.activeSession = s;
      this.form = { numPeople: 1, contactName: user.name || "", contactPhone: "", note: "" };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
      this.activeSession = null;
    },
    async submitBooking() {
      if (!this.activeSession) return;
      const payload = {
        numPeople: Number(this.form.numPeople) || 1,
        contactName: this.form.contactName.trim(),
        contactPhone: this.form.contactPhone.trim(),
        note: this.form.note.trim() || null
      };
      try {
        await NongAuth.request(`/api/farm-trips/sessions/${this.activeSession.id}/orders`, {
          method: "POST", body: JSON.stringify(payload)
        });
        this.closeModal();
        this.showToast("預約成功");
        setTimeout(() => (location.href = "my-farm-trip-bookings.html"), 700);
      } catch (err) {
        this.showToast(err.message || "預約失敗", true);
      }
    },
    rating(t) {
      return (t.averageRating ?? 0).toFixed(1);
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    fmtDate(iso) {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())}`;
    },
    fmtTime(iso) {
      if (!iso) return "";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return "";
      const p = (n) => String(n).padStart(2, "0");
      return `${p(d.getHours())}:${p(d.getMinutes())}`;
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
