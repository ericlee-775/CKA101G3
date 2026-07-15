package com.farmily.product.dto;

import com.farmily.product.model.ProductStatus;

import jakarta.validation.constraints.Min;


/**
 * 修改商品用的 DTO（價格＋上下架狀態）。 設計重點：只放可被修改的欄位 = 一張「白名單」。 入口只開 retailPrice /
 * groupPrice / status，使用者就算用 Postman 也塞不進 name、category 等欄位。 為什麼可以開放改價：團購價在
 * GROUP_BUY、零售價在訂單都另存了快照，改價不會回頭竄改歷史訂單/團購。 三個欄位都允許 null → 對應 PATCH
 * 局部更新：只帶哪個就只改哪個。 驗證：@Min 只在「有帶值」時檢查(null 不會觸發 @Min)；status 是 enum，
 * 傳不存在的狀態值會在 JSON 反序列化時直接失敗，不用另外寫驗證。
 */
public class ProductUpdatedDTO {

	// 無參數建構式(JSON 反序列化會用到)
	public ProductUpdatedDTO() {
	}

	@Min(value = 0, message = "零售價不可為負數")
	private Integer retailPrice; // 零售價

	@Min(value = 0, message = "團購價不可為負數")
	private Integer groupPrice; // 團購價
	
	private ProductStatus status;

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
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

}
