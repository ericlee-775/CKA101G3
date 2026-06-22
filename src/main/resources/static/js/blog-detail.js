/*
  blog-detail.html — 文章詳情 + 按讚 + 留言（Vue 改寫版）
  - GET  /api/blogs/:id
  - GET  /api/blogs/:id/comments
  - POST /api/blogs/:id/like
  - POST /api/blogs/:id/report
  - POST /api/blogs/:id/comments
*/

const { createApp } = Vue;

const BD_FEATURED_FALLBACK = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=600";

createApp({
  data() {
    return {
      blogId: null,
      blog: null,
      comments: [],
      loadState: "loading", // loading / ready / error:訊息
      commentText: "",
      toast: { msg: "", error: false, visible: false },
      toastTimer: null
    };
  },
  mounted() {
    this.blogId = new URLSearchParams(location.search).get("id");
    if (!this.blogId) { this.loadState = "error:缺少文章 id"; return; }
    this.reload();
  },
  methods: {
    async reload() {
      try {
        const [b, comments] = await Promise.all([
          NongAuth.request(`/api/blogs/${this.blogId}`),
          NongAuth.request(`/api/blogs/${this.blogId}/comments?size=50`)
        ]);
        this.blog = b;
        this.comments = comments.content || [];
        this.loadState = "ready";
      } catch (e) {
        this.loadState = "error:文章載入失敗：" + e.message;
      }
    },
    async addFeaturedToCart(p) {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入"); setTimeout(() => (location.href = "login.html"), 700); return; }
      if (user.type !== "MEMBER") { this.showToast("非會員身份無法購買", true); return; }
      p._adding = true;
      try {
        await NongAuth.request("/api/cart/items", { method: "POST", body: JSON.stringify({ productId: Number(p.id), quantity: 1 }) });
        this.showToast("已加入購物車");
        if (window.NongHeader && NongHeader.refreshCartBadge) await NongHeader.refreshCartBadge();
      } catch (e) {
        this.showToast(e.message || "加入失敗", true);
      } finally {
        p._adding = false;
      }
    },
    async like() {
      try {
        const updated = await NongAuth.request(`/api/blogs/${this.blogId}/like`, { method: "POST" });
        this.blog = updated;
        this.showToast("已按讚");
      } catch (e) {
        this.showToast(e.message || "按讚失敗", true);
      }
    },
    async report() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入再檢舉"); return; }
      const reason = prompt("檢舉原因？(會送到管理員)");
      if (!reason) return;
      try {
        await NongAuth.request(`/api/blogs/${this.blogId}/report`, { method: "POST", body: JSON.stringify({ reason }) });
        this.showToast("已送出檢舉，謝謝你");
      } catch (e) {
        this.showToast(e.message || "檢舉失敗", true);
      }
    },
    async submitComment() {
      const user = NongAuth.getConsumerUser();
      if (!user) { this.showToast("請先登入再留言"); setTimeout(() => (location.href = "login.html"), 700); return; }
      const content = this.commentText.trim();
      if (!content) { this.showToast("請輸入留言內容", true); return; }
      try {
        await NongAuth.request(`/api/blogs/${this.blogId}/comments`, { method: "POST", body: JSON.stringify({ content }) });
        this.showToast("已留言");
        this.commentText = "";
        await this.reload();
      } catch (err) {
        this.showToast(err.message || "留言失敗", true);
      }
    },
    featuredImage(p) {
      return p.imageUrl && p.imageUrl.trim() ? p.imageUrl : BD_FEATURED_FALLBACK;
    },
    currency(v) {
      return `NT$ ${Number(v).toLocaleString("zh-TW")}`;
    },
    fmt(iso) {
      if (!iso) return "—";
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      const p = (n) => String(n).padStart(2, "0");
      return `${d.getFullYear()}/${p(d.getMonth() + 1)}/${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
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
