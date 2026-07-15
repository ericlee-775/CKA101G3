package com.farmily.product.dto;

import java.util.List;

import com.farmily.coupon.dto.MyCouponDTO;
import com.farmily.user.dto.CityDistrictResponse;

// 用於前端 checkout-info 顯示消費者預設資料 (地址)、購買商品清單、選擇優惠券
public class ProductOrderCheckoutInfoDTO {
	
	private String userName;
	private String phone;
	private CityDistrictResponse district;
	private String detailAddress;
	private List<ProductOrderFarmerGroupDTO> farmerGroup;  	// 訂購明細按農場分組
	private Integer totalAmount;
	private List<MyCouponDTO> usableCoupons; 	// 可用優惠券
	private String recommendedCouponId;
	
	public ProductOrderCheckoutInfoDTO() {
		super();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public List<ProductOrderFarmerGroupDTO> getFarmerGroup() {
		return farmerGroup;
	}

	public void setFarmerGroup(List<ProductOrderFarmerGroupDTO> farmerGroup) {
		this.farmerGroup = farmerGroup;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<MyCouponDTO> getUsableCoupons() {
		return usableCoupons;
	}

	public void setUsableCoupons(List<MyCouponDTO> usableCoupons) {
		this.usableCoupons = usableCoupons;
	}

	public String getRecommendedCouponId() {
		return recommendedCouponId;
	}

	public void setRecommendedCouponId(String recommendedCouponId) {
		this.recommendedCouponId = recommendedCouponId;
	}


}
