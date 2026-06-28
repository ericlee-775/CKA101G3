package com.farmily.product.dto;

/**
 * 商品詳情頁用的 DTO。
 * 用建構式投影（SELECT new ...）一次撈齊詳情頁要的欄位：
 *   - 不含圖片 byte[]（圖片走 GET /api/products/{id}/image，不塞進 JSON）
 *   - 分類用 LEFT JOIN 帶出 subCatClassId + subCatClassName（沒分類也不會漏掉商品）
 * 前端要顯示圖片時，用 productId 組網址：/api/products/{productId}/image
 */
public class ProductDetailDTO {

    // 無參數建構式
    public ProductDetailDTO() {
    }

    // 投影查詢用的建構式（參數順序、型別要跟 @Query 完全一致！）
    public ProductDetailDTO(Integer productId, String productName, Integer retailPrice,
                            Integer groupPrice, String unitPricingMeasure, String description,
                            Boolean isGroupBuy, Integer subCatClassId, String subCatClassName) {
        this.productId = productId;
        this.productName = productName;
        this.retailPrice = retailPrice;
        this.groupPrice = groupPrice;
        this.unitPricingMeasure = unitPricingMeasure;
        this.description = description;
        this.isGroupBuy = isGroupBuy;
        this.subCatClassId = subCatClassId;
        this.subCatClassName = subCatClassName;
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

}
