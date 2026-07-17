package com.farmily.trip.dto;

import java.sql.Timestamp;

// 一筆報名紀錄的回應
public class OrderResponse {

    private Integer farmTripOrderId;
    private Integer farmSessionId;
    private String farmTripOrderBookingNo;
    private Integer numPeople;
    private String orderStatus;
    private Timestamp bookedAt;
    private String userName;
    private String userPhoneNum;
    private String note;
    private Integer farmTripId;
    private String farmTripTitle;
    private java.sql.Timestamp farmTripEnd;
    private java.sql.Timestamp farmTripStart;
    
    public Integer getFarmTripOrderId() { return farmTripOrderId; }
    public void setFarmTripOrderId(Integer farmTripOrderId) { this.farmTripOrderId = farmTripOrderId; }

    public Integer getFarmSessionId() { return farmSessionId; }
    public void setFarmSessionId(Integer farmSessionId) { this.farmSessionId = farmSessionId; }

    public String getFarmTripOrderBookingNo() { return farmTripOrderBookingNo; }
    public void setFarmTripOrderBookingNo(String farmTripOrderBookingNo) { this.farmTripOrderBookingNo = farmTripOrderBookingNo; }

    public Integer getNumPeople() { return numPeople; }
    public void setNumPeople(Integer numPeople) { this.numPeople = numPeople; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public Timestamp getBookedAt() { return bookedAt; }
    public void setBookedAt(Timestamp bookedAt) { this.bookedAt = bookedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhoneNum() { return userPhoneNum; }
    public void setUserPhoneNum(String userPhoneNum) { this.userPhoneNum = userPhoneNum; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getFarmTripId() { return farmTripId; }
    public void setFarmTripId(Integer farmTripId) { this.farmTripId = farmTripId; }

    public String getFarmTripTitle() { return farmTripTitle; }
    public void setFarmTripTitle(String farmTripTitle) { this.farmTripTitle = farmTripTitle; }

    public java.sql.Timestamp getFarmTripEnd() { return farmTripEnd; }
    public void setFarmTripEnd(java.sql.Timestamp farmTripEnd) { this.farmTripEnd = farmTripEnd; }
    
    public java.sql.Timestamp getFarmTripStart() { return farmTripStart; }
    public void setFarmTripStart(java.sql.Timestamp farmTripStart) { this.farmTripStart = farmTripStart; }
    
}