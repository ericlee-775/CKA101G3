package com.farmily.trip.dto;

// 活動詳情頁：比列表多了完整介紹和評論資訊
public class TripDetailResponse {

    private Integer farmTripId;
    private String farmTripTitle;
    private String farmTripType;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;
    private Integer commentNumbers;
    private Integer starNumbers;
    private String farmName;

    public Integer getFarmTripId() { return farmTripId; }
    public void setFarmTripId(Integer farmTripId) { this.farmTripId = farmTripId; }

    public String getFarmTripTitle() { return farmTripTitle; }
    public void setFarmTripTitle(String farmTripTitle) { this.farmTripTitle = farmTripTitle; }

    public String getFarmTripType() { return farmTripType; }
    public void setFarmTripType(String farmTripType) { this.farmTripType = farmTripType; }

    public String getFarmTripIntro() { return farmTripIntro; }
    public void setFarmTripIntro(String farmTripIntro) { this.farmTripIntro = farmTripIntro; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getReferPrice() { return referPrice; }
    public void setReferPrice(Integer referPrice) { this.referPrice = referPrice; }

    public Integer getCommentNumbers() { return commentNumbers; }
    public void setCommentNumbers(Integer commentNumbers) { this.commentNumbers = commentNumbers; }

    public Integer getStarNumbers() { return starNumbers; }
    public void setStarNumbers(Integer starNumbers) { this.starNumbers = starNumbers; }
    
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

}