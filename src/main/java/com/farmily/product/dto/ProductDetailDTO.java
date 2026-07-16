package com.farmily.product.dto;

public class ProductDetailDTO {

    // 無參數建構式
    public ProductDetailDTO() {
    }

   

    public ProductDetailDTO(Integer productId, String productName, Integer retailPrice, Integer groupPrice,
			String unitPricingMeasure, String description, Boolean isGroupBuy, Integer subCatClassId,
			String subCatClassName, String farmName) {
		super();
		this.productId = productId;
		this.productName = productName;
		this.retailPrice = retailPrice;
		this.groupPrice = groupPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.description = description;
		this.isGroupBuy = isGroupBuy;
		this.subCatClassId = subCatClassId;
		this.subCatClassName = subCatClassName;
		this.farmName = farmName;
	}

	private Integer productId;
    private String productName;
    private Integer retailPrice;
    private Integer groupPrice;
    private String unitPricingMeasure;
    private String description;
    private Boolean isGroupBuy;
    private Integer subCatClassId;      // 子分類 id
    private String subCatClassName;     // 子分類名稱（給詳情頁直接顯示）
    private String farmName;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsGroupBuy() {
        return isGroupBuy;
    }

    public void setIsGroupBuy(Boolean isGroupBuy) {
        this.isGroupBuy = isGroupBuy;
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



	public String getFarmName() {
		return farmName;
	}



	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}
    

}
