package com.farmily.user.exception;

import java.time.LocalDateTime;

// 統一錯誤回應格式
public class ApiError {

    private final String code;             // 錯誤碼：給前端程式判斷用
    private final int status;              // HTTP 狀態碼，例如 409
    private final String message;          // 錯誤訊息：給人看
    private final LocalDateTime timestamp; // 發生時間，方便對 log

    public ApiError(String code, int status, String message, LocalDateTime timestamp) {
        this.message = message;
        this.code = code;
        this.status = status;
        this.timestamp = timestamp;
    }

    // 靜態工廠：呼叫端少寫一次 LocalDateTime.now()
    public static ApiError of(String code, int status, String message) {
        return new ApiError(code, status, message, LocalDateTime.now());
    }

    // getter：Jackson 序列化轉成 JSON 回給前端
    public String getMessage() { return message; }
    public String getCode() { return code; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}