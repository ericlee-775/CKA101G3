package com.farmily.product.dto;

// 用於前端顯示 checkout-info 時的商品清單列表
public class ProductOrderCheckoutItemDTO {
	
	private Integer productId;
	private String productName;
	private Integer price;
	private Integer quantity;
	private Integer itemSubtotal; 	// 該品項小計
	
	public Integer getProductId() {
		return productId;
	}
	public void setProductId(Integer productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
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
	public Integer getItemSubtotal() {
		return itemSubtotal;
	}
	public void setItemSubtotal(Integer itemSubtotal) {
		this.itemSubtotal = itemSubtotal;
	}
	
	
}
