package com.farmily.groupbuy.exception.ApiExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
		Map<String, String> body = new HashMap<>();
		body.put("message", e.getMessage());
		return ResponseEntity.badRequest().body(body);
	}
}