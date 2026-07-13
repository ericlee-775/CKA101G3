package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class WishListDTO {
	private String hostUser;
	private Integer groupBuyId;
	private Integer retailPrice;// 原價
	private GroupBuyStatus status;
	private Timestamp ddlDatetime;// 團購截止日期
	private String pickupAddress;// 取貨地址
	private String productName;
	private Integer groupPrice;
	private Integer productId;
	private Integer targetAmount;// 達標金額
	private Integer currentAmount;// 目前累積金額
	private Integer difference;//差額
	private boolean favorite;

	public Integer getDifference() {
		return difference;
	}

	public Integer getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(Integer groupBuyId) {
		this.groupBuyId = groupBuyId;
	}

	public void setDifference(Integer difference) {
		this.difference = difference;
	}

	public String getHostUser() {
		return hostUser;
	}

	public void setHostUser(String hostUser) {
		this.hostUser = hostUser;
	}



	public Integer getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(Integer retailPrice) {
		this.retailPrice = retailPrice;
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

	public Integer getGroupPrice() {
		return groupPrice;
	}

	public void setGroupPrice(Integer groupPrice) {
		this.groupPrice = groupPrice;
	}


	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getTargetAmount() {
		return targetAmount;
	}

	public void setTargetAmount(Integer targetAmount) {
		this.targetAmount = targetAmount;
	}

	public Integer getCurrentAmount() {
		return currentAmount;
	}

	public void setCurrentAmount(Integer currentAmount) {
		this.currentAmount = currentAmount;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public WishListDTO(String hostUser,Integer groupBuyId, Integer retailPrice,
			GroupBuyStatus status, Timestamp ddlDatetime, String pickupAddress, String productName,
			Integer groupPrice,Integer productId, Integer targetAmount, Integer currentAmount, Integer difference,boolean favorite) {
		super();
		this.hostUser = hostUser;
		this.groupBuyId=groupBuyId;
		this.retailPrice = retailPrice;
		this.status = status;
		this.ddlDatetime = ddlDatetime;
		this.pickupAddress = pickupAddress;
		this.productName = productName;
		this.groupPrice = groupPrice;
		this.productId = productId;
		this.targetAmount = targetAmount;
		this.currentAmount = currentAmount;
		this.difference=difference;
		this.favorite = favorite;
		
	}

	public WishListDTO() {
		super();
	}

}
