package com.farmily.trip.dto;

public class TripReviewRequest {
	
	// 管理員審核：通過或退回，附上意見
	    private Boolean approved;   // true = 通過上架, false = 退回
	    private String comment;     // 審核意見（退回時說明原因）

	    public Boolean getApproved() { return approved; }
	    public void setApproved(Boolean approved) { this.approved = approved; }

	    public String getComment() { return comment; }
	    public void setComment(String comment) { this.comment = comment; }
	}

