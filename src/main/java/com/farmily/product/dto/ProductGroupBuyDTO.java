package com.farmily.product.dto;

import java.sql.Timestamp;

import com.farmily.groupbuy.model.GroupBuyStatus;

public class ProductGroupBuyDTO {
	 private Integer groupBuyId;
	    private Integer productId;
	    private String productName;
	    private Integer groupPrice;
	    private Integer targetAmount;
	    private Timestamp openDatetime;
	    private Timestamp ddlDatetime;
	    private String pickupAddress;
	    private GroupBuyStatus status;
		public ProductGroupBuyDTO() {
			super();
			// TODO Auto-generated constructor stub
		}
		public Integer getGroupBuyId() {
			return groupBuyId;
		}
		public void setGroupBuyId(Integer groupBuyId) {
			this.groupBuyId = groupBuyId;
		}
		public Integer getProductId() {
			return productId;
		}
		public void setProductId(Integer productId) {
			this.productId = productId;
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
		public ProductGroupBuyDTO(Integer groupBuyId, Integer productId, String productName, Integer groupPrice,
				Integer targetAmount, Timestamp openDatetime, Timestamp ddlDatetime, String pickupAddress,
				GroupBuyStatus status) {
			super();
			this.groupBuyId = groupBuyId;
			this.productId = productId;
			this.productName = productName;
			this.groupPrice = groupPrice;
			this.targetAmount = targetAmount;
			this.openDatetime = openDatetime;
			this.ddlDatetime = ddlDatetime;
			this.pickupAddress = pickupAddress;
			this.status = status;
		}
	 
	 
	
	 
 
}
