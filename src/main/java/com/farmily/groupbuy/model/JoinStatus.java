package com.farmily.groupbuy.model;

public enum JoinStatus {
	active("參加"),
	cancelled("退出");
	private final String displayName;
	JoinStatus(String displayName){
		this.displayName=displayName;
	}
	public String getDisplayName() {
		return displayName;
	}

}
