package com.farmily.groupbuy.model;

public enum OrderStatus {
	pending("等待中"),
	confirmed("確認收貨");
	
	private final String displayName;

	
	OrderStatus(String displayName){
		this.displayName=displayName;
	}
	public String getDisplayName() {
		return displayName;
	}
	
	

}
