<script setup>
import { computed, watch, onUnmounted } from 'vue'

/*
  使用條款 / 隱私權政策彈窗。
  用法：
    <TermsModal v-model="showTerms" type="member" @agree="agreed = true" />
  - type: 'member'（會員）或 'farmer'（小農），決定顯示哪一份服務條款
  - @agree：使用者按下「我已閱讀並同意」時觸發，方便呼叫端自動勾選同意框
  條款文案集中在本檔，方便日後統一維護。
*/
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  type: { type: String, default: 'member' }, // 'member' | 'farmer'
})
const emit = defineEmits(['update:modelValue', 'agree'])

function close() {
  emit('update:modelValue', false)
}

// 按下「我已閱讀並同意」：通知呼叫端勾選同意，並關閉彈窗
function agreeAndClose() {
  emit('agree')
  close()
}

// 生效日期（顯示於條款開頭）
const effectiveDate = '2026 年 7 月 22 日'

// ===== 會員服務條款 =====
const memberTerms = {
  title: '會員服務條款',
  intro:
    '歡迎加入 Farmily 小農市集（以下簡稱「本平台」）。當您完成註冊或使用本平台服務，即表示您已閱讀、瞭解並同意接受本服務條款之全部內容。',
  sections: [
    {
      heading: '一、服務內容',
      items: [
        '本平台提供產地直送的農產品購物、揪團購物、農遊體驗活動報名及相關內容瀏覽等服務。',
        '本平台得因營運需要，隨時新增、修改或終止部分或全部服務，並於平台上公告，恕不另行個別通知。',
      ],
    },
    {
      heading: '二、帳號註冊與資格',
      items: [
        '您應提供正確、最新且完整的個人資料，並於資料變更時即時更新，以維護自身權益。',
        '每一電子信箱僅能註冊一個會員帳號；本平台得對以不實資料或重複註冊之帳號予以停權或刪除。',
        '完成註冊後，您將收到啟用驗證信，需點擊信中連結完成 Email 驗證後始能登入使用完整服務。',
      ],
    },
    {
      heading: '三、帳號安全與會員義務',
      items: [
        '您應妥善保管帳號與密碼，不得將帳號提供、轉讓或出借予他人使用。',
        '凡使用您帳號及密碼進行之行為，均視為您本人之行為，並由您負一切法律責任。',
        '如發現帳號遭他人冒用或有任何安全疑慮，應立即通知本平台並協助處理。',
        '您不得利用本平台從事任何違反法令、公共秩序、善良風俗或侵害他人權益之行為。',
      ],
    },
    {
      heading: '四、訂單與交易',
      items: [
        '商品頁面所示之價格、規格與供應狀況，以您下單當時平台顯示者為準。',
        '訂單成立後，退換貨、退款等事宜依《消費者保護法》及本平台公告之退換貨規範辦理。',
        '本平台為小農與消費者之交易媒合服務，商品品質與出貨由各小農負責，本平台將協助處理相關爭議。',
      ],
    },
    {
      heading: '五、智慧財產權',
      items: [
        '本平台上之商標、文字、圖片、介面設計及其他內容，其智慧財產權均屬本平台或合法權利人所有。',
        '未經事前書面同意，您不得以任何方式重製、散布、公開傳輸或作商業使用。',
      ],
    },
    {
      heading: '六、責任限制',
      items: [
        '本平台將盡合理努力維持服務正常運作，但不保證服務不會中斷或全無錯誤。',
        '因不可抗力、系統維護或第三方因素導致之服務中斷或資料損失，本平台於法令允許範圍內不負賠償責任。',
      ],
    },
    {
      heading: '七、條款修改',
      items: [
        '本平台得隨時修改本服務條款，並於平台上公告；修改後若您繼續使用服務，視為同意修改後之條款。',
      ],
    },
    {
      heading: '八、準據法與管轄',
      items: [
        '本服務條款之解釋與適用，以及與本平台服務有關之爭議，均以中華民國法律為準據法，並以臺灣臺北地方法院為第一審管轄法院。',
      ],
    },
  ],
}

