package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

// 自訂業務例外基底 + 擴充 Spring 需要的 code / status
public class BusinessException extends RuntimeException{

    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, HttpStatus status, String message) {
        super(message);          // 呼叫父類別 RuntimeException 建構子
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
