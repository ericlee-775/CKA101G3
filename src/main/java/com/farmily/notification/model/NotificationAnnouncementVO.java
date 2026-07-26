package com.farmily.notification.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.farmily.user.model.Admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table (name = "notification_announcement")
public class NotificationAnnouncementVO implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column (name = "announcement_id")
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private Integer announcementId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id", referencedColumnName = "admin_id")
	private Admin admin;
	
	@Column (name = "audience")
	@Enumerated (EnumType.STRING)
	private NotifAnnouncementAudience audience;
	
	@Column (name = "content")
	private String content;
	
	@Column (name = "created_at", updatable=false)
	private LocalDateTime createdAt;
	
	@Column (name = "recipient_count")
	private Integer recipientCount;

	public Integer getAnnouncementId() {
		return announcementId;
	}

	public void setAnnouncementId(Integer announcementId) {
		this.announcementId = announcementId;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public NotifAnnouncementAudience getAudience() {
		return audience;
	}

	public void setAudience(NotifAnnouncementAudience audience) {
		this.audience = audience;
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

	public Integer getRecipientCount() {
		return recipientCount;
	}

	public void setRecipientCount(Integer recipientCount) {
		this.recipientCount = recipientCount;
	}
	
	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
	
}
