package com.farmily.trip.model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity 
@Table(name = "farm_trip_order") 

public class FarmTripOrder {
	
	
	    @Id 
	    @GeneratedValue(strategy = GenerationType.IDENTITY) 
	    @Column(name = "farm_trip_order_id", updatable = false)
	    private Integer farmTripOrderId;

	    @Column(name = "farm_session_id")
	    private Integer farmSessionId;

	    @Column(name = "user_id")
	    private Integer userId;

	    @Column(name = "farm_trip_order_booking_no")
	    private String farmTripOrderBookingNo;

	    @Column(name = "num_people")
	    private Integer numPeople;

	    @Column(name = "order_status")
	    @Enumerated(EnumType.STRING)
	    private OrderStatus orderStatus;

	    @Column(name = "booked_at")
	    private Timestamp bookedAt;

	    @Column(name = "cancelled_at")
	    private Timestamp cancelledAt;

	    @Column(name = "completed_at")
	    private Timestamp completedAt;

	    @Column(name = "user_name")
	    private String userName;

	    @Column(name = "user_phone_num")
	    private String userPhoneNum;

	    @Column(name = "note")
	    private String note;

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

		public OrderStatus getOrderStatus() {
			return orderStatus;
		}

		public void setOrderStatus(OrderStatus orderStatus) {
			this.orderStatus = orderStatus;
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
