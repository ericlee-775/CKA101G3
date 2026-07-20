package com.farmily.coupon.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmily.coupon.model.CouponRepository;
import com.farmily.coupon.model.CouponVO;
import com.farmily.user.model.User;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.EmailService;






@Service
@Transactional
public class CouponServiceImpl implements CouponService{
	@Autowired
	private CouponRepository couponRepository;
	@Autowired 
	private UserRepository userRepository;
	@Autowired
	private EmailService emailService;
	
	@Override
	public void createCoupon(CouponVO coupon, boolean sendMail) {
		
		List<String> errors = new ArrayList<>();
		if(coupon.getCouponId() == null || coupon.getCouponId().isBlank()) {
			errors.add("券代碼不可空白");
		} else if(couponRepository.existsById(coupon.getCouponId())) {
			errors.add("優惠券代碼已存在：" + coupon.getCouponId());
		}
		if(coupon.getAmount() == null || coupon.getAmount() <= 0) {
			errors.add("折抵金額必須大於 0");
		}
		if(coupon.getMinSpending() == null || coupon.getMinSpending() < 0) {
			errors.add("滿額門檻不可為負數");
		}
		if(coupon.getAmount() != null && coupon.getMinSpending() != null
				&& coupon.getAmount() >= coupon.getMinSpending()) {
			errors.add("折抵金額必須低於滿額門檻");
		}
		if(coupon.getIssueStartDate() == null) {
			errors.add("發放開始時間必填");
		}
		if(coupon.getIssueEndDate() == null) {
			errors.add("發放結束時間必填");
		}
		if(coupon.getIssueStartDate() != null && coupon.getIssueEndDate() != null
				&& coupon.getIssueStartDate().isAfter(coupon.getIssueEndDate())) {
			errors.add("發放開始時間不可晚於發放結束時間");
		}
		if(!errors.isEmpty()) {
			throw new IllegalArgumentException(String.join("；", errors));
		}
		couponRepository.save(coupon);

		// 只有勾選「發送通知信」時才寄
		if(sendMail) {
			List<User> users = userRepository.findAll();
			for (User user : users) {
			    emailService.sendCouponEmail(user.getEmail(), user.getUserName(), coupon);
			}
		}
	}
	
	//admin看
	@Override
	@Transactional(readOnly = true)
	public List<CouponVO> getALLCoupons() {
		return couponRepository.findAll();
	}
	
	
}
