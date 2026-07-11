package com.farmily.coupon.model;

// 對齊資料庫 coupon_details.status 的 ENUM('UNUSED','USED','EXPIRED')
// 用 @Enumerated(EnumType.STRING) 存字串，名稱必須跟 DB ENUM 完全一致
public enum CouponStatus {
	UNUSED,    // 未使用（剛領到）
	USED,      // 已使用
	EXPIRED;   // 已過期
}
