package com.farmily.coupon.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="coupon")
public class CouponVO {
	@Id
	@Column(name="coupon_id")
	private String couponId;

	@Column(name="coupon_info")
	private String couponInfo;

	@Column(name="issue_start_date")
	private LocalDateTime issueStartDate;

	@Column(name="issue_end_date")
	private LocalDateTime issueEndDate;

	@Column(name="amount")
	private Integer amount;

	@Column(name="min_spending")
	private Integer minSpending;
	
	
	
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
	public LocalDateTime getIssueStartDate() {
		return issueStartDate;
	}
	public void setIssueStartDate(LocalDateTime issueStartDate) {
		this.issueStartDate = issueStartDate;
	}
	public LocalDateTime getIssueEndDate() {
		return issueEndDate;
	}
	public void setIssueEndDate(LocalDateTime issueEndDate) {
		this.issueEndDate = issueEndDate;
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
	
	
	

}
