package com.farmily.coupon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(CouponDetailId.class)
@Table(name="coupon_details")
public class CouponDetailVO {

	@Id
	@Column(name="user_id")
	private Integer userId;

	@Id
	@Column(name="coupon_id")
	private String couponId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	
}
