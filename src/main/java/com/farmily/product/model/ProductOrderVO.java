package com.farmily.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table (name = "orders")
public class ProductOrderVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column (name = "order_id")
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	private Integer orderId;
	
	@Column (name = "user_id")
	private Integer userId;
	
	@Column (name = "recipient_name")
	private String recipientName;
	
	@Column (name = "recipient_phone")
	private String recipientPhone;
	
	@Column (name = "coupon_id")
	private String couponId;
	
	@Column (name = "shipping_address")
	private String shippingAddress;
	
	@Column (name = "payment_id")
	private Integer paymentId;
	
	@Column (name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column (name = "total_amount")
	private Integer totalAmount;
	
	@Column (name = "discount_amount")
	private Integer discountAmount;
	
	@Column (name = "final_payment")
	private Integer finalPayment;

	@Column (name = "order_status")
	@Enumerated (EnumType.STRING)
	private OrderStatus orderStatus;

	@Column (name = "completed_at")
	private LocalDateTime completedAt;
	
	@OneToMany (mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductOrderItemVO> items = new ArrayList<>();

	public ProductOrderVO() {
		super();
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getRecipientPhone() {
		return recipientPhone;
	}

	public void setRecipientPhone(String recipientPhone) {
		this.recipientPhone = recipientPhone;
	}

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public Integer getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Integer paymentId) {
		this.paymentId = paymentId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Integer getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(Integer discountAmount) {
		this.discountAmount = discountAmount;
	}

	public Integer getFinalPayment() {
		return finalPayment;
	}

	public void setFinalPayment(Integer finalPayment) {
		this.finalPayment = finalPayment;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public List<ProductOrderItemVO> getItems() {
		return items;
	}

	public void setItems(List<ProductOrderItemVO> items) {
		this.items = items;
	}
	
	@PrePersist
	protected void onCreate() {
		if (orderStatus == null) {
			orderStatus = OrderStatus.pending;
		}
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (discountAmount == null) {
			discountAmount = 0;
		}
	}
	
}
