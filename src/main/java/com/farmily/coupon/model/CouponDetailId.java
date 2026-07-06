package com.farmily.coupon.model;

import java.io.Serializable;
import java.util.Objects;

public class CouponDetailId implements Serializable{
	private Integer userId;
	private String couponId;
	
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
	@Override
	public int hashCode() {
		return Objects.hash(couponId, userId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CouponDetailId other = (CouponDetailId) obj;
		return Objects.equals(couponId, other.couponId) && Objects.equals(userId, other.userId);
	}
	
	
	
	
}
