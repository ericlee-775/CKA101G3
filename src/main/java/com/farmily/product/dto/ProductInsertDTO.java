 package com.farmily.product.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * 新增商品用的 DTO（白名單）。
 * 只放「該由前端填」的欄位；避免 @ModelAttribute 直接綁 ProductVO 造成 mass assignment。
 * 刻意不放的欄位（由後端決定，不讓 client 控制）：
 *   - productId ：主鍵，DB 自動產生
 *   - farmerId  ：小農 id，應取自登入者，不是 request 帶
 *   - status    ：上下架狀態，由後端給預設值（例如新增一律待審/下架）
 * 圖片用 MultipartFile，所以這支 API 仍走 multipart/form-data。
 */
public class ProductInsertDTO {

    // 無參數建構式(表單繫結會用到)
    public ProductInsertDTO() {
    }

    private Integer subCatClassId;        // 子分類(對應 ProductVO.subCategoryVO 的 sub_cat_class_id)
    private String productName;           // 商品名稱
    private Integer retailPrice;          // 零售價
    private Integer groupPrice;           // 團購價
    private String unitPricingMeasure;    // 計價單位
    private Boolean isGroupBuy;           // 是否開團
    private String description;           // 商品描述
    private MultipartFile productImage;   // 商品圖片

    public Integer getSubCatClassId() {
        return subCatClassId;
    }

    public void setSubCatClassId(Integer subCatClassId) {
        this.subCatClassId = subCatClassId;
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

    public MultipartFile getProductImage() {
        return productImage;
    }

    public void setProductImage(MultipartFile productImage) {
        this.productImage = productImage;
    }

}
