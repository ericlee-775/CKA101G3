package com.farmily.product.dto;

import java.time.LocalDateTime;

// 用於會員前端顯示的訂單列表資訊
public class ProductOrderResponseDTO {
	
	private LocalDateTime createdAt; 	// 訂單建立日期
	private Integer orderId;			// 訂單編號
	private Integer totalAmount;		// 總金額
	private Integer discountAmount; 	// 折扣金額
	private Integer shippingFee;		// 運費
	private Integer finalPayment;		// 實付金額

	public ProductOrderResponseDTO() {
		super();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
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
	
	public Integer getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(Integer shippingFee) {
		this.shippingFee = shippingFee;
	}

	public Integer getFinalPayment() {
		return finalPayment;
	}
	
	public void setFinalPayment(Integer finalPayment) {
		this.finalPayment = finalPayment;
	}
	
}
