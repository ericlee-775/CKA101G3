package com.farmily.user.event;

import com.farmily.user.model.AccountToken;

// 事件類別 (一包資料)：告訴監聽器「什麼身分」註冊
public class MemberRegisteredEvent {
    private final String email;
    private final AccountToken.AccountType accountType;

    public MemberRegisteredEvent(String email, AccountToken.AccountType accountType) {
        this.email = email;
        this.accountType = accountType;
    }

    public String getEmail() { return email; }
    public AccountToken.AccountType getAccountType() { return accountType; }
}