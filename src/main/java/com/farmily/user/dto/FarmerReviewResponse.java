package com.farmily.user.dto;

import com.farmily.user.model.Admin;
import com.farmily.user.model.Farmer;
import com.farmily.user.model.FarmerReview;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 回傳給管理員看的審核資料包成 dto、給小農看審核紀錄遮蔽管理員資訊
public class FarmerReviewResponse {

    private Integer reviewId;
    private Integer farmerId;
    private String farmName;
    private String farmerEmail;
    private Integer reviewRound;
    private Integer adminId;               // 認領（負責）此案件的管理員 id
    private String adminName;
    private String adminEmail;
    private String reviewStatus;          // PENDING / REVIEWING / APPROVED / REJECTED
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectReason;
    private String notes;                 // 管理員核准時的內部備註（僅後台可見，maskAdminInfo 會遮蔽）
    private String submittedFarmName;     // 本輪提交快照
    private String submittedFarmAddress;
    private String submittedCityName;
    private String submittedDistName;
    private BigDecimal submittedLocLat;
    private BigDecimal submittedLocLong;
    private Boolean hasCertLand;          // 文件只標「有沒有上傳」
    private Boolean hasCertProduct;
    private Boolean hasCertIdentity;
    private Boolean emailVerified;        // 小農是否已完成 Email 驗證（查詢進度頁判斷是否顯示「重寄啟用信」）
    private String farmerStatus;          // PENDING / ACTIVE / SUSPENDED

    // getter
    public Integer getReviewId() {
        return reviewId;
    }
    public Integer getFarmerId() {
        return farmerId;
    }
    public String getFarmName() {
        return farmName;
    }
    public String getFarmerEmail() {
        return farmerEmail;
    }
    public Integer getReviewRound() {
        return reviewRound;
    }
    public Integer getAdminId() {
        return adminId;
    }
    public String getAdminName() {
        return adminName;
    }
    public String getAdminEmail() {
        return adminEmail;
    }
    public String getReviewStatus() {
        return reviewStatus;
    }

    // 給未啟用小農審核狀態 Reviewing 設定仍為 Pending (前端顯示用)
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    public String getRejectReason() {
        return rejectReason;
    }
    public String getNotes() {
        return notes;
    }
    public String getSubmittedFarmName() {
        return submittedFarmName;
    }
    public String getSubmittedFarmAddress() {
        return submittedFarmAddress;
    }
    public String getSubmittedCityName() {
        return submittedCityName;
    }
    public String getSubmittedDistName() {
        return submittedDistName;
    }
    public BigDecimal getSubmittedLocLat() {
        return submittedLocLat;
    }
    public BigDecimal getSubmittedLocLong() {
        return submittedLocLong;
    }
    public Boolean getHasCertLand() {
        return hasCertLand;
    }
    public Boolean getHasCertProduct() {
        return hasCertProduct;
    }
    public Boolean getHasCertIdentity() {
        return hasCertIdentity;
    }
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public String getFarmerStatus() {
        return farmerStatus;
    }
    public void setFarmerStatus(String farmerStatus) {
        this.farmerStatus = farmerStatus;
    }

    // 遮蔽承辦管理員資訊：小農端查看自己的審核紀錄時，不得得知是哪位管理員審核（避免外洩）
    // notes 為管理員內部備註，同樣不得外洩給小農
    public void maskAdminInfo() {
        this.adminId = null;
        this.adminName = null;
        this.adminEmail = null;
        this.notes = null;
    }

    public static FarmerReviewResponse from(FarmerReview r){
        FarmerReviewResponse dto = new FarmerReviewResponse();
        dto.reviewId = r.getReviewId();

        Farmer farmer = r.getFarmer();
        if (farmer != null) {
            dto.farmerId = farmer.getFarmerId();
            dto.farmerEmail = farmer.getEmail();
            dto.farmName = farmer.getFarmName();
        }

        dto.reviewRound = r.getReviewRound();

        Admin admin = r.getAdmin();
        if(admin != null){
            dto.adminId = admin.getAdminId();
            dto.adminName = admin.getAdminName();
            dto.adminEmail = admin.getAdminEmail();
        }

        dto.reviewStatus = r.getReviewStatus() != null ? r.getReviewStatus().name() : null;
        dto.submittedAt = r.getSubmittedAt();
        dto.reviewedAt = r.getReviewedAt();
        dto.rejectReason = r.getRejectReason();
        dto.notes = r.getNotes();
        dto.submittedFarmName = r.getSubmittedFarmName();
        dto.submittedFarmAddress = r.getSubmittedFarmAddress();
        if (r.getSubmittedDistrict() != null) {
            dto.submittedCityName = r.getSubmittedDistrict().getCityName();
            dto.submittedDistName = r.getSubmittedDistrict().getDistName();
        }
        dto.submittedLocLat = r.getSubmittedLocLat();
        dto.submittedLocLong = r.getSubmittedLocLong();
        dto.hasCertLand = r.getCertFileLand() != null;
        dto.hasCertProduct = r.getCertFileProduct() != null;
        dto.hasCertIdentity = r.getCertFileIdentity() != null;
        return dto;
    }
}
