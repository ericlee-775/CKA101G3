package com.farmily.coupon.service;

import java.util.List;

import com.farmily.coupon.model.CouponDetailVO;

public interface CouponDetailService {
	 boolean claimCoupon(String couponId, Integer userId);   // 領券（true=領到, false=已領過）
     List<CouponDetailVO> getMyCoupons(Integer userId);       // 查我的券
}