// ===== 小農合作條款 =====
const farmerTerms = {
  title: '小農合作條款',
  intro:
    '感謝您申請成為 Farmily 小農市集（以下簡稱「本平台」）的小農夥伴。當您送出申請或使用小農服務，即表示您已閱讀、瞭解並同意接受本合作條款之全部內容。',
  sections: [
    {
      heading: '一、申請資格',
      items: [
        '申請者須為實際從事農業生產或農產加工之個人或事業，並具備合法經營與販售之資格。',
        '每一小農帳號與一般會員帳號分開管理，僅供經營者本人使用。',
      ],
    },
    {
      heading: '二、資料與證明文件真實性',
      items: [
        '您須提供真實、正確且有效的農場資訊，並上傳農地證明、產品證明及身分證明等文件供本平台審核。',
        '本平台將對申請資料進行審核，審核期間或通過後如發現文件不實、偽造或冒用，本平台得駁回申請、下架商品或終止合作，情節重大者將依法追究責任。',
        '您上傳之證明文件僅供本平台審核與依法保存之用，本平台將依隱私權政策妥善保管。',
      ],
    },
    {
      heading: '三、商品上架規範',
      items: [
        '上架商品之名稱、產地、成分、價格及圖片等資訊須真實正確，不得有誇大不實或引人錯誤之標示。',
        '禁止上架法令禁止販售、侵害他人權益或未取得必要許可之商品。',
        '您須確保所售商品之品質、衛生與安全，並符合相關食品安全與標示法規。',
      ],
    },
    {
      heading: '四、交易、費用與撥款',
      items: [
        '本平台提供交易媒合與金流服務，並得就成交訂單收取約定之服務費用，實際費率以雙方約定或平台公告為準。',
        '貨款將於扣除相關費用後，依平台公告之結算週期撥付至您指定之帳戶。',
      ],
    },
    {
      heading: '五、出貨與售後服務',
      items: [
        '您須依訂單內容準時、正確地完成出貨，並妥善處理消費者之退換貨與客訴。',
        '因商品品質、出貨延誤或標示不實所生之爭議與賠償，由您自行負責，本平台得協助居中協調。',
      ],
    },
    {
      heading: '六、審核與帳號權益',
      items: [
        '小農帳號須通過本平台初審並完成 Email 驗證後始能上架與販售。',
        '審核未通過者將收到退件理由，您可補正資料後重新送審。',
      ],
    },
    {
      heading: '七、終止合作',
      items: [
        '如您違反本條款、相關法令或有損害消費者權益、平台商譽之情事，本平台得暫停或終止您的帳號與合作關係。',
        '合作終止後，未完成之訂單仍應依約履行，尚未撥付之合法貨款將於結算後給付。',
      ],
    },
    {
      heading: '八、準據法與管轄',
      items: [
        '本合作條款之解釋與適用，以及與小農服務有關之爭議，均以中華民國法律為準據法，並以臺灣臺北地方法院為第一審管轄法院。',
      ],
    },
  ],
}

// ===== 隱私權政策（會員與小農共用）=====
const privacyPolicy = {
  title: '隱私權政策',
  intro:
    '本平台重視您的個人資料保護，並依《個人資料保護法》及相關法令蒐集、處理及利用您的個人資料。',
  sections: [
    {
      heading: '一、蒐集之資料',
      items: [
        '註冊與使用服務時所提供之姓名、電子信箱、聯絡電話、地址等識別資料。',
        '小農申請另包含農場資訊及農地、產品、身分等證明文件。',
        '使用服務所產生之訂單紀錄、瀏覽紀錄及裝置與連線相關資訊。',
      ],
    },
    {
      heading: '二、利用目的與範圍',
      items: [
        '用於會員管理、身分驗證、訂單處理、客戶服務、交易撥款及依法令所需之用途。',
        '在您同意或法令許可之範圍內，用於服務優化、活動通知與行銷資訊之提供。',
      ],
    },
    {
      heading: '三、資料之保護與保存',
      items: [
        '本平台採取合理之技術與管理措施保護您的個人資料，防止未經授權之存取、竄改或洩漏。',
        '個人資料於達成蒐集目的所需期間或依法令規定之期間內保存，逾期將予刪除或匿名化處理。',
      ],
    },
    {
      heading: '四、當事人權利',
      items: [
        '您得依法就本平台保有之個人資料，請求查詢、閱覽、製給複本、補正、停止蒐集處理利用或刪除。',
        '如您欲行使上述權利，可透過平台客服管道提出申請。',
      ],
    },
    {
      heading: '五、第三方揭露',
      items: [
        '除經您同意、為完成交易所必要（如物流、金流服務商）或依法令要求外，本平台不會將您的個人資料提供予第三方。',
      ],
    },
  ],
}

const terms = computed(() => (props.type === 'farmer' ? farmerTerms : memberTerms))

