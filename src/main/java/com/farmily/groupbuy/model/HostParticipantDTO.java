package com.farmily.groupbuy.model;

import java.sql.Timestamp;

public class HostParticipantDTO {
	  private Integer userId;
	    private String userName;
	    private String userPhoneNum;
	    private String userEmail;

	    private Integer buyQty;
	    private Integer paidAmount;

	    private Timestamp joinDatetime;
	    private Timestamp paidDatetime;

	    private Boolean isHost;

	    public HostParticipantDTO() {
	    }

		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getUserPhoneNum() {
			return userPhoneNum;
		}

		public void setUserPhoneNum(String userPhoneNum) {
			this.userPhoneNum = userPhoneNum;
		}

		public String getUserEmail() {
			return userEmail;
		}

		public void setUserEmail(String userEmail) {
			this.userEmail = userEmail;
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

		public Timestamp getJoinDatetime() {
			return joinDatetime;
		}

		public void setJoinDatetime(Timestamp joinDatetime) {
			this.joinDatetime = joinDatetime;
		}

		public Timestamp getPaidDatetime() {
			return paidDatetime;
		}

		public void setPaidDatetime(Timestamp paidDatetime) {
			this.paidDatetime = paidDatetime;
		}

		public Boolean getIsHost() {
			return isHost;
		}

		public void setIsHost(Boolean isHost) {
			this.isHost = isHost;
		}

		public HostParticipantDTO(Integer userId, String userName, String userPhoneNum, String userEmail, Integer buyQty,
				Integer paidAmount, Timestamp joinDatetime, Timestamp paidDatetime, Boolean isHost) {
			super();
			this.userId = userId;
			this.userName = userName;
			this.userPhoneNum = userPhoneNum;
			this.userEmail = userEmail;
			this.buyQty = buyQty;
			this.paidAmount = paidAmount;
			this.joinDatetime = joinDatetime;
			this.paidDatetime = paidDatetime;
			this.isHost = isHost;
		}
	    
}
