package com.farmily.user.exception;
import org.springframework.http.HttpStatus;

public class AccountSuspendedException extends BusinessException {

    public AccountSuspendedException() {
        super("ACCOUNT_SUSPENDED", HttpStatus.FORBIDDEN, "此帳號已遭停權或終止，有任何疑問請聯繫客服");
    }
}