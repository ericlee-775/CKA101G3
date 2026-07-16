package com.farmily.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.farmily.user.validation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

// 小農註冊申請請求端(api/farmer)
public class FarmerRegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 60, message = "密碼長度需 8~60 字")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密碼需同時包含英文字母與數字")
    private String password;

    @NotBlank
    @Size(max = 50, message = "農場名稱最多 50 字")
    private String farmName;

    @NotBlank
    @Size(max = 100, message = "農場地址最多 100 字")
    private String farmAddress;

    @NotNull(message = "請選擇所在區域")
    private Integer districtId;

    @NotBlank
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤（需為 09 開頭共 10 碼）")
    private String farmerPhoneNum;

    @NotBlank
    @Size(max = 2000, message = "農場描述最多 2000 字")
    private String farmDesc;

    // 前端自動抓取後送入；緯度 -90~90、經度 -180~180，小數精度對齊 DB 欄位
    @DecimalMin(value = "-90.0", message = "緯度超出範圍")
    @DecimalMax(value = "90.0", message = "緯度超出範圍")
    @Digits(integer = 2, fraction = 8, message = "緯度格式錯誤")
    private BigDecimal locLat;

    @DecimalMin(value = "-180.0", message = "經度超出範圍")
    @DecimalMax(value = "180.0", message = "經度超出範圍")
    @Digits(integer = 3, fraction = 8, message = "經度格式錯誤")
    private BigDecimal locLong;

    // 證明文件走 multipart/form-data（同商品做法），用 MultipartFile 接收；
    // service 端以 getBytes() 轉成 byte[] 存進 FarmerReview。大小上限吃 application.properties 的 multipart 設定。
    // 初次申請三份證明文件皆必填（required = true）
    @ValidImage(required = true)
    private MultipartFile certFileLand;

    @ValidImage(required = true)
    private MultipartFile certFileProduct;

    @ValidImage(required = true)
    private MultipartFile certFileIdentity;


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

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public String getFarmAddress() {
        return farmAddress;
    }

    public void setFarmAddress(String farmAddress) {
        this.farmAddress = farmAddress;
    }

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

    public BigDecimal getLocLat() {
        return locLat;
    }

    public void setLocLat(BigDecimal locLat) {
        this.locLat = locLat;
    }

    public BigDecimal getLocLong() {
        return locLong;
    }

    public void setLocLong(BigDecimal locLong) {
        this.locLong = locLong;
    }

    public MultipartFile getCertFileLand() {
        return certFileLand;
    }

    public void setCertFileLand(MultipartFile certFileLand) {
        this.certFileLand = certFileLand;
    }

    public MultipartFile getCertFileProduct() {
        return certFileProduct;
    }

    public void setCertFileProduct(MultipartFile certFileProduct) {
        this.certFileProduct = certFileProduct;
    }

    public MultipartFile getCertFileIdentity() {
        return certFileIdentity;
    }

    public void setCertFileIdentity(MultipartFile certFileIdentity) {
        this.certFileIdentity = certFileIdentity;
    }
}
