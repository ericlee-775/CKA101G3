package com.farmily.coupon.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.farmily.coupon.service.CouponDetailService;
import com.farmily.user.event.MemberRegisteredEvent;
import com.farmily.user.model.AccountToken;
import com.farmily.user.repository.UserRepository;
@Component
public class WelcomeCouponListener {
	private static final String WELCOME_COUPON_ID = "WELCOME100";
	@Autowired
	private CouponDetailService couponDetailService;
	@Autowired
	private UserRepository userRepository;   // 事件只帶 email，要靠它換 userId
	
	 @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	    public void onMemberRegistered(MemberRegisteredEvent event) {
	        // 只有會員註冊才送
	        if (event.getAccountType() != AccountToken.AccountType.MEMBER) return;

	        // email → userId → 領券
	        userRepository.findByEmail(event.getEmail()).ifPresent(user -> {
	            try {
	                couponDetailService.claimCoupon(WELCOME_COUPON_ID, user.getUserId());
	            } catch (Exception e) {
	                // 發券失敗絕不能影響註冊，吞掉就好（可加 log）
	            }
	        });
	    }
	}