package com.farmily.product.dto;

public class ProductGroupBuyDTO {
	 private Integer productId;
	 private String productName;
	 private Integer groupPrice;
	 private String unitPricingMeasure;
	 private String description;
	 private Integer subCatClassId;      // 子分類 id
	 private String subCatClassName;     // 子分類名稱（給詳情頁直接顯示）
	 
	 
	 public ProductGroupBuyDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	 
	 public ProductGroupBuyDTO(Integer productId, String productName, Integer groupPrice, String unitPricingMeasure,
			String description, Integer subCatClassId, String subCatClassName) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.groupPrice = groupPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.description = description;
		this.subCatClassId = subCatClassId;
		this.subCatClassName = subCatClassName;
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
	 public String getDescription() {
		 return description;
	 }
	 public void setDescription(String description) {
		 this.description = description;
	 }
	 public Integer getSubCatClassId() {
		 return subCatClassId;
	 }
	 public void setSubCatClassId(Integer subCatClassId) {
		 this.subCatClassId = subCatClassId;
	 }
	 public String getSubCatClassName() {
		 return subCatClassName;
	 }
	 public void setSubCatClassName(String subCatClassName) {
		 this.subCatClassName = subCatClassName;
	 }
	 
	 
 
}
