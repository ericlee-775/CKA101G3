package com.farmily.user.service.impl;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.event.FarmerApprovedEvent;
import com.farmily.user.event.FarmerRejectedEvent;
import com.farmily.user.model.Admin;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;
import com.farmily.user.repository.AdminRepository;
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.FarmerReviewRepository;
import com.farmily.user.service.AdminReviewService;
import com.farmily.user.service.EmailVerificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminReviewServiceImpl implements AdminReviewService {

    private final FarmerRepository farmerRepository;
    private final AdminRepository adminRepository;
    private final FarmerReviewRepository farmerReviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminReviewServiceImpl(FarmerRepository farmerRepository,
                                  AdminRepository adminRepository,
                                  FarmerReviewRepository farmerReviewRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.farmerRepository = farmerRepository;
        this.adminRepository = adminRepository;
        this.farmerReviewRepository = farmerReviewRepository;
        this.eventPublisher = eventPublisher;
    }

    // 待審清單
    @Override
    @Transactional(readOnly = true)
    public List<FarmerReviewResponse> listPending() {
        // 撈出所有待審 (PENDING)
        List<FarmerReview> reviews = farmerReviewRepository.findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus.PENDING);

        // 準備一個空清單，逐筆把 FarmerReview 轉成回應用的 FarmerReviewResponse
        List<FarmerReviewResponse> result = new ArrayList<>();
        for (FarmerReview review : reviews) {
            result.add(FarmerReviewResponse.from(review)); // 轉成 DTO 後加進結果清單
        }
        return result;
    }

    // 某小農的審核紀錄
    @Override
    public List<FarmerReviewResponse> listByFarmer(Integer farmerId) {
        List<FarmerReview> reviews = farmerReviewRepository.findByFarmer_FarmerIdOrderByReviewRoundDesc(farmerId);

        List<FarmerReviewResponse> result = new ArrayList<>();
        for (FarmerReview review : reviews) {
            result.add(FarmerReviewResponse.from(review));
        }
        return result;
    }

    // 列出審核中清單
    @Override
    public List<FarmerReviewResponse> listReviewing() {
        List<FarmerReview> reviews = farmerReviewRepository.findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus.REVIEWING);

        List<FarmerReviewResponse> result = new ArrayList<>();
        for(FarmerReview review : reviews){
            result.add(FarmerReviewResponse.from(review));
        }
        return result;
    }

    // 審核歷史：已核准 / 已退件（跨所有小農）複合查詢 + 分頁（reviewedAt 新到舊）
    // 顯示邏輯與 reviewHistory 一致：文件僅負責管理員可見，退件理由 / 備註所有管理員皆可見
    @Override
    @Transactional(readOnly = true)
    public Page<FarmerReviewResponse> listHistory(String keyword, String status, LocalDate from, LocalDate to,
                                                  int page, int size) {
        // 狀態篩選：指定 APPROVED / REJECTED 則只查該狀態，否則兩者皆列
        List<String> statuses;
        if (FarmerReview.ReviewStatus.APPROVED.name().equalsIgnoreCase(status)) {
            statuses = List.of(FarmerReview.ReviewStatus.APPROVED.name());
        } else if (FarmerReview.ReviewStatus.REJECTED.name().equalsIgnoreCase(status)) {
            statuses = List.of(FarmerReview.ReviewStatus.REJECTED.name());
        } else {
            statuses = List.of(FarmerReview.ReviewStatus.APPROVED.name(),
                               FarmerReview.ReviewStatus.REJECTED.name());
        }

        // 預設依審核編號由小到大排序（原生查詢排序需用資料表欄位名 review_id）
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.ASC, "review_id"));

        return farmerReviewRepository.searchHistory(
                statuses, blankToNull(keyword), startOfDay(from), endOfDay(to), pageable
        ).map(FarmerReviewResponse::from);
    }

    // 空白字串視為 null（不套用關鍵字條件）
    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // 日期轉當日起訖時間（含整日）；null 則不套用
    private static LocalDateTime startOfDay(LocalDate d) {
        return d == null ? null : d.atStartOfDay();
    }
    private static LocalDateTime endOfDay(LocalDate d) {
        return d == null ? null : d.atTime(LocalTime.MAX);
    }

    // 開始審核 PENDING 案件 - REVIEWING
    @Override
    public FarmerReviewResponse reviewing(Integer reviewId, Integer adminId) {
        FarmerReview review = findReview(reviewId);
        if(review.getReviewStatus() != FarmerReview.ReviewStatus.PENDING){
            throw new IllegalStateException("此案件審核中或已審核");
        }
        review.setReviewStatus(FarmerReview.ReviewStatus.REVIEWING);
        review.setAdmin(findAdmin(adminId));

        return FarmerReviewResponse.from(farmerReviewRepository.save(review));
    }

    // 核准過審：把這一輪 submitted_XXX 寫回 Farmer，並啟用帳號
    // notes：管理員內部備註（選填），僅後台可見，不會外洩給小農
    @Override
    public FarmerReviewResponse approve(Integer reviewId, Integer adminId, String notes) {
        FarmerReview review = findReview(reviewId);
        ensureReviewing(review);       // 若 APPROVED/REJECTED 就擋下

        if (review.getAdmin() == null || !review.getAdmin().getAdminId().equals(adminId)) {
            throw new AccessDeniedException("此案件由其他管理員認領，您無法審核");
        }

        // 更新到 Farmer 核准的資料
        Farmer farmer = review.getFarmer();
        farmer.setFarmName(review.getSubmittedFarmName());
        farmer.setFarmAddress(review.getSubmittedFarmAddress());
        farmer.setCityDistrict(review.getSubmittedDistrict());
        farmer.setLocLat(review.getSubmittedLocLat());
        farmer.setLocLong(review.getSubmittedLocLong());
        farmer.setFarmerStatus(Farmer.FarmerStatus.ACTIVE);   // 啟用之後能登入
        farmerRepository.save(farmer);

        // 同步更新 FarmerReview 核准的資料
        review.setReviewStatus(FarmerReview.ReviewStatus.APPROVED);
        review.setReviewedAt(LocalDateTime.now());
        review.setAdmin(findAdmin(adminId));                  // 記錄是誰審的
        // 管理員內部備註（選填）：null/空白時存 null，避免存入空字串
        review.setNotes((notes != null && !notes.isBlank()) ? notes.trim() : null);

        FarmerReviewResponse result = FarmerReviewResponse.from(farmerReviewRepository.save(review));

        // 核准後才寄出「啟用 + Email 驗證」信：小農須點連結完成驗證才能登入
        // 帳號雖已設為 ACTIVE，但 email_verified 仍為 false，未點連結前無法自行登入
        // 發布事件監聽，交易成功 commit 後才執行寄啟用信
        eventPublisher.publishEvent(
                new FarmerApprovedEvent(farmer.getEmail()));

        return result;
    }

    // 退件重審
    @Override
    public FarmerReviewResponse reject(Integer reviewId, Integer adminId, String rejectReason) {
        FarmerReview review = findReview(reviewId);
        ensureReviewing(review);
        if (review.getAdmin() == null || !review.getAdmin().getAdminId().equals(adminId)) {
            throw new AccessDeniedException("此案件由其他管理員認領，您無法審核");
        }

        // 同步更新 FarmerReview 拒絕的資料
        review.setReviewStatus(FarmerReview.ReviewStatus.REJECTED);
        review.setReviewedAt(LocalDateTime.now());
        review.setRejectReason(rejectReason);
        review.setAdmin(findAdmin(adminId));

        FarmerReviewResponse result = FarmerReviewResponse.from(farmerReviewRepository.save(review));

        // 退件後寄出通知信（附退件理由 + 重新送審頁連結）；@Async 寄信失敗不影響退件本身
        // 發布事件監聽，交易成功 commit 後才執行寄退件信
        eventPublisher.publishEvent(
                new FarmerRejectedEvent(review.getFarmer().getEmail(), rejectReason));

        return result;
    }


    // 取某輪審核的證明文件 bytes（給管理員預覽 / 下載）
    // 僅負責（認領/審核）此案件的管理員才能取檔，讓「小農管理詳情」與「小農審核」兩處存取權一致
    @Override
    @Transactional(readOnly = true)
    public byte[] getCertFile(Integer reviewId, String type, Integer adminId) {
        FarmerReview review = findReview(reviewId);

        // adminId 來自已登入管理員，保證非 null，放在 equals 前面避免 NPE
        Admin owner = review.getAdmin();
        if (owner == null || !adminId.equals(owner.getAdminId())) {
            throw new AccessDeniedException("此案件由其他管理員負責，您無法檢視文件");
        }

        String t = (type == null) ? "" : type.toLowerCase();
        byte[] bytes;
        if ("land".equals(t)) {
            bytes = review.getCertFileLand();
        } else if ("product".equals(t)) {
            bytes = review.getCertFileProduct();
        } else if ("identity".equals(t)) {
            bytes = review.getCertFileIdentity();
        } else {
            throw new IllegalArgumentException("不支援的文件類型: " + type);
        }

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("此文件未上傳");
        }
        return bytes;
    }


    // ---- 自訂方法 ----
    private FarmerReview findReview(Integer reviewId) {
        return farmerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("查無此審核案件"));
    }
    private Admin findAdmin(Integer adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("查無此管理員"));
    }

    // 確保一定要先 REVIEWING (除了 REVIEWING，擋住其他審核狀態)
    private void ensureReviewing(FarmerReview review) {
        FarmerReview.ReviewStatus s = review.getReviewStatus();
        if (s != FarmerReview.ReviewStatus.REVIEWING) {
            throw new IllegalStateException("請先認領案件才能進行審核");
        }
    }
}
