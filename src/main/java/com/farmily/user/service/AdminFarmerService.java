package com.farmily.user.service;

import com.farmily.user.dto.FarmerProfileResponse;
import com.farmily.user.model.Farmer;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

// 管理員對小農操作
public interface AdminFarmerService {

    // 列出所有小農
    List<FarmerProfileResponse> listAll();

    // 複合查詢 + 分頁：keyword 比對農場名 / email / 電話（可空）；status 比對小農狀態（可空）
    Page<FarmerProfileResponse> search(String keyword, String status, int page, int size);

    // 查單一小農
    FarmerProfileResponse getById(Integer farmerId);

    // 改小農狀態
    FarmerProfileResponse suspend(Integer farmerId);     // ACTIVE → SUSPENDED

    FarmerProfileResponse reinstate(Integer farmerId);   // SUSPENDED → ACTIVE
    
    // 取得指定狀態的小農 id (發送系統公告用)
    Set<Integer> getIdsByStatuses(List<Farmer.FarmerStatus> statuses);
}
