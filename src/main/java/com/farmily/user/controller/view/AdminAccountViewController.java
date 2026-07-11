package com.farmily.user.controller.view;

import com.farmily.user.dto.AdminCreateRequest;
import com.farmily.user.dto.AdminUpdateRequest;
import com.farmily.user.dto.PermissionResponse;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 管理員對管理員 CRUD 頁（Thymeleaf）；整條 /admin/admins/** 需 PERM_ADMIN 由 Security 擋下
@Controller
@RequestMapping("/admin/admins")
public class AdminAccountViewController {

    private final AdminService adminService;

    public AdminAccountViewController(AdminService adminService) {
        this.adminService = adminService;
    }

    // CRUD - 新增管理員
    @PostMapping
    public String create(AdminCreateRequest req, RedirectAttributes ra) {
        adminService.createAdmin(req);
        ra.addFlashAttribute("success", "（已新增管理員）");
        return "redirect:/admin/admins";            // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // CRUD - 查全部管理員
    @GetMapping
    public String listAll(ModelMap model) {
        model.addAttribute("adminListData", adminService.listAll());
        return "back-end/admin/listAllAdmin";
    }

    // CRUD - 查單一管理員 / 顯示「修改頁」
    @GetMapping("/{adminId}/edit")
    public String editForm(@PathVariable Integer adminId, ModelMap model) {
        model.addAttribute("admin", adminService.getById(adminId));
        return "back-end/admin/updateAdminInput";
    }

    // CRUD - 修改管理員
    @PostMapping("/{adminId}/update")
    public String update(@PathVariable Integer adminId, AdminUpdateRequest req, RedirectAttributes ra) {
        adminService.updateAdmin(adminId, req);
        ra.addFlashAttribute("success", "（已修改管理員）");
        return "redirect:/admin/admins";        // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // CRUD - 刪除管理員（軟刪除），帶目前登入者 id 以擋「刪自己」
    @PostMapping("/{adminId}/delete")
    public String delete(@PathVariable Integer adminId,
                         @AuthenticationPrincipal AdminUserDetails me, RedirectAttributes ra) {
        adminService.deleteAdmin(adminId, me.getAdminId());
        ra.addFlashAttribute("success", "（已刪除管理員）");
        return "redirect:/admin/admins";        // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 列出可指派權限清單
    @ModelAttribute("permissionListData")
    public List<PermissionResponse> permissionListData() {
        return adminService.listPermissions();
    }
}