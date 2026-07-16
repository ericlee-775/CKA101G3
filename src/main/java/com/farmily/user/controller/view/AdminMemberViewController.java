package com.farmily.user.controller.view;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.service.AdminMemberService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 會員管理（Thymeleaf）
@Controller
@RequestMapping("/admin/members")
public class AdminMemberViewController {

    private static final int PAGE_SIZE = 10;

    private final AdminMemberService adminMemberService;

    public AdminMemberViewController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    // 查所有會員（複合查詢 + 分頁）
    @GetMapping
    public String listAll(@RequestParam(value = "kw", required = false) String kw,
                          @RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          ModelMap model) {
        Page<UserProfileResponse> members = adminMemberService.search(kw, status, page, PAGE_SIZE);
        model.addAttribute("members", members);
        model.addAttribute("kw", kw);       // 回填查詢條件供表單與分頁連結沿用
        model.addAttribute("status", status);
        return "back-end/admin/listAllMember";
    }

    // 查單一會員
    @GetMapping("/{userId}")
    public String detail(@PathVariable Integer userId, ModelMap model) {
        model.addAttribute("member", adminMemberService.getById(userId));
        return "back-end/admin/memberDetail";
    }

    // 修改: 變更會員狀態 (POST)
    @PostMapping("/{userId}/status")
    public String updateStatus(@PathVariable Integer userId,
                               @RequestParam("status") String status,
                               RedirectAttributes ra) {
        adminMemberService.updateStatus(userId, status);

        ra.addFlashAttribute("success", "（會員狀態已更新）");
        return "redirect:/admin/members";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }
}