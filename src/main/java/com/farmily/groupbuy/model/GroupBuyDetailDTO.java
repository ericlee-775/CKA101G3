package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class GroupBuyDetailDTO {
	private Integer productId;
	  private Integer groupBuyId;
	    private String productName;
	    private Integer groupPrice;
	    private Integer targetAmount;
	    private Timestamp openDatetime;
	    private Timestamp ddlDatetime;
	    private String pickupAddress;
	    private GroupBuyStatus status;
	    private Integer retailPrice;

	    
	    
	    public Integer getRetailPrice() {
			return retailPrice;
		}

		public void setRetailPrice(Integer retailPrice) {
			this.retailPrice = retailPrice;
		}

		public Integer getProductId() {
			return productId;
		}

		public void setProductId(Integer productId) {
			this.productId = productId;
		}

		public GroupBuyDetailDTO() {
	    }

	    public GroupBuyDetailDTO(Integer productId,Integer groupBuyId,  String productName,
	                             Integer groupPrice, Integer targetAmount,
	                             Timestamp openDatetime, Timestamp ddlDatetime,
	                             String pickupAddress, GroupBuyStatus status,Integer retailPrice) {
	       this.productId=productId;
	    	this.groupBuyId = groupBuyId;
	        this.productName = productName;
	        this.groupPrice = groupPrice;
	        this.targetAmount = targetAmount;
	        this.openDatetime = openDatetime;
	        this.ddlDatetime = ddlDatetime;
	        this.pickupAddress = pickupAddress;
	        this.status = status;
	        this.retailPrice=retailPrice;
	    }

		public Integer getGroupBuyId() {
			return groupBuyId;
		}

		public void setGroupBuyId(Integer groupBuyId) {
			this.groupBuyId = groupBuyId;
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

		public Integer getTargetAmount() {
			return targetAmount;
		}

		public void setTargetAmount(Integer targetAmount) {
			this.targetAmount = targetAmount;
		}

		public Timestamp getOpenDatetime() {
			return openDatetime;
		}

		public void setOpenDatetime(Timestamp openDatetime) {
			this.openDatetime = openDatetime;
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

		public GroupBuyStatus getStatus() {
			return status;
		}

		public void setStatus(GroupBuyStatus status) {
			this.status = status;
		}
	    
	    
}
