package com.farmily.product.dto;

import java.time.LocalDateTime;
import java.util.List;

//用於小農前端顯示的訂單列表資訊
public class ProductOrderFarmerResponseDTO {
	
	private Integer orderId;
	private Integer userId;
	private String shippingAddress;
	private LocalDateTime createdAt;
	private Integer subtotal;
	private String shippedStatus;
	private LocalDateTime shippedAt;
	private String payoutStatus;
	private List<ProductOrderItemFarmerResponseDTO> items;
	
	public ProductOrderFarmerResponseDTO() {
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

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Integer subtotal) {
		this.subtotal = subtotal;
	}

	public String getShippedStatus() {
		return shippedStatus;
	}

	public void setShippedStatus(String shippedStatus) {
		this.shippedStatus = shippedStatus;
	}

	public LocalDateTime getShippedAt() {
		return shippedAt;
	}

	public void setShippedAt(LocalDateTime shippedAt) {
		this.shippedAt = shippedAt;
	}

	public String getPayoutStatus() {
		return payoutStatus;
	}

	public void setPayoutStatus(String payoutStatus) {
		this.payoutStatus = payoutStatus;
	}

	public List<ProductOrderItemFarmerResponseDTO> getItems() {
		return items;
	}

	public void setItems(List<ProductOrderItemFarmerResponseDTO> items) {
		this.items = items;
	}

	

}
