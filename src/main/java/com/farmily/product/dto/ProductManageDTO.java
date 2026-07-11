package com.farmily.product.dto;

import com.farmily.product.model.ProductStatus;

public class ProductManageDTO {

	public ProductManageDTO() {

	}

	public ProductManageDTO(Integer productId, String productName, Integer retailPrice, Integer groupPrice,
			String unitPricingMeasure, ProductStatus status) {

		this.productId = productId;
		this.productName = productName;
		this.retailPrice = retailPrice;
		this.groupPrice = groupPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.status = status;

	}

	private Integer productId;
	private String productName;
	private Integer retailPrice;
	private Integer groupPrice;
	private String unitPricingMeasure;
	private ProductStatus status;

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

	public Integer getGroupPrice() {
		return groupPrice;
	}

	public void setGroupPrice(Integer groupPrice) {
		this.groupPrice = groupPrice;
	}

	public String getUnitPricingMeasure() {
		return unitPricingMeasure;
	}

	public void setUnitPricingMeasure(String unitPricingMeasure) {
		this.unitPricingMeasure = unitPricingMeasure;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
	}

}
