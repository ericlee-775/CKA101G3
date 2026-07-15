package com.farmily.user.dto;

import com.farmily.user.validation.ValidImage;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

// 重新送審用（暫存，通過才生效） - 適用於已通過初審
// 證明文件走 multipart/form-data；沒上傳（null/空）時 service 端會沿用上一輪審核的圖片
public class FarmerResubmitRequest {

    @Size(max = 50, message = "農場名稱最多 50 字")
    private String farmName;
    private Integer districtId;
    @Size(max = 100, message = "農場地址最多 100 字")
    private String farmAddress;
    private BigDecimal locLat;
    private BigDecimal locLong;

    // 沒上傳時沿用上一輪的圖片，required = false（預設），有上傳才驗格式與大小
    @ValidImage
    private MultipartFile certFileLand;

    @ValidImage
    private MultipartFile certFileProduct;

    @ValidImage
    private MultipartFile certFileIdentity;

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
