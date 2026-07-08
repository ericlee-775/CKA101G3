package com.farmily.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

// 小農註冊申請請求端(api/farmer)
public class FarmerRegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String farmName;

    @NotBlank
    private String farmAddress;

    private Integer districtId;

    @NotBlank
    private String farmerPhoneNum;

    @NotBlank
    private String farmDesc;

    private BigDecimal locLat;      // 前端自動抓取後送入
    private BigDecimal locLong;
    // 證明文件走 multipart/form-data（同商品做法），故用 MultipartFile 接收；
    // service 端以 getBytes() 轉成 byte[] 存進 FarmerReview。大小上限吃 application.properties 的 multipart 設定。
    private MultipartFile certFileLand;
    private MultipartFile certFileProduct;
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
