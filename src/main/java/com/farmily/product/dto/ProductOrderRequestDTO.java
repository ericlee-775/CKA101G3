package com.farmily.product.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 接收使用者填寫的訂單資訊
public class ProductOrderRequestDTO {
	
	@NotNull (message = "請選擇縣市區域")
	private Integer districtId;
	
	@NotBlank (message = "請填寫收件地址")
	@Size (max = 80, message = "地址過長")
	private String detailAddress;
	
	// 記錄哪一張小農訂單有用優惠券 Map<farmerId, couponId>
	private Map<Integer, String> coupon = new HashMap<>();

	
	public ProductOrderRequestDTO() {
		super();
	}

	public Integer getDistrictId() {
		return districtId;
	}

	public void setDistrictId(Integer districtId) {
		this.districtId = districtId;
	}

	public String getDetailAddress() {
		return detailAddress;
	}

	public void setDetailAddress(String detailAddress) {
		this.detailAddress = detailAddress;
	}

	public Map<Integer, String> getCoupon() {
		return coupon;
	}

	public void setCoupon(Map<Integer, String> coupon) {
		this.coupon = coupon;
	}

}
