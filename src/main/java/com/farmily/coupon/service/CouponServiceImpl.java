package com.farmily.coupon.service;

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
		// 這兩件事一定會做:檢查代碼重複、存券
		if(couponRepository.existsById(coupon.getCouponId())) {
			throw new IllegalArgumentException("優惠卷代碼已存在:"+coupon.getCouponId());
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
