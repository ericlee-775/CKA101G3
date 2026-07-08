package com.farmily.notification.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<NotificationVO, Integer> {
	
	// 取得該用戶所有通知，並分頁顯示
	public Page<NotificationVO> findByRecipientTypeAndRecipientId(NotificationRecipientType recipientType, Integer recipientId, Pageable pageable);
//	public List<NotificationVO> findByRecipientTypeAndRecipientId(NotificationRecipientType recipientType, Integer recipientId);

	
	// 取得該用戶所有通知，需要分類，並分頁顯示
	public Page<NotificationVO> findByRecipientTypeAndRecipientIdAndTargetType(NotificationRecipientType recipientType, Integer recipientId, String targetType, Pageable pageable);
	
//	@Query("FROM NotificationVO WHERE recipientType = ?1 AND recipientId = ?2 AND targetType = ?3")
//	public Page<NotificationVO> findByRecipientAndTarget(NotificationRecipientType recipientType, Integer recipientId, String targetType, Pageable pageable);
//	public List<NotificationVO> findByRecipientAndTarget(NotificationRecipientType recipientType, Integer recipientId, String targetType);


	// 取得小鈴鐺預覽通知，只需要前{五}筆，
	public List<NotificationVO> findTop5ByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(NotificationRecipientType recipientType, Integer recipientId);
	
	
	// 取得使用者目前未讀通知筆數
	public long countByRecipientTypeAndRecipientIdAndStatus(NotificationRecipientType recipientType, Integer recipientId, NotificationStatus status);
	
	
	// 批量更新 (全部已讀), 回傳改了幾筆 (maybe 前端可用於更新未讀數)
	@Modifying
	@Query(value = "update NotificationVO set status = :read where recipientType = :recipientType AND recipientId = :recipientId AND status = :unread")
	public int updateAllStatus(NotificationStatus read, NotificationRecipientType recipientType, Integer recipientId, NotificationStatus unread);

	

}
