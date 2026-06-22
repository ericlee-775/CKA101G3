/*
  producers.html / producer-detail.html — 農場清單與農場介紹（Vue 改寫版）
  資料彙整（buildProducers / enrichProducer）維持原邏輯，
  渲染改由各頁 #app 的 Vue 樣板負責；同一支檔案依 data-page-kind 掛載對應 App。
*/

const { createApp } = Vue;

const PRODUCER_FALLBACKS = [
  "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=1200&q=80",
  "https://images.unsplash.com/photo-1523741543316-beb7fc7023d8?auto=format&fit=crop&w=1200&q=80",
  "https://images.unsplash.com/photo-1492496913980-501348b61469?auto=format&fit=crop&w=1200&q=80",
  "https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=1200&q=80"
];
const PRODUCT_FALLBACK = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?auto=format&fit=crop&w=900&q=80";
const FARM_VISUALS = [
  { names: ["示範農場"], url: "https://storage.googleapis.com/cka101-15/form/exampleform1.png" },
  { names: ["梨山雲頂果園"], url: "https://storage.googleapis.com/cka101-15/form/fruitform.png" },
  { names: ["池上禾田"], url: "https://storage.googleapis.com/cka101-15/form/miform1.png" },
  { names: ["東山放牧蛋場"], url: "https://storage.googleapis.com/cka101-15/form/eggform.png" },
  { names: ["杉林溪有機茶坊"], url: "https://storage.googleapis.com/cka101-15/form/teaform1.png" }
];
const TRIP_TYPE_LABELS = { FARM_EXPERIENCE: "農場體驗", FIELD_VISIT: "產地參訪" };
const PRICING_LABELS = { PER_PERSON: "每人", PER_WEIGHT: "每斤" };
const SCOPES = [
  { key: "all", label: "全部農場" },
  { key: "products", label: "有商品" },
  { key: "groupBuys", label: "有團購" },
  { key: "trips", label: "有體驗" },
  { key: "blogs", label: "有日記" }
];

/* ---------- 共用 fetch 與資料處理（不依賴元件 this） ---------- */
async function fetchPage(path) {
  try {
    const page = await NongAuth.request(path);
    if (Array.isArray(page)) return page;
    return Array.isArray(page?.content) ? page.content : [];
  } catch (e) {
    console.warn(path, e.message);
    return [];
  }
}
async function fetchOne(path) {
  try {
    return await NongAuth.request(path);
  } catch (e) {
    console.warn(path, e.message);
    return null;
  }
}
function cleanImage(value) {
  const v = String(value || "").trim();
  return v ? v : "";
}
function resolveFarmVisual(producer) {
  const name = String(producer.farmName || producer.name || "");
  const matched = FARM_VISUALS.find((entry) => entry.names.some((candidate) => name.includes(candidate)));
  return matched ? matched.url : "";
}
function unique(values) {
  return [...new Set(values.map((v) => String(v || "").trim()).filter(Boolean))];
}
function truncate(value, max) {
  const text = String(value || "").replace(/\s+/g, " ").trim();
  return text.length > max ? text.slice(0, max) + "..." : text;
}
function formatDate(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return `${d.getMonth() + 1}/${d.getDate()}`;
}
function currency(v) {
  return `NT$ ${Number(v || 0).toLocaleString("zh-TW")}`;
}
function enrichProducer(producer) {
  const productImages = producer.products.map((p) => p.imageUrl);
  const groupImages = producer.groupBuys.map((g) => g.productImageUrl);
  const tripImages = producer.trips.map((t) => t.imageUrl);
  const blogImages = producer.blogs.map((b) => b.coverImageUrl);
  const images = [...productImages, ...tripImages, ...groupImages, ...blogImages].map(cleanImage).filter(Boolean);
  const origins = unique([
    producer.farmAddress,
    ...producer.products.map((p) => p.origin),
    ...producer.trips.map((t) => t.location)
  ]);
  const tags = unique([
    producer.farmAddress,
    ...producer.products.map((p) => p.categoryName),
    ...producer.trips.map((t) => TRIP_TYPE_LABELS[t.tripType]),
    ...producer.blogs.map((b) => b.blogTypeName)
  ]).slice(0, 5);
  const introSource =
    producer.farmDesc ||
    producer.products.find((p) => p.description)?.description ||
    producer.trips.find((t) => t.intro)?.intro ||
    producer.blogs.find((b) => b.content)?.content ||
    "";
  const score = producer.products.length * 4 + producer.groupBuys.length * 3 + producer.trips.length * 3 + producer.blogs.length;
  const farmVisual = resolveFarmVisual(producer);
  return {
    ...producer,
    logoUrl: farmVisual,
    heroImage: farmVisual || images[0] || PRODUCER_FALLBACKS[producer.id % PRODUCER_FALLBACKS.length],
    gallery: unique([farmVisual, ...images]).slice(0, 4),
    origins,
    tags,
    intro: truncate(introSource, 150),
    score
  };
}

