package com.farmily.product.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

public class ProductDTO {
	
	private Integer productId;
	private Integer retailPrice;
	private String unitPricingMeasure;
	private String productName;
	
	
	public Integer getProductId() {
		return productId;
	}
	public void setProductId(Integer productId) {
		this.productId = productId;
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
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	
}
