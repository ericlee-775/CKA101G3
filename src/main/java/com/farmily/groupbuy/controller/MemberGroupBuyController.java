package com.farmily.groupbuy.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmily.groupbuy.model.GroupBuyHostCreateDTO;
import com.farmily.groupbuy.model.GroupBuyShowToUserJoinDTO;
import com.farmily.groupbuy.model.ShowJoinedGroupBuyDTO;
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

	//給會員看的已參加的團購清單(尚未成團)
	@GetMapping("/joinedGroupBuyList")
	public ResponseEntity<List<ShowJoinedGroupBuyDTO>>JoinedList
	(@AuthenticationPrincipal MemberUserDetails me){
		List<ShowJoinedGroupBuyDTO>list=groupBuySvc.showJoinedGroupBuy(me.getUserId());
		return ResponseEntity.ok(list);
	}
	
	
	//給會員中心看的已參加的團購清單(已成訂單)

	
	//消費者加入團購
	@PostMapping("/joinGroupBuy/{groupBuyId}")
	public ResponseEntity<String> joinGroupBuy(
	        @PathVariable Integer groupBuyId,
	        @RequestBody @Valid GroupBuyShowToUserJoinDTO join,
	        @AuthenticationPrincipal MemberUserDetails me) {

	    groupBuySvc.joinGroupBuy(join, groupBuyId, me.getUserId());
	    return ResponseEntity.ok("參加完成");
	}

	// 團購主的發起請求
	@PostMapping("/hostCreate/{productId}")
	public ResponseEntity<String> createGroupBuy(@RequestBody @Valid GroupBuyHostCreateDTO hostCreate,
			 @PathVariable Integer productId, @AuthenticationPrincipal MemberUserDetails me) {
		Integer hostUserId = me.getUserId();
		groupBuySvc.hostRequest(hostCreate, productId, hostUserId);
		return ResponseEntity.ok("申請完成");
	}
	
	

	
}
