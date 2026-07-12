package com.farmily.product.dto;

import java.time.LocalDateTime;

// 用於前端顯示的訂單列表資訊
public class ProductOrderResponseDTO {
	
	private LocalDateTime createdAt; 	// 訂單建立日期
	private Integer orderId;			// 訂單編號
	private Integer discountAmount; 	// 折扣金額
	private Integer finalPayment;		// 實付金額
	private LocalDateTime shippedAt;	// 出貨時間
	private LocalDateTime receivedAt;	// 確認收貨時間;
	private String shippedStatus;		// 配送狀態
	private String orderStatus; 		// 訂單狀態

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

	public LocalDateTime getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}

	public LocalDateTime getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(LocalDateTime receivedAt) {
		this.receivedAt = receivedAt;
	}

	public String getShippedStatus() {
		return shippedStatus;
	}

	public void setShippedStatus(String shippedStatus) {
		this.shippedStatus = shippedStatus;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	
}
