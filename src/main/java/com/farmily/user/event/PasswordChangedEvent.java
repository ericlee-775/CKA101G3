package com.farmily.user.event;

// 修改密碼事件類別
public class PasswordChangedEvent {

    private final String email;

    public PasswordChangedEvent(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}