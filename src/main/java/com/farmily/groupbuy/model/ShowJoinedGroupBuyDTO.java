package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class ShowJoinedGroupBuyDTO {
	
	private GroupBuyStatus status;
	
	private Timestamp ddlDatetime; 

	private String pickupAddress;
	
	private String productName;
	
	private Integer buyQty;
	
	private Integer paidAmount;

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
			Integer buyQty, Integer paidAmount) {
		super();
		this.status = status;
		this.ddlDatetime = ddlDatetime;
		this.pickupAddress = pickupAddress;
		this.productName = productName;
		this.buyQty = buyQty;
		this.paidAmount = paidAmount;
	}

	public ShowJoinedGroupBuyDTO() {
		super();
	}
	
	
}
