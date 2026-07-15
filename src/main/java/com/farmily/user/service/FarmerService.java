package com.farmily.user.service;

import com.farmily.user.dto.*;
import java.util.List;

public interface FarmerService {

    // 本地註冊
    FarmerProfileResponse register(FarmerRegisterRequest reg);

    // 本地登入
    FarmerProfileResponse login(LoginRequest log);

    // 查個人資料
    FarmerProfileResponse getMyProfile(Integer farmerId);

    // 修改不用重審資料
    FarmerProfileResponse updateContactInfo(Integer farmerId, FarmerProfileUpdateRequest req);

    // 修改需要重審資料
    FarmerProfileResponse updateReviewRequiredInfo(Integer farmerId, FarmerResubmitRequest req);

    // 修改密碼
    void changePassword(Integer farmerId, ChangePasswordRequest pw);

    // 查自己所有審核輪次紀錄（含每輪文件有無；已遮蔽承辦管理員資訊）
    List<FarmerReviewResponse> listMyReviews(Integer farmerId);

    // 取自己某輪審核的證明文件 bytes；僅本人可取（非本人的 reviewId 一律拒絕）
    byte[] getMyCertFile(Integer farmerId, Integer reviewId, String type);

}
