package com.farmily.notification.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.farmily.notification.dto.NotificationResponseDTO;
import com.farmily.notification.model.NotificationRecipientType;
import com.farmily.notification.service.NotificationService;
import com.farmily.user.security.FarmerUserDetails;

@RestController
@RequestMapping("/api/famer/notifications")
public class FarmerNotificationController {
	
	@Autowired
	private NotificationService nSvc;
	
	
	// 取得該用戶全部通知，(分頁，未讀放前面)
	// 收 用戶 Id
	// 收 query 參數 targetType 判斷是否分類
	// 收 page 做分頁排序
	@GetMapping
	public ResponseEntity<Page<NotificationResponseDTO>> getMyNotif(
			@AuthenticationPrincipal FarmerUserDetails user, 
			@RequestParam(required = false) String targetType, 
			@RequestParam(defaultValue = "0") int page){
		
		// 取得用戶身分、Id
		NotificationRecipientType recipientType = NotificationRecipientType.farmer;
		Integer recipientId = user.getFarmerId();
		
		// 判斷是否需要分類篩選通知
		// targetType: ACCOUNT, PRODUCT, ORDER, GROUPBUY, TRIP, BLOG,...
		// targetType = null, 不分類, 顯示全部
		Page<NotificationResponseDTO> notifList = null;
		if (targetType == null) {
			notifList = nSvc.getNotifByRecipient(recipientType, recipientId, page);
			
		}
		// targetType != null, 分類顯示
		else {
			notifList = nSvc.getNotifBytarget(recipientType, recipientId, targetType, page);
		}
		
		return ResponseEntity.ok(notifList);
	}

	
	// 已讀鈕
	@PatchMapping("/{notifId}/read")
	public ResponseEntity<Void> updateStatus(@PathVariable Integer notifId) {
		// markOneAsRead(Integer notificationId)
		nSvc.markOneAsRead(notifId);
		return ResponseEntity.ok().build();
		
	}
	
		
	// 全部已讀鈕
	@PatchMapping("/read-all")
	public ResponseEntity<Void> updatAllStatus(@AuthenticationPrincipal FarmerUserDetails user) {
		// markAllAsRead(NotificationRecipientType recipientType, Integer recipientId)
		// 取得用戶身分、用戶 Id
		NotificationRecipientType recipientType = NotificationRecipientType.farmer;
		Integer recipientId = user.getFarmerId();
		nSvc.markAllAsRead(recipientType, recipientId);
	
		return ResponseEntity.ok().build();
	}
	
	
	// ============== 通知小鈴鐺 ==============
	// 取得小鈴鐺預覽最新通知列表
	// getNotifForPreview(NotificationRecipientType recipientType, Integer recipientId)
	@GetMapping("/preview")
	public ResponseEntity<List<NotificationResponseDTO>> getNotifPreview(@AuthenticationPrincipal FarmerUserDetails user){
		// 取得用戶身分、用戶 Id
		NotificationRecipientType recipientType = NotificationRecipientType.farmer;
		Integer recipientId = user.getFarmerId();
		
		List<NotificationResponseDTO> list = nSvc.getNotifForPreview(recipientType, recipientId);
		
		return ResponseEntity.ok(list);
	}
	
	
	
	// 取得小鈴鐺上紅點未讀數字 (count)
	// countUnread(NotificationRecipientType recipientType, Integer recipientId)
	@GetMapping("/unread-count")
	public ResponseEntity<Long> getNotifUnreadCount(@AuthenticationPrincipal FarmerUserDetails user) {
		// 取得用戶身分、用戶 Id
		NotificationRecipientType recipientType = NotificationRecipientType.farmer;
		Integer recipientId = user.getFarmerId();
		
		long count = nSvc.countUnread(recipientType, recipientId);
		
		return ResponseEntity.ok(count);
	}
	

}
