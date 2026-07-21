package com.farmily.coupon.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmily.coupon.dto.MyCouponDTO;
import com.farmily.coupon.model.CouponDetailId;
import com.farmily.coupon.model.CouponDetailRepository;
import com.farmily.coupon.model.CouponDetailVO;
import com.farmily.coupon.model.CouponRepository;
import com.farmily.coupon.model.CouponStatus;
import com.farmily.coupon.model.CouponVO;

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
	public List<MyCouponDTO> getMyCoupons(Integer userId) {
		//判斷註冊優惠眷領取
		try{
		
		claimCoupon("WELCOME100",userId);
		}catch (Exception e) {
			
		}
		
		//AI教我的好難
		couponDetailRepository.expireMyCoupons(userId, java.time.LocalDateTime.now());
		// 2. 再撈最新狀態回傳，過期的會顯示 EXPIRED
		return couponDetailRepository.findMyCoupons(userId);
	}

	@Override
	public int useCoupon(Integer userId, String couponId, Integer orderAmount) {
		//卷存在
		CouponVO coupon = couponRepository.findById(couponId).orElseThrow(()->new IllegalArgumentException("查無此優惠卷"));
		//有優惠卷
		CouponDetailId id = new CouponDetailId();
		id.setUserId(userId);
		id.setCouponId(couponId);
		CouponDetailVO detail = couponDetailRepository.findById(id).orElseThrow(()->new IllegalArgumentException("你沒有這張優惠卷"));
		//未使用
		if(detail.getStatus()!=CouponStatus.UNUSED) {
			throw new IllegalArgumentException("此優惠券無法使用已使用或已過期");
				}
		//過期
		if(coupon.getIssueEndDate()!=null && java.time.LocalDateTime.now().isAfter(coupon.getIssueEndDate())) {
			detail.setStatus(CouponStatus.EXPIRED);
			couponDetailRepository.save(detail);
			throw new IllegalArgumentException("優惠卷已過期");
		}
		
		//訂單沒達標
		if (orderAmount == null || orderAmount < coupon.getMinSpending()) {
		    throw new IllegalArgumentException("未達最低消費 " + coupon.getMinSpending() + " 元");
		}
		//不能折到負數
		int discount = Math.min(coupon.getAmount(), orderAmount);
		//標已用
		 detail.setStatus(CouponStatus.USED);
		 couponDetailRepository.save(detail);
		 return discount;
	}
	
	
	
}
