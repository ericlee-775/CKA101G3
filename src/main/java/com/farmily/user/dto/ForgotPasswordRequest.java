package com.farmily.user.dto;

import com.farmily.user.model.AccountToken;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

// 忘記密碼第一步：輸入 email，請系統寄重設密碼信
// 會員、小農用
public class ForgotPasswordRequest {

    @Email
    @NotNull
    private String email;

    // 要找哪一種身分：MEMBER 或 FARMER
    @NotNull
    private AccountToken.AccountType accountType;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountToken.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountToken.AccountType accountType) {
        this.accountType = accountType;
    }
}