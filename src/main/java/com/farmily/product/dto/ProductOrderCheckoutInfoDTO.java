package com.farmily.product.dto;

import com.farmily.user.dto.CityDistrictResponse;

// 用於前端 checkout-info 顯示消費者預設資料 (地址)
public class ProductOrderCheckoutInfoDTO {
	
	private CityDistrictResponse district;
	private String detailAddress;
	
	public ProductOrderCheckoutInfoDTO() {
		super();
	}

	public CityDistrictResponse getDistrict() {
		return district;
	}

	public void setDistrict(CityDistrictResponse district) {
		this.district = district;
	}

	public String getDetailAddress() {
		return detailAddress;
	}

	public void setDetailAddress(String detailAddress) {
		this.detailAddress = detailAddress;
	}
	
	
}
