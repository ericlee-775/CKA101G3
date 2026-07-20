package com.farmily.notification.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationAnnouncementRepository extends JpaRepository<NotificationAnnouncementVO, Integer>{
	
	// 取得公告列表
//	Page<NotificationAnnouncementVO> findAllByOrderByAnnouncementIdDesc(Pageable pageable);
	
	// 取得公告列表, 同時取得管理員資料 (JOIN FETCH)
	@Query(value = "SELECT a FROM NotificationAnnouncementVO a "
			+ "LEFT JOIN FETCH a.admin "
			+ "ORDER BY a.announcementId DESC",
			countQuery = "SELECT COUNT(a) FROM NotificationAnnouncementVO a")
	Page<NotificationAnnouncementVO> findAllWithAdmin(Pageable pageable);
	
}
