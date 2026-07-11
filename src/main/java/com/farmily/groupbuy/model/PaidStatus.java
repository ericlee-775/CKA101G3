package com.farmily.groupbuy.model;

public enum PaidStatus {
	UNPAID("待撥款"),
	PAID("已撥款");
private final String displayName;

	PaidStatus(String displayName){
		this.displayName=displayName;
	}
	
public String getDisplayName() {
	return displayName;
}
	
	
}
