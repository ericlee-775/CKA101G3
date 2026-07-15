package com.farmily.product.dto;

import java.time.LocalDateTime;

//用於會員前端顯示的訂單明細資訊
public class ProductOrderItemResponseDTO {
	
	private String productName;
	private Integer productId;
	private Integer farmerId;
	private Integer price;  	// 單價
	private Integer quantity;	// 購買數量
	private String shippedStatus;
	private LocalDateTime shippedAt;
	private LocalDateTime receivedAt;


	public ProductOrderItemResponseDTO() {
		super();
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public LocalDateTime getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(LocalDateTime receivedAt) {
		this.receivedAt = receivedAt;
	}
	
	
}
