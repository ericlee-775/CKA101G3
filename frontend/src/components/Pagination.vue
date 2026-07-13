<script setup>
// 通用分頁號碼列。純畫面元件，不打 API、不管資料——上層用 usePagination() 切好頁碼陣列後，
// 只需要把 page / totalPages 傳進來，點擊時透過 v-model:page 改變上層的頁碼。
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
})
const emit = defineEmits(['update:page'])

function go(p) {
  emit('update:page', p)
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pager" aria-label="分頁">
    <button
      type="button"
      class="pager-btn"
      :disabled="page <= 1"
      @click="go(page - 1)"
    >
      ‹ 上一頁
    </button>

    <button
      v-for="p in totalPages"
      :key="p"
      type="button"
      class="pager-num"
      :class="{ 'pager-num--active': p === page }"
      @click="go(p)"
    >
      {{ p }}
    </button>

    <button
      type="button"
      class="pager-btn"
      :disabled="page >= totalPages"
      @click="go(page + 1)"
    >
      下一頁 ›
    </button>
  </nav>
</template>

<style scoped>
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 20px;
}
.pager-btn,
.pager-num {
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink-soft);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.pager-num {
  min-width: 34px;
  text-align: center;
}
.pager-btn:hover:not(:disabled),
.pager-num:hover {
  border-color: var(--leaf);
  color: var(--leaf-dark);
}
.pager-btn:disabled {
  opacity: 0.5;
  cursor: default;
}
.pager-num--active {
  background: var(--leaf);
  border-color: var(--leaf);
  color: #fff;
}
</style>
