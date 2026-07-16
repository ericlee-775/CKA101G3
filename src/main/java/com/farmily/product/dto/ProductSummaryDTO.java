package com.farmily.product.dto;

public class ProductSummaryDTO {
	
    public ProductSummaryDTO() {
    }

	private Integer productId;
	private Integer retailPrice;
	private String unitPricingMeasure;
	private String productName;
	private String description;
	private String farmName;
	private Integer farmerId;
	
	public ProductSummaryDTO(Integer productId, Integer retailPrice, String unitPricingMeasure, String productName) {
		super();
		this.productId = productId;
		this.retailPrice = retailPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.productName = productName;
		
	}

	public ProductSummaryDTO(Integer productId, Integer retailPrice, String unitPricingMeasure, String productName,
			String farmName, Integer farmerId) {
		super();
		this.productId = productId;
		this.retailPrice = retailPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.productName = productName;
		this.farmName = farmName;
		this.farmerId = farmerId;
	}
	
	public ProductSummaryDTO(Integer productId, Integer retailPrice, String unitPricingMeasure, String productName,
			String farmName) {
		super();
		this.productId = productId;
		this.retailPrice = retailPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.productName = productName;
		this.farmName = farmName;

	}

	// 收藏清單用（7 參數，多帶 description）：不能做成「4 參數＋description」的 5 參數版，
	// 會跟上面的 5 參數（farmName）版參數型別完全相同而編譯衝突
	public ProductSummaryDTO(Integer productId, Integer retailPrice, String unitPricingMeasure, String productName,
			String farmName, Integer farmerId, String description) {
		super();
		this.productId = productId;
		this.retailPrice = retailPrice;
		this.unitPricingMeasure = unitPricingMeasure;
		this.productName = productName;
		this.farmName = farmName;
		this.farmerId = farmerId;
		this.description = description;
	}
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

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}
	public Integer getFarmerId() {
		return farmerId;
	}
	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}
	
	
}
