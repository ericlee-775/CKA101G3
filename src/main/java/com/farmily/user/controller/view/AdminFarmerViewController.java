package com.farmily.user.controller.view;


import com.farmily.user.service.AdminFarmerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 管理後台－小農管理頁（Thymeleaf）
@Controller
@RequestMapping("/admin/farmers")
public class AdminFarmerViewController {

    private final AdminFarmerService adminFarmerService;

    public AdminFarmerViewController(AdminFarmerService adminFarmerService) {
        this.adminFarmerService = adminFarmerService;
    }

    // 查所有小農
    @GetMapping
    public String listAll(ModelMap model) {
        model.addAttribute("farmerListData", adminFarmerService.listAll());
        return "back-end/admin/listAllFarmer";
    }

    // 查單一小農
    @GetMapping("/{farmerId}")
    public String detail(@PathVariable Integer farmerId, ModelMap model) {
        model.addAttribute("farmer", adminFarmerService.getById(farmerId));
        return "back-end/admin/farmerDetail";
    }

    // 停權（ACTIVE -> SUSPENDED）；POST-Redirect-GET 避免重整重送表單
    @PostMapping("/{farmerId}/suspend")
    public String suspend(@PathVariable Integer farmerId, RedirectAttributes ra) {
        adminFarmerService.suspend(farmerId);
        ra.addFlashAttribute("success", "（已停權）");
        return "redirect:/admin/farmers";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }

    // 恢復（SUSPENDED -> ACTIVE）
    @PostMapping("/{farmerId}/reinstate")
    public String reinstate(@PathVariable Integer farmerId, RedirectAttributes ra) {
        adminFarmerService.reinstate(farmerId);
        ra.addFlashAttribute("success", "（已恢復）");
        return "redirect:/admin/farmers";       // POST-Redirect-GET：操作完導回清單頁，避免重新整理時重送表單
    }
}