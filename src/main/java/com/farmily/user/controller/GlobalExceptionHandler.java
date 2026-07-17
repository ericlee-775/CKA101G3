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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

// 負責 user 模組的 REST 錯誤出口
// 優先權要低於 AdminViewExceptionHandler，後台頁面的錯誤才會是 opError 頁而不是 JSON
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RestControllerAdvice(basePackages = "com.farmily.user")
public class GlobalExceptionHandler {

    // 處理所有業務例外，轉成結構化 ApiError
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException e) {
        ApiError apiErrorBody = ApiError.of(e.getCode(), e.getStatus().value(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(apiErrorBody);
    }

    // Spring Security 拋出例外 - 密碼錯誤、帳號不存在、Google token 無效
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of("BAD_CREDENTIALS", HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    // Spring Security 拋出例外 - 沒有權限
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of("ACCESS_DENIED", HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    // Bean Validation 拋出例外 - 驗證失敗（JSON @RequestBody）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fieldErrors(e.getBindingResult()));
    }

    // 框架拋出例外 - 驗證失敗（multipart @ModelAttribute，例如小農申請含證明文件圖片時）
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBind(BindException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fieldErrors(e.getBindingResult()));
    }

    // 把 Bean Validation 的欄位錯誤整理成 { 欄位名: 錯誤訊息 }，前端可逐欄顯示
    private Map<String, String> fieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : bindingResult.getFieldErrors()) {
            // 同一欄位多個錯誤時保留第一個
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return errors;
    }

    // 框架拋出例外 - 上傳檔案過大（吃 application.properties 的 multipart 上限）
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiError.of("FILE_TOO_LARGE", HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "上傳檔案過大，單張圖片請小於 5MB"));
    }

    // 框架拋出例外 - 用錯 HTTP 方法
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiError.of("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED.value(), "不支援的請求方法"));
    }

    // 框架拋出例外 - 找不到靜態資源 / 頁面：回正確的 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("PAGE_NOT_FOUND", HttpStatus.NOT_FOUND.value(), "找不到頁面"));
    }

    // 其他未預期錯誤：一律視為伺服器錯誤，不把內部訊息（例如 No enum constant ...）洩漏給前端
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR.value(), "伺服器發生錯誤"));
    }
}
