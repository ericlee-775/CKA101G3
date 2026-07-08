package com.farmily.shoppingcart.dto;

public class ShoppingcartDTO {
	private Integer productId;
    private String  productName;
    private Integer retailPrice;
    private String  unitPricingMeasure;
    private Integer quantity;


	public ShoppingcartDTO() {
		super();
	}
	public ShoppingcartDTO(Integer productId, String productName, Integer retailPrice, String unitPricingMeasure, Integer quantity) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.retailPrice = retailPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.quantity = quantity;
	}
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
	public Integer getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(Integer retailPrice) {
		this.retailPrice = retailPrice;
	}
	public String getUnitPricingMeasure() {
		return unitPricingMeasure;
	}
	public void setUnitPricingMeasure(String unitPricingMeasure) {
		this.unitPricingMeasure = unitPricingMeasure;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
    
    
}
