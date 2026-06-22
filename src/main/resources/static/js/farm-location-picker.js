/**
 * 農場位置選點器 (Leaflet + OSM Nominatim,免 API key)
 *
 * 用法:
 *   const picker = FarmLocationPicker.create({
 *     mapId: "pickMap",
 *     latInputId: "locLat",
 *     lngInputId: "locLong",
 *     addressInputId: "farmAddress",
 *     locateBtnId: "locateBtn",
 *     coordReadoutId: "coordReadout",
 *     initialLat: null,           // 編輯頁可帶入既有座標
 *     initialLng: null,
 *     onOk:    (msg) => {...},     // 成功訊息回呼(交給頁面顯示)
 *     onError: (msg) => {...},     // 錯誤訊息回呼
 *   });
 *
 * 三種設定座標的方式都會匯流到 setCoord():按地址定位、點地圖、拖曳標記。
 */
(function (global) {
  "use strict";

  const TAIWAN_CENTER = [23.7, 121];

  // 把台灣地址由細到粗,產生多組查詢候選(Nominatim 門牌層級常查不到,逐層退讓)
  function buildAddressCandidates(addr) {
    const candidates = [addr];
    // 依序砍掉:號 -> 弄 -> 巷 -> 段 -> 路/街/大道,每砍一層就多一個候選
    const cutMarkers = [
      /[0-9０-９之\-]+號.*$/,                       // 門牌號(含之/樓等後綴)
      /[0-9０-９一二三四五六七八九十]+弄.*$/,
      /[0-9０-９一二三四五六七八九十]+巷.*$/,
      /[一二三四五六七八九十0-9０-９]+段.*$/,
      /[^縣市區鄉鎮]*?(路|街|大道).*$/              // 整條路名
    ];
    let cur = addr;
    for (const re of cutMarkers) {
      const next = cur.replace(re, "").trim();
      if (next && next !== cur && !candidates.includes(next)) {
        candidates.push(next);
        cur = next;
      }
    }
    return candidates;
  }

  async function geocode(query) {
    const url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&countrycodes=tw&q="
              + encodeURIComponent(query);
    const res = await fetch(url, { headers: { "Accept-Language": "zh-TW" } });
    return res.json();
  }

  function create(opts) {
    const onOk = opts.onOk || function () {};
    const onError = opts.onError || function () {};

    const latEl = document.getElementById(opts.latInputId);
    const lngEl = document.getElementById(opts.lngInputId);
    const addrEl = document.getElementById(opts.addressInputId);
    const locateBtn = document.getElementById(opts.locateBtnId);
    const coordReadout = opts.coordReadoutId ? document.getElementById(opts.coordReadoutId) : null;

    const map = L.map(opts.mapId).setView(TAIWAN_CENTER, 7);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors"
    }).addTo(map);

    let marker = null;

    function setCoord(lat, lng, fly) {
      const latNum = Number(lat), lngNum = Number(lng);
      latEl.value = latNum.toFixed(7);
      lngEl.value = lngNum.toFixed(7);
      if (coordReadout) coordReadout.textContent = `座標:${latNum.toFixed(5)}, ${lngNum.toFixed(5)}`;
      if (!marker) {
        marker = L.marker([latNum, lngNum], { draggable: true }).addTo(map);
        marker.on("dragend", () => {
          const p = marker.getLatLng();
          setCoord(p.lat, p.lng, false);
        });
      } else {
        marker.setLatLng([latNum, lngNum]);
      }
      if (fly) map.setView([latNum, lngNum], 15);
    }

    // 點地圖任一處 => 放/移動標記
    map.on("click", (e) => setCoord(e.latlng.lat, e.latlng.lng, false));

    // 依地址定位:逐層退讓查詢
    if (locateBtn) {
      locateBtn.addEventListener("click", async () => {
        const addr = (addrEl.value || "").trim();
        if (!addr) { onError("請先填寫農場地址再定位"); return; }
        const original = locateBtn.textContent;
        locateBtn.disabled = true; locateBtn.textContent = "定位中…";
        try {
          const candidates = buildAddressCandidates(addr);
          for (let i = 0; i < candidates.length; i++) {
            const data = await geocode(candidates[i]);
            if (data.length) {
              setCoord(data[0].lat, data[0].lon, true);
              if (i === 0) {
                onOk("已定位到地址位置,如需更精準請拖曳地圖上的標記。");
              } else {
                onOk(`找不到完整門牌,已定位到大概位置(${candidates[i]}),請拖曳標記到農場確切位置。`);
              }
              return;
            }
            // Nominatim 免費版限每秒 1 次,候選之間稍微間隔
            if (i < candidates.length - 1) await new Promise(r => setTimeout(r, 1100));
          }
          onError("找不到這個地址,請直接在地圖上點選農場位置。");
        } catch {
          onError("定位服務暫時無法使用,請手動點地圖標出位置");
        } finally {
          locateBtn.disabled = false; locateBtn.textContent = original;
        }
      });
    }

    // 帶入既有座標(編輯頁用)
    if (opts.initialLat != null && opts.initialLng != null
        && Number.isFinite(Number(opts.initialLat)) && Number.isFinite(Number(opts.initialLng))) {
      setCoord(opts.initialLat, opts.initialLng, true);
    }

    // Leaflet 在隱藏/動態容器初次渲染常需要 invalidateSize
    setTimeout(() => map.invalidateSize(), 200);

    return {
      map,
      setCoord,
      getCoord() {
        return { lat: latEl.value || null, lng: lngEl.value || null };
      }
    };
  }

  global.FarmLocationPicker = { create };
})(window);
