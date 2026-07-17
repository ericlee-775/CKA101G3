package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

public class DistrictNotFoundException extends BusinessException{

    // 404
    public DistrictNotFoundException(){
        super("DISTRICT_NOT_FOUND", HttpStatus.NOT_FOUND, "查無此區域");
    }
}
