package com.farmily.groupbuy.model;

public class GroupBuyReviewDTO {
	
    private RequestStatus requestStatus;
    private String rejectReason;
	public RequestStatus getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(RequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	public GroupBuyReviewDTO(RequestStatus requestStatus, String rejectReason) {
		super();
		this.requestStatus = requestStatus;
		this.rejectReason = rejectReason;
	}
	public GroupBuyReviewDTO() {
		super();
	}

}
