package com.farmily.groupbuy.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class GroupBuyOrderDTO {
	private Integer orderId;

	private Integer groupBuyId;

	private Integer totalQuantity;

	private Integer groupPrice;

	private Integer totalAmount;

	private String shippingAddress;

	private ShippedStatus shippedStatus;

	private Timestamp shippedAt;

	private Timestamp createdAt;

	private Timestamp receivedAt;

	private OrderStatus orderStatus;

	private PaidStatus paidStatus;

	private Timestamp completedAt;
	private String hostUserName;
	private String hostUserPhone;
	private String hostUserEmail;

	
	public String getHostUserName() {
		return hostUserName;
	}

	public void setHostUserName(String hostUserName) {
		this.hostUserName = hostUserName;
	}

	public String getHostUserPhone() {
		return hostUserPhone;
	}

	public void setHostUserPhone(String hostUserPhone) {
		this.hostUserPhone = hostUserPhone;
	}

	public String getHostUserEmail() {
		return hostUserEmail;
	}

	public void setHostUserEmail(String hostUserEmail) {
		this.hostUserEmail = hostUserEmail;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}



	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Integer totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public Integer getGroupPrice() {
		return groupPrice;
	}

	public void setGroupPrice(Integer groupPrice) {
		this.groupPrice = groupPrice;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}


	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public ShippedStatus getShippedStatus() {
		return shippedStatus;
	}

	public void setShippedStatus(ShippedStatus shippedStatus) {
		this.shippedStatus = shippedStatus;
	}

	public Timestamp getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(Timestamp shippedAt) {
		this.shippedAt = shippedAt;
	}


	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(Timestamp receivedAt) {
		this.receivedAt = receivedAt;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public PaidStatus getPaidStatus() {
		return paidStatus;
	}

	public void setPaidStatus(PaidStatus paidStatus) {
		this.paidStatus = paidStatus;
	}

	public Timestamp getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Timestamp completedAt) {
		this.completedAt = completedAt;
	}

	public GroupBuyOrderDTO() {
		super();
	}

	public Integer getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(Integer groupBuyId) {
		this.groupBuyId = groupBuyId;
	}

	public GroupBuyOrderDTO(Integer orderId, Integer groupBuyId, Integer totalQuantity, Integer groupPrice,
			Integer totalAmount, String shippingAddress, ShippedStatus shippedStatus, Timestamp shippedAt,
			 Timestamp createdAt, Timestamp receivedAt, OrderStatus orderStatus,
			PaidStatus paidStatus, Timestamp completedAt,String hostUserName,String hostUserPhone,String hostUserEmail) {
		super();
		this.orderId = orderId;
		this.groupBuyId = groupBuyId;
		this.totalQuantity = totalQuantity;
		this.groupPrice = groupPrice;
		this.totalAmount = totalAmount;
		this.shippingAddress = shippingAddress;
		this.shippedStatus = shippedStatus;
		this.shippedAt = shippedAt;
		this.createdAt = createdAt;
		this.receivedAt = receivedAt;
		this.orderStatus = orderStatus;
		this.paidStatus = paidStatus;
		this.completedAt = completedAt;
		this.hostUserName=hostUserName;
		this.hostUserPhone=hostUserPhone;
		this.hostUserEmail=hostUserEmail;
	}

	
	
	
}