/* ========== 清單頁 App ========== */
const listApp = {
  data() {
    return {
      scopes: SCOPES,
      activeScope: "all",
      keyword: "",
      raw: { farmers: [], products: [], groupBuys: [], trips: [], blogs: [] },
      producers: [],
      loadState: "loading"
    };
  },
  computed: {
    filteredProducers() {
      const keyword = this.keyword.trim().toLowerCase();
      return this.producers.filter((producer) => {
        const scopeOk = this.activeScope === "all" || producer[this.activeScope].length > 0;
        if (!scopeOk) return false;
        if (!keyword) return true;
        const haystack = [
          producer.name,
          producer.intro,
          ...producer.origins,
          ...producer.tags,
          ...producer.products.map((p) => p.name),
          ...producer.groupBuys.map((g) => g.productName),
          ...producer.trips.map((t) => t.title),
          ...producer.blogs.map((b) => b.title)
        ].join(" ").toLowerCase();
        return haystack.includes(keyword);
      });
    }
  },
  mounted() {
    this.readListQuery();
    this.loadData();
  },
  methods: {
    readListQuery() {
      const q = new URLSearchParams(location.search);
      const scope = q.get("scope");
      this.activeScope = SCOPES.some((s) => s.key === scope) ? scope : "all";
      this.keyword = q.get("keyword") || "";
    },
    async loadData() {
      this.loadState = "loading";
      const params = new URLSearchParams({ size: "100", sort: "createdAt,desc" });
      if (this.keyword) params.set("keyword", this.keyword);
      const [farmers, products, groupBuys, trips, blogs] = await Promise.all([
        fetchPage("/api/public/farmers?" + params.toString()),
        fetchPage("/api/public/products?size=100&sort=createdAt,desc"),
        fetchPage("/api/group-buys?size=100&sort=createdAt,desc"),
        fetchPage("/api/farm-trips?size=100&sort=createdAt,desc"),
        fetchPage("/api/blogs?size=100&sort=createdAt,desc")
      ]);
      this.raw = { farmers, products, groupBuys, trips, blogs: blogs.filter((b) => b.authorType === "FARMER") };
      this.producers = this.buildProducers();
      this.loadState = "ready";
    },
    buildProducers() {
      const map = new Map();
      this.raw.farmers.forEach((farmer) => {
        if (farmer.id == null) return;
        map.set(farmer.id, {
          id: farmer.id,
          name: farmer.farmName || "未命名農場",
          farmName: farmer.farmName,
          farmAddress: farmer.farmAddress,
          farmDesc: farmer.farmDesc,
          phone: farmer.phone,
          locLat: farmer.locLat,
          locLong: farmer.locLong,
          products: [], groupBuys: [], trips: [], blogs: []
        });
      });
      const addItem = (id, name, bucket, item) => {
        if (id == null || !map.has(id)) return;
        const producer = map.get(id);
        if (!producer.name && name) producer.name = name;
        producer[bucket].push(item);
      };
      this.raw.products.forEach((item) => addItem(item.farmerId, item.farmerName, "products", item));
      this.raw.groupBuys.forEach((item) => addItem(item.farmerId, item.farmerName, "groupBuys", item));
      this.raw.trips.forEach((item) => addItem(item.farmerId, item.farmerName, "trips", item));
      this.raw.blogs.forEach((item) => addItem(item.authorId, item.authorName, "blogs", item));
      return Array.from(map.values())
        .map(enrichProducer)
        .sort((a, b) => b.score - a.score || a.name.localeCompare(b.name, "zh-Hant"));
    },
    selectScope(key) {
      this.activeScope = key || "all";
      this.syncListQuery();
    },
    submitSearch() {
      this.keyword = this.keyword.trim();
      this.syncListQuery();
      this.loadData();
    },
    syncListQuery() {
      const params = new URLSearchParams();
      if (this.activeScope !== "all") params.set("scope", this.activeScope);
      if (this.keyword) params.set("keyword", this.keyword);
      const next = params.toString();
      history.replaceState(null, "", next ? `?${next}` : location.pathname);
    },
    contentCount(p) {
      return p.products.length + p.groupBuys.length + p.trips.length + p.blogs.length;
    }
  }
};

