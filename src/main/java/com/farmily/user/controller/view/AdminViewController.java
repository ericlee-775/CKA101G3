package com.farmily.user.controller.view;

import com.farmily.user.dto.AdminSelfUpdateRequest;
import com.farmily.user.dto.ChangePasswordRequest;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminService;
import com.farmily.user.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 管理後台頁面（Thymeleaf）
@Controller
public class AdminViewController {

    private final AdminService adminService;
    private final SessionService sessionService;

    public AdminViewController(AdminService adminService, SessionService sessionService) {
        this.adminService = adminService;
        this.sessionService = sessionService;
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

    // 個人資料頁（任何登入的管理員都能進）；資料直接由 AdminService 撈
    @GetMapping("/admin/profile")
    public String profile(@AuthenticationPrincipal AdminUserDetails me, ModelMap model) {
        model.addAttribute("profile", adminService.getMyProfile(me.getAdminId()));
        return "back-end/admin/profile";
    }

    // 送出修改（只改名字）；th:action 會自動帶 CSRF
    @PostMapping("/admin/profile")
    public String updateProfile(@AuthenticationPrincipal AdminUserDetails me,
                                @Valid AdminSelfUpdateRequest req, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", buildErrorMessage(br));
            return "redirect:/admin/profile";
        }
        adminService.updateMyProfile(me.getAdminId(), req);
        ra.addFlashAttribute("success", "（已更新個人資料）");
        return "redirect:/admin/profile";           // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 修改自己密碼
    @PostMapping("/admin/profile/password")
    public String changePassword(@AuthenticationPrincipal AdminUserDetails me,
                                 @Valid ChangePasswordRequest pw, BindingResult br,
                                 HttpServletRequest request, RedirectAttributes ra) {
        // 格式驗證失敗：帶錯誤訊息導回個人資料頁
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", buildErrorMessage(br));
            return "redirect:/admin/profile";
        }
        // 改密碼成功後由 service 寄出密碼變更通知信
        adminService.changeMyPassword(me.getAdminId(), pw);

        // 踢掉「其他裝置」的 session（保留自己這台）
        HttpSession session = request.getSession(false);        // 若有 session 回傳，沒有回傳 null (不建立)
        String currentSessionId = null;
        if (session != null) {
            currentSessionId = session.getId();
        }
        sessionService.expireSessions(me.getUsername(), currentSessionId);

        ra.addFlashAttribute("success", "（密碼已變更）");
        return "redirect:/admin/profile";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 把 Bean Validation 的欄位錯誤串成一句話，交給 layout 的 ${error} 提示顯示
    private String buildErrorMessage(BindingResult br) {
        StringBuilder sb = new StringBuilder();
        for (FieldError fe : br.getFieldErrors()) {
            if (sb.length() > 0) sb.append("；");
            sb.append(fe.getDefaultMessage());
        }
        return sb.length() > 0 ? sb.toString() : "輸入格式不符";
    }
}