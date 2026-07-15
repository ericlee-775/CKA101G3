package com.farmily.coupon.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.farmily.coupon.dto.MyCouponDTO;

public interface CouponDetailRepository extends JpaRepository<CouponDetailVO,CouponDetailId>{
	 //查卷
	 List<CouponDetailVO> findByUserId(Integer userId);
	 
	 @Query("SELECT new com.farmily.coupon.dto.MyCouponDTO("
		     + "c.couponId, c.couponInfo, c.amount, c.minSpending, c.issueEndDate, cd.status) "
		     + "FROM CouponDetailVO cd JOIN CouponVO c ON cd.couponId = c.couponId "
		     + "WHERE cd.userId = :userId")
		List<MyCouponDTO> findMyCoupons(@Param("userId") Integer userId);
}
