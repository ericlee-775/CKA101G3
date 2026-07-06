package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class GroupBuyFarmerDTO {

	private Integer groupBuyId;
	private Integer productId;
	private String productName;
	private Integer groupPrice;
	private String hostUserName;
	private Integer targetAmount;
	private Timestamp openDatetime;
	private Timestamp ddlDatetime;
	private String pickupAddress;
	private RequestStatus requestStatus;
	private Timestamp requestDatetime;
	private GroupBuyStatus status;
	private Timestamp replyDatetime;
	private String rejectReason;
	
	
	public GroupBuyFarmerDTO(Integer groupBuyId, Integer productId, String productName, Integer groupPrice,
			String hostUserName, Integer targetAmount, Timestamp openDatetime, Timestamp ddlDatetime,
			String pickupAddress, RequestStatus requestStatus, Timestamp requestDatetime, GroupBuyStatus status,
			Timestamp replyDatetime, String rejectReason) {
		super();
		this.groupBuyId = groupBuyId;
		this.productId = productId;
		this.productName = productName;
		this.groupPrice = groupPrice;
		this.hostUserName = hostUserName;
		this.targetAmount = targetAmount;
		this.openDatetime = openDatetime;
		this.ddlDatetime = ddlDatetime;
		this.pickupAddress = pickupAddress;
		this.requestStatus = requestStatus;
		this.requestDatetime = requestDatetime;
		this.status = status;
		this.replyDatetime = replyDatetime;
		this.rejectReason = rejectReason;
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
	public String getHostUserName() {
		return hostUserName;
	}
	public void setHostUserName(String hostUserName) {
		this.hostUserName = hostUserName;
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
	public RequestStatus getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(RequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}
	public Timestamp getRequestDatetime() {
		return requestDatetime;
	}
	public void setRequestDatetime(Timestamp requestDatetime) {
		this.requestDatetime = requestDatetime;
	}
	public GroupBuyStatus getStatus() {
		return status;
	}
	public void setStatus(GroupBuyStatus status) {
		this.status = status;
	}
	public Timestamp getReplyDatetime() {
		return replyDatetime;
	}
	public void setReplyDatetime(Timestamp replyDatetime) {
		this.replyDatetime = replyDatetime;
	}
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}


}
