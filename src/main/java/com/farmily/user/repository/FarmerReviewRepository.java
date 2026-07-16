package com.farmily.user.repository;

import com.farmily.user.model.FarmerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 管理員對小農審核永續層操作
public interface FarmerReviewRepository extends JpaRepository<FarmerReview, Integer> {

    // 查某小農「最新一筆」審核
    FarmerReview findTopByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 查某小農「所有」審核紀錄
    List<FarmerReview> findByFarmer_FarmerIdOrderByReviewRoundDesc (Integer farmerId);

    // 依狀態列出（待審清單，submittedAt 舊到新）
    List<FarmerReview> findByReviewStatusOrderBySubmittedAtAsc(FarmerReview.ReviewStatus status);

    // 審核歷史複合查詢 + 分頁（原生 SQL）：狀態集合(可只選 APPROVED 或 REJECTED)、
    // 關鍵字比對農場名(當前 farm_name 或送審快照 submitted_farm_name)或 Email、審核時間(reviewed_at)區間。
    // keyword / from / to 皆可為 null（null 代表不套用該條件）；排序與分頁由 Pageable 帶入
    @Query(value = """
            SELECT r.* FROM farmer_review r
            JOIN FARMER f ON f.farmer_id = r.farmer_id
            WHERE r.review_status IN (:statuses)
              AND (:keyword IS NULL
                   OR f.farm_name LIKE CONCAT('%', :keyword, '%')
                   OR f.email LIKE CONCAT('%', :keyword, '%')
                   OR r.submitted_farm_name LIKE CONCAT('%', :keyword, '%'))
              AND (:from IS NULL OR r.reviewed_at >= :from)
              AND (:to IS NULL OR r.reviewed_at <= :to)
            """,
            countQuery = """
            SELECT COUNT(*) FROM farmer_review r
            JOIN FARMER f ON f.farmer_id = r.farmer_id
            WHERE r.review_status IN (:statuses)
              AND (:keyword IS NULL
                   OR f.farm_name LIKE CONCAT('%', :keyword, '%')
                   OR f.email LIKE CONCAT('%', :keyword, '%')
                   OR r.submitted_farm_name LIKE CONCAT('%', :keyword, '%'))
              AND (:from IS NULL OR r.reviewed_at >= :from)
              AND (:to IS NULL OR r.reviewed_at <= :to)
            """,
            nativeQuery = true)
    Page<FarmerReview> searchHistory(@Param("statuses") List<String> statuses,
                                     @Param("keyword") String keyword,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to,
                                     Pageable pageable);

    // 查出所有審核的輕量欄位，用於列出所有小農時避免逐筆查最新審核 (N+1)
    @Query(value =
            "SELECT farmer_id, review_status, review_round FROM farmer_review",
            nativeQuery = true)
    List<Object[]> findAllReviewStatusRounds();

    // 查某小農「最新一筆」+「通過審核」的資料
//    FarmerReview findTopByFarmer_FarmerIdAndReviewStatusOrderByReviewRoundDesc(
//            Integer farmerId, FarmerReview.ReviewStatus status); // 傳 APPROVED

}
