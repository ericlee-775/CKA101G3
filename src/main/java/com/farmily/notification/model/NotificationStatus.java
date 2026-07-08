package com.farmily.notification.model;

public enum NotificationStatus {
	unread("未讀"),
	read("已讀");
	
	private final String displayName;
	
	NotificationStatus(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
