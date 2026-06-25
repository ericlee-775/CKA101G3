package com.farmily.groupbuy.model;

public enum RequestStatus {

    pending("待審核"),
    approved("通過"),
    rejected("拒絕");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}