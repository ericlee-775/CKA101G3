package com.farmily.user.event;

// 修改密碼事件類別 - for admin 專用的修改密碼事件
public class AdminPasswordChangedEvent {

    private final String email;

    public AdminPasswordChangedEvent(String email) {
        this.email = email;
    }

    public String getEmail(){
        return email;
    }
}
