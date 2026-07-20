package com.farmily.coupon.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.farmily.coupon.model.CouponVO;
import com.farmily.coupon.service.CouponService;


@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {
	@Autowired
	private CouponService couponService;
	
	@GetMapping
	public String listAll(ModelMap model) {
		model.addAttribute("couponListData",couponService.getALLCoupons());
		return "back-end/admin/Coupon";
	}
	@PostMapping
    public String create(@RequestParam(defaultValue = "false") boolean sendMail,
    					 @RequestParam String couponId,
                         @RequestParam String couponInfo,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issueStartDate,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issueEndDate,
                         @RequestParam Integer amount,
                         @RequestParam Integer minSpending,
                         RedirectAttributes ra) {
             CouponVO coupon = new CouponVO();
             coupon.setCouponId(couponId.trim());
             coupon.setCouponInfo(couponInfo);
             coupon.setAmount(amount);
             coupon.setMinSpending(minSpending);
             // Spring 已用 @DateTimeFormat 幫我們把字串轉成 LocalDateTime，直接塞
             coupon.setIssueStartDate(issueStartDate);
             coupon.setIssueEndDate(issueEndDate);
             try {
                     couponService.createCoupon(coupon,sendMail);
                     ra.addFlashAttribute("success", "已新增優惠券");
                     return "redirect:/admin/coupons";
             } catch (IllegalArgumentException e) {
                     // 驗證錯誤（欄位不合法、券代碼重複…）
                     ra.addFlashAttribute("error", e.getMessage());
             } catch (Exception e) {
                     // 其他非預期錯誤（例如寄信失敗）也走同一個 toast，不要跳白頁
                     ra.addFlashAttribute("error", "發券失敗：" + e.getMessage());
             }
             // 失敗共用：把剛才填的欄位回填，使用者不用整張重打
             ra.addFlashAttribute("form", coupon);
             ra.addFlashAttribute("sendMail", sendMail);
             return "redirect:/admin/coupons";
     }
}
