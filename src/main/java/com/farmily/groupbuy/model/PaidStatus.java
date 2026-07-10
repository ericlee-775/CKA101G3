package com.farmily.groupbuy.model;

public enum PaidStatus {
unpaid("待撥款"),
paid("已撥款");
private final String displayName;

	PaidStatus(String displayName){
		this.displayName=displayName;
	}
	
public String getDisplayName() {
	return displayName;
}
	
	
}
