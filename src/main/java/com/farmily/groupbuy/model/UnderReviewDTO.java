package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class UnderReviewDTO {
	private Integer groupBuyId;
	
	
	private String productName;
	
	private Integer groupPrice;
	
	private RequestStatus requestStatus;
	
	private Integer targetAmount;//達標金額
	
	private Timestamp openDatetime;
	
	private Timestamp ddlDatetime;
	
	private String rejectReason;
	
	private Timestamp replyDatetime;
	
	private Timestamp requestDatetime;
	

	public Timestamp getRequestDatetime() {
		return requestDatetime;
	}

	public void setRequestDatetime(Timestamp requestDatetime) {
		this.requestDatetime = requestDatetime;
	}

	public Integer getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(Integer groupBuyId) {
		this.groupBuyId = groupBuyId;
	}

	public Timestamp getReplyDatetime() {
		return replyDatetime;
	}

	public void setReplyDatetime(Timestamp replyDatetime) {
		this.replyDatetime = replyDatetime;
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

	public RequestStatus getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(RequestStatus requestStatus) {
		this.requestStatus = requestStatus;
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

	public String getRejectReason() {
		return rejectReason;
	}

	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}

	public UnderReviewDTO(Integer groupBuyId,String productName, Integer groupPrice, RequestStatus requestStatus, Integer targetAmount,
			Timestamp openDatetime, Timestamp ddlDatetime, String rejectReason,Timestamp replyDatetime,Timestamp requestDatetime) {
		super();
		this.groupBuyId=groupBuyId;
		this.productName = productName;
		this.groupPrice = groupPrice;
		this.requestStatus = requestStatus;
		this.targetAmount = targetAmount;
		this.openDatetime = openDatetime;
		this.ddlDatetime = ddlDatetime;
		this.rejectReason = rejectReason;
		this.replyDatetime=replyDatetime;
		this.requestDatetime=requestDatetime;
	}

	public UnderReviewDTO() {
		super();
	}
	
	
	

}
