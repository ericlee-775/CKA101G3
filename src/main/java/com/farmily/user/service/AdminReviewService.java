package com.farmily.user.service;

import com.farmily.user.dto.FarmerReviewResponse;
import java.util.List;

// 管理員對小農審核操作
public interface AdminReviewService {

    // 列出待審清單
    List<FarmerReviewResponse> listPending();

    // 列出審核中清單
    List<FarmerReviewResponse> listReviewing();

    // 某小農所有審核紀錄
    List<FarmerReviewResponse> listByFarmer(Integer farmerId);

    // 審核中 REVIEWING
    FarmerReviewResponse reviewing(Integer reviewId, Integer adminId);

    // 核准 APPROVE（notes：管理員內部備註，選填，僅後台可見、不外洩給小農）
    FarmerReviewResponse approve(Integer reviewId, Integer adminId, String notes);

    // 退件 REJECTED
    FarmerReviewResponse reject(Integer reviewId, Integer adminId, String rejectReason);

    // 取某輪審核的證明文件原始位元組（type = land | product | identity）
    // 僅負責（認領/審核）此案件的管理員才能取檔，非負責者拋 AccessDeniedException
    byte[] getCertFile(Integer reviewId, String type, Integer adminId);

}
