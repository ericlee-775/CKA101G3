package com.farmily.product.dto;

public class ProductSummeryDTO {
	
	 // ① 一定要保留無參數建構式(JSON 序列化等會用到)
    public ProductSummeryDTO() {
    }

    // ② 新增:給投影查詢用的建構式(參數順序、型別要跟 @Query 完全一致!)
    public ProductSummeryDTO(Integer productId, Integer retailPrice,
                      String unitPricingMeasure, String productName) {
        this.productId = productId;
        this.retailPrice = retailPrice;
        this.unitPricingMeasure = unitPricingMeasure;
        this.productName = productName;
    }
	
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
