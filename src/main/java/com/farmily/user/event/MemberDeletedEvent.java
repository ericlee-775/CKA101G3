package com.farmily.user.event;

// 會員註銷事件類別 - for 會員自行註銷帳號
public class MemberDeletedEvent {

    private final String email;

    public MemberDeletedEvent(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}