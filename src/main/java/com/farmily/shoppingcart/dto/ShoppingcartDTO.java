package com.farmily.shoppingcart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ShoppingcartDTO {
	private Integer productId;
    private String  productName;
    private Integer retailPrice;
    private String  unitPricingMeasure;
    @NotNull(message="數量不能為空")
    @Min(value=1,message="數量至少為1")
    @Max(value=99,message="數量過大")
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
