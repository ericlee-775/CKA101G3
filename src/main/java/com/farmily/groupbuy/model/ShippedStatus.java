package com.farmily.groupbuy.model;

public enum ShippedStatus {
	pending("待出貨"),
	shipped("已出貨"),
	delivered("已送達");
	
	private final String displayName;
	
	ShippedStatus(String displayName){
		this.displayName=displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
