package com.farmily.trip.dto;

// 小農發起活動時送進來的資料
// 注意：沒有 status 欄位，狀態一律由後端設成 PENDING
public class TripCreateRequest {

    private Integer farmerId;       // 之後接上登入功能後改從 token 拿，先由前端送
    private String farmTripType;    // 收字串，Service 裡轉成 enum
    private String farmTripTitle;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;

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
}