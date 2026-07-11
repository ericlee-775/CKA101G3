package com.farmily.trip.dto;

import org.springframework.web.multipart.MultipartFile;   // 【新增】

// 小農發起活動時送進來的資料
// 注意：沒有 status 欄位，狀態一律由後端設成 PENDING
public class TripCreateRequest {

    private Integer farmerId;
    private String farmTripType;
    private String farmTripTitle;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;
    private MultipartFile pic;   // 【新增】活動圖片（可不傳）

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

    public MultipartFile getPic() { return pic; }             // 【新增】
    public void setPic(MultipartFile pic) { this.pic = pic; } // 【新增】
}