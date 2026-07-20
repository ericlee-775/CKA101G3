package com.farmily.notification.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.farmily.notification.dto.NotificationAnnouncementResponseDTO;
import com.farmily.notification.model.NotifAnnouncementAudience;
import com.farmily.notification.model.NotificationAnnouncementVO;
import com.farmily.notification.service.NotificationService;
import com.farmily.user.security.AdminUserDetails;

// 管理員後台通知中心 (Thymeleaf), 可發送系統通知

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationViewController {
	
	private final NotificationService nSvc;
	
	public AdminNotificationViewController(NotificationService nSvc) {
		this.nSvc = nSvc;

	}


	// 取得該管理員通知列表
	// Page<NotificationAnnouncementResponseDTO> getAnnouncement(int page)
	@GetMapping
	public String listAllNotif(@RequestParam(defaultValue = "0") int page, ModelMap model){
		Page<NotificationAnnouncementResponseDTO> notifs = nSvc.getAnnouncement(page);
		model.addAttribute("notifList", notifs);
		
		return "back-end/admin/notifications";
	}
	
	
	// 管理員發系統公告
	// announcement(NotifAnnouncementAudience audience, String message, Integer adminId)
	@PostMapping
	public String sendAnnouncement(
			@AuthenticationPrincipal AdminUserDetails admin,
			@RequestParam String message,
			@RequestParam NotifAnnouncementAudience audience,
			RedirectAttributes ra) {
		
		if (message == null || message.isBlank()) {
			ra.addFlashAttribute("error", "請輸入公告內容");
			return "redirect:/admin/notifications";
		}
		
		if (message.trim().length() > 500) {
			ra.addFlashAttribute("error", "公告內容不可超過 500 字");
			ra.addFlashAttribute("prevMessage", message);
			return "redirect:/admin/notifications";
		}
		
		nSvc.announcement(audience, message.trim(), admin.getAdmin());
		ra.addFlashAttribute("success", "(公告已發送)");
		
		return "redirect:/admin/notifications";
	}
	

}
