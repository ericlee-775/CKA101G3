package com.farmily.coupon.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponDetailRepository extends JpaRepository<CouponDetailVO,CouponDetailId>{
	 //查卷
	 List<CouponDetailVO> findByUserId(Integer userId);
}
