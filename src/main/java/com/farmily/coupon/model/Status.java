package com.farmily.coupon.model;

public enum Status {
    INACTIVE(0),   // 未啟用
    ACTIVE(1),     // 啟用中
    EXPIRED(2);    // 已過期

    private final int code;

    Status(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
