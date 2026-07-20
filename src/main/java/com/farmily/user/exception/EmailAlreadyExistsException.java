package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException  {

    // 409
    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "帳號已註冊使用");
    }

}
