package com.farmily.user.controller.view;

import com.farmily.user.dto.AdminCreateRequest;
import com.farmily.user.dto.AdminUpdateRequest;
import com.farmily.user.dto.PermissionResponse;
import com.farmily.user.security.AdminUserDetails;
import com.farmily.user.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
    public String create(@Valid AdminCreateRequest req, BindingResult br, RedirectAttributes ra) {
        // 格式驗證失敗：帶錯誤訊息導回清單頁，並用網址片段自動打開「新增管理員」彈窗
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", buildErrorMessage(br));
            return "redirect:/admin/admins#addAdminModal";
        }
        adminService.createAdmin(req);
        ra.addFlashAttribute("success", "（已新增管理員）");
        return "redirect:/admin/admins";            // PRG (POST-Redirect-GET)：操作完導回清單頁，避免重新整理時重送表單
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
    public String update(@PathVariable Integer adminId, @Valid AdminUpdateRequest req,
                         BindingResult br, RedirectAttributes ra) {
        // 格式驗證失敗：帶錯誤訊息導回該管理員的修改頁
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", buildErrorMessage(br));
            return "redirect:/admin/admins/" + adminId + "/edit";
        }
        adminService.updateAdmin(adminId, req);
        ra.addFlashAttribute("success", "（已修改管理員）");
        return "redirect:/admin/admins";        // PRG (POST-Redirect-GET)：操作完導回清單頁，避免重新整理時重送表單
    }

    // CRUD - 刪除管理員（軟刪除），帶目前登入者 id 以擋「刪自己」
    @PostMapping("/{adminId}/delete")
    public String delete(@PathVariable Integer adminId,
                         @AuthenticationPrincipal AdminUserDetails me, RedirectAttributes ra) {
        adminService.deleteAdmin(adminId, me.getAdminId());
        ra.addFlashAttribute("success", "（已刪除管理員）");
        return "redirect:/admin/admins";        // PRG (POST-Redirect-GET)：操作完導回清單頁，避免重新整理時重送表單
    }

    // 列出可指派權限清單
    @ModelAttribute("permissionListData")
    public List<PermissionResponse> permissionListData() {
        return adminService.listPermissions();
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