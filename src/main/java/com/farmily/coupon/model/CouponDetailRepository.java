package com.farmily.coupon.model;

import java.util.List;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	 //把「未使用但已過期」的券批次標成 EXPIRED（一句 UPDATE 搞定，不用逐筆 save）
	 @Modifying
	 @Query("UPDATE CouponDetailVO cd SET cd.status = com.farmily.coupon.model.CouponStatus.EXPIRED "
	     + "WHERE cd.userId = :userId AND cd.status = com.farmily.coupon.model.CouponStatus.UNUSED "
	     + "AND cd.couponId IN (SELECT c.couponId FROM CouponVO c WHERE c.issueEndDate < :now)")
	 int expireMyCoupons(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}
