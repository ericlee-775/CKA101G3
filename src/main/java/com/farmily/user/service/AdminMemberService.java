package com.farmily.user.service;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.model.User;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

// 管理員對會員操作
public interface AdminMemberService {

    // 列出所有會員
    List<UserProfileResponse> listAll();

    // 複合查詢 + 分頁：keyword 比對 email / 姓名 / 電話（可空）；status 比對會員狀態（可空）
    Page<UserProfileResponse> search(String keyword, String status, int page, int size);

    // 查單一會員
    UserProfileResponse getById(Integer userId);

    // 改會員狀態(警告/停權/恢復)
    UserProfileResponse updateStatus(Integer userId, String status);
    
    // 取得指定狀態的會員 id (發送系統公告用)
    Set<Integer> getIdsByStatuses(List<User.UserStatus> statuses);

    // 篩選查詢會員條件: 消費級距、狀態（皆可複選；null 或空清單 = 不限）
//    List<UserProfileResponse> list(List<String> tierNames, List<String> statuses);
}
