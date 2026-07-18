package com.farmily.product.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 接收使用者填寫的訂單資訊
public class ProductOrderRequestDTO {
	
	@NotBlank (message = "請填寫收件人姓名")
	@Size (max = 50, message = "姓名過長")
	private String recipientName;

	@NotBlank (message = "請填寫收件人電話")
	@Pattern (regexp = "^09\\d{8}$", message = "手機格式錯誤 (09 開頭共 10 碼)")
	private String recipientPhone;
	
	@NotNull (message = "請選擇縣市區域")
	private Integer districtId;
	
	@NotBlank (message = "請填寫收件地址")
	@Size (max = 80, message = "地址過長")
	private String detailAddress;
	
	private String coupon;
	
//	// 記錄哪一張小農訂單有用優惠券 Map<farmerId, couponId>
//	private Map<Integer, String> coupon = new HashMap<>();


	
	public ProductOrderRequestDTO() {
		super();
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getRecipientPhone() {
		return recipientPhone;
	}

	public void setRecipientPhone(String recipientPhone) {
		this.recipientPhone = recipientPhone;
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

	public String getCoupon() {
		return coupon;
	}

	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}

	
	

//	public Map<Integer, String> getCoupon() {
//		return coupon;
//	}
//
//	public void setCoupon(Map<Integer, String> coupon) {
//		this.coupon = coupon;
//	}

}
