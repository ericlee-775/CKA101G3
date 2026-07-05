package com.farmily.groupbuy.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyHostCreateDTO;
import com.farmily.groupbuy.model.GroupBuyShowToUserJoinDTO;
import com.farmily.groupbuy.service.GroupBuyService;
import com.farmily.product.service.ProductService;
import com.farmily.user.security.MemberUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/member/groupBuy")
public class MemberGroupBuyController {

	
	@Autowired
	GroupBuyService groupBuySvc;

	@Autowired
	ProductService productSvc;

	//消費者加入團購
	@PostMapping("/joinGroupBuy/{groupBuyId}")
	public ResponseEntity<String>joinGroupBuy
	(@RequestBody @Valid GroupBuyShowToUserJoinDTO join,
	 @RequestParam Integer productId,@RequestParam Integer groupBuyId, @AuthenticationPrincipal MemberUserDetails me){
		groupBuySvc.joinGroupBuy(join, groupBuyId, me.getUserId());
		return ResponseEntity.ok("參加完成");
	}
	

	// 團購主的發起請求
	@PostMapping("/hostCreate")
	public ResponseEntity<String> createGroupBuy(@RequestBody @Valid GroupBuyHostCreateDTO hostCreate,
			@RequestParam Integer productId, @AuthenticationPrincipal MemberUserDetails me) {
		Integer hostUserId = me.getUserId();
		groupBuySvc.hostRequest(hostCreate, productId, hostUserId);
		return ResponseEntity.ok("申請完成");
	}
	
	

	
}
