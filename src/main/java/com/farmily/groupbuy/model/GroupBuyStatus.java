package com.farmily.groupbuy.model;

public enum GroupBuyStatus {
    open("開團中"),
    success("已成團"),
    failed("未成團"),
    cancelled("已取消"),
    pending("待開團");
    private final String displayName;

    
    
    GroupBuyStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    
}