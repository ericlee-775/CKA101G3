package com.farmily.coupon.model;

import java.sql.Timestamp;

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
	private Timestamp issueStartDate;
	@Column(name="issue_end_date")
	private Timestamp issueEndDate;
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
	public Timestamp getIssueStartDate() {
		return issueStartDate;
	}
	public void setIssueStartDate(Timestamp issueStartDate) {
		this.issueStartDate = issueStartDate;
	}
	public Timestamp getIssueEndDate() {
		return issueEndDate;
	}
	public void setIssueEndDate(Timestamp issueEndDate) {
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
