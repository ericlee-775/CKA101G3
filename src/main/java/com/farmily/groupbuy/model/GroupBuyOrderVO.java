package com.farmily.groupbuy.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "gb_order")
public class GroupBuyOrderVO implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Integer orderId;

	@ManyToOne
	@JoinColumn(name = "group_buy_id")
	private GroupBuyVO groupBuyId;

	@Column(name = "total_quantity")
	private Integer totalQuantity;

	@Column(name = "group_price")
	private Integer groupPrice;

	@Column(name = "total_amount")
	private Integer totalAmount;

	


	@Enumerated(EnumType.STRING)
	@Column(name = "shipped_status")
	private ShippedStatus shippedStatus;

	@Column(name = "shipped_at")
	private Timestamp shippedAt;

	

	@Column(name = "created_at")
	private Timestamp createdAt;

	@Column(name = "received_at")
	private Timestamp receivedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status")
	private OrderStatus orderStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "paid_status")
	private PaidStatus paidStatus;

	@Column(name = "completed_at")
	private Timestamp completedAt;

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public GroupBuyVO getGroupBuyId() {
		return groupBuyId;
	}

	public void setGroupBuyId(GroupBuyVO groupBuyId) {
		this.groupBuyId = groupBuyId;
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

	public GroupBuyOrderVO(Integer orderId, GroupBuyVO groupBuyId, Integer totalQuantity, Integer groupPrice,
			Integer totalAmount, ShippedStatus shippedStatus, Timestamp shippedAt,
			String trackingNum, Timestamp createdAt, Timestamp receivedAt, OrderStatus orderStatus,
			PaidStatus paidStatus, Timestamp completedAt) {
		super();
		this.orderId = orderId;
		this.groupBuyId = groupBuyId;
		this.totalQuantity = totalQuantity;
		this.groupPrice = groupPrice;
		this.totalAmount = totalAmount;
	
		this.shippedStatus = shippedStatus;
		this.shippedAt = shippedAt;
		this.createdAt = createdAt;
		this.receivedAt = receivedAt;
		this.orderStatus = orderStatus;
		this.paidStatus = paidStatus;
		this.completedAt = completedAt;
	}

	public GroupBuyOrderVO() {
		super();
	}

}
