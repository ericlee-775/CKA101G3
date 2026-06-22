/*
  blogs.html — 部落格列表（Vue 改寫版）
  - GET /api/blogs?size=&typeId=&keyword=
  - GET /api/blogs/types
*/

const { createApp } = Vue;

createApp({
  data() {
    return {
      activeTypeId: null,
      keyword: "",
      blogTypes: [],
      blogs: [],
      listState: "loading" // loading / ready / empty / error:訊息
    };
  },
  computed: {
    pills() {
      return [{ id: null, name: "全部" }, ...this.blogTypes.map((type) => ({ id: type.id, name: type.name }))];
    }
  },
  mounted() {
    const q = new URLSearchParams(location.search);
    if (q.has("typeId")) this.activeTypeId = Number(q.get("typeId")) || null;
    else if (q.has("blogTypeId")) this.activeTypeId = Number(q.get("blogTypeId")) || null;
    if (q.has("keyword")) this.keyword = q.get("keyword");
    this.loadTypes();
    this.loadBlogs();
  },
  methods: {
    async loadTypes() {
      try {
        const types = await NongAuth.request("/api/blogs/types");
        this.blogTypes = Array.isArray(types) ? types : [];
      } catch (e) {
        console.warn("/api/blogs/types", e.message);
        this.blogTypes = [];
      }
    },
    async loadBlogs() {
      this.listState = "loading";
      const params = new URLSearchParams({ size: "24" });
      if (this.activeTypeId != null) params.set("typeId", String(this.activeTypeId));
      if (this.keyword) params.set("keyword", this.keyword);
      try {
        const page = await NongAuth.request("/api/blogs?" + params.toString());
        this.blogs = page.content || [];
        this.listState = this.blogs.length ? "ready" : "empty";
      } catch (e) {
        this.listState = "error:文章載入失敗：" + e.message;
      }
    },
    selectType(id) {
      this.activeTypeId = id;
      this.syncQuery();
      this.loadBlogs();
    },
    submitSearch() {
      this.keyword = this.keyword.trim();
      this.syncQuery();
      this.loadBlogs();
    },
    syncQuery() {
      const params = new URLSearchParams();
      if (this.activeTypeId != null) params.set("typeId", String(this.activeTypeId));
      if (this.keyword) params.set("keyword", this.keyword);
      const next = params.toString();
      history.replaceState(null, "", next ? `?${next}` : location.pathname);
    },
    truncate(s, n) {
      s = String(s).replace(/\s+/g, " ");
      return s.length > n ? s.slice(0, n) + "…" : s;
    },
    same(a, b) {
      return (a == null && b == null) || a === b;
    },
    errorText(state) {
      return state.startsWith("error:") ? state.slice(6) : "";
    }
  }
}).mount("#app");
