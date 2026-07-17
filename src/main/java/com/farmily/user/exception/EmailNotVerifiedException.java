package com.farmily.user.exception;
import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BusinessException {

    // 403
    public EmailNotVerifiedException() {
        super("EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN, "請先完成 Email 驗證後再登入，可至信箱點擊驗證連結");
    }
}