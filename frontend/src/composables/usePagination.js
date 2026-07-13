// 通用的前端分頁 composable：資料一次全部從 API 撈回來，這裡只負責切頁。
// 用法：const { page, totalPages, pageItems } = usePagination(computed(() => fullList.value), 10)
import { ref, computed, watch } from 'vue'

export function usePagination(sourceRef, pageSize = 10) {
  const page = ref(1)

  const totalPages = computed(() => Math.max(1, Math.ceil(sourceRef.value.length / pageSize)))

  const pageItems = computed(() => {
    const start = (page.value - 1) * pageSize
    return sourceRef.value.slice(start, start + pageSize)
  })

  // 來源清單變動（篩選頁籤切換、重新整理）時跳回第一頁，避免停在一個已經不存在的頁碼。
  watch(sourceRef, () => {
    page.value = 1
  })

  // 資料變少導致目前頁碼超出範圍時，退回最後一頁。
  watch(totalPages, (tp) => {
    if (page.value > tp) page.value = tp
  })

  return { page, totalPages, pageItems }
}

export default usePagination
