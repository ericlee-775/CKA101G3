package com.farmily.user.exception;

import org.springframework.http.HttpStatus;

public class FarmerNotFoundException extends BusinessException {

    // 404
    public FarmerNotFoundException() {
        super("FARMER_NOT_FOUND", HttpStatus.NOT_FOUND, "查無此小農");
    }
}
