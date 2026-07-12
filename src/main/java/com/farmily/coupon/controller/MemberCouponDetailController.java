package com.farmily.coupon.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.coupon.dto.MyCouponDTO;
import com.farmily.coupon.model.CouponDetailVO;
import com.farmily.coupon.service.CouponDetailService;
import com.farmily.user.security.MemberUserDetails;

@RestController
@RequestMapping("/api/member/coupons")
public class MemberCouponDetailController {
	
	@Autowired
	private CouponDetailService couponDetailService;

    // 領券：POST /api/member/coupons/{couponId}
    @PostMapping("/{couponId}")
    public ResponseEntity<Void> claim(@PathVariable String couponId,
                                      @AuthenticationPrincipal MemberUserDetails me) {
            boolean claimed = couponDetailService.claimCoupon(couponId, me.getUserId());
            // 領到 → 200；已領過 → 409 Conflict
            return claimed ? ResponseEntity.ok().build()
                           : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // 查我的券：GET /api/member/coupons
    @GetMapping
    public ResponseEntity<List<MyCouponDTO>> myCoupons(@AuthenticationPrincipal MemberUserDetails me) {
            return ResponseEntity.ok(couponDetailService.getMyCoupons(me.getUserId()));
    }

}
