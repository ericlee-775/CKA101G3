package com.farmily.trip.dto;

// 消費者報名時送進來的資料（報哪個場次由網址決定）
public class OrderCreateRequest {

    private Integer userId;       // 之後接登入功能改從 token 拿，先由前端送
    private Integer numPeople;    // 報名人數
    private String userName;      // 聯絡人姓名
    private String userPhoneNum;  // 聯絡電話
    private String note;          // 備註（可不填）

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getNumPeople() { return numPeople; }
    public void setNumPeople(Integer numPeople) { this.numPeople = numPeople; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhoneNum() { return userPhoneNum; }
    public void setUserPhoneNum(String userPhoneNum) { this.userPhoneNum = userPhoneNum; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}