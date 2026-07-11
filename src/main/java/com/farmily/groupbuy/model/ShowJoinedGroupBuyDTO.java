package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class ShowJoinedGroupBuyDTO {
	
	private GroupBuyStatus status;
	
	private Timestamp ddlDatetime; 

	private String pickupAddress;
	
	private String productName;
	
	private Integer buyQty;
	
	private Integer paidAmount;
	
	private Integer targetAmount;//目標金額
	
	private Integer difference;//還差多少錢成團
	
	
	
	public Integer getTargetAmount() {
		return targetAmount;
	}

	public void setTargetAmount(Integer targetAmount) {
		this.targetAmount = targetAmount;
	}

	public Integer getDifference() {
		return difference;
	}

	public void setDifference(Integer difference) {
		this.difference = difference;
	}

	public GroupBuyStatus getStatus() {
		return status;
	}

	public void setStatus(GroupBuyStatus status) {
		this.status = status;
	}

	public Timestamp getDdlDatetime() {
		return ddlDatetime;
	}

	public void setDdlDatetime(Timestamp ddlDatetime) {
		this.ddlDatetime = ddlDatetime;
	}

	public String getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getBuyQty() {
		return buyQty;
	}

	public void setBuyQty(Integer buyQty) {
		this.buyQty = buyQty;
	}

	public Integer getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Integer paidAmount) {
		this.paidAmount = paidAmount;
	}

	public ShowJoinedGroupBuyDTO(GroupBuyStatus status, Timestamp ddlDatetime, String pickupAddress, String productName,
			Integer buyQty, Integer paidAmount,Integer targetAmount,Integer difference) {
		super();
		this.status = status;
		this.ddlDatetime = ddlDatetime;
		this.pickupAddress = pickupAddress;
		this.productName = productName;
		this.buyQty = buyQty;
		this.paidAmount = paidAmount;
		this.targetAmount=targetAmount;
		this.difference=difference;
	}

	public ShowJoinedGroupBuyDTO() {
		super();
	}
	
	
}
