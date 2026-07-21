package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// 新增其他管理員時，前端要送的資料
public class AdminCreateRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 60, message = "密碼長度需 8~60 字")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密碼需同時包含英文字母與數字")
    private String password;

    @NotBlank
    @Size(max = 50, message = "名稱最多 50 字")
    private String name;

    // 要指派給他的權限代碼（新增管理員時必須至少指派一項）
    @NotEmpty(message = "至少需指派一項權限")
    private List<String> permissionCodes;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}