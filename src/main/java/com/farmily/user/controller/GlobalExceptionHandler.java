package com.farmily.user.controller;

import com.farmily.user.exception.ApiError;
import com.farmily.user.exception.BusinessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.farmily.user")    // 只管 user 模組的 controller
public class GlobalExceptionHandler {

    // 處理所有業務例外，轉成結構化 ApiError
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException e) {
        ApiError apiErrorBody = ApiError.of(e.getCode(), e.getStatus().value(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(apiErrorBody);
    }


    // 查無此用戶、查無此區域  404
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 帳號已存在、帳號已停用、第三方帳號衝突 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // Spring Security 拋出例外 - 密碼錯誤、帳號不存在、Google token 無效
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    // Spring Security 拋出例外 - 沒有權限
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    // Bean Validation 拋出例外 - 驗證失敗（JSON @RequestBody）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("驗證失敗");
    }

    // 框架拋出例外 - 驗證失敗（multipart @ModelAttribute，例如小農申請含證明文件圖片時）
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBind(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("驗證失敗");
    }

    // 框架拋出例外 - 上傳檔案過大（吃 application.properties 的 multipart 上限）
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("上傳檔案過大，單張圖片請小於 5MB");
    }

    // 框架拋出例外 - 用錯 HTTP 方法
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("不支援的請求方法");
    }

    // 框架拋出例外 - 找不到靜態資源 / 頁面：回正確的 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到頁面");
    }

    // 其他未預期錯誤
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("伺服器發生錯誤");
    }
}
