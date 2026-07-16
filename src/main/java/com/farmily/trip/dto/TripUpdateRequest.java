package com.farmily.trip.dto;

import org.springframework.web.multipart.MultipartFile;

// 小農修改自己活動時送進來的資料
// farmerId 用來驗證「這活動是不是他本人的」；修改後狀態一律改回 PENDING 重新送審
public class TripUpdateRequest {

    private Integer farmerId;
    private String farmTripType;
    private String farmTripTitle;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;
    private MultipartFile pic;   // 選填：有傳才更新圖片

    public Integer getFarmerId() { return farmerId; }
    public void setFarmerId(Integer farmerId) { this.farmerId = farmerId; }

    public String getFarmTripType() { return farmTripType; }
    public void setFarmTripType(String farmTripType) { this.farmTripType = farmTripType; }

    public String getFarmTripTitle() { return farmTripTitle; }
    public void setFarmTripTitle(String farmTripTitle) { this.farmTripTitle = farmTripTitle; }

    public String getFarmTripIntro() { return farmTripIntro; }
    public void setFarmTripIntro(String farmTripIntro) { this.farmTripIntro = farmTripIntro; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getReferPrice() { return referPrice; }
    public void setReferPrice(Integer referPrice) { this.referPrice = referPrice; }

    public MultipartFile getPic() { return pic; }
    public void setPic(MultipartFile pic) { this.pic = pic; }
}