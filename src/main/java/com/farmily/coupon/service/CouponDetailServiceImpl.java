package com.farmily.coupon.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.coupon.model.CouponDetailId;
import com.farmily.coupon.model.CouponDetailRepository;
import com.farmily.coupon.model.CouponDetailVO;
import com.farmily.coupon.model.CouponRepository;
import com.farmily.coupon.model.CouponStatus;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CouponDetailServiceImpl implements CouponDetailService{
	
	@Autowired
	private CouponRepository couponRepository;
	
	@Autowired
	private CouponDetailRepository couponDetailRepository;
	
	@Override
	public boolean claimCoupon(String couponId, Integer userId) {
		// 檢查 1：券要真的存在
        if (!couponRepository.existsById(couponId)) {
                throw new IllegalArgumentException("查無此優惠券");
        }

        // 檢查 2：防重複領 —— 用複合鍵組一個 id 去查有沒有領過
        CouponDetailId id = new CouponDetailId();
        id.setUserId(userId);
        id.setCouponId(couponId);
        if (couponDetailRepository.existsById(id)) {
                return false;   // 已經領過，不重複發
        }

        // 通過 → 新增一筆「我擁有這張券」，狀態 UNUSED（剛領到、還沒用）
        CouponDetailVO detail = new CouponDetailVO();
        detail.setUserId(userId);
        detail.setCouponId(couponId);
        detail.setStatus(CouponStatus.UNUSED);
        couponDetailRepository.save(detail);
        return true;
	}

	@Override
	public List<CouponDetailVO> getMyCoupons(Integer userId) {
		 return couponDetailRepository.findByUserId(userId);
	}
	
}
