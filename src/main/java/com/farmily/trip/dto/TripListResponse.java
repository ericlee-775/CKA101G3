package com.farmily.trip.dto;

// 消費者活動列表：一張卡片需要的資訊
public class TripListResponse {

    private Integer farmTripId;
    private String farmTripTitle;
    private String farmTripType;
    private String location;
    private Integer referPrice;
    private Integer starNumbers;
    private String farmName;
    private Integer farmerId;
    private String registrationStatus;   // OPEN / CLOSED / CANCELLED（由場次計算）


    public Integer getFarmTripId() { return farmTripId; }
    public void setFarmTripId(Integer farmTripId) { this.farmTripId = farmTripId; }

    public String getFarmTripTitle() { return farmTripTitle; }
    public void setFarmTripTitle(String farmTripTitle) { this.farmTripTitle = farmTripTitle; }

    public String getFarmTripType() { return farmTripType; }
    public void setFarmTripType(String farmTripType) { this.farmTripType = farmTripType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getReferPrice() { return referPrice; }
    public void setReferPrice(Integer referPrice) { this.referPrice = referPrice; }

    public Integer getStarNumbers() { return starNumbers; }
    public void setStarNumbers(Integer starNumbers) { this.starNumbers = starNumbers; }
    
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public Integer getFarmerId() { return farmerId; }
    public void setFarmerId(Integer farmerId) { this.farmerId = farmerId; }

    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    
}