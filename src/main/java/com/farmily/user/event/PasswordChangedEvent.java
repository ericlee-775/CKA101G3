package com.farmily.user.event;

// 修改密碼事件類別 - for 會員/小農專用的修改密碼事件
public class PasswordChangedEvent {

    private final String email;

    public PasswordChangedEvent(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}