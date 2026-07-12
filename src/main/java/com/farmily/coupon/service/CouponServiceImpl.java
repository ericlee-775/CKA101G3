package com.farmily.coupon.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.coupon.model.CouponRepository;
import com.farmily.coupon.model.CouponVO;






@Service
@Transactional
public class CouponServiceImpl implements CouponService{
	@Autowired
	private CouponRepository couponRepository;
	
	@Override
	public void createCoupon(CouponVO coupon) {
		if(couponRepository.existsById(coupon.getCouponId())) {
			throw new IllegalArgumentException("優惠卷代碼已存在:"+coupon.getCouponId());
		}
		couponRepository.save(coupon);
	}
	
	//admin看
	@Override
	@Transactional(readOnly = true)
	public List<CouponVO> getALLCoupons() {
		return couponRepository.findAll();
	}
	
	
}
