package com.farmily.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 證明文件（MultipartFile）格式驗證：只接受 jpg / png 圖片，且不超過 5MB
// Bean Validation 對 MultipartFile 無內建約束，自訂此 constraint，由 ValidImageValidator 執行
@Constraint(validatedBy = ValidImageValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {

    String message() default "證明文件需為 jpg 或 png 圖片，且小於 5MB";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 是否必填：小農初次申請 = true；重新送審沒上傳會沿用上一輪 = false（預設）
    boolean required() default false;
}
