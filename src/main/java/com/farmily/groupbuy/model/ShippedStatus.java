package com.farmily.groupbuy.model;

public enum ShippedStatus {
	PENDING("待出貨"),
	DELIVERED("已送達");
	
	private final String displayName;
	
	ShippedStatus(String displayName){
		this.displayName=displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