/* ========== 農場介紹頁 App ========== */
const detailApp = {
  data() {
    return {
      producer: null,
      loadState: "loading", // loading / ready / error:訊息
      tripTypeLabels: TRIP_TYPE_LABELS,
      pricingLabels: PRICING_LABELS
    };
  },
  computed: {
    heroGallery() {
      if (!this.producer) return [];
      return (this.producer.gallery.length ? this.producer.gallery : [this.producer.heroImage]).slice(0, 4);
    },
    contactFacts() {
      if (!this.producer) return [];
      return [
        this.producer.farmAddress ? { label: "地址", value: this.producer.farmAddress } : null,
        this.producer.phone ? { label: "電話", value: this.producer.phone } : null
      ].filter(Boolean);
    }
  },
  mounted() {
    this.loadDetail();
  },
  methods: {
    async loadDetail() {
      const id = Number(new URLSearchParams(location.search).get("id"));
      if (!id) { this.loadState = "error:找不到農場 id"; return; }
      const [farmer, products, groupBuys, trips, blogs] = await Promise.all([
        fetchOne(`/api/public/farmers/${id}`),
        fetchPage(`/api/public/farmers/${id}/products?size=24&sort=createdAt,desc`),
        fetchPage(`/api/public/farmers/${id}/group-buys?size=12&sort=deadlineDate,asc`),
        fetchPage(`/api/public/farmers/${id}/farm-trips?size=12&sort=createdAt,desc`),
        fetchPage(`/api/public/farmers/${id}/blogs?size=12&sort=createdAt,desc`)
      ]);
      if (!farmer) { this.loadState = "error:目前沒有這個農場的公開內容"; return; }
      this.producer = enrichProducer({
        id: farmer.id,
        name: farmer.farmName || "未命名農場",
        farmName: farmer.farmName,
        farmAddress: farmer.farmAddress,
        farmDesc: farmer.farmDesc,
        phone: farmer.phone,
        locLat: farmer.locLat,
        locLong: farmer.locLong,
        products, groupBuys, trips, blogs
      });
      this.loadState = "ready";
    },
    productImage(p) { return cleanImage(p.imageUrl) || PRODUCT_FALLBACK; },
    groupImage(g) { return cleanImage(g.productImageUrl) || PRODUCT_FALLBACK; },
    tripImage(t) { return cleanImage(t.imageUrl) || PRODUCER_FALLBACKS[t.id % PRODUCER_FALLBACKS.length]; },
    tripUnit(t) { return PRICING_LABELS[t.pricingMode] || "每人"; },
    truncate, formatDate, currency,
    errorText(state) { return state.startsWith("error:") ? state.slice(6) : ""; }
  }
};

/* ---------- 依頁面掛載對應 App ---------- */
const mountEl = document.getElementById("app");
if (mountEl) {
  const kind = mountEl.dataset.pageKind;
  createApp(kind === "detail" ? detailApp : listApp).mount("#app");
}
