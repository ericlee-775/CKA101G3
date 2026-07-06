package com.farmily.notification.dto;

import java.time.LocalDateTime;

import com.farmily.notification.model.NotificationStatus;

// 用於回傳給前端顯示的通知列表資訊
public class NotificationResponseDTO {
	
	private String targetType;
	private Integer targetId;
	private String content;
	private LocalDateTime createdAt;
	private String status;

	public NotificationResponseDTO() {
		super();
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public Integer getTargetId() {
		return targetId;
	}

	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
