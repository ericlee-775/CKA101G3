package com.farmily.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 立即生效、不觸發重審的欄位
public class FarmerProfileUpdateRequest {

    // 選填（有填才改）；有填就要符合手機格式（09 開頭共 10 碼）
    @Pattern(regexp = "^(09\\d{8})?$", message = "手機號碼格式錯誤（需為 09 開頭共 10 碼）")
    private String farmerPhoneNum;
    @Size(max = 2000, message = "農場介紹最多 2000 字")
    private String farmDesc;

    public String getFarmerPhoneNum() {
        return farmerPhoneNum;
    }

    public void setFarmerPhoneNum(String farmerPhoneNum) {
        this.farmerPhoneNum = farmerPhoneNum;
    }

    public String getFarmDesc() {
        return farmDesc;
    }

    public void setFarmDesc(String farmDesc) {
        this.farmDesc = farmDesc;
    }
}
