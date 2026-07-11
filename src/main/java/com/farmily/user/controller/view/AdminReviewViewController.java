package com.farmily.user.controller.view;

import com.farmily.user.dto.FarmerReviewResponse;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminReviewService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

// 管理後台－小農審核頁（Thymeleaf）
@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewViewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewViewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    // 查所有「待審 PENDING」與「審核中 Reviewing」案件清單
    @GetMapping
    public String list(ModelMap model) {
        return backToList(model, null);     // 自定義方法
    }

    // 查某小農所有審核紀錄
    @GetMapping("/farmer/{farmerId}")
    public String historyByFarmer(@PathVariable Integer farmerId, ModelMap model) {
        List<FarmerReviewResponse> reviews = adminReviewService.listByFarmer(farmerId);

        model.addAttribute("reviews", reviews);
        model.addAttribute("farmerId", farmerId);
        if (!reviews.isEmpty()) {
            model.addAttribute("farmName", reviews.get(0).getFarmName());
            model.addAttribute("farmerEmail", reviews.get(0).getFarmerEmail());
        }
        return "back-end/admin/reviewHistory";
    }

    // 修改: 開始審核（PENDING -> REVIEWING）
    @PostMapping("/{reviewId}/start")
    public String start(@PathVariable Integer reviewId,
                        @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        adminReviewService.reviewing(reviewId, me.getAdminId());
        return backToList(model, "（已開始審核）");
    }

    // 修改: 核准（APPROVED）
    @PostMapping("/{reviewId}/approve")
    public String approve(@PathVariable Integer reviewId,
                          @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        adminReviewService.approve(reviewId, me.getAdminId());
        return backToList(model, "（已核准）");
    }

    // 修改: 退件（REJECTED，需填理由）
    @PostMapping("/{reviewId}/reject")
    public String reject(@PathVariable Integer reviewId,
                         @RequestParam("rejectReason") String rejectReason,
                         @AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        adminReviewService.reject(reviewId, me.getAdminId(), rejectReason);
        return backToList(model, "（已退件）");
    }

    // 證明文件圖片（type = land | product | identity）；回圖片位元組，讓 <img> 直接內嵌
    @GetMapping("/{reviewId}/cert/{type}")
    @ResponseBody
    public ResponseEntity<byte[]> cert(@PathVariable Integer reviewId, @PathVariable String type) {
        byte[] bytes = adminReviewService.getCertFile(reviewId, type);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    // 私有小工具：合併查「待審 + 審核中」兩份清單，回審核頁
    private String backToList(ModelMap model, String success) {
        model.addAttribute("pendingData", adminReviewService.listPending());
        model.addAttribute("reviewingData", adminReviewService.listReviewing());
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "back-end/admin/listReview";
    }
}