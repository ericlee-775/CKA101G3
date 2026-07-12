package com.farmily.user.event;

// 管理員對小農審核通過寄信事件類別
public class FarmerApprovedEvent {

    private final String email;

    public FarmerApprovedEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}