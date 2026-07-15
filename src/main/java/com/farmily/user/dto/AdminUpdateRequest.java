package com.farmily.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// 修改其他管理員時前端可送的資料（欄位都可不填，有填才改）
public class AdminUpdateRequest {

    @Size(max = 50, message = "名稱最多 50 字")
    private String updateName;
    // 選填；有填才驗，須為 DB ENUM 允許值
    @Pattern(regexp = "^(ACTIVE|SUSPENDED|DELETED)?$", message = "狀態值不合法")
    private String updateStatus;
    private List<String> updatePermissionCodes;


    public String getUpdateName() {
        return updateName;
    }

    public void setUpdateName(String updateName) {
        this.updateName = updateName;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public List<String> getUpdatePermissionCodes() {
        return updatePermissionCodes;
    }

    public void setUpdatePermissionCodes(List<String> updatePermissionCodes) {
        this.updatePermissionCodes = updatePermissionCodes;
    }
}