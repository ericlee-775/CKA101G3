package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

// 404
public class AdminNotFoundException extends BusinessException{
    public AdminNotFoundException(){
        super("ADMIN_NOT_FOUND", HttpStatus.NOT_FOUND,"查無此管理員");
    }
}
