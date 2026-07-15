package com.farmily.user.controller;

import com.farmily.user.controller.view.AdminAccountViewController;
import com.farmily.user.controller.view.AdminFarmerViewController;
import com.farmily.user.controller.view.AdminMemberViewController;
import com.farmily.user.controller.view.AdminReviewViewController;
import com.farmily.user.controller.view.AdminViewController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// 後台頁面（Thymeleaf）專用錯誤處理：把業務規則擋下的例外顯示成友善頁面，
// 而不是走全域 @RestControllerAdvice 的純文字
// 只套用在後台這 5 個 @Controller，並用最高優先權蓋過全域處理
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(assignableTypes = {
        AdminViewController.class,
        AdminAccountViewController.class,
        AdminMemberViewController.class,
        AdminFarmerViewController.class,
        AdminReviewViewController.class
})
public class AdminViewExceptionHandler {

    // 權限不足（例：同階保護－不能修改/刪除其他超級管理員）
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }

    // 表單欄位格式不符的最後防線（有接 BindingResult 的端點會自行處理，不會走到這）
    @ExceptionHandler(BindException.class)
    public String handleBind(BindException e, Model model) {
        model.addAttribute("errorMessage", "輸入格式不符，請檢查後重新輸入");
        return "back-end/admin/opError";
    }

    // 密碼錯誤（例：變更密碼時舊密碼不正確）
    @ExceptionHandler(BadCredentialsException.class)
    public String handleBadCredentials(BadCredentialsException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }

    // 狀態衝突（例：不能刪除自己、帳號已存在）
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }

    // 查無資料 / 參數錯誤
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "back-end/admin/opError";
    }
}