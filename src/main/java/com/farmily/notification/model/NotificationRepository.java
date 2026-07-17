package com.farmily.notification.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationVO, Integer> {
	
	// 取得該用戶所有通知，並分頁顯示
	@Query("SELECT n FROM NotificationVO n WHERE n.recipientType = :recipientType AND n.recipientId = :recipientId "
			 + "ORDER BY n.notificationId DESC")
	public Page<NotificationVO> findNotifs(
			@Param("recipientType") NotificationRecipientType recipientType, 
			@Param("recipientId") Integer recipientId, 
			Pageable pageable);
//	public List<NotificationVO> findByRecipientTypeAndRecipientId(NotificationRecipientType recipientType, Integer recipientId);

	
	// 取得該用戶所有通知，需要分類，並分頁顯示
//	@Query("SELECT n FROM NotificationVO n WHERE n.recipientType = :recipientType AND n.recipientId = :recipientId AND n.targetType = :targetType "
//			+ "ORDER BY CASE WHEN n.status = :unread THEN 0 ELSE 1 END, n.createdAt DESC")
	@Query("SELECT n FROM NotificationVO n WHERE n.recipientType = :recipientType AND n.recipientId = :recipientId AND n.targetType = :targetType "
			+ "ORDER BY n.notificationId DESC")
	public Page<NotificationVO> findnNotifsByTargetType(
			@Param("recipientType") NotificationRecipientType recipientType, 
			@Param("recipientId") Integer recipientId, 
			@Param("targetType") String targetType, 
			Pageable pageable);
	

	// 取得小鈴鐺預覽通知，只需要前{五}筆，
	public List<NotificationVO> findTop5ByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(NotificationRecipientType recipientType, Integer recipientId);

	
	// 取得使用者目前未讀通知筆數
	public long countByRecipientTypeAndRecipientIdAndStatus(NotificationRecipientType recipientType, Integer recipientId, NotificationStatus status);
	
	
	// 批量更新 (全部已讀), 回傳改了幾筆 (maybe 前端可用於更新未讀數)
	@Modifying
	@Query("UPDATE NotificationVO SET status = :read WHERE recipientType = :recipientType AND recipientId = :recipientId AND status = :unread")
	public int updateAllStatus(
			@Param("read") NotificationStatus read, 
			@Param("recipientType") NotificationRecipientType recipientType, 
			@Param("recipientId") Integer recipientId, 
			@Param("unread") NotificationStatus unread);

	

}
