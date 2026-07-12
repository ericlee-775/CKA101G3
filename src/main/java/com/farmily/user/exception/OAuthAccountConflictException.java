package com.farmily.user.exception;
import org.springframework.http.HttpStatus;

public class OAuthAccountConflictException extends BusinessException {

    public OAuthAccountConflictException() {
        super("OAUTH_ACCOUNT_CONFLICT", HttpStatus.CONFLICT, "此帳號已使用 Google 登入，請改用 Google 登入");
    }
}