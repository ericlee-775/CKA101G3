package com.farmily.user.exception;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    // 404
    public UserNotFoundException() {
        super("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "查無此用戶");
    }
}