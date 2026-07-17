package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends BusinessException{

    // 404
    public ReviewNotFoundException(){
        super("REVIEW_NOT_FOUND", HttpStatus.NOT_FOUND, "查無此審核案件");
    }
}
