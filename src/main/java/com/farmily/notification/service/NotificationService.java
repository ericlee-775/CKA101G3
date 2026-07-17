package com.farmily.notification.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.notification.dto.NotificationCreateRequestDTO;
import com.farmily.notification.dto.NotificationResponseDTO;
import com.farmily.notification.model.NotificationRecipientType;
import com.farmily.notification.model.NotificationRepository;
import com.farmily.notification.model.NotificationStatus;
import com.farmily.notification.model.NotificationTemplateRepository;
import com.farmily.notification.model.NotificationTemplateVO;
import com.farmily.notification.model.NotificationVO;

@Service
public class NotificationService {
	
	private static final int PAGE_SIZE = 5;
	
	@Autowired
	NotificationRepository repository;
	
	@Autowired
	NotificationTemplateRepository templateRepository;
	
	// add notification
//	@Transactional
	public void addNotif(NotificationVO notification) {
		repository.save(notification);
	}
	
	
	// ============== 更新已讀 (status) 狀態 ==============
	// mark as read, single 單筆已讀
	@Transactional
	public void markOneAsRead(Integer notificationId, NotificationRecipientType recipientType, Integer recipientId) {
		NotificationVO notification = repository.findById(notificationId)
				.orElseThrow(() -> new IllegalArgumentException("查無此通知"));
		
		// 檢查這則通知是否屬於此會員
		if (notification.getRecipientType() != recipientType || !notification.getRecipientId().equals(recipientId)){
			throw new AccessDeniedException("無權限操作此通知");
		}
		notification.setStatus(NotificationStatus.read);
		repository.save(notification);			
	}
	
	// mark as read, all 全部已讀
	@Transactional
	public int markAllAsRead(NotificationRecipientType recipientType, Integer recipientId) {
		return repository.updateAllStatus(NotificationStatus.read, recipientType, recipientId, NotificationStatus.unread);
	}
	
	
	// 全部已讀 for-each loop 版本, 查詢次數多, 不優
	/*
	public void markAllAsRead(NotificationRecipientType recipientType, Integer recipientId) {
		List<NotificationVO> list = getNotifByRecipient(recipientType, recipientId);
		for (NotificationVO notif : list) {
			if (notif.getStatus().equals(NotificationStatus.unread)) {
				notif.setStatus(NotificationStatus.read);
				repository.save(notif);
			}
		}
	}
	*/
	
	
	// ============== 發送通知 ==============
	// send notifications to single user
	// receive typeCode, recipientType, recipientId, targetType, targetId
	// 使用 DTO 打包參數的版本
	@Transactional
	public void sendOneNotif(NotificationCreateRequestDTO req) {
		NotificationVO notif = new NotificationVO();
		notif.setTypeCode(req.getTypeCode());
		notif.setRecipientType(req.getRecipientType());
		notif.setRecipientId(req.getRecipientId());
		notif.setTargetType(req.getTargetType());
		notif.setTargetId(req.getTargetId());
		
		NotificationTemplateVO template = templateRepository.findById(req.getTypeCode()).orElseThrow(() -> new IllegalArgumentException("type_code 錯誤"));
		String content = template.getTemplate();
		if (req.getVariables() != null) {
			for (Map.Entry<String, String> e : req.getVariables().entrySet()) {
				content = content.replace("{" + e.getKey() + "}", e.getValue());
			}
		}
		notif.setContent(content);
		repository.save(notif);
		
	}
	
	
	// 沒有使用 DTO 打包參數的版本
	/*
	public void sendNotif(String typeCode, NotificationRecipientType recipientType, Integer recipientId, String targetType, Integer targetId, Map<String, String> variables) {
		NotificationVO notification = new NotificationVO();
		notification.setTypeCode(typeCode);
		notification.setRecipientType(recipientType);
		notification.setRecipientId(recipientId);
		notification.setTargetType(targetType);
		notification.setTargetId(targetId);
		
		// use notification_template to create content
		NotificationTemplateVO template = templateRepository.findById(typeCode).orElseThrow(() -> new IllegalArgumentException("type_code 錯誤"));
		String content = template.getTemplate();  // 取得文字模板
		
		// 依照傳入的 key-value 替換模板中的 placeholder
		for(Map.Entry<String, String> e : variables.entrySet()) {
			content = content.replace("{" + e.getKey() + "}", e.getValue());  // {orderId} -> 1
		}
		
		notification.setContent(content);
		repository.save(notification);
	}
	*/
	
	
	// send a same notification to multiple/different users (same typeCode, content, targetType, targetId
	@Transactional
	public void sendMultipleNotif(Set<Integer> recipientId, NotificationRecipientType recipientType, String typeCode, String targetType, Integer targetId, Map<String, String> variables) {
		
		// build a list to store all notifications
		Set<NotificationVO> set = new HashSet<>();
		
		
		// get the content template (只需要查一次)
		NotificationTemplateVO template = templateRepository.findById(typeCode).orElseThrow(() -> new IllegalArgumentException("type_code 錯誤"));
		String content = template.getTemplate();
		if (variables != null) {
			for (Map.Entry<String, String> variable : variables.entrySet()) {
				content = content.replace("{" + variable.getKey() + "}", variable.getValue());
			}			
		}
		
		for (Integer id : recipientId) {
			NotificationVO notif = new NotificationVO();
			notif.setRecipientId(id);
			notif.setTypeCode(typeCode);
			notif.setRecipientType(recipientType);
			notif.setRecipientId(id);
			notif.setTargetType(targetType);
			notif.setTargetId(targetId);
			notif.setContent(content);
			set.add(notif);
		}
		
		// store the list, all notifications, to DB
		repository.saveAll(set);
	}
	
	
	// ============== 顯示通知列表 ==============
	// find notifications by member
	// 傳入 recipientType, recipientId, page
	@Transactional(readOnly = true)
	public Page<NotificationResponseDTO> getNotifByRecipient(NotificationRecipientType recipientType, Integer recipientId, int page){
		// pageable (起始頁, 每頁筆數, 排序: 未讀在前-日期先後)
		Pageable pageable = PageRequest.of(page, PAGE_SIZE);
		Page<NotificationVO> list = repository.findNotifs(recipientType, recipientId, pageable);
		Page<NotificationResponseDTO> dtoList = list.map(this::toDTO);
		return dtoList;
	}
	
