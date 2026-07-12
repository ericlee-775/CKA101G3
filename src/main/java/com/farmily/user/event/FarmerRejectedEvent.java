package com.farmily.user.event;

// 管理員對小農審核拒絕寄信事件類別
public class FarmerRejectedEvent {

    private final String email;
    private final String rejectReason;

    public FarmerRejectedEvent(String email, String rejectReason) {
        this.email = email;
        this.rejectReason = rejectReason;
    }
    public String getEmail() {
        return email;
    }
    public String getRejectReason() {
        return rejectReason;
    }
}