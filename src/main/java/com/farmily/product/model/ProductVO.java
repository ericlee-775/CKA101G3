package com.farmily.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="product_detail")
public class ProductVO {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="product_id")
	private Integer productId;
	
	@Column(name="sub_cat_class_id")
	private Integer subCatClassId;
	
	@Column(name="farmer_id")
	private Integer farmerId;
	
	@Column(name="retail_price")
	private Integer retailPrice;
	
	@Column(name="group_price")
	private Integer groupPrice;
	
	@Column(name="unit_pricing_measure")
	private String unitPricingMeasure;
	
	@Column(name="product_image")
	private byte[] productImage;
	
	@Column(name="is_group_buy")
	private Boolean isGroupBuy;
	
	@Column(name="description")
	private String description;
	
	@Column(name="status")
	private Status status;
	
	@Column(name="product_name")
	private String productName;

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getSubCatClassId() {
		return subCatClassId;
	}

	public void setSubCatClassId(Integer subCatClassId) {
		this.subCatClassId = subCatClassId;
	}

	public Integer getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
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

	public byte[] getProductImage() {
		return productImage;
	}

	public void setProductImage(byte[] productImage) {
		this.productImage = productImage;
	}

	public Boolean getIsGroupBuy() {
		return isGroupBuy;
	}

	public void setIsGroupBuy(Boolean isGroupBuy) {
		this.isGroupBuy = isGroupBuy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	
}
