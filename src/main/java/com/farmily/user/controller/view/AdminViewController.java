package com.farmily.user.controller.view;

import com.farmily.user.dto.AdminSelfUpdateRequest;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

// 管理後台頁面（Thymeleaf）：登入頁、總覽頁、個人資料頁
@Controller
public class AdminViewController {

    private final AdminService adminService;

    public AdminViewController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 登入頁（GET）；POST /admin/login 由 Spring Security 的 formLogin 處理，不用自己寫
    @GetMapping("/admin/login")
    public String loginPage() {
        return "back-end/admin/login";
    }

    // 總覽頁（登入後才進得來）
    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        model.addAttribute("adminName", me.getAdmin().getAdminName());
        return "back-end/admin/dashboard";
    }

    // 個人資料頁（任何登入的管理員都能進）；資料直接由 AdminService 撈，走 Thymeleaf 表單、不經 REST
    @GetMapping("/admin/profile")
    public String profile(@AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        model.addAttribute("profile", adminService.getMyProfile(me.getAdminId()));
        return "back-end/admin/profile";
    }

    // 送出修改（只改名字）；th:action 會自動帶 CSRF
    @PostMapping("/admin/profile")
    public String updateProfile(@AuthenticationPrincipal AdminUserDetails me,
                                AdminSelfUpdateRequest req, ModelMap model) {
        adminService.updateMyProfile(me.getAdminId(), req);
        model.addAttribute("profile", adminService.getMyProfile(me.getAdminId()));
        model.addAttribute("success", "（已更新個人資料）");
        return "back-end/admin/profile";
    }
}