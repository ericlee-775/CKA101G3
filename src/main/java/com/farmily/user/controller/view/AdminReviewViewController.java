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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 管理後台小農審核頁（Thymeleaf）
@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewViewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewViewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    // 查所有「待審 PENDING」與「審核中 Reviewing」案件清單
    @GetMapping
    public String list(@AuthenticationPrincipal AdminUserDetails me,
                       ModelMap model) {
        model.addAttribute("pendingData", adminReviewService.listPending());
        model.addAttribute("reviewingData", adminReviewService.listReviewing());
        model.addAttribute("currentAdminId", me.getAdminId());
        return "back-end/admin/listReview";
    }

    // 查某小農所有審核紀錄
    @GetMapping("/farmer/{farmerId}")
    public String historyByFarmer(@PathVariable Integer farmerId,
                                  @AuthenticationPrincipal AdminUserDetails me,
                                  ModelMap model) {
        List<FarmerReviewResponse> reviews = adminReviewService.listByFarmer(farmerId);

        model.addAttribute("reviews", reviews);
        model.addAttribute("farmerId", farmerId);
        model.addAttribute("currentAdminId", me.getAdminId());  // 文件僅負責此案件的管理員可見
        if (!reviews.isEmpty()) {
            model.addAttribute("farmName", reviews.get(0).getFarmName());
            model.addAttribute("farmerEmail", reviews.get(0).getFarmerEmail());
        }
        return "back-end/admin/reviewHistory";
    }

    // 修改: 開始審核（PENDING -> REVIEWING）
    @PostMapping("/{reviewId}/start")
    public String start(@PathVariable Integer reviewId,
                        @AuthenticationPrincipal AdminUserDetails me,
                        RedirectAttributes ra) {
        adminReviewService.reviewing(reviewId, me.getAdminId());
        ra.addFlashAttribute("success", "（已認領審核）");
        return "redirect:/admin/reviews";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 修改: 核准（APPROVED）
    @PostMapping("/{reviewId}/approve")
    public String approve(@PathVariable Integer reviewId,
                          @AuthenticationPrincipal AdminUserDetails me,
                          RedirectAttributes ra) {
        adminReviewService.approve(reviewId, me.getAdminId());
        ra.addFlashAttribute("success", "（已核准）");
        return "redirect:/admin/reviews";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 修改: 退件（REJECTED，需填理由）
    @PostMapping("/{reviewId}/reject")
    public String reject(@PathVariable Integer reviewId,
                         @RequestParam("rejectReason") String rejectReason,
                         @AuthenticationPrincipal AdminUserDetails me,
                         RedirectAttributes ra) {
        adminReviewService.reject(reviewId, me.getAdminId(), rejectReason);
        ra.addFlashAttribute("success", "（已退件）");
        return "redirect:/admin/reviews";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 證明文件圖片（type = land | product | identity）；回圖片位元組，讓 <img> 直接內嵌
    @GetMapping("/{reviewId}/cert/{type}")
    @ResponseBody
    public ResponseEntity<byte[]> cert(@PathVariable Integer reviewId,
                                       @PathVariable String type,
                                       @AuthenticationPrincipal AdminUserDetails me) {
        byte[] bytes = adminReviewService.getCertFile(reviewId, type, me.getAdminId());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
}