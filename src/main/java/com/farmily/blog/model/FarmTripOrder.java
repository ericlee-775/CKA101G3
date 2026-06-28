package com.farmily.blog.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity //這個意思是，告訴JPA/Hibernate ，這個class是要拿來對應資料庫表格
@Table(name = "farm_trip_order") //告訴JPA 這個table的名字
public class FarmTripOrder {

    @Id //告訴JPA 這是PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) //告訴JPA 這個 id 由資料庫自動產生
    @Column(name = "farm_trip_order_id", updatable = false)
    Integer farmTripOrderId;

    @Column(name = "farm_session_id")
    Integer farmSessionId;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "farm_trip_order_booking_no")
    String farmTripOrderBookingNo;

    @Column(name = "num_people")
    Integer numPeople;

    @Column(name = "status")
    String status;

    @Column(name = "booked_at")
    Timestamp bookedAt;

    @Column(name = "cancelled_at")
    Timestamp cancelledAt;

    @Column(name = "completed_at")
    Timestamp completedAt;

    @Column(name = "user_name")
    String userName;

    @Column(name = "user_phone_num")
    String userPhoneNum;

    @Column(name = "note")
    String note;

    public Integer getFarmTripOrderId() {
        return farmTripOrderId;
    }

    public void setFarmTripOrderId(Integer farmTripOrderId) {
        this.farmTripOrderId = farmTripOrderId;
    }

    public Integer getFarmSessionId() {
        return farmSessionId;
    }

    public void setFarmSessionId(Integer farmSessionId) {
        this.farmSessionId = farmSessionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFarmTripOrderBookingNo() {
        return farmTripOrderBookingNo;
    }

    public void setFarmTripOrderBookingNo(String farmTripOrderBookingNo) {
        this.farmTripOrderBookingNo = farmTripOrderBookingNo;
    }

    public Integer getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(Integer numPeople) {
        this.numPeople = numPeople;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Timestamp bookedAt) {
        this.bookedAt = bookedAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
