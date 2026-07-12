package com.farmily.coupon.dto;

import java.time.LocalDateTime;

import com.farmily.coupon.model.CouponStatus;

public class MyCouponDTO {
	 private String couponId;
	 private String couponInfo;
	 private Integer amount;          // 折抵
	 private Integer minSpending;     // 門檻
	 private LocalDateTime issueEndDate; // 到期
	 private CouponStatus status;     // 我這張的狀態
	 
	 
	 
	 public MyCouponDTO(String couponId, String couponInfo, Integer amount, Integer minSpending,
			LocalDateTime issueEndDate, CouponStatus status) {
		super();
		this.couponId = couponId;
		this.couponInfo = couponInfo;
		this.amount = amount;
		this.minSpending = minSpending;
		this.issueEndDate = issueEndDate;
		this.status = status;
	}
	 public String getCouponId() {
		 return couponId;
	 }
	 public void setCouponId(String couponId) {
		 this.couponId = couponId;
	 }
	 public String getCouponInfo() {
		 return couponInfo;
	 }
	 public void setCouponInfo(String couponInfo) {
		 this.couponInfo = couponInfo;
	 }
	 public Integer getAmount() {
		 return amount;
	 }
	 public void setAmount(Integer amount) {
		 this.amount = amount;
	 }
	 public Integer getMinSpending() {
		 return minSpending;
	 }
	 public void setMinSpending(Integer minSpending) {
		 this.minSpending = minSpending;
	 }
	 public LocalDateTime getIssueEndDate() {
		 return issueEndDate;
	 }
	 public void setIssueEndDate(LocalDateTime issueEndDate) {
		 this.issueEndDate = issueEndDate;
	 }
	 public CouponStatus getStatus() {
		 return status;
	 }
	 public void setStatus(CouponStatus status) {
		 this.status = status;
	 }
	 
	 
}