	// find notifications by user id and targetType, 篩選使用者接收到的某一類通知
	// 傳入 recipientType, recipientId, targetType, page
	@Transactional(readOnly = true)
	public Page<NotificationResponseDTO> getNotifBytarget(NotificationRecipientType recipientType, Integer recipientId, String targetType, int page){
		Pageable pageable = PageRequest.of(page, PAGE_SIZE);
		Page<NotificationVO> list = repository.findnNotifsByTargetType(recipientType, recipientId, targetType, pageable);
		Page<NotificationResponseDTO> dtoList = list.map(this::toDTO);
		return dtoList;
	}
	
	
	// 把 Repository 查回來的 VO 轉成給 Controller 的 DTO
	private NotificationResponseDTO toDTO(NotificationVO vo) {
		NotificationResponseDTO dto = new NotificationResponseDTO();
		dto.setNotificationId(vo.getNotificationId());
		dto.setTargetType(vo.getTargetType());
		dto.setTargetId(vo.getTargetId());
		dto.setContent(vo.getContent());
		dto.setCreatedAt(vo.getCreatedAt());
		dto.setStatus(vo.getStatus().name()); // 把 enum 轉成 String
		
		return dto;
	}
	
	
	// ============== 通知小鈴鐺 ==============
	// 取得小鈴鐺預覽最新通知列表
	public List<NotificationResponseDTO> getNotifForPreview(NotificationRecipientType recipientType, Integer recipientId){
		List<NotificationVO> list = repository.findTop5ByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(recipientType, recipientId);
		List<NotificationResponseDTO> dtoList = list.stream().map(this::toDTO).toList();
		
		return dtoList;
	}
	
	// 取得未讀筆數
	public long countUnread(NotificationRecipientType recipientType, Integer recipientId) {
		long count = repository.countByRecipientTypeAndRecipientIdAndStatus(recipientType, recipientId, NotificationStatus.unread);
		
		return count;
	}
	

}
