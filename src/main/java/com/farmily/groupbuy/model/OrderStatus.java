package com.farmily.groupbuy.model;

public enum OrderStatus {
	PENDING("等待中"),
	COMPLETED("確認收貨");
	
	private final String displayName;

	
	OrderStatus(String displayName){
		this.displayName=displayName;
	}
	public String getDisplayName() {
		return displayName;
	}
	
	

}
