package com.farmily.product.dto;

public class ProductSummaryDTO {
	
	 // ① 一定要保留無參數建構式(JSON 序列化等會用到)
    public ProductSummaryDTO() {
    }

    // ② 新增:給投影查詢用的建構式(參數順序、型別要跟 @Query 完全一致!)
    public ProductSummaryDTO(Integer productId, Integer retailPrice,
                      String unitPricingMeasure, String productName) {
        this.productId = productId;
        this.retailPrice = retailPrice;
        this.unitPricingMeasure = unitPricingMeasure;
        this.productName = productName;
    }

    // ③ 多載建構子（收藏清單用）：比 ② 多帶 description，JPQL 的 new 依參數數量自動挑建構子
    //    ② 的 4 參數版不能刪——熱門商品、複合查詢的 @Query 還在用
    public ProductSummaryDTO(Integer productId, Integer retailPrice,
                      String unitPricingMeasure, String productName, String description) {
        this.productId = productId;
        this.retailPrice = retailPrice;
        this.unitPricingMeasure = unitPricingMeasure;
        this.productName = productName;
        this.description = description;
    }
	
	private Integer productId;
	private Integer retailPrice;
	private String unitPricingMeasure;
	private String productName;
	private String description;
	
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
