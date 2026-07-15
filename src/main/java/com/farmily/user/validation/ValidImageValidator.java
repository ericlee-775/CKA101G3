package com.farmily.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

// @ValidImage 的實際驗證邏輯
public class ValidImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private static final long MAX_SIZE = 5 * 1024 * 1024;   // 5MB

    private boolean required;

    @Override
    public void initialize(ValidImage anno) {
        this.required = anno.required();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // 沒選檔：必填要擋，非必填放行（重新送審會沿用上一輪的圖片）
        if (file == null || file.isEmpty()) {
            return !required;
        }

        // 大小上限 5MB
        if (file.getSize() > MAX_SIZE) {
            return false;
        }

        // 格式只接受 jpg / png
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") || contentType.equals("image/png");
    }
}
