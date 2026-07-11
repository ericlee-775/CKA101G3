package com.farmily.groupbuy.model;

public class GroupBuyParticipationDTO {

	private Integer totalAmount;
	
	private Integer totalQuantity;
	
	private Integer participationId;
	
	private Integer groupBuyId;
	
	private String hostName;
	
	private String pickupAddress;

	
	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Integer getParticipationId() {
		return participationId;
	}

	public void setParticipationId(Integer participationId) {
		this.participationId = participationId;
	}

	public Integer getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(Integer groupBuyId) {
		this.groupBuyId = groupBuyId;
	}



	public String getPickupAddress() {
		return pickupAddress;
	}

	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public GroupBuyParticipationDTO(Integer totalAmount, Integer totalQuantity, Integer participationId,
			Integer groupBuyId, String hostName,String pickupAddress) {
		super();
		this.totalAmount = totalAmount;
		this.totalQuantity = totalQuantity;
		this.participationId = participationId;
		this.groupBuyId = groupBuyId;
		this.hostName = hostName;
		this.pickupAddress=pickupAddress;
	}

	public GroupBuyParticipationDTO() {
		super();
	}
	
	
	
}
