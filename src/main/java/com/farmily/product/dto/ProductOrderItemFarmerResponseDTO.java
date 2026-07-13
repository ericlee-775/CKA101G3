package com.farmily.product.dto;


//用於小農前端顯示的訂單明細資訊
public class ProductOrderItemFarmerResponseDTO {
	private String productName;
	private Integer productId;
	private Integer price;
	private Integer quantity;
	
	public ProductOrderItemFarmerResponseDTO() {
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
	
	
}
