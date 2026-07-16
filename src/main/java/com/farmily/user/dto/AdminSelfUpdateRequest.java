package com.farmily.user.dto;

import jakarta.validation.constraints.Size;

// 管理員修改「自己」時可送的資料（只開放安全欄位：名字）
public class AdminSelfUpdateRequest {

    @Size(max = 50, message = "名稱最多 50 字")
    private String name;   // 新名字

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}