package com.farmily.notification.dto;

import java.time.LocalDateTime;

// 用於回傳給管理員後台顯示的通知列表資訊
public class NotificationAnnouncementResponseDTO {
	
	private Integer announcementId;
	private String audience;
	private String content;
	private Integer recipientCount;
	private String adminName;
	private LocalDateTime createdAt;
	
	public Integer getAnnouncementId() {
		return announcementId;
	}
	
	public void setAnnouncementId(Integer announcementId) {
		this.announcementId = announcementId;
	}
	
	public String getAudience() {
		return audience;
	}
	
	public void setAudience(String audience) {
		this.audience = audience;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public Integer getRecipientCount() {
		return recipientCount;
	}
	
	public void setRecipientCount(Integer recipientCount) {
		this.recipientCount = recipientCount;
	}
	
	public String getAdminName() {
		return adminName;
	}
	
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public NotificationAnnouncementResponseDTO() {
		super();
	}
	
	
}
