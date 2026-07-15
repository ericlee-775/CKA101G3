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
                     ra.addFlashAttribute("success", "（已新增優惠券）");
             } catch (IllegalArgumentException e) {
                     ra.addFlashAttribute("error", "（" + e.getMessage() + "）");
             }
             return "redirect:/admin/coupons";
     }
}
