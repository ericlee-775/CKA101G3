package com.farmily.user.exception;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BusinessException {

    // 400
    public InvalidTokenException() {
        super("INVALID_TOKEN", HttpStatus.BAD_REQUEST, "連結無效或已過期，請重新申請");
    }
}