package com.farmily.user.service.impl;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.exception.BusinessException;
import com.farmily.user.exception.UserNotFoundException;
import com.farmily.user.model.SpendingTier;
import com.farmily.user.model.User;
import com.farmily.user.repository.SpendingTierRepository;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.AdminMemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// 管理員管理一般會員
@Service
@Transactional
public class AdminMemberServiceImpl implements AdminMemberService {

    private final UserRepository userRepository;
    private final SpendingTierRepository spendingTierRepository;

    public AdminMemberServiceImpl(UserRepository userRepository, SpendingTierRepository spendingTierRepository) {
        this.userRepository = userRepository;
        this.spendingTierRepository = spendingTierRepository;
    }

    // 列出所有會員
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAll() {
        List<User> users = userRepository.findAll();

        // 消費級距表只查 1 次，迴圈內在記憶體比對，避免每筆會員都查一次 DB (n+1)
        List<SpendingTier> tiers = spendingTierRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User u : users) {
            BigDecimal amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : BigDecimal.ZERO;
            String tierName = resolveTierName(tiers, amount);

            result.add(UserProfileResponse.from(u, tierName));
        }
        return result;
    }

    // 複合查詢 + 分頁：keyword 比對 email / 姓名 / 電話；status 比對會員狀態；消費級距於分頁後在記憶體補上
    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> search(String keyword, String status, int page, int size) {
        // 預設依會員編號由小到大排序
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.ASC, "user_id"));
        Page<User> users = userRepository.searchMembers(blankToNull(keyword), blankToNull(status), pageable);

        // 消費級距表只查 1 次，於本頁會員在記憶體比對，避免 N+1
        List<SpendingTier> tiers = spendingTierRepository.findAll();
        return users.map(u -> {
            BigDecimal amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : BigDecimal.ZERO;
            return UserProfileResponse.from(u, resolveTierName(tiers, amount));
        });
    }

    // 空白字串視為 null（不套用該條件）
    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // 在記憶體用金額比對出級距名稱（找不到回傳 null）
    private String resolveTierName(List<SpendingTier> tiers, BigDecimal amount) {
        for (SpendingTier tier : tiers) {
            // Int 轉型成 BigDecimal
            BigDecimal minAmount = BigDecimal.valueOf(tier.getMinAmount());

            // 判斷會員消費金額 (amount) 是否 >= 級距下限 (minAmount)
            boolean aboveMin = amount.compareTo(minAmount) >= 0;       // amount 大於 minAmount，回傳正數

            boolean belowMax = true;
            if (tier.getMaxAmount() != null) {
                BigDecimal maxAmount = BigDecimal.valueOf(tier.getMaxAmount());
                // 判斷：會員消費金額 (amount) 是否 <= 級距上限 (maxAmount)
                belowMax = amount.compareTo(maxAmount) <= 0;            // amount 小於 maxAmount，回傳負數
            }
            // 如果金額剛好落在這個級距區間內，就回傳這個級距的名稱
            if (aboveMin && belowMax) {
                return tier.getTierName();
            }
        }
        return null;
    }

    // 查單一會員
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
//                .orElseThrow(UserNotFoundException::new);

        // +消費級距
        BigDecimal amount = user.getMonthlySpending() != null ? user.getMonthlySpending() : BigDecimal.ZERO;
        String tierName = spendingTierRepository.findTierNameByAmount(amount);

        return UserProfileResponse.from(user, tierName);
    }

    // 改狀態：字串轉 enum
    @Override
    public UserProfileResponse updateStatus(Integer userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
//                .orElseThrow(UserNotFoundException::new);

        User.UserStatus newStatus;
        try {
            newStatus = User.UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("UNSUPPORTED_MEMBER_STATUS", HttpStatus.BAD_REQUEST, "不支援的會員狀態: " + status);
        }
        user.setUserStatus(newStatus);
        return UserProfileResponse.from(userRepository.save(user));
    }




    // 依條件篩選會員：消費級距、狀態（皆可複選；null 或空清單 = 不限）
//    @Override
//    @Transactional(readOnly = true)
//    public List<UserProfileResponse> list(List<String> tierNames, List<String> statuses) {
//        List<User> users = userRepository.findAll();
//
//        // 消費級距對照表只查 1 次，迴圈內在記憶體比對，避免每筆會員都查一次 DB
//        List<SpendingTier> tiers = spendingTierRepository.findAll();
//        List<UserProfileResponse> result = new ArrayList<>();
//
//        for (User u : users) {
//            BigDecimal amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : BigDecimal.ZERO;
//            String userTier = resolveTierName(tiers, amount);
//
//            // 條件 1：消費級距（有勾選才比對；級距不在勾選清單就跳過這筆）
//            if (tierNames != null && !tierNames.isEmpty() && !tierNames.contains(userTier)) {
//                continue;
//            }
//
//            // 條件 2：會員狀態（有勾選才比對）
//            if (statuses != null && !statuses.isEmpty()) {
//                String userStatus = u.getUserStatus() != null ? u.getUserStatus().name() : null;
//                if (!statuses.contains(userStatus)) {
//                    continue;
//                }
//            }
//
//            // 兩個條件都通過，才加進結果
//            result.add(UserProfileResponse.from(u, userTier));
//        }
//        return result;
//    }

}
