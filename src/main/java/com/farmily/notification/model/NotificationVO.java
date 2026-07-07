package com.farmily.notification.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification")
public class NotificationVO implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "notification_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer notificationId;
	
	@Column(name = "recipient_type")
	@Enumerated(EnumType.STRING)
	private NotificationRecipientType recipientType;
	
	@Column(name = "recipient_id")
	private Integer recipientId;
	
	@Column(name = "type_code")
	private String typeCode;
	
	@Column(name="target_type")
	private String targetType;
	
	@Column(name = "target_id")
	private Integer targetId;
	
	@Column(name = "content")
	private String content;
	
	@Column(name = "status", insertable=false)
	@Enumerated(EnumType.STRING)
	private NotificationStatus status;
	
	@Column(name = "created_at", insertable=false, updatable=false)
	private LocalDateTime createdAt;
	
	public Integer getNotificationId() {
		return notificationId;
	}
	
	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}
	
	public NotificationRecipientType getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(NotificationRecipientType recipientType) {
		this.recipientType = recipientType;
	}

	public Integer getRecipientId() {
		return recipientId;
	}
	
	public void setRecipientId(Integer recipientId) {
		this.recipientId = recipientId;
	}
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
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
	
	public NotificationStatus getStatus() {
		return status;
	}

	public void setStatus(NotificationStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public NotificationVO() {
		super();
	}

}
