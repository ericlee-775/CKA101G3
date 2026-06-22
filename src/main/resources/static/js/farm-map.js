/*
  farm-map.html — 產地地圖（Vue 改寫版）
  - GET /api/public/farmers?size=100
  - GET /api/public/products?size=200
  Leaflet 仍直接操作 #map 節點；農場清單與詳情卡改由 Vue 反應式渲染。
*/

const { createApp } = Vue;

const FARM_DEFAULT_IMAGE = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=900&q=80";

function hasCoordinate(value) {
  return value !== null && value !== undefined && String(value).trim() !== "" && Number.isFinite(Number(value));
}
function resolveFarmImage(farmer, products) {
  const product = products.find((item) => Number(item.farmerId) === Number(farmer.id) && item.imageUrl);
  return product ? product.imageUrl : FARM_DEFAULT_IMAGE;
}

createApp({
  data() {
    return {
      farms: [],
      selectedFarm: null
    };
  },
  mounted() {
    // Leaflet 實例不放進 data（避免被反應式代理）
    this.map = null;
    this.markers = new Map();
    this.farmIcon = L.divIcon({
      className: "farm-marker-shell",
      html: `
        <div style="width:42px;height:42px;display:grid;place-items:center;border:3px solid #4f9368;border-radius:50%;background:#fff;color:#4f9368;box-shadow:0 4px 12px rgba(0,0,0,.28);">
          <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
            <g fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M7 15h18l-2.3 9.5a3 3 0 0 1-2.9 2.3h-7.6a3 3 0 0 1-2.9-2.3L7 15Z"/>
              <path d="M10 15c.8-3.9 3-6 6-6s5.2 2.1 6 6"/>
              <path d="M16 9V4"/>
              <path d="M16 8c-3.1-.2-5.3-1.7-6.5-4.3"/>
              <path d="M16 8c3.1-.2 5.3-1.7 6.5-4.3"/>
              <path d="M11 20h10"/>
            </g>
          </svg>
        </div>`,
      iconSize: [42, 42],
      iconAnchor: [21, 21],
      popupAnchor: [0, -24]
    });

    this.map = L.map("map", {
      center: [23.7, 121],
      zoom: 8.7,
      minZoom: 7,
      maxBounds: [[21.7, 119.0], [25.5, 122.8]]
    });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors"
    }).addTo(this.map);

    this.init();
  },
  methods: {
    async init() {
      this.farms = await this.loadFarms();
      this.renderMarkers();
    },
    async loadFarms() {
      const farmerPage = await NongAuth.request("/api/public/farmers?size=100");
      const productPage = await NongAuth.request("/api/public/products?size=200");
      const farmers = Array.isArray(farmerPage.content) ? farmerPage.content : farmerPage;
      const products = Array.isArray(productPage.content) ? productPage.content : productPage;
      return farmers
        .filter((farmer) => hasCoordinate(farmer.locLat) && hasCoordinate(farmer.locLong))
        .map((farmer) => ({
          id: farmer.id,
          name: farmer.farmName || "未命名農場",
          subtitle: farmer.farmAddress || "台灣在地小農",
          description: farmer.farmDesc || "這位小農尚未填寫介紹。",
          lat: Number(farmer.locLat),
          lng: Number(farmer.locLong),
          image: resolveFarmImage(farmer, products),
          link: `producer-detail.html?id=${farmer.id}`
        }));
    },
    renderMarkers() {
      this.farms.forEach((farm) => {
        const marker = L.marker([farm.lat, farm.lng], { icon: this.farmIcon })
          .addTo(this.map)
          .bindPopup(farm.name);
        marker.on("click", () => this.selectFarm(farm));
        this.markers.set(farm.id, marker);
      });
    },
    selectFarm(farm) {
      this.selectedFarm = farm;
      this.map.flyTo([farm.lat, farm.lng], 11);
      const marker = this.markers.get(farm.id);
      if (marker) marker.openPopup();
    },
    closeDetail() {
      this.selectedFarm = null;
    }
  }
}).mount("#app");
