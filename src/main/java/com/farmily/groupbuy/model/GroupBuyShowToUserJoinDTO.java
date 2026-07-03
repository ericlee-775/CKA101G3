package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class GroupBuyShowToUserJoinDTO {
	
	private String productName;
	private Integer target;
	private Integer groupPrice;
	private Timestamp ddlDatetime;
	private String pickupAddress;//前端顯示為貨到時的取件地址
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Integer getTarget() {
		return target;
	}
	public void setTarget(Integer target) {
		this.target = target;
	}
	public Integer getGroupPrice() {
		return groupPrice;
	}
	public void setGroupPrice(Integer groupPrice) {
		this.groupPrice = groupPrice;
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
	public GroupBuyShowToUserJoinDTO(String productName, Integer target, Integer groupPrice, Timestamp ddlDatetime,
			String pickupAddress) {
		super();
		this.productName = productName;
		this.target = target;
		this.groupPrice = groupPrice;
		this.ddlDatetime = ddlDatetime;
		this.pickupAddress = pickupAddress;
	}
	public GroupBuyShowToUserJoinDTO() {
		super();
	}
	
	
	
}
