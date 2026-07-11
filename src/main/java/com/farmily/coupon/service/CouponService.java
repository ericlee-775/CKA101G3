package com.farmily.coupon.service;

import java.util.List;

import com.farmily.coupon.model.CouponVO;

public interface CouponService {
	void createCoupon(CouponVO coupon);
	List<CouponVO> getALLCoupons();
}