// 彈窗開啟時鎖住背景捲動、監聽 Esc 關閉
function onKeydown(e) {
  if (props.modelValue && e.key === 'Escape') close()
}
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      window.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
    } else {
      window.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
    }
  }
)
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <!-- Teleport 到 body：避免被 header 的 sticky / z-index 蓋住 -->
  <Teleport to="body">
    <Transition name="tm-fade">
      <div v-if="modelValue" class="tm-overlay" @click.self="close">
        <div class="tm-card" role="dialog" aria-modal="true" aria-labelledby="tm-title">
          <header class="tm-head">
            <h3 id="tm-title" class="tm-title">{{ terms.title }}</h3>
            <button type="button" class="tm-close" aria-label="關閉" @click="close">×</button>
          </header>

          <div class="tm-body">
            <p class="tm-date">生效日期：{{ effectiveDate }}</p>
            <p class="tm-intro">{{ terms.intro }}</p>

            <section v-for="sec in terms.sections" :key="sec.heading" class="tm-sec">
              <h4 class="tm-sec-title">{{ sec.heading }}</h4>
              <ol class="tm-list">
                <li v-for="(item, i) in sec.items" :key="i">{{ item }}</li>
              </ol>
            </section>

            <!-- 隱私權政策（共用）-->
            <hr class="tm-divider" />
            <h3 class="tm-sub-title">{{ privacyPolicy.title }}</h3>
            <p class="tm-intro">{{ privacyPolicy.intro }}</p>
            <section v-for="sec in privacyPolicy.sections" :key="sec.heading" class="tm-sec">
              <h4 class="tm-sec-title">{{ sec.heading }}</h4>
              <ol class="tm-list">
                <li v-for="(item, i) in sec.items" :key="i">{{ item }}</li>
              </ol>
            </section>
          </div>

          <footer class="tm-foot">
            <button type="button" class="tm-btn tm-ghost" @click="close">關閉</button>
            <button type="button" class="tm-btn tm-agree" @click="agreeAndClose">
              我已閱讀並同意
            </button>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.tm-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(30, 30, 25, 0.45);
}
.tm-card {
  width: 100%;
  max-width: 640px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 18px 48px rgba(30, 25, 15, 0.28);
  overflow: hidden;
}
.tm-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid var(--line, #e3d8c4);
}
.tm-title {
  margin: 0;
  font-size: 19px;
  color: var(--ink, #2c3a2e);
}
.tm-close {
  border: none;
  background: transparent;
  font-size: 26px;
  line-height: 1;
  color: var(--muted, #7d8a78);
  cursor: pointer;
  padding: 0 4px;
}
.tm-close:hover {
  color: var(--ink, #2c3a2e);
}

.tm-body {
  padding: 20px 22px;
  overflow-y: auto;
  color: var(--ink-soft, #4a564c);
  font-size: 14px;
  line-height: 1.75;
}
.tm-date {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--muted, #7d8a78);
}
.tm-intro {
  margin: 0 0 16px;
}
.tm-sec {
  margin-bottom: 14px;
}
.tm-sec-title {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--ink, #2c3a2e);
}
.tm-list {
  margin: 0;
  padding-left: 22px;
}
.tm-list li {
  margin-bottom: 4px;
}
.tm-divider {
  margin: 22px 0;
  border: none;
  border-top: 1px dashed var(--line, #e3d8c4);
}
.tm-sub-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--ink, #2c3a2e);
}

.tm-foot {
  display: flex;
  gap: 10px;
  padding: 14px 22px;
  border-top: 1px solid var(--line, #e3d8c4);
}
.tm-btn {
  flex: 1;
  padding: 11px 16px;
  border-radius: 10px;
  font-size: 15px;
  font-family: inherit;
  cursor: pointer;
  border: 1px solid transparent;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}
.tm-ghost {
  flex: 0 0 auto;
  min-width: 96px;
  background: #fff;
  border-color: var(--line, #e3d8c4);
  color: var(--ink-soft, #4a564c);
}
.tm-ghost:hover {
  border-color: var(--muted, #7d8a78);
}
.tm-agree {
  background: var(--leaf, #3b8a57);
  color: #fff;
}
.tm-agree:hover {
  background: var(--leaf-dark, #2f6e46);
}

/* 淡入 + 卡片微縮放的過場 */
.tm-fade-enter-active,
.tm-fade-leave-active {
  transition: opacity 0.18s ease;
}
.tm-fade-enter-from,
.tm-fade-leave-to {
  opacity: 0;
}
.tm-fade-enter-active .tm-card,
.tm-fade-leave-active .tm-card {
  transition: transform 0.18s ease;
}
.tm-fade-enter-from .tm-card,
.tm-fade-leave-to .tm-card {
  transform: scale(0.96);
}

@media (max-width: 560px) {
  .tm-card {
    max-height: 90vh;
  }
  .tm-foot {
    flex-direction: column-reverse;
  }
  .tm-ghost {
    width: 100%;
  }
}
</style>